package com.myAuth.authenticationSystem.controllers;

import com.myAuth.authenticationSystem.Security.CookieService;
import com.myAuth.authenticationSystem.Security.JwtService;
import com.myAuth.authenticationSystem.dtos.LoginRequest;
import com.myAuth.authenticationSystem.dtos.RefreshTokenRequest;
import com.myAuth.authenticationSystem.dtos.TokenResponse;
import com.myAuth.authenticationSystem.dtos.UserDto;
import com.myAuth.authenticationSystem.entities.RefreshToken;
import com.myAuth.authenticationSystem.entities.User;
import com.myAuth.authenticationSystem.repositories.RefreshTokenRepository;
import com.myAuth.authenticationSystem.repositories.UserRepository;
import com.myAuth.authenticationSystem.services.AuthService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    public final AuthService authService;
    public final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieService cookieService;

    @PostMapping
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        System.out.println("login hit");
        Authentication authenticate =  authenticate(loginRequest);
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(() -> new BadCredentialsException("User not found"));

        if(!user.isEnabled()) {
            throw new DisabledException("User is not enabled");
        }

        String jti = UUID.randomUUID().toString();
        var refreshTokenOb = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenOb);


        String accessToken =  jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, refreshTokenOb.getJti());

        cookieService.attachRefreshCookie(response, refreshToken, (int)jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);

       TokenResponse tokenResponse =  TokenResponse.of(accessToken, "", jwtService.getAccessTtlSeconds(), modelMapper.map(user, UserDto.class));
            return ResponseEntity.ok(tokenResponse);

}

    private Authentication authenticate(LoginRequest loginRequest) {
        try{
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );
    } catch (Exception e) {
            throw new RuntimeException("Invalid credentials");
        }


    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestBody(required = false)RefreshTokenRequest body,
            HttpServletResponse response,
            HttpServletRequest request
            ){
        String refreshToken = readRefreshTokenFromRequest(body, request).orElseThrow(()-> new BadCredentialsException("Refresh token is required"));

        if(!jwtService.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String jti = jwtService.getJti(refreshToken);
        UUID userId = jwtService.getUserId(refreshToken);
        RefreshToken storedRefreshToken = refreshTokenRepository.findByJti(jti).orElseThrow(() -> new BadCredentialsException("Refresh token not found"));

        if(storedRefreshToken.isRevoked() ) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        if(storedRefreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token expired");
        }
        if (!storedRefreshToken.getUser().getId().equals(userId)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        // Refresh Token Rotation
        storedRefreshToken.setRevoked(true);
        String newJti = UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJti);
        refreshTokenRepository.save(storedRefreshToken);

        User user = storedRefreshToken.getUser();

        var newRefreshTokenObj = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newRefreshTokenObj);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user, newRefreshTokenObj.getJti());

        cookieService.attachRefreshCookie(response, newRefreshToken, (int) jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);

        return ResponseEntity.ok(TokenResponse.of(newAccessToken, newRefreshToken, jwtService.getRefreshTtlSeconds(), modelMapper.map(user, UserDto.class)));

    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {

        readRefreshTokenFromRequest(null, request).ifPresent(token -> {
            try {
               if (jwtService.isRefreshToken(token)) {
                   String jti = jwtService.getJti(token);
                   refreshTokenRepository.findByJti(jti).ifPresent(refreshToken -> {
                       refreshToken.setRevoked(true);
                       refreshTokenRepository.save(refreshToken);
                   });
               }
            } catch (JwtException ignored ) {
                // log token parsing error
            }
        });
        cookieService.clearRefreshCookie(response);
        cookieService.addNoStoreHeaders(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
        if(request.getCookies() != null) {
           Optional<String> fromCookie = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals("refreshToken"))
                    .map(Cookie::getValue)
                    .filter(value -> value != null && !value.isBlank())
                    .findFirst();
            if (fromCookie.isPresent()) {
                return fromCookie;
            }


        }

        if(body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
            return Optional.of(body.refreshToken());
        }
        String refresHeader = request.getHeader("X-refresh-Token");
        if(refresHeader != null && !refresHeader.isBlank()) {
            return Optional.of(refresHeader);
        }

        String AuthHeader = request.getHeader("Authorization");
        if(AuthHeader != null && AuthHeader.regionMatches(true,0,"Bearer ",0,7 )) {
            String token = AuthHeader.substring(7).trim();
            if (!token.isBlank()) {
                try{
                    if(jwtService.isRefreshToken(token)) {
                        return Optional.of(token);
                    }
                } catch (Exception e) {
                    // log token parsing error
                }
            }
        }
        return Optional.empty();
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
        // Implement registration logic here

    }
}

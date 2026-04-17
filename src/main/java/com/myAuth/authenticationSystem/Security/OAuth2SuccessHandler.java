package com.myAuth.authenticationSystem.Security;


import com.myAuth.authenticationSystem.config.AppConstants;
import com.myAuth.authenticationSystem.entities.Provider;
import com.myAuth.authenticationSystem.entities.RefreshToken;
import com.myAuth.authenticationSystem.entities.Role;
import com.myAuth.authenticationSystem.entities.User;
import com.myAuth.authenticationSystem.repositories.RefreshTokenRepository;
import com.myAuth.authenticationSystem.repositories.RoleRepository;
import com.myAuth.authenticationSystem.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository roleRepository;

    @Value("${app.auth.frontend.success-redirect}")
    private String frontEndSuccessUrl ;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        logger.info("OAuth2 authentication successful");
        logger.info(authentication.toString());

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = "unknown";
        if(authentication instanceof OAuth2AuthenticationToken token) {
            registrationId = token.getAuthorizedClientRegistrationId();
        }

        logger.info("Registration ID: " + registrationId);
        logger.info("User attributes: " + oAuth2User.getAttributes().toString());

        User user;
        Role guestRole = roleRepository.findByName(AppConstants.GUEST_ROLE)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        switch (registrationId) {
            case "google" -> {
                String googleId = oAuth2User.getAttributes().getOrDefault("sub", "").toString();
                String email = oAuth2User.getAttributes().getOrDefault("email", "").toString();
                String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();
                String image = oAuth2User.getAttributes().getOrDefault("picture", "").toString();

                user = userRepository.findByEmail(email).orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .name(name)
                            .image(image)
                            .provider(Provider.GOOGLE)
                            .providerId(googleId)
                            .enabled(true)
                            .roles(Set.of(guestRole))
                            .build();
                    return userRepository.save(newUser);
                });
            }
            case "github" -> {
                String githubId = oAuth2User.getAttributes().getOrDefault("id", "").toString();

                String name = oAuth2User.getAttributes().getOrDefault("login", "").toString();
                String image = oAuth2User.getAttributes().getOrDefault("avatar_url", "").toString();
                String email = (String) oAuth2User.getAttributes().get("email");
                if (email == null){
                    email = name + "@github.com";
                }
                final String finalEmail = email;
                user = userRepository.findByEmail(email).orElseGet(() -> {
                    User newUser = User.builder()
                            .email(finalEmail)
                            .name(name)
                            .image(image)
                            .provider(Provider.GITHUB)
                            .providerId(githubId)
                            .enabled(true)
                            .roles(Set.of(guestRole))
                            .build();
                    return userRepository.save(newUser);
                });
            }

                default -> {
                    throw new RuntimeException("Invalid registration id");
                }
        }
        String jti = UUID.randomUUID().toString();
        RefreshToken refreshTokenObj =RefreshToken.builder().jti(jti)
                .user(user).revoked(false)
                        .createdAt( Instant.now())
                                .expiresAt(java.time.Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                                        .build();

        refreshTokenRepository.save(refreshTokenObj);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, refreshTokenObj.getJti());
        cookieService.attachRefreshCookie(response, refreshToken, (int)jwtService.getRefreshTtlSeconds());



        response.sendRedirect(frontEndSuccessUrl );
    }
}

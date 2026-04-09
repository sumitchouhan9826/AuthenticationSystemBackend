package com.myAuth.authenticationSystem.Security;

import com.myAuth.authenticationSystem.helpers.UserHelper;
import com.myAuth.authenticationSystem.repositories.UserRepository;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final CustomUserDetailService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        logger.info("Authorization header: {}", header);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);


            try {
                if(!jwtService.isAccessToken(token)){
                    filterChain.doFilter(request, response);
                    return ;
                }
                Jws<Claims> parse = jwtService.parse(token);


                Claims payload = parse.getPayload();


                String userId = payload.getSubject();
                UUID userUuid = UserHelper.parseUUID(userId);

                userRepository.findById(userUuid)
                        .ifPresent(user -> {

                            if(user.isEnabled()){
                                List<GrantedAuthority> authorities = user.getRoles()==null ? List.of():user.getRoles()
                                        .stream()
                                        .map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList()) ;

                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                user.getEmail(),
                                                null,
                                                authorities
                                        );

                                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                                if(SecurityContextHolder.getContext().getAuthentication() == null)
                                    SecurityContextHolder.getContext().setAuthentication(authentication);
                            }


                });





            } catch (ExpiredJwtException  e) {
               request.setAttribute("error", "Expired token ");
            }
            catch (MalformedJwtException e) {
//                e.printStackTrace();
                request.setAttribute("error", "Invalid token format");
            }
            catch (JwtException e) {
//                e.printStackTrace();
                request.setAttribute("error", "Invalid token");
            }
            catch(Exception e){
//                e.printStackTrace();
                request.setAttribute("error", "Authentication error: " + e.getMessage());
            }

        }
        filterChain.doFilter(request, response);
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        return path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/refresh")
                || path.equals("/api/v1/auth/logout");
    }
}
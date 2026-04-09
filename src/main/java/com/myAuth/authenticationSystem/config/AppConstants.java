package com.myAuth.authenticationSystem.config;

import org.springframework.boot.CommandLineRunner;

public class AppConstants {
    public static final String[]  AUTH_PUBLIC_URLS = {
            "/api/v1/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
};
    public static final String ADMIN_ROLE = "ADMIN";
    public static final String GUEST_ROLE = "GUEST";







}

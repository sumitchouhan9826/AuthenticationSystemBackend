package com.myAuth.authenticationSystem.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;


@OpenAPIDefinition(
        info = @Info(
                title = "Authentication System API",
                description = "API documentation for the Authentication System",
                contact = @Contact(
                        name = "Sumit Chouhan",
                        email = "sumit45807@gmail.com",
                        url = "https://www.substringtechnologies.com/"
                ),
                version = "1.0",
                summary = "API documentation for the Authentication System"

        ),
        security = @SecurityRequirement(name = "Bearer Authentication")
)

@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)

public class ApiDocConfig {
}

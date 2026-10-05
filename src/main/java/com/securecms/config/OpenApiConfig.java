package com.securecms.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Secure Content Management System API",
                version = "1.0.0",
                description = "Professional Spring Boot backend combining REST, JWT, RBAC, AES, Caching, Pagination, N+1 solution and more.",
                contact = @Contact(name = "College Project", email = "student@example.com")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local Development Server")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter JWT access token (obtained from /api/auth/login)"
)
public class OpenApiConfig {
}

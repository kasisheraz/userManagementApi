package com.fincore.usermgmt.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${spring.application.name:user-management-api}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("FinCore User Management API")
                        .description("""
                                Complete API for User Management and Organisation Onboarding
                                
                                ## Features
                                * User Management (CRUD operations)
                                * Authentication with OTP
                                * Role-Based Access Control
                                * Organisation Onboarding
                                * KYC Document Management
                                * Address Management
                                * Questionnaire Management
                                
                                ## Authentication
                                This API uses JWT Bearer token authentication. 
                                To authenticate:
                                1. Request OTP for phone number using `/api/auth/request-otp`
                                2. Verify OTP using `/api/auth/verify-otp` to get access token
                                3. Use the access token in the Authorization header for subsequent requests
                                
                                ## Security
                                Protected endpoints require a valid JWT token in the Authorization header:
                                ```
                                Authorization: Bearer <your_token>
                                ```
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FinCore Development Team")
                                .email("dev@fincore.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://fincore.com/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://fincore-npe-api-994490239798.europe-west2.run.app")
                                .description("NPE Environment (GCP Cloud Run)"),
                        new Server()
                                .url("https://fincore-prod-api.example.com")
                                .description("Production Environment")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token obtained from /api/auth/verify-otp endpoint")));
    }
}

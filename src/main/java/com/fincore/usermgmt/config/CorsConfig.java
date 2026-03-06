package com.fincore.usermgmt.config;
// ...existing code...

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns(
                "http://localhost:*",              // Allow any localhost port
                "https://localhost:*",
                "http://127.0.0.1:*",
                "https://127.0.0.1:*",
                "https://*.run.app",               // Allow any Cloud Run app
                "https://*.web.app",               // Allow Firebase Hosting
                "https://*.firebaseapp.com",
                "*"                                 // Allow all origins (NPE only)
            )
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
            .allowedHeaders("*")
            .exposedHeaders("Authorization", "Content-Type")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
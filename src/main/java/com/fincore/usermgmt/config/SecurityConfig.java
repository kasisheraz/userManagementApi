package com.fincore.usermgmt.config;

import com.fincore.usermgmt.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> {})  // Enable CORS using CorsConfig
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                // User endpoints
                .requestMatchers("/api/users/**").authenticated()
                // Role endpoints
                .requestMatchers("/api/roles/**").authenticated()
                // Organisation endpoints
                .requestMatchers("/api/organisations/**").authenticated()
                // Address endpoints
                .requestMatchers("/api/addresses/**").authenticated()
                // KYC Document endpoints
                .requestMatchers("/api/kyc-documents/**").authenticated()
                // Phase 2 API v1 endpoints
                .requestMatchers("/api/v1/questions/**").authenticated()
                .requestMatchers("/api/v1/questionnaires/**").authenticated()
                .requestMatchers("/api/v1/answers/**").authenticated()
                .requestMatchers("/api/v1/kyc-verification/**").authenticated()
                .requestMatchers("/api/v1/aml-screening/**").authenticated()
                // Catch-all - require authentication for everything else
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // For H2 Console
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

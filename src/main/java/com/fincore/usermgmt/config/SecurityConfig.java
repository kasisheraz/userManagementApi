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
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                // User endpoints
                .requestMatchers(HttpMethod.GET, "/api/users/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/users/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/users/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").authenticated()
                // Organisation endpoints
                .requestMatchers(HttpMethod.GET, "/api/organisations/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/organisations/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/organisations/**").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/organisations/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/organisations/**").authenticated()
                // Address endpoints
                .requestMatchers(HttpMethod.GET, "/api/addresses/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/addresses/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/addresses/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/addresses/**").authenticated()
                // KYC Document endpoints
                .requestMatchers(HttpMethod.GET, "/api/kyc-documents/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/kyc-documents/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/kyc-documents/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/kyc-documents/**").authenticated()
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

package com.fincore.usermgmt.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Test configuration for mail-related beans to support integration tests.
 * Provides mock beans for JavaMailSender and SpringTemplateEngine.
 */
@TestConfiguration
public class TestMailConfig {

    /**
     * Provides a basic JavaMailSender implementation for tests.
     * This bean will not actually send emails but allows the application context to load.
     */
    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(1025); // Fake SMTP port
        mailSender.setUsername("test@example.com");
        mailSender.setPassword("password");
        return mailSender;
    }

    /**
     * Provides a basic SpringTemplateEngine for tests.
     * Configured with minimal settings for email template processing.
     */
    @Bean
    @Primary
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }
}

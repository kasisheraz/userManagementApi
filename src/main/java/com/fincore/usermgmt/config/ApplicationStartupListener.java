package com.fincore.usermgmt.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@Slf4j
public class ApplicationStartupListener {

    private final DataSource dataSource;

    public ApplicationStartupListener(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("========================================");
        log.info("Application started successfully!");
        log.info("========================================");
        
        // Test database connection
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                log.info("✓ Database connection is healthy");
            }
        } catch (Exception e) {
            log.error("✗ Database connection failed: {}", e.getMessage());
        }
        
        log.info("Application is ready to accept requests");
        log.info("========================================");
    }
}

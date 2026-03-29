package com.fincore.usermgmt.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway configuration that repairs any failed migrations before running pending ones.
 * This is needed to recover from a failed V3.0 migration state in the NPE database,
 * and ensures idempotent startup behavior for all environments.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy repairAndMigrate() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}

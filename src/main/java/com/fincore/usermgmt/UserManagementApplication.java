package com.fincore.usermgmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@EnableScheduling
public class UserManagementApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(UserManagementApplication.class);
        // Optimize startup
        System.setProperty("spring.devtools.restart.enabled", "false");
        app.run(args);
    }
}

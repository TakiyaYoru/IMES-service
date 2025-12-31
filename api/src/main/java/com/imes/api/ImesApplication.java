package com.imes.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for IMES (Intern Management & Evaluation System)
 * 
 * @author IMES Team
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.imes.api",
    "com.imes.core",
    "com.imes.infra",
    "com.imes.common"
})
@EnableJpaRepositories(basePackages = "com.imes.infra.repository")
@EntityScan(basePackages = "com.imes.infra.entity")
public class ImesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImesApplication.class, args);
    }
}

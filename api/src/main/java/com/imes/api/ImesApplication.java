package com.imes.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

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
public class ImesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImesApplication.class, args);
    }
}

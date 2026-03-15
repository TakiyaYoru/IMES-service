package com.imes.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Auth Service - Authentication & Authorization Microservice
 * 
 * Responsibilities:
 * - User login/logout
 * - JWT token generation and validation
 * - Password authentication
 * 
 * Port: 8081
 * Eureka: Registers as AUTH-SERVICE
 * 
 * @author IMES Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.imes.auth", "com.imes.core", "com.imes.infra"})
@EntityScan(basePackages = "com.imes.infra.entity")
@EnableJpaRepositories(basePackages = "com.imes.infra.repository")
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

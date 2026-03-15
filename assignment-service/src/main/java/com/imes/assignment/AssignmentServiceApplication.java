package com.imes.assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.imes.assignment", "com.imes.common"})
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.imes.assignment.entity"})
@EnableJpaRepositories(basePackages = {"com.imes.assignment.repository"})
public class AssignmentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssignmentServiceApplication.class, args);
    }
}

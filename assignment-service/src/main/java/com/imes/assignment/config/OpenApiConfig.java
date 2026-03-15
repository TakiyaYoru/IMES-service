package com.imes.assignment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI assignmentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IMES Assignment Management API")
                        .description("Quan ly nhiem vu task-based, workflow chuyen trang thai, checklist, comment, attachment va time log")
                        .version("1.0.0"))
                .servers(List.of(new Server().url("http://localhost:8085").description("Assignment Service")));
    }
}

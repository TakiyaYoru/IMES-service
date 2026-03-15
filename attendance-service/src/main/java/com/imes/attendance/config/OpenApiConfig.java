package com.imes.attendance.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI attendanceServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("IMES Attendance & Evaluation API")
                        .description("API phục vụ chấm công, phân tích attendance và evaluation cho demo khóa luận")
                        .version("1.0")
                        .contact(new Contact()
                                .name("IMES Team")
                                .email("support@imes.local")))
                .servers(List.of(
                        new Server().url("http://localhost:8084").description("Attendance Service")
                ));
    }
}

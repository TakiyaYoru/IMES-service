package com.imes.intern.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI internServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IMES Intern Management API")
                        .description("Quan ly ho so thuc tap sinh, phan cong mentor va vong doi thuc tap")
                        .version("1.0.0"))
                .servers(List.of(new Server().url("http://localhost:8083").description("Intern Service")));
    }
}

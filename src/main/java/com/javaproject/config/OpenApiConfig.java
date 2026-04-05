package com.javaproject.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Manager API")
                        .description("A RESTful API for managing tasks with full CRUD operations, "
                                + "filtering, pagination, and validation.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Hamza")
                                .url("https://github.com/amiineed/taskmanager")));
    }
}
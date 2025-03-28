package com.example.lms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lmsOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Loan Management System API")
                        .description("API documentation for the Loan Management System")
                        .version("v1.0.0")
                        .contact(new Contact().name("LMS Team").email("lms@example.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}

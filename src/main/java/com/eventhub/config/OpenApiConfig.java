package com.eventhub.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI eventHubOpenApi() {
        return new OpenAPI()
                .info(apiInfo())
                // for all controllers bearerAuth
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, jwtSecurityScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("EventHub API")
                .description("""
                        EventHub is a REST API for event management.
                        
                        The system supports user registration, JWT authentication,
                        organizer applications, event management, ticket types,
                        ticket purchase, payments, reviews, ratings, statistics,
                        and audit logging.
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("EventHub Support")
                        .email("support@eventhub.com"));
    }

    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter JWT token without the Bearer prefix");
    }
}
package com.eventhub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventHubApplicationTests {

    @Test
    void applicationShouldHaveSpringBootApplicationAnnotation() {
        SpringBootApplication annotation = EventHubApplication.class.getAnnotation(SpringBootApplication.class);

        assertNotNull(annotation);
    }

    // Spring will see JwtProperties
    @Test
    void applicationShouldHaveConfigurationPropertiesScanAnnotation() {
        ConfigurationPropertiesScan annotation = EventHubApplication.class.getAnnotation(ConfigurationPropertiesScan.class);

        assertNotNull(annotation);
    }
}
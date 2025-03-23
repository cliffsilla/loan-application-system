package com.example.middleware.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * Configuration class for mock services.
 * This configuration is only active when the "mock" profile is enabled.
 */
@Configuration
@Profile("mock")
public class MockServicesConfig {

    private static final Logger logger = LoggerFactory.getLogger(MockServicesConfig.class);

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("==================================================");
        logger.info("MOCK SERVICES ACTIVE - USING SIMULATED RESPONSES");
        logger.info("==================================================");
        logger.info("External services (Scoring Engine, Core Banking) are being simulated.");
        logger.info("This is suitable for development and testing purposes only.");
        logger.info("To use real services, disable the 'mock' profile.");
    }
}

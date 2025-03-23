package com.example.middleware.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> serverCustomizer() {
        return factory -> {
            // Explicitly set connector to listen on all interfaces
            factory.addConnectorCustomizers(connector -> {
                connector.setProperty("address", "0.0.0.0");
            });
        };
    }
}

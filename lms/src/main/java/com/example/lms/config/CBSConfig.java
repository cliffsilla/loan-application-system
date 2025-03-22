package com.example.lms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.ws.client.core.WebServiceTemplate;

@Configuration
@EnableIntegration
public class CBSConfig {

    @Bean
    public MessageChannel cbsRequestChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel cbsResponseChannel() {
        return new DirectChannel();
    }

    @ServiceActivator(inputChannel = "cbsRequestChannel")
    public void callCbs(Message<String> request) {
        try {
            // Simply log the request for now to avoid integration issues
            System.out.println("Received request for CBS: " + request.getPayload());
            
            // For now, we'll skip the actual web service call to avoid integration issues
            // This allows the application to start without external dependencies
            
            // Mock a successful response
            System.out.println("CBS service call simulated successfully");
            
        } catch (Exception e) {
            System.err.println("Error in CBS service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Bean
    public WebServiceTemplate webServiceTemplate() {
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setDefaultUri("http://kycapitest.credable.io/service/customerWsdl.wsdl");
        return webServiceTemplate;
    }
}

package org.alfresco.genai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The {@code AiListenerApplication} class serves as the entry point for the Spring Boot application designed to listen
 * for and process AI-related events.
 */
@SpringBootApplication
public class AiListenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiListenerApplication.class, args);
    }

}

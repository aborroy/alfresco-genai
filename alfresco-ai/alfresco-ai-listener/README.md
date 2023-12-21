# Alfresco AI Listener

AI Listener is a Spring Boot Service designed to enhance documents stored in an Alfresco Repository.

## Configuration

Use [application.properties](src/main/resources/application.properties) file, command line argument or Docker environment variable.

## Building

To build the AI Listener, use the default Maven command:

```bash
mvn clean package
```

## Running

Execute the program from the command line:

```bash
$ java -jar target/alfresco-ai-listener-0.8.0.jar
Started AiListenerApplication in 1.876 seconds (process running for 2.123)
PDF Rendition has been requested for document b20efa32-c368-4125-ab5f-feb4d5c56975
Summarizing document b20efa32-c368-4125-ab5f-feb4d5c56975
Document b20efa32-c368-4125-ab5f-feb4d5c56975 has been updated with summary and tag
```

The application will run indefinitely; stop the program using `Ctrl+C`

## Using the Docker Image

Docker Image can be built to be used in Docker Compose:

```yaml
    alfresco-ai-listener:
        image: alfresco-ai-listener
        environment:
            CONTENT_SERVICE_URL: "http://alfresco:8080"
            SPRING_ACTIVEMQ_BROKERURL: "tcp://activemq:61616"
            GENAI_URL: "http://genai:8506"
        depends_on:
            - alfresco
```
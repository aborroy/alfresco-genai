# Alfresco integration with Private Generative AI

This project provides a collection of resources to enable the utilization of Private Generative AI in conjunction with Alfresco. Each service within the project is designed to operate locally, offering flexibility for usage in a development environment.

The primary scenarios covered by this project are centered around a document:

* Summarize a document in any language and recognize various tags
* Select a term from a provided list that characterizes a document
* Answer to a question related to the document


## Requirements

Following tools can be used to build and deploy this project:

* [Docker 4.25](https://docs.docker.com/get-docker/) (with 20 GB of RAM allocated)
* [ollama](https://ollama.ai/)
* [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
* [Maven 3.9](https://maven.apache.org/download.cgi)

>> Deploying this project in a production environment could require additional steps to ensure minimal performance and successful execution of actions


## Description

The project includes following components:

* [genai-stack](genai-stack) folder is using https://github.com/docker/genai-stack project to build a REST endpoint that provides AI services for a given document
* [alfresco](alfresco) folder includes a Docker Compose template to deploy Alfresco Community 23.1
* [alfresco-ai](alfresco-ai) folder includes a set of projects
  * [alfresco-ai-model](alfresco-ai/alfresco-ai-model) defines a custom Alfresco content model to store summaries, terms and prompts to be deployed in Alfresco Repository and Share App
  * [alfresco-ai-applier](alfresco-ai/alfresco-ai-applier) uses the Alfresco REST API to apply summaries or terms for a populated Alfresco Repository based on the application of the `genai:summarizable` aspect
  * [alfresco-ai-listener](alfresco-ai/alfresco-ai-listener) listens to messages and generates summaries, apply terms and reply answers for create or updated nodes in Alfresco Repository

```
┌────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                                            │
│                        ┌─Compose────────────────────┐          ┌─Compose────────────────────┐              │
│                        │                            │          │                            │              │
│                        │       A L F R E S C O      │          │     G E N  A I  Stack      │              │
│                        │                            │          │                            │              │
│                        │ ┌──────────┐ ┌───────────┐ │          │ ┌───┐ ┌─────┐ ┌─────────┐  │              │
│           ┌────────────┤ │model-repo│ │model-share│ │          │ │llm│ │neo4j│ │langchain│  │              │
│           │            │ └──────────┘ └───────────┘ │          │ └───┘ └─────┘ └─────────┘  │              │
│           │            │                            │          │  ollama                    │              │
│           │            └──────────────▲─────────────┘          └─────────────▲──────────────┘              │
│           │                           │                                      │                             │
│           │                           │ http://alfresco:8080                 │ http://genai:8506/summary   │
│           │            ┌─App──────────┴─────────────┐                        │ http://genai:8506/classify  │
│           │            │                            │                        │ http://genai:8506/prompt    │
│           │            │   alfresco-ai-applier      ├────────────────────────┤                             │
│           │            │                            │                        │                             │
│           │            └──────────────┬─────────────┘                        │                             │
│           │                           │                                      │                             │
│           │                           │                                      │                             │
│           │            ┌─Service──────┴─────────────┐                        │                             │
│           │            │                            │                        │                             │
│           └────────────►   alfresco-ai-listener     ├────────────────────────┘                             │
│   tcp://activemq:61616 │                            │                                                      │
│                        └────────────────────────────┘                                                      │
│                                                                                                            │
└────────────────────────────────────────────────────────────────────────────────────────DOCKER NETWORK──────┘
```


## GenAI Stack

This service, available in [genai-stack](genai-stack) folder, offers various REST endpoints for applying AI operations to a given document.

```
 ┌─Compose────────────────────┐              
 │                            │              
 │     G E N  A I  Stack      │              
 │                            │              
 │ ┌───┐ ┌─────┐ ┌─────────┐  │              
 │ │llm│ │neo4j│ │langchain│  │              
 │ └───┘ └─────┘ └─────────┘  │              
 │  ollama                    │              
 └─────────────▲──────────────┘              
               │                             
               │ http://genai:8506/
```

* Summarizing a document and extracting tags from it

```bash
curl --location 'http://localhost:8506/summary' --form 'file=@"./file.pdf"'

{
    "summary": " The text discusses...",
    "tags": " Golang, Merkle, Difficulty",
    "model": "mistral"
}
```

* Selecting a term from a list that best matches the document

```bash
curl --location 'http://localhost:8506/classify?termList="Japanese,Spanish,Korean,English,Vietnamese"' --form 'file=./file.pdf"'

{
    "term": " English",
    "model": "mistral"
}
```

* Responding to questions related to the document

```bash
curl --location 'http://localhost:8506/prompt?prompt="What is this text about?"' --form 'file=./file.pdf"'

{
    "answer": " Yes, it is difficult to find childcare in Tokyo.",
    "model": "mistral"
}
```

### Configuration

Modify `.env` file values:

```
# Choose any of the on premise models supported by ollama
LLM=mistral

 # Any language name supported by chosen LLM
SUMMARY_LANGUAGE=English
# Number of words for the summary
SUMMARY_SIZE=120
# Number of tags to be identified with the summary
TAGS_NUMBER=3 
```


## Alfresco

Alfresco service, available in [alfresco](alfresco) folder, includes custom content model definition and additional events configuration.

```
 ┌─Compose────────────────────┐
 │                            │
 │       A L F R E S C O      │
 │                            │
 │ ┌──────────┐ ┌───────────┐ │
 │ │model-repo│ │model-share│ │
 │ └──────────┘ └───────────┘ │
 │                            │
 └──────────────▲─────────────┘
                │              
                │ http://alfresco:8080
```

* Content Model for Repository is available in [genai-model-repo](alfresco-ai/alfresco-ai-model/genai-model-repo)
  * `genai:summarizable` aspect is used to store `summary` and `tags` generated with AI
  * `genai:promptable` aspect is used to store the `question` provided by the user and the `answer` generated with AI
  * `genai:classifiable` aspect is used to store the list of terms available for the AI to classify a document. It should be applied to a folder
  * `genai:classified` aspect is used to store the term selected by the AI. It should be applied to a document

* Forms and configuration to handle custom Content Model from Share are available in [genai-model-share](alfresco-ai/alfresco-ai-model/genai-model-share)

* Additional configuration for Repository
  * Since `alfresco-ai-listener` is listening to renditions, default `event2` filter should be modified. Following configuration has been added to `alfresco` service in `compose.yaml`
```
-Drepo.event2.filter.nodeTypes="sys:*, fm:*, cm:failedThumbnail, cm:rating, rma:rmsite include_subtypes, usr:user"
```  


## Alfresco AI Applier

This Spring Boot application utilizes the Alfresco REST API to fetch all documents from a given Alfresco folder and apply a single action:

* The `Summarizing` action involves retrieving documents from a folder using the Alfresco Search API, checking for the availability of PDF renditions, and updating document nodes with summaries obtained from the GenAi service.
* The `Classifying` action retrieves documents from a folder using the Alfresco Search API, checks for the availability of PDF renditions, and updates document nodes by selecting a term, from a list of terms using the GenAi service.

```
 ┌─Compose────────────────────┐          ┌─Compose────────────────────┐          
 │                            │          │                            │          
 │       A L F R E S C O      │          │     G E N  A I  Stack      │          
 │                            │          │                            │          
 │ ┌──────────┐ ┌───────────┐ │          │ ┌───┐ ┌─────┐ ┌─────────┐  │          
 │ │model-repo│ │model-share│ │          │ │llm│ │neo4j│ │langchain│  │          
 │ └──────────┘ └───────────┘ │          │ └───┘ └─────┘ └─────────┘  │          
 │                            │          │  ollama                    │          
 └──────────────▲─────────────┘          └─────────────▲──────────────┘          
                │                                      │                         
                │ http://alfresco:8080                 │ http://genai:8506/summary
 ┌─App──────────┴─────────────┐                        │ http://genai:8506/classify
 │                            │                        │ 
 │   alfresco-ai-applier      ├────────────────────────┘
 │                            │                        
 └────────────────────────────┘                        
```

### Configuration

Modify property values in `application.properties` file:

```
# Spring Boot properties
# Disable Spring Boot banner
spring.main.banner-mode=off

# Logging Configuration
logging.level.org.springframework=ERROR
logging.level.org.alfresco=INFO
logging.pattern.console=%msg%n

# Alfresco Server Configuration
# Basic authentication credentials for Alfresco Server
content.service.security.basicAuth.username=admin
content.service.security.basicAuth.password=admin
# URL and path for Alfresco Server API
content.service.url=http://localhost:8080
content.service.path=/alfresco/api/-default-/public/alfresco/versions/1

# Alfresco Repository Content Model (Summary)
# Aspect that triggers the summarization task
content.service.summary.aspect=genai:summarizable
# Node property to store the summary obtained from GenAI Stack
content.service.summary.summary.property=genai:summary
# Node property to store tags obtained from GenAI Stack; use TAG as a value to use a tag instead of a property
content.service.summary.tags.property=genai:tags
# Node property to store the Large Language Model (LLM) used; use TAG as a value to use a tag instead of a property
content.service.summary.model.property=genai:llmSummary

# Alfresco Repository Content Model (Classify)
# Node property that includes terms for classification
content.service.classify.terms.property=genai:terms
# Aspect that enables classification task
content.service.classify.aspect=genai:classified
# Node property to fill with the term
content.service.classify.term.property=genai:term
# Node property to fill with the model
content.service.classify.model.property=genai:llmClassify

# GenAI Client Configuration
# Host URL for the Document GenAI service
genai.url=http://localhost:8506
# Request timeout in seconds for GenAI service requests
genai.request.timeout=1200

# Alfresco AI Applier Configuration
# Root folder in Alfresco Repository to apply GenAI action
applier.root.folder=/app:company_home/app:shared
# Choose one action: SUMMARY, CLASSIFY
applier.action=SUMMARY
# List of terms to be applied for CLASSIFY action (ignored when using SUMMARY action)
applier.action.classify.term.list=English,Spanish,Japanese,Vietnamese
# Maximum number of items to be retrieved from Alfresco Repository in each iteration
request.max.items=20
```

>> Configuration parameters can be also used as command line arguments or Docker environment variables.


## Alfresco AI Listener

This Spring Boot application is designed to capture summary, classification, or prompting aspect settings by listening to ActiveMQ events. The application then forwards the request to the GenAI Stack and subsequently updates the Alfresco Node using the REST API.

```
              ┌─Compose────────────────────┐          ┌─Compose────────────────────┐
              │                            │          │                            │
              │       A L F R E S C O      │          │     G E N  A I  Stack      │
              │                            │          │                            │
              │ ┌──────────┐ ┌───────────┐ │          │ ┌───┐ ┌─────┐ ┌─────────┐  │
 ┌────────────┤ │model-repo│ │model-share│ │          │ │llm│ │neo4j│ │langchain│  │
 │            │ └──────────┘ └───────────┘ │          │ └───┘ └─────┘ └─────────┘  │
 │            │                            │          │  ollama                    │
 │            └──────────────▲─────────────┘          └─────────────▲──────────────┘
 │ tcp://activemq:61616      │                                      │               
 │                           │ http://alfresco:8080                 │ http://genai:8506/summary
 │            ┌─Service──────┴─────────────┐                        │ http://genai:8506/classify
 │            │                            │                        │ http://genai:8506/prompt
 └────────────►   alfresco-ai-listener     ├────────────────────────┘                         
              │                            │                                                  
              └────────────────────────────┘                                                  
```

### Configuration

Modify property values in `application.properties` file or use Docker environment setings:

```
# Alfresco Server Configuration
# Basic authentication credentials for Alfresco Server
content.service.security.basicAuth.username=admin
content.service.security.basicAuth.password=admin
# URL and path for Alfresco Server API
content.service.url=http://localhost:8080
content.service.path=/alfresco/api/-default-/public/alfresco/versions/1


# Alfresco Repository Content Model (Summary)
# Aspect that triggers the summarization task
content.service.summary.aspect=genai:summarizable
# Node property to store the summary obtained from GenAI Stack
content.service.summary.summary.property=genai:summary
# Node property to store tags obtained from GenAI Stack; use TAG as a value to use a tag instead of a property
content.service.summary.tags.property=genai:tags
# Node property to store the Large Language Model (LLM) used; use TAG as a value to use a tag instead of a property
content.service.summary.model.property=genai:llmSummary

# Alfresco Repository Content Model (Prompt)
# Aspect that enables prompt task
content.service.prompt.aspect=genai:promptable
# Node property that contains a question
content.service.prompt.question.property=genai:question
# Node property to fill with the answer
content.service.prompt.answer.property=genai:answer
# Node property to fill with the model
content.service.prompt.model.property=genai:llmPrompt

# Alfresco Repository Content Model (Classify)
# Node property that includes terms for classification
content.service.classify.terms.property=genai:terms
# Aspect that enables classification task
content.service.classify.aspect=genai:classified
# Node property to fill with the term
content.service.classify.term.property=genai:term
# Node property to fill with the model
content.service.classify.model.property=genai:llmClassify


# ActiveMQ Server
spring.activemq.brokerUrl=tcp://localhost:61616
spring.jms.cache.enabled=false
alfresco.events.enableSpringIntegration=false
alfresco.events.enableHandlers=true

# GenAI Client Configuration
# Host URL for the Document GenAI service
genai.url=http://localhost:8506
# Request timeout in seconds for GenAI service requests
genai.request.timeout=1200
```

>> Configuration parameters can be also used as command line arguments or Docker environment variables.


# Use Case 1: Existing Content

1. Start GenAI Stack

>> Verify Docker and ollama are up & running

```sh
$ cd genai-stack
$ docker compose up
```

2. Start Alfresco

>> Verify that `alfresco-ai-listener` is not present or is commented in [compose.yaml](alfresco/compose.yaml)

```sh
$ cd alfresco
$ docker compose up
```

3. Upload a number of documents to a given folder, for instance `/app:company_home/app:shared`

4. (OPTIONAL) Compile the Alfresco AI Applier (if required)

Compile the Alfresco AI Applier app if required

```sh
$ cd alfresco-ai/alfresco-ai-applier
$ mvn clean package
```

5. Run the Alfresco AI Applier to summarize the documents

```sh
$ cd alfresco-ai/alfresco-ai-applier
$ java -jar target/alfresco-ai-applier-0.8.0.jar \
  --applier.root.folder=/app:company_home/app:shared \
  --applier.action=SUMMARY
```

>> Once this command has finished, every document in the folder should include a populated `Summary` property (accessible in "view" mode)

6. Run the Alfresco AI Applier to classify the documents

```sh
$ cd alfresco-ai/alfresco-ai-applier
$ java -jar target/alfresco-ai-applier-0.8.0.jar \
  --applier.root.folder=/app:company_home/app:shared \
  --applier.action=CLASSIFY \
  --applier.action.classify.term.list=English,Spanish,Japanese,Vietnamese
```

>> Once this command has finished, every document in the folder should include a populated `Term` property selected from the Term List (accessible in "view" mode)


# Use Case 2: New Content

1. Start GenAI Stack

>> Verify Docker and ollama are up & running

```sh
$ cd genai-stack
$ docker compose up
```

2. (Optional) Build `alfresco-ai-listener` Docker Image if required

```sh
$ cd alfresco-ai/alfresco-ai-listener
$ mvn clean package
$ docker build . -t alfresco-ai-listener
```

3. Start Alfresco

>> Verify that `alfresco-ai-listener` is present in [compose.yaml](alfresco/compose.yaml)

```sh
$ cd alfresco
$ docker compose up
```

4. Get a summary for a document

* Apply Summarizable with AI (`genai:summarizable`) aspect to a node
* Wait until GenAI populates `Summary` property (accesible in "view" mode)

5. Classify a document 

* Apply Classifiable with AI (`genai:classifiable`) aspect to a folder
* Add a list of terms separated by comma in property Terms (`genai:terms`) of the folder
* Add a document inside this folder 
* Apply the aspect Classified with AI (`genai:classified`) to the document
* Wait until GenAI selects one term from the list and populates `Term` property of the document (accesible in "view" mode)

6. Ask a question

* Apply Promptable with AI (`genai:promptable`) aspect to a document
* Type your question in the property Question
* Wait until GenAI populates `Answer` property (accesible in "view" mode)

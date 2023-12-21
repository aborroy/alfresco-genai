# Alfresco addons to define Custom Content Model for GenAI

Alfresco Custom Content Model defined as Repository and Share addons

## Requirements

Following tools can be used to build and deploy these projects:

* [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
* [Maven 3.9](https://maven.apache.org/download.cgi)


## Description

This folder includes following projects:

* [genai-model-repo](genai-model-repo) defines a custom Alfresco content model to store summaries, terms and prompts to be deployed in Alfresco Repository
* [genai-model-share](genai-model-share) defines forms and configuration fot the custom Alfresco content model

```
 ┌────────────────────────────┐
 │                            │
 │       A L F R E S C O      │
 │                            │
 │ ┌──────────┐ ┌───────────┐ │
 │ │model-repo│ │model-share│ │
 │ └──────────┘ └───────────┘ │
 │                            │
 └────────────────────────────┘
```
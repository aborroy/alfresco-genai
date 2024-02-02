# Alfresco addons to support integration with GenAI

Set of Alfresco addons, applications and services to support the integration of GenAI with Alfresco

## Requirements

Following tools can be used to build and deploy these projects:

* [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
* [Maven 3.9](https://maven.apache.org/download.cgi)


## Description

This folder includes following projects:

* [alfresco-ai-model](alfresco-ai-model) defines a custom Alfresco content model to store summaries, descriptions, terms and prompts to be deployed in Alfresco Repository and Share App
* [alfresco-ai-applier](alfresco-ai-applier) defines a command line application that uses the Alfresco REST API to apply summaries, descriptions or terms for a populated Alfresco Repository
* [alfresco-ai-listener](alfresco-ai-listener) defines a containerizable service that listens to messages and generates summaries, descriptions, apply terms and reply answers for create or updated nodes in Alfresco Repository

```
                                                
                       ┌────────────────────────────┐
                       │                            │
                       │       A L F R E S C O      │
                       │                            │
                       │ ┌──────────┐ ┌───────────┐ │
          ┌────────────┤ │model-repo│ │model-share│ │
          │            │ └──────────┘ └───────────┘ │
          │            │                            │
          │            └──────────────▲─────────────┘
          │                           │              
          │                           │ http://alfresco:8080
          │            ┌─App──────────┴─────────────┐       
          │            │                            │       
          │            │   alfresco-ai-applier      │
          │            │                            │       
          │            └──────────────┬─────────────┘       
          │                           │                     
          │                           │                     
          │            ┌─Service──────┴─────────────┐       
          │            │                            │       
          └────────────►   alfresco-ai-listener     │
  tcp://activemq:61616 │                            │       
                       └────────────────────────────┘       
```

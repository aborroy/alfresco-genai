package org.alfresco.genai.event;

import org.alfresco.event.sdk.handling.filter.PropertyChangedFilter;
import org.alfresco.genai.service.GenAiClient;
import org.alfresco.genai.service.NodeUpdateService;
import org.alfresco.genai.service.RenditionService;
import org.alfresco.event.sdk.handling.filter.EventFilter;
import org.alfresco.event.sdk.handling.handler.OnNodeUpdatedEventHandler;
import org.alfresco.event.sdk.model.v1.model.DataAttributes;
import org.alfresco.event.sdk.model.v1.model.NodeResource;
import org.alfresco.event.sdk.model.v1.model.RepoEvent;
import org.alfresco.event.sdk.model.v1.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * The {@code PropertyPromptUpdatedHandler} class is a Spring component responsible for handling events triggered upon the
 * update of specific properties of nodes. It implements the {@link OnNodeUpdatedEventHandler} interface to define
 * custom logic for processing node update events related to property changes.
 *
 * <p>When a property associated with asking a question is updated, this handler retrieves the question and the
 * corresponding document UUID. It then uses the GenAI service to obtain an answer based on the document's content,
 * updating the document node with the obtained answer.
 *
 */
@Component
public class PropertyPromptUpdatedHandler implements OnNodeUpdatedEventHandler {

    /**
     * Logger for logging information and error messages.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PropertyPromptUpdatedHandler.class);

    /**
     * Name of the property that holds the question to be answered.
     */
    @Value("${content.service.prompt.question.property}")
    private String questionProperty;

    /**
     * Autowired instance of {@link GenAiClient} for interacting with the GenAI service.
     */
    @Autowired
    private GenAiClient genAiClient;

    /**
     * Autowired instance of {@link RenditionService} for handling document renditions.
     */
    @Autowired
    private RenditionService renditionService;

    /**
     * Autowired instance of {@link NodeUpdateService} for updating document nodes.
     */
    @Autowired
    private NodeUpdateService nodeUpdateService;

    /**
     * Handles the node update event triggered by the system when a specified property is updated. Retrieves the
     * question associated with the property, obtains an answer using the GenAI service, and updates the document node
     * with the obtained answer.
     *
     * @param repoEvent The event containing information about the updated node.
     */
    @Override
    public void handleEvent(RepoEvent<DataAttributes<Resource>> repoEvent) {

        String uuid = ((NodeResource) repoEvent.getData().getResource()).getId();
        String question = ((NodeResource) repoEvent.getData().getResource()).getProperties().get(questionProperty).toString();

        LOG.info("Answering question '{}' for document {}", question, uuid);
        try {
            nodeUpdateService.updateNodeAnswer(uuid, genAiClient.getAnswer(renditionService.getRenditionContent(uuid), question));
        } catch (IOException e) {
            LOG.error("Error updating document {}", uuid, e);
        }
        LOG.info("Document {} has been updated with answer", uuid);

    }

    /**
     * Specifies the event filter to determine which node update events this handler should process. In this case,
     * the filter is based on the updated property specified by {@code questionProperty}.
     *
     * @return An {@link EventFilter} representing the filter criteria for node update events.
     */
    @Override
    public EventFilter getEventFilter() {
        return PropertyChangedFilter.of(questionProperty);
    }

}

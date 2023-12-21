package org.alfresco.genai.event;

import org.alfresco.event.sdk.handling.handler.EventHandler;
import org.alfresco.event.sdk.model.v1.model.DataAttributes;
import org.alfresco.event.sdk.model.v1.model.NodeResource;
import org.alfresco.event.sdk.model.v1.model.RepoEvent;
import org.alfresco.event.sdk.model.v1.model.Resource;
import org.alfresco.genai.service.GenAiClient;
import org.alfresco.genai.service.NodeUpdateService;
import org.alfresco.genai.service.RenditionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * The {@code AbstractContentTypeHandler} class is an abstract base class that implements the {@link EventHandler} interface.
 * It serves as a common foundation for handlers focused on content type-specific node events within the Alfresco repository.
 * This handler defines common functionality for handling events related to document content types, such as summarization
 * through the GenAI service.
 *
 * <p>The class includes autowired instances of essential services, such as {@link GenAiClient}, {@link RenditionService},
 * and {@link NodeUpdateService}, required for interacting with external services and updating document nodes.
 *
 * <p>Concrete subclasses should extend this class and provide specific event handling logic for their targeted content types.
 *
 */
public abstract class AbstractContentTypeHandler implements EventHandler {

    /**
     * Logger for logging information and error messages.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractContentTypeHandler.class);

    /**
     * Autowired instance of {@link GenAiClient} for interacting with the GenAI service.
     */
    @Autowired
    GenAiClient genAiClient;

    /**
     * Autowired instance of {@link RenditionService} for handling document renditions.
     */
    @Autowired
    RenditionService renditionService;

    /**
     * Autowired instance of {@link NodeUpdateService} for updating document nodes.
     */
    @Autowired
    NodeUpdateService nodeUpdateService;

    /**
     * Handles the node-related event by defining common logic for content type-specific events. It checks if a PDF
     * rendition is already created for the document. If yes, it initiates the document summarization process; otherwise,
     * it requests the creation of a PDF rendition.
     *
     * @param repoEvent The event containing information about the node.
     */
    @Override
    public void handleEvent(RepoEvent<DataAttributes<Resource>> repoEvent) {
        String uuid = ((NodeResource) repoEvent.getData().getResource()).getId();
        if (renditionService.pdfRenditionIsCreated(uuid)) {
            LOG.info("Summarizing document {}", uuid);
            try {
                nodeUpdateService.updateNodeSummary(uuid, genAiClient.getSummary(renditionService.getRenditionContent(uuid)));
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
            LOG.info("Document {} has been updated with summary and tag", uuid);
        } else {
            LOG.info("PDF Rendition has been requested for document {}", uuid);
            renditionService.createPdfRendition(uuid);
        }
    }
}

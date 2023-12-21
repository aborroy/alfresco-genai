package org.alfresco.genai.action;

import org.alfresco.genai.service.GenAiClient;
import org.alfresco.genai.service.NodeUpdateService;
import org.alfresco.genai.service.RenditionService;
import org.alfresco.search.model.ResultSetRowEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * The {@code AiApplierSummary} class is a Spring component that implements the {@link AiApplierAction} interface
 * for performing document summarization in the AI Applier application.
 */
@Component
public class AiApplierSummary implements AiApplierAction {

    static final Logger LOG = LoggerFactory.getLogger(AiApplierSummary.class);

    /**
     * The property name for storing the document summary in the Alfresco repository obtained from configuration.
     */
    @Value("${content.service.summary.summary.property}")
    String summaryProperty;

    /**
     * Http client for interacting with the GenAI service
     */
    @Autowired
    GenAiClient genAiClient;

    /**
     * Http client for handling document renditions in Alfresco
     */
    @Autowired
    RenditionService renditionService;

    /**
     * Http client for updating Alfresco document nodes
     */
    @Autowired
    NodeUpdateService nodeUpdateService;

    /**
     * Executes the document summarization action on the given {@code ResultSetRowEntry}.
     *
     * @param entry The entry representing an Alfresco document for summarization.
     * @return {@code true} if the summarization was successful; otherwise, {@code false}.
     * @throws RuntimeException If an error occurs during summarization, such as IO exception.
     */
    @Override
    public boolean execute(ResultSetRowEntry entry) {

        String uuid = entry.getEntry().getId();

        LOG.debug("Summarizing document {} ({})", entry.getEntry().getName(), uuid);

        if (renditionService.pdfRenditionIsCreated(uuid)) {

            try {

                nodeUpdateService.updateNodeSummary(uuid, genAiClient.getSummary(renditionService.getRenditionContent(uuid)));
                LOG.debug("Document {} has been updated with summary and tag", entry.getEntry().getName());
                return true;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {

            LOG.debug("PDF rendition for document {} was not available, it has been requested", entry.getEntry().getName());
            renditionService.createPdfRendition(uuid);

        }

        return false;

    }

    /**
     * Returns the property name for storing the document summary in the Alfresco repository.
     *
     * @return The name of the property used for storing document summaries in Alfresco.
     */
    @Override
    public String getUpdateField() {
        return summaryProperty;
    }
}

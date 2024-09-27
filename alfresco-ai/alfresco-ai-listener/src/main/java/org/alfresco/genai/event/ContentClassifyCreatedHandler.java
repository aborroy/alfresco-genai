package org.alfresco.genai.event;

import org.alfresco.core.handler.ContentApi;
import org.alfresco.core.handler.ContentApiClient;
import org.alfresco.core.handler.NodesApi;
import org.alfresco.event.sdk.handling.filter.EventFilter;
import org.alfresco.event.sdk.handling.filter.NodeAspectFilter;
import org.alfresco.event.sdk.handling.filter.NodeTypeFilter;
import org.alfresco.event.sdk.handling.handler.OnNodeCreatedEventHandler;
import org.alfresco.event.sdk.model.v1.model.DataAttributes;
import org.alfresco.event.sdk.model.v1.model.NodeResource;
import org.alfresco.event.sdk.model.v1.model.RepoEvent;
import org.alfresco.event.sdk.model.v1.model.Resource;
import org.alfresco.genai.model.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * The {@code ContentClassifyCreatedHandler} class is a Spring component that extends the {@link AbstractContentTypeHandler}
 * and implements the {@link OnNodeCreatedEventHandler} interface. It is responsible for handling events triggered upon
 * the creation of nodes with a specified content type, focusing on nodes with the "cm:content" type and a specific classified aspect.
 *
 * <p>This handler provides a concise event filter definition using a combination of filters to identify relevant node
 * creation events. The filter criteria include the presence of the classified aspect and the "cm:content" node type.
 */
@Component
public class ContentClassifyCreatedHandler extends AbstractContentTypeHandler implements OnNodeCreatedEventHandler {

    /**
     * Logger for logging information and error messages.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ContentClassifyCreatedHandler.class);

    /**
     * Aspect name associated with document classification.
     */
    @Value("${content.service.classify.aspect}")
    private String classifyAspect;

    @Autowired
    NodesApi nodesApi;

    @Override
    public void handleEvent(final RepoEvent<DataAttributes<Resource>> repoEvent) {

        NodeResource nodeResource = (NodeResource) repoEvent.getData().getResource();
        String uuid = nodeResource.getId();

        LOG.info("Classifying document {}", uuid);

        try {

            // PDF Documents can be classified using original content
            if (nodeResource.getName().toLowerCase().endsWith(".pdf")) {

                byte[] pdfFileContent = nodesApi.getNodeContent(uuid, false, null, null).getBody().getContentAsByteArray();
                File pdfFile = Files.createTempFile(null, null).toFile();
                Files.write(pdfFile.toPath(), pdfFileContent);
                nodeUpdateService.updateNodeTerm(uuid, genAiClient.getTerm(pdfFile, nodeUpdateService.getTermList(uuid)));

                LOG.debug("Document {} classified from content", uuid);

            } else {

                // For Non PDF Documents is required to wait for the PDF rendition

                File renditionContent = null;
                int maxRetries = 10;
                int retryCount = 0;
                long retryInterval = 2000;

                while (renditionContent == null && retryCount < maxRetries) {
                    if (renditionService.pdfRenditionIsCreated(uuid)) {
                        renditionContent = renditionService.getRenditionContent(uuid);
                    } else {
                        LOG.info("Rendition not available for document {}. Retrying {}/{}", uuid, retryCount + 1, maxRetries);
                        Thread.sleep(retryInterval);
                        retryCount++;
                    }
                }

                if (renditionContent == null) {
                    throw new RuntimeException("Failed to get rendition content after " + maxRetries + " retries.");
                }

                nodeUpdateService.updateNodeTerm(uuid, genAiClient.getTerm(renditionContent, nodeUpdateService.getTermList(uuid)));

                LOG.debug("Document {} classified from rendition", uuid);

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Specifies the event filter to determine which node creation events this handler should process. The filter criteria
     * include the presence of the classified aspect and the "cm:content" node type.
     *
     * @return An {@link EventFilter} representing the filter criteria for node creation events.
     */
    @Override
    public EventFilter getEventFilter() {
        return NodeAspectFilter.of(classifyAspect)
                .and(NodeTypeFilter.of("cm:content"));
    }
}
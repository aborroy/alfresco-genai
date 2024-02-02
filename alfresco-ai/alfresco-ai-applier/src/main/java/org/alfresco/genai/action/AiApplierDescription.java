package org.alfresco.genai.action;

import org.alfresco.core.handler.NodesApi;
import org.alfresco.genai.service.GenAiClient;
import org.alfresco.genai.service.NodeUpdateService;
import org.alfresco.genai.service.RenditionService;
import org.alfresco.search.model.ResultSetRowEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * The {@code AiApplierDescription} class is a Spring component that implements the {@link AiApplierAction} interface
 * for performing picture description in the AI Applier application.
 */
@Component
public class AiApplierDescription implements AiApplierAction {

    static final Logger LOG = LoggerFactory.getLogger(AiApplierDescription.class);

    /**
     * The property name for storing the picture description in the Alfresco repository obtained from configuration.
     */
    @Value("${content.service.description.description.property}")
    String descriptionProperty;

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

    @Autowired
    NodesApi nodesApi;

    /**
     * Executes the picture description action on the given {@code ResultSetRowEntry}.
     *
     * @param entry The entry representing an Alfresco picture for description.
     * @return {@code true} if the description was successful; otherwise, {@code false}.
     * @throws RuntimeException If an error occurs during description, such as IO exception.
     */
    @Override
    public boolean execute(ResultSetRowEntry entry) {

        String uuid = entry.getEntry().getId();

        LOG.debug("Describing picture {} ({})", entry.getEntry().getName(), uuid);

        try {

            byte[] fileContent = nodesApi.getNodeContent(uuid, true, null, null).getBody().getContentAsByteArray();
            File pictureFile = Files.createTempFile(null, null).toFile();
            Files.write(pictureFile.toPath(), fileContent);

            nodeUpdateService.updateNodeDescription(uuid, genAiClient.getDescription(pictureFile));
            LOG.debug("Picture {} has been updated with description", entry.getEntry().getName());

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Returns the property name for storing the picture description in the Alfresco repository.
     *
     * @return The name of the property used for storing picture description in Alfresco.
     */
    @Override
    public String getUpdateField() {
        return descriptionProperty;
    }
}

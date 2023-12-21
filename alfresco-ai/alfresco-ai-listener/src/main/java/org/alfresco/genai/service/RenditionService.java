package org.alfresco.genai.service;

import org.alfresco.core.handler.RenditionsApi;
import org.alfresco.core.model.Rendition;
import org.alfresco.core.model.RenditionBodyCreate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * The {@code RenditionService} class is a Spring service responsible for managing document renditions in an Alfresco
 * Repository. It interacts with the Alfresco Renditions API to retrieve, check, and create PDF renditions for documents.
 */
@Service
public class RenditionService {

    /**
     * Autowired instance of {@link RenditionsApi} for communication with the Alfresco Renditions API.
     */
    @Autowired
    RenditionsApi renditionsApi;

    /**
     * Retrieves the content of the PDF rendition for the document identified by its UUID.
     *
     * @param uuid The unique identifier of the document.
     * @return A {@link File} object representing the PDF rendition content.
     * @throws IOException If an I/O error occurs during the retrieval of the PDF rendition content.
     */
    public File getRenditionContent(String uuid) throws IOException {
        byte[] pdfFileContent =
                renditionsApi.getRenditionContent(uuid, "pdf",
                        false, null, null, null).getBody().getContentAsByteArray();
        File pdfFile = Files.createTempFile(null, null).toFile();
        Files.write(pdfFile.toPath(), pdfFileContent);
        return pdfFile;
    }

    /**
     * Checks if a PDF rendition has been created for the document identified by its UUID.
     *
     * @param uuid The unique identifier of the document.
     * @return {@code true} if the PDF rendition exists and is in the "CREATED" status, {@code false} otherwise.
     */
    public boolean pdfRenditionIsCreated(String uuid) {
        return renditionsApi.getRendition(uuid, "pdf").getBody().getEntry().getStatus() == Rendition.StatusEnum.CREATED;
    }

    /**
     * Creates a PDF rendition for the document identified by its UUID.
     *
     * @param uuid The unique identifier of the document.
     */
    public void createPdfRendition(String uuid) {
        renditionsApi.createRendition(uuid, new RenditionBodyCreate().id("pdf"));
    }

}

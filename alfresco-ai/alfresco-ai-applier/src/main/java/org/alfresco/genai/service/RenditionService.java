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
 * The {@code RenditionService} class provides methods for interacting with document renditions through the Alfresco
 * Renditions API. It is a Spring service responsible for retrieving, checking, and creating PDF renditions for
 * documents identified by their unique identifiers (UUIDs).
 */
@Service
public class RenditionService {

    /**
     * Autowired instance of {@link RenditionsApi} for communication with the Alfresco Renditions API.
     */
    @Autowired
    RenditionsApi renditionsApi;

    /**
     * Retrieves the content of the PDF rendition for a specified document identified by its UUID.
     *
     * @param uuid The unique identifier of the document.
     * @return A {@link java.io.File} containing the content of the PDF rendition.
     * @throws IOException If an I/O error occurs while retrieving or creating the PDF file.
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
     * Checks if a PDF rendition for a specified document identified by its UUID has been created.
     *
     * @param uuid The unique identifier of the document.
     * @return {@code true} if the PDF rendition is created, {@code false} otherwise.
     */
    public boolean pdfRenditionIsCreated(String uuid) {
        return renditionsApi.getRendition(uuid, "pdf").getBody().getEntry().getStatus() == Rendition.StatusEnum.CREATED;
    }

    /**
     * Initiates the creation of a PDF rendition for a specified document identified by its UUID.
     *
     * @param uuid The unique identifier of the document.
     */
    public void createPdfRendition(String uuid) {
        renditionsApi.createRendition(uuid, new RenditionBodyCreate().id("pdf"));
    }
}

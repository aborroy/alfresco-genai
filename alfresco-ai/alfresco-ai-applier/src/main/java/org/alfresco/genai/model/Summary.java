package org.alfresco.genai.model;

import java.util.List;

/**
 * The {@code Summary} class represents the result of summarizing a document using AI services.
 * It contains the summary text, a list of tags associated with the document, and the model used for summarization.
 *
 * <p>This class follows the builder pattern, allowing for a fluent and readable way to construct instances.
 *
 */
public class Summary {

    /**
     * The summarized text content of the document.
     */
    private String summary;

    /**
     * A list of tags associated with the document.
     */
    private List<String> tags;

    /**
     * The model used for summarization.
     */
    private String model;

    /**
     * Gets the summarized text content of the document.
     *
     * @return The summary text.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Sets the summarized text content of the document.
     *
     * @param summary The summary text.
     * @return This {@code Summary} instance for method chaining.
     */
    public Summary summary(String summary) {
        this.summary = summary;
        return this;
    }

    /**
     * Gets the list of tags associated with the document.
     *
     * @return The list of tags.
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets the list of tags associated with the document.
     *
     * @param tags The list of tags.
     * @return This {@code Summary} instance for method chaining.
     */
    public Summary tags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Gets the model used for summarization.
     *
     * @return The summarization model.
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the model used for summarization.
     *
     * @param model The summarization model.
     * @return This {@code Summary} instance for method chaining.
     */
    public Summary model(String model) {
        this.model = model;
        return this;
    }

}

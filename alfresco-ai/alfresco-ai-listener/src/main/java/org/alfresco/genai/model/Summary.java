package org.alfresco.genai.model;

import java.util.List;

/**
 * The {@code Summary} class represents information summarizing the content of a document, including the summary text,
 * associated tags, and the model used for generating the summary.
 *
 * <p>Instances of this class are used to encapsulate summary information obtained from AI services.
 */
public class Summary {

    /**
     * The summary text obtained from the document content.
     */
    private String summary;

    /**
     * The list of tags associated with the document content.
     */
    private List<String> tags;

    /**
     * The model information associated with the document content.
     */
    private String model;

    /**
     * Gets the summary text.
     *
     * @return The summary text.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Sets the summary text and returns the current instance for method chaining.
     *
     * @param summary The summary text to set.
     * @return The current {@code Summary} instance.
     */
    public Summary summary(String summary) {
        this.summary = summary;
        return this;
    }

    /**
     * Gets the list of tags associated with the document content.
     *
     * @return The list of tags.
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets the list of tags and returns the current instance for method chaining.
     *
     * @param tags The list of tags to set.
     * @return The current {@code Summary} instance.
     */
    public Summary tags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Gets the model information associated with the document content.
     *
     * @return The model information.
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the model information and returns the current instance for method chaining.
     *
     * @param model The model information to set.
     * @return The current {@code Summary} instance.
     */
    public Summary model(String model) {
        this.model = model;
        return this;
    }

}

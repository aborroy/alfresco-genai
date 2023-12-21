package org.alfresco.genai.model;

/**
 * The {@code Term} class represents the result of an AI-generated classification. It contains information about the term
 * and the model used for generating the answer.
 *
 * <p>Instances of this class are used to encapsulate AI responses containing both the selected term and model
 * information.
 */
public class Term {

    /**
     * The selected term content.
     */
    private String term;

    /**
     * The model information associated with the generated answer.
     */
    private String model;

    /**
     * Gets the generated term content.
     *
     * @return The term content.
     */
    public String getTerm() {
        return term;
    }

    /**
     * Sets the term content and returns the current instance for method chaining.
     *
     * @param term The term content to set.
     * @return The current {@code Term} instance.
     */
    public Term term(String term) {
        this.term = term;
        return this;
    }

    /**
     * Gets the model information associated with the generated answer.
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
     * @return The current {@code Answer} instance.
     */
    public Term model(String model) {
        this.model = model;
        return this;
    }
}


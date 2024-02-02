package org.alfresco.genai.model;

import java.util.List;

/**
 * The {@code Description} class represents the result of description a picture using AI services.
 * It contains the description text and the model used for description.
 *
 * <p>This class follows the builder pattern, allowing for a fluent and readable way to construct instances.
 *
 */
public class Description {

    /**
     * The description text content of the picture.
     */
    private String description;

    /**
     * The model used for description.
     */
    private String model;

    /**
     * Gets the description text content of the picture.
     *
     * @return The description text.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description text content of the picture.
     *
     * @param description The description text.
     * @return This {@code Description} instance for method chaining.
     */
    public Description description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Gets the model used for description.
     *
     * @return The description model.
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the model used for description.
     *
     * @param model The description model.
     * @return This {@code Description} instance for method chaining.
     */
    public Description model(String model) {
        this.model = model;
        return this;
    }

}

package org.alfresco.genai.model;

/**
 * The {@code Answer} class represents the result of an AI-generated answer. It contains information about the answer
 * content and the model used for generating the answer.
 *
 * <p>Instances of this class are used to encapsulate AI responses containing both the generated answer and model
 * information.
 */
public class Answer {

    /**
     * The generated answer content.
     */
    private String answer;

    /**
     * The model information associated with the generated answer.
     */
    private String model;

    /**
     * Gets the generated answer content.
     *
     * @return The answer content.
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Sets the answer content and returns the current instance for method chaining.
     *
     * @param answer The answer content to set.
     * @return The current {@code Answer} instance.
     */
    public Answer answer(String answer) {
        this.answer = answer;
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
    public Answer model(String model) {
        this.model = model;
        return this;
    }
}


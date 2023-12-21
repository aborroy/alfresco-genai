package org.alfresco.genai.action;

import org.alfresco.search.model.ResultSetRowEntry;

/**
 * The {@code AiApplierAction} interface defines the contract for various AI actions
 * that can be performed on Alfresco documents in the AI Applier application.
 */
public interface AiApplierAction {

    /**
     * Enumeration of supported AI actions.
     */
    enum Action {
        SUMMARY, CLASSIFY
    }

    /**
     * Executes the specified AI action on the given {@code ResultSetRowEntry}.
     *
     * @param entry The entry representing an Alfresco document for the AI action.
     * @return {@code true} if the document was successfully updated; otherwise, {@code false}.
     */
    boolean execute(ResultSetRowEntry entry);

    /**
     * Retrieves the field associated with the update performed by the AI action.
     *
     * @return The name of the field updated by the AI action.
     */
    String getUpdateField();

}

package org.alfresco.genai.event;

import org.alfresco.event.sdk.handling.filter.EventFilter;
import org.alfresco.event.sdk.handling.filter.NodeAspectFilter;
import org.alfresco.event.sdk.handling.filter.NodeTypeFilter;
import org.alfresco.event.sdk.handling.handler.OnNodeCreatedEventHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
     * Aspect name associated with document classification.
     */
    @Value("${content.service.classify.aspect}")
    private String classifyAspect;

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
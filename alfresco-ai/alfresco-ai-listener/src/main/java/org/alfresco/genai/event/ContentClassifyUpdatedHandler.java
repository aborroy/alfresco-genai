package org.alfresco.genai.event;

import org.alfresco.event.sdk.handling.filter.*;
import org.alfresco.event.sdk.handling.handler.OnNodeUpdatedEventHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The {@code ContentClassifyUpdatedHandler} class is a Spring component that extends the {@link AbstractContentTypeHandler}
 * and implements the {@link OnNodeUpdatedEventHandler} interface. It is responsible for handling events triggered upon the
 * update of nodes with a specified content type, focusing on nodes with the "cm:content" type and a specific classified aspect.
 *
 * <p>This handler provides a detailed event filter definition using a combination of filters to identify relevant node
 * update events. The filter criteria include the presence of the summary aspect, the "cm:content" node type, content
 * changes, or the addition of the classified aspect.
 */
@Component
public class ContentClassifyUpdatedHandler extends AbstractContentTypeHandler implements OnNodeUpdatedEventHandler {

    /**
     * Aspect name associated with document classification.
     */
    @Value("${content.service.classify.aspect}")
    private String classifyAspect;

    /**
     * Specifies the event filter to determine which node update events this handler should process. The filter criteria
     * include the presence of the classified aspect, the "cm:content" node type, content changes, or the addition of the
     * classified aspect.
     *
     * @return An {@link EventFilter} representing the filter criteria for node update events.
     */
    @Override
    public EventFilter getEventFilter() {
        return NodeAspectFilter.of(classifyAspect)
                .and(NodeTypeFilter.of("cm:content"))
                .and(ContentChangedFilter.get())
                .or(AspectAddedFilter.of(classifyAspect));
    }
}
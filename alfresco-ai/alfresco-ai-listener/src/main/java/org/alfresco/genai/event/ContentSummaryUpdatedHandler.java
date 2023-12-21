package org.alfresco.genai.event;

import org.alfresco.event.sdk.handling.filter.*;
import org.alfresco.event.sdk.handling.handler.OnNodeUpdatedEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

/**
 * The {@code ContentSummaryUpdatedHandler} class is a Spring component that extends the {@link AbstractContentTypeHandler}
 * and implements the {@link OnNodeUpdatedEventHandler} interface. It is responsible for handling events triggered upon the
 * update of nodes with a specified content type, focusing on nodes with the "cm:content" type and a specific summary aspect.
 *
 * <p>This handler provides a detailed event filter definition using a combination of filters to identify relevant node
 * update events. The filter criteria include the presence of the summary aspect, the "cm:content" node type, content
 * changes, or the addition of the summary aspect.
 */
@Component
public class ContentSummaryUpdatedHandler extends AbstractContentTypeHandler implements OnNodeUpdatedEventHandler {

    /**
     * Aspect name associated with document summaries.
     */
    @Value("${content.service.summary.aspect}")
    private String summaryAspect;

    /**
     * Specifies the event filter to determine which node update events this handler should process. The filter criteria
     * include the presence of the summary aspect, the "cm:content" node type, content changes, or the addition of the
     * summary aspect.
     *
     * @return An {@link EventFilter} representing the filter criteria for node update events.
     */
    @Override
    public EventFilter getEventFilter() {
        return NodeAspectFilter.of(summaryAspect)
                .and(NodeTypeFilter.of("cm:content"))
                .and(ContentChangedFilter.get())
                .or(AspectAddedFilter.of(summaryAspect));
    }
}
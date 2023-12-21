package org.alfresco.genai.service;

import org.alfresco.core.handler.NodesApi;
import org.alfresco.core.handler.TagsApi;
import org.alfresco.core.model.NodeAssociationEntry;
import org.alfresco.core.model.NodeBodyUpdate;
import org.alfresco.core.model.TagBody;
import org.alfresco.genai.model.Answer;
import org.alfresco.genai.model.Summary;
import org.alfresco.genai.model.Term;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code NodeUpdateService} class is a Spring service responsible for updating document nodes in an Alfresco
 * Repository with summary information and answers. It utilizes the Alfresco Nodes API and Tags API for updating node
 * properties and creating tags associated with the document identified by its UUID.
 */
@Service
public class NodeUpdateService {

    /**
     * Constant representing the property name for tags.
     */
    static final String TAG_PROPERTY = "TAG";

    /**
     * The property name for storing the document summary in the Alfresco repository obtained from configuration.
     */
    @Value("${content.service.summary.summary.property}")
    String summaryProperty;

    /**
     * The property name for storing the document tags in the Alfresco repository obtained from configuration.
     */
    @Value("${content.service.summary.tags.property}")
    String summaryTagsProperty;

    /**
     * The property name for storing the document model information in the Alfresco repository obtained from configuration.
     */
    @Value("${content.service.summary.model.property}")
    String summaryModelProperty;

    /**
     * The property name for storing the answer content in the Alfresco repository obtained from configuration.
     */
    @Value("${content.service.prompt.answer.property}")
    private String answerProperty;

    /**
     * The property name for storing the answer model information in the Alfresco repository obtained from configuration.
     */
    @Value("${content.service.prompt.model.property}")
    private String answerModelProperty;

    /**
     * Property that includes a list of terms for classification.
     */
    @Value("${content.service.classify.terms.property}")
    private String termsProperty;

    /**
     * The property name for storing the term content in the Alfresco repository obtained from configuration.
     */
    @Value("${content.service.classify.term.property}")
    private String termProperty;

    /**
     * The property name for storing the answer model information in the Alfresco repository obtained from configuration.
     */
    @Value("${content.service.classify.model.property}")
    private String termModelProperty;

    /**
     * Autowired instance of {@link NodesApi} for communication with the Alfresco Nodes API.
     */
    @Autowired
    NodesApi nodesApi;

    /**
     * Autowired instance of {@link TagsApi} for communication with the Alfresco Tags API.
     */
    @Autowired
    TagsApi tagsApi;

    /**
     * Updates the node properties and creates tags for the document identified by its UUID based on the provided
     * {@link Summary} object.
     *
     * @param uuid     The unique identifier of the document node.
     * @param summary  The {@link Summary} object containing summary, tags, and model information.
     */
    public void updateNodeSummary(String uuid, Summary summary) {

        Map<String, Object> properties = new HashMap<>();
        properties.put(summaryProperty, summary.getSummary());
        if (!summaryModelProperty.equals(TAG_PROPERTY)) {
            properties.put(summaryModelProperty, summary.getModel());
        }
        if (!summaryTagsProperty.equals(TAG_PROPERTY)) {
            properties.put(summaryTagsProperty, summary.getTags());
        }
        nodesApi.updateNode(uuid,
                new NodeBodyUpdate().properties(properties),
                null, null);

        if (summaryModelProperty.equals(TAG_PROPERTY)) {
            tagsApi.createTagForNode(uuid, new TagBody().tag(summary.getModel()), null);
        }

        if (summaryTagsProperty.equals(TAG_PROPERTY)) {
            summary.getTags().forEach(tag -> {
                tagsApi.createTagForNode(uuid, new TagBody().tag(tag.replace('.', ' ').trim()), null);
            });
        }

    }

    /**
     * Updates the node properties with answer content and model information for the document identified by its UUID based
     * on the provided {@link Answer} object.
     *
     * @param uuid    The unique identifier of the document node.
     * @param answer  The {@link Answer} object containing the answer content and model information.
     */
    public void updateNodeAnswer(String uuid, Answer answer) {
        nodesApi.updateNode(uuid,
                new NodeBodyUpdate()
                        .properties(Map.of(
                                answerProperty, answer.getAnswer(),
                                answerModelProperty, answer.getModel())),
                null, null);
    }

    /**
     * Updates the node properties with term content and model information for the document identified by its UUID based
     * on the provided {@link Term} object.
     *
     * @param uuid  The unique identifier of the document node.
     * @param term  The {@link Term} object containing the answer content and model information.
     */
    public void updateNodeTerm(String uuid, Term term) {
        nodesApi.updateNode(uuid,
                new NodeBodyUpdate()
                        .properties(Map.of(
                                termProperty, term.getTerm(),
                                termModelProperty, term.getModel())),
                null, null);
    }

    /**
     * Gets the list of terms stored in the primary parent of the document uuid
     *
     * @param uuid  The unique identifier of the document node.
     */
    public String getTermList(String uuid) {
        String primaryParentId =
                nodesApi.listParents(uuid, "(isPrimary=true)", null, 0, 1, false, null)
                        .getBody()
                        .getList()
                        .getEntries()
                        .get(0)
                        .getEntry()
                        .getId();
        Map<String, Serializable> properties = (Map<String, Serializable>)
                nodesApi.getNode(primaryParentId, null, null, null)
                .getBody()
                .getEntry().getProperties();
        return properties.get(termsProperty).toString().replace("[", "").replace("]", "");
    }
}


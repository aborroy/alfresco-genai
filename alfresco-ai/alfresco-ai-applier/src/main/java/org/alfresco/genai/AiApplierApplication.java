package org.alfresco.genai;

import org.alfresco.genai.action.AiApplierAction;
import org.alfresco.genai.action.AiApplierActionFactory;
import org.alfresco.search.handler.SearchApi;
import org.alfresco.search.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * The main class for the AI Applier application, {@code AiApplierApplication},
 * is responsible for various AI actions on Alfresco documents.
 * <p>
 * The Summarizing action involves retrieving documents from a folder using the Alfresco Search API,
 * checking for the availability of PDF renditions, and updating document nodes with summaries
 * obtained from the GenAi service.
 * <p>
 * The Classifying action retrieves documents from a folder using the Alfresco Search API,
 * checks for the availability of PDF renditions, and updates document nodes by selecting a term
 * from a list of terms using the GenAi service.
 */

@SpringBootApplication
public class AiApplierApplication implements CommandLineRunner {

    static final Logger LOG = LoggerFactory.getLogger(AiApplierApplication.class);

    /**
     * Action to be applied (SUMMARY, CLASSIFY)
     */
    @Value("${applier.action}")
    AiApplierAction.Action actionName;

    /**
     * Factory to retrieve the Action to be executed (AiApplierClassify, AiApplierSummary)
     */
    @Autowired
    AiApplierActionFactory aiApplierActionFactory;

    /**
     * Maximum number of items to process in each iteration
     */
    @Value("${request.max.items}")
    int maxItems;

    /**
     * Root Alfresco Repository folder to apply GenAI action
     */
    @Value("${applier.root.folder}")
    String folder;

    /**
     * Http client for searching and retrieving documents from Alfresco
     */
    @Autowired
    SearchApi searchApi;

    /**
     * List to keep track of updated document nodes to avoid redundant updates
     */
    List<String> updatedNodes = new ArrayList<>();

    /**
     * Runs the application logic to summarize or classify documents. Retrieves documents from the search API, checks for PDF
     * renditions, and updates document nodes with response from GenAI.
     */
    @Override
    public void run(String... args) {

        AiApplierAction action = aiApplierActionFactory.getAiApplierAction(actionName);

        RequestSortDefinition sortDefinition = new RequestSortDefinition();
        sortDefinition.add(new RequestSortDefinitionInner()
                .type(RequestSortDefinitionInner.TypeEnum.FIELD)
                .field("id")
                .ascending(true));

        boolean hasMoreItems;

        do {

            ResponseEntity<ResultSetPaging> results = searchApi.search(
                    new SearchRequest()
                            .query(new RequestQuery()
                                    .language(RequestQuery.LanguageEnum.AFTS)
                                    .query("PATH:\"" + folder + "//*\" AND NOT EXISTS:\"" + action.getUpdateField() + "\""))
                            .sort(sortDefinition)
                            .paging(new RequestPagination().maxItems(maxItems).skipCount(0)));

            LOG.info("Processing {} documents of a total of {}",
                    results.getBody().getList().getEntries().size(),
                    results.getBody().getList().getPagination().getTotalItems());

            Instant start = Instant.now();

            results.getBody().getList().getEntries().parallelStream().forEach((entry) -> {

                String uuid = entry.getEntry().getId();

                // Fix SOLR latency to catch up with node updates in Repository
                if (!updatedNodes.contains(uuid)) {
                    if (action.execute(entry)) {
                        updatedNodes.add(uuid);
                    }
                }

            });

            Instant finish = Instant.now();
            LOG.debug(">> {} seconds spent in this iteration", Duration.between(start, finish).toSeconds());

            hasMoreItems = results.getBody().getList().getPagination().isHasMoreItems();

        } while (hasMoreItems);

        LOG.info("END: All documents have been processed. The app may need to be executed again for nodes without existing PDF rendition.");

    }

    public static void main(String[] args) {
        SpringApplication.run(AiApplierApplication.class, args);
    }

}

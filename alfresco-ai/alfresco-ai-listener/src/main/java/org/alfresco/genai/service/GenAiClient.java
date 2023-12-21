package org.alfresco.genai.service;

import jakarta.annotation.PostConstruct;
import okhttp3.*;
import org.alfresco.genai.model.Answer;
import org.alfresco.genai.model.Summary;
import org.alfresco.genai.model.Term;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The {@code GenAiClient} class is a Spring service that interacts with the GenAI service to obtain document summaries
 * and answers to specific questions. It uses an {@link OkHttpClient} to perform HTTP requests to the GenAI service
 * endpoint, and it is configured with properties such as the GenAI service URL and request timeout.
 */
@Service
public class GenAiClient {

    /**
     * The base URL of the GenAI service obtained from configuration.
     */
    @Value("${genai.url}")
    String genaiUrl;

    /**
     * The request timeout for GenAI service requests obtained from configuration.
     */
    @Value("${genai.request.timeout}")
    Integer genaiTimeout;

    /**
     * Static instance of {@link JsonParser} to parse JSON responses from the GenAI service.
     */
    static final JsonParser JSON_PARSER = JsonParserFactory.getJsonParser();

    /**
     * The OkHttpClient instance for making HTTP requests to the GenAI service.
     */
    OkHttpClient client;

    /**
     * Initializes the OkHttpClient with specified timeouts during bean creation.
     */
    @PostConstruct
    public void init() {
        client = new OkHttpClient()
                .newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(genaiTimeout, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Retrieves a document summary from the GenAI service for the provided PDF file.
     *
     * @param pdfFile The PDF file for which the summary is requested.
     * @return A {@link Summary} object containing the summary, tags, and model information.
     * @throws IOException If an I/O error occurs during the HTTP request or response processing.
     */
    public Summary getSummary(File pdfFile) throws IOException {

        RequestBody requestBody = new MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", pdfFile.getName(), RequestBody.create(pdfFile, MediaType.parse("application/pdf")))
                .build();

        Request request = new Request
                .Builder()
                .url(genaiUrl + "/summary")
                .post(requestBody)
                .build();

        String response = client.newCall(request).execute().body().string();
        Map<String, Object> aiResponse = JSON_PARSER.parseMap(response);
        return new Summary()
                .summary(aiResponse.get("summary").toString().trim())
                .tags(Arrays.asList(aiResponse.get("tags").toString().split(",", -1)))
                .model(aiResponse.get("model").toString());

    }

    /**
     * Retrieves an answer to a specific question from the GenAI service for the provided PDF file.
     *
     * @param pdfFile   The PDF file containing the document related to the question.
     * @param question  The question for which an answer is requested.
     * @return An {@link Answer} object containing the answer and the model information.
     * @throws IOException If an I/O error occurs during the HTTP request or response processing.
     */
    public Answer getAnswer(File pdfFile, String question) throws IOException {

        RequestBody requestBody = new MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", pdfFile.getName(), RequestBody.create(pdfFile, MediaType.parse("application/pdf")))
                .build();

        HttpUrl httpUrl = HttpUrl.parse(genaiUrl + "/prompt")
                .newBuilder()
                .addQueryParameter("prompt", question)
                .build();

        Request request = new Request
                .Builder()
                .url(httpUrl)
                .post(requestBody)
                .build();

        String response = client.newCall(request).execute().body().string();
        Map<String, Object> aiResponse = JSON_PARSER.parseMap(response);
        return new Answer()
                .answer(aiResponse.get("answer").toString().trim())
                .model(aiResponse.get("model").toString());

    }

    /**
     * Selects a term from a term list using the GenAI service for the provided PDF file.
     *
     * @param pdfFile   The PDF file containing the document related to the question.
     * @param termList  List of terms that includes options to be selected.
     * @return An {@link Term} object containing the term and the model information.
     * @throws IOException If an I/O error occurs during the HTTP request or response processing.
     */
    public Term getTerm(File pdfFile, String termList) throws IOException {

        RequestBody requestBody = new MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", pdfFile.getName(), RequestBody.create(pdfFile, MediaType.parse("application/pdf")))
                .build();

        HttpUrl httpUrl = HttpUrl.parse(genaiUrl + "/classify")
                .newBuilder()
                .addQueryParameter("termList", "\"" + termList + "\"")
                .build();

        Request request = new Request
                .Builder()
                .url(httpUrl)
                .post(requestBody)
                .build();

        String response = client.newCall(request).execute().body().string();

        Map<String, Object> aiResponse = JSON_PARSER.parseMap(response);
        return new Term()
                .term(aiResponse.get("term").toString().trim())
                .model(aiResponse.get("model").toString());

    }

}

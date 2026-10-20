package cz.cvut.kbss.annotace.lemmatizerllm.llm_api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.kbss.annotace.lemmatizerllm.exception.TruncatedResponseException;
import lombok.Setter;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public abstract class BaseLLMClient implements LLMClient {
    protected final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(60))
            .build();

    protected final ObjectMapper objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final String[] apiKey;
    private int currentIndex = 0;
    protected final int maxIndex;

    @Setter
    protected String model;

    public BaseLLMClient(String[] apiKey) {
        this.apiKey = apiKey;
        if (apiKey == null || apiKey.length == 0) {
            throw new IllegalArgumentException("API key is required");
        }
        this.maxIndex = apiKey.length - 1;
    }

    private static final int MAX_TRUNCATION_RETRIES = 3;
    private static final double RETRY_TOKEN_MULTIPLIER = 1.75;

    protected int retryTokenCeiling() {
        return 4096;
    }

    @Override
    public String send(String prompt, int maxTokens) {
        if (prompt == null || prompt.isBlank()) {
            throw new RuntimeException("Prompt cannot be null or empty");
        }

        try {
            HttpRequest request = buildRequest(prompt, maxTokens);
            HttpResponse<String> response = null;

            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("API error: status=%d, body=%s".formatted(
                        response.statusCode(), response.body()));
            }

            final String result;
            result = extractAnswer(response.body()).trim();
            return result;
        }
        catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getApiKey() {
        final String currentApiKey = apiKey[currentIndex];
        ++currentIndex;
        if (currentIndex > maxIndex) {
            currentIndex = 0;
        }
        return currentApiKey;
    }

    protected final String[] getApiKeys() {
        return apiKey;
    }

    protected abstract HttpRequest buildRequest(String prompt, int maxTokens) throws JsonProcessingException;
    protected abstract String extractAnswer(String jsonResponse) throws JsonProcessingException;
}
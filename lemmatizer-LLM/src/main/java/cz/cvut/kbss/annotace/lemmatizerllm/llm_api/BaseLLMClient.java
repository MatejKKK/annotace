package cz.cvut.kbss.annotace.lemmatizerllm.llm_api;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public abstract class BaseLLMClient implements LLMClient {
    protected final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    protected final ObjectMapper objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    protected final String apiKey;

    public BaseLLMClient(String apiKey) {
        this.apiKey = apiKey;
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API key is required");
        }
    }

    @Override
    public String send(String prompt) throws RuntimeException {
        if (prompt == null || prompt.isBlank()) {
            throw new RuntimeException("Prompt cannot be null or empty");
        }

        try {
            HttpRequest request = buildRequest(prompt);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("API error: status=%d, body=%s".formatted(
                        response.statusCode(), response.body()));
            }

            return extractAnswer(response.body()).trim();

        } catch (Exception e) {
            throw new RuntimeException("Error sending request: " + e.getMessage(), e);
        }
    }

    protected abstract HttpRequest buildRequest(String prompt) throws Exception;
    protected abstract String extractAnswer(String jsonResponse) throws Exception;

    @Override
    public boolean isHealthy() {
        try {
            send("Hello");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
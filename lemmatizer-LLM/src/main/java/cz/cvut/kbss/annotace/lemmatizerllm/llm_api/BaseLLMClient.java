package cz.cvut.kbss.annotace.lemmatizerllm.llm_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.kbss.annotace.lemmatizerllm.exception.TruncatedResponseException;

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

    private final String[] apiKey;
    private int currentIndex = 0;
    private final int maxIndex;
    private int attempts = 0;

    public BaseLLMClient(String[] apiKey) {
        this.apiKey = apiKey;
        if (apiKey == null || apiKey.length == 0) {
            throw new IllegalArgumentException("API key is required");
        }
        this.maxIndex = apiKey.length - 1;
    }

    protected final String getApiKey() {
        final String currentApiKey = apiKey[currentIndex];
        ++currentIndex;
        if (currentIndex > maxIndex) {
            currentIndex = 0;
        }
        return currentApiKey;
    }

    private static final int MAX_TRUNCATION_RETRIES = 3;
    private static final double RETRY_TOKEN_MULTIPLIER = 1.75;

    protected int retryTokenCeiling() {
        return 4096;
    }

    @Override
    public String send(String prompt, int maxTokens) throws RuntimeException {
        if (prompt == null || prompt.isBlank()) {
            throw new RuntimeException("Prompt cannot be null or empty");
        }

        int currentMaxTokens = maxTokens;
        String reserveResult = "";

        for (int attempt = 0; attempt <= MAX_TRUNCATION_RETRIES; attempt++) {
            try {
                HttpRequest request = buildRequest(prompt, currentMaxTokens);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException("API error: status=%d, body=%s".formatted(
                            response.statusCode(), response.body()));
                }

                final String result = extractAnswer(response.body()).trim();
                attempts = 0;
                return result;
            }
            catch (TruncatedResponseException e) {
                if (attempt == MAX_TRUNCATION_RETRIES) {
                    throw new RuntimeException("Error sending request: " + e.getMessage()
                            + " (gave up after %d retries, last max_tokens=%d)"
                            .formatted(attempt, currentMaxTokens), e);
                }
                currentMaxTokens = Math.min(retryTokenCeiling(),
                        (int) Math.ceil(currentMaxTokens * RETRY_TOKEN_MULTIPLIER));
                System.out.println("Truncated response (finish_reason=length), retrying with max_tokens="
                        + currentMaxTokens);

            }
            catch (RuntimeException e) {
                if (attempt < 3 && e.getMessage().contains("Rate limit reached for model") && e.getMessage().contains("tokens per minute ")) {
                    final int startIndex = e.getMessage().indexOf("Please try again in ");
                    if (startIndex == -1) {
                        throw new RuntimeException("Error sending request: " + e.getMessage(), e);
                    }
                    final int endIndex = e.getMessage().lastIndexOf(". Need more tokens? ");
                    String time = e.getMessage().substring(startIndex + 20, endIndex);
                    final boolean ms = time.contains("ms");
                    while(time.endsWith("s")) {
                        time = time.replace("s", "");
                    }
                    while(time.endsWith("m")) {
                        time = time.replace("s", "");
                    }
                    reserveResult = sendAgain(prompt, maxTokens, Double.parseDouble(time) * (ms ? 1 : 1000));
                }
                else throw new RuntimeException("Error sending request: " + e.getMessage(), e);
            }
            catch (Exception e) {
                throw new RuntimeException("Error sending request: " + e.getMessage(), e);
            }
        }

        attempts = 0;
        return reserveResult;
    }

    private String sendAgain(String prompt, int maxTokens, double time) {
        try {
            Thread.sleep(Math.min((long) Math.floor(time), 20000L));
        }
        catch (InterruptedException ignore) {}
        ++attempts;
        return send(prompt, maxTokens);
    }

    protected abstract HttpRequest buildRequest(String prompt, int maxTokens) throws Exception;
    protected abstract String extractAnswer(String jsonResponse) throws Exception;
}
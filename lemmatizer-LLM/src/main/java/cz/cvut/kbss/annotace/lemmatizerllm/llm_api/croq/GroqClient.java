package cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.BaseLLMClient;
import cz.cvut.kbss.annotace.lemmatizerllm.exception.TruncatedResponseException;
import lombok.Setter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GroqClient extends BaseLLMClient {
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    // openai/gpt-oss-20b and openai/gpt-oss-120b support "low" | "medium" | "high".
    // Groq's default is "medium", which eats a large chunk of max_tokens on hidden
    // reasoning before the model writes any actual output - "low" leaves much more
    // room for the answer at the same token budget. reasoning_format isn't supported
    // for gpt-oss models, so this is the only lever to reduce that overhead.
    @Setter
    private String reasoningEffort = "low";

    private final Map<String, Integer> models = new HashMap<>();

    // Running conversation for sendWithMemory(). Empty => no thread started yet.
    // send(...) never touches this, so one-shot and memory-based calls on the same
    // client instance don't interfere with each other.
    private final List<GroqRequest.Message> conversationHistory = new ArrayList<>();

    public GroqClient(String[] apiKey, String model) {
        super(apiKey);
        this.model = model != null ? model : "meta-llama/llama-prompt-guard-2-86m";
        this.models.put(model, 0);
    }

    // ---- one-shot, no memory (unchanged behaviour, used via inherited send()) ----

    @Override
    protected HttpRequest buildRequest(String prompt, int maxTokens) throws JsonProcessingException {
        System.out.println(model + ": " + prompt);
        return buildRequest(List.of(new GroqRequest.Message("user", prompt)), maxTokens);
    }

    // ---- shared request builder for both one-shot and memory-based calls ----

    private HttpRequest buildRequest(List<GroqRequest.Message> messages, int maxTokens) throws JsonProcessingException {
        if (model.contains("120b")) { System.out.println("tokens: " + maxTokens); }
        String jsonBody = objectMapper.writeValueAsString(
                new GroqRequest(model, messages, maxTokens, 0.0, reasoningEffort)
        );

        return HttpRequest.newBuilder(URI.create(GROQ_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer %s".formatted(getApiKey()))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
    }

    /**
     * Appends {@code prompt} to the running conversation and sends the WHOLE history
     * so far (Groq keeps no state server-side - see console.groq.com/docs/responses-api),
     * so every call still carries the full accumulated context, incl. the rules/examples
     * from the first turn. On success the answer is appended too, so the next call
     * continues the same thread; a failed call is rolled back so a retry with the same
     * prompt doesn't duplicate it in history.
     *
     * There is intentionally no separate "first call" method - an empty history simply
     * means this call seeds the conversation, a non-empty one means it's a follow-up.
     */
    public String sendWithMemory(String prompt, int maxTokens) {
        if (prompt == null || prompt.isBlank()) {
            throw new RuntimeException("Prompt cannot be null or empty");
        }

        System.out.println(model + " (memory, turn " + (conversationHistory.size() / 2 + 1) + "): " + prompt);
        conversationHistory.add(new GroqRequest.Message("user", prompt));

        try {
            HttpRequest request = buildRequest(conversationHistory, maxTokens);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                conversationHistory.removeLast();
                throw new RuntimeException("API error: status=%d, body=%s".formatted(
                        response.statusCode(), response.body()));
            }

            String result = extractAnswer(response.body()).trim();
            conversationHistory.add(new GroqRequest.Message("assistant", result));
            return result;
        }
        catch (TruncatedResponseException | IllegalStateException e) {
            // no usable answer came back - drop the dangling user turn so a retry
            // (e.g. with a bumped max_tokens) re-adds it cleanly instead of twice
            conversationHistory.removeLast();
            throw e;
        }
        catch (IOException | InterruptedException e) {
            conversationHistory.removeLast();
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            conversationHistory.removeLast();
            throw e;
        }
    }

    /** Starts a fresh conversation thread - call between paragraphs/model switches. */
    public void resetMemory() {
        conversationHistory.clear();
    }

    public boolean hasMemory() {
        return !conversationHistory.isEmpty();
    }

    // ---- unchanged ----

    @Override
    protected String extractAnswer(String jsonResponse) throws JsonProcessingException {
        GroqResponse response = objectMapper.readValue(jsonResponse, GroqResponse.class);
        List<GroqResponse.Choice> choices = response.choices();

        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("No choices in response");
        }

        GroqResponse.Choice choice = choices.getFirst();
        String content = choice.message().content();

        if (content == null || content.isBlank()) {
            String finishReason = choice.finish_reason();
            String message = ("Empty content in response (finish_reason=%s). This usually means the whole "
                    + "max_tokens budget was spent on hidden reasoning before any answer text was "
                    + "written - raise max_tokens for this prompt and/or lower reasoning_effort.")
                    .formatted(finishReason);

            if ("length".equals(finishReason)) {
                throw new TruncatedResponseException(message);
            }
            throw new IllegalStateException(message);
        }

        return content;
    }

    @Override
    protected int retryTokenCeiling() {
        return getMaxToken();
    }

    private int getMaxToken() {
        return switch (this.model) {
            case "openai/gpt-oss-20b" -> 4096;
            case "qwen/qwen3.8-27b" -> 2048;
            case "meta-llama/llama-prompt-guard-2-86m" -> 500;
            default -> 8192 * 2;
        };
    }

    @Override
    public void setModel(String model) {
        this.model = model;
        if (!models.containsKey(model)) {
            models.put(model, 0);
        }
    }

    @Override
    protected String getApiKey() {
        Integer currentModelIndex = models.get(model);
        final String currentApiKey = getApiKeys()[models.get(model)];
        ++currentModelIndex;
        if (currentModelIndex > maxIndex) {
            currentModelIndex = 0;
        }
        models.put(model, currentModelIndex);
        return currentApiKey;
    }
}
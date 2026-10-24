package cz.cvut.kbss.annotace.lemmatizerllm.llm_api.einfra;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.cvut.kbss.annotace.lemmatizerllm.exception.TruncatedResponseException;
import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.BaseLLMClient;
import lombok.Setter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for e-infra.cz's shared LLM API (<a href="https://llm.ai.e-infra.cz">...</a>), a LiteLLM proxy
 * in front of vLLM-hosted open models - OpenAI-compatible chat/completions, documented
 * at docs.cerit-sc.cz/en/docs/ai-as-a-service/ai-api. Free for academic/research use via
 * an e-infra.cz account; get a key at <a href="https://chat.ai.e-infra.cz">...</a> (Settings > Account >
 * API keys), not the JWT token shown on the same page.
 * Differences from GroqClient that shaped this implementation:
 *  - reasoning_effort has no documented support here for gpt-oss models, and there's an
 *    open LiteLLM bug for exactly that combination via the hosted_vllm provider
 *    (BerriAI/litellm#14225) - so it defaults to null/omitted (see EInfraRequest) rather
 *    than being set to "low" by default as it is for Groq. Enable it yourself only after
 *    verifying it doesn't break your chosen model/route.
 *  - No documented rate-limit error format, so this client doesn't attempt to parse a
 *    retry-after value out of error bodies the way the Groq proxy does for Groq's own
 *    "tokens per minute" message - a caller-side retry loop should just back off generically.
 *  - Model catalog is heterogeneous (llama3.3, deepseek, qwen2.5-coder, gpt-oss-120b,
 *    kimi-k2.6, ...) and the docs page itself contains inconsistent examples (a "mini"
 *    model used in one example that isn't in the listed IDs) - always confirm the exact
 *    model id with GET /v1/models before relying on it.
 *  - Non-streaming requests have a hard 30-minute infrastructure timeout that cannot be
 *    raised; this client sets a 25-minute per-request timeout so a stuck call fails on
 *    our side first instead of silently hanging for half an hour.
 */
public class EInfraClient extends BaseLLMClient {
    private static final String E_INFRA_URL = "https://llm.ai.e-infra.cz/v1/chat/completions";
    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(25);

    // Null by default - see class javadoc. Set explicitly only if verified for your model.
    @Setter
    private String reasoningEffort = null;

    private final Map<String, Integer> models = new HashMap<>();

    // Running conversation for sendWithMemory(). Empty => no thread started yet.
    // send(...) never touches this, so one-shot and memory-based calls on the same
    // client instance don't interfere with each other.
    private final List<EInfraRequest.Message> conversationHistory = new ArrayList<>();

    public EInfraClient(String[] apiKey, String model) {
        super(apiKey);
        // gpt-oss-120b is confirmed present via /v1/model/info at time of writing;
        // re-check GET /v1/models if this stops resolving.
        this.model = model != null ? model : "gpt-oss-120b";
        this.models.put(this.model, 0);
    }

    // ---- one-shot, no memory (inherited send()) ----

    @Override
    protected HttpRequest buildRequest(String prompt, int maxTokens) throws JsonProcessingException {
        System.out.println(model + ": " + prompt);
        return buildRequest(List.of(new EInfraRequest.Message("user", prompt)), maxTokens);
    }

    // ---- shared request builder for both one-shot and memory-based calls ----

    private HttpRequest buildRequest(List<EInfraRequest.Message> messages, int maxTokens) throws JsonProcessingException {
        String jsonBody = objectMapper.writeValueAsString(
                new EInfraRequest(model, messages, maxTokens, 0.0, reasoningEffort)
        );

        return HttpRequest.newBuilder(URI.create(E_INFRA_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer %s".formatted(getApiKey()))
                .timeout(REQUEST_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
    }

    // ---- stateful call: covers both "first" and "subsequent" turns ----

    /**
     * Appends {@code prompt} to the running conversation and sends the WHOLE history so
     * far - e-infra's LiteLLM proxy keeps no per-key conversation state either, so every
     * call must still carry the full accumulated context (rules/examples from the first
     * turn included). On success the answer is appended too, so the next call continues
     * the same thread; a failed call is rolled back so a retry with the same prompt
     * doesn't duplicate it in history.
     * There is intentionally no separate "first call" method - an empty history simply
     * means this call seeds the conversation, a non-empty one means it's a follow-up.
     */
    public String sendWithMemory(String prompt, int maxTokens) {
        if (prompt == null || prompt.isBlank()) {
            throw new RuntimeException("Prompt cannot be null or empty");
        }

        System.out.println(model + " (memory, turn " + (conversationHistory.size() / 2 + 1) + "): " + prompt);
        conversationHistory.add(new EInfraRequest.Message("user", prompt));

        try {
            HttpRequest request = buildRequest(conversationHistory, maxTokens);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                conversationHistory.removeLast();
                throw new RuntimeException("API error: status=%d, body=%s".formatted(
                        response.statusCode(), response.body()));
            }

            String result = extractAnswer(response.body()).trim();
            conversationHistory.add(new EInfraRequest.Message("assistant", result));
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
    }

    /** Starts a fresh conversation thread - call between paragraphs/model switches. */
    public void resetMemory() {
        conversationHistory.clear();
    }

    public boolean hasMemory() {
        return !conversationHistory.isEmpty();
    }

    // ---- response parsing ----

    @Override
    protected String extractAnswer(String jsonResponse) throws JsonProcessingException {
        EInfraResponse response = objectMapper.readValue(jsonResponse, EInfraResponse.class);
        List<EInfraResponse.Choice> choices = response.choices();

        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("No choices in response");
        }

        EInfraResponse.Choice choice = choices.getFirst();
        String content = choice.message().content();

        if (content == null || content.isBlank()) {
            String finishReason = choice.finish_reason();
            String message = ("Empty content in response (finish_reason=%s). This usually means the whole "
                    + "max_tokens budget was spent before any answer text was written - raise max_tokens "
                    + "for this prompt (reasoning_effort is not a reliable lever here - see class javadoc).")
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
        // No documented per-model context/output ceiling split like Groq's; model/info
        // reports e.g. 131072 context for gpt-oss-120b, so this is a conservative,
        // output-side cap rather than the true model limit.
        return 8192 * 2;
    }

    @Override
    public void setModel(String model) {
        this.model = model;
        if (!models.containsKey(model)) {
            models.put(model, 0);
        }
    }

    // ---- per-model key rotation (same scheme as GroqClient) ----

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

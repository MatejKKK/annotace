package cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq;

import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.BaseLLMClient;
import cz.cvut.kbss.annotace.lemmatizerllm.exception.TruncatedResponseException;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;

public class GroqClient extends BaseLLMClient {
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Setter
    private String model;

    // openai/gpt-oss-20b and openai/gpt-oss-120b support "low" | "medium" | "high".
    // Groq's default is "medium", which eats a large chunk of max_tokens on hidden
    // reasoning before the model writes any actual output - "low" leaves much more
    // room for the answer at the same token budget. reasoning_format isn't supported
    // for gpt-oss models, so this is the only lever to reduce that overhead.
    @Setter
    private String reasoningEffort = "low";

    public GroqClient(String[] apiKey, String model) {
        super(apiKey);
        this.model = model != null ? model : "meta-llama/llama-prompt-guard-2-86m";
    }

    @Override
    protected HttpRequest buildRequest(String prompt, int maxTokens) throws Exception {
        if (model.contains("120b")) { System.out.println("tokens: " + maxTokens); }
        String jsonBody = objectMapper.writeValueAsString(
                new GroqRequest(
                        model,
                        List.of(new GroqRequest.Message("user", prompt)),
                        maxTokens,
                        0.0,
                        reasoningEffort
                )
        );

        return HttpRequest.newBuilder(URI.create(GROQ_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer %s".formatted(getApiKey()))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
    }

    @Override
    protected String extractAnswer(String jsonResponse) throws Exception {
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
}
package cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq;

import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.BaseLLMClient;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;

public class GroqClient extends BaseLLMClient {
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Setter
    private String model;

    public GroqClient(String apiKey, String model) {
        super(apiKey);
        this.model = model != null ? model : "llama-3.3-70b-versatile";
    }

    @Override
    protected HttpRequest buildRequest(String prompt) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(
                new GroqRequest(
                        model,
                        List.of(new GroqRequest.Message("user", prompt)),
                        2048,
                        0.0
                )
        );

        return HttpRequest.newBuilder(URI.create(GROQ_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer %s".formatted(apiKey))
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

        return choices.getFirst().message().content();
    }
}

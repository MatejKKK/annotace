package cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq;

import java.util.List;

public record GroqRequest(
        String model,
        List<Message> messages,
        int max_completion_tokens,
        double temperature,
        String reasoning_effort
) {
    record Message(String role, String content) {}
}
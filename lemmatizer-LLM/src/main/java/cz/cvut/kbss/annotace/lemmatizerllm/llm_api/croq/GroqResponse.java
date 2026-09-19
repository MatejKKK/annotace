package cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq;

import java.util.List;

public record GroqResponse(List<Choice> choices) {
    record Choice(Message message) {}
    record Message(String content) {}
}
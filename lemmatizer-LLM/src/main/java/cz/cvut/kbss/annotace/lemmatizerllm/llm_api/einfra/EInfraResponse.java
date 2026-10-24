package cz.cvut.kbss.annotace.lemmatizerllm.llm_api.einfra;

import java.util.List;

public record EInfraResponse(List<Choice> choices) {
    public record Choice(Message message, String finish_reason) {}
    public record Message(String content) {}
}
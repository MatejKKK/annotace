package cz.cvut.kbss.annotace.lemmatizerllm.llm_api.einfra;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Request body for e-infra.cz's OpenAI-compatible chat completions endpoint
 * (<a href="https://llm.ai.e-infra.cz/v1/chat/completions">...</a>, LiteLLM in front of vLLM).
 * Unlike Groq, e-infra documents no reasoning_effort/reasoning_format support for
 * gpt-oss models, and there's an open LiteLLM bug (BerriAI/litellm#14225) reporting
 * errors when reasoning_effort is sent to gpt-oss via the hosted_vllm provider.
 * reasoning_effort is therefore nullable and omitted (@JsonInclude.NON_NULL) unless
 * explicitly set - send it only if you've verified it works for your model/route.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EInfraRequest(
        String model,
        List<Message> messages,
        int max_tokens,
        double temperature,
        String reasoning_effort
) {
    public record Message(String role, String content) {}
}

package cz.cvut.kbss.annotace.lemmatizerllm.llm_api;

public interface ProxyInterface {
    String send(String prompt, int maxTokens);
}

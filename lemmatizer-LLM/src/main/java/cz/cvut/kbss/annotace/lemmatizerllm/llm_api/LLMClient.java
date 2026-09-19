package cz.cvut.kbss.annotace.lemmatizerllm.llm_api;

public interface LLMClient {
    String send(String prompt) throws Exception;
    boolean isHealthy();
}
package cz.cvut.kbss.annotace.lemmatizerllm.configuration;

public record LLMTermExtractorConf (
        Api api,
        String[] apiKeys,
        String delimiter
) {
}

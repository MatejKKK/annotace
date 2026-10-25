package cz.cvut.kbss.annotace.lemmatizerllm.configuration;

public record LLMConf(
        Api api,
        String[] apiKeys,
        String delimiter,
        int maxSentenceLength,
        int maxParagraphLength,
        boolean usePostProcessing,
        boolean selectOneCharWords
) {
}
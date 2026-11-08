package cz.cvut.kbss.annotace.lemmatizerllm.configuration;

public record LLMLemmatizerConf(
        Api api,
        String[] apiKeys,
        String delimiter,
        int maxSentenceLength,
        int maxParagraphLength,
        boolean usePostProcessing,
        boolean selectOneCharWords
) {
}
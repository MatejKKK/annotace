package cz.cvut.kbss.annotace.lemmatizerllm.configuration;

public record LLMConf (
        String delimiter,
        int maxSentenceLength,
        int maxParagraphLength,
        boolean usePostProcessing
) {
    public LLMConf(String delimiter, int maxSentenceLength, boolean usePostProcessing) {
        this(
                delimiter,
                maxSentenceLength,
                (int) Math.floor(maxSentenceLength * Math.pow(1.1, maxSentenceLength)),
                usePostProcessing
        );
    }
}
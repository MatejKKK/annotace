package cz.cvut.kbss.annotace.lemmatizerllm.configuration;

import lombok.Getter;

@Getter
public class LLMConf {
    private final String delimiter;
    private final int maxSentenceLength;
    private final int maxParagraphLength;

    public LLMConf(String delimiter, int maxSentenceLength) {
        this.delimiter = delimiter;
        this.maxSentenceLength = maxSentenceLength;
        this.maxParagraphLength = (int) Math.floor(20. * maxSentenceLength / Math.log(maxSentenceLength));
    }
}
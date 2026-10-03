package cz.cvut.kbss.annotace.lemmatizerllm.configuration;

public record LLMConf(
        String delimiter, int maxSentenceLength
) {

}

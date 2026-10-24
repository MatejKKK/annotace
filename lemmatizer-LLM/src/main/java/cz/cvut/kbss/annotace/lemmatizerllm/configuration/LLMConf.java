package cz.cvut.kbss.annotace.lemmatizerllm.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("annotace.llm")
public class LLMConf {
    private Api api;
    private String delimiter;
    private int maxSentenceLength;
    private int maxParagraphLength;
    private boolean usePostProcessing;
    private String[] apiKeys;
    private boolean selectOneCharWords = true;

    public LLMConf(
            Api api,
            String delimiter,
            int maxSentenceLength,
            int maxParagraphLength,
            boolean usePostProcessing,
            String[] apiKeys
    ) {
        this.api = api;
        this.delimiter = delimiter;
        this.maxSentenceLength = maxSentenceLength;
        this.maxParagraphLength = maxParagraphLength;
        this.usePostProcessing = usePostProcessing;
        this.apiKeys = apiKeys;
    }

    public LLMConf() {
        this.api = Api.GROQ;
        this.delimiter = "^";
        this.maxSentenceLength = 500;
        this.maxParagraphLength = (int) Math.floor(
                3 * Math.E * maxSentenceLength * Math.log(2 * Math.E) / Math.log(maxSentenceLength)
        );
        this.usePostProcessing = true;
        this.apiKeys = new String[]{"GROQ_API_KEY_2", "GROQ_API_KEY"};
    }

    public Api api() {
        return api;
    }

    public String delimiter() {
        return delimiter;
    }

    public int maxSentenceLength() {
        return maxSentenceLength;
    }

    public int maxParagraphLength() {
        return maxParagraphLength;
    }

    public boolean usePostProcessing() {
        return usePostProcessing;
    }

    public String[] apiKeys() {
        return apiKeys;
    }

    public boolean selectOneCharWords() {
        return selectOneCharWords;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LLMConf) obj;
        return Objects.equals(this.api, that.api) &&
                Objects.equals(this.delimiter, that.delimiter) &&
                this.maxSentenceLength == that.maxSentenceLength &&
                this.maxParagraphLength == that.maxParagraphLength &&
                this.usePostProcessing == that.usePostProcessing &&
                Objects.equals(this.apiKeys, that.apiKeys);
    }
}
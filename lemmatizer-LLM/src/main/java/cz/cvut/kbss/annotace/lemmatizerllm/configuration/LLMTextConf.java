package cz.cvut.kbss.annotace.lemmatizerllm.configuration;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("annotace.llm")
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class LLMTextConf {
    private String api;
    private String apiKeys;
    private String delimiter;
    private String maxSentenceLength;
    private String usePostProcessing;
    private String selectOneCharWords;

    public LLMConf toLLMConf() {
        return new LLMConf(
                Api.valueOf(api),
                apiKeys.split(","),
                delimiter,
                Integer.parseInt(maxSentenceLength),
                (int) Math.floor(Double.parseDouble(maxSentenceLength) * Math.pow(1.1, Double.parseDouble(maxSentenceLength))),
                usePostProcessing.equals("true"),
                selectOneCharWords.equals("true")
        );
    }
}

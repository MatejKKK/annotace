package cz.cvut.kbss.annotace.lemmatizerllm.configuration;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("annotace.llm-te")
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class LLMTermExtractorTextConf {
    private String api;
    private String apiKeys;
    private String delimiter;

    public LLMTermExtractorConf toLLMTermExtractorConf() {
        return new LLMTermExtractorConf(
                Api.valueOf(api),
                apiKeys.split(","),
                delimiter
        );
    }
}

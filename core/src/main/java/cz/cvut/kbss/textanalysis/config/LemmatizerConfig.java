/*
 * Annotace
 * Copyright (C) 2026 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.textanalysis.config;

import cz.cvut.kbss.annotace.configuration.MorphoditaConf;
import cz.cvut.kbss.annotace.configuration.SparkConf;
import cz.cvut.kbss.annotace.lemmatizer.MorphoDitaServiceJNI;
import cz.cvut.kbss.annotace.lemmatizer.MorphoDitaServiceOnline;
import cz.cvut.kbss.annotace.lemmatizer.SparkLemmatizer;
import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMConf;
import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMTextConf;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.LLMService;
import cz.cvut.kbss.textanalysis.lemmatizer.LemmatizerApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@Slf4j
public class LemmatizerConfig {

    @Value("${annotace.lemmatizer:spark}")
    private String lemmatizer;

    @Bean
    public LemmatizerApi lemmatizer(ApplicationContext context) {
        return switch (lemmatizer) {
            case "morphodita-jni" -> {
                log.info("Instantiating MorphoDiTa JNI lemmatizer.");
                yield new MorphoDitaServiceJNI(context.getBean(MorphoditaConf.class));
            }
            case "morphodita-online" -> {
                log.info("Instantiating MorhoDiTa online lemmatizer.");
                yield new MorphoDitaServiceOnline(RestClient.builder(), context.getBean(
                        MorphoditaConf.class));
            }
            case "llm" -> {
                log.info("Instantiating LLM lemmatizer.");
                yield new LLMService(context.getBean(LLMTextConf.class));
            }
            default -> {
                log.info("Instantiating Apache Spark lemmatizer.");
                yield new SparkLemmatizer(context.getBean(SparkConf.class));
            }
        };
    }
}

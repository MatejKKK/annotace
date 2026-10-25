package einfra.training_test;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMTextConf;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.LLMService;

abstract class AbstractTextTest {

    protected AbstractTextTest() {
        lemmatizer = new LLMService(new LLMTextConf(
                "EINFRA_GLM", "INFRA_1", "^", "400", "true", "false"
        ));
    }

    protected final LLMService lemmatizer;

    protected abstract void testText(String input, String expected, String expectedParagraphsCountParam);
}

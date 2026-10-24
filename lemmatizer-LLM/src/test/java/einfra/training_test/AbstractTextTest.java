package einfra.training_test;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMConfSimpleFactory;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.LLMService;

@SuppressWarnings("unused")
abstract class AbstractTextTest {

    protected AbstractTextTest() {
        lemmatizer = new LLMService(LLMConfSimpleFactory.createLLMConf(
                "EINFRA_GLM", "^", "200", "true", "INFRA_1"
        ));
    }

    protected final LLMService lemmatizer;

    protected abstract void testText(String input, String expected, String expectedParagraphsCountParam);
}

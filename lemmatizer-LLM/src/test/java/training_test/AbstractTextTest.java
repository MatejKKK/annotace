package training_test;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMConfSimpleFactory;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.LLMService;

@SuppressWarnings("unused")
abstract class AbstractTextTest {

    protected AbstractTextTest() {
        lemmatizer = new LLMService(LLMConfSimpleFactory.createLLMConf(
                "GROQ", "^", "500", "true", "GROQ_API_KEY,GROQ_API_KEY_2"
        ));
    }

    protected final LLMService lemmatizer;

    protected abstract void testText(String input, String expected, String expectedParagraphsCountParam);
}

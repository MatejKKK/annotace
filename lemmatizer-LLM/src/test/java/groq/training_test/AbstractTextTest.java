package groq.training_test;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMTextConf;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.LLMService;

abstract class AbstractTextTest {

    protected AbstractTextTest() {
        lemmatizer = new LLMService(new LLMTextConf(
                "GROQ", "GROQ_API_KEY,GROQ_API_KEY_2", "^", "400", "true", "false"
        ));
    }

    protected final LLMService lemmatizer;

    protected abstract void testText(String input, String expected, String expectedParagraphsCountParam);
}

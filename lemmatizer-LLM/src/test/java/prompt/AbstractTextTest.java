package prompt;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMConf;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.LLMService;

@SuppressWarnings("unused")
abstract public class AbstractTextTest {

    protected AbstractTextTest() {
        lemmatizer = new LLMService(
                new LLMConf("^", 500)
        );
    }

    protected final LLMService lemmatizer;

    protected abstract void testText(String input, String expected, String expectedParagraphsCountParam);
}

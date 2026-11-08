package reference_test;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMLemmatizerTextConf;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.LLMLemmatizer;

abstract class AbstractTextTest {

    protected AbstractTextTest() {
        lemmatizer = new LLMLemmatizer(new LLMLemmatizerTextConf(
                "GROQ", "GROQ_API_KEY,GROQ_API_KEY_2", "^", "500", "true", "false"
        ));
    }

    protected final LLMLemmatizer lemmatizer;

    protected abstract void testText(String input, String expected);
}

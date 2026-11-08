package training_test;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMLemmatizerTextConf;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.LLMLemmatizer;

abstract class AbstractTextTest {

    protected AbstractTextTest() {
        lemmatizer = new LLMLemmatizer(new LLMLemmatizerTextConf(
                //"GROQ", "GROQ_API_KEY,GROQ_API_KEY_2", "^", "500", "true", "false"
                "EINFRA_QWEN", "INFRA_2", "^", "200", "true", "false"
                //"CLAUDE_SONNET", "CLAUDE_API_KEY", "^", "400", "true", "false"
        ));
    }

    protected final LLMLemmatizer lemmatizer;

    protected abstract void testText(String input, String expected, String expectedParagraphsCountParam);
}

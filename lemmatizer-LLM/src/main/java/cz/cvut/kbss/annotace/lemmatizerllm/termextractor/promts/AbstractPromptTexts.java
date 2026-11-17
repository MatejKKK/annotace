package cz.cvut.kbss.annotace.lemmatizerllm.termextractor.promts;

import java.util.List;

public abstract sealed class AbstractPromptTexts permits LongPromptTexts {
    public final static List<String> SUPPORTED_LANGUAGES = List.of("en", "sk", "de", "cs");

    public abstract String CZECH(String terms);
    public abstract String ENGLISH(String terms);
    public abstract String GERMAN(String terms);
    public abstract String SLOVAK(String terms);

    protected AbstractPromptTexts() {}
}

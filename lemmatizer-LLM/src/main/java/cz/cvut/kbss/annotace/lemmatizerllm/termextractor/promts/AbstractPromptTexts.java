package cz.cvut.kbss.annotace.lemmatizerllm.termextractor.promts;

import java.util.List;

public abstract sealed class AbstractPromptTexts permits LongPromptTexts {
    public final static List<String> SUPPORTED_LANGUAGES = List.of("en", "cz", "sk", "de", "cs");

    public abstract String CZECH();
    public abstract String ENGLISH();
    public abstract String GERMAN();
    public abstract String SLOVAK();

    protected AbstractPromptTexts() {}
}

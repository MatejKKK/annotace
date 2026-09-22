package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts;

import java.util.List;

public abstract sealed class AbstractPromptTexts permits ShortPromptTexts, LongPromptTexts, SecondLongPromptTexts {
    public final static List<String> SUPPORTED_LANGUAGES = List.of("en", "cz", "sk", "de");

    public abstract String CZECH();
    public abstract String ENGLISH();
    public abstract String GERMAN();
    public abstract String SLOVAK();

    public abstract String CZECH(String original);
    public abstract String ENGLISH(String original);
    public abstract String GERMAN(String original);
    public abstract String SLOVAK(String original);

    protected AbstractPromptTexts() {}
}

package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts;

import java.util.List;

public abstract sealed class AbstractPromptTexts permits ShortPromptTexts, FirstLongPromptTexts, SecondLongPromptTexts {
    public final static List<String> SUPPORTED_LANGUAGES = List.of("en", "cz", "sk", "de");

    public abstract String CZECH();
    public abstract String ENGLISH();
    public abstract String GERMAN();
    public abstract String SLOVAK();

    protected AbstractPromptTexts() {}

    public void setOriginal(String paragraph) {

    }
}

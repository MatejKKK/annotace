package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer;

import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.AbstractPromptTexts;

public class PromptGenerator {

    private enum Language {
        CZECH("cz"),
        ENGLISH("en"),
        GERMAN("de"),
        SLOVAK("sk");

        private String shortcut;
        private Language(String shortcut) {
            this.shortcut = shortcut;
        }
    }

    private Language language = Language.ENGLISH;

    private AbstractPromptTexts promptTexts;

    public PromptGenerator(AbstractPromptTexts promptTexts) {
        this.promptTexts = promptTexts;
    }

    public void setPromptTexts(AbstractPromptTexts promptTexts) {
        this.promptTexts = promptTexts;
    }

    public void setLanguage(String shortcut) {
        language = Language.valueOf(shortcut);
    }

    public String promt(String paragraph) {
        return switch(this.language) {
            case CZECH -> promptTexts.CZECH();
            case ENGLISH -> promptTexts.ENGLISH();
            case GERMAN -> promptTexts.GERMAN();
            case SLOVAK -> promptTexts.SLOVAK();
        } + paragraph;
    }

    public String promt(String paragraph, String originalParagraph) {
        promptTexts.setOriginal(originalParagraph);
        return switch(this.language) {
            case CZECH -> promptTexts.CZECH();
            case ENGLISH -> promptTexts.ENGLISH();
            case GERMAN -> promptTexts.GERMAN();
            case SLOVAK -> promptTexts.SLOVAK();
        } + paragraph;
    }
}

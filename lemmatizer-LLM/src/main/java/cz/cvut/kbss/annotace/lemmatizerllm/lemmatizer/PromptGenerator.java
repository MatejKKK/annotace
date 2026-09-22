package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer;

import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.AbstractPromptTexts;
import lombok.Setter;

public class PromptGenerator {

    private enum Language {
        CZ("cz"),
        EN("en"),
        DE("de"),
        SK("sk");

        private String shortcut;
        private Language(String shortcut) {
            this.shortcut = shortcut;
        }
    }

    private Language language = Language.EN;

    @Setter
    private AbstractPromptTexts promptTexts;

    public PromptGenerator(AbstractPromptTexts promptTexts) {
        this.promptTexts = promptTexts;
    }

    public void setLanguage(String shortcut) {
        language = Language.valueOf(shortcut.toUpperCase());
    }

    public String prompt(String paragraph) {
        return switch(this.language) {
            case CZ -> promptTexts.CZECH();
            case EN -> promptTexts.ENGLISH();
            case DE -> promptTexts.GERMAN();
            case SK -> promptTexts.SLOVAK();
        } + paragraph;
    }

    public String prompt(String paragraph, String originalParagraph) {
        return switch(this.language) {
            case CZ -> promptTexts.CZECH(originalParagraph);
            case EN -> promptTexts.ENGLISH(originalParagraph);
            case DE -> promptTexts.GERMAN(originalParagraph);
            case SK -> promptTexts.SLOVAK(originalParagraph);
        } + paragraph;
    }
}

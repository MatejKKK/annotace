package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts;

import lombok.Setter;

@Setter
public class PromptGenerator {

    private Language language = Language.EN;

    private AbstractPromptTexts promptTexts;

    public PromptGenerator(AbstractPromptTexts promptTexts) {
        this.promptTexts = promptTexts;
    }

    public String prompt(String paragraph) {
        return switch(this.language) {
            case CZ, CS -> promptTexts.CZECH();
            case EN -> promptTexts.ENGLISH();
            case DE -> promptTexts.GERMAN();
            case SK -> promptTexts.SLOVAK();
        } + paragraph;
    }

    public String prompt(String paragraph, String originalParagraph) {
        return switch(this.language) {
            case CZ, CS -> promptTexts.CZECH(originalParagraph);
            case EN -> promptTexts.ENGLISH(originalParagraph);
            case DE -> promptTexts.GERMAN(originalParagraph);
            case SK -> promptTexts.SLOVAK(originalParagraph);
        } + paragraph;
    }
}

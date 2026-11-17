package cz.cvut.kbss.annotace.lemmatizerllm.termextractor.promts;

import cz.cvut.kbss.annotace.lemmatizerllm.model.Language;
import cz.cvut.kbss.textanalysis.termextractor.model.Term;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
public class PromptGenerator {

    private Language language = Language.EN;

    private AbstractPromptTexts promptTexts;

    public PromptGenerator(AbstractPromptTexts promptTexts) {
        this.promptTexts = promptTexts;
    }

    public String prompt(String text, String termsSublist) {
        return switch(this.language) {
            case CS -> promptTexts.CZECH(termsSublist);
            case EN -> promptTexts.ENGLISH(termsSublist);
            case DE -> promptTexts.GERMAN(termsSublist);
            case SK -> promptTexts.SLOVAK(termsSublist);
        } + text;
    }
}

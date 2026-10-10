package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.factory;

import cz.cvut.kbss.annotace.lemmatizerllm.model.Language;
import cz.cvut.kbss.annotace.lemmatizerllm.text_service.postprocessor.CzechSublemmaProcessor;
import cz.cvut.kbss.annotace.lemmatizerllm.text_service.postprocessor.SublemmaProcessor;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;

import java.util.List;

public class PostprocessedSingleLemmaResultFactory extends SingleLemmaResultFactory {

    private final static String[] BRACKETS = {"(", ")", "[", "]", "<", ">", "{", "}", "\"", "\""};
    private final static String[] SYMBOLS = {",", ".", "\"", "-", "–", ":", "'"};

    private SublemmaProcessor sublemmaProcessor;

    public PostprocessedSingleLemmaResultFactory(String delimiter, Language language) {
        super(delimiter);
        setLanguage(language);
    }

    @Override
    public List<SingleLemmaResult> createSingleLemmaResult(String line) {
        SingleLemmaResult singleLemmaResult = parseSingleLemmaResult(line);
        final String lemma = singleLemmaResult.getLemma();
        if (!lemma.matches("^(?=.*[A-Z])(?=.*[0-9])[A-Z0-9]+$") || lemma.length() < 2) {
            return List.of(singleLemmaResult);
        }

        for (int i = 1; i < BRACKETS.length; i+=2) {
            if (lemma.startsWith(BRACKETS[i - 1]) && lemma.endsWith(BRACKETS[i])) {
                SingleLemmaResult result1 = new SingleLemmaResult(BRACKETS[i - 1], BRACKETS[i - 1], false);
                SingleLemmaResult result2 = new SingleLemmaResult(
                        singleLemmaResult.getToken().substring(1, singleLemmaResult.getToken().length() - 2),
                        lemma.substring(1, lemma.length() - 2),
                        false
                );
                SingleLemmaResult result3 = new SingleLemmaResult(BRACKETS[i], BRACKETS[i], false);
                return List.of(result1, result2, result3);
            }
        }

        for (final String symbol : SYMBOLS) {
            /*if (lemma.startsWith(symbol)) {
                SingleLemmaResult result1 = new SingleLemmaResult(symbol, symbol, false);
                SingleLemmaResult result2 = new SingleLemmaResult(
                        lemma.substring(1),
                        singleLemmaResult.getToken().substring(1),
                        false
                );
                return List.of(result1, result2);
            }*/
            if (lemma.endsWith(symbol)) {
                SingleLemmaResult result1 = new SingleLemmaResult(
                        lemma.substring(0, lemma.length() - 1),
                        singleLemmaResult.getToken().substring(0, singleLemmaResult.getToken().length() - 1),
                        false
                );
                SingleLemmaResult result2 = new SingleLemmaResult(symbol, symbol, false);
                return List.of(result1, result2);
            }
        }

        return List.of(singleLemmaResult);
    }

    @Override
    public void setLanguage(Language language) {
        this.sublemmaProcessor = switch (language) {
            case CS -> new CzechSublemmaProcessor();
            default -> new SublemmaProcessor();
        };
    }
}

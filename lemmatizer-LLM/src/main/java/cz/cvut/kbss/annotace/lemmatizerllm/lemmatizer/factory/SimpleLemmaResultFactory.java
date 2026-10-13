package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.factory;

import cz.cvut.kbss.annotace.lemmatizerllm.model.Language;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;

import java.util.List;

public class SimpleLemmaResultFactory extends LemmaResultFactory {

    public SimpleLemmaResultFactory(String delimiter) {
        super(delimiter);
    }

    @Override
    public List<SingleLemmaResult> createSingleLemmaResult(String line) {
        SingleLemmaResult singleLemmaResult = parseSingleLemmaResult(line);
        singleLemmaResult.setNegated(false);
        return List.of(singleLemmaResult);
    }

    @Override
    public void setLanguage(Language language) {}
}

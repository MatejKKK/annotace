package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer;

import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;

public class SingleLemmaResultFactory {

    public static SingleLemmaResult createSingleLemmaResult(String line) {
        String[] words = line.split("-");
        assert words.length == 2;

        final SingleLemmaResult result = new SingleLemmaResult();
        result.setToken(words[0]);
        result.setLemma(words[1]);
        return result;
    }
}

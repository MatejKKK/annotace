package cz.cvut.kbss.annotace.lemmatizerllm.text_service.postprocessor;

import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;

import java.util.List;

public class SublemmaProcessor {

    private final String[][] TABLE;
    private final int MAX_LENGTH;

    public SublemmaProcessor() {
        TABLE = getTable();
        MAX_LENGTH = TABLE.length;
    }

    protected String[][] getTable() {
        return new String[][]{};
    }

    public List<SingleLemmaResult> process(List<SingleLemmaResult> singleLemmaResults) {
        singleLemmaResults.replaceAll(this::translateSublemmaToLemma);
        return singleLemmaResults;
    }

    private SingleLemmaResult translateSublemmaToLemma(SingleLemmaResult sublemma) {
        final String original = sublemma.getLemma();
        sublemma.setLemma(original.length() > MAX_LENGTH ? original : searchTable(original));
        return sublemma;
    }

    private String searchTable(String original) {
//        final int originalLen = original.length();
//        for (int i = 1; i < TABLE.length; i+=2) {
//            if (original.endsWith(TABLE[originalLen][i])) {
//                return TABLE[originalLen][i-1];
//            }
//        }
        return original;
    }
}

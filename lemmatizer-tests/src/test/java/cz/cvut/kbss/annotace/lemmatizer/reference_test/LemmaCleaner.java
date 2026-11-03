package cz.cvut.kbss.annotace.lemmatizer.reference_test;

import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;

import static java.lang.Character.isDigit;

public class LemmaCleaner {
    static LemmatizerResult getClearLemmas(LemmatizerResult result) {
        result.setResult(result.getResult().stream()
                .filter(its -> !its.isEmpty())
                .map(its ->
                        its.stream().map(LemmaCleaner::getClearLemma).toList()
                ).toList());
        return result;
    }

    private static SingleLemmaResult getClearLemma(SingleLemmaResult result) {
        String lemma = result.getLemma();
        if (lemma == null) return result;

        int index11 = -1;
        char[] charArray = lemma.toCharArray();
        for (int i = 0; i < charArray.length - 1 || (i < charArray.length && index11 > 0); i++) {
            if (charArray[i] == '-')  index11 = i;
            else if (index11 > -1 && isDigit(charArray[i]))  break;
            else  index11 = -1;
        }

        if (index11 > 0) {
            lemma = lemma.substring(0, index11);
            charArray = lemma.toCharArray();
        }

        int index12 = -1;
        boolean found = false;
        for (int i = 0; i < charArray.length - 1 || (i < charArray.length && index12 > 0); i++) {
            if (charArray[i] == '`')  index12 = i;
            else if (index12 > -1 && isDigit(charArray[i])) {
                found = true;
                break;
            }
            else  index12 = -1;
        }

        if (index12 > 0 && found) {
            lemma = lemma.substring(0, index12);
        }

        int index2 = lemma.indexOf("_:");
        if (index2 > 0) {
            result.setLemma(lemma.substring(0, index2));
            return result;
        }

        int index3 = lemma.indexOf("_;");
        if (index3 > 0) {
            result.setLemma(lemma.substring(0, index3));
            return result;
        }

        int index4 = lemma.indexOf("_^");
        if (index4 > 0) {
            result.setLemma(lemma.substring(0, index4));
            return result;
        }

        int index5 = lemma.indexOf("_,");
        if (index5 > 0) {
            result.setLemma(lemma.substring(0, index5));
            return result;
        }

        result.setLemma(lemma);
        return result;
    }
}

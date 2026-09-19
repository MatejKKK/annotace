package cz.cvut.kbss.annotace.lemmatizerllm;

import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.LLMService;
import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;

import java.util.List;

public class Main {

    private static final LLMService lemmatizer = new LLMService();

    public static void main(String[] args) {
        try {
            LemmatizerResult lemmatizerResult = lemmatizer.process("Pepa jdo do kina", "cz");
            for (List<SingleLemmaResult> results : lemmatizerResult.getResult()) {
                for (SingleLemmaResult result : results) {
                    System.out.println(singleLemmaResultObserver(result));
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Error sending request: " + e.getMessage(), e);
        }
    }

    private static String singleLemmaResultObserver(SingleLemmaResult result) {
        return result.getToken().concat(" ").concat(result.getLemma());
    }
}

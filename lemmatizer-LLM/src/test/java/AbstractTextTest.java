import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.LLMService;
import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

abstract public class AbstractTextTest {

    protected AbstractTextTest() {
        lemmatizer = new LLMService();
    }

    protected final LLMService lemmatizer;

    protected abstract void testSimpleText();


    protected void test(final List<String> correctLemmas, final LemmatizerResult result, double successRate) {
        if (successRate <= 0. || successRate > 100.)  throw new IllegalArgumentException("Percentage has to be between 0 and 100.");
        if (result.getResult().isEmpty() || result.getResult().getFirst().isEmpty())  throw new AssertionError("Empty result");

        int correct = 0, total = 0;
        for (final List<SingleLemmaResult> it : result.getResult()) {
            for (final SingleLemmaResult singleLemmaResult : it) {
                total++;
                if (correctLemmas.contains(singleLemmaResult.getLemma().toLowerCase()) || correctLemmas.contains(singleLemmaResult.getLemma())) {
                    correct++;
                }
                else System.out.println(singleLemmaResult.getLemma().toLowerCase());
            }
        }

        final double rate = 100. * (double) correct / total;
        boolean success = rate >= successRate;

        System.out.println(
                "Tested " + result.getResult().size() + (result.getResult().size() == 1 ? " paragraph including " : " paragraps including ") +
                        total + " words, dots and commas."
        );
        System.out.println(
                correct + " of them was lemmatizered correctly. This makes precision " + rate + "% which is " +
                        (success ? "higher (or equal)" : "lower") + " than success rate (" +  successRate + "%)."
        );

        assertTrue(success);
    }
}

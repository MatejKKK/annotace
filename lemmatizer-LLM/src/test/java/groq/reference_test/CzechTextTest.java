package groq.reference_test;

import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static cz.cvut.kbss.annotace.Tests.completeRateTest;
import static cz.cvut.kbss.annotace.TxtReader.getExpectedList;
import static cz.cvut.kbss.annotace.TxtReader.getInputText;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CzechTextTest extends AbstractTextTest {

    @ParameterizedTest
    @CsvSource({
            //"src/test/resources/cz/inputs/cac/2071.txt,src/test/resources/cz/expected/cac/2071.txt", //97.14285714285714%
            //"src/test/resources/cz/inputs/ortofon_v3/1250.txt,src/test/resources/cz/expected/ortofon_v3/1250.txt",  //85.29698149951315%
            //"src/test/resources/cz/inputs/pdtc/15.txt,src/test/resources/cz/expected/pdtc/15.txt",  //82.560706401766%
            "src/test/resources/cz/inputs/pud/2263.txt,src/test/resources/cz/expected/pud/2263.txt"  //77.12146422628952%
    })
    @Override
    protected void testText(String input, String expected) {
        String text = getInputText(input);
        List<String> lemmas = getExpectedList(expected);
        LemmatizerResult result = lemmatizer.process(text, "cs");

        result.getResult().forEach(its -> {
            its.forEach(it -> System.out.print(it.getLemma() + " "));
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
        });

        assertTrue(completeRateTest(lemmas, text, result, 80, 5));
    }
}

package reference_test;

import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static cz.cvut.kbss.annotace.Tests.completeRateTest;
import static cz.cvut.kbss.annotace.TxtReader.getExpectedList;
import static cz.cvut.kbss.annotace.TxtReader.getInputText;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnglishTextTest extends AbstractTextTest {

    @ParameterizedTest
    @CsvSource({
            "src/test/resources/en/inputs/ewt/11.txt,src/test/resources/en/expected/ewt/10.txt",  //
            "src/test/resources/en/inputs/gum/111.txt,src/test/resources/en/expected/gum/110.txt",  //
            "src/test/resources/en/inputs/lines/405.txt,src/test/resources/en/expected/lines/405.txt"  //
    })
    @Override
    protected void testText(String input, String expected) {
        String text = getInputText(input);
        List<String> lemmas = getExpectedList(expected);
        LemmatizerResult result = lemmatizer.process(text, "en");

        result.setResult(result.getResult().stream()
                .filter(its -> !its.isEmpty())
                .toList()
        );

        result.getResult().forEach(its -> {
            its.forEach(it -> System.out.print(it.getLemma() + " "));
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
        });

        assertTrue(completeRateTest(lemmas, text, result, 50, Math.min(10, lemmas.size() / 2)));
    }
}
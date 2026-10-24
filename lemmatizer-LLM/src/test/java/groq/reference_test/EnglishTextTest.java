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

class EnglishTextTest extends AbstractTextTest {

    @ParameterizedTest
    @CsvSource({
            //"src/test/resources/en/inputs/ewt/11.txt,src/test/resources/en/expected/ewt/11.txt",  //94.79820627802691%
            "src/test/resources/en/inputs/gum/111.txt,src/test/resources/en/expected/gum/111.txt",  //98.11320754716981%
            //"src/test/resources/en/inputs/lines/405.txt,src/test/resources/en/expected/lines/405.txt"  //93.37748344370861%
    })
    @Override
    protected void testText(String input, String expected) {
        String text = getInputText(input);
        List<String> lemmas = getExpectedList(expected);
        LemmatizerResult result = lemmatizer.process(text, "en");

        result.getResult().forEach(its -> {
            its.forEach(it -> System.out.print(it.getLemma() + " "));
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
        });

        assertTrue(completeRateTest(lemmas, text, result, 80, Math.min(10, lemmas.size() / 2)));
    }
}
package prompt;

import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static cz.cvut.kbss.annotace.Tests.simpleRateTest;
import static cz.cvut.kbss.annotace.TxtReader.getExpectedList;
import static cz.cvut.kbss.annotace.TxtReader.getInputText;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CzechTextTest extends AbstractTextTest {

    @ParameterizedTest
    @CsvSource({
            "src/test/resources/cz/inputs/1.txt,src/test/resources/cz/expected/1.txt",
            "src/test/resources/cz/inputs/2.txt,src/test/resources/cz/expected/2.txt",
    })
    @Override
    protected void testText(String input, String expected) {
        String text = getInputText(input);
        List<String> lemmas = getExpectedList(expected);
        LemmatizerResult result = lemmatizer.process(text, "cs");
        assertTrue(simpleRateTest(lemmas, result, 90));
    }
}

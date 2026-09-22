package prompt;

import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class CzechTextTest extends AbstractTextTest {

    static Stream<Arguments> multiParamProviderSimple() {
        return Stream.of(
                arguments("src/test/resources/cz/inputs/1.txt", "src/test/resources/cz/expected/1.txt")
        );
    }

    @ParameterizedTest
    @MethodSource("multiParamProviderSimple")
    @Override
    public void testSimpleText(String input, String expected) {
        String text = this.getInputText(input);
        LemmatizerResult result = lemmatizer.process(text, "cz");
        List<String> lemmas = this.getExpectedList(expected);
        test(lemmas, result, 90);
    }

    static Stream<Arguments> multiParamProviderParagraph() {
        return Stream.of(
                arguments("src/test/resources/cz/inputs/2.txt", "src/test/resources/cz/expected/2.txt")
        );
    }

    @ParameterizedTest
    @MethodSource("multiParamProviderParagraph")
    @Override
    public void testOneParagraphText(String input, String expected) {
        String text = this.getInputText(input);
        LemmatizerResult result = lemmatizer.process(text, "cz");
        List<String> lemmas = this.getExpectedList(expected);
        test(lemmas, result, 90);
    }
}

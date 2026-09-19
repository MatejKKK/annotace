import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import org.junit.jupiter.api.Test;

import java.util.List;

public class EnglishTextTest extends AbstractTextTest {

    @Test
    @Override
    public void testSimpleText() {
        final LemmatizerResult result = lemmatizer.process("UK is going down.", "en");
        final List<String> lemmas = List.of("UK", "be", "go", "down", ".");
        test(lemmas, result, 95);
    }
}
import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import org.junit.jupiter.api.Test;

import java.util.List;

public class GermanTextTest  extends AbstractTextTest {

    @Test
    @Override
    public void testSimpleText() {
        final LemmatizerResult result = lemmatizer.process("Die schönsten Berge sind in den Alpen.", "de");
        final List<String> lemmas = List.of("Die", "schön", "schon", "Berg", "sein", "in", "der", "Alpen", ".");
        test(lemmas, result, 90);
    }
}
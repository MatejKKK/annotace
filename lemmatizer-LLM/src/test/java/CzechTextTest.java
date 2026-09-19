import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CzechTextTest extends AbstractTextTest {


    @Test
    @Override
    public void testSimpleText() {
        final LemmatizerResult result =
                lemmatizer.process("Starý člověk chodí do kostela častěji než mladší lidé.",
                        "cs");
        final List<String> lemmas = List.of("Starý", "člověk", "chodit", "do", "kostel", "často", "než", "mladý", "lidé", ".");
        test(lemmas, result, 90);
    }
}

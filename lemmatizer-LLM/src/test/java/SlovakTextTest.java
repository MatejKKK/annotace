import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SlovakTextTest extends AbstractTextTest {

    @Test
    @Override
    public void testSimpleText() {
        final LemmatizerResult result =
                lemmatizer.process("Otvorením okna v budove sa narušia jeho izolačné vlastnosti",
                        "sk");
        final List<String> lemmas = List.of(
                "Otvorenie", "okno", "v", "budova", "sa", "narušiť", "narúšiť", "jeho", "izolačné", "izolačný", "vlastnosť");
        test(lemmas, result, 90);
    }
}
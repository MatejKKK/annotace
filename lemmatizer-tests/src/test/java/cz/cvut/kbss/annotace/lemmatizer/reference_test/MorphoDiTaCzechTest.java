package cz.cvut.kbss.annotace.lemmatizer.reference_test;

import cz.cvut.kbss.annotace.configuration.MorphoditaConf;
import cz.cvut.kbss.annotace.lemmatizer.MorphoDitaServiceJNI;
import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static cz.cvut.kbss.annotace.Tests.completeRateTest;
import static cz.cvut.kbss.annotace.TxtReader.getExpectedList;
import static cz.cvut.kbss.annotace.TxtReader.getInputText;
import static cz.cvut.kbss.annotace.lemmatizer.reference_test.LemmaCleaner.getClearLemmas;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class,
        classes = {MorphoditaConf.class, MorphoDitaServiceJNI.class})
class MorphoDiTaCzechTest {

    @Autowired
    private MorphoDitaServiceJNI morphoditaLemmatizer;

    @ParameterizedTest
    @CsvSource({
            "src/test/resources/cz/inputs/cac/2069.txt,src/test/resources/cz/expected/cac/2069.txt", //
            "src/test/resources/cz/inputs/cac/2068.txt,src/test/resources/cz/expected/cac/2068.txt", //
            //"src/test/resources/cz/inputs/ortofon_v3/1250.txt,src/test/resources/cz/expected/ortofon_v3/1250.txt",  //
            //"src/test/resources/cz/inputs/pdtc/15.txt,src/test/resources/cz/expected/pdtc/15.txt",  //
            "src/test/resources/cz/inputs/pud/2265.txt,src/test/resources/cz/expected/pud/2265.txt",  //
            "src/test/resources/cz/inputs/pud/2266.txt,src/test/resources/cz/expected/pud/2266.txt",  //
    })
    void testText(String input, String expected) {
        String text = getInputText(input);
        List<String> lemmas = getExpectedList(expected);
        LemmatizerResult result = getClearLemmas(morphoditaLemmatizer.process(text, "cs"));

        result.getResult().forEach(its -> {
            its.forEach(it -> System.out.print(it.getLemma() + " "));
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
        });

        assertTrue(completeRateTest(lemmas, text, result, 50, 5));
    }
}

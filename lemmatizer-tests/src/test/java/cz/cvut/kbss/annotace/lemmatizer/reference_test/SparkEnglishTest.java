package cz.cvut.kbss.annotace.lemmatizer.reference_test;

import cz.cvut.kbss.annotace.configuration.SparkConf;
import cz.cvut.kbss.annotace.lemmatizer.SparkLemmatizer;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class,
        classes = {SparkConf.class, SparkLemmatizer.class})
public class SparkEnglishTest {
    @Autowired
    private SparkLemmatizer sparkLemmatizer;

    @ParameterizedTest
    @CsvSource({
            "src/test/resources/en/inputs/ewt/10.txt,src/test/resources/en/expected/ewt/10.txt",  //
            "src/test/resources/en/inputs/ewt/11.txt,src/test/resources/en/expected/ewt/11.txt",  //
            "src/test/resources/en/inputs/gum/110.txt,src/test/resources/en/expected/gum/110.txt",  //
            "src/test/resources/en/inputs/gum/111.txt,src/test/resources/en/expected/gum/111.txt",  //
            "src/test/resources/en/inputs/lines/405.txt,src/test/resources/en/expected/lines/405.txt"  //
    })
    void testText(String input, String expected) {
        String text = getInputText(input);
        List<String> lemmas = getExpectedList(expected);
        LemmatizerResult result = sparkLemmatizer.process(text, "en");

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

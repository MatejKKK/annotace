package cz.cvut.kbss.annotace.lemmatizer;

import cz.cvut.kbss.annotace.CorrectAndTotal;
import cz.cvut.kbss.annotace.Tests;
import cz.cvut.kbss.annotace.configuration.MorphoditaConf;
import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMConf;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.LLMService;
import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static cz.cvut.kbss.annotace.Tests.getCounts;
import static cz.cvut.kbss.annotace.TxtReader.getExpectedList;
import static cz.cvut.kbss.annotace.TxtReader.getInputText;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class,
        classes = {MorphoditaConf.class, MorphoDitaServiceJNI.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MorphoDitaAndLLMLemmatizerTest {
    @Autowired
    private MorphoDitaServiceJNI morphoditaLemmatizer;

    private final LLMService LLMLemmatizer = new LLMService(
            new LLMConf("^", 750, true)
    );

    private final static List<CorrectAndTotal> sparkResults = new ArrayList<>();
    private final static List<CorrectAndTotal> LLMResults = new ArrayList<>();


    @ParameterizedTest
    @CsvSource({
            "src/test/resources/cz/inputs/1.txt,src/test/resources/cz/expected/1.txt,cs",
            "src/test/resources/cz/inputs/2.txt,src/test/resources/cz/expected/2.txt,cs",
            "src/test/resources/cz/inputs/3.txt,src/test/resources/cz/expected/3.txt,cs",
            "src/test/resources/cz/inputs/4.txt,src/test/resources/cz/expected/4.txt,cs"
    })
    @Order(1)
    void verifyThatLLMLemmatizerIsBetterThatSpark(String input, String expected, String lang) {
        final double APLHA = 1.;

        String text = getInputText(input);
        List<String> lemmas = getExpectedList(expected);

        LemmatizerResult minResult = morphoditaLemmatizer.process(text, lang);
        CorrectAndTotal sparkResult = getCounts(lemmas, minResult);

        LemmatizerResult actualResult = LLMLemmatizer.process(text, lang);
        CorrectAndTotal LLMResult = getCounts(lemmas, actualResult);

        sparkResults.add(sparkResult);
        LLMResults.add(LLMResult);

        System.out.println("Expected rate is: " + sparkResult.rate() +
                " and and actual rate is: " + LLMResult.rate());

        assertTrue(
                Tests.statisticsTest(sparkResult, LLMResult, APLHA),
                "Zero hypothesis (that LLM lemmatizer is better or same than Spark lemmatizer) is rejected in favor of an alternative hypothesis (that LLM lemmatizer is worse than Spark lemmatizer) " +
                        "on level " + APLHA + "%."
        );
    }


    @Test
    @Order(2)
    void overallTest() {
        long sparkTotalSum =    sparkResults.stream().mapToLong(CorrectAndTotal::total).sum();
        long sparkCorrectSum =  sparkResults.stream().mapToLong(CorrectAndTotal::correct).sum();
        long LLMTotalSum =      LLMResults.stream().mapToLong(CorrectAndTotal::total).sum();
        long LLMCorrectSum =    LLMResults.stream().mapToLong(CorrectAndTotal::correct).sum();

        assertTrue(
                (double) sparkCorrectSum / sparkTotalSum <=
                        (double) LLMCorrectSum / LLMTotalSum,
                "In overall test, spark lemmatizer is better than LLM lemmatizer. " +
                        "Spark's rate was " + (double) sparkCorrectSum / sparkTotalSum +
                        " and LLM's rate was " + (double) LLMCorrectSum / LLMTotalSum
        );
    }
}

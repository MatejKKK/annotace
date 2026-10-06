import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.TextExtractor;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.SingleLemmaResultFactory;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SentenceExtractionTest {

    private int simpleSentenceCounter(List<String> paragraph, int maxSentenceLength) {
        List<String> sentences = new ArrayList<>();
        StringBuilder currentSentence = new StringBuilder();
        for (String sentence : paragraph) {
            if (sentence.length() + currentSentence.length() > maxSentenceLength) {
                sentences.add(currentSentence.toString());
                currentSentence = new StringBuilder(sentence);
            }
            else currentSentence.append(sentence);
        }
        if (!currentSentence.isEmpty()) {
            sentences.add(currentSentence.toString());
        }
        return sentences.size();
    }


    static Stream<Arguments> multiParamProviderSentenceExtractor() {
        return MultiParamProviders.multiParamProviderSentenceExtractor();
    }

    @ParameterizedTest
    @MethodSource("multiParamProviderSentenceExtractor")
    void testSentenceExtraction(String input, int expectedSentencesCount, int maxSentenceLength) {
        List<String> result = TextExtractor.extractSentences(input);
        for (String sentence : result) {
            System.out.println(sentence + "\n");
        }
        assertEquals(expectedSentencesCount, result.size());

        System.out.println("\n\n");

        String[] mergedSentences = TextExtractor.mergeSentences(result, maxSentenceLength);

        for (String sentence : mergedSentences) {
            System.out.println(sentence + "\n");
        }

        assertEquals(
                result.stream().map(String::length).mapToInt(i -> i).sum(),
                Arrays.stream(mergedSentences).map(String::length).mapToInt(i -> i).sum(),
                "Merged sentences should have same number of letters as original sentences."
        );

        assertTrue(mergedSentences.length <= simpleSentenceCounter(result, maxSentenceLength),
                "Smart algorithm should return same or better (=shorter) result than simple one.");
    }


    static Stream<Arguments> multiParamProviderParagraphExtractor() {
        return MultiParamProviders.multiParamProviderParagraphExtractor();
    }


    @ParameterizedTest
    @MethodSource("multiParamProviderParagraphExtractor")
    void testParagraphExtraction(String input, int expectedParagraphCount, int expectedMergedParagraphCount, int maxParagraphLength) {
        List<String> result = TextExtractor.extractParagraphs(input);
        for (String paragraph : result) {
            System.out.println(paragraph + "\n");
        }
        assertEquals(expectedParagraphCount, result.size(), "Unexpected paragraph count.");

        System.out.println("\n\n");

        String[] mergedParagraphs = TextExtractor.mergeParagraphs(result, maxParagraphLength);

        for (String paragraph : mergedParagraphs) {
            System.out.println(paragraph + "\n");
        }

        assertTrue(
                result.stream().map(String::length).mapToInt(i -> i).sum() <=
                Arrays.stream(mergedParagraphs).map(String::length).mapToInt(i -> i).sum(),  //mergedParagraphs could have \n\n\n\n between single paragraphs
                "Merged paragraphs should have same number of letters as original paragraphs."
        );

        assertEquals(expectedMergedParagraphCount, mergedParagraphs.length, "Unexpected merged paragraphs count.");
    }

    
    static Stream<Arguments> multiParamProviderSpaces() {
        return MultiParamProviders.multiParamProviderSpaces();
    }

    @ParameterizedTest
    @MethodSource("multiParamProviderSpaces")
    void lemmaSpacesTest(String paragraph, List<SingleLemmaResult> input, List<SingleLemmaResult> expected) {
        List<SingleLemmaResult> result = SingleLemmaResultFactory.addSpacesToLemma(input, paragraph);

        result.forEach(singleLemmaResult ->
            assertTrue(expected.stream().anyMatch(e ->
                    e.getToken().equals(singleLemmaResult.getToken()) &&
                            e.getLeadingSpaces().equals(singleLemmaResult.getLeadingSpaces()) &&
                            e.getTrailingSpaces().equals(singleLemmaResult.getTrailingSpaces())

            ), "Token " + singleLemmaResult.getToken() + " should have match with " +
                    singleLemmaResult.getLeadingSpaces().length() + " leading spaces and " + singleLemmaResult.getTrailingSpaces().length() + " trailing spaces.")
        );
    }
}

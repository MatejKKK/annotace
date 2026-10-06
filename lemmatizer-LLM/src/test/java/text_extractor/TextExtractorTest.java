package text_extractor;

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

import static cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.TextExtractor.*;
import static org.junit.jupiter.api.Assertions.*;
import static text_extractor.Helper.*;

public class TextExtractorTest {

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

        String[] mergedSentences = mergeSentences(result, maxSentenceLength);

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
        List<String> result = extractParagraphs(input);
        for (String paragraph : result) {
            System.out.println(paragraph + "\n");
        }
        assertEquals(expectedParagraphCount, result.size(), "Unexpected paragraph count.");

        System.out.println("\n\n");

        String[] mergedParagraphs = mergeParagraphs(result, maxParagraphLength);

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


    static Stream<Arguments> multiParamProviderProcessTest() {
        return MultiParamProviders.multiParamProviderProcessTest();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("multiParamProviderProcessTest")
    void wholeProcessKeepsTheTextAndRespectsTheLimits(
            String description, String text, String survivingText,
            int maxParagraphLength, int maxSentenceLength, int expectedParagraphChunks, int expectedSentenceChunks) {

        // The same calls as in the lemmatizer.
        final String[] paragraphChunks = mergeParagraphs(extractParagraphs(text), maxParagraphLength * 2);

        final List<String> allSentenceChunks = new ArrayList<>();
        for (final String paragraphChunk : paragraphChunks) {
            final List<String> sentences = extractSentences(paragraphChunk);
            final String[] sentenceChunks = mergeSentences(sentences, maxSentenceLength);

            assertLimitRespected(sentenceChunks, sentences, maxSentenceLength, description);
            allSentenceChunks.addAll(Arrays.asList(sentenceChunks));
        }

        assertLimitRespected(paragraphChunks, extractParagraphs(text), maxParagraphLength * 2, description);
        for (final String chunk : allSentenceChunks) {
            assertFalse(chunk.isBlank(), description + ": no chunk may be blank");
            assertFalse(chunk.startsWith(BREAK), description + ": no chunk may start with a paragraph break");
        }

        assertEquals(content(survivingText == null ? text : survivingText), content(String.join("", allSentenceChunks)),
                description + ": text must be neither lost nor reordered");
        assertEquals(expectedParagraphChunks, paragraphChunks.length, description + ": paragraph chunks");
        assertEquals(expectedSentenceChunks, allSentenceChunks.size(), description + ": sentence chunks");
    }

    /** Limits {maxParagraphLength, maxSentenceLength} from "everything alone" to "everything in one chunk". */
    static Stream<Arguments> multiParamProviderProcessLimits() {
        return MultiParamProviders.multiParamProviderProcessLimits();
    }

    /**
     * Every word of the input is unique (t0001, t0002, ...), so a repeated word means duplicated text, a
     * missing one lost text and a word out of order reordered text. Neighbouring chunks must also join
     * without a gap or an overlap: the first word of a chunk is the one right after the last of the previous.
     */
    @ParameterizedTest(name = "paragraph limit {0}, sentence limit {1}")
    @MethodSource("multiParamProviderProcessLimits")
    void chunksNeitherOverlapNorSkipAnyText(int maxParagraphLength, int maxSentenceLength) {
        final int wordCount = 120;
        final String text = textOfUniqueWords(wordCount);

        final String[] paragraphChunks = mergeParagraphs(extractParagraphs(text), maxParagraphLength * 2);

        final List<String> allSentenceChunks = new ArrayList<>();
        for (final String paragraphChunk : paragraphChunks) {
            allSentenceChunks.addAll(Arrays.asList(mergeSentences(extractSentences(paragraphChunk), maxSentenceLength)));
        }

        assertChunksFormTheSequence(Arrays.asList(paragraphChunks), wordCount, "paragraph chunks");
        assertChunksFormTheSequence(allSentenceChunks, wordCount, "sentence chunks");
    }
}
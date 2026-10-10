package text_extractor;

import cz.cvut.kbss.annotace.lemmatizerllm.text_service.TextExtractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Helper {
    static final String BREAK = "\n\n\n\n";

    /** Value of "surviving text" for texts of which nothing is removed. */
    static final String ALL_KEPT = null;

    static int simpleSentenceCounter(List<String> paragraph, int maxSentenceLength) {
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

    /** Letters and digits only: dots, spaces and paragraph breaks are not content. */
    static String content(String text) {
        return text.replaceAll("[^\\p{L}\\p{N}]", "");
    }

    /** A chunk may exceed the limit only when it is a single, untouched unit (sentence or paragraph). */
    static void assertLimitRespected(String[] chunks, Collection<String> units, int limit, String description) {
        for (final String chunk : chunks) {
            assertTrue(chunk.length() <= limit || units.contains(chunk),
                    description + ": chunk longer than " + limit + " that is not a single unit: " + chunk.replace("\n", "↵"));
        }
    }

    static void assertChunksFormTheSequence(List<String> chunks, int wordCount, String name) {
        int expectedNext = 1;
        for (final String chunk : chunks) {
            final List<Integer> words = new ArrayList<>();
            final java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("t(\\d{4})").matcher(chunk);
            while (matcher.find()) {
                words.add(Integer.parseInt(matcher.group(1)));
            }
            assertFalse(words.isEmpty(), name + ": a chunk without any word: " + chunk.replace("\n", "↵"));
            final int firstWord = words.getFirst();
            assertEquals(expectedNext, firstWord,
                    name + ": chunk does not continue where the previous one ended (overlap or gap): " + chunk.replace("\n", "↵"));
            for (int i = 1; i < words.size(); i++) {
                final int previous = words.get(i - 1);
                final int current = words.get(i);
                assertEquals(previous + 1, current, name + ": words inside a chunk are not consecutive");
            }
            expectedNext = words.getLast() + 1;
        }
        assertEquals(wordCount + 1, expectedNext, name + ": the last words are missing");
    }

    /** Paragraphs of 1-4 sentences of 1-5 words; sentence ends are varied to cover every terminator. */
    static String textOfUniqueWords(int wordCount) {
        final String[] terminators = {". ", "! ", "? ", ".\n"};
        final List<String> paragraphs = new ArrayList<>();
        final StringBuilder paragraph = new StringBuilder();
        int word = 1;
        for (int sentence = 0; word <= wordCount; sentence++) {
            for (int i = 0; i < 1 + sentence % 5 && word <= wordCount; i++) {
                paragraph.append(String.format("t%04d", word++)).append(i < sentence % 5 && word <= wordCount ? " " : "");
            }
            paragraph.append(terminators[sentence % terminators.length]);
            if (sentence % 4 == 3) {
                paragraphs.add(paragraph.toString().strip());
                paragraph.setLength(0);
            }
        }
        if (!paragraph.isEmpty()) {
            paragraphs.add(paragraph.toString().strip());
        }
        return MultiParamProviders.paragraphs(paragraphs.toArray(String[]::new));
    }

    static int count(String text, String part) {
        return (text.length() - text.replace(part, "").length()) / part.length();
    }

    /**
     * Merged paragraphs are joined by exactly one mark per join: n original paragraphs in k chunks need
     * n - k marks, and a mark is never left dangling at the end of a chunk.
     */
    static void assertMergeMarksJoinOriginalParagraphs(String[] paragraphChunks, int originalParagraphCount, String description) {
        assertEquals(originalParagraphCount - paragraphChunks.length, count(String.join("", paragraphChunks), TextExtractor.PARAGRAPH_MERGE_MARK),
                description + ": number of merge marks");
        for (String chunk : paragraphChunks) {
            assertFalse(chunk.endsWith(TextExtractor.PARAGRAPH_MERGE_MARK), description + ": a merge mark may not end a chunk: " + chunk);
        }
    }
}

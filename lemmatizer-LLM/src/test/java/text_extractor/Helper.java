package text_extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static cz.cvut.kbss.annotace.lemmatizerllm.text_service.TextExtractor.*;
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

    /**
     * Merged paragraphs are joined by exactly one mark per join: n original paragraphs in k chunks need
     * n - k marks, and a mark is never left dangling at the end of a chunk.
     */
    static void assertMergeMarksJoinOriginalParagraphs(String[] paragraphChunks, int originalParagraphCount, String description) {
        assertEquals(originalParagraphCount - paragraphChunks.length, count(String.join("", paragraphChunks), PARAGRAPH_MERGE_MARK),
                description + ": number of merge marks");
        for (final String chunk : paragraphChunks) {
            assertFalse(chunk.endsWith(PARAGRAPH_MERGE_MARK), description + ": a merge mark may not end a chunk: " + chunk);
        }
    }

    /**
     * Inside a merged paragraph the original paragraphs are separated by PARAGRAPH_MERGE_MARK (and only
     * there). So cutting every chunk at the mark must give back exactly the original paragraphs, each once
     * and in the original order: nothing lost, nothing duplicated, no mark left over.
     */
    static void assertParagraphChunksAreTheOriginalParagraphs(
            String[] paragraphChunks, List<String> originalParagraphs, String description) {
        final List<String> restored = Arrays.stream(paragraphChunks)
                .flatMap(chunk -> Arrays.stream(chunk.split(Pattern.quote(PARAGRAPH_MERGE_MARK), -1)))
                .toList();

        assertEquals(originalParagraphs, restored, description + ": paragraph chunks cut at PARAGRAPH_MERGE_MARK");
    }

    static int count(String text, String part) {
        return (text.length() - text.replace(part, "").length()) / part.length();
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
        return paragraphs(paragraphs.toArray(String[]::new));
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
            final int firstWord = words.get(0);
            assertEquals(expectedNext, firstWord,
                    name + ": chunk does not continue where the previous one ended (overlap or gap): " + chunk.replace("\n", "↵"));
            for (int i = 1; i < words.size(); i++) {
                final int previous = words.get(i - 1);
                final int current = words.get(i);
                assertEquals(previous + 1, current, name + ": words inside a chunk are not consecutive");
            }
            expectedNext = words.get(words.size() - 1) + 1;
        }
        assertEquals(wordCount + 1, expectedNext, name + ": the last words are missing");
    }

    /**
     * A chunk made of n whole paragraphs (joined by PARAGRAPH_MERGE_MARK) must fit into
     * limit * 0.9^(n-1). A chunk of a single paragraph, or of sentences of a single paragraph, must fit into
     * limit, except when it is one untouched unit (a sentence or a paragraph that is longer on its own).
     */
    static void assertLimitRespected(String[] chunks, Collection<String> units, int limit, String description) {
        for (final String chunk : chunks) {
            final int joins = count(withoutOuterMergeMarks(chunk), PARAGRAPH_MERGE_MARK);
            final double allowed = limit * Math.pow(PARAGRAPH_MERGE_DECAY, joins);

            assertTrue(chunk.length() <= allowed || (joins == 0 && units.contains(chunk)),
                    description + ": chunk of " + (joins + 1) + " paragraph(s) is longer than " + allowed
                            + " and is not a single unit: " + chunk.replace(PARAGRAPH_MERGE_MARK, "<MERGE>").replace("\n", "↵"));
        }
    }

    /** A mark at the edge of a chunk only closes (or opens) a paragraph, it does not join two of them. */
    static String withoutOuterMergeMarks(String chunk) {
        String result = chunk;
        while (result.startsWith(PARAGRAPH_MERGE_MARK)) {
            result = result.substring(PARAGRAPH_MERGE_MARK.length());
        }
        while (result.endsWith(PARAGRAPH_MERGE_MARK)) {
            result = result.substring(0, result.length() - PARAGRAPH_MERGE_MARK.length());
        }
        return result;
    }

    static String paragraphs(String... parts) {
        return String.join(BREAK, parts);
    }

    /** Letters and digits only: dots, spaces, paragraph breaks and PARAGRAPH_MERGE_MARK are not content. */
    static String content(String text) {
        return text.replace(PARAGRAPH_MERGE_MARK, "").replaceAll("[^\\p{L}\\p{N}]", "");
    }
}

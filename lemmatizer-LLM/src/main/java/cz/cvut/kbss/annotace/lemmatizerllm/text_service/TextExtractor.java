package cz.cvut.kbss.annotace.lemmatizerllm.text_service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TextExtractor {

    /** Paragraph separator. Text split into paragraphs is always rejoined with exactly this string. */
    private static final String PARAGRAPH_BREAK = "\n\n\n\n";

    /**
     * Marker appended by {@link #mergeParagraphs} to the end of every original paragraph that is
     * followed, inside the same merged result, by another original paragraph. Distinct from
     * {@value #PARAGRAPH_BREAK}: it does not mark a real paragraph boundary in the source text, only
     * where {@link #mergeParagraphs} glued two originally separate paragraphs together.
     */
    public static final String PARAGRAPH_MERGE_MARK = "9348435854367645";

    /**
     * Marker appended by {@link #mergeParagraphs} to the end of every original paragraph that is
     * followed, inside the same merged result, by another original paragraph. Distinct from
     * {@value #PARAGRAPH_BREAK}: it does not mark a real paragraph boundary in the source text, only
     * where {@link #mergeParagraphs} glued two originally separate paragraphs together.
     */
    public static final String PARAGRAPH_MERGE_MARK_LINE = PARAGRAPH_MERGE_MARK + "^" + PARAGRAPH_MERGE_MARK;

    /**
     * Shrink factor applied, per extra paragraph, to the length cap when {@link #mergeSentences}
     * merges whole paragraphs together: merging n paragraphs must fit into
     * {@code maxSentenceLength * PARAGRAPH_MERGE_DECAY^(n-1)}.
     */
    private static final double PARAGRAPH_MERGE_DECAY = 0.9;

    /** One text unit (a sentence or a paragraph) together with its original position. */
    private record IndexedText(String text, int order) { }

    /**
     * Length of one unit (or several merged units) + the original order of all units it was created
     * from. Thanks to the orders list, the result can always be mapped back to the original IndexedText.
     */
    private record LengthOrder(int length, List<Integer> orders) {
        /**
         * Merges two neighbours: lengths are summed (plus the separator placed between them when the
         * units are joined back into text), orders are concatenated (this precedes next).
         */
        private LengthOrder merge(LengthOrder next, int separatorLength) {
            List<Integer> mergedOrders = new ArrayList<>(orders);
            mergedOrders.addAll(next.orders);
            return new LengthOrder(length + next.length + separatorLength, mergedOrders);
        }
    }

    /**
     * Best known partition of the first N elements into contiguous groups.
     *
     * @param groups         number of groups (fewer is better)
     * @param squares        sum of squared group lengths; for the same number of groups and the same
     *                       total sum this is exactly what determines the variance (lower is better)
     * @param lastGroupStart index where the last group starts (used to reconstruct the solution)
     */
    private record Best(int groups, long squares, int lastGroupStart) {
        private boolean isBetterThan(Best other) {
            return groups < other.groups || (groups == other.groups && squares < other.squares);
        }
    }

    /** Splits text into single paragraphs wherever {@value #PARAGRAPH_BREAK} occurs. */
    public static List<String> extractParagraphs(String text) {
        return Arrays.asList(text.split(PARAGRAPH_BREAK));
    }

    /**
     * Splits text into single sentences. The text may consist of several paragraphs (see
     * {@link #extractParagraphs}); sentences are extracted paragraph by paragraph and flattened into
     * one list, but the separator is kept attached to the end of the last sentence of every
     * non-final paragraph, so the paragraph boundary stays recognisable later (see {@link #mergeSentences}).
     */
    public static List<String> extractSentences(String paragraph) {
        final List<String> paragraphs = extractParagraphs(paragraph);
        final List<String> result = new ArrayList<>();

        for (int i = 0; i < paragraphs.size(); i++) {
            final List<String> sentences = extractSentencesFromParagraph(paragraphs.get(i));
            final boolean hasMoreParagraphs = i < paragraphs.size() - 1;

            if (hasMoreParagraphs && !sentences.isEmpty()) {
                final int lastIndex = sentences.size() - 1;
                sentences.set(lastIndex, sentences.get(lastIndex) + PARAGRAPH_BREAK);
            }
            result.addAll(sentences);
        }
        return result;
    }

    /** Extracts a single paragraph (no {@value #PARAGRAPH_BREAK} inside it) into single sentences. */
    private static List<String> extractSentencesFromParagraph(String paragraph) {
        StringBuilder noBracketsParagraph = new StringBuilder();
        short bracketsDepth = 0;

        for (final char ch : paragraph.toCharArray()) {
            if (ch == '(') bracketsDepth++;
            if (ch == ')') bracketsDepth--;
            if (bracketsDepth <= 0) noBracketsParagraph.append(ch);
        }

        final List<String> dotsExtracted =
                Arrays.stream(noBracketsParagraph.toString().split("\\. "))
                        .flatMap(str -> Arrays.stream(str.split("\\.\n")))
                        .flatMap(str -> Arrays.stream(str.split("! ")))
                        .flatMap(str -> Arrays.stream(str.split("!\n")))
                        .flatMap(str -> Arrays.stream(str.split("\\? ")))
                        .flatMap(str -> Arrays.stream(str.split("\\?\n")))
                        .filter(str -> !str.isEmpty()).toList();

        List<String> result = new ArrayList<>();
        AtomicBoolean added = new AtomicBoolean(false);

        dotsExtracted.forEach(betweenDots -> {
            betweenDots = betweenDots.replace('\n', ' ')
                    .replace('\t', ' ')
                    .concat(". ");
            if (!result.isEmpty()) {
                if (betweenDots.length() < 5) {
                    final int lastIndex = result.size() - 1;
                    result.set(lastIndex, result.get(lastIndex) + betweenDots);
                    added.set(true);
                } else if (added.get()) {
                    added.set(false);
                    final int lastIndex = result.size() - 1;
                    result.set(lastIndex, result.get(lastIndex) + betweenDots);
                } else if (betweenDots.length() + result.getLast().length() < 56) {
                    final int lastIndex = result.size() - 1;
                    result.set(lastIndex, result.get(lastIndex) + betweenDots);
                } else {
                    result.add(betweenDots);
                }
            } else {
                result.add(betweenDots);
            }
        });
        return result;
    }

    /**
     * Merges paragraphs into the shortest possible list where every merged paragraph has a length of
     * at most maxParahgraphLength. Unlike sentences, paragraphs have no forbidden boundary, so any
     * neighbours may be merged. Whenever two original paragraphs end up next to each other inside the
     * same merged result, {@value #PARAGRAPH_MERGE_MARK} is appended to the end of the first one, so
     * that join stays recognisable in the resulting text.
     *
     * @param paragraphs is array of every single paragraph
     * @return merged paragraphs
     */
    public static String[] mergeParagraphs(List<String> paragraphs, int maxParahgraphLength) {
        final List<IndexedText> orders = index(paragraphs);
        final List<LengthOrder> merged = mergeLengths(toLengths(orders), PARAGRAPH_MERGE_MARK.length(), n -> maxParahgraphLength);
        final Map<Integer, String> textByOrder = textByOrder(orders);

        return merged.stream()
                .map(group -> appendMarkBetweenMembers(group.orders(), textByOrder))
                .toArray(String[]::new);
    }

    /** Concatenates a merged group's original paragraphs, appending {@value #PARAGRAPH_MERGE_MARK} after every member that is followed by another member of the same group. */
    private static String appendMarkBetweenMembers(List<Integer> groupOrders, Map<Integer, String> textByOrder) {
        final StringBuilder result = new StringBuilder();

        for (int i = 0; i < groupOrders.size(); i++) {
            result.append(textByOrder.get(groupOrders.get(i)));
            if (i < groupOrders.size() - 1) {
                result.append(PARAGRAPH_MERGE_MARK);
            }
        }
        return result.toString();
    }

    /**
     * Full pipeline from raw text to a single string of evenly sized chunks: splits the text into
     * sentences ({@link #extractSentences}) and merges them into the shortest possible list where
     * every chunk has a length of at most maxSentenceLength ({@link #mergeSentences}, which may merge
     * sentences across a paragraph boundary too, as long as that boundary's paragraphs are each short
     * enough on their own; see its own javadoc for the exact rule).
     * <p>
     * The chunks are concatenated directly, with no extra separator inserted between them: a
     * {@value #PARAGRAPH_BREAK} already sits wherever a paragraph boundary survived into the result
     * (carried over from {@link #extractSentences}), and a chunk boundary created purely by
     * maxSentenceLength carries no separator at all.
     * <p>Example:
     * <pre>{@code
     * String text = "First sentence. Second one.\n\n\n\nSecond paragraph starts here.";
     * String result = TextExtractor.splitText(text, 60);
     * }</pre>
     *
     * @param text raw text, optionally containing several {@value #PARAGRAPH_BREAK}-separated paragraphs
     * @return the chunks, in the original order, concatenated into a single string
     */
    public static String splitText(String text, int maxSentenceLength) {
        final String[] chunks = mergeSentences(extractSentences(text), maxSentenceLength);
        return String.join("", chunks);
    }

    /**
     * Merges sentences into the shortest possible list where every merged sentence has a length of
     * at most maxSentenceLength. This happens in two steps:
     * <ol>
     *   <li>Sentences are first grouped within their own paragraph (never across a
     *       {@value #PARAGRAPH_BREAK} boundary, see {@link #extractSentences}), exactly like before.</li>
     *   <li>A paragraph whose sentences all fit into a single group of step 1 (i.e. the whole paragraph
     *       is at most maxSentenceLength long) may then be merged further with its neighbouring
     *       paragraphs, provided they are likewise single-group. Merging n such paragraphs together
     *       must fit into {@code maxSentenceLength * PARAGRAPH_MERGE_DECAY^(n-1)}, so the allowed
     *       length shrinks with every extra paragraph pulled in. A paragraph that needed more than one
     *       group in step 1 is left untouched and blocks this cross-paragraph merging on both sides,
     *       the same way a {@value #PARAGRAPH_BREAK} blocks step 1.</li>
     * </ol>
     * Whenever whole paragraphs end up merged, the {@value #PARAGRAPH_BREAK} between them does not
     * need to be re-inserted: it is already embedded in the text, carried over from
     * {@link #extractSentences}.
     *
     * @param sentences is array of every single sentence
     * @return merged sentences
     */
    public static String[] mergeSentences(List<String> sentences, int maxSentenceLength) {
        final List<IndexedText> orders = index(sentences);
        final List<List<IndexedText>> paragraphs = splitOnParagraphBreak(orders);

        final List<LengthOrder> result = new ArrayList<>();
        final List<LengthOrder> wholeParagraphsPendingMerge = new ArrayList<>();

        for (List<IndexedText> paragraph : paragraphs) {
            final List<LengthOrder> groups = mergeLengths(toLengths(paragraph), 0, n -> maxSentenceLength);

            if (isSingleGroupWithinLimit(groups, maxSentenceLength)) {
                wholeParagraphsPendingMerge.add(groups.getFirst());
            } else {
                mergeAndAppendWholeParagraphs(wholeParagraphsPendingMerge, maxSentenceLength, result);
                result.addAll(groups);
            }
        }
        mergeAndAppendWholeParagraphs(wholeParagraphsPendingMerge, maxSentenceLength, result);

        return toMergedArray(result, textByOrder(orders), "");
    }

    /** True when step 1 collapsed a paragraph into exactly one group that itself respects maxSentenceLength, making it eligible for step 2. */
    private static boolean isSingleGroupWithinLimit(List<LengthOrder> groups, int maxSentenceLength) {
        return groups.size() == 1 && groups.getFirst().length() <= maxSentenceLength;
    }

    /** Runs step 2 on a run of adjacent single-group paragraphs, appends the resulting merges, then clears the run so the next one starts fresh. */
    private static void mergeAndAppendWholeParagraphs(List<LengthOrder> pendingParagraphs, int maxSentenceLength, List<LengthOrder> result) {
        if (!pendingParagraphs.isEmpty()) {
            result.addAll(mergeLengths(pendingParagraphs, 0, n -> maxSentenceLength * Math.pow(PARAGRAPH_MERGE_DECAY, n - 1)));
            pendingParagraphs.clear();
        }
    }

    private static List<IndexedText> index(List<String> texts) {
        return IntStream.range(0, texts.size())
                .mapToObj(i -> new IndexedText(texts.get(i), i))
                .toList();
    }

    private static Map<Integer, String> textByOrder(List<IndexedText> orders) {
        return orders.stream().collect(Collectors.toMap(IndexedText::order, IndexedText::text));
    }

    private static List<LengthOrder> toLengths(List<IndexedText> orders) {
        return orders.stream()
                .map(it -> new LengthOrder(it.text().length(), List.of(it.order())))
                .toList();
    }

    /** Cuts the sentence list into segments wherever a sentence ends with {@value #PARAGRAPH_BREAK}, so that such a sentence always ends its segment and can never be merged with what follows it. */
    private static List<List<IndexedText>> splitOnParagraphBreak(List<IndexedText> orders) {
        final List<List<IndexedText>> segments = new ArrayList<>();
        List<IndexedText> current = new ArrayList<>();

        for (IndexedText unit : orders) {
            current.add(unit);
            if (unit.text().endsWith(PARAGRAPH_BREAK)) {
                segments.add(current);
                current = new ArrayList<>();
            }
        }
        if (!current.isEmpty()) {
            segments.add(current);
        }
        return segments;
    }

    /** Resolves a merge result back to text, joining each group's original units with the given separator. */
    private static String[] toMergedArray(List<LengthOrder> merged, Map<Integer, String> textByOrder, String separator) {
        return merged.stream()
                .map(group -> group.orders().stream()
                        .map(textByOrder::get)
                        .collect(Collectors.joining(separator)))
                .toArray(String[]::new);
    }

    /**
     * Splits the sequence into contiguous groups so that there are as few groups as possible and, for
     * an equal count, the variance is as small as possible. A group of n elements must have a length
     * of at most {@code maxLengthForGroupSize.applyAsDouble(n)}; passing a constant function reproduces
     * a plain, fixed length cap. Dynamic programming: best[end] = best solution for the first `end`
     * elements.
     *
     * @param separatorLength extra length contributed by every joint inside a merged group (0 when
     *                        units are simply concatenated, as with sentences)
     */
    private static List<LengthOrder> mergeLengths(
            List<LengthOrder> lengths, int separatorLength, IntToDoubleFunction maxLengthForGroupSize
    ) {
        final int n = lengths.size();
        final Best[] best = new Best[n + 1];
        best[0] = new Best(0, 0, 0);

        for (int end = 1; end <= n; end++) {
            int groupLength = 0;

            // the last group is lengths[start .. end); we gradually extend it to the left
            for (int start = end - 1; start >= 0; start--) {
                final int groupSize = end - start;
                final boolean singleElement = groupSize == 1;
                groupLength += lengths.get(start).length() + (singleElement ? 0 : separatorLength);

                if (groupLength > maxLengthForGroupSize.applyAsDouble(groupSize) && !singleElement) {
                    break; // extending further only increases the length while the cap can only shrink or stay put
                }

                Best candidate = new Best(
                        best[start].groups() + 1,
                        best[start].squares() + (long) groupLength * groupLength,
                        start
                );
                if (best[end] == null || candidate.isBetterThan(best[end])) {
                    best[end] = candidate;
                }
            }
        }

        return buildGroups(lengths, best, separatorLength);
    }

    /** Walks the solution from the end and merges each group into a single LengthOrder. */
    private static List<LengthOrder> buildGroups(List<LengthOrder> lengths, Best[] best, int separatorLength) {
        final List<LengthOrder> result = new ArrayList<>();

        for (int end = lengths.size(); end > 0; end = best[end].lastGroupStart()) {
            List<LengthOrder> group = lengths.subList(best[end].lastGroupStart(), end);
            result.add(group.stream().reduce((a, b) -> a.merge(b, separatorLength)).orElseThrow());
        }

        Collections.reverse(result);
        return result;
    }
}
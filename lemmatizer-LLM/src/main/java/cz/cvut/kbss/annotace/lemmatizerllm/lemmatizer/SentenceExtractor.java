package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SentenceExtractor {

    private record SentenceOrder(String sentence, int order) { }

    /**
     * Length of one sentence (or several merged sentences) + the original order of all sentences it was created from.
     * Thanks to the orders list, the result can always be mapped back to the original SentenceOrder.
     */
    private record LengthOrder(int length, List<Integer> orders) {
        /** Merges two neighbours: lengths are summed, orders are concatenated (this precedes next). */
        private LengthOrder merge(LengthOrder next) {
            List<Integer> mergedOrders = new ArrayList<>(orders);
            mergedOrders.addAll(next.orders);
            return new LengthOrder(length + next.length, mergedOrders);
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

    /** Extracts text into single sentences. */
    public static List<String> extractSentence(String paragraph) {
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
                }
                else if (added.get()) {
                    added.set(false);
                    final int lastIndex = result.size() - 1;
                    result.set(lastIndex, result.get(lastIndex) + betweenDots);
                }
                else if (betweenDots.length() + result.getLast().length() < 56) {
                    final int lastIndex = result.size() - 1;
                    result.set(lastIndex, result.get(lastIndex) + betweenDots);
                }
                else {
                    result.add(betweenDots);
                }
            }
            else {
                result.add(betweenDots);
            }
        });
        return result;
    }

    /**
     * Splits the sequence into contiguous groups with a length of at most maxSentenceLength so that
     * there are as few groups as possible and, for an equal count, the variance is as small as possible.
     * Dynamic programming: best[end] = best solution for the first `end` elements.
     */
    public static String[] mergeSentences(List<String> sentences, int maxSentenceLength) {
        final List<SentenceOrder> orders = IntStream.range(0, sentences.size())
                .mapToObj(i -> new SentenceOrder(sentences.get(i), i))
                .toList();

        final List<LengthOrder> lengths = orders.stream()
                .map(it -> new LengthOrder(it.sentence().length(), List.of(it.order())))
                .toList();

        final Map<Integer, String> sentenceByOrder = orders.stream()
                .collect(Collectors.toMap(SentenceOrder::order, SentenceOrder::sentence));

        return mergeLengths(maxSentenceLength, lengths).stream()
                .map(merged -> merged.orders().stream()
                        .map(sentenceByOrder::get)
                        .collect(Collectors.joining()))
                .toArray(String[]::new);
    }

    /**
     * Splits the sequence into contiguous groups with a length of at most maxSentenceLength so that
     * there are as few groups as possible and, for an equal count, the variance is as small as possible.
     * Dynamic programming: best[end] = best solution for the first `end` elements.
     */
    private static List<LengthOrder> mergeLengths(int maxSentenceLength, List<LengthOrder> lengths) {
        final int n = lengths.size();
        final Best[] best = new Best[n + 1];
        best[0] = new Best(0, 0, 0);

        for (int end = 1; end <= n; end++) {
            int groupLength = 0;

            // the last group is lengths[start .. end); we gradually extend it to the left
            for (int start = end - 1; start >= 0; start--) {
                groupLength += lengths.get(start).length();

                boolean singleElement = start == end - 1;
                if (groupLength > maxSentenceLength && !singleElement) {
                    break; // extending further only increases the length
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

        return buildGroups(lengths, best);
    }

    /** Walks the solution from the end and merges each group into a single LengthOrder. */
    private static List<LengthOrder> buildGroups(List<LengthOrder> lengths, Best[] best) {
        final List<LengthOrder> result = new ArrayList<>();

        for (int end = lengths.size(); end > 0; end = best[end].lastGroupStart()) {
            List<LengthOrder> group = lengths.subList(best[end].lastGroupStart(), end);
            result.add(group.stream().reduce(LengthOrder::merge).orElseThrow());
        }

        Collections.reverse(result);
        return result;
    }
}

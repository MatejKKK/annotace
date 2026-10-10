package cz.cvut.kbss.annotace;

import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;

import java.util.Collection;
import java.util.List;

public class Tests {

    private static class CorrectResult {
        private final String correctLemma;
        private boolean used = false;

        private CorrectResult(String correctLemma) {
            this.correctLemma = correctLemma;
        }

        private boolean isSame(String actualLemma) {
            if (used) return false;
            boolean result = correctLemma.equals(actualLemma) || correctLemma.equals(actualLemma.toLowerCase());
            used = result;
            return result;
        }
    }

    public static CorrectAndTotal getCounts(final List<String> correctLemmas, final LemmatizerResult result) {
        long correct = 0, total = 0;
        for (final List<SingleLemmaResult> it : result.getResult()) {
            for (final SingleLemmaResult singleLemmaResult : it) {
                total++;
                if (correctLemmas.contains(singleLemmaResult.getLemma().toLowerCase()) || correctLemmas.contains(singleLemmaResult.getLemma())) {
                    correct++;
                }
                else System.out.println(singleLemmaResult.getLemma().toLowerCase());
            }
        }

        return new CorrectAndTotal(correct, total);
    }

    public static boolean simpleRateTest(final List<String> correctLemmas, final LemmatizerResult result, double successRate) {
        if (successRate <= 0. || successRate > 100.)  throw new IllegalArgumentException("Percentage has to be between 0 and 100.");
        if (result.getResult().isEmpty() || result.getResult().stream().anyMatch(List::isEmpty))  throw new AssertionError("Empty result");

        boolean sameSize = Math.abs(
                correctLemmas.size() - result.getResult().stream().flatMap(Collection::stream).toList().size()
        ) <= correctLemmas.size() / 25.0;

        if (!sameSize) {
            System.out.println("Sizes of expected and actual lists are too much different.");
            return false;
        }

        final CorrectAndTotal counts = getCounts(correctLemmas, result);
        final double rate = 100. * counts.rate();
        boolean success = rate >= successRate;

        System.out.println(
                "Tested " + result.getResult().size() + (result.getResult().size() == 1 ? " paragraph including " : " paragraps including ") +
                        counts.total() + " words, dots and commas."
        );
        System.out.println(
                counts.correct() + " of them was lemmatizered correctly. This makes precision " + rate + "% which is " +
                        (success ? "higher (or equal)" : "lower") + " than success rate (" +  successRate + "%)."
        );

        return success;
    }

    public static boolean completeRateTest(
            List<String> correctLemmas,
            String originalText,
            LemmatizerResult result,
            double successRate,
            int basePositionTolerance
    ) {
        if (successRate <= 0. || successRate > 100.)
            throw new IllegalArgumentException("Percentage has to be between 0 and 100.");
        if (basePositionTolerance < 0)
            throw new IllegalArgumentException("basePositionTolerance cannot be negative.");
        if (basePositionTolerance >= correctLemmas.size())
            throw new IllegalArgumentException("Tolerance cannot be larger than correct list length.");
        if (result.getResult().isEmpty() || result.getResult().stream().anyMatch(List::isEmpty))
            throw new AssertionError("Empty result");


        boolean sameSize = true; //todo

        if (!sameSize) {
            System.out.println("Sizes of expected and actual lists are too much different.");
            return false;
        }

        List<SingleLemmaResult> singleLemmaResults = result.getResult().stream()
                .flatMap(Collection::stream)
                .toList();

        int total = singleLemmaResults.size();
        int diff = correctLemmas.size() - total;

//        System.out.println(originalText);
//
//        singleLemmaResults.forEach(singleLemmaResult ->
//            System.out.println(originalText.contains(singleLemmaResult.getToken()) + ": " + singleLemmaResult.getToken())
//        );

        List<String> actual = singleLemmaResults.stream()
                .filter(it -> originalText.contains(it.getToken()))
                .map(SingleLemmaResult::getLemma)
                .toList();

        int tolerance = (int) (basePositionTolerance + Math.ceil(Math.sqrt(correctLemmas.size())));

        final List<CorrectResult> expected = List.copyOf(
                correctLemmas.stream().map(CorrectResult::new).toList()
                );

        diff = Math.max(diff, 0);
        int correct = 0;

        System.out.println("Expected lemmas: ");
        expected.forEach(it -> System.out.println(it.correctLemma));

        for (int i = 0; i < actual.size(); ++i) {
            String actualLemma = actual.get(i);
            System.out.println(actualLemma);

            int lowBound = Math.max(i - tolerance, 0);
            int highBound = Math.min(i + tolerance + diff, expected.size() - 1);

            for (int j = lowBound; j <= highBound; ++j) {
                if (expected.get(j).isSame(actualLemma)) {
                    ++correct;
                    System.out.println("correct: " + actualLemma + " on position " + i);
                    j = highBound + 1;
                }
            }
        }

        final double rate = 100. * correct / total;
        final boolean success = rate >= successRate;

        System.out.println(
                "Tested " + result.getResult().size() + (result.getResult().size() == 1 ? " paragraph including " : " paragraps including ") +
                        total + " words, dots and commas."
        );
        System.out.println(
                correct + " of them was lemmatizered correctly. This makes precision " + rate + "% which is " +
                        (success ? "higher (or equal)" : "lower") + " than success rate (" +  successRate + "%)."
        );

        return success;
    }

    public static double testingStatistic(CorrectAndTotal counts, double expectedMeanValue) {
        final double dispersion = (counts.correct() * ((1. - counts.rate()) * (1. - counts.rate())) +
                counts.incorrect() * counts.rate() * counts.rate())
                / (counts.total() - 1);

        return Math.sqrt(counts.total()) *
                (counts.rate() - expectedMeanValue) /   //reason why testingStatistic is smaller than 0
                Math.sqrt(dispersion);
    }

    public static boolean statisticsTest(CorrectAndTotal expectedCounts, CorrectAndTotal actualCounts, double alfaInPercentages) {

        if (expectedCounts.rate() <= actualCounts.rate()) return true;  //One-side test does not have any price in this case.

        final double invertedAlpha = 1. - (alfaInPercentages * 0.01);
        final double testingStatistic = testingStatistic(actualCounts, expectedCounts.rate());
        final double quantile = -Distributions.studentQuantile(actualCounts.total() - 1, invertedAlpha);

        return testingStatistic >= quantile;
    }
}
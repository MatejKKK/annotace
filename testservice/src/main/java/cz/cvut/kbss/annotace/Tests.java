package cz.cvut.kbss.annotace;

import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;

import java.util.List;

public class Tests {

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
        if (result.getResult().isEmpty() || result.getResult().getFirst().isEmpty())  throw new AssertionError("Empty result");

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
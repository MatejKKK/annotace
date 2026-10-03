import cz.cvut.kbss.annotace.CorrectAndTotal;
import cz.cvut.kbss.annotace.Distributions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static cz.cvut.kbss.annotace.Tests.testingStatistic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class DistributionsAndStatisticsTest {

    private boolean approximatelyEqual(double a, double b) {
        double diff = Math.abs(a - b);
        return diff < 0.01;
    }

    /**
     * @link <a href="https://cmp.felk.cvut.cz/~navara/pms/PMScvic.pdf">source</a>
     */
    static Stream<Arguments> inputProviderStudent() {
        return Stream.of(
                arguments(2, 0.95, 2.92),
                arguments(12, 0.999, 3.93),
                arguments(100, 0.99, 2.36)
        );
    }

    @ParameterizedTest
    @MethodSource("inputProviderStudent")
    public void studentQuantileTest(int degreesOfFreedom, double level, double expected) {
        double result = Distributions.studentQuantile(degreesOfFreedom, level);
        assertTrue(approximatelyEqual(expected, result));
    }

    static Stream<Arguments> inputProviderStat() {
        return Stream.of(
                arguments(200, 190, 5., -3.236_3, false),
                arguments(1_000, 992, 0.1, -2.839_1, true)
        );
    }

    @ParameterizedTest
    @MethodSource("inputProviderStat")
    public void statisticalTest(long total, long correct, double alfaInPercentages, double expectedTestStatistic, boolean expectedResult) {
        CorrectAndTotal counts = new CorrectAndTotal(correct, total);
        double invertedALfa = 1. - (alfaInPercentages * 0.01);
        double quantile = -Distributions.studentQuantile(counts.total() - 1, invertedALfa);
        double testingStatistic = testingStatistic(counts, 1);

        assertTrue(approximatelyEqual(expectedTestStatistic, testingStatistic));
        assertEquals(expectedResult, testingStatistic >= quantile);
    }
}

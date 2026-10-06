import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MinuteTokensTest {
    private Double tested(String input) {
        if (input.contains("Rate limit reached for model") && input.contains("tokens per minute ")) {
            final int startIndex = input.indexOf("Please try again in ");
            if (startIndex == -1) {
                throw new RuntimeException("input sending request: " + input);
            }
            final int endIndex = input.lastIndexOf(". Need more tokens? ");
            String time = input.substring(startIndex + 20, endIndex);
            final boolean ms = time.contains("ms");
            while (time.endsWith("s")) {
                time = time.replace("s", "");
            }
            while (time.endsWith("m")) {
                time = time.replace("m", "");
            }
            return Double.parseDouble(time) * (ms ? 1 : 1000);
        }
        else throw new RuntimeException("input sending request: " + input);
    }

    static Stream<Arguments> multiParamProviderTokens() {
        return MultiParamProviders.multiParamProviderTokens();
    }

    @ParameterizedTest
    @MethodSource("multiParamProviderTokens")
    void tokensTest(String input, double expectedTime) {
        assertEquals(expectedTime, tested(input));
    }
}

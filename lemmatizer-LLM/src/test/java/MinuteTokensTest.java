import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

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
        return Stream.of(
                arguments("""
                        java.lang.RuntimeException: Error sending request: API error: status=429, body={"error":{"message":"Rate limit reached for model `openai/gpt-oss-20b` in organization `org_01m2jqatmbe5gsxj6afjp7zd6d` service tier `on_demand` on tokens per minute (TPM): Limit 8000, Used 5420, Requested 2757. Please try again in 1.3275s. Need more tokens? Upgrade to Dev Tier today at https://console.groq.com/settings/billing","type":"tokens","code":"rate_limit_exceeded"}}
                        """, 1_327.5),
                arguments("""
                        java.lang.RuntimeException: Error sending request: API error: status=429, body={"error":{"message":"Rate limit reached for model `openai/gpt-oss-120b` in organization `org_01m2jqatmbe5gsxj6afjp7zd6d` service tier `on_demand` on tokens per minute (TPM): Limit 8000, Used 5420, Requested 2757. Please try again in 670.5ms. Need more tokens? Upgrade to Dev Tier today at https://console.groq.com/settings/billing","type":"tokens","code":"rate_limit_exceeded"}}
                        """, 670.5)
        );
    }

    @ParameterizedTest
    @MethodSource("multiParamProviderTokens")
    void tokensTest(String input, double expectedTime) {
        assertEquals(expectedTime, tested(input));
    }
}

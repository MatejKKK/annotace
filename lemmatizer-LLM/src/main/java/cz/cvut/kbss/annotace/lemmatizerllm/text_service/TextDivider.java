package cz.cvut.kbss.annotace.lemmatizerllm.text_service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static cz.cvut.kbss.annotace.lemmatizerllm.text_service.TextExtractor.*;

public class TextDivider {

    private TextDivider() {}
    /**
     * Merges consecutive paragraphs into exactly n chunks (or paragraphs.length chunks
     * if n >= paragraphs.length), minimizing the variance of chunk lengths in characters.
     * Paragraphs are indivisible and the original order is preserved.
     *
     * @param paragraphs input paragraphs
     * @param n          desired number of chunks
     */
    public static Iterator<String> merge(String[] paragraphs, int n) {
        final int m = paragraphs.length;
        if (m == 0) {
            return new ArrayList<String>().iterator();
        }
        if (n < 1) {
            throw new IllegalArgumentException("n must be >= 1");
        }
        if (n >= m) {
            return Arrays.asList(paragraphs).iterator(); // each paragraph on its own
        }

        final int sepLen = PARAGRAPH_BREAK.length();

        // prefix[i] = total length of the first i paragraphs, each followed by a separator
        long[] prefix = new long[m + 1];
        for (int i = 0; i < m; ++i) {
            prefix[i + 1] = prefix[i] + paragraphs[i].length() + sepLen;
        }

        // Dp[k][j] = minimal sum of squared chunk lengths for the first j paragraphs split into k chunks.
        // The total length is constant for fixed n, so minimizing the sum of squares
        // is equivalent to minimizing the variance.
        long[][] dp = new long[n + 1][m + 1];
        int[][] cut = new int[n + 1][m + 1];
        for (long[] row : dp) {
            Arrays.fill(row, Long.MAX_VALUE);
        }
        dp[0][0] = 0;

        for (int k = 1; k <= n; ++k) {
            // need at least k paragraphs for k chunks, and leave n-k paragraphs for the rest
            for (int j = k; j <= m - (n - k); ++j) {
                for (int i = k - 1; i < j; ++i) {
                    if (dp[k - 1][i] == Long.MAX_VALUE) continue;
                    long len = prefix[j] - prefix[i] - sepLen; // no trailing separator
                    long cost = dp[k - 1][i] + len * len;
                    if (cost < dp[k][j]) {
                        dp[k][j] = cost;
                        cut[k][j] = i;
                    }
                }
            }
        }

        // Reconstruct the chunk boundaries
        int[] bounds = new int[n + 1];
        bounds[n] = m;
        for (int k = n; k >= 1; --k) {
            bounds[k - 1] = cut[k][bounds[k]];
        }

        List<String> result = new ArrayList<>(n);
        for (int k = 0; k < n; ++k) {
            StringBuilder sb = new StringBuilder();
            for (int i = bounds[k]; i < bounds[k + 1]; ++i) {
                if (i > bounds[k]) sb.append(PARAGRAPH_BREAK);
                sb.append(paragraphs[i]);
            }
            result.add(sb.toString());
        }
        return result.iterator();
    }
}

package cz.cvut.kbss.textanalysis.service.lemma_aligner;

import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Assigns lemmas from the LLM output to the original text nodes (chunks).
 * <p>
 * The LLM output cannot be trusted (it alters words, duplicates and drops passages, loses paragraph
 * boundaries), so only <b>the lemma and the negation flag</b> are taken from it. Tokens and whitespace are
 * always derived from the original text, so {@code leadingSpaces + sum(token + trailingSpaces)} of every chunk
 * is exactly the original text.
 * <p>
 * Procedure:
 * <ol>
 *   <li>the chunk texts and the LLM tokens are split into "atoms" (runs of letters/digits, case-insensitive),</li>
 *   <li>the atom sequences are aligned by a global LCS (Hirschberg), which survives duplicated, dropped and
 *       reordered paragraphs because it relies neither on paragraph order nor on their count,</li>
 *   <li>similar words in the gaps between equal atoms are paired up (e.g. "zemských" ~ "zemský"),</li>
 *   <li>tokens are built from the original; punctuation marks are separate tokens, whitespace is appended
 *       as trailingSpaces.</li>
 * </ol>
 * Atoms without an LLM lemma get their original form as the lemma. A chunk with a low {@link Chunk#coverage()}
 * should be lemmatized separately.
 */
public final class LemmaAligner {

    private static final int MAX_FUZZY_GAP_CELLS = 250_000;

    private LemmaAligner() {}

    /**
     * @param chunks texts of the text nodes in document order
     * @param llm    lemmatization output for the whole text (may be null)
     * @return a list of the same length as {@code chunks}
     */
    public static List<Chunk> align(final List<String> chunks, final LemmatizerResult llm) {
        // --- 1) LLM tokens and their atoms
        final List<SingleLemmaResult> llmTokens = flatten(llm);
        final List<String> llmKeys = new ArrayList<>();
        final List<Integer> llmAtomToken = new ArrayList<>();
        final int[] atomsPerToken = new int[llmTokens.size()];
        for (int t = 0; t < llmTokens.size(); ++t) {
            final String token = llmTokens.get(t).getToken();
            for (final int[] r : atoms(token)) {
                llmKeys.add(normalize(token.substring(r[0], r[1])));
                llmAtomToken.add(t);
                ++atomsPerToken[t];
            }
        }

        // --- 2) atoms of the original chunks
        final List<String> origKeys = new ArrayList<>();
        final List<List<int[]>> chunkAtoms = new ArrayList<>();
        final int[] firstAtom = new int[chunks.size() + 1];
        for (int c = 0; c < chunks.size(); ++c) {
            final String text = chunks.get(c);
            firstAtom[c] = origKeys.size();
            final List<int[]> at = atoms(text);
            chunkAtoms.add(at);
            for (final int[] r : at) {
                origKeys.add(normalize(text.substring(r[0], r[1])));
            }
        }
        firstAtom[chunks.size()] = origKeys.size();

        // --- 3) global alignment (LCS) + pairing of similar words
        final Map<String, Integer> ids = new HashMap<>();
        final int[] a = toIds(origKeys, ids);
        final int[] b = toIds(llmKeys, ids);
        final int[] match = new int[a.length]; // original atom index -> LLM atom index, or -1
        Arrays.fill(match, -1);
        lcs(a, 0, a.length, b, 0, b.length, match);
        fuzzyGaps(origKeys, llmKeys, match);

        // --- 4) tokens built from the original
        final List<Chunk> result = new ArrayList<>(chunks.size());
        for (int c = 0; c < chunks.size(); ++c) {
            final String text = chunks.get(c);
            final List<int[]> at = chunkAtoms.get(c);
            final List<Tok> toks = new ArrayList<>();
            int matchedAtoms = 0;

            int k = 0;
            while (k < at.size()) {
                final int g = firstAtom[c] + k;
                int token = match[g] >= 0 ? llmAtomToken.get(match[g]) : -1;
                int e = k;
                if (token >= 0) {
                    // all atoms of one LLM token (e.g. "Karlo-Ferdinandova") must be adjacent without whitespace
                    while (e + 1 < at.size()
                            && match[g + (e - k) + 1] >= 0
                            && llmAtomToken.get(match[g + (e - k) + 1]) == token
                            && !containsWhitespace(text, at.get(e)[1], at.get(e + 1)[0])) {
                        e++;
                    }
                    if (e - k + 1 != atomsPerToken[token]) {
                        token = -1; // only part of the LLM token matched -> its lemma would not fit
                    }
                }
                if (token >= 0) {
                    toks.add(new Tok(at.get(k)[0], at.get(e)[1], llmTokens.get(token)));
                    matchedAtoms += e - k + 1;
                    k = e + 1;
                } else {
                    toks.add(new Tok(at.get(k)[0], at.get(k)[1], null));
                    k++;
                }
            }

            List<SingleLemmaResult> tokens = build(text, toks);
            if (!reconstructs(text, tokens)) { // safety net, should not happen
                final List<Tok> plain = new ArrayList<>();
                for (final int[] r : at) {
                    plain.add(new Tok(r[0], r[1], null));
                }
                tokens = build(text, plain);
                matchedAtoms = 0;
                if (!reconstructs(text, tokens)) {
                    continue;
                }
            }
            result.add(new Chunk(tokens, at.isEmpty() ? 1.0 : (double) matchedAtoms / at.size()));
        }
        return result;
    }

    // ------------------------------------------------------------------ token building

    private static final class Tok {

        final int start;

        final int end;

        final SingleLemmaResult src;

        Tok(int start, int end, SingleLemmaResult src) {
            this.start = start;
            this.end = end;
            this.src = src;
        }
    }

    private static List<SingleLemmaResult> build(final String text, final List<Tok> toks) {
        final List<SingleLemmaResult> out = new ArrayList<>();
        final StringBuilder leading = new StringBuilder();
        int pos = 0;
        for (final Tok t : toks) {
            appendGap(text, pos, t.start, out, leading);
            out.add(newToken(text.substring(t.start, t.end), t.src));
            pos = t.end;
        }
        appendGap(text, pos, text.length(), out, leading);
        if (!out.isEmpty()) {
            out.getFirst().setLeadingSpaces(leading.toString());
        }
        return out;
    }

    /** Whitespace is attached to the previous token (to leading at the chunk start); any other character is a separate token. */
    private static void appendGap(final String text, final int from, final int to,
                                  final List<SingleLemmaResult> out, final StringBuilder leading) {
        int i = from;
        while (i < to) {
            final int cp = text.codePointAt(i);
            final int len = Character.charCount(cp);
            if (isSpace(cp)) {
                final String ws = text.substring(i, i + len);
                if (out.isEmpty()) {
                    leading.append(ws);
                } else {
                    final SingleLemmaResult last = out.getLast();
                    last.setTrailingSpaces(last.getTrailingSpaces() + ws);
                }
            } else {
                out.add(newToken(text.substring(i, i + len), null));
            }
            i += len;
        }
    }

    private static SingleLemmaResult newToken(final String token, final SingleLemmaResult src) {
        final SingleLemmaResult r = new SingleLemmaResult();
        r.setToken(token);
        if (src != null && src.getLemma() != null && !src.getLemma().isEmpty()) {
            r.setLemma(src.getLemma());
            r.setNegated(src.isNegated());
        } else {
            r.setLemma(token);
        }
        r.setLeadingSpaces("");
        r.setTrailingSpaces("");
        return r;
    }

    private static boolean reconstructs(final String text, final List<SingleLemmaResult> tokens) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.size(); ++i) {
            final SingleLemmaResult t = tokens.get(i);
            if (i == 0) {
                sb.append(t.getLeadingSpaces());
            }
            sb.append(t.getToken()).append(t.getTrailingSpaces());
        }
        return sb.toString().equals(text);
    }

    // ------------------------------------------------------------------ atoms

    private static List<SingleLemmaResult> flatten(final LemmatizerResult llm) {
        final List<SingleLemmaResult> out = new ArrayList<>();
        if (llm != null && llm.getResult() != null) {
            for (final List<SingleLemmaResult> sentence : llm.getResult()) {
                if (sentence == null) {
                    continue;
                }
                for (final SingleLemmaResult t : sentence) {
                    if (t != null && t.getToken() != null) {
                        out.add(t);
                    }
                }
            }
        }
        return out;
    }

    /** Runs of letters and digits as [start, end). */
    private static List<int[]> atoms(final String s) {
        final List<int[]> out = new ArrayList<>();
        final int n = s.length();
        int i = 0;
        while (i < n) {
            int cp = s.codePointAt(i);
            if (isWordChar(cp)) {
                final int st = i;
                while (i < n && isWordChar(cp = s.codePointAt(i))) {
                    i += Character.charCount(cp);
                }
                out.add(new int[]{st, i});
            } else {
                i += Character.charCount(cp);
            }
        }
        return out;
    }

    private static boolean isWordChar(final int cp) {
        return Character.isLetterOrDigit(cp) || Character.getType(cp) == Character.NON_SPACING_MARK;
    }

    private static boolean isSpace(final int cp) {
        return Character.isWhitespace(cp) || Character.isSpaceChar(cp);
    }

    private static boolean containsWhitespace(final String s, final int from, final int to) {
        for (int i = from; i < to; ) {
            final int cp = s.codePointAt(i);
            if (isSpace(cp)) {
                return true;
            }
            i += Character.charCount(cp);
        }
        return false;
    }

    private static String normalize(final String s) {
        return s.toLowerCase(Locale.ROOT);
    }

    private static int[] toIds(final List<String> keys, final Map<String, Integer> ids) {
        final int[] out = new int[keys.size()];
        for (int i = 0; i < out.length; ++i) {
            out[i] = ids.computeIfAbsent(keys.get(i), k -> ids.size());
        }
        return out;
    }

    // ------------------------------------------------------------------ LCS (Hirschberg, linear memory)

    private static void lcs(final int[] a, int aLo, int aHi, final int[] b, int bLo, int bHi, final int[] match) {
        while (aLo < aHi && bLo < bHi && a[aLo] == b[bLo]) {
            match[aLo++] = bLo++;
        }
        while (aLo < aHi && bLo < bHi && a[aHi - 1] == b[bHi - 1]) {
            match[--aHi] = --bHi;
        }
        final int n = aHi - aLo;
        final int m = bHi - bLo;
        if (n == 0 || m == 0) {
            return;
        }
        if (n == 1) {
            for (int j = bLo; j < bHi; ++j) {
                if (b[j] == a[aLo]) {
                    match[aLo] = j;
                    return;
                }
            }
            return;
        }
        final int mid = aLo + n / 2;
        final int[] left = lengths(a, aLo, mid, b, bLo, bHi, false);
        final int[] right = lengths(a, mid, aHi, b, bLo, bHi, true);
        int best = -1;
        int split = 0;
        for (int j = 0; j <= m; ++j) {
            final int s = left[j] + right[m - j];
            if (s > best) {
                best = s;
                split = j;
            }
        }
        lcs(a, aLo, mid, b, bLo, bLo + split, match);
        lcs(a, mid, aHi, b, bLo + split, bHi, match);
    }

    /**
     * Forward: res[j] = LCS(a[aLo..aHi), b[bLo..bLo+j)).
     * Reverse: res[j] = LCS(a[aLo..aHi), the last j elements of b[bLo..bHi)).
     */
    private static int[] lengths(final int[] a, final int aLo, final int aHi, final int[] b, final int bLo,
                                 final int bHi, final boolean reverse) {
        final int m = bHi - bLo;
        int[] prev = new int[m + 1];
        int[] cur = new int[m + 1];
        for (int step = 0; step < aHi - aLo; step++) {
            final int x = reverse ? a[aHi - 1 - step] : a[aLo + step];
            cur[0] = 0;
            for (int j = 1; j <= m; j++) {
                final int y = reverse ? b[bHi - j] : b[bLo + j - 1];
                cur[j] = x == y ? prev[j - 1] + 1 : Math.max(prev[j], cur[j - 1]);
            }
            final int[] tmp = prev;
            prev = cur;
            cur = tmp;
        }
        return prev;
    }

    // ------------------------------------------------------------------ pairing of similar words

    private static void fuzzyGaps(final List<String> o, final List<String> l, final int[] match) {
        int prevO = -1;
        int prevL = -1;
        for (int i = 0; i <= o.size(); ++i) {
            if (i == o.size() || match[i] >= 0) {
                final int lEnd = i == o.size() ? l.size() : match[i];
                fuzzyGap(o, prevO + 1, i, l, prevL + 1, lEnd, match);
                prevO = i;
                prevL = lEnd;
            }
        }
    }

    private static void fuzzyGap(final List<String> o, final int oLo, final int oHi, final List<String> l,
                                 final int lLo, final int lHi, final int[] match) {
        final int g = oHi - oLo;
        final int h = lHi - lLo;
        if (g <= 0 || h <= 0 || (long) g * h > MAX_FUZZY_GAP_CELLS) {
            return;
        }
        final int[][] dp = new int[g + 1][h + 1];
        for (int i = g - 1; i >= 0; --i) {
            for (int j = h - 1; j >= 0; --j) {
                dp[i][j] = similar(o.get(oLo + i), l.get(lLo + j))
                        ? dp[i + 1][j + 1] + 1
                        : Math.max(dp[i + 1][j], dp[i][j + 1]);
            }
        }
        int i = 0;
        int j = 0;
        while (i < g && j < h) {
            if (similar(o.get(oLo + i), l.get(lLo + j)) && dp[i][j] == dp[i + 1][j + 1] + 1) {
                match[oLo + i] = lLo + j;
                ++i;
                ++j;
            } else if (dp[i + 1][j] >= dp[i][j + 1]) {
                ++i;
            } else {
                ++j;
            }
        }
    }

    /** Words with a long enough common beginning and similar length (another case/form of the same word). */
    private static boolean similar(final String x, final String y) {
        final int min = Math.min(x.length(), y.length());
        final int max = Math.max(x.length(), y.length());
        if (min < 3 || min * 10 < max * 7) {
            return false;
        }
        if (!Character.isLetter(x.codePointAt(0)) || !Character.isLetter(y.codePointAt(0))) {
            return false;
        }
        int p = 0;
        while (p < min && x.charAt(p) == y.charAt(p)) {
            ++p;
        }
        return p >= 3 && p * 10 >= max * 6;
    }
}
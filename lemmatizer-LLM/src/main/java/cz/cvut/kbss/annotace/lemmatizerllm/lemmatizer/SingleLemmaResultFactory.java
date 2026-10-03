package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer;

import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;

import java.util.List;

public class SingleLemmaResultFactory {

    private final String delimiter;

    public SingleLemmaResultFactory(String delimiter) {
        this.delimiter = "\\" + delimiter;
    }

    public SingleLemmaResult createSingleLemmaResult(String line) {
        SingleLemmaResult singleLemmaResult = parseSingleLemmaResult(line);
        singleLemmaResult.setNegated(false);
        return singleLemmaResult;
    }

    protected final SingleLemmaResult parseSingleLemmaResult(String line) {
        while (line.endsWith(" ")) {
            line = line.substring(0, line.length() - 1);
        }
        String[] words = line.split(delimiter);
        final SingleLemmaResult result = new SingleLemmaResult();

        if (words.length == 2) {
            result.setToken(words[0]);
            result.setLemma(words[1]);
            return result;
        }
        else if (words.length == 4) {
            result.setToken(words[0].concat(words[1]));
            result.setLemma(words[2].concat(words[3]));
            return result;
        }
        else if (words.length > 0) {
            result.setToken(words[0]);
            result.setLemma(words[0]);
            return result;
        }
        result.setToken("");
        result.setLemma("");
        return result;
    }

    private static String getLeadingSpaces(int index, String originalParagraph) {
        if (index == 0) return "";

        originalParagraph = originalParagraph.substring(0, index);
        char[] reversedParagraph = new StringBuilder(originalParagraph).reverse().toString().toCharArray();
        StringBuilder spaces = new StringBuilder();

        for (final char c : reversedParagraph) {
            if (c == ' ') spaces.append(' ');
            else return spaces.toString();
        }
        return spaces.toString();
    }

    private static String getTrailingSpaces(int index, String originalParagraph) {
        if (index > originalParagraph.length() - 1) return "";

        originalParagraph = originalParagraph.substring(index);
        StringBuilder spaces = new StringBuilder();

        for (final char c : originalParagraph.toCharArray()) {
            if (c == ' ') spaces.append(' ');
            else return spaces.toString();
        }
        return spaces.toString();
    }

    public static List<SingleLemmaResult> addSpacesToLemma(List<SingleLemmaResult> paragraph, String originalParagraph) {
        int lastIndex = 0;

        for (final SingleLemmaResult lemma : paragraph) {
            final String token = lemma.getToken();
            int currentIndex = originalParagraph.indexOf(token, lastIndex);

            if (currentIndex == -1) {
                currentIndex = originalParagraph.indexOf(token.toLowerCase(), lastIndex);

                if (currentIndex == -1) {
                    currentIndex = originalParagraph.indexOf(token);

                    if (currentIndex == -1) {
                        currentIndex = originalParagraph.indexOf(token.toLowerCase());
                    }
                    else System.out.println("Token " + token + " not found.");
                }
            }

            if (currentIndex > -1) {
                lemma.setLeadingSpaces(getLeadingSpaces(currentIndex, originalParagraph));
                lemma.setTrailingSpaces(getTrailingSpaces(currentIndex + token.length(), originalParagraph));

                if (lastIndex < currentIndex && lastIndex + lemma.getToken().length() + 1 > currentIndex) {
                    lastIndex = Math.min(currentIndex + lemma.getToken().length() - 1, originalParagraph.length() - 1);
                }
            }
        }

        return paragraph;
    }
}

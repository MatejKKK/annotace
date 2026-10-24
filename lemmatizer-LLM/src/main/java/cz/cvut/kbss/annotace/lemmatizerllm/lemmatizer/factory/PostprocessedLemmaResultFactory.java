package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.factory;

import cz.cvut.kbss.annotace.lemmatizerllm.model.Language;
import cz.cvut.kbss.annotace.lemmatizerllm.text_service.TextExtractor;
import cz.cvut.kbss.annotace.lemmatizerllm.text_service.postprocessor.CzechSublemmaProcessor;
import cz.cvut.kbss.annotace.lemmatizerllm.text_service.postprocessor.SublemmaProcessor;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostprocessedLemmaResultFactory extends LemmaResultFactory {

    private final static String[] BRACKETS = {"(", ")", "[", "]", "<", ">", "{", "}", "\"", "\""};

    private SublemmaProcessor sublemmaProcessor;
    private final boolean selectOneCharWords;
    private String paragraph;
    private int currIndex = 0;
    private SingleLemmaResult lastResult = null;
    private boolean lastIsLinked = false;

    public PostprocessedLemmaResultFactory(String delimiter, boolean selectOneCharWords, Language language) {
        super(delimiter);
        this.selectOneCharWords = selectOneCharWords;
        setLanguage(language);
    }

    @Override
    public List<SingleLemmaResult> createSingleLemmaResult(String line) {
        final List<SingleLemmaResult> results = this._createSingleLemmaResult(line);

        if (lastIsLinked && currIndex > 0) {
            final int resultsIndex = paragraph.indexOf(results.getFirst().getToken(), currIndex);

            if (resultsIndex != -1) {
                final String space = paragraph.substring(currIndex, resultsIndex);

                if (!space.isBlank()) {
                    List<SingleLemmaResult> addit = findAdditionalResult(space.toCharArray());
                    if (!addit.isEmpty()) {
                        System.out.println("addit: " + addit);
                    }
                    results.addAll(0, addit);
                }
            }
        }
        lastResult = results.getLast();

        final int index = paragraph.indexOf(lastResult.getToken(), currIndex);
        if (index == -1) {
            lastIsLinked = false;
            currIndex = currIndex + results.stream()
                .map(it -> it.getLeadingSpaces().length() + it.getToken().length() + it.getTrailingSpaces().length())
                .reduce(0, Integer::sum);
        }
        else {
            lastIsLinked = true;
            currIndex = index + lastResult.getToken().length();
        }

        return results;
    }

    private List<SingleLemmaResult> _createSingleLemmaResult(String line) {
        SingleLemmaResult singleLemmaResult = parseSingleLemmaResult(line);
        final String lemma = singleLemmaResult.getLemma();

        if (selectOneCharWords && lemma.length() == 1 && !SYMBOLS.contains(lemma) && lemma.equals(singleLemmaResult.getToken())) {
            singleLemmaResult.setLemma(lemma.concat("-1"));
            return new ArrayList<>(List.of(singleLemmaResult));
        }
        else if (!lemma.matches("^(?=.*[A-Z])(?=.*[0-9])[A-Z0-9]+$") || lemma.length() < 2) {
            return new ArrayList<>(List.of(singleLemmaResult));
        }

        for (int i = 1; i < BRACKETS.length; i+=2) {
            if (lemma.startsWith(BRACKETS[i - 1]) && lemma.endsWith(BRACKETS[i])) {
                SingleLemmaResult result1 = new SingleLemmaResult(BRACKETS[i - 1], BRACKETS[i - 1], false);
                SingleLemmaResult result2 = new SingleLemmaResult(
                        singleLemmaResult.getToken().substring(1, singleLemmaResult.getToken().length() - 2),
                        lemma.substring(1, lemma.length() - 2),
                        false
                );
                SingleLemmaResult result3 = new SingleLemmaResult(BRACKETS[i], BRACKETS[i], false);
                return new ArrayList<>(List.of(result1, result2, result3));
            }
        }

        for (final String symbol : SYMBOLS) {
            if (lemma.endsWith(symbol)) {
                SingleLemmaResult result1 = new SingleLemmaResult(
                        lemma.substring(0, lemma.length() - 1),
                        singleLemmaResult.getToken().substring(0, singleLemmaResult.getToken().length() - 1),
                        false
                );
                SingleLemmaResult result2 = new SingleLemmaResult(symbol, symbol, false);
                return new ArrayList<>(List.of(result1, result2));
            }
        }

        return new ArrayList<>(List.of(singleLemmaResult));
    }

    @Override
    public List<SingleLemmaResult> addSpacesToLemmas(List<SingleLemmaResult> results) {
        if (lastIsLinked) {
            final int resultsIndex = paragraph.indexOf(results.getLast().getToken(), currIndex);

            if (resultsIndex != -1) {
                final int lastUsedIndex = resultsIndex + results.getLast().getToken().length();

                if (lastUsedIndex < paragraph.length()) {
                    final String space = paragraph.substring(lastUsedIndex);

                    if (!space.isBlank()) {
                        results.addAll(findAdditionalResult(space.toCharArray()));
                    }
                }
            }
        }

        for (int i = 0; i < results.size() - 1; ++i) {
            if (!results.get(i).getTrailingSpaces().isEmpty()) continue;

            final String nextToken = results.get(i + 1).getToken();
            if (SYMBOLS.stream().anyMatch(it -> it.equals(nextToken))) continue;

            final SingleLemmaResult lemmaResult = results.get(i);
            lemmaResult.setTrailingSpaces(" ");
            results.set(i, lemmaResult);
        }

        return results;
    }

    @Override
    public void setLanguage(Language language) {
        this.sublemmaProcessor = switch (language) {
            case CS -> new CzechSublemmaProcessor();
            default -> new SublemmaProcessor();
        };
    }

    @Override
    public void setParagraph(String paragraph) {
        this.paragraph = paragraph;
        this.currIndex = 0;
        this.lastResult = null;
        this.lastIsLinked = false;
    }

    /**
     *
     * @param space is substring of end of the paragraph that is not lemmatized.
     * @return all tokens founded in space as {@link ArrayList} of {@link SingleLemmaResult} where token equals lemma
     */
    private static List<SingleLemmaResult> findAdditionalResult(final char[] space) {
        final List<SingleLemmaResult> additionalResult = new ArrayList<>();
        StringBuilder sB = new StringBuilder();
        int spacesCount = 0;

        for (char c : space) {
            if (c == ' ') {
                ++spacesCount;
            }
            else if (spacesCount == 0) {
                sB.append(c);
            }
            else {
                final String token = sB.toString();
                if (!(token.isBlank() || token.contains(TextExtractor.PARAGRAPH_MERGE_MARK))) {
                    additionalResult.addLast(new SingleLemmaResult(
                            token,
                            token,
                            new String(new char[spacesCount]).replace('\0', ' '),
                            false
                    ));
                }
                sB = new StringBuilder();
                spacesCount = 0;
                sB.append(c);
            }
        }

        final String token = sB.toString();
        if (!(token.isBlank() || token.contains(TextExtractor.PARAGRAPH_MERGE_MARK))) {
            additionalResult.addLast(new SingleLemmaResult(
                    token,
                    token,
                    new String(new char[spacesCount]).replace('\0', ' '),
                    false
            ));
        }
        return additionalResult;
    }
}

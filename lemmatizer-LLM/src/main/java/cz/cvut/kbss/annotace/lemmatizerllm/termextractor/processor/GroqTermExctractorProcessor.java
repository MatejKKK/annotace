package cz.cvut.kbss.annotace.lemmatizerllm.termextractor.processor;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMTermExtractorConf;
import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq.GroqClient;
import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq.GroqProxy;
import cz.cvut.kbss.annotace.lemmatizerllm.model.Language;
import cz.cvut.kbss.annotace.lemmatizerllm.termextractor.promts.LongPromptTexts;
import cz.cvut.kbss.annotace.lemmatizerllm.termextractor.promts.PromptGenerator;
import cz.cvut.kbss.textanalysis.termextractor.model.OrdinaryTextChunk;
import cz.cvut.kbss.textanalysis.termextractor.model.Paragraph;
import cz.cvut.kbss.textanalysis.termextractor.model.Term;
import cz.cvut.kbss.textanalysis.termextractor.model.TextChunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.lang.Character.isDigit;

public class GroqTermExctractorProcessor {

    private record TermInText (
            String term,
            String termAndId
    ) { }

    private GroqProxy groqProxy;
    private List<TermInText> terms;
    private PromptGenerator promptGenerator;

    public GroqTermExctractorProcessor(LLMTermExtractorConf conf, int index) {
        this.groqProxy = new GroqProxy(new GroqClient(Arrays.stream(conf.apiKeys()).map(System::getenv).toArray(String[]::new), "openai/gpt-oss-20b"));
        this.promptGenerator = new PromptGenerator(new LongPromptTexts());
    }

    public List<Paragraph> process(List<Paragraph> paragraphs, Set<Term> terms) {
        this.terms = terms.stream()
                .map(it -> new TermInText(it.getText().toLowerCase(), String.valueOf(it.getId()).concat(". ").concat(it.getText())))
                .toList();

        List<List<Paragraph>> paragraphLists = List.of(paragraphs);  //TODO get diverse paragraphs

        List<Paragraph> result = new ArrayList<>();
        for (List<Paragraph> paragraphList : paragraphLists) {
            final String contentText = paragraphList.stream()
                    .map(Paragraph::toContent)
                    .map(String::toLowerCase)
                    .map(it -> Arrays.stream(it.split(" ")).toList())
                    .flatMap(List::stream)
                    .distinct()
                    .reduce("", (a, b) -> a.concat(" ").concat(b));

            final List<String> termsSublist = possibleTermsInText(contentText);
            if (termsSublist.isEmpty()) {
                continue;
            }

            final String textForPrompt = paragraphList.stream()
                    .map(Paragraph::toString)
                    .map(String::toLowerCase)
                    .reduce("", (a, b) -> a.concat("\n").concat(b))
                    .replaceFirst("\n", "");

            final String termsForPrompt = termsSublist.stream()
                    .reduce("", (a, b) -> a.concat("\n").concat(b))
                    .replaceFirst("\n", "");

            final String prompt = promptGenerator.prompt(textForPrompt, termsForPrompt);
            System.out.println("prompt: " + prompt);
            System.out.println("terms: " + termsForPrompt);
            System.out.println("textForPrompt: " + textForPrompt);
            final String response = groqProxy.send(prompt, 2048);
            result.addAll(parseResponse(response));
        }
        return result;
    }

    public void setLanguage(Language language) {
        this.promptGenerator.setLanguage(language);
    }

    private List<Paragraph> parseResponse(String response) {
        System.out.println(response);
        final String[] lines = response.split("\n");
        final List<Paragraph> result = new ArrayList<>(lines.length);

        for (final String line : lines) {
            StringBuilder sb = new StringBuilder();
            final List<TextChunk> content = new ArrayList<>();
            boolean isTerm = false;
            boolean lastWasDelimiter = false;
            boolean passNext = false;
            int pos = 0;
            int endIndex = -1;
            int idStartIndex = -1;
            System.out.println(line);

            for (final char c : line.toCharArray()) {
                if (idStartIndex == -1) {
                    if (c >= '0' && c <= '9') {
                        idStartIndex = pos;
                    }
                    ++pos;
                    continue;
                }
                if (pos < endIndex) {
                    ++pos;
                    continue;
                }
                if (passNext) {
                    passNext = false;
                    ++pos;
                    continue;
                }
                if (c == '^') {
                    if (lastWasDelimiter) {
                        lastWasDelimiter = false;
                        if (isTerm) {
                            isTerm = false;
                            int startIndex = line.indexOf("[", pos);
                            endIndex = line.indexOf("]", startIndex);
                            content.add(new Term(sb.toString(), Long.parseLong(line.substring(startIndex + 1, endIndex))));
                            passNext = true;
                        }
                        else {
                            isTerm = true;
                            content.add(new OrdinaryTextChunk(sb.toString()));
                        }
                        sb = new StringBuilder();
                    }
                    else {
                        lastWasDelimiter = true;
                    }
                    ++pos;
                    continue;
                }
                else if (lastWasDelimiter) {
                    lastWasDelimiter = false;
                    sb.append(line.charAt(pos - 1));
                }
                sb.append(c);
                ++pos;
            }
            result.add(new Paragraph(content, Long.parseLong(line.substring(idStartIndex == -1 ? 0 : idStartIndex, line.indexOf(")")))));
        }
        return result;
    }

    private List<String> possibleTermsInText(final String text) {
        return terms.stream()
                .filter(it -> couldBeTermInText(it.term, text))
                .map(TermInText::termAndId)
                .toList();
    }

    private static boolean couldBeTermInText(String term, String text) {
        return true;  //TODO
    }
}
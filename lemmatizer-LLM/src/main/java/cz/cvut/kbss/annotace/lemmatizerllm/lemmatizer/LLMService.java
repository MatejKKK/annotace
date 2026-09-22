package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer;

import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.AbstractPromptTexts;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.LongPromptTexts;
import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq.GroqClient;
import cz.cvut.kbss.textanalysis.lemmatizer.LemmatizerApi;
import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.SentenceExtractor.*;

@Slf4j
public class LLMService implements LemmatizerApi {

    static final int BASE_TOKENS = 2048;
    static final double TOKENS_PER_WORD = 5.5;
    static final double GROWTH_EXPONENT = 1.12;
    static final int MIN_TOKENS = 2048;
    static final int MAX_TOKENS = 8192;


    private final PromptGenerator promptGenerator = new PromptGenerator(new LongPromptTexts());

    //private final GeminiClient geminiClient = new GeminiClient(System.getenv("GEMINI_API_KEY"), "gemini-3.1-pro-preview");
    private final GroqClient groqClient = new GroqClient(
            new String[]{System.getenv("GROQ_API_KEY"), System.getenv("GROQ_API_KEY_2")}, "openai/gpt-oss-20b"
    );
    //private final ClaudeClient claudeClient = new ClaudeClient(System.getenv("CLAUDE_API_KEY"), "claude-sonnet-5");
    //private final OpenAIClient openAIClient = new OpenAIClient(System.getenv("GPT_API_KEY"), "gpt-5.2");

    @Override
    public LemmatizerResult process(String text, String lang) {
        promptGenerator.setLanguage(lang);
        String[] paragraphs = text.split("\n\n\n\n");
        final List<List<SingleLemmaResult>> results = new ArrayList<>();

        for (final String paragraph : paragraphs) {
            promptGenerator.setPromptTexts(new LongPromptTexts());
            groqClient.setModel("openai/gpt-oss-20b");
            final List<String> paragraphResults = new ArrayList<>();

            final List<String> singleSentences = extractSentence(paragraph);
            final String[] sentences = mergeSentences(singleSentences, 400);

            for (int i = 0; i < sentences.length; i++) {
                if (i > 0) {
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException ignored) {}
                }
                final String prompt = promptGenerator.prompt(sentences[i]);
                final String response = groqClient.send(prompt, getMaxTokens(sentences[i].length()));
                paragraphResults.addAll(responseWordParser(response));
            }
            groqClient.setModel("openai/gpt-oss-120b");

            final String prompt = promptGenerator.prompt(wordResultObserver(paragraphResults), paragraph);
            final String response = groqClient.send(prompt, getMaxTokens(paragraph.length()));
            results.add(responseLemmaParser(response));
        }


        final LemmatizerResult result = new LemmatizerResult();
        result.setResult(results);
        result.setLemmatizer(this.getClass().getName());
        return result;
    }

    @Override
    public List<String> getSupportedLanguages() {
        return AbstractPromptTexts.SUPPORTED_LANGUAGES;
    }

    private List<SingleLemmaResult> responseLemmaParser(String response) {
        List<SingleLemmaResult> result = new ArrayList<>();
        final String[] lines = Arrays.stream(response.split("\n")).filter(s -> s.contains("-")).toArray(String[]::new);
        for (String line : lines) {
            if (line.isBlank() || line.length() < 3) continue;

            while(line.endsWith(" ")) {
                line = line.substring(0, line.length() - 1);
            }

            result.add(SingleLemmaResultFactory.createSingleLemmaResult(line));
        }
        return result;
    }

    private List<String> responseWordParser(String response) {
        List<String> result = new ArrayList<>();
        final String[] lines = Arrays.stream(response.split("\n")).filter(
                s -> s.chars().filter(ch -> ch == '-').count() == 1
        ).toArray(String[]::new);

        for (String line : lines) {
            if (line.isBlank() || line.length() < 3) continue;

            while (line.endsWith(" ")) {
                line = line.substring(0, line.length() - 1);
            }

            result.add(line);
        }
        return result;
    }

    private String wordResultObserver(List<String> paragraphResults) {
        StringBuilder result = new StringBuilder();
        for (final String paragraphResult : paragraphResults) {
            result.append(paragraphResult.concat("\n"));
        }
        System.out.println(result.toString());
        return result.toString();
    }

    private int getMaxTokens(int paragraphLength) {
        final double estimated = BASE_TOKENS + TOKENS_PER_WORD * Math.pow(paragraphLength / 5., GROWTH_EXPONENT);
        return (int) Math.min(MAX_TOKENS, Math.max(MIN_TOKENS, Math.ceil(estimated)));
    }
}
package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer;

import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.AbstractPromptTexts;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.FirstLongPromptTexts;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.SecondLongPromptTexts;
import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq.GroqClient;
import cz.cvut.kbss.textanalysis.lemmatizer.LemmatizerApi;
import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class LLMService implements LemmatizerApi {
    private final PromptGenerator promptGenerator = new PromptGenerator(new FirstLongPromptTexts());

    //private final GeminiClient geminiClient = new GeminiClient(System.getenv("GEMINI_API_KEY"), "gemini-3.1-pro-preview");
    private final GroqClient groqClient = new GroqClient(System.getenv("GROQ_API_KEY"), "openai/gpt-oss-20b");
    //private final ClaudeClient claudeClient = new ClaudeClient(System.getenv("CLAUDE_API_KEY"), "claude-sonnet-5");
    //private final OpenAIClient openAIClient = new OpenAIClient(System.getenv("GPT_API_KEY"), "gpt-5.2");

    @Override
    public LemmatizerResult process(String text, String lang) {
        String[] paragraphs = text.split("\n\n\n\n");
        final List<List<SingleLemmaResult>> results = new ArrayList<>();

        for (final String paragraph : paragraphs) {
            promptGenerator.setPromptTexts(new FirstLongPromptTexts());
            groqClient.setModel("openai/gpt-oss-20b");
            final List<String> paragraphResults = new ArrayList<>();

            for (final String sentence : extractSentence(paragraph)) {
                final String prompt = promptGenerator.promt(sentence);
                final String response = groqClient.send(prompt);
                paragraphResults.addAll(responseWordParser(response));
            }

            promptGenerator.setPromptTexts(new SecondLongPromptTexts());
            groqClient.setModel("openai/gpt-oss-120b");

            final String prompt = promptGenerator.promt(wordResultObserver(paragraphResults), paragraph);
            final String response = groqClient.send(prompt);
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
        return result.toString();
    }

    private String[] extractSentence(String paragraph) {
        return Arrays.stream(paragraph.split("\\."))
                .filter(str -> str.length() > 1)
                .toArray(String[]::new);
    }
}

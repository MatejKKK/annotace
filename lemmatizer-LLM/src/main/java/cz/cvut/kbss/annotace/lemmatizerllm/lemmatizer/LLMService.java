package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMConf;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.AbstractPromptTexts;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.Language;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.LongPromptTexts;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.PromptGenerator;
import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq.GroqClient;
import cz.cvut.kbss.textanalysis.lemmatizer.LemmatizerApi;
import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import lombok.Setter;
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


    public LLMService(LLMConf conf) {
        this.conf = conf;
        this.singleLemmaResultFactory = new SingleLemmaResultFactory(conf.delimiter());
    }

    @Setter
    private LLMConf conf;

    private final PromptGenerator promptGenerator = new PromptGenerator(new LongPromptTexts());
    private SingleLemmaResultFactory singleLemmaResultFactory;
    private Language language = Language.EN;

    private final GroqClient groqClient = new GroqClient(
            new String[]{System.getenv("GROQ_API_KEY"), System.getenv("GROQ_API_KEY_2")}, "openai/gpt-oss-20b"
    );

    @Override
    public LemmatizerResult process(String text, String lang) {
        setLanguage(lang);

        String[] paragraphs = text.split("\n\n\n\n");
        final List<List<SingleLemmaResult>> results = new ArrayList<>();

        for (final String paragraph : paragraphs) {
            promptGenerator.setPromptTexts(new LongPromptTexts());
            groqClient.setModel("openai/gpt-oss-20b");
            final List<String> paragraphResults = new ArrayList<>();

            final List<String> singleSentences = extractSentence(paragraph);
            final String[] sentences = mergeSentences(singleSentences, conf.maxSentenceLength());

            for (int i = 0; i < sentences.length; i++) {
                if (i > 0) {
                    try {
                        Thread.sleep(500);
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
            results.add(responseLemmaParser(response, paragraph));
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

    private void setLanguage(String shortcut) {
        this.language = Language.valueOf(shortcut.toUpperCase());
        promptGenerator.setLanguage(this.language);
    }

    private List<SingleLemmaResult> responseLemmaParser(String response, String paragraph) {
        List<SingleLemmaResult> result = new ArrayList<>();
        final String[] lines = Arrays.stream(response.split("\n")).filter(s -> s.contains(conf.delimiter())).toArray(String[]::new);
        for (String line : lines) {
            if (line.isBlank()) continue;

            result.add(singleLemmaResultFactory.createSingleLemmaResult(line));
        }

        return SingleLemmaResultFactory.addSpacesToLemma(result, paragraph);
    }

    private List<String> responseWordParser(String response) {
        List<String> result = new ArrayList<>();
        final String[] lines = Arrays.stream(response.split("\n")).filter(
                s -> s.chars().filter(ch -> ch == conf.delimiter().toCharArray()[0]).count() == 1  //todo conf.delimiter().toCharArray()[0]
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

    private int getMaxTokens(int paragraphLength) {
        final double estimated = BASE_TOKENS + TOKENS_PER_WORD * Math.pow(paragraphLength / 5., GROWTH_EXPONENT);
        return (int) Math.min(MAX_TOKENS, Math.max(MIN_TOKENS, Math.ceil(estimated)));
    }
}
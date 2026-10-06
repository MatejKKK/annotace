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
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.TextExtractor.*;

@Slf4j
public class LLMService implements LemmatizerApi {

    private static final int BASE_TOKENS = 2048;
    private static final double TOKENS_PER_WORD = 5.5;
    private static final double GROWTH_EXPONENT = 1.12;
    private static final int MIN_TOKENS = 2048;
    private static final int MAX_TOKENS = 8192;
    private LLMConf conf;
    private final StringParser stringParser;
    private final PromptGenerator promptGenerator = new PromptGenerator(new LongPromptTexts());
    private final SingleLemmaResultFactory singleLemmaResultFactory;
    private Language language = Language.EN;

    private final GroqClient groqClient = new GroqClient(
            new String[]{System.getenv("GROQ_API_KEY"), System.getenv("GROQ_API_KEY_2")}, "openai/gpt-oss-20b"
    );

    public LLMService(LLMConf conf) {
        this.conf = conf;
        this.singleLemmaResultFactory = new SingleLemmaResultFactory(conf.getDelimiter());
        this.stringParser = new StringParser(conf.getDelimiter());
    }

    public void setConf(LLMConf conf) {
        this.conf = conf;
        this.stringParser.setDelimiter(conf.getDelimiter());
    }

    private void setLanguage(String shortcut) {
        this.language = Language.valueOf(shortcut.toUpperCase());
        promptGenerator.setLanguage(this.language);
    }

    @Override
    public LemmatizerResult process(String text, String lang) {
        setLanguage(lang);

        String[] paragraphs = mergeParagraphs(extractParagraphs(text), conf.getMaxParagraphLength() * 2);
        final List<List<SingleLemmaResult>> results = new ArrayList<>();

        for (final String paragraph : paragraphs) {
            promptGenerator.setPromptTexts(new LongPromptTexts());
            groqClient.setModel("openai/gpt-oss-20b");
            final List<String> paragraphResults = new ArrayList<>();

            final String[] sentences = mergeSentences(extractSentences(paragraph), conf.getMaxSentenceLength());

            for (int i = 0; i < sentences.length; i++) {
                if (i > 0) {
                    try {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException ignored) {}
                }
                final String prompt = promptGenerator.prompt(sentences[i]);
                final String response = groqClient.send(prompt, getMaxTokens(sentences[i].length()));
                paragraphResults.addAll(stringParser.responseWordParser(response));
            }
            groqClient.setModel("openai/gpt-oss-120b");

            final String prompt = promptGenerator.prompt(stringParser.wordResultObserver(paragraphResults), paragraph);
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

    private List<SingleLemmaResult> responseLemmaParser(String response, String paragraph) {
        List<SingleLemmaResult> result = new ArrayList<>();
        final String[] paragraphs = response.split("\n\n");
        for (final String paragraphResponse : paragraphs) {
            final String[] lines = Arrays.stream(paragraphResponse.split("\n"))
                    .filter(s -> s.contains(conf.getDelimiter()))
                    .toArray(String[]::new);
            for (final String line : lines) {
                if (line.isBlank()) continue;

                result.add(singleLemmaResultFactory.createSingleLemmaResult(line));
            }
        }

        return SingleLemmaResultFactory.addSpacesToLemma(result, paragraph);
    }

    private int getMaxTokens(int paragraphLength) {
        final double estimated = BASE_TOKENS + TOKENS_PER_WORD * Math.pow(paragraphLength / 5., GROWTH_EXPONENT);
        return (int) Math.min(MAX_TOKENS, Math.max(MIN_TOKENS, Math.ceil(estimated)));
    }
}
package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMConf;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.factory.PostprocessedSingleLemmaResultFactory;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.factory.SimpleSingleLemmaResultFactory;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.factory.SingleLemmaResultFactory;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.AbstractPromptTexts;
import cz.cvut.kbss.annotace.lemmatizerllm.model.Language;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.LongPromptTexts;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.PromptGenerator;
import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq.GroqClient;
import cz.cvut.kbss.annotace.lemmatizerllm.text_service.StringParser;
import cz.cvut.kbss.textanalysis.lemmatizer.LemmatizerApi;
import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.factory.SimpleSingleLemmaResultFactory.*;
import static cz.cvut.kbss.annotace.lemmatizerllm.text_service.TextExtractor.*;
import static cz.cvut.kbss.annotace.lemmatizerllm.text_service.TextPreprocessor.removeBorderSpacesFromText;
import static java.util.Arrays.*;

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
    private SingleLemmaResultFactory singleLemmaResultFactory;
    private Language language = Language.EN;

    private final GroqClient groqClient = new GroqClient(
            new String[]{System.getenv("GROQ_API_KEY"), System.getenv("GROQ_API_KEY_2")}, "openai/gpt-oss-20b"
    );

    public LLMService(LLMConf conf) {
        this.conf = conf;
        this.stringParser = new StringParser(conf.delimiter());

        this.singleLemmaResultFactory = conf.usePostProcessing() ?
                new PostprocessedSingleLemmaResultFactory(conf.delimiter(), language) :
                new SimpleSingleLemmaResultFactory(conf.delimiter());
    }

    public void setConf(LLMConf conf) {
        this.conf = conf;
        this.stringParser.setDelimiter(conf.delimiter());
        
        this.singleLemmaResultFactory = conf.usePostProcessing() ?
                new PostprocessedSingleLemmaResultFactory(conf.delimiter(), language) : 
                new SimpleSingleLemmaResultFactory(conf.delimiter());
    }

    private void setLanguage(String shortcut) {
        this.language = Language.valueOf(shortcut.toUpperCase());
        this.promptGenerator.setLanguage(this.language);
        this.singleLemmaResultFactory.setLanguage(this.language);
    }

    @Override
    public LemmatizerResult process(String text, String lang) {
        setLanguage(lang);

        final String[] paragraphs = mergeParagraphs(
                extractParagraphs(removeBorderSpacesFromText(text)),
                conf.maxParagraphLength() * 2
        );
        final List<List<SingleLemmaResult>> results = new ArrayList<>();

        for (final String paragraph : paragraphs) {
            promptGenerator.setPromptTexts(new LongPromptTexts());
            groqClient.setModel("openai/gpt-oss-20b");
            final List<String> paragraphResults = new ArrayList<>();

            final String[] sentences = mergeSentences(extractSentences(paragraph), conf.maxSentenceLength());

            for (int i = 0; i < sentences.length; ++i) {
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
            results.addAll(processParagraph(paragraphResults, paragraph));
        }


        final LemmatizerResult result = new LemmatizerResult();
        result.setResult(results);
        result.setLemmatizer(this.getClass().getName());
        return result;
    }

    public LemmatizerResult process(String text, String lang, boolean processParagraph, boolean postProcessTable) {
        setLanguage(lang);

        final String[] paragraphs = mergeParagraphs(
                extractParagraphs(removeBorderSpacesFromText(text)),
                conf.maxParagraphLength() * 2
        );
        final List<List<SingleLemmaResult>> results = new ArrayList<>();

        for (final String paragraph : paragraphs) {
            promptGenerator.setPromptTexts(new LongPromptTexts());
            groqClient.setModel("openai/gpt-oss-20b");
            final List<String> paragraphResults = new ArrayList<>();

            final String[] sentences = mergeSentences(extractSentences(paragraph), conf.maxSentenceLength());

            for (int i = 0; i < sentences.length; ++i) {
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
            if (processParagraph) {
                results.addAll(processParagraph(paragraphResults, paragraph));
            }
            else {
                StringBuilder builder = new StringBuilder();
                paragraphResults.forEach(str -> builder.append(str.concat("\n")));
                results.addAll(responseLemmaParser(builder.toString(), paragraph));
            }
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

    private List<List<SingleLemmaResult>> processParagraph(List<String> paragraphResults, String paragraph) {
        groqClient.setModel("openai/gpt-oss-120b");
        final String prompt = promptGenerator.prompt(stringParser.wordResultObserver(paragraphResults), paragraph);
        final String response = groqClient.send(prompt, getMaxTokens(paragraph.length()));
        return responseLemmaParser(response, paragraph);
    }

    private List<List<SingleLemmaResult>> responseLemmaParser(String response, String paragraph) {
        List<List<SingleLemmaResult>> results = new ArrayList<>();
        final String[] paragraphs = response.split(PARAGRAPH_MERGE_MARK_LINE);

        for (final String paragraphResponse : paragraphs) {
            final String[] lines = stream(paragraphResponse.split("\n"))
                    .filter(s -> s.contains(conf.delimiter()))
                    .toArray(String[]::new);

            List<SingleLemmaResult> result = new ArrayList<>();
            for (final String line : lines) {
                if (line.isBlank()) continue;
                result.addAll(addSpacesToLemmas(singleLemmaResultFactory.createSingleLemmaResult(line), paragraph));
            }
            results.add(result);
        }

        return results;
    }

    private int getMaxTokens(int paragraphLength) {
        final double estimated = BASE_TOKENS + TOKENS_PER_WORD * Math.pow(paragraphLength / 5., GROWTH_EXPONENT);
        return (int) Math.min(Math.min(MAX_TOKENS, Math.max(MIN_TOKENS, Math.ceil(estimated))), 8_000);
    }
}
package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMLemmatizerConf;
import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMLemmatizerTextConf;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.factory.PostprocessedLemmaResultFactory;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.factory.SimpleLemmaResultFactory;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.factory.LemmaResultFactory;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.AbstractPromptTexts;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.processor.ClaudeLemmatizerProcessor;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.processor.EInfraLemmatizerProcessor;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.processor.GroqLemmatizerProcessor;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.processor.LLMLemmatizerProcessor;
import cz.cvut.kbss.annotace.lemmatizerllm.model.Language;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.LongPromptTexts;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.PromptGenerator;
import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq.GroqClient;
import cz.cvut.kbss.annotace.lemmatizerllm.text_service.StringParser;
import cz.cvut.kbss.annotace.lemmatizerllm.text_service.TextDivider;
import cz.cvut.kbss.textanalysis.lemmatizer.LemmatizerApi;
import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.factory.SimpleLemmaResultFactory.*;
import static cz.cvut.kbss.annotace.lemmatizerllm.text_service.TextExtractor.*;
import static cz.cvut.kbss.annotace.lemmatizerllm.text_service.TextPreprocessor.removeBorderSpacesFromText;
import static java.util.Arrays.*;

@Slf4j
public class LLMLemmatizer implements LemmatizerApi {

    @Deprecated private static final int BASE_TOKENS = 2048;
    @Deprecated private static final double TOKENS_PER_WORD = 5.5;
    @Deprecated private static final double GROWTH_EXPONENT = 1.12;
    @Deprecated private static final int MIN_TOKENS = 2048;
    @Deprecated private static final int MAX_TOKENS = 8192;

    private LLMLemmatizerConf conf;
    private final StringParser stringParser;
    private final PromptGenerator promptGenerator = new PromptGenerator(new LongPromptTexts());
    private LemmaResultFactory lemmaResultFactory;
    private LLMLemmatizerProcessor processor;
    private Language language = Language.EN;

    private final GroqClient groqClient = new GroqClient(
            new String[]{System.getenv("GROQ_API_KEY"), System.getenv("GROQ_API_KEY_2")}, "openai/gpt-oss-20b"
    );

    public LLMLemmatizer(LLMLemmatizerTextConf conf) {
        this.conf = conf.toLLMConf();
        this.stringParser = new StringParser(this.conf.delimiter());

        this.lemmaResultFactory = this.conf.usePostProcessing() ?
                new PostprocessedLemmaResultFactory(this.conf.delimiter(), this.conf.selectOneCharWords(), language) :
                new SimpleLemmaResultFactory(this.conf.delimiter());

        this.processor = switch (this.conf.api()) {
            case GROQ -> new GroqLemmatizerProcessor(this.conf, language);
            case EINFRA_QWEN, EINFRA_KIMI, EINFRA_GLM -> new EInfraLemmatizerProcessor(this.conf, language);
            case CLAUDE_SONNET -> new ClaudeLemmatizerProcessor(this.conf, language);
            default -> throw new RuntimeException("Unknown api type");
        };
    }

    public void setConf(LLMLemmatizerConf conf) {
        this.conf = conf;
        this.stringParser.setDelimiter(conf.delimiter());
        
        this.lemmaResultFactory = conf.usePostProcessing() ?
                new PostprocessedLemmaResultFactory(conf.delimiter(), conf.selectOneCharWords(), language) :
                new SimpleLemmaResultFactory(conf.delimiter());

        this.processor = switch (conf.api()) {
            case GROQ -> new GroqLemmatizerProcessor(conf, language);
            case EINFRA_QWEN, EINFRA_KIMI, EINFRA_GLM -> new EInfraLemmatizerProcessor(conf, language);
            case CLAUDE -> new ClaudeLemmatizerProcessor(conf, language);
            default -> throw new RuntimeException("Unknown api type");
        };
    }

    private void setLanguage(String shortcut) {
        this.language = Language.valueOf(shortcut.toUpperCase());
        this.promptGenerator.setLanguage(this.language);
        this.lemmaResultFactory.setLanguage(this.language);
        this.processor.setLanguage(this.language);
    }

    @Override
    public LemmatizerResult process(String text, String lang) {
        setLanguage(lang);

        final List<List<SingleLemmaResult>> results;

        switch (determineThreadMode(text)) {
            case SINGLE_THREAD -> {
                final Random random = new SecureRandom();
                processor.setKey(conf.apiKeys()[random.nextInt(0, conf.apiKeys().length)]);
                results = processor.process(text, lang);
            }
            case FEW_THREADS -> {
                List<Thread> proxies = launchProcessorProxiesSingleKeyPerProxy(text, lang).stream()
                        .map(it -> {
                            Thread t = new Thread(it);
                            t.setPriority(it.getPriority());
                            return t;
                        }).toList();
                proxies.forEach(Thread::start);
                for (final Thread t : proxies) {
                    try {
                        t.join();
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                results = ResultsStore.getResults();
            }
            default -> throw new RuntimeException("Unsupported thread mode yet");
        }


        final LemmatizerResult result = new LemmatizerResult();
        result.setResult(results);
        result.setLemmatizer(this.getClass().getName());
        return result;
    }

    @Deprecated
    public LemmatizerResult process(String text, String lang, boolean processParagraph, boolean postProcessTable) {
        setLanguage(lang);

        final String[] paragraphs = mergeParagraphs(
                extractParagraphs(removeBorderSpacesFromText(text)),
                conf.maxParagraphLength()
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

    @Override
    public boolean processAllTogether(String text) {
        return !determineThreadMode(text).equals(ThreadMode.SINGLE_THREAD);
    }

    private enum ThreadMode {
        SINGLE_THREAD, FEW_THREADS, MANY_THREADS
    }

    private ThreadMode determineThreadMode(String text) {
        int countOfPar = text.split(PARAGRAPH_BREAK).length;
        return switch (conf.api()) {
            case GROQ, EINFRA_QWEN, EINFRA_KIMI, EINFRA_GLM, CLAUDE_SONNET -> {
                boolean longText = countOfPar > 1 && text.length() > 1_000;
                if (conf.apiKeys().length < 2 || !longText) {
                    yield ThreadMode.SINGLE_THREAD;
                }
                if (conf.apiKeys().length < 17) {
                    yield ThreadMode.FEW_THREADS;
                }
                yield countOfPar > 8 && text.length() > 3_000 ?
                        ThreadMode.MANY_THREADS : ThreadMode.FEW_THREADS;
            }
            default -> throw new RuntimeException("Unsupported api type yet");
        };
    }

    private List<LLMLemmatizerProcessor> launchProcessorProxiesSingleKeyPerProxy(String text, String lang) {
        final String[] paragraphs = text.split(PARAGRAPH_BREAK);
        final String[] keys = conf.apiKeys();
        final int threadsCount = (int) Math.min(Math.min(paragraphs.length, (double) keys.length), 8.);

        final int keysPerThread = 1;
        int keyRemainder = 0;
        int keyCurrIndex = keysPerThread;

        final Iterator<String> dividedTexts = TextDivider.merge(paragraphs, threadsCount);

        this.processor.setIndex(0);
        //this.processorProxy.setPriority(paragraphCurrIndex == paragraphsPerThread ? 5 : 7);

        List<LLMLemmatizerProcessor> proxies = new  ArrayList<>(List.of(
                this.processor.setTextAndGet(
                        dividedTexts.next(),
                        lang,
                        Arrays.stream(keys).limit(keyCurrIndex))
        ));

        for (int i = 1; i < threadsCount; ++i) {
            final boolean keyRemainded = keyRemainder-- > 0;
            final int keyDiff = keysPerThread + (keyRemainded ? 1 : 0);

            //final boolean paragraphRemainded = paragraphRemainder-- > 0;
            //final int paragraphDiff = paragraphsPerThread + (paragraphRemainded ? 1 : 0);

            proxies.add(
                    new GroqLemmatizerProcessor(conf, language, i)
                            .setTextAndGet(
                                    dividedTexts.next(),
                                    lang,
                                    Arrays.stream(keys)
                                            .skip(keyCurrIndex)
                                            .limit(keyDiff)
                            )
            );

            keyCurrIndex += keyDiff;
            //paragraphCurrIndex += paragraphDiff;
        }
        return proxies;
    }

    @Deprecated
    private List<LLMLemmatizerProcessor> launchProcessorProxiesMultiKeysPerProxy(String text, String lang) {
        final String[] paragraphs = text.split(PARAGRAPH_BREAK);
        final String[] keys = conf.apiKeys();
        final int threadsCount = (int) Math.min(Math.min(paragraphs.length, Math.ceil(keys.length / 2.)), 8.);

        final int keysPerThread = (int) Math.ceil((double) keys.length / threadsCount);
        int keyRemainder = (keys.length % threadsCount) - 1;
        int keyCurrIndex = keyRemainder > -1 ? keysPerThread : keysPerThread + 1;

        final int paragraphsPerThread = (int) Math.ceil((double) paragraphs.length / threadsCount);
        int paragraphRemainder = (paragraphs.length % threadsCount) - 1;
        int paragraphCurrIndex = paragraphRemainder > -1 ? paragraphsPerThread : paragraphsPerThread + 1;

        this.processor.setIndex(0);
        this.processor.setPriority(paragraphCurrIndex == paragraphsPerThread ? 5 : 7);

        List<LLMLemmatizerProcessor> proxies = new  ArrayList<>(List.of(
                this.processor.setTextAndGet(
                        Arrays.stream(paragraphs)
                                .limit(paragraphCurrIndex)
                                .map(it -> it.concat(PARAGRAPH_BREAK))
                                .collect(Collectors.joining()),
                        lang,
                        Arrays.stream(keys).limit(keyCurrIndex))
        ));

        for (int i = 1; i < threadsCount; ++i) {
            final boolean keyRemainded = keyRemainder-- > 0;
            final int keyDiff = keysPerThread + (keyRemainded ? 1 : 0);

            final boolean paragraphRemainded = paragraphRemainder-- > 0;
            final int paragraphDiff = paragraphsPerThread + (paragraphRemainded ? 1 : 0);

            proxies.add(
                    new GroqLemmatizerProcessor(conf, language, paragraphRemainded, keyRemainded, i)
                    .setTextAndGet(
                            Arrays.stream(paragraphs)
                                    .skip(paragraphCurrIndex)
                                    .limit(paragraphDiff)
                                    .map(it -> it.concat(PARAGRAPH_BREAK))
                                    .collect(Collectors.joining()),
                            lang,
                            Arrays.stream(keys)
                                    .skip(keyCurrIndex)
                                    .limit(keyDiff)
                    )
            );

            keyCurrIndex += keyDiff;
            paragraphCurrIndex += paragraphDiff;
        }
        return proxies;
    }

    @Deprecated
    private List<List<SingleLemmaResult>> processParagraph(List<String> paragraphResults, String paragraph) {
        groqClient.setModel("openai/gpt-oss-120b");
        final String prompt = promptGenerator.prompt(stringParser.wordResultObserver(paragraphResults), paragraph);
        final String response = groqClient.send(prompt, getMaxTokens(paragraph.length()));
        return responseLemmaParser(response, paragraph);
    }

    @Deprecated
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
                result.addAll(addSpacesToLemmas(lemmaResultFactory.createSingleLemmaResult(line), paragraph));
            }
            results.add(result);
        }

        return results;
    }

    @Deprecated
    private int getMaxTokens(int paragraphLength) {
        final double estimated = BASE_TOKENS + TOKENS_PER_WORD * Math.pow(paragraphLength / 5., GROWTH_EXPONENT);
        return (int) Math.min(Math.min(MAX_TOKENS, Math.max(MIN_TOKENS, Math.ceil(estimated))), 8_000);
    }
}
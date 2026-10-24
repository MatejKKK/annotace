package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.proxy;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMConf;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.ResultsStore;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.factory.LemmaResultFactory;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.factory.PostprocessedLemmaResultFactory;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.factory.SimpleLemmaResultFactory;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.LongPromptTexts;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.PromptGenerator;
import cz.cvut.kbss.annotace.lemmatizerllm.model.Language;
import cz.cvut.kbss.annotace.lemmatizerllm.text_service.StringParser;
import cz.cvut.kbss.textanalysis.exception.UnsupportedLanguageException;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static cz.cvut.kbss.annotace.lemmatizerllm.text_service.TextExtractor.PARAGRAPH_MERGE_MARK;

public abstract class ProcessorProxy implements Runnable {

    protected static final int BASE_TOKENS = 2048;
    protected static final double TOKENS_PER_WORD = 5.5;
    protected static final double GROWTH_EXPONENT = 1.12;
    protected static final int MIN_TOKENS = 2048;
    protected static final int MAX_TOKENS = 8192;
    protected static final int MAX_RETRIES = 7;
    protected static final double RETRY_TOKEN_MULTIPLIER = 1.75;

    protected final LLMConf conf;
    protected final StringParser stringParser;
    protected final PromptGenerator promptGenerator = new PromptGenerator(new LongPromptTexts());
    protected final LemmaResultFactory lemmaResultFactory;
    @Setter
    @Getter
    protected int priority = 5;
    @Setter
    protected int index;
    protected String text;
    protected final AtomicBoolean locked = new AtomicBoolean(false);

    protected ProcessorProxy(LLMConf conf, Language language) {
        this.conf = conf;
        this.stringParser = new StringParser(conf.delimiter());
        this.lemmaResultFactory = conf.usePostProcessing() ?
                new PostprocessedLemmaResultFactory(conf.delimiter(), conf.selectOneCharWords(), language) :
                new SimpleLemmaResultFactory(conf.delimiter());
    }

    @Override
    final public void run() {
        try {
            List<List<SingleLemmaResult>> results = process();
            int diff = 0;
            do {
                try {
                    Thread.sleep(Math.min(diff == 1 ? 50L : diff * 250L, 5_000L));
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                diff = ResultsStore.addResults(results, index);
            } while (diff != 0);
        }
        finally {
            locked.set(false);
        }
    }

    public abstract ProcessorProxy setTextAndGet(String text, String lang, Stream<String> keys);

    abstract List<List<SingleLemmaResult>> process();

    final public List<List<SingleLemmaResult>> process(String text, String lang) {
        synchronized (locked) {
            if (locked.get()) throw new RuntimeException("Cannot process text");
            try {
                setLanguage(Language.valueOf(lang.toUpperCase()));
            }
            catch (IllegalArgumentException e) {
                throw new UnsupportedLanguageException(e.getMessage());
            }
            locked.set(true);
        }
        this.text = text;
        try {
            return process();
        }
        finally {
            locked.set(false);
        }
    }

    public void setLanguage(Language language) {
        if (this.locked.get()) throw new RuntimeException("Cannot process text");
        this.promptGenerator.setLanguage(language);
        this.lemmaResultFactory.setLanguage(language);
    }

    protected List<List<SingleLemmaResult>> responseLemmaParser(String response) {
        List<List<SingleLemmaResult>> results = new ArrayList<>();
        List<SingleLemmaResult> result = new ArrayList<>();
        final String[] lines = response.split("\n");

        for (final String line : lines) {
            if (line.contains(PARAGRAPH_MERGE_MARK)) {
                if (!result.isEmpty()) {
                    results.add(lemmaResultFactory.addSpacesToLemmas(result));
                    result = new ArrayList<>();
                }
                continue;
            }
            if (line.isBlank() || line.length() < 3 || !line.contains(conf.delimiter())) {
                continue;
            }
            result.addAll(lemmaResultFactory.createSingleLemmaResult(line));
        }
        if (!result.isEmpty()) {
            if (result.stream().map(it -> it.getLemma().length()).reduce(0, Integer::sum) > 2) {
                results.add(lemmaResultFactory.addSpacesToLemmas(result));
            }
            else if (results.isEmpty()) {
                results.add(lemmaResultFactory.addSpacesToLemmas(result));
            }
            else {
                result = lemmaResultFactory.addSpacesToLemmas(result);
                result.addAll(0, results.removeLast());
                results.add(result);
            }
        }

        return results;
    }
}

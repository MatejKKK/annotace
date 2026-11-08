package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.processor;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMLemmatizerConf;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.LongPromptTexts;
import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq.GroqClient;
import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq.GroqProxy;
import cz.cvut.kbss.annotace.lemmatizerllm.model.Language;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static cz.cvut.kbss.annotace.lemmatizerllm.text_service.TextExtractor.*;
import static cz.cvut.kbss.annotace.lemmatizerllm.text_service.TextPreprocessor.removeBorderSpacesFromText;
import static java.util.Arrays.stream;

public class GroqLemmatizerProcessor extends LLMLemmatizerProcessor {

    private GroqProxy groqProxy;

    public GroqLemmatizerProcessor(LLMLemmatizerConf conf, Language language) {
        super(conf, language);
        final String[] keys = stream(conf.apiKeys()).map(System::getenv).toArray(String[]::new);
        groqProxy = new GroqProxy(new GroqClient(keys, "openai/gpt-oss-20b"));
    }

    @Deprecated
    public GroqLemmatizerProcessor(LLMLemmatizerConf conf, Language language, boolean parRem, boolean keyRem, int index) {
        this(conf, language);
        if (keyRem) this.priority = parRem ? 6 : 4;
        else this.priority = parRem ? 8 : 5;
        this.index = index;
    }

    public GroqLemmatizerProcessor(LLMLemmatizerConf conf, Language language, int index) {
        this(conf, language);
        this.index = index;
    }

    @Override
    public LLMLemmatizerProcessor setTextAndGet(String text, String lang, Stream<String> keys) {
        synchronized (locked) {
            if (locked.get()) throw new RuntimeException("Cannot process text");
            setLanguage(Language.valueOf(lang.toUpperCase()));
            locked.set(true);
        }
        this.text = text;
        System.out.println("text for proxy: " + text + "\n");
        groqProxy = new GroqProxy(new GroqClient(keys.map(System::getenv).toArray(String[]::new), "openai/gpt-oss-20b"));
        return this;
    }

    @Override
    public void setKey(String key) {
        if (locked.get()) throw new RuntimeException("Cannot process text");
        groqProxy.setGroqClient(new GroqClient(new String[]{System.getenv(key)}, "openai/gpt-oss-20b"));
    }

    @Override
    void process() {
        final String[] paragraphs = stream(mergeParagraphs(
                extractParagraphs(removeBorderSpacesFromText(text)),
                conf.maxParagraphLength()
        )).map(it -> it + PARAGRAPH_MERGE_MARK).toArray(String[]::new);

        for (final String paragraph : paragraphs) {
            promptGenerator.setPromptTexts(new LongPromptTexts());
            groqProxy.setModel("openai/gpt-oss-20b");
            lemmaResultFactory.setParagraph(paragraph);
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
                String response = groqProxy.send(prompt, getMaxTokens(sentences[i].length()));
                System.out.println("Thread and response: " + index + " :: " + response);
                paragraphResults.addAll(stringParser.responseWordParser(response));
            }
            results.addAll(processParagraph(paragraphResults, paragraph));
        }

        locked.set(false);
    }

    private List<List<SingleLemmaResult>> processParagraph(List<String> paragraphResults, String paragraph) {
        groqProxy.setModel("openai/gpt-oss-120b");
        String response;
        final String prompt = promptGenerator.prompt(stringParser.wordResultObserver(paragraphResults), paragraph);
        response = groqProxy.send(prompt, getMaxTokens(paragraph.length()));
        return responseLemmaParser(response);
    }

    private int getMaxTokens(int paragraphLength) {
        final double estimated = BASE_TOKENS + TOKENS_PER_WORD * Math.pow(paragraphLength / 5., GROWTH_EXPONENT);
        return (int) Math.min(Math.min(MAX_TOKENS, Math.max(MIN_TOKENS, Math.ceil(estimated))), 8_000);
    }
}

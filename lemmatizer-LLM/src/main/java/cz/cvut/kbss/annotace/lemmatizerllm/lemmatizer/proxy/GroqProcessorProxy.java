package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.proxy;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMConf;
import cz.cvut.kbss.annotace.lemmatizerllm.exception.TruncatedResponseException;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.LongPromptTexts;
import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq.GroqClient;
import cz.cvut.kbss.annotace.lemmatizerllm.model.Language;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static cz.cvut.kbss.annotace.lemmatizerllm.text_service.TextExtractor.*;
import static cz.cvut.kbss.annotace.lemmatizerllm.text_service.TextPreprocessor.removeBorderSpacesFromText;
import static java.util.Arrays.stream;

public class GroqProcessorProxy extends ProcessorProxy {

    private GroqClient groqClient;

    public GroqProcessorProxy(LLMConf conf, Language language) {
        super(conf, language);
        final String[] keys = stream(conf.apiKeys()).map(System::getenv).toArray(String[]::new);
        groqClient = new GroqClient(keys, "openai/gpt-oss-20b");
    }

    public GroqProcessorProxy(LLMConf conf, Language language, boolean parRem, boolean keyRem, int index) {
        this(conf, language);
        if (keyRem) this.priority = parRem ? 6 : 4;
        else this.priority = parRem ? 8 : 5;
        this.index = index;
    }

    @Override
    public ProcessorProxy setTextAndGet(String text, String lang, Stream<String> keys) {
        if (locked.get()) throw new RuntimeException("Cannot process text");
        locked.set(true);
        setLanguage(Language.valueOf(lang.toUpperCase()));
        this.text = text;
        groqClient = new GroqClient(keys.map(System::getenv).toArray(String[]::new), "openai/gpt-oss-20b");
        return this;
    }

    @Override
    List<List<SingleLemmaResult>> process() {
        System.out.println("Starting Groq process on thread " + index);
        final String[] paragraphs = stream(mergeParagraphs(
                extractParagraphs(removeBorderSpacesFromText(text)),
                conf.maxParagraphLength()
        )).map(it -> it + PARAGRAPH_MERGE_MARK).toArray(String[]::new);
        final List<List<SingleLemmaResult>> results = new ArrayList<>();

        for (final String paragraph : paragraphs) {
            promptGenerator.setPromptTexts(new LongPromptTexts());
            groqClient.setModel("openai/gpt-oss-20b");
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
                String response = "";
                try {
                    response = send(prompt, getMaxTokens(sentences[i].length()), i);
                }
                catch (RuntimeException ignored) {

                }
                paragraphResults.addAll(stringParser.responseWordParser(response));
            }
            results.addAll(processParagraph(paragraphResults, paragraph));
        }

        return results;
    }

    @NonNull
    private String send(String prompt, int maxTokens, int orderInTask) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty");
        }

        int currentMaxTokens = maxTokens;
        if (orderInTask % 3 == 0) groqClient.resetMemory();

        for (int attempt = 0; attempt <= MAX_RETRIES; ++attempt) {
            try {
                return this.groqClient.sendWithMemory(prompt, maxTokens);
            }
            catch (TruncatedResponseException e) {
                if (attempt >= MAX_RETRIES) {
                    throw new RuntimeException("Error sending request: " + e.getMessage()
                            + " (gave up after %d retries, last max_tokens=%d)"
                            .formatted(attempt, currentMaxTokens), e);
                }
                currentMaxTokens = Math.min(4096,
                        (int) Math.ceil(currentMaxTokens * RETRY_TOKEN_MULTIPLIER));
                if (attempt > MAX_RETRIES - 2) {
                    currentMaxTokens = 4096;
                }

                System.out.println("Truncated response (finish_reason=length), retrying with max_tokens="
                        + currentMaxTokens);
            }
            catch (RuntimeException e) {
                if (attempt >= MAX_RETRIES) {
                    throw new RuntimeException("Error sending request: " + e.getMessage()
                            + " (gave up after %d retries, last max_tokens=%d)"
                            .formatted(attempt, currentMaxTokens), e);
                }
                if (!(e.getMessage() == null || e.getMessage().isEmpty())) {
                    if (e.getMessage().contains("Rate limit reached for model") && e.getMessage().contains("tokens per minute")) {
                        final int startIndex = e.getMessage().indexOf("Please try again in ");
                        if (startIndex == -1) {
                            throw new RuntimeException("Error sending request: " + e.getMessage(), e);
                        }
                        final int endIndex = e.getMessage().lastIndexOf(". Need more tokens?");
                        String time = e.getMessage().substring(startIndex + 20, endIndex);

                        System.out.println("It is necessary to wait for tokens per minute for " + time + ".");

                        final boolean ms = time.contains("ms");
                        while (time.endsWith("s")) {
                            time = time.replace("s", "");
                        }
                        while (time.endsWith("m")) {
                            time = time.replace("m", "");
                        }

                        try {
                            Thread.sleep(Math.min((long) Math.floor(1.0 + Double.parseDouble(time) * (ms ? 1 : 1000)), 60_001L));
                        } catch (InterruptedException ignore) {
                        }
                    }
                    else if (e.getMessage().contains("Empty content in response (finish_reason=stop).")) {
                        ++attempt;
                    }
                    else throw new RuntimeException("Error sending request: " + e.getMessage(), e);
                }
                else ++attempt;
            }
            catch (Exception e) {
                throw new RuntimeException("Error sending request: " + e.getMessage(), e);
            }
        }
        throw new RuntimeException("Error sending request after " + MAX_RETRIES + " retries.");
    }

    private List<List<SingleLemmaResult>> processParagraph(List<String> paragraphResults, String paragraph) {
        groqClient.setModel("openai/gpt-oss-120b");
        String response;
        try {
            final String prompt = promptGenerator.prompt(stringParser.wordResultObserver(paragraphResults), paragraph);
            response = send(prompt, getMaxTokens(paragraph.length()), 0);
        }
        catch (RuntimeException e) {
            response = paragraphResults.stream()
                    .sorted()
                    .map(it -> it.concat("\n"))
                    .reduce("", (a, b) -> a.concat("\n").concat(b));
        }
        return responseLemmaParser(response);
    }

//    private List<List<SingleLemmaResult>> responseLemmaParser(String response, String paragraph) {
//        List<List<SingleLemmaResult>> results = new ArrayList<>();
//        final String[] paragraphs = response.split(PARAGRAPH_MERGE_MARK_LINE);
//
//        for (final String paragraphResponse : paragraphs) {
//            final String[] lines = stream(paragraphResponse.split("\n"))
//                    .filter(s -> s.contains(conf.delimiter()))
//                    .toArray(String[]::new);
//
//            List<SingleLemmaResult> result = new ArrayList<>();
//            for (final String line : lines) {
//                if (line.isBlank()) continue;
//                result.addAll(addSpacesToLemmas(lemmaResultFactory.createSingleLemmaResult(line), paragraph));
//            }
//            results.add(result);
//        }
//
//        return results;
//    }

    private int getMaxTokens(int paragraphLength) {
        final double estimated = BASE_TOKENS + TOKENS_PER_WORD * Math.pow(paragraphLength / 5., GROWTH_EXPONENT);
        return (int) Math.min(Math.min(MAX_TOKENS, Math.max(MIN_TOKENS, Math.ceil(estimated))), 8_000);
    }
}

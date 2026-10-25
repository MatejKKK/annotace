package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.proxy;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMConf;
import cz.cvut.kbss.annotace.lemmatizerllm.exception.TruncatedResponseException;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts.LongPromptTexts;
import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.einfra.EInfraClient;
import cz.cvut.kbss.annotace.lemmatizerllm.model.Language;
import cz.cvut.kbss.textanalysis.exception.UnsupportedLanguageException;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static cz.cvut.kbss.annotace.lemmatizerllm.text_service.TextExtractor.*;
import static cz.cvut.kbss.annotace.lemmatizerllm.text_service.TextPreprocessor.removeBorderSpacesFromText;
import static java.util.Arrays.stream;

/**
 * Same two-pass pipeline as ProccesorProxyCroq (per-sentence-group pass, then a
 * paragraph-level consolidation pass), against e-infra.cz's shared LLM API instead
 * of Groq. See EInfraClient's javadoc for how it differs from the Groq client.
 * Unlike the Groq proxy, this one does NOT parse a model-specific rate-limit message
 * out of error bodies - e-infra.cz documents no rate limits or a specific error format
 * for them, so a non-truncation failure just backs off generically and retries.
 * e-infra's catalog doesn't document a confirmed small/large two-tier pair the way
 * Groq's gpt-oss-20b/120b split does, so both passes default to the same model here;
 * swap in a cheaper first-pass model yourself once you've checked GET /v1/models.
 */
public class EInfraProccesorProxy extends ProcessorProxy {
    private static final long GENERIC_RETRY_BACKOFF_MS = 2000;

    private final String FIRST_PASS_MODEL;
    private final String SECOND_PASS_MODEL;

    private EInfraClient eInfraClient;

    public EInfraProccesorProxy(LLMConf conf, Language language) {
        super(conf, language);

        final String[] keys = stream(conf.apiKeys()).map(System::getenv).toArray(String[]::new);
        this.FIRST_PASS_MODEL = switch (conf.api()) {
            case EINFRA_QWEN -> "qwen3.8-27b";
            case EINFRA_KIMI -> "kimi";
            case EINFRA_GLM -> "glm";
            default -> throw new IllegalArgumentException("Invalid combination.");
        };

        this.SECOND_PASS_MODEL = FIRST_PASS_MODEL;

        eInfraClient = new EInfraClient(keys, FIRST_PASS_MODEL);
    }

    @Override
    public ProcessorProxy setTextAndGet(String text, String lang, Stream<String> keys) {
        if (locked.get()) throw new RuntimeException("Cannot process text");
        locked.set(true);
        setLanguage(Language.valueOf(lang.toUpperCase()));
        this.text = text;
        eInfraClient = new EInfraClient(keys.map(System::getenv).toArray(String[]::new), FIRST_PASS_MODEL);
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
            eInfraClient.setModel("openai/gpt-oss-20b");
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

        locked.set(false);
        return results;
    }

    private String send(String prompt, int maxTokens, int orderInTask) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty");
        }

        int currentMaxTokens = maxTokens;
        if (orderInTask % 3 == 0) eInfraClient.resetMemory();

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                return this.eInfraClient.send(prompt, currentMaxTokens);
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

                // e-infra documents no specific rate-limit error format to parse a
                // retry-after value out of (unlike Groq's "...tokens per minute..."
                // message handled in ProccesorProxyCroq), so just back off generically.
                final long backoffMs = GENERIC_RETRY_BACKOFF_MS * (attempt + 1);
                System.out.println("Request failed (" + e.getMessage() + "), retrying in "
                        + backoffMs + " ms.");
                try {
                    Thread.sleep(backoffMs);
                }
                catch (InterruptedException ignored) {}
            }
            catch (Exception e) {
                throw new RuntimeException("Error sending request: " + e.getMessage(), e);
            }
        }
        throw new RuntimeException("Error sending request after " + MAX_RETRIES + " retries.");
    }

    private List<List<SingleLemmaResult>> processParagraph(List<String> paragraphResults, String paragraph) {
        eInfraClient.setModel(SECOND_PASS_MODEL);
        final String prompt = promptGenerator.prompt(stringParser.wordResultObserver(paragraphResults), paragraph);
        final String response = eInfraClient.send(prompt, getMaxTokens(paragraph.length()));
        return responseLemmaParser(response);
    }

    private int getMaxTokens(int paragraphLength) {
        final double estimated = BASE_TOKENS + TOKENS_PER_WORD * Math.pow(paragraphLength / 5., GROWTH_EXPONENT);
        return (int) Math.min(Math.min(MAX_TOKENS, Math.max(MIN_TOKENS, Math.ceil(estimated))), 8_000);
    }
}

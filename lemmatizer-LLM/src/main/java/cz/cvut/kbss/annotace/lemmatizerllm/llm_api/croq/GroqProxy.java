package cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq;

import cz.cvut.kbss.annotace.lemmatizerllm.exception.TruncatedResponseException;
import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.ProxyInterface;
import lombok.NonNull;
import lombok.Setter;

import static cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.processor.LLMLemmatizerProcessor.MAX_RETRIES;
import static cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.processor.LLMLemmatizerProcessor.RETRY_TOKEN_MULTIPLIER;

public class GroqProxy implements ProxyInterface {

    @Setter
    private GroqClient groqClient;

    public GroqProxy(GroqClient groqClient) {
        this.groqClient = groqClient;
    }

    public void setModel(@NonNull String model) {
        this.groqClient.setModel(model);
    }

    @Override
    @NonNull
    public String send(String prompt, int maxTokens) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty");
        }

        int currentMaxTokens = maxTokens;

        for (int attempt = 0; attempt <= MAX_RETRIES; ++attempt) {
            try {
                return this.groqClient.send(prompt, maxTokens);
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
                    if (e.getMessage().contains("Error sending request: Empty content in response")) {
                        return "";
                    }
                    if (e.getMessage().contains("Rate limit reached for model") && e.getMessage().contains("tokens per minute")) {
                        final int startIndex = e.getMessage().indexOf("Please try again in ");
                        if (startIndex == -1) {
                            throw new RuntimeException("Error sending request: " + e.getMessage(), e);
                        }
                        final int endIndex = e.getMessage().lastIndexOf(". Need more tokens?");
                        String time = e.getMessage().substring(startIndex + 20, endIndex);

                        System.out.println("It is necessary to wait for tokens per minute for " + time + ".");

                        final boolean ms = time.contains("ms");
                        if (time.endsWith("s")) {
                            time = time.replace("s", "");
                        }
                        if (time.endsWith("m")) {
                            time = time.replace("m", "");
                        }

                        try {
                            Thread.sleep(Math.min((long) Math.floor(1.0 + Double.parseDouble(time) * (ms ? 1 : 1000)), 60_001L));
                        } catch (InterruptedException ignore) {
                        }
                    }
                    else if (e.getMessage().contains("tokens per day")) {
                        throw e;
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
}

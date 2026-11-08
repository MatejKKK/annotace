package cz.cvut.kbss.annotace.lemmatizerllm.termextractor.processor;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMTermExtractorConf;
import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq.GroqClient;
import cz.cvut.kbss.annotace.lemmatizerllm.llm_api.croq.GroqProxy;
import cz.cvut.kbss.annotace.lemmatizerllm.model.Language;

public class GroqTermExctractorProcessor {

    private GroqProxy groqProxy;

    public GroqTermExctractorProcessor(LLMTermExtractorConf conf, Language language, int index) {
        this.groqProxy = new GroqProxy(new GroqClient(conf.apiKeys(), conf.api().toString()));
    }


}

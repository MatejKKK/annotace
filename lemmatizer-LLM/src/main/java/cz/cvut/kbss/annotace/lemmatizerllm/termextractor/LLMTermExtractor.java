package cz.cvut.kbss.annotace.lemmatizerllm.termextractor;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMTermExtractorConf;
import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMTermExtractorTextConf;
import cz.cvut.kbss.annotace.lemmatizerllm.model.Language;
import cz.cvut.kbss.annotace.lemmatizerllm.termextractor.processor.GroqTermExctractorProcessor;
import cz.cvut.kbss.textanalysis.termextractor.TermExtractorApi;
import cz.cvut.kbss.textanalysis.termextractor.model.Paragraph;
import cz.cvut.kbss.textanalysis.termextractor.model.Term;
import cz.cvut.kbss.textanalysis.termextractor.model.TermExtractorResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

@Slf4j
public class LLMTermExtractor implements TermExtractorApi {

    private LLMTermExtractorConf conf;
    private GroqTermExctractorProcessor processor;

    public LLMTermExtractor(LLMTermExtractorTextConf conf) {
        this.conf = conf.toLLMTermExtractorConf();
        this.processor = new GroqTermExctractorProcessor(this.conf, 0);
    }

    @Override
    public TermExtractorResult process(List<Paragraph> paragraphs, Set<Term> terms, String lang) {
        this.processor.setLanguage(Language.valueOf(lang.toUpperCase()));
        return new TermExtractorResult(processor.process(paragraphs, terms));
    }

    @Override
    public List<String> getSupportedLanguages() {
        return List.of("en", "cs");
    }
}

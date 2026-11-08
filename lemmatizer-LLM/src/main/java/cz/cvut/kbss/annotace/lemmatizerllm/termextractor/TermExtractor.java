package cz.cvut.kbss.annotace.lemmatizerllm.termextractor;

import cz.cvut.kbss.textanalysis.termextractor.TermExtractorApi;
import cz.cvut.kbss.textanalysis.termextractor.model.Paragraph;
import cz.cvut.kbss.textanalysis.termextractor.model.Term;
import cz.cvut.kbss.textanalysis.termextractor.model.TermExtractorResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class TermExtractor implements TermExtractorApi {

    @Override
    public TermExtractorResult process(List<Paragraph> paragraphs, List<Term> terms) {
        return null;
    }

    @Override
    public List<String> getSupportedLanguages() {
        return List.of("en", "cs");
    }
}

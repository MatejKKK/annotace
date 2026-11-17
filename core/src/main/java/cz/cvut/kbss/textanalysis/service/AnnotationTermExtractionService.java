package cz.cvut.kbss.textanalysis.service;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMTermExtractorTextConf;
import cz.cvut.kbss.annotace.lemmatizerllm.termextractor.LLMTermExtractor;
import cz.cvut.kbss.textanalysis.termextractor.TermExtractorApi;
import cz.cvut.kbss.textanalysis.termextractor.model.Paragraph;
import cz.cvut.kbss.textanalysis.termextractor.model.Term;
import cz.cvut.kbss.textanalysis.termextractor.model.TermExtractorResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AnnotationTermExtractionService {

    private final TermExtractorApi termExtractor = new LLMTermExtractor(
            new LLMTermExtractorTextConf("GROQ", "GROQ_API_KEY,GROQ_API_KEY_2", "^")
    );

    public TermExtractorResult extractTerms(List<Paragraph> paragraphs, Set<Term> terms, String lang) {
        return termExtractor.process(paragraphs, terms, lang);
    }
}

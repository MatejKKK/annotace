package cz.cvut.kbss.textanalysis.termextractor;

import cz.cvut.kbss.textanalysis.termextractor.model.Paragraph;
import cz.cvut.kbss.textanalysis.termextractor.model.Term;
import cz.cvut.kbss.textanalysis.termextractor.model.TermExtractorResult;

import java.util.List;

public interface TermExtractorApi {

    /**
     *
     * @param paragraphs
     * @param terms are from vocabulary
     * @return List of paragraphs with found terms
     */
    TermExtractorResult process(List<Paragraph> paragraphs, List<Term> terms);

    /**
     * Returns a set of languages for which annotation is supported.
     *
     * @return List of languages
     */
    List<String> getSupportedLanguages();
}

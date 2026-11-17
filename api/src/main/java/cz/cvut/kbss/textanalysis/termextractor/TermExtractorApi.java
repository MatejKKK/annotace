package cz.cvut.kbss.textanalysis.termextractor;

import cz.cvut.kbss.textanalysis.termextractor.model.Paragraph;
import cz.cvut.kbss.textanalysis.termextractor.model.Term;
import cz.cvut.kbss.textanalysis.termextractor.model.TermExtractorResult;

import java.util.List;
import java.util.Set;

public interface TermExtractorApi {

    /**
     *
     * @param paragraphs
     * @param terms are from vocabulary
     * @return List of paragraphs with found terms
     */
    TermExtractorResult process(List<Paragraph> paragraphs, Set<Term> terms, String lang);

    /**
     * Returns a set of languages for which annotation is supported.
     *
     * @return List of languages
     */
    List<String> getSupportedLanguages();
}

/*
 * Annotace
 * Copyright (C) 2026 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.textanalysis.service;

import cz.cvut.kbss.textanalysis.Stopwords;
import cz.cvut.kbss.textanalysis.keywordextractor.model.KeywordExtractorResult;
import cz.cvut.kbss.textanalysis.lemmatizer.LemmatizerApi;
import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import cz.cvut.kbss.textanalysis.model.Phrase;
import cz.cvut.kbss.textanalysis.model.QueryResult;
import cz.cvut.kbss.textanalysis.model.Word;
import org.apache.jena.base.Sys;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnnotationService {

    private final LemmatizerApi lemmatizer;

    public Stopwords stopwords = new Stopwords();

    List<String> stopwordsList;

    public AnnotationService(LemmatizerApi lemmatizer) {
        this.lemmatizer = lemmatizer;
    }

    public List<Word> getAnnotations(final String textChunk,
                                     final List<QueryResult> queryResultList,
                                     final KeywordExtractorResult result, String lang)
            throws AnnotationException {
        try {
            return this._getAnnotations(textChunk, queryResultList, result, lang);
        } catch (Exception e) {
            throw new AnnotationException("Annotation failed.", e);
        }
    }

    private List<Word> _getAnnotations(final String textChunk,
                                       final List<QueryResult> queryResultList,
                                       final KeywordExtractorResult result, String lang) {
        final LemmatizerResult lemmatizerResult = lemmatizer.process(textChunk, lang);
        return annotateOntologyLabels(lemmatizerResult, queryResultList, result, lang);
    }

    private List<Word> annotateOntologyLabels(LemmatizerResult lemmatizerResult, List<QueryResult> queryResultList,
                                              final KeywordExtractorResult kerResult, String lang) {

        List<Word> annotationsResults = new ArrayList<>();

        stopwordsList = stopwords.getStopwords(lang);

        for (List<SingleLemmaResult> results : lemmatizerResult.getResult()) {
            for (SingleLemmaResult result : results) {

                System.out.println("slr:" + singleLemmaResultObserver(result));

                List<Phrase> matchedAnnotations = new ArrayList<>();
                boolean isKeyword = kerResult.getKeywords().contains(result.getLemma());
                boolean isStopword = stopwordsList.contains(result.getLemma());
                boolean isMatched;
                boolean isNotNegation;

                for (QueryResult queryResults : queryResultList) {
                    for (SingleLemmaResult ontologyResults : queryResults.getSingleLemmaResults()) {

                        isMatched = queryResults.getSingleLemmaResults().size() == 1;
                        isNotNegation = result.isNegated() == ontologyResults.isNegated();

                        if ((result.getLemma().contentEquals(ontologyResults.getLemma())
                                || result.getToken().toLowerCase()
                                         .contentEquals(ontologyResults.getToken().toLowerCase())
                                || result.getLemma().toLowerCase()
                                         .contentEquals(ontologyResults.getToken().toLowerCase())) &&
                                isNotNegation) {

                            Phrase matchedAnnotation = new Phrase(
                                    queryResults.getType(),
                                    (kerResult.getKeywords().contains(result.getLemma())
                                            && (result.getToken().equals(queryResults.getLabel()))),
                                    isMatched,
                                    queryResults.getLabel(),
                                    queryResults.getPropertyName());

                            matchedAnnotations.add(matchedAnnotation);
                        }
                    }
                }

                if ((matchedAnnotations.isEmpty()) && (isKeyword) && !(isStopword)) {
                    Phrase matchedAnnotation = new Phrase("", true, true, "", "");
                    matchedAnnotations.add(matchedAnnotation);
                }

                annotationsResults.add(new Word(result.getLemma(), result.getToken(), result.getLeadingSpaces(),
                                                result.getTrailingSpaces(),
                                                matchedAnnotations.toArray(new Phrase[]{})));

            }
        }

        return annotationsResults;
    }

    /**
     * Gets a list of languages supported by the lemmatizer.
     *
     * @return List of supported languages
     */
    public List<String> getSupportedLanguages() {
        return lemmatizer.getSupportedLanguages();
    }



    private String singleLemmaResultObserver(SingleLemmaResult singleLemmaResult) {
        return "result: " + singleLemmaResult.getLemma() + ", token: " + singleLemmaResult.getToken();
    }
}

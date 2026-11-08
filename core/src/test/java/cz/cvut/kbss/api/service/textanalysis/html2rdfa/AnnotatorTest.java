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
package cz.cvut.kbss.api.service.textanalysis.html2rdfa;

import cz.cvut.kbss.textanalysis.Constants;
import cz.cvut.kbss.textanalysis.model.Phrase;
import cz.cvut.kbss.textanalysis.model.Word;
import cz.cvut.kbss.textanalysis.service.html2rdfa.Annotator;
import cz.cvut.kbss.api.service.textanalysis.TestChunkFactory;
import java.util.stream.Stream;
import org.apache.jena.vocabulary.SKOS;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AnnotatorTest {

    private Annotator a;

    @BeforeEach
    public void init() {
        a = new Annotator("cs");
    }

    private Word[] getChunk(int i) {
        return TestChunkFactory.createTestChunks().values().toArray(new Word[][]{})[i];
    }

    @Test
    public void testSingleTermOccurrence() {
        final Word[] words = getChunk(0);
        final Stream<Node> nodes = a.annotate(words);
        Object[] aNodes= nodes.toArray();
        Assertions.assertEquals(1, aNodes.length);

        final Element element = (Element) aNodes[0];
        Assertions.assertNotNull(element.attr("about"));
        Assertions.assertEquals(Constants.NS_TERMIT_PREFIX + ":je-přiřazením-termu", element.attr("property"));
        Assertions.assertEquals("http://test.org/pojem/Test",element.attr("resource"));
        Assertions.assertEquals(Constants.NS_TERMIT_PREFIX + ":výskyt-termu",element.attr("typeof"));
    }

    @Test
    public void testTwoTermOccurrenceCount() {
        final Stream<Node> nodes = a.annotate(getChunk(1));
        Assertions.assertEquals(2,
            nodes.filter(n -> n instanceof Element).filter(n -> ((Element) n).tagName().equals("span")).count());
    }

    @Test
    void testChoosePhraseReturnsPrefLabel() {
        Phrase[] phraseList = new Phrase[] {
                new Phrase("http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/wing",false, false, "wing",
                    SKOS.prefLabel.toString()),
                new Phrase("http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/wing2", false,false, "wing",
                    SKOS.altLabel.toString()),
                };
        phraseList = a.sortArrayOfPhrasesLabelLength(phraseList);
        Assertions.assertEquals(a.choosePhrase(phraseList).getTermIri(), "http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/wing");
    }

    @Test
    void testChoosePhraseReturnsPrefLabelWhenAltLabelExactMatchFound() {
        Phrase[] phraseList = new Phrase[] {
                new Phrase("http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/struktura",false, false, "struktura", SKOS.prefLabel.toString()),
                new Phrase("http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/typ-struktury", false,false, "struktura", SKOS.altLabel.toString()),
                new Phrase("http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/typ-struktury", false,false, "typ struktury", SKOS.prefLabel.toString()),
        };
        phraseList = a.sortArrayOfPhrasesLabelLength(phraseList);
        Assertions.assertEquals(a.choosePhrase(phraseList).getTermIri(), "http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/struktura");
    }

    @Test
    void testChoosePhraseReturnsPrefLabelevenWhenAltLabelWithMoreMatchesFound() {
        Phrase[] phraseList = new Phrase[] {
                new Phrase("http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/wing",false, false, "wing", SKOS.prefLabel.toString()),
                new Phrase("http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/wing2", false,false, "wing", SKOS.altLabel.toString()),
                new Phrase("http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/wing2", false,false, "right wing", SKOS.prefLabel.toString()),
                new Phrase("http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/wing2", false,false, "right hand wing", SKOS.altLabel.toString()),
        };
        phraseList = a.sortArrayOfPhrasesLabelLength(phraseList);
        Assertions.assertEquals(a.choosePhrase(phraseList).getTermIri(), "http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/wing");
    }

    @Test
    void testChoosePhraseReturnsHigherScoreWhenPartialMatch() {
        Phrase[] phraseList = new Phrase[] {
                new Phrase("http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/wing-connector",false, false, "wing connector", SKOS.prefLabel.toString()),
                new Phrase("http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/wing2", false,false, "wing that will not match", SKOS.prefLabel.toString()),
                new Phrase("http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/right-wing",false, false, "Right wing", SKOS.prefLabel.toString()),
                new Phrase("http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/wing2", false, false, "right hand wing and flap", SKOS.altLabel.toString()),
                new Phrase("http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/right-wing", false,false, "rh wing", SKOS.altLabel.toString())
        };
        phraseList = a.sortArrayOfPhrasesLabelLength(phraseList);
        Assertions.assertEquals(a.choosePhrase(phraseList).getTermIri(), "http://onto.fel.cvut.cz/ontologies/slovnik/ls-test/pojem/right-wing");
    }
}

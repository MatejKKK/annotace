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
package cz.cvut.kbss.textanalysis.service.html2rdfa;

import cz.cvut.kbss.textanalysis.Constants;
import cz.cvut.kbss.textanalysis.model.Phrase;
import cz.cvut.kbss.textanalysis.model.Word;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cz.cvut.kbss.textanalysis.Stopwords;
import org.apache.jena.vocabulary.SKOS;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.*;

public class Annotator {

    private static final Tag ANNOTATION_TAG = Tag.valueOf(Constants.ANNOTATION_ELEMENT);

    private int i = 0;

    public Stopwords stopwords = new Stopwords();

    private final String uniqueId = generateID();

    List<String> stopwordsList;

    public Annotator(String lang) {
        stopwordsList = stopwords.getStopwords(lang);
    }

    public Stream<Node> annotate(final Word[] words) {
        final List<Node> list = new ArrayList<>();

        Node currentNode = null;

        Phrase[] previousPhrases = null;
        Phrase[] currentPhrases;
        StringBuilder content = new StringBuilder();

        double score;
        String stopChars = "";
        double numberOfTokens = 1;
        boolean previousWordisStopword = false;
        TextNode tn = new TextNode("");

        if (words != null) {
            for (Word word : words) {
                // TODO overlap

                System.out.println("word: " + word);

                if ((word.getPhrases() == null || word.getPhrases().length == 0)
                        || ((currentNode instanceof TextNode || currentNode == null) && (isStopword(
                        word.getToken())))) {
                    if (currentNode == null) {
                        currentNode = new TextNode(word.getLeadingChars());

                    } else {
                        if (!(currentNode instanceof TextNode)) {
                            if (!previousWordisStopword) {
                                if (tn.text().substring(tn.text().length() - 1).equals(stopChars)) {
                                    ((Element) currentNode).textNodes().get(0).text(tn.text().trim());
                                    list.add(currentNode);
                                    currentNode = new TextNode(stopChars);
                                } else {
                                    list.add(currentNode);
                                    currentNode = new TextNode("");
                                }
                            } else {
                                list.add(currentNode.childNode(0));
                                currentNode = new TextNode("");
                            }
                            previousPhrases = null;
                            content = new StringBuilder();
                        }
                    }
                    numberOfTokens = 0;
                    tn = ((TextNode) currentNode);
                } else {
                    List<String> commonPhraseIRI = new ArrayList<>();
                    currentPhrases = word.getPhrases();
                    Phrase[] newPhrases;
                    if (previousPhrases != null) {
                        Arrays.stream(previousPhrases).forEach(phrase -> commonPhraseIRI.add(phrase.getTermIri()));
                        newPhrases = Arrays.stream(currentPhrases)
                                           .filter(phrase -> commonPhraseIRI.contains(phrase.getTermIri()))
                                           .collect(Collectors.toList()).toArray(new Phrase[]{});
                    } else {
                        newPhrases = currentPhrases;
                    }

                    if (currentNode == null) {
                        if (!word.getLeadingChars().isEmpty()) {
                            list.add(new TextNode(word.getLeadingChars()));
                        }
                        currentNode = createEmptyAnnotationNode();
                    } else if (currentNode instanceof TextNode) {
                        list.add(currentNode);
                        currentNode = createEmptyAnnotationNode();
                    } else if (newPhrases.length == 0) {
                        if (!previousWordisStopword) {
                            ((Element) currentNode).textNodes().get(0).text(tn.text().trim());
                            list.add(currentNode);
                            TextNode spaceTn = new TextNode(" ");
                            list.add(spaceTn);
                        } else {
                            list.add(currentNode.childNode(0));
                        }
                        content = new StringBuilder();
                        currentNode = createEmptyAnnotationNode();
                        numberOfTokens = 1;
                    }
                    // TODO multiple
                    if (newPhrases.length == 0) {
                        newPhrases = currentPhrases;
                    }
                    previousPhrases = newPhrases;
                    double labelCount;

                    Phrase matchedPhrase = choosePhrase(newPhrases);
                    if (matchedPhrase.getTermIri() == null || matchedPhrase.getTermIri().isEmpty()) {
                        labelCount = numberOfTokens;
                    } else {
                        labelCount = getNumberOfTokens(matchedPhrase.getTermLabel());
                    }
                    score = numberOfTokens / labelCount;
                    if (score > 1) {
                        score = 1.0;
                    }
                    content.append(parseLemma(word.getLemma())).append(" ");
                    annotateNode((Element) currentNode, content.toString().trim(), matchedPhrase,
                                 Math.round(100 * score) / (double) 100, i++);

                    final List<TextNode> textNodes = ((Element) currentNode).textNodes();
                    if (textNodes.isEmpty()) {
                        tn = new TextNode("");
                        ((Element) currentNode).appendChild(tn);
                    } else if (textNodes.size() == 1) {
                        tn = textNodes.get(0);
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
                stopChars = word.getStopChars();
                tn.text(tn.getWholeText() + word.getToken() + stopChars);
                previousWordisStopword = isStopword(word.getToken());
                numberOfTokens += 1;
            }

            if (currentNode != null) {
                if (previousWordisStopword && !(currentNode instanceof TextNode))
                    list.add(currentNode.childNode(0));
                else if (!(currentNode instanceof TextNode) && (tn.text().endsWith(" "))) {
                    ((Element) currentNode).textNodes().get(0).text(tn.text().trim());
                    list.add(currentNode);
                    TextNode spaceTn = new TextNode(" ");
                    list.add(spaceTn);
                } else {
                    list.add(currentNode);
                }
            }
        }

        return list.stream();
    }

    private void annotateNode(final Element node, final String content, final Phrase phrase, final double score,
                              int i) {
        node.attr("about", Constants.BNODE_PREFIX + uniqueId + i);
        String iri = phrase.getTermIri();
        node.attr("property", Constants.TERM_OCCURRENCE_PROPERTY_PREFIXED);
        if ((iri != null) && (!iri.isEmpty())) {
            node.attr("resource", phrase.getTermIri());
        } else {
            node.attr("content", content);
        }
        node.attr("typeof", Constants.TERM_OCCURRENCE_PREFIXED);
        node.attr("score", Double.toString(score));
    }

    private int getNumberOfTokens(String string) {
        String trimmed = string.trim().replace("-", " - ").replace("/", " / ");
        return trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
    }

    public Phrase[] sortArrayOfPhrasesLabelLength(Phrase[] phraseList) {
        return Arrays.stream(phraseList).sorted(Comparator.comparingInt(x -> getNumberOfTokens(x.getTermLabel())))
                     .collect(Collectors.toList()).toArray(new Phrase[]{});
    }

    public Phrase choosePhrase(Phrase[] phraseList) {
        if (phraseList.length > 1) {
            phraseList = sortArrayOfPhrasesLabelLength(phraseList);
            // to insure shorter match gets priority regardless of property type
            if (getNumberOfTokens(phraseList[0].getTermLabel()) != getNumberOfTokens(phraseList[1].getTermLabel()))
                return phraseList[0];
            else {
                return countScore(phraseList);
            }
        } else {
            return phraseList[0];
        }
    }

    private Phrase countScore(Phrase[] phraseList) {
        final Map<Phrase, Double> scoredPhrases = new LinkedHashMap<>();
        Arrays.stream(phraseList).forEach(p -> scoredPhrases.put(p, countIndividualScore(p, phraseList)));
        return scoredPhrases.entrySet().stream().sorted(comparingByValue())
                            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new))
                            .entrySet().iterator().next().getKey();
    }

    private Double countIndividualScore(Phrase p, Phrase[] phraseList) {
        int matches;
        // label length
        double score = getNumberOfTokens(p.getTermLabel()) * 3;
        // property type
        score = p.getPropertyName().equals(SKOS.altLabel.toString()) ? score * 3 :
                p.getPropertyName().equals(SKOS.hiddenLabel.toString()) ? score * 4 : score;
        // number of times matched
        matches = (int) Arrays.stream(phraseList).filter(phrase -> phrase.getTermIri().equals(p.getTermIri())).count();
        switch (matches) {
            case 2:
                score *= 1.3;
                break;
            case 1:
                score *= 1.7;
                break;
        }
        return score;
    }

    private boolean isStopword(String s) {
        return stopwordsList.contains(s.trim());
    }

    private String parseLemma(String s) {
        String[] parts = s.split("[-_]");
        if (parts.length > 0) {
            return parts[0];
        }
        else {
            return s;
        }
    }

    private Element createEmptyAnnotationNode() {
        return new Element(ANNOTATION_TAG, "");
    }

    public String generateID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().substring(0, 4).concat("-");
    }
}

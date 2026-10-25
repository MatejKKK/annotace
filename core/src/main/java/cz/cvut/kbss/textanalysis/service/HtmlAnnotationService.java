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

import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.LLMService;
import cz.cvut.kbss.textanalysis.Constants;
import cz.cvut.kbss.textanalysis.dto.TextAnalysisInput;
import cz.cvut.kbss.textanalysis.keywordextractor.KeywordExtractorAPI;
import cz.cvut.kbss.textanalysis.keywordextractor.model.KeywordExtractorResult;
import cz.cvut.kbss.textanalysis.lemmatizer.LemmatizerApi;
import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import cz.cvut.kbss.textanalysis.model.QueryResult;
import cz.cvut.kbss.textanalysis.model.Word;
import cz.cvut.kbss.textanalysis.service.html2rdfa.Annotator;
import cz.cvut.kbss.textanalysis.service.lemma_aligner.Chunk;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.vocabulary.SKOS;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static cz.cvut.kbss.textanalysis.service.lemma_aligner.LemmaAligner.*;

@Service
@Slf4j
public class HtmlAnnotationService {

    private final AnnotationService annotationService;

    private final OntologyService ontologyService;

    private final KeywordExtractorAPI keywordExtractionService;

    private final LemmatizerApi lemmatizerApi;

    private static final double MIN_LEMMA_COVERAGE = 0.8;

    @Autowired
    public HtmlAnnotationService(AnnotationService annotationService,
                                 OntologyService ontologyService,
                                 KeywordExtractorAPI kerService,
                                 LemmatizerApi lemmatizerApi) {
        this.annotationService = annotationService;
        this.ontologyService = ontologyService;
        this.keywordExtractionService = kerService;
        this.lemmatizerApi = lemmatizerApi;
    }


    private Set<URI> getQueryUrlsForVocabularyContext(final TextAnalysisInput input) {
        final Set<URI> uriSet = new HashSet<>();
        final String iTerm = SKOS.Concept.toString();
        if (input.getVocabularyContexts() != null) {
            for (final URI graphUri : input.getVocabularyContexts()) {
                final String uri = input.getVocabularyRepository() + "?query=" + encode(
                        "CONSTRUCT {?s ?p ?o} WHERE { GRAPH <" + graphUri + "> {?s a <" + iTerm +
                                "> .?s ?p ?o}}");
                uriSet.add(URI.create(uri));
            }
        } else {
            final String uri = input.getVocabularyRepository() + "?query=" + encode(
                    "CONSTRUCT {?s ?p ?o} WHERE {?s a <" + iTerm + "> . ?s ?p ?o}");
            uriSet.add(URI.create(uri));
        }
        return uriSet;
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public String annotate(Boolean enableKeywordExtraction, final TextAnalysisInput input) {
        final Set<URI> vocabularies = getQueryUrlsForVocabularyContext(input);
        final String lang = input.getLanguage();
        final List<QueryResult> queryResultList = ontologyService
                .analyzeModel(vocabularies, input.getVocabularyRepositoryUserName(),
                              input.getVocabularyRepositoryPassword(), lang);

        final Document doc = Jsoup.parse(  //transfer HTML text into document
                unwrapSpan(  //removes all elements qeuried as span[score] ant return HTML text
                        Jsoup.parse(input.getContent())
                ).toString()
        );
        final List<String> chunks = new ArrayList<>();
        final NodeVisitor chunkCollector = new ChunkIterator(chunk -> chunks.add(chunk.getWholeText()));
        NodeTraversor.traverse(chunkCollector, doc);

        //extraction words:
        final KeywordExtractorResult kerResult;
        if (enableKeywordExtraction) {
            final String documentChunksString = String.join("\r\n", chunks);
            kerResult = keywordExtractionService.process(documentChunksString);
        } else {
            kerResult = KeywordExtractorResult.createEmpty();
        }

        if (lemmatizerApi instanceof LLMService) {
            final String fullText = String.join("\n\n\n\n", chunks);
            final LemmatizerResult llmResult = lemmatizerApi.process(fullText, lang);

            final List<List<SingleLemmaResult>> perChunk;
            if (llmResult != null && llmResult.getResult() != null
                    && llmResult.getResult().size() == chunks.size()) {
                // The LLM returned exactly one lemmatized paragraph per chunk -> rely on the order.
                perChunk = llmResult.getResult();
            }
            else {
                // Paragraph count differs -> align lemmas to the original texts.
                log.debug("LLM returned {} paragraphs for {} chunks, aligning lemmas.",
                        llmResult != null && llmResult.getResult() != null ? llmResult.getResult().size() : 0,
                        chunks.size());
                final List<Chunk> aligned = new ArrayList<>(align(chunks, llmResult));
                for (int i = 0; i < chunks.size() && i < aligned.size(); ++i) {
                    if (aligned.get(i).coverage() < MIN_LEMMA_COVERAGE) {
                        // The LLM dropped or garbled this chunk -> lemmatize it separately.
                        try {
                            final LemmatizerResult retry = lemmatizerApi.process(chunks.get(i), lang);
                            aligned.set(i, align(List.of(chunks.get(i)), retry).getFirst());
                        }
                        catch (Exception ex) {
                            log.warn("Re-lemmatization of chunk {} failed.", i, ex);
                        }
                    }
                }
                perChunk = aligned.stream().map(Chunk::tokens).collect(Collectors.toList());
            }

            // Keyword extraction result + ontology are evaluated once over the whole text,
            // then the words are split back into the text nodes.
            final LemmatizerResult allResult = new LemmatizerResult();
            allResult.setLemmatizer(llmResult != null ? llmResult.getLemmatizer() : null);
            allResult.setResult(perChunk);

            final Map<String, List<Word[]>> wordsByChunk = new HashMap<>();
            try {
                final List<Word> allWords = annotationService
                        .getAnnotationsWithoutLemmatization(allResult, queryResultList, kerResult, lang);
                int offset = 0; // one Word per token, in the same order
                for (int i = 0; i < chunks.size(); ++i) {
                    final int size = perChunk.get(i).size();
                    wordsByChunk.computeIfAbsent(chunks.get(i), k -> new ArrayList<>())
                            .add(allWords.subList(offset, offset + size).toArray(new Word[]{}));
                    offset += size;
                }
            } catch (Exception ex) {
                log.error("Document annotation failed.", ex);
            }

            return this.annotate(textChunk -> {
                final List<Word[]> queue = wordsByChunk.get(textChunk);
                if (queue != null && !queue.isEmpty()) {
                    return queue.removeFirst();
                }
                return new Word[]{new Word("", "", textChunk, "")}; // text stays unchanged
            }, doc, lang).toString();
        }

        //lemmatizer:
        return this.annotate(textChunk -> {
            try {
                return annotationService.getAnnotations(textChunk, queryResultList, kerResult, lang)
                                        .toArray(new Word[]{});
            } catch (Exception ex) {
                log.error("Document annotation failed.", ex);
                return new Word[]{new Word("", "", textChunk, "")};
            }
        }, doc, lang).toString();
    }

    static class ChunkIterator implements NodeVisitor {

        final Consumer<TextNode> consumer;

        ChunkIterator(Consumer<TextNode> consumer) {
            this.consumer = consumer;
        }

        public void head(Node node, int depth) {
            // nothing
        }

        public void tail(Node node, int depth) {
            if (node instanceof TextNode) {

                final TextNode textNode = (TextNode) node;
                if (!textNode.getWholeText().trim().isEmpty()) {
                    consumer.accept(textNode);
                }
            }
        }
    }

    public Document annotate(final ChunkAnnotationService p, final Document doc,
                             final String lang) {
        log.debug("Annotating document has started");
        final Document output = doc.clone();

        Elements elements = output.getAllElements();
        for (Element element : elements) {
            System.out.print(element.html());
            if (isTermOccurrence(element) && element.hasText()) {
                System.out.print(": " + element.text());
                // Unwrap any suggested occurrence with score attribute to keep the annotations up-to-date with the vocabulary terms. Keep the assigned occurrences untouched.
                element.select("span[score]").unwrap();
            }
            System.out.println();
        }

        output.outputSettings().prettyPrint(false);
        final Element eHtml = output.selectFirst("html");
        assert eHtml != null;
        eHtml.attr("prefix", Constants.NS_TERMIT_PREFIX + ": " + Constants.NS_TERMIT);

        final Map<TextNode, List<Node>> replaceMap = new HashMap<>();

        final Annotator a = new Annotator(lang);
        final NodeVisitor visitor = new ChunkIterator(chunk -> {
            final List<Node> newNode = a.annotate(
                    p.process(chunk.getWholeText())).collect(Collectors.toList());
            replaceMap.put(chunk, newNode);
        });

        NodeTraversor.traverse(visitor, output);

        for (final Map.Entry<TextNode, List<Node>> e : replaceMap.entrySet()) {
            final Node parent = e.getKey().parentNode();
            if (parent != null && !(Constants.ANNOTATION_ELEMENT.equals(parent.nodeName()) && isTermOccurrence(
                    parent))) {
                final Node node = e.getKey().text("");
                e.getValue().forEach(node::before);
            }
        }
        log.debug("Annotating document has finished");

        return output;
    }

    private Document unwrapSpan(Document doc) {
        Elements elements = doc.getElementsByTag(Constants.ANNOTATION_ELEMENT);
        for (Element element : elements) {
            if (isTermOccurrence(element) && element.hasText()) {
                // Unwrap any suggested occurrence with score attribute to keep the annotations up-to-date with the vocabulary terms. Keep the assigned occurrences untouched.
                element.select("span[score]").unwrap();
            }
        }
        return doc;
    }

    /**
     * @param node is document
     * @return if typeof is kbss`s attribute
     */

    private boolean isTermOccurrence(Node node) {
        final String typeOf = node.attr("typeof");
        return (typeOf.equals(Constants.TERM_OCCURRENCE) || typeOf.equals(Constants.TERM_OCCURRENCE_PREFIXED));
    }

    /**
     * Returns a list of supported languages.
     *
     * @return List of supported languages
     */
    public List<String> getSupportedLanguages() {
        return annotationService.getSupportedLanguages();
    }
}

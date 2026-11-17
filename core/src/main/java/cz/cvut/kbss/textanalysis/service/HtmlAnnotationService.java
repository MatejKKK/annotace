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

import cz.cvut.kbss.textanalysis.Constants;
import cz.cvut.kbss.textanalysis.dto.TextAnalysisInput;
import cz.cvut.kbss.textanalysis.keywordextractor.KeywordExtractorAPI;
import cz.cvut.kbss.textanalysis.keywordextractor.model.KeywordExtractorResult;
import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import cz.cvut.kbss.textanalysis.model.Phrase;
import cz.cvut.kbss.textanalysis.model.QueryResult;
import cz.cvut.kbss.textanalysis.model.Word;
import cz.cvut.kbss.textanalysis.service.html2rdfa.Annotator;
import cz.cvut.kbss.textanalysis.service.lemma_aligner.Chunk;
import cz.cvut.kbss.textanalysis.service.termextractor.TransferTerm;
import cz.cvut.kbss.textanalysis.service.termextractor.TransferTermTable;
import cz.cvut.kbss.textanalysis.termextractor.model.Paragraph;
import cz.cvut.kbss.textanalysis.termextractor.model.Term;
import cz.cvut.kbss.textanalysis.termextractor.model.TermExtractorResult;
import cz.cvut.kbss.textanalysis.termextractor.model.TextChunk;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.vocabulary.SKOS;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cz.cvut.kbss.textanalysis.service.lemma_aligner.LemmaAligner.*;

@Service
@Slf4j
public class HtmlAnnotationService {

    private final AnnotationTermExtractionService annotationTermExtractionService;

    private final AnnotationLemmaService annotationLemmaService;

    private final OntologyService ontologyService;

    private final KeywordExtractorAPI keywordExtractionService;

    private static final Pattern TOKEN_PATTERN = Pattern.compile("(\\S+)(\\s*)");

    private static final double MIN_LEMMA_COVERAGE = 0.8;

    @Autowired
    public HtmlAnnotationService(AnnotationTermExtractionService annotationTermExtractionService,
                                 AnnotationLemmaService annotationLemmaService,
                                 OntologyService ontologyService,
                                 KeywordExtractorAPI kerService) {
        this.annotationTermExtractionService = annotationTermExtractionService;
        this.annotationLemmaService = annotationLemmaService;
        this.ontologyService = ontologyService;
        this.keywordExtractionService = kerService;
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

    /**
     * can be changed
     * */
    public String annotate(final TextAnalysisInput input) {
        final Set<URI> vocabularies = getQueryUrlsForVocabularyContext(input);
        final String lang = input.getLanguage();
        final List<QueryResult> queryResultList = ontologyService
                .analyzeModel(vocabularies, input.getVocabularyRepositoryUserName(),
                        input.getVocabularyRepositoryPassword(), lang);
        final Document doc = Jsoup.parse(  //transfer HTML label into document
                unwrapSpan(  //removes all elements qeuried as span[score] ant return HTML label
                        Jsoup.parse(input.getContent())
                ).toString()
        );

        final List<Paragraph> chunks = new ArrayList<>();
        AtomicLong paragraphId = new AtomicLong(1);
        final NodeVisitor chunkCollector = new ChunkIterator(chunk -> chunks.add(new Paragraph(chunk.getWholeText(), paragraphId.incrementAndGet())));
        NodeTraversor.traverse(chunkCollector, doc);

        final TransferTermTable table = new TransferTermTable(queryResultList);
        final Set<Term> terms = table.getTerms();
        final List<TransferTerm> transferTermList = table.getTermTransfers();

        final TermExtractorResult termExtractorResult = annotationTermExtractionService.extractTerms(chunks, terms, lang);

        return this.annotate(termExtractorResult.getParagraphs(), table, doc, lang).toString();
    }

    public String annotate(Boolean enableKeywordExtraction, final TextAnalysisInput input) {
        final Set<URI> vocabularies = getQueryUrlsForVocabularyContext(input);
        final String lang = input.getLanguage();
        final List<QueryResult> queryResultList = ontologyService
                .analyzeModel(vocabularies, input.getVocabularyRepositoryUserName(),
                              input.getVocabularyRepositoryPassword(), lang);

        final Document doc = Jsoup.parse(  //transfer HTML label into document
                unwrapSpan(  //removes all elements qeuried as span[score] ant return HTML label
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

        final String fullText = String.join("\n\n\n\n", chunks);

        if (annotationLemmaService.processAllTogether(fullText)) {
            final LemmatizerResult llmResult = annotationLemmaService.processLemmatization(fullText, lang);

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

                int errors = 0;
                for (int i = 0; i < chunks.size() && errors < 5; ++i) {
                    if (i >= aligned.size() || aligned.get(i).coverage() < MIN_LEMMA_COVERAGE) {
                        // The LLM dropped or garbled this chunk -> lemmatize it separately.
                        try {
                            final LemmatizerResult retry = annotationLemmaService.processLemmatization(chunks.get(i), lang);
                            aligned.set(i, align(List.of(chunks.get(i)), retry).getFirst());
                        }
                        catch (Exception ex) {
                            ++errors;
                            log.warn("Re-lemmatization of chunk {} failed.", i, ex);
                        }
                    }
                }
                perChunk = aligned.stream().map(Chunk::tokens).collect(Collectors.toList());
            }

            // Keyword extraction result + ontology are evaluated once over the whole label,
            // then the words are split back into the label nodes.
            final LemmatizerResult allResult = new LemmatizerResult();
            allResult.setLemmatizer(llmResult != null ? llmResult.getLemmatizer() : null);
            allResult.setResult(perChunk);

            final Map<String, List<Word[]>> wordsByChunk = new HashMap<>();
            try {
                final List<Word> allWords = annotationLemmaService
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
                final List<Word[]> words = wordsByChunk.get(textChunk);
                if (!(words == null || words.isEmpty())) {
                    return words.removeFirst();
                }
                return new Word[]{new Word("", "", textChunk, "")}; // label stays unchanged
            }, doc, lang).toString();
        }

        //lemmatizer:
        return this.annotate(textChunk -> {
            try {
                return annotationLemmaService.getAnnotations(textChunk, queryResultList, kerResult, lang)
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
        final Document output = prepareDocument(doc, lang);

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

    private record InternParagraph(
            long id,
            Word[] words
    ) {}

    private Document annotate(final List<Paragraph> proccessedChunks, TransferTermTable table, Document doc, String lang) {
        //log.debug("Annotating document has started");

        final Map<Long, String> typeById = table.getTermTransfers().stream()
                .collect(Collectors.toMap(TransferTerm::id, TransferTerm::type, (a, b) -> a));
        final Map<Long, String> labelById = table.getTerms().stream()
                .collect(Collectors.toMap(Term::getId, Term::getText, (a, b) -> a));

        final List<InternParagraph> wordsInOrder = proccessedChunks.stream()
                .sorted(Comparator.comparingLong(Paragraph::id))
                .map(paragraph -> new InternParagraph(paragraph.id(), toWords(paragraph, typeById, labelById)))
                .toList();

        final AtomicLong position = new AtomicLong(2);

        final Document output = this.annotate(textChunk -> {
            final long index = position.getAndIncrement();
            if (wordsInOrder.stream().anyMatch(word -> word.id() == index)) {
                return wordsInOrder.stream()
                        .filter(word -> word.id() == index)
                        .findFirst().orElseThrow()
                        .words();
            }
            log.warn("No annotated paragraph for chunk at position {}", index);
            return new Word[]{new Word("", "", textChunk, "")};
        }, doc, lang);

        if (position.get() != wordsInOrder.size()) {
            log.warn("Chunk count {} differs from paragraph count {}", position.get(), wordsInOrder.size());
        }

        //log.debug("Annotating document has finished");
        return output;
    }

    private String normalize(final String s) {
        return s == null ? "" : s.replaceAll("\\s+", " ").strip();
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
     * @param doc is original document
     * @param lang is language of processed document
     * @return document with prepared attributes of html node, "!doctype html" and charset in head
     */
    private Document prepareDocument(Document doc, String lang) {
        final Document output = doc.clone();
        output.prependChild(new DocumentType("html", "", "")); //adding <!doctype html>
        output.outputSettings().prettyPrint(false);

        final Element eHtml = output.selectFirst("html");
        assert eHtml != null;
        Attribute langAttr = eHtml.attribute("lang");
        if (langAttr == null || langAttr.getValue().isBlank()) {
            PolicyFactory policy = new HtmlPolicyBuilder()  //prevention against XSS attacks
                    .requireRelNofollowOnLinks()
                    .toFactory();
            eHtml.attr("lang", policy.sanitize(lang));
        }
        eHtml.attr("prefix", Constants.NS_TERMIT_PREFIX + ": " + Constants.NS_TERMIT);

        if (output.head().children().stream().noneMatch(it -> it.is("meta[charset]"))) {
            output.head().prependElement("meta")
                    .attr("charset", "UTF-8");
        }
        return output;
    }

    private Word[] toWords(final Paragraph paragraph,
                           final Map<Long, String> typeById,
                           final Map<Long, String> labelById) {
        final List<Word> words = new ArrayList<>();

        for (final TextChunk chunk : paragraph.content()) {
            final String text = chunk.getText();
            if (text == null || text.isEmpty()) {
                continue;
            }

            final String type = typeById.get(chunk.getId());
            final String trimmed = text.strip();

            if (type == null || trimmed.isEmpty()) {
                words.add(new Word("", text, "", ""));
                continue;
            }

            final int start = text.indexOf(trimmed);
            final String leading = text.substring(0, start);
            final String trailing = text.substring(start + trimmed.length());

            if (!leading.isEmpty()) {
                words.add(new Word("", leading, "", ""));
            }

            final String label = labelById.getOrDefault(chunk.getId(), trimmed);
            final Phrase phrase = new Phrase(type, true, true, label, "");

            final Matcher matcher = TOKEN_PATTERN.matcher(trimmed);
            while (matcher.find()) {
                final String token = matcher.group(1);
                final String stopChars = matcher.group(2);
                words.add(new Word(token, token, "", stopChars, phrase));
            }

            if (!trailing.isEmpty()) {
                words.add(new Word("", trailing, "", ""));
            }
        }

        return words.toArray(new Word[]{});
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
        return annotationLemmaService.getSupportedLanguages();
    }
}

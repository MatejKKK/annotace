package cz.cvut.kbss.textanalysis.service.lemma_aligner;

import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;

import java.util.List;

/**
 * Aligned chunk: tokens that reconstruct the text exactly, and the share of atoms that got an LLM lemma.
 *
 * @param coverage 0..1; share of the chunk's words (atoms) for which a lemma was found in the LLM output.
 */
public record Chunk(List<SingleLemmaResult> tokens, double coverage) { }
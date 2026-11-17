package cz.cvut.kbss.textanalysis.termextractor.model;

public record TermTextChunk(
        Term term
) implements TextChunk {

    @Override
    public String getText() {
        return term.text();
    }

    @Override
    public long getId() {
        return term.id();
    }
}

package cz.cvut.kbss.textanalysis.termextractor.model;

public record OrdinaryTextChunk (
        String text
) implements TextChunk {

    @Override
    public String getText() {
        return text;
    }

    @Override
    public long getId() {
        return -1;
    }
}

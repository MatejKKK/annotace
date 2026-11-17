package cz.cvut.kbss.textanalysis.termextractor.model;

public final class OrdinaryTextChunk implements TextChunk {
    private final String text;

    public OrdinaryTextChunk(
            String text
    ) {
        this.text = text.replace("\n", "");
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public long getId() {
        return -1;
    }
}

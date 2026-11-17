package cz.cvut.kbss.textanalysis.termextractor.model;

public class Term implements TextChunk {
    private final String label;
    private final long id;

    public Term(
            String label,
            long id
    ) {
        this.label = label.replace("\n", "");
        this.id = id;
    }

    @Override
    public String getText() {
        return label;
    }

    @Override
    public long getId() {
        return id;
    }
}

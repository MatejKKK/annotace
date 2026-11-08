package cz.cvut.kbss.textanalysis.termextractor.model;

public interface TextChunk {

    /**
     * @return original text content
     */
    String getText();

    /**
     * @return id of term or -1 if TextChunk is not term
     */
    long getId();
}

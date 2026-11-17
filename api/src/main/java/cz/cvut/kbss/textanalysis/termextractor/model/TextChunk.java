package cz.cvut.kbss.textanalysis.termextractor.model;

public interface TextChunk {

    /**
     * @return original label content
     */
    String getText();

    /**
     * @return id of term or -1 if TextChunk is not term
     */
    long getId();
}

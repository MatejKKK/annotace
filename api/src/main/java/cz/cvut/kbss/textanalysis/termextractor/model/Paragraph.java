package cz.cvut.kbss.textanalysis.termextractor.model;

import java.util.List;

public record Paragraph (
        List<TextChunk> content,
        long id
) {

}

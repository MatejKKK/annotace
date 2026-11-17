package cz.cvut.kbss.textanalysis.termextractor.model;

import lombok.NonNull;

import java.util.List;

public record Paragraph (
        List<TextChunk> content,
        long id
) {
    public Paragraph(String content, long id) {
        this(List.of(new OrdinaryTextChunk(content)), id);
    }

    @NonNull
    public String toContent() {
        return content.isEmpty() ? "" :
                content.stream()
                        .map(TextChunk::getText)
                        .reduce("", String::concat);
    }

    @NonNull
    @Override
    public String toString() {
        return content.isEmpty() ? "" :
                content.stream()
                        .map(it -> String.valueOf(id).concat(") ").concat(it.getText()))
                        .reduce("", String::concat);
    }
}

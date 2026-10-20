package cz.cvut.kbss.dto;

public record LemmatizeRequest(
        String text,
        String lang
) {
}

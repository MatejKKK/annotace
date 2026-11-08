package cz.cvut.kbss.api.dto;

public record LemmatizeRequest(
        String text,
        String lang
) {
}

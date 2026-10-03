package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts;

import lombok.Getter;

public enum Language {
    CZ("cz"),
    CS("cs"),
    EN("en"),
    DE("de"),
    SK("sk");

    @Getter
    private final String shortcut;

    Language(String shortcut) {
        this.shortcut = shortcut;
    }
}

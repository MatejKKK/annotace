package cz.cvut.kbss.annotace.lemmatizerllm.model;

import lombok.Getter;

public enum Language {
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

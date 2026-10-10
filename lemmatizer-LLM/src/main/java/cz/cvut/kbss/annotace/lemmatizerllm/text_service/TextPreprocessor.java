package cz.cvut.kbss.annotace.lemmatizerllm.text_service;

import static cz.cvut.kbss.annotace.lemmatizerllm.text_service.StringParser.removeBorderSpaces;

public class TextPreprocessor {
    public static String removeBorderSpacesFromText(String text) {
        final StringBuilder result = new StringBuilder();
        final String[] paragraphs = text.split("\n\n\n\n");
        for (int i = 0; i < paragraphs.length; ++i) {
                result.append(removeBorderSpacesFromParagraph(paragraphs[i]).concat(i == paragraphs.length - 1 ? "" : "\n\n\n"));
        }
        System.out.println("removed: " + result.toString());
        return result.toString();
    }

    private static String removeBorderSpacesFromParagraph(String paragraph) {
        final char[] lines = paragraph.toCharArray();
        final StringBuilder result = new StringBuilder();
        StringBuilder line = new StringBuilder();

        for (char c : lines) {
            if (c == '\n') {
                result.append(removeBorderSpaces(line.toString().concat("\n")));
                line = new StringBuilder();
            }
            else line.append(c);
        }

        if (lines[lines.length - 1] != '\n') result.append(removeBorderSpaces(line.toString().concat("\n")));
        return result.toString();
    }
}

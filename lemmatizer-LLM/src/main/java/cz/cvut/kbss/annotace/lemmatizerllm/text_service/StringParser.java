package cz.cvut.kbss.annotace.lemmatizerllm.text_service;

import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class StringParser {

    @Setter
    private String delimiter;

    public List<String> responseWordParser(String response) {
        List<String> result = new ArrayList<>();
        final String[] lines = Arrays.stream(response.split("\n")).filter(
                s -> s.contains(delimiter)
        ).toArray(String[]::new);

        boolean started = false;

        for (String line : lines) {
            if (line.isBlank() || line.length() < 3) {
                if (!started) continue;
                started = false;
            }
            else started = true;

            while (line.endsWith(" ")) {
                line = line.substring(0, line.length() - 1);
            }

            result.add(line);
        }
        return result;
    }

    public String wordResultObserver(List<String> paragraphResults) {
        StringBuilder result = new StringBuilder();
        for (final String paragraphResult : paragraphResults) {
            result.append(paragraphResult.concat("\n"));
        }
        return result.toString();
    }

    public static String removeBorderSpaces(String input) {
        while (input.startsWith(" ")) {
            input = input.substring(1);
        }
        while (input.endsWith(" ")) {
            input = input.substring(0, input.length() - 1);
        }
        return input;
    }
}

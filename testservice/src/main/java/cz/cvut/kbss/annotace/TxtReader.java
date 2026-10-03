package cz.cvut.kbss.annotace;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TxtReader {
    public static String getInputText(String fileName) {
        StringBuilder sb = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    public static List<String> getExpectedList(String fileName) {
        List<String> result = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();

            while (line != null) {
                if (line.startsWith("//// ") || line.startsWith(" //// ")) {
                    line = br.readLine();
                    continue;
                }
                else if (line.contains(" //// ")) {
                    int index = line.indexOf(" //// ");
                    line = line.substring(0, index);
                }
                result.add(line);
                line = br.readLine();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}

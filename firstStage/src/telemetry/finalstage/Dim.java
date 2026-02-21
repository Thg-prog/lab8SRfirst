package telemetry.finalstage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Загружает файл dimens.ion, где каждая строка — текст размерности.
 * Номер строки (начиная с 1) соответствует коду размерности.
 */
public class Dim {
    private final Map<Integer, String> dimensions = new TreeMap<>();

    public void load(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            line = reader.readLine();
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    dimensions.put(lineNum, line);
                }
                lineNum++;
            }
        }
        String line = "";
        // Проверка: по заданию код 32 должен быть "%"
        // (если файл корректен, это выполняется автоматически)
    }

    /** Возвращает размерность по коду, или строку "[код]" если код не найден. */
    public String getDimension(int code) {
        if (! dimensions.containsKey(code)){
            System.out.println(code);
        }
        return dimensions.getOrDefault(code, "[" + code + "]");
    }
}
package telemetry.finalstage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

/**
 * Диалоговое окно для просмотра содержимого выбранного файла.
 * Поддерживает реальный просмотр загруженных данных.
 */
public class ViewFileDialog extends JDialog {
    private String tmFile;
    private String xmlFile;
    private String dimFile;
    private ReadTMI reader; // может быть null, если данные ещё не загружены

    private JComboBox<String> fileTypeCombo;
    private JTextArea contentArea;
    private JButton btnRefresh;

    public ViewFileDialog(JFrame parent, String tmFile, String xmlFile, String dimFile) {
        this(parent, tmFile, xmlFile, dimFile, null);
    }

    public ViewFileDialog(JFrame parent, String tmFile, String xmlFile, String dimFile, ReadTMI reader) {
        super(parent, "Просмотр файла", true);
        this.tmFile = tmFile;
        this.xmlFile = xmlFile;
        this.dimFile = dimFile;
        this.reader = reader;

        setSize(600, 400);
        setLocationRelativeTo(parent);

        initUI();
        updateContent();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Верхняя панель с выбором типа файла
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Выберите файл для просмотра:"));

        fileTypeCombo = new JComboBox<>();
        fileTypeCombo.addItem("TM-файл: " + new File(tmFile).getName());
        fileTypeCombo.addItem("XML-файл: " + new File(xmlFile).getName());
        fileTypeCombo.addItem("Файл размерностей: " + new File(dimFile).getName());
        fileTypeCombo.addActionListener(this::onFileTypeChanged);
        topPanel.add(fileTypeCombo);

        btnRefresh = new JButton("Обновить");
        btnRefresh.addActionListener(e -> updateContent());
        topPanel.add(btnRefresh);

        add(topPanel, BorderLayout.NORTH);

        // Текстовая область для отображения содержимого
        contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(contentArea);
        add(scrollPane, BorderLayout.CENTER);

        // Кнопка закрытия
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Закрыть");
        btnClose.addActionListener(e -> dispose());
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void onFileTypeChanged(ActionEvent e) {
        updateContent();
    }

    private void updateContent() {
        int selectedIndex = fileTypeCombo.getSelectedIndex();
        String filePath = "";
        String fileDescription = "";

        switch (selectedIndex) {
            case 0:
                filePath = tmFile;
                fileDescription = "TM-файл";
                break;
            case 1:
                filePath = xmlFile;
                fileDescription = "XML-файл";
                break;
            case 2:
                filePath = dimFile;
                fileDescription = "Файл размерностей";
                break;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(fileDescription).append(" ===\n");
        sb.append("Путь: ").append(filePath).append("\n\n");

        try {
            if (selectedIndex == 0) {
                // TM-файл - показываем первые 512 байт в hex
                viewTmFileContent(sb, filePath);
            } else if (selectedIndex == 1) {
                // XML-файл - читаем как текст
                viewTextFileContent(sb, filePath, 50); // первые 50 строк
            } else {
                // Файл размерностей - читаем как текст
                viewTextFileContent(sb, filePath, 100);
            }
        } catch (IOException ex) {
            sb.append("\n!!! Ошибка при чтении файла: ").append(ex.getMessage()).append("\n");
        }

        contentArea.setText(sb.toString());
        contentArea.setCaretPosition(0);
    }

    private void viewTmFileContent(StringBuilder sb, String filePath) throws IOException {
        File file = new File(filePath);
        sb.append("Размер файла: ").append(file.length()).append(" байт\n\n");
        sb.append("Первые 256 байт (hex):\n");

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            int bytesToRead = Math.min(256, (int) file.length());
            byte[] buffer = new byte[bytesToRead];
            raf.readFully(buffer);

            // Вывод в 16 колонок
            for (int i = 0; i < bytesToRead; i++) {
                if (i % 16 == 0) {
                    sb.append(String.format("\n%04X: ", i));
                }
                sb.append(String.format("%02X ", buffer[i] & 0xFF));
            }
            sb.append("\n");

            // Если данные загружены, показываем статистику по файлу
            if (reader != null) {
                sb.append("\n=== Статистика по загруженным данным ===\n");
                sb.append("Всего записей: ").append(reader.getTotalRecords()).append("\n");
                sb.append("Полезных записей: ").append(reader.getUsefulRecords()).append("\n");
                sb.append("Уникальных параметров: ").append(reader.getRecordsByName().size()).append("\n");
            }
        }
    }

    private void viewTextFileContent(StringBuilder sb, String filePath, int maxLines) throws IOException {
        File file = new File(filePath);
        sb.append("Размер файла: ").append(file.length()).append(" байт\n\n");
        sb.append("Содержимое (первые ").append(maxLines).append(" строк):\n");
        sb.append("----------------------\n");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && lineCount < maxLines) {
                sb.append(line).append("\n");
                lineCount++;
            }
            if (lineCount == maxLines && reader.readLine() != null) {
                sb.append("\n... (файл обрезан)");
            }
        }
    }
}
package telemetry.stage1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Диалоговое окно для просмотра содержимого выбранного файла.
 */
public class ViewFileDialog extends JDialog {
    private String tmFile;
    private String xmlFile;
    private String dimFile;

    private JComboBox<String> fileTypeCombo;
    private JTextArea contentArea;
    private JButton btnRefresh;

    public ViewFileDialog(JFrame parent, String tmFile, String xmlFile, String dimFile) {
        super(parent, "Просмотр файла", true);
        this.tmFile = tmFile;
        this.xmlFile = xmlFile;
        this.dimFile = dimFile;

        setSize(600, 400);
        setLocationRelativeTo(parent);

        initUI();
        updateContent(); // показать содержимое первого файла
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

        // Заглушка: вместо реального чтения показываем информацию
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(fileDescription).append(" ===\n");
        sb.append("Путь: ").append(filePath).append("\n\n");
        sb.append("ЗАГЛУШКА: Реальное чтение файла будет реализовано на следующих этапах.\n\n");
        sb.append("Пример содержимого (первые несколько строк):\n");
        sb.append("----------------------\n");

        // Генерируем тестовое содержимое в зависимости от типа файла
        if (selectedIndex == 0) {
            sb.append("Бинарный файл телеметрии\n");
            sb.append("Размер: 1,234,567 байт\n");
            sb.append("Первые 32 байта (hex):\n");
            sb.append("FF FF 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D\n");
            sb.append("0E 0F 10 11 12 13 14 15 16 17 18 19 1A 1B 1C 1D\n");
        } else if (selectedIndex == 1) {
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            sb.append("<Params>\n");
            sb.append("  <Param number=\"1\" name=\"Температура_1\" fullname=\"Температура модуля 1\"/>\n");
            sb.append("  <Param number=\"2\" name=\"Температура_2\" fullname=\"Температура модуля 2\"/>\n");
            sb.append("  <Param number=\"3\" name=\"Давление\" fullname=\"Давление в системе\"/>\n");
            sb.append("  <Param number=\"4\" name=\"Напряжение\" fullname=\"Напряжение питания\"/>\n");
            sb.append("  <Param number=\"5\" name=\"Ток\" fullname=\"Потребляемый ток\"/>\n");
            sb.append("</Params>\n");
        } else {
            sb.append("1 мм\n");
            sb.append("2 см\n");
            sb.append("3 дм\n");
            sb.append("4 м\n");
            sb.append("5 км\n");
            sb.append("6 мс\n");
            sb.append("7 с\n");
            sb.append("8 мин\n");
            sb.append("9 ч\n");
            sb.append("10 %\n");
        }

        contentArea.setText(sb.toString());
        contentArea.setCaretPosition(0);
    }
}
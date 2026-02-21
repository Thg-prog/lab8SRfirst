package telemetry.stage1;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Первый этап разработки: графический интерфейс с заглушками.
 * Выбор файлов вынесен в отдельное диалоговое окно.
 * Добавлена кнопка для просмотра содержимого выбранных файлов.
 * Убран список чекбоксов для статистики по параметру.
 */
public class TelemetryDialogStage1 extends JFrame {
    // Компоненты интерфейса
    private JList<String> paramList;
    private DefaultListModel<String> listModel;
    private JTextArea valueArea;
    private JTextArea statsArea;

    private JLabel lblTmFile;
    private JLabel lblXmlFile;
    private JLabel lblDimFile;
    private JButton btnSelectFiles;
    private JButton btnViewFile;
    private JButton btnLoad;

    // Список чекбоксов для общей статистики (единственный список)
    private JList<String> statListGeneral;
    private boolean[] statSelectedGeneral;

    private JButton btnShowSelected;
    private JButton btnResetStats;
    private JButton btnSaveStats;
    private JButton btnClearValues;

    // Названия пунктов общей статистики (13 пунктов)
    private static final String[] STAT_ITEMS_GENERAL = {
            "Общее количество записей",
            "Служебные записи",
            "Полезные записи",
            "Записей с неизвестным типом",
            "Long (0)",
            "Double (1)",
            "Code (2)",
            "Point (3)",
            "Уникальные параметры",
            "Point < 4 байт",
            "Point > 4 байт",
            "Code < 8 разрядов",
            "Code > 8 разрядов"
    };

    // Храним выбранные файлы
    private String selectedTmFile = "";
    private String selectedXmlFile = "";
    private String selectedDimFile = "";

    public TelemetryDialogStage1() {
        setTitle("Telemetry Viewer (Этап 1 - Заглушки)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);

        initUI();
        fillTestData(); // заполняем тестовыми данными
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ---------- Верхняя панель с информацией о файлах и кнопками ----------
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель с информацией о выбранных файлах
        JPanel fileInfoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        fileInfoPanel.setBorder(BorderFactory.createTitledBorder("Выбранные файлы"));

        JPanel tmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tmPanel.add(new JLabel("TM-файл:"));
        lblTmFile = new JLabel("не выбран");
        lblTmFile.setForeground(Color.GRAY);
        tmPanel.add(lblTmFile);
        fileInfoPanel.add(tmPanel);

        JPanel xmlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        xmlPanel.add(new JLabel("XML-файл:"));
        lblXmlFile = new JLabel("не выбран");
        lblXmlFile.setForeground(Color.GRAY);
        xmlPanel.add(lblXmlFile);
        fileInfoPanel.add(xmlPanel);

        JPanel dimPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dimPanel.add(new JLabel("Размерности:"));
        lblDimFile = new JLabel("не выбран");
        lblDimFile.setForeground(Color.GRAY);
        dimPanel.add(lblDimFile);
        fileInfoPanel.add(dimPanel);

        topPanel.add(fileInfoPanel, BorderLayout.CENTER);

        // Панель с кнопками (три кнопки в ряд)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        btnSelectFiles = new JButton("Выбрать файлы...");
        btnSelectFiles.addActionListener(this::showFileSelectionDialog);
        buttonPanel.add(btnSelectFiles);

        btnViewFile = new JButton("Просмотреть файл...");
        btnViewFile.addActionListener(this::showViewFileDialog);
        btnViewFile.setEnabled(false); // неактивна, пока не выбраны файлы
        buttonPanel.add(btnViewFile);

        btnLoad = new JButton("Загрузить данные (заглушка)");
        btnLoad.addActionListener(this::loadDataActionStub);
        btnLoad.setEnabled(false); // неактивна, пока не выбраны файлы
        buttonPanel.add(btnLoad);

        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // ---------- Центральная часть: список параметров и значения ----------
        listModel = new DefaultListModel<>();
        paramList = new JList<>(listModel);
        paramList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paramList.addListSelectionListener(this::paramSelectedStub);

        valueArea = new JTextArea();
        valueArea.setEditable(false);
        valueArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(paramList), new JScrollPane(valueArea));
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);

        // ---------- Нижняя панель: один список чекбоксов и кнопки ----------
        JPanel southPanel = new JPanel(new BorderLayout());

        // Панель для одного списка статистики
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBorder(BorderFactory.createTitledBorder("Общая статистика (заглушка)"));

        statSelectedGeneral = new boolean[STAT_ITEMS_GENERAL.length];
        DefaultListModel<String> statModel = new DefaultListModel<>();
        for (String item : STAT_ITEMS_GENERAL) {
            statModel.addElement(item);
        }

        statListGeneral = new JList<>(statModel);
        statListGeneral.setCellRenderer(new CheckBoxListRenderer(statSelectedGeneral));
        statListGeneral.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = statListGeneral.locationToIndex(e.getPoint());
                if (index != -1) {
                    statSelectedGeneral[index] = !statSelectedGeneral[index];
                    statListGeneral.repaint();
                }
            }
        });

        JScrollPane statScroll = new JScrollPane(statListGeneral);
        statScroll.setPreferredSize(new Dimension(400, 150));
        statsPanel.add(statScroll, BorderLayout.CENTER);

        // Панель для кнопок под списком статистики
        JPanel statsButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnShowSelected = new JButton("Показать выбранное (заглушка)");
        btnShowSelected.addActionListener(this::showSelectedStub);
        statsButtonPanel.add(btnShowSelected);

        btnResetStats = new JButton("Полная статистика (заглушка)");
        btnResetStats.addActionListener(e -> statsArea.setText("Полная статистика (заглушка)\nОбщее количество записей: 12345"));
        statsButtonPanel.add(btnResetStats);

        statsPanel.add(statsButtonPanel, BorderLayout.SOUTH);

        southPanel.add(statsPanel, BorderLayout.NORTH);

        // Панель для остальных кнопок (сохранение, очистка)
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        btnSaveStats = new JButton("Сохранить статистику (заглушка)");
        btnSaveStats.addActionListener(this::saveStatsStub);
        bottomButtonPanel.add(btnSaveStats);

        btnClearValues = new JButton("Очистить значения");
        btnClearValues.addActionListener(e -> valueArea.setText(""));
        bottomButtonPanel.add(btnClearValues);

        southPanel.add(bottomButtonPanel, BorderLayout.CENTER);

        // Текстовая область для отображения статистики
        statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statsArea.setBackground(new Color(240, 240, 240));
        statsArea.setText("Статистика будет здесь (заглушка)");
        southPanel.add(new JScrollPane(statsArea), BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);
    }

    /**
     * Показывает диалог выбора файлов.
     */
    private void showFileSelectionDialog(ActionEvent e) {
        FileSelectionDialog dialog = new FileSelectionDialog(this,
                selectedTmFile, selectedXmlFile, selectedDimFile);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            selectedTmFile = dialog.getTmFile();
            selectedXmlFile = dialog.getXmlFile();
            selectedDimFile = dialog.getDimFile();

            // Обновляем отображение
            updateFileLabels();
            btnViewFile.setEnabled(true);
            btnLoad.setEnabled(true);
        }
    }

    /**
     * Показывает диалог для просмотра выбранного файла.
     */
    private void showViewFileDialog(ActionEvent e) {
        ViewFileDialog dialog = new ViewFileDialog(this,
                selectedTmFile, selectedXmlFile, selectedDimFile);
        dialog.setVisible(true);
    }

    /**
     * Обновляет метки с именами выбранных файлов.
     */
    private void updateFileLabels() {
        if (!selectedTmFile.isEmpty()) {
            lblTmFile.setText(new File(selectedTmFile).getName());
            lblTmFile.setForeground(Color.BLACK);
        } else {
            lblTmFile.setText("не выбран");
            lblTmFile.setForeground(Color.GRAY);
        }

        if (!selectedXmlFile.isEmpty()) {
            lblXmlFile.setText(new File(selectedXmlFile).getName());
            lblXmlFile.setForeground(Color.BLACK);
        } else {
            lblXmlFile.setText("не выбран");
            lblXmlFile.setForeground(Color.GRAY);
        }

        if (!selectedDimFile.isEmpty()) {
            lblDimFile.setText(new File(selectedDimFile).getName());
            lblDimFile.setForeground(Color.BLACK);
        } else {
            lblDimFile.setText("не выбран");
            lblDimFile.setForeground(Color.GRAY);
        }
    }

    /**
     * Рендерер для отображения элементов списка как JCheckBox.
     */
    private static class CheckBoxListRenderer extends JCheckBox implements ListCellRenderer<String> {
        private final boolean[] selected;

        public CheckBoxListRenderer(boolean[] selected) {
            this.selected = selected;
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            setText(value);
            setSelected(selected[index]);
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return this;
        }
    }

    // ---------- Заглушки для обработчиков ----------

    private void loadDataActionStub(ActionEvent e) {
        JOptionPane.showMessageDialog(this,
                "Заглушка: загрузка данных не реализована.\n\n" +
                        "Выбраны файлы:\n" +
                        "TM: " + selectedTmFile + "\n" +
                        "XML: " + selectedXmlFile + "\n" +
                        "DIM: " + selectedDimFile,
                "Заглушка",
                JOptionPane.INFORMATION_MESSAGE);
        // В реальном коде здесь будет загрузка, а пока заполняем тестовые параметры
        fillTestData();
    }

    private void paramSelectedStub(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        String selected = paramList.getSelectedValue();
        if (selected == null) return;

        // Тестовые данные для параметра
        StringBuilder sb = new StringBuilder();
        sb.append("Параметр: ").append(selected).append("\n");
        sb.append("Всего записей: 42\n");
        sb.append("--------------------------------------------------\n");
        for (int i = 1; i <= 5; i++) {
            sb.append(String.format("12:34:%02d,123  значение %d\n", i, i));
        }

        String currentText = valueArea.getText();
        if (!currentText.isEmpty()) {
            valueArea.setText(currentText + "\n----------------------\n\n" + sb.toString());
        } else {
            valueArea.setText(sb.toString());
        }
        valueArea.setCaretPosition(0);
    }

    private void showSelectedStub(ActionEvent e) {
        // Формируем тестовый текст на основе отмеченных чекбоксов общей статистики
        StringBuilder sb = new StringBuilder();
        sb.append("Выборочная статистика (заглушка)\n");
        sb.append("================================\n\n");

        boolean anySelected = false;

        // Проверяем все чекбоксы общей статистики
        if (statSelectedGeneral[0]) {
            sb.append("Общее количество записей: 12345\n");
            anySelected = true;
        }
        if (statSelectedGeneral[1]) {
            sb.append("Служебные записи: 234\n");
            anySelected = true;
        }
        if (statSelectedGeneral[2]) {
            sb.append("Полезные записи: 12111\n");
            anySelected = true;
        }
        if (statSelectedGeneral[3]) {
            sb.append("Записей с неизвестным типом: 56\n");
            anySelected = true;
        }
        if (statSelectedGeneral[4]) {
            sb.append("Long (0): 5000\n");
            anySelected = true;
        }
        if (statSelectedGeneral[5]) {
            sb.append("Double (1): 3000\n");
            anySelected = true;
        }
        if (statSelectedGeneral[6]) {
            sb.append("Code (2): 2500\n");
            anySelected = true;
        }
        if (statSelectedGeneral[7]) {
            sb.append("Point (3): 1611\n");
            anySelected = true;
        }
        if (statSelectedGeneral[8]) {
            sb.append("Уникальные параметры: 45\n");
            anySelected = true;
        }
        if (statSelectedGeneral[9]) {
            sb.append("Point < 4 байт: 200\n");
            anySelected = true;
        }
        if (statSelectedGeneral[10]) {
            sb.append("Point > 4 байт: 1411\n");
            anySelected = true;
        }
        if (statSelectedGeneral[11]) {
            sb.append("Code < 8 разрядов: 800\n");
            anySelected = true;
        }
        if (statSelectedGeneral[12]) {
            sb.append("Code > 8 разрядов: 1700\n");
            anySelected = true;
        }

        if (!anySelected) {
            sb.append("Ничего не выбрано.");
        }

        statsArea.setText(sb.toString());
    }

    private void saveStatsStub(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить статистику (заглушка)");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            JOptionPane.showMessageDialog(this,
                    "Заглушка: файл " + file.getName() + " был бы сохранён.",
                    "Заглушка",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Заполняет список параметров тестовыми данными.
     */
    private void fillTestData() {
        listModel.clear();
        listModel.addElement("Температура_1");
        listModel.addElement("Температура_2");
        listModel.addElement("Давление");
        listModel.addElement("Напряжение");
        listModel.addElement("Ток");
        listModel.addElement("Режим_НП");
        listModel.addElement("Режим_ВП");
        listModel.addElement("Code_param");
        listModel.addElement("Point_param");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TelemetryDialogStage1().setVisible(true);
        });
    }
}
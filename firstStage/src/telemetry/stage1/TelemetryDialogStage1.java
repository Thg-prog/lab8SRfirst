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
 * Все кнопки и списки работают, но используют тестовые данные.
 * Реальная загрузка файлов не производится.
 */
public class TelemetryDialogStage1 extends JFrame {
    // Компоненты интерфейса
    private JList<String> paramList;
    private DefaultListModel<String> listModel;
    private JTextArea valueArea;
    private JTextArea statsArea;

    private JTextField txtTmFile;
    private JTextField txtXmlFile;
    private JTextField txtDimFile;
    private JButton btnLoad;

    // Список чекбоксов для общей статистики
    private JList<String> statListGeneral;
    private boolean[] statSelectedGeneral;

    // Список чекбоксов для статистики по параметру
    private JList<String> statListParam;
    private boolean[] statSelectedParam;

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

    // Названия пунктов статистики по параметру (10 пунктов)
    private static final String[] STAT_ITEMS_PARAM = {
            "Всего записей параметра",
            "Long (0)",
            "Double (1)",
            "Code (2)",
            "Point (3)",
            "Неизвестный тип",
            "Point < 4 байт",
            "Point > 4 байт",
            "Code < 8 разрядов",
            "Code > 8 разрядов"
    };

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

        // ---------- Панель выбора файлов ----------
        JPanel filePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        filePanel.add(new JLabel("TM-файл:"), gbc);
        txtTmFile = new JTextField(30);
        txtTmFile.setEditable(false);
        txtTmFile.setText("test. knp");
        gbc.gridx = 1; gbc.weightx = 1.0;
        filePanel.add(txtTmFile, gbc);
        JButton btnTm = new JButton("Обзор...");
        btnTm.addActionListener(this::chooseTmFile);
        gbc.gridx = 2; gbc.weightx = 0;
        filePanel.add(btnTm, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        filePanel.add(new JLabel("XML-файл:"), gbc);
        txtXmlFile = new JTextField(30);
        txtXmlFile.setEditable(false);
        txtXmlFile.setText("test.xml");
        gbc.gridx = 1; gbc.weightx = 1.0;
        filePanel.add(txtXmlFile, gbc);
        JButton btnXml = new JButton("Обзор...");
        btnXml.addActionListener(this::chooseXmlFile);
        gbc.gridx = 2; gbc.weightx = 0;
        filePanel.add(btnXml, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        filePanel.add(new JLabel("Размерности:"), gbc);
        txtDimFile = new JTextField(30);
        txtDimFile.setEditable(false);
        txtDimFile.setText("dimens.ion");
        gbc.gridx = 1; gbc.weightx = 1.0;
        filePanel.add(txtDimFile, gbc);
        JButton btnDim = new JButton("Обзор...");
        btnDim.addActionListener(this::chooseDimFile);
        gbc.gridx = 2; gbc.weightx = 0;
        filePanel.add(btnDim, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        btnLoad = new JButton("Загрузить данные (заглушка)");
        btnLoad.addActionListener(this::loadDataActionStub);
        filePanel.add(btnLoad, gbc);

        add(filePanel, BorderLayout.NORTH);

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

        // ---------- Нижняя панель: два списка чекбоксов и кнопки ----------
        JPanel southPanel = new JPanel(new BorderLayout());

        // Панель для двух списков (горизонтально)
        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // Список общей статистики
        JPanel generalPanel = new JPanel(new BorderLayout());
        generalPanel.setBorder(BorderFactory.createTitledBorder("Общая статистика (заглушка)"));
        statSelectedGeneral = new boolean[STAT_ITEMS_GENERAL.length];
        DefaultListModel<String> generalModel = new DefaultListModel<>();
        for (String item : STAT_ITEMS_GENERAL) {
            generalModel.addElement(item);
        }
        statListGeneral = new JList<>(generalModel);
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
        JScrollPane generalScroll = new JScrollPane(statListGeneral);
        generalScroll.setPreferredSize(new Dimension(200, 150));
        generalPanel.add(generalScroll, BorderLayout.CENTER);
        listsPanel.add(generalPanel);

        // Список статистики по параметру
        JPanel paramStatPanel = new JPanel(new BorderLayout());
        paramStatPanel.setBorder(BorderFactory.createTitledBorder("Статистика выбранного параметра (заглушка)"));
        statSelectedParam = new boolean[STAT_ITEMS_PARAM.length];
        DefaultListModel<String> paramModel = new DefaultListModel<>();
        for (String item : STAT_ITEMS_PARAM) {
            paramModel.addElement(item);
        }
        statListParam = new JList<>(paramModel);
        statListParam.setCellRenderer(new CheckBoxListRenderer(statSelectedParam));
        statListParam.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = statListParam.locationToIndex(e.getPoint());
                if (index != -1) {
                    statSelectedParam[index] = !statSelectedParam[index];
                    statListParam.repaint();
                }
            }
        });
        JScrollPane paramScroll = new JScrollPane(statListParam);
        paramScroll.setPreferredSize(new Dimension(200, 150));
        paramStatPanel.add(paramScroll, BorderLayout.CENTER);
        listsPanel.add(paramStatPanel);

        // Панель для кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnShowSelected = new JButton("Показать выбранное (заглушка)");
        btnShowSelected.addActionListener(this::showSelectedStub);
        buttonPanel.add(btnShowSelected);

        btnResetStats = new JButton("Полная статистика (заглушка)");
        btnResetStats.addActionListener(e -> statsArea.setText("Полная статистика (заглушка)\nОбщее количество записей: 12345"));
        buttonPanel.add(btnResetStats);

        btnSaveStats = new JButton("Сохранить статистику (заглушка)");
        btnSaveStats.addActionListener(this::saveStatsStub);
        buttonPanel.add(btnSaveStats);

        btnClearValues = new JButton("Очистить значения");
        btnClearValues.addActionListener(e -> valueArea.setText(""));
        buttonPanel.add(btnClearValues);

        // Сборка нижней панели
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(listsPanel, BorderLayout.CENTER);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);

        southPanel.add(controlPanel, BorderLayout.NORTH);

        // Текстовая область для отображения статистики
        statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statsArea.setBackground(new Color(240, 240, 240));
        statsArea.setText("Статистика будет здесь (заглушка)");
        southPanel.add(new JScrollPane(statsArea), BorderLayout.CENTER);

        add(southPanel, BorderLayout.SOUTH);
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

    private void chooseTmFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберите TM-файл (заглушка)");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtTmFile.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void chooseXmlFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберите XML-файл (заглушка)");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtXmlFile.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void chooseDimFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберите файл размерностей (заглушка)");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtDimFile.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void loadDataActionStub(ActionEvent e) {
        JOptionPane.showMessageDialog(this,
                "Заглушка: загрузка данных не реализована.",
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
        // Формируем тестовый текст на основе отмеченных чекбоксов
        StringBuilder sb = new StringBuilder();
        sb.append("Выборочная статистика (заглушка)\n");
        sb.append("================================\n\n");

        boolean anySelected = false;

        // Общая статистика
        if (statSelectedGeneral[0]) {
            sb.append("Общее количество записей: 12345\n");
            anySelected = true;
        }
        if (statSelectedGeneral[1]) {
            sb.append("Служебные записи: 234\n");
            anySelected = true;
        }
        // ... остальные пункты можно не перечислять для краткости

        // Статистика параметра (если выбран)
        if (paramList.getSelectedValue() != null) {
            String param = paramList.getSelectedValue();
            if (statSelectedParam[0]) {
                sb.append("\nВсего записей параметра ").append(param).append(": 42\n");
                anySelected = true;
            }
            if (statSelectedParam[1]) {
                sb.append("Long для ").append(param).append(": 10\n");
                anySelected = true;
            }
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
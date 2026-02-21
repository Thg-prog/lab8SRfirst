package telemetry.finalstage;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

/**
 * Финальный этап разработки: полностью работающий интерфейс.
 * Все функции реализованы: загрузка файлов, просмотр, статистика.
 */
public class TelemetryDialogFinal extends JFrame {
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

    // Список чекбоксов для общей статистики
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

    // Данные
    private Dim dim;
    private DatXML datXML;
    private ReadTMI reader;

    public TelemetryDialogFinal() {
        setTitle("Telemetry Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);

        initUI();
        dim = new Dim();
        datXML = new DatXML();
        reader = null;
        updateStatsPlaceholder();
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

        // Панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        btnSelectFiles = new JButton("Выбрать файлы...");
        btnSelectFiles.addActionListener(this::showFileSelectionDialog);
        buttonPanel.add(btnSelectFiles);

        btnViewFile = new JButton("Просмотреть файл...");
        btnViewFile.addActionListener(this::showViewFileDialog);
        btnViewFile.setEnabled(false);
        buttonPanel.add(btnViewFile);

        btnLoad = new JButton("Загрузить данные");
        btnLoad.addActionListener(this::loadDataAction);
        btnLoad.setEnabled(false);
        buttonPanel.add(btnLoad);

        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // ---------- Центральная часть: список параметров и значения ----------
        listModel = new DefaultListModel<>();
        paramList = new JList<>(listModel);
        paramList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paramList.addListSelectionListener(this::paramSelected);

        valueArea = new JTextArea();
        valueArea.setEditable(false);
        valueArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(paramList), new JScrollPane(valueArea));
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);

        // ---------- Нижняя панель: список чекбоксов и кнопки ----------
        JPanel southPanel = new JPanel(new BorderLayout());

        // Панель для списка статистики
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBorder(BorderFactory.createTitledBorder("Общая статистика"));

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
        btnShowSelected = new JButton("Показать выбранное");
        btnShowSelected.addActionListener(this::showSelectedStatistics);
        statsButtonPanel.add(btnShowSelected);

        btnResetStats = new JButton("Полная статистика");
        btnResetStats.addActionListener(e -> buildStatistics());
        statsButtonPanel.add(btnResetStats);

        statsPanel.add(statsButtonPanel, BorderLayout.SOUTH);

        southPanel.add(statsPanel, BorderLayout.NORTH);

        // Панель для остальных кнопок
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        btnSaveStats = new JButton("Сохранить статистику");
        btnSaveStats.addActionListener(this::saveCurrentStatistics);
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

            updateFileLabels();
            btnViewFile.setEnabled(true);
            btnLoad.setEnabled(true);
        }
    }

    /**
     * Показывает диалог для просмотра выбранного файла.
     */
    private void showViewFileDialog(ActionEvent e) {
        if (reader != null) {
            // Если данные уже загружены, передаём reader для реального просмотра
            ViewFileDialog dialog = new ViewFileDialog(this,
                    selectedTmFile, selectedXmlFile, selectedDimFile, reader);
            dialog.setVisible(true);
        } else {
            // Только выбор файлов, без данных
            ViewFileDialog dialog = new ViewFileDialog(this,
                    selectedTmFile, selectedXmlFile, selectedDimFile);
            dialog.setVisible(true);
        }
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

    // ---------- Загрузка данных ----------

    private void loadDataAction(ActionEvent e) {
        if (selectedTmFile.isEmpty() || selectedXmlFile.isEmpty() || selectedDimFile.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Выберите все три файла.");
            return;
        }

        btnLoad.setEnabled(false);
        btnLoad.setText("Загрузка...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Dim newDim = new Dim();
                newDim.load(selectedDimFile);

                DatXML newDat = new DatXML();
                newDat.load(selectedXmlFile);

                ReadTMI newReader = new ReadTMI();
                newReader.load(selectedTmFile, newDim, newDat);

                dim = newDim;
                datXML = newDat;
                reader = newReader;
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    updateUIAfterLoad();
                    JOptionPane.showMessageDialog(TelemetryDialogFinal.this,
                            "Данные успешно загружены.",
                            "Успех", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(TelemetryDialogFinal.this,
                            "Ошибка загрузки:\n" + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnLoad.setEnabled(true);
                    btnLoad.setText("Загрузить данные");
                }
            }
        };
        worker.execute();
    }

    private void updateUIAfterLoad() {
        listModel.clear();
        for (String name : reader.getRecordsByName().keySet()) {
            listModel.addElement(name);
        }
        buildStatistics();
        valueArea.setText("");

        // Сбросить состояния чекбоксов
        for (int i = 0; i < statSelectedGeneral.length; i++) {
            statSelectedGeneral[i] = false;
        }
        statListGeneral.repaint();
    }

    // ---------- Обработчики ----------

    private void paramSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() || reader == null) return;
        String selected = paramList.getSelectedValue();
        if (selected == null) return;

        List<TmDat> records = reader.getRecordsByName().get(selected);
        if (records == null) return;

        records.sort(Comparator.comparingLong(TmDat::getTime));

        StringBuilder sb = new StringBuilder();
        sb.append("Параметр: ").append(selected).append("\n");
        sb.append("Всего записей: ").append(records.size()).append("\n");
        sb.append("--------------------------------------------------\n");
        for (TmDat rec : records) {
            sb.append(TmDat.formatTime(rec.getTime()))
                    .append("  ")
                    .append(rec.getValueAsString())
                    .append("\n");
        }

        String currentText = valueArea.getText();
        if (!currentText.isEmpty()) {
            valueArea.setText(currentText + "\n----------------------\n\n" + sb.toString());
        } else {
            valueArea.setText(sb.toString());
        }
        valueArea.setCaretPosition(0);
    }

    private void buildStatistics() {
        if (reader == null) {
            statsArea.setText("Нет загруженных данных.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Статистика по файлу:\n");
        sb.append("  Общее количество ТМ-записей: ").append(reader.getTotalRecords()).append("\n");
        sb.append("  Служебных записей: ").append(reader.getServiceRecords()).append("\n");
        sb.append("  Полезных записей: ").append(reader.getUsefulRecords()).append("\n");
        sb.append("  Записей с неизвестным типом: ").append(reader.getUnknownRecords()).append("\n");

        int[] typeCounts = reader.getTypeCounts();
        sb.append("  Распределение полезных по типам:\n");
        sb.append("    Long  (0): ").append(typeCounts[0]).append("\n");
        sb.append("    Double(1): ").append(typeCounts[1]).append("\n");
        sb.append("    Code  (2): ").append(typeCounts[2]).append("\n");
        sb.append("    Point (3): ").append(typeCounts[3]).append("\n");

        sb.append("  Уникальных параметров: ").append(reader.getRecordsByName().size()).append("\n");
        sb.append("  Point < 4 байт: ").append(reader.getPointLess4()).append("\n");
        sb.append("  Point > 4 байт: ").append(reader.getPointGreater4()).append("\n");
        sb.append("  Code < 8 разрядов: ").append(reader.getCodeLess8()).append("\n");
        sb.append("  Code > 8 разрядов: ").append(reader.getCodeGreater8()).append("\n");
        statsArea.setText(sb.toString());
    }

    private void updateStatsPlaceholder() {
        statsArea.setText("Загрузите данные для отображения статистики.");
    }

    private void showSelectedStatistics(ActionEvent e) {
        if (reader == null) {
            JOptionPane.showMessageDialog(this, "Нет загруженных данных.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Выборочная статистика\n");
        sb.append("=====================\n\n");

        boolean anySelected = false;

        int[] tc = reader.getTypeCounts();

        if (statSelectedGeneral[0]) {
            sb.append(STAT_ITEMS_GENERAL[0]).append(": ").append(reader.getTotalRecords()).append("\n");
            anySelected = true;
        }
        if (statSelectedGeneral[1]) {
            sb.append(STAT_ITEMS_GENERAL[1]).append(": ").append(reader.getServiceRecords()).append("\n");
            anySelected = true;
        }
        if (statSelectedGeneral[2]) {
            sb.append(STAT_ITEMS_GENERAL[2]).append(": ").append(reader.getUsefulRecords()).append("\n");
            anySelected = true;
        }
        if (statSelectedGeneral[3]) {
            sb.append(STAT_ITEMS_GENERAL[3]).append(": ").append(reader.getUnknownRecords()).append("\n");
            anySelected = true;
        }
        if (statSelectedGeneral[4]) {
            sb.append(STAT_ITEMS_GENERAL[4]).append(": ").append(tc[0]).append("\n");
            anySelected = true;
        }
        if (statSelectedGeneral[5]) {
            sb.append(STAT_ITEMS_GENERAL[5]).append(": ").append(tc[1]).append("\n");
            anySelected = true;
        }
        if (statSelectedGeneral[6]) {
            sb.append(STAT_ITEMS_GENERAL[6]).append(": ").append(tc[2]).append("\n");
            anySelected = true;
        }
        if (statSelectedGeneral[7]) {
            sb.append(STAT_ITEMS_GENERAL[7]).append(": ").append(tc[3]).append("\n");
            anySelected = true;
        }
        if (statSelectedGeneral[8]) {
            sb.append(STAT_ITEMS_GENERAL[8]).append(": ").append(reader.getRecordsByName().size()).append("\n");
            anySelected = true;
        }
        if (statSelectedGeneral[9]) {
            sb.append(STAT_ITEMS_GENERAL[9]).append(": ").append(reader.getPointLess4()).append("\n");
            anySelected = true;
        }
        if (statSelectedGeneral[10]) {
            sb.append(STAT_ITEMS_GENERAL[10]).append(": ").append(reader.getPointGreater4()).append("\n");
            anySelected = true;
        }
        if (statSelectedGeneral[11]) {
            sb.append(STAT_ITEMS_GENERAL[11]).append(": ").append(reader.getCodeLess8()).append("\n");
            anySelected = true;
        }
        if (statSelectedGeneral[12]) {
            sb.append(STAT_ITEMS_GENERAL[12]).append(": ").append(reader.getCodeGreater8()).append("\n");
            anySelected = true;
        }

        if (!anySelected) {
            sb.append("Ничего не выбрано.");
        }

        statsArea.setText(sb.toString());
    }

    private void saveCurrentStatistics(ActionEvent e) {
        if (reader == null) {
            JOptionPane.showMessageDialog(this, "Нет загруженных данных.");
            return;
        }
        String content = statsArea.getText();
        if (content == null || content.trim().isEmpty() ||
                content.equals("Загрузите данные для отображения статистики.")) {
            JOptionPane.showMessageDialog(this, "Нет статистики для сохранения.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить статистику");
        chooser.setSelectedFile(new File("statistics.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(content);
                JOptionPane.showMessageDialog(this,
                        "Статистика сохранена в файл:\n" + file.getAbsolutePath(),
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Ошибка при сохранении: " + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TelemetryDialogFinal().setVisible(true);
        });
    }
}
package telemetry.finalstage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Диалоговое окно для выбора трёх файлов.
 */
public class FileSelectionDialog extends JDialog {
    private JTextField txtTmFile;
    private JTextField txtXmlFile;
    private JTextField txtDimFile;

    private JButton btnOk;
    private JButton btnCancel;

    private boolean confirmed = false;

    // Результаты выбора
    private String tmFile = "";
    private String xmlFile = "";
    private String dimFile = "";

    public FileSelectionDialog(JFrame parent, String initialTm, String initialXml, String initialDim) {
        super(parent, "Выбор файлов", true);
        setSize(500, 250);
        setLocationRelativeTo(parent);

        tmFile = initialTm;
        xmlFile = initialXml;
        dimFile = initialDim;

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Основная панель с полями
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // TM-файл
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("TM-файл:"), gbc);

        txtTmFile = new JTextField(30);
        txtTmFile.setText(tmFile);
        gbc.gridx = 1; gbc.weightx = 1.0;
        mainPanel.add(txtTmFile, gbc);

        JButton btnTm = new JButton("Обзор...");
        btnTm.addActionListener(this::chooseTmFile);
        gbc.gridx = 2; gbc.weightx = 0;
        mainPanel.add(btnTm, gbc);

        // XML-файл
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("XML-файл:"), gbc);

        txtXmlFile = new JTextField(30);
        txtXmlFile.setText(xmlFile);
        gbc.gridx = 1; gbc.weightx = 1.0;
        mainPanel.add(txtXmlFile, gbc);

        JButton btnXml = new JButton("Обзор...");
        btnXml.addActionListener(this::chooseXmlFile);
        gbc.gridx = 2; gbc.weightx = 0;
        mainPanel.add(btnXml, gbc);

        // Файл размерностей
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Размерности:"), gbc);

        txtDimFile = new JTextField(30);
        txtDimFile.setText(dimFile);
        gbc.gridx = 1; gbc.weightx = 1.0;
        mainPanel.add(txtDimFile, gbc);

        JButton btnDim = new JButton("Обзор...");
        btnDim.addActionListener(this::chooseDimFile);
        gbc.gridx = 2; gbc.weightx = 0;
        mainPanel.add(btnDim, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Панель с кнопками OK/Cancel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnOk = new JButton("OK");
        btnOk.addActionListener(this::onOk);
        buttonPanel.add(btnOk);

        btnCancel = new JButton("Отмена");
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnCancel);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void chooseTmFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберите TM-файл (.KNP)");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtTmFile.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void chooseXmlFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберите XML-файл с параметрами");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtXmlFile.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void chooseDimFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберите файл размерностей (dimens.ion)");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtDimFile.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void onOk(ActionEvent e) {
        tmFile = txtTmFile.getText().trim();
        xmlFile = txtXmlFile.getText().trim();
        dimFile = txtDimFile.getText().trim();

        // Проверка, что все поля заполнены
        if (tmFile.isEmpty() || xmlFile.isEmpty() || dimFile.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Необходимо выбрать все три файла.",
                    "Предупреждение",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getTmFile() {
        return tmFile;
    }

    public String getXmlFile() {
        return xmlFile;
    }

    public String getDimFile() {
        return dimFile;
    }
}
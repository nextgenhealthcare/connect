/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.amazonaws.regions.Regions;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;

public class AdvancedS3SettingsDialog extends AdvancedSettingsDialog {

    private final String NAME_COLUMN_NAME = "Name";
    private final String VALUE_COLUMN_NAME = "Value";

    private boolean saved;
    private boolean anonymous;
    private boolean updatingRegion;

    public AdvancedS3SettingsDialog(S3SchemeProperties schemeProperties, boolean anonymous) {
        this.anonymous = anonymous;

        setTitle("S3 Advanced Settings");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new MigLayout("insets 8 8 0 8, novisualpadding, hidemode 3"));
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);

        initComponents();
        initLayout();

        if (anonymous) {
            useDefaultCredentialProviderChainLabel.setEnabled(false);
            anonymousWarningLabel.setVisible(true);
            useDefaultCredentialProviderChainYesRadio.setEnabled(false);
            useDefaultCredentialProviderChainNoRadio.setEnabled(false);
            useTemporaryCredentialsLabel.setEnabled(false);
            useTemporaryCredentialsYesRadio.setEnabled(false);
            useTemporaryCredentialsNoRadio.setEnabled(false);
            durationLabel.setEnabled(false);
            durationField.setEnabled(false);
        } else {
            anonymousWarningLabel.setVisible(false);
        }

        setFileSchemeProperties(schemeProperties);

        durationField.setBackground(null);
        setPreferredSize(new Dimension(600, 400));
        pack();
        setLocationRelativeTo(PlatformUI.MIRTH_FRAME);
        setVisible(true);
    }

    @Override
    public boolean wasSaved() {
        return saved;
    }

    @Override
    public SchemeProperties getSchemeProperties() {
        S3SchemeProperties props = new S3SchemeProperties();

        props.setUseDefaultCredentialProviderChain(useDefaultCredentialProviderChainYesRadio.isSelected());

        if (useTemporaryCredentialsYesRadio.isSelected()) {
            props.setUseTemporaryCredentials(true);
        } else if (useTemporaryCredentialsNoRadio.isSelected()) {
            props.setUseTemporaryCredentials(false);
        }

        props.setDuration(NumberUtils.toInt(durationField.getText(), 7200));
        props.setRegion(regionField.getText());

        Map<String, List<String>> headers = new LinkedHashMap<>();

        for (int rowCount = 0; rowCount < customHttpHeadersTable.getRowCount(); rowCount++) {
            String name = (String) customHttpHeadersTable.getValueAt(rowCount, 0);
            List<String> values = headers.get(name);

            if (values == null) {
                values = new ArrayList<String>();
                headers.put(name, values);
            }

            values.add((String) customHttpHeadersTable.getValueAt(rowCount, 1));
        }

        props.setCustomHeaders(headers);

        return props;
    }

    public void setFileSchemeProperties(S3SchemeProperties schemeProperties) {

        if (schemeProperties.isUseDefaultCredentialProviderChain()) {
            useDefaultCredentialProviderChainYesRadio.setSelected(true);
        } else {
            useDefaultCredentialProviderChainNoRadio.setSelected(true);
        }

        if (schemeProperties.isUseTemporaryCredentials()) {
            useTemporaryCredentialsYesRadio.setSelected(true);
        } else {
            useTemporaryCredentialsNoRadio.setSelected(true);
        }
        useTemporaryRadioButtonActionPerformed();

        durationField.setText(schemeProperties.getDuration() + "");

        updatingRegion = true;
        try {
            regionField.setText(schemeProperties.getRegion());
            regionFieldUpdated();
        } finally {
            updatingRegion = false;
        }

        Map<String, List<String>> headers = schemeProperties.getCustomHeaders();
        if (headers != null && headers.size() > 0) {
            DefaultTableModel model = (DefaultTableModel) customHttpHeadersTable.getModel();
            model.setNumRows(0);

            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                List<String> values = entry.getValue();
                for (String value : values) {
                    model.addRow(new Object[] { entry.getKey(), value });
                }
            }

            deleteButton.setEnabled(true);
        } else {
            customHttpHeadersTable.setModel(new RefreshTableModel(new Object[0][1], new String[] {
                    "Name", "Value" }));
            customHttpHeadersTable.getColumnModel().getColumn(customHttpHeadersTable.getColumnModel().getColumnIndex(NAME_COLUMN_NAME)).setCellEditor(new CustomHttpHeadersTableCellEditor(true));
            customHttpHeadersTable.getColumnModel().getColumn(customHttpHeadersTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new CustomHttpHeadersTableCellEditor(false));
        }

        useTemporaryRadioButtonActionPerformed();
    }

    public boolean validateProperties() {
        boolean valid = true;

        String errors = "";

        durationField.setBackground(null);
        if (useTemporaryCredentialsYesRadio.isSelected()) {
            if (StringUtils.isBlank(durationField.getText())) {
                valid = false;
                errors += "Duration cannot be blank.\n";
                durationField.setBackground(UIConstants.INVALID_COLOR);
            } else {
                int duration = NumberUtils.toInt(durationField.getText(), 0);

                if (duration < 900 || duration > 129600) {
                    valid = false;
                    errors += "Duration must be between 900 seconds (15 minutes) and 129600 seconds (36 hours).\n";
                    durationField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        if (StringUtils.isBlank(regionField.getText())) {
            valid = false;
            errors += "Region cannot be blank.\n";
            regionField.setBackground(UIConstants.INVALID_COLOR);
        } else {
            regionField.setBackground(null);
        }

        if (StringUtils.isNotBlank(errors)) {
            PlatformUI.MIRTH_FRAME.alertError(this, errors);
            return valid;
        }

        return valid;
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        useDefaultCredentialProviderChainLabel = new JLabel("Use Default Credential Provider Chain:");
        ButtonGroup useDefaultCredentialProviderChainButtonGroup = new ButtonGroup();
        String toolTipText = "<html>If enabled and no explicit credentials are provided, the default provider chain looks for credentials in this order:<br/><ul><li><b>Environment variables:</b> AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY.</li><li><b>Java system properties:</b> aws.accessKeyId and aws.secretKey.</li><li><b>Default credentials profile file:</b> Typically located at ~/.aws/credentials (location can vary per platform).</li><li><b>ECS container credentials:</b> Loaded from an Amazon ECS environment variable.</li><li><b>Instance profile credentials:</b> Loaded from the EC2 metadata service.</li></ul></html>";

        anonymousWarningLabel = new JLabel("Anonymous credentials are currently in use");
        anonymousWarningLabel.setForeground(Color.RED);

        useDefaultCredentialProviderChainYesRadio = new JRadioButton("Yes");
        useDefaultCredentialProviderChainYesRadio.setBackground(getBackground());
        useDefaultCredentialProviderChainYesRadio.setToolTipText(toolTipText);
        useDefaultCredentialProviderChainButtonGroup.add(useDefaultCredentialProviderChainYesRadio);

        useDefaultCredentialProviderChainNoRadio = new JRadioButton("No");
        useDefaultCredentialProviderChainNoRadio.setBackground(getBackground());
        useDefaultCredentialProviderChainNoRadio.setToolTipText(toolTipText);
        useDefaultCredentialProviderChainButtonGroup.add(useDefaultCredentialProviderChainNoRadio);

        useTemporaryCredentialsLabel = new JLabel("Use Temporary Credentials:");

        useTemporaryCredentialsYesRadio = new JRadioButton("Yes");
        useTemporaryCredentialsYesRadio.setSelected(true);
        useTemporaryCredentialsYesRadio.setFocusable(false);
        useTemporaryCredentialsYesRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        useTemporaryCredentialsYesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useTemporaryCredentialsYesRadio.setToolTipText("Select whether or not to use temporary credentials.");
        useTemporaryCredentialsYesRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useTemporaryRadioButtonActionPerformed();
            }
        });

        useTemporaryCredentialsNoRadio = new JRadioButton("No");
        useTemporaryCredentialsNoRadio.setFocusable(false);
        useTemporaryCredentialsNoRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        useTemporaryCredentialsNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useTemporaryCredentialsNoRadio.setToolTipText("Select whether or not to use temporary credentials.");
        useTemporaryCredentialsNoRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useTemporaryRadioButtonActionPerformed();
            }
        });

        useTemporaryCredentialsButtonGroup = new ButtonGroup();
        useTemporaryCredentialsButtonGroup.add(useTemporaryCredentialsYesRadio);
        useTemporaryCredentialsButtonGroup.add(useTemporaryCredentialsNoRadio);

        durationLabel = new JLabel("Duration (seconds):");
        durationField = new JTextField();
        durationField.setDocument(new MirthFieldConstraints(0, false, false, true));
        durationField.setToolTipText("<html>The duration that the temporary credentials are valid. Must be<br/>between 900 seconds (15 minutes) and 129600 seconds (36 hours).</html>");

        regionLabel = new JLabel("Region:");

        regionField = new JTextField();
        regionField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent evt) {
                update();
            }

            @Override
            public void insertUpdate(DocumentEvent evt) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                update();
            }

            private void update() {
                if (!updatingRegion) {
                    updatingRegion = true;
                    try {
                        regionFieldUpdated();
                    } finally {
                        updatingRegion = false;
                    }
                }
            }
        });

        List<String> regions = new ArrayList<String>();
        regions.add("Custom");
        for (Regions region : Regions.values()) {
            regions.add(region.getName());
        }

        regionComboBox = new JComboBox<String>();
        regionComboBox.setModel(new DefaultComboBoxModel<String>(regions.toArray(new String[regions.size()])));
        regionComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (!updatingRegion) {
                    updatingRegion = true;
                    try {
                        String selectedRegion = (String) regionComboBox.getSelectedItem();
                        if (!StringUtils.equals(selectedRegion, "Custom")) {
                            regionComboBoxActionPerformed();
                        }
                    } finally {
                        updatingRegion = false;
                    }
                }
            }
        });

        customHttpHeadersLabel = new JLabel("Custom HTTP Headers:");
        customHttpHeadersTable = new MirthTable();

        Object[][] tableData = new Object[0][1];
        customHttpHeadersTable.setModel(new RefreshTableModel(tableData, new String[] { "Name",
                "Value" }));
        customHttpHeadersTable.setOpaque(true);
        customHttpHeadersTable.getColumnModel().getColumn(customHttpHeadersTable.getColumnModel().getColumnIndex(NAME_COLUMN_NAME)).setCellEditor(new CustomHttpHeadersTableCellEditor(true));
        customHttpHeadersTable.getColumnModel().getColumn(customHttpHeadersTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new CustomHttpHeadersTableCellEditor(false));

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            customHttpHeadersTable.setHighlighters(highlighter);
        }

        customHttpHeadersTable.setToolTipText("These headers will be included on all PUT requests (writing objects).");
        customHttpHeadersTable.getTableHeader().setToolTipText(customHttpHeadersTable.getToolTipText());

        customHttpHeadersScrollPane = new JScrollPane();
        customHttpHeadersScrollPane.getViewport().add(customHttpHeadersTable);

        newButton = new JButton("New");
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel model = (DefaultTableModel) customHttpHeadersTable.getModel();

                Vector<String> row = new Vector<String>();
                String header = "Property";

                for (int i = 1; i <= customHttpHeadersTable.getRowCount() + 1; i++) {
                    boolean exists = false;
                    for (int index = 0; index < customHttpHeadersTable.getRowCount(); index++) {
                        if (((String) customHttpHeadersTable.getValueAt(index, 0)).equalsIgnoreCase(header + i)) {
                            exists = true;
                        }
                    }

                    if (!exists) {
                        row.add(header + i);
                        break;
                    }
                }

                model.addRow(row);

                int rowSelectionNumber = customHttpHeadersTable.getRowCount() - 1;
                customHttpHeadersTable.setRowSelectionInterval(rowSelectionNumber, rowSelectionNumber);

                Boolean enabled = deleteButton.isEnabled();
                if (!enabled) {
                    deleteButton.setEnabled(true);
                }
            }
        });

        deleteButton = new JButton("Delete");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowSelectionNumber = customHttpHeadersTable.getSelectedRow();
                if (rowSelectionNumber > -1) {
                    DefaultTableModel model = (DefaultTableModel) customHttpHeadersTable.getModel();
                    model.removeRow(rowSelectionNumber);

                    rowSelectionNumber--;
                    if (rowSelectionNumber > -1) {
                        customHttpHeadersTable.setRowSelectionInterval(rowSelectionNumber, rowSelectionNumber);
                    }

                    if (customHttpHeadersTable.getRowCount() == 0) {
                        deleteButton.setEnabled(false);
                    }
                }
            }
        });

        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                okButtonActionPerformed();
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });
    }

    private void initLayout() {
        JPanel propertiesPanel = new JPanel(new MigLayout("insets 12, novisualpadding, hidemode 3, fill, gapy 6", "[right]13[grow]", "[][][][grow]"));
        propertiesPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        propertiesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(204, 204, 204)), "Amazon S3 Advanced Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        propertiesPanel.add(useDefaultCredentialProviderChainLabel);
        propertiesPanel.add(useDefaultCredentialProviderChainYesRadio, "split 3");
        propertiesPanel.add(useDefaultCredentialProviderChainNoRadio);
        propertiesPanel.add(anonymousWarningLabel, "gapleft 12");

        propertiesPanel.add(useTemporaryCredentialsLabel, "newline");
        propertiesPanel.add(useTemporaryCredentialsYesRadio, "split 2");
        propertiesPanel.add(useTemporaryCredentialsNoRadio);

        propertiesPanel.add(durationLabel, "newline");
        propertiesPanel.add(durationField, "w 100!");

        propertiesPanel.add(regionLabel, "newline");
        propertiesPanel.add(regionField, "w 100!, split 2");
        propertiesPanel.add(regionComboBox);

        propertiesPanel.add(customHttpHeadersLabel, "newline, aligny top");
        propertiesPanel.add(customHttpHeadersScrollPane, "span, grow, split 2");

        JPanel customHttpHeadersButtonPanel = new JPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        customHttpHeadersButtonPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        customHttpHeadersButtonPanel.add(newButton, "w 50!, wrap");
        customHttpHeadersButtonPanel.add(deleteButton, "w 50!");
        propertiesPanel.add(customHttpHeadersButtonPanel, "top");

        add(propertiesPanel, "grow, push, top, wrap");

        JPanel buttonPanel = new JPanel(new MigLayout("insets 0 8 8 8, novisualpadding, hidemode 3, fill"));
        buttonPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        buttonPanel.add(new JSeparator(), "growx, sx, wrap");
        buttonPanel.add(okButton, "newline, w 50!, sx, right, split");
        buttonPanel.add(cancelButton, "w 50!");

        add(buttonPanel, "south, span");
    }

    private void useTemporaryRadioButtonActionPerformed() {
        durationLabel.setEnabled(useTemporaryCredentialsYesRadio.isSelected() && !anonymous);
        durationField.setEnabled(useTemporaryCredentialsYesRadio.isSelected() && !anonymous);
    }

    private void regionFieldUpdated() {
        String region = regionField.getText();
        if (isCustomRegion(region)) {
            regionComboBox.setSelectedItem("Custom");
        } else {
            regionComboBox.setSelectedItem(region);
        }
    }

    private boolean isCustomRegion(String region) {
        for (Regions regionValue : Regions.values()) {
            if (StringUtils.equals(region, regionValue.getName())) {
                return false;
            }
        }
        return true;
    }

    private void regionComboBoxActionPerformed() {
        regionField.setText((String) regionComboBox.getSelectedItem());
    }

    private void okButtonActionPerformed() {
        if (customHttpHeadersTable.isEditing()) {
            customHttpHeadersTable.getCellEditor().stopCellEditing();
        }

        if (!validateProperties()) {
            return;
        }

        saved = true;
        PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
        dispose();
    }

    class CustomHttpHeadersTableCellEditor extends TextFieldCellEditor {
        boolean checkProperties;

        public CustomHttpHeadersTableCellEditor(boolean checkProperties) {
            super();
            this.checkProperties = checkProperties;
        }

        @Override
        public boolean isCellEditable(EventObject evt) {
            boolean editable = super.isCellEditable(evt);

            if (editable) {
                deleteButton.setEnabled(false);
            }

            return editable;
        }

        @Override
        protected boolean valueChanged(String value) {
            deleteButton.setEnabled(true);

            if (checkProperties && value.length() == 0) {
                return false;
            }

            return true;
        }
    }

    private JLabel useDefaultCredentialProviderChainLabel;
    private JLabel anonymousWarningLabel;
    private JRadioButton useDefaultCredentialProviderChainYesRadio;
    private JRadioButton useDefaultCredentialProviderChainNoRadio;

    private JLabel useTemporaryCredentialsLabel;
    private JRadioButton useTemporaryCredentialsYesRadio;
    private JRadioButton useTemporaryCredentialsNoRadio;
    private ButtonGroup useTemporaryCredentialsButtonGroup;

    private JLabel durationLabel;
    private JTextField durationField;

    private JLabel regionLabel;
    private JTextField regionField;
    private JComboBox<String> regionComboBox;

    private JLabel customHttpHeadersLabel;
    private MirthTable customHttpHeadersTable;
    private JScrollPane customHttpHeadersScrollPane;
    private JButton newButton;
    private JButton deleteButton;

    private JButton okButton;
    private JButton cancelButton;
}

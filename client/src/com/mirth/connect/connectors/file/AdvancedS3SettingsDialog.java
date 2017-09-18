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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;

import net.miginfocom.swing.MigLayout;

public class AdvancedS3SettingsDialog extends AdvancedSettingsDialog {

    private final String NAME_COLUMN_NAME = "Name";
    private final String VALUE_COLUMN_NAME = "Value";

    private boolean saved;

    public AdvancedS3SettingsDialog(S3SchemeProperties schemeProperties) {
        setTitle("Method Settings");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new MigLayout("insets 8 8 0 8, novisualpadding, hidemode 3"));
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);

        initComponents();
        initLayout();

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

        if (yesRadio.isSelected()) {
            props.setUseTemporaryCredentials(true);
        } else if (noRadio.isSelected()) {
            props.setUseTemporaryCredentials(false);
        }

        props.setDuration(NumberUtils.toInt(durationField.getText(), 7200));

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
            yesRadio.setSelected(true);
        } else {
            noRadio.setSelected(true);
        }

        durationField.setText(schemeProperties.getDuration() + "");

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

        if (yesRadio.isSelected() && StringUtils.isEmpty(durationField.getText())) {
            valid = false;
            errors += "Duration cannot be blank.\n";
            durationField.setBackground(UIConstants.INVALID_COLOR);
        } else {
            durationField.setBackground(null);
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

        useDefaultCredentialProviderChainYesRadio = new JRadioButton("Yes");
        useDefaultCredentialProviderChainYesRadio.setBackground(getBackground());
        useDefaultCredentialProviderChainYesRadio.setToolTipText(toolTipText);
        useDefaultCredentialProviderChainButtonGroup.add(useDefaultCredentialProviderChainYesRadio);

        useDefaultCredentialProviderChainNoRadio = new JRadioButton("No");
        useDefaultCredentialProviderChainNoRadio.setBackground(getBackground());
        useDefaultCredentialProviderChainNoRadio.setToolTipText(toolTipText);
        useDefaultCredentialProviderChainButtonGroup.add(useDefaultCredentialProviderChainNoRadio);

        useTemporaryCredentialsLabel = new JLabel("Use Temporary Credentials:");

        yesRadio = new JRadioButton("Yes");
        yesRadio.setSelected(true);
        yesRadio.setFocusable(false);
        yesRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        yesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        yesRadio.setToolTipText("Select whether or not to use temporary credentials.");
        yesRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useTemporaryRadioButtonActionPerformed();
            }
        });

        noRadio = new JRadioButton("No");
        noRadio.setFocusable(false);
        noRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        noRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noRadio.setToolTipText("Select whether or not to use temporary credentials.");
        noRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useTemporaryRadioButtonActionPerformed();
            }
        });

        useTemporaryCredentialsButtonGroup = new ButtonGroup();
        useTemporaryCredentialsButtonGroup.add(yesRadio);
        useTemporaryCredentialsButtonGroup.add(noRadio);

        durationLabel = new JLabel("Duration (s):");
        durationField = new JTextField();
        durationField.setDocument(new MirthFieldConstraints(0, false, false, true));
        durationField.setToolTipText("The duration that the temporary credentials are valid.");

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
        JPanel propertiesPanel = new JPanel(new MigLayout("insets 12, novisualpadding, hidemode 3, fill, gapy 6", "[right]13[left]", "[][][][grow]"));
        propertiesPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        propertiesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(204, 204, 204)), "Amazon S3 Advanced Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        propertiesPanel.add(useDefaultCredentialProviderChainLabel);
        propertiesPanel.add(useDefaultCredentialProviderChainYesRadio, "split 2");
        propertiesPanel.add(useDefaultCredentialProviderChainNoRadio, "push, wrap");

        propertiesPanel.add(useTemporaryCredentialsLabel);
        propertiesPanel.add(yesRadio, "split 2");
        propertiesPanel.add(noRadio, "push, wrap");

        propertiesPanel.add(durationLabel);
        propertiesPanel.add(durationField, "w 100!, wrap");

        propertiesPanel.add(customHttpHeadersLabel, "aligny top");
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
        durationLabel.setEnabled(yesRadio.isSelected());
        durationField.setEnabled(yesRadio.isSelected());
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
    private JRadioButton useDefaultCredentialProviderChainYesRadio;
    private JRadioButton useDefaultCredentialProviderChainNoRadio;

    private JLabel useTemporaryCredentialsLabel;
    private JRadioButton yesRadio;
    private JRadioButton noRadio;
    private ButtonGroup useTemporaryCredentialsButtonGroup;

    private JLabel durationLabel;
    private JTextField durationField;

    private JLabel customHttpHeadersLabel;
    private MirthTable customHttpHeadersTable;
    private JScrollPane customHttpHeadersScrollPane;
    private JButton newButton;
    private JButton deleteButton;

    private JButton okButton;
    private JButton cancelButton;

}

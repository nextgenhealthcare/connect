/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.components.MirthButton;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;

public class RegexAttachmentDialog extends MirthDialog {

    private Frame parent;
    private boolean initialFocus = true;
    private AttachmentHandlerProperties attachmentHandlerProperties;

    public static final int DATA_TYPE_COLUMN_NUMBER = 1;

    public RegexAttachmentDialog(AttachmentHandlerProperties properties) {
        super(PlatformUI.MIRTH_FRAME, true);
        this.parent = PlatformUI.MIRTH_FRAME;

        setTitle("Set Attachment Properties");
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
        setLayout(new MigLayout("novisualpadding, hidemode 3, insets 12", "[fill, grow]"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(650, 550));
        setLocationRelativeTo(parent);

        initComponents();
        initLayout();
        initInboundReplacementTable();
        initOutboundReplacementTable();

        attachmentHandlerProperties = properties;

        regexTextField.setText(StringUtils.defaultIfEmpty(attachmentHandlerProperties.getProperties().get("regex.pattern"), ""));
        regexTextField.requestFocus();
        regexTextField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if (initialFocus) {
                    regexTextField.setCaretPosition(0);
                    initialFocus = false;
                }
            }

        });
        mimeTypeField.setText(StringUtils.defaultIfEmpty(attachmentHandlerProperties.getProperties().get("regex.mimetype"), ""));

        int count = 0;
        while (attachmentHandlerProperties.getProperties().containsKey("regex.replaceKey" + count)) {
            DefaultTableModel tableModel = (DefaultTableModel) inboundReplacementTable.getModel();
            tableModel.addRow(new Object[] {
                    attachmentHandlerProperties.getProperties().get("regex.replaceKey" + count),
                    attachmentHandlerProperties.getProperties().get("regex.replaceValue" + count) });
            count++;
        }

        count = 0;
        while (attachmentHandlerProperties.getProperties().containsKey("outbound.regex.replaceKey" + count)) {
            DefaultTableModel tableModel = (DefaultTableModel) outboundReplacementTable.getModel();
            tableModel.addRow(new Object[] {
                    attachmentHandlerProperties.getProperties().get("outbound.regex.replaceKey" + count),
                    attachmentHandlerProperties.getProperties().get("outbound.regex.replaceValue" + count) });
            count++;
        }

        pack();
        setVisible(true);
    }

    private void initInboundReplacementTable() {
        inboundReplacementTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        DefaultTableModel model = new DefaultTableModel(new Object[][] {}, new String[] {
                "Replace All", "Replace With" }) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }

            @Override
            public void setValueAt(Object value, int row, int column) {
                if (!value.equals(getValueAt(row, column))) {
                    parent.setSaveEnabled(true);
                }

                super.setValueAt(value, row, column);
            }
        };

        inboundReplacementTable.setSortable(false);
        inboundReplacementTable.getTableHeader().setReorderingAllowed(false);
        inboundReplacementTable.setModel(model);

        inboundReplacementTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                inboundDeleteButton.setEnabled(inboundReplacementTable.getSelectedRow() != -1);
            }
        });

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            inboundReplacementTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        inboundDeleteButton.setEnabled(false);
    }

    private void initOutboundReplacementTable() {
        DefaultTableModel model = new DefaultTableModel(new Object[][] {}, new String[] {
                "Replace All", "Replace With" }) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }

            @Override
            public void setValueAt(Object value, int row, int column) {
                if (!value.equals(getValueAt(row, column))) {
                    parent.setSaveEnabled(true);
                }

                super.setValueAt(value, row, column);
            }
        };

        outboundReplacementTable.setSortable(false);
        outboundReplacementTable.getTableHeader().setReorderingAllowed(false);
        outboundReplacementTable.setModel(model);

        outboundReplacementTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                outboundDeleteButton.setEnabled(outboundReplacementTable.getSelectedRow() != -1);
            }
        });

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            outboundReplacementTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        outboundDeleteButton.setEnabled(false);
    }

    private void initComponents() {
        regularExpressionPanel = new JPanel(new MigLayout("novisualpadding, hidemode 3, fill, insets 0"));
        regularExpressionPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        regularExpressionPanel.setBorder(BorderFactory.createTitledBorder("Regular Expression"));

        regexTextField = new MirthTextField();
        regexTextField.setToolTipText("<html>The regular expression that will be used to match and extract attachments.<br>If capturing groups are used, only the last group will be extracted.</html>");

        regexExampleTextField = new JTextField();
        regexExampleTextField.setText("Example for OBX 5.5: (?:OBX\\|(?:[^|]*\\|){4}(?:[^|^]*\\^){4})([^|^\\r\\n]*)(?:[|^\\r\\n]|$)");
        regexExampleTextField.setEditable(false);
        regexExampleTextField.setBorder(null);
        regexExampleTextField.setOpaque(false);

        mimeTypePanel = new JPanel(new MigLayout("novisualpadding, hidemode 3, fill, insets 0"));
        mimeTypePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        mimeTypePanel.setBorder(BorderFactory.createTitledBorder("Mime Type"));

        mimeTypeField = new MirthTextField();
        mimeTypeField.setToolTipText("The mime type of the extracted attachment data.");

        stringReplacementPanel = new JPanel(new MigLayout("novisualpadding, hidemode 3, insets 0"));
        stringReplacementPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        stringReplacementPanel.setBorder(BorderFactory.createTitledBorder("String Replacement"));

        stringReplacementLabel = new JLabel();
        stringReplacementLabel.setText("<html>Replace strings on the matched data before storing. Do not use regular expressions in these fields or surround with quotes.<br>\nExample: Use <b>\\\\X0D0A\\\\</b> and <b>\\r\\n</b> to replace \\X0D0A\\ with actual CRLF characters. </html>");

        inboundReplacementTable = new MirthTable();

        inboundScrollPane = new JScrollPane();
        inboundScrollPane.setViewportView(inboundReplacementTable);

        outboundReplacementTable = new MirthTable();

        outboundScrollPane = new JScrollPane();
        outboundScrollPane.setViewportView(outboundReplacementTable);

        inboundButtonPanel = new JPanel(new MigLayout("novisualpadding, hidemode 3, insets 0", "", ""));
        inboundButtonPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        inboundNewButton = new MirthButton("New");
        inboundNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                inboundNewButtonActionPerformed(evt);
            }
        });

        inboundDeleteButton = new MirthButton("Delete");
        inboundDeleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                inboundDeleteButtonActionPerformed(evt);
            }
        });

        outboundButtonPanel = new JPanel(new MigLayout("novisualpadding, hidemode 3, insets 0", "", ""));
        outboundButtonPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        outboundNewButton = new MirthButton("New");
        outboundNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                outboundNewButtonActionPerformed(evt);
            }
        });

        outboundDeleteButton = new MirthButton("Delete");
        outboundDeleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                outboundDeleteButtonActionPerformed(evt);
            }
        });

        separator = new JSeparator();

        closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
    }

    private void initLayout() {
        regularExpressionPanel.add(regexExampleTextField, "sx, growx, wrap");
        regularExpressionPanel.add(regexTextField, "sx, growx");

        mimeTypePanel.add(mimeTypeField, "sx, growx");

        stringReplacementPanel.add(stringReplacementLabel, "left, sx, growx, wrap");
        stringReplacementPanel.add(new JLabel("<html><b>Inbound Replacements</b></html>"), "newline, wrap");
        stringReplacementPanel.add(inboundScrollPane, "right, grow, push");
        inboundButtonPanel.add(inboundNewButton, "w 50!, wrap");
        inboundButtonPanel.add(inboundDeleteButton, "w 50!");
        stringReplacementPanel.add(inboundButtonPanel, "aligny top, left");

        stringReplacementPanel.add(new JLabel("<html><b>Outbound Replacements</b></html>"), "newline, wrap");
        stringReplacementPanel.add(outboundScrollPane, "right, grow, push");
        outboundButtonPanel.add(outboundNewButton, "w 50!, wrap");
        outboundButtonPanel.add(outboundDeleteButton, "w 50!");
        stringReplacementPanel.add(outboundButtonPanel, "aligny top, left");

        add(regularExpressionPanel, "wrap");
        add(mimeTypePanel, "wrap");
        add(stringReplacementPanel, "wrap");

        add(separator, "wrap");
        add(closeButton, " w 50!, right");
    }

    private void closeButtonActionPerformed(ActionEvent evt) {
        attachmentHandlerProperties.getProperties().put("regex.pattern", regexTextField.getText());
        attachmentHandlerProperties.getProperties().put("regex.mimetype", mimeTypeField.getText());

        DefaultTableModel inboundTableModel = (DefaultTableModel) inboundReplacementTable.getModel();
        for (int row = 0; row < inboundTableModel.getRowCount(); row++) {
            String replaceKey = (String) inboundTableModel.getValueAt(row, 0);
            String replaceValue = (String) inboundTableModel.getValueAt(row, 1);

            attachmentHandlerProperties.getProperties().put("regex.replaceKey" + row, replaceKey);
            attachmentHandlerProperties.getProperties().put("regex.replaceValue" + row, replaceValue);
        }

        DefaultTableModel outboundTableModel = (DefaultTableModel) outboundReplacementTable.getModel();
        for (int row = 0; row < outboundTableModel.getRowCount(); row++) {
            String replaceKey = (String) outboundTableModel.getValueAt(row, 0);
            String replaceValue = (String) outboundTableModel.getValueAt(row, 1);

            attachmentHandlerProperties.getProperties().put("outbound.regex.replaceKey" + row, replaceKey);
            attachmentHandlerProperties.getProperties().put("outbound.regex.replaceValue" + row, replaceValue);
        }

        attachmentHandlerProperties = null;
        this.dispose();
    }

    private void inboundNewButtonActionPerformed(ActionEvent evt) {
        DefaultTableModel model = ((DefaultTableModel) inboundReplacementTable.getModel());
        int row = model.getRowCount();

        model.addRow(new Object[] { "", "" });

        inboundReplacementTable.setRowSelectionInterval(row, row);

        parent.setSaveEnabled(true);
    }

    private void inboundDeleteButtonActionPerformed(ActionEvent evt) {
        int selectedRow = inboundReplacementTable.getSelectedRow();

        if (selectedRow != -1 && !inboundReplacementTable.isEditing()) {
            ((DefaultTableModel) inboundReplacementTable.getModel()).removeRow(selectedRow);
        }

        int rowCount = inboundReplacementTable.getRowCount();

        if (rowCount > 0) {
            if (selectedRow >= rowCount) {
                selectedRow--;
            }

            inboundReplacementTable.setRowSelectionInterval(selectedRow, selectedRow);
        }

        parent.setSaveEnabled(true);
    }

    private void outboundNewButtonActionPerformed(ActionEvent evt) {
        DefaultTableModel model = ((DefaultTableModel) outboundReplacementTable.getModel());
        int row = model.getRowCount();

        model.addRow(new Object[] { "", "" });

        outboundReplacementTable.setRowSelectionInterval(row, row);

        parent.setSaveEnabled(true);
    }

    private void outboundDeleteButtonActionPerformed(ActionEvent evt) {
        int selectedRow = outboundReplacementTable.getSelectedRow();

        if (selectedRow != -1 && !outboundReplacementTable.isEditing()) {
            ((DefaultTableModel) outboundReplacementTable.getModel()).removeRow(selectedRow);
        }

        int rowCount = outboundReplacementTable.getRowCount();

        if (rowCount > 0) {
            if (selectedRow >= rowCount) {
                selectedRow--;
            }

            outboundReplacementTable.setRowSelectionInterval(selectedRow, selectedRow);
        }

        parent.setSaveEnabled(true);
    }

    private JPanel regularExpressionPanel;
    private JTextField regexExampleTextField;
    private MirthTextField regexTextField;

    private JPanel mimeTypePanel;
    private MirthTextField mimeTypeField;

    private JPanel stringReplacementPanel;
    private JLabel stringReplacementLabel;
    private MirthTable inboundReplacementTable;
    private JScrollPane inboundScrollPane;
    private MirthTable outboundReplacementTable;
    private JScrollPane outboundScrollPane;

    private JPanel inboundButtonPanel;
    private MirthButton inboundNewButton;
    private MirthButton inboundDeleteButton;

    private JPanel outboundButtonPanel;
    private MirthButton outboundNewButton;
    private MirthButton outboundDeleteButton;

    private JSeparator separator;
    private JButton closeButton;
}

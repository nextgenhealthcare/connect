/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.attachments;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;

public class RegexAttachmentDialog extends MirthDialog {

    private Frame parent;
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

        initComponents();
        initLayout();
        initInboundReplacementTable();
        initOutboundReplacementTable();

        attachmentHandlerProperties = properties;

        List<RegexInfo> regexInfoList = new ArrayList<RegexInfo>();

        if (attachmentHandlerProperties.getProperties().containsKey("regex.pattern")) {
            String regex = StringUtils.defaultString(attachmentHandlerProperties.getProperties().get("regex.pattern"));
            String mimeType = StringUtils.defaultString(attachmentHandlerProperties.getProperties().get("regex.mimetype"));
            regexInfoList.add(new RegexInfo(regex, mimeType));
        }

        int count = 0;
        while (attachmentHandlerProperties.getProperties().containsKey("regex.pattern" + count)) {
            String regex = StringUtils.defaultString(attachmentHandlerProperties.getProperties().get("regex.pattern" + count));
            String mimeType = StringUtils.defaultString(attachmentHandlerProperties.getProperties().get("regex.mimetype" + count));
            regexInfoList.add(new RegexInfo(regex, mimeType));
            count++;
        }

        Object[][] regexTableData = new Object[regexInfoList.size()][2];
        int i = 0;
        for (RegexInfo regexInfo : regexInfoList) {
            regexTableData[i][0] = regexInfo.pattern;
            regexTableData[i][1] = regexInfo.mimeType;
            i++;
        }
        ((RefreshTableModel) regexTable.getModel()).refreshDataVector(regexTableData);

        count = 0;
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

        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void initInboundReplacementTable() {
        inboundReplacementTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        DefaultTableModel model = new DefaultTableModel(new Object[][] {}, new String[] {
                "Replace All", "Replace With" }) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
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
        regularExpressionPanel.setBorder(BorderFactory.createTitledBorder("Regular Expressions"));

        regexExampleTextField = new JTextField();
        regexExampleTextField.setText("(?:OBX\\|(?:[^|]*\\|){4}(?:[^|^]*\\^){4})([^|^\\r\\n]*)(?:[|^\\r\\n]|$)");
        regexExampleTextField.setEditable(false);
        regexExampleTextField.setBorder(null);
        regexExampleTextField.setOpaque(false);

        regexTable = new MirthTable();
        regexTable.setModel(new RefreshTableModel(new Object[] { "Regular Expression", "MIME Type" }, 0));
        regexTable.setDragEnabled(false);
        regexTable.setRowSelectionAllowed(true);
        regexTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        regexTable.setRowHeight(UIConstants.ROW_HEIGHT);
        regexTable.setFocusable(true);
        regexTable.setOpaque(true);
        regexTable.getTableHeader().setReorderingAllowed(false);
        regexTable.setEditable(true);
        regexTable.setSortable(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            regexTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        regexTable.getColumnExt(0).setMinWidth(105);
        regexTable.getColumnExt(0).setPreferredWidth(350);
        regexTable.getColumnExt(0).setToolTipText("<html>The regular expression that will be used to match and extract attachments.<br>If capturing groups are used, only the last group will be extracted.</html>");

        regexTable.getColumnExt(1).setMinWidth(63);
        regexTable.getColumnExt(1).setToolTipText("The MIME type of the extracted attachment data. Source map variables may be used here.");

        regexTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                regexDeleteButton.setEnabled(regexTable.getSelectedRow() >= 0);
            }
        });

        regexTableScrollPane = new JScrollPane(regexTable);

        regexNewButton = new JButton("New");
        regexNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                regexNewButtonActionPerformed();
            }
        });

        regexDeleteButton = new JButton("Delete");
        regexDeleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                regexDeleteButtonActionPerformed();
            }
        });
        regexDeleteButton.setEnabled(false);

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

        inboundNewButton = new JButton("New");
        inboundNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                inboundNewButtonActionPerformed(evt);
            }
        });

        inboundDeleteButton = new JButton("Delete");
        inboundDeleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                inboundDeleteButtonActionPerformed(evt);
            }
        });

        outboundButtonPanel = new JPanel(new MigLayout("novisualpadding, hidemode 3, insets 0", "", ""));
        outboundButtonPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        outboundNewButton = new JButton("New");
        outboundNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                outboundNewButtonActionPerformed(evt);
            }
        });

        outboundDeleteButton = new JButton("Delete");
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
        regularExpressionPanel.add(new JLabel("Example for OBX 5.5: "), "sx, split 2");
        regularExpressionPanel.add(regexExampleTextField, "push");
        regularExpressionPanel.add(regexTableScrollPane, "newline, h 80:, grow, push");
        regularExpressionPanel.add(regexNewButton, "w 50!, top, split 2, flowy");
        regularExpressionPanel.add(regexDeleteButton, "w 50!");

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

        add(regularExpressionPanel, "grow, wrap");
        add(stringReplacementPanel, "wrap");

        add(separator, "wrap");
        add(closeButton, " w 50!, right");
        pack();
    }

    private void regexNewButtonActionPerformed() {
        int row = regexTable.getSelectedRow() + 1;
        ((RefreshTableModel) regexTable.getModel()).insertRow(row, new Object[] { "", "" });
        regexTable.getSelectionModel().setSelectionInterval(row, row);
        regexTable.scrollRowToVisible(row);
    }

    private void regexDeleteButtonActionPerformed() {
        int selectedRow = regexTable.getSelectedRow();
        if (selectedRow >= 0) {
            ((RefreshTableModel) regexTable.getModel()).removeRow(selectedRow);
            if (selectedRow < regexTable.getRowCount()) {
                regexTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
            } else if (regexTable.getRowCount() > 0) {
                regexTable.getSelectionModel().setSelectionInterval(regexTable.getRowCount() - 1, regexTable.getRowCount() - 1);
            }
        }
    }

    private void closeButtonActionPerformed(ActionEvent evt) {
        if (regexTable.isEditing()) {
            regexTable.getCellEditor().stopCellEditing();
        }
        if (inboundReplacementTable.isEditing()) {
            inboundReplacementTable.getCellEditor().stopCellEditing();
        }
        if (outboundReplacementTable.isEditing()) {
            outboundReplacementTable.getCellEditor().stopCellEditing();
        }

        attachmentHandlerProperties.getProperties().clear();

        for (int row = 0; row < regexTable.getModel().getRowCount(); row++) {
            String regex = (String) regexTable.getModel().getValueAt(row, 0);
            String mimeType = (String) regexTable.getModel().getValueAt(row, 1);

            attachmentHandlerProperties.getProperties().put("regex.pattern" + row, regex);
            attachmentHandlerProperties.getProperties().put("regex.mimetype" + row, mimeType);
        }

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
        parent.setSaveEnabled(true);
        this.dispose();
    }

    private void inboundNewButtonActionPerformed(ActionEvent evt) {
        DefaultTableModel model = ((DefaultTableModel) inboundReplacementTable.getModel());
        int row = model.getRowCount();

        model.addRow(new Object[] { "", "" });

        inboundReplacementTable.setRowSelectionInterval(row, row);
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
    }

    private void outboundNewButtonActionPerformed(ActionEvent evt) {
        DefaultTableModel model = ((DefaultTableModel) outboundReplacementTable.getModel());
        int row = model.getRowCount();

        model.addRow(new Object[] { "", "" });

        outboundReplacementTable.setRowSelectionInterval(row, row);
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
    }

    private class RegexInfo {
        public String pattern;
        public String mimeType;

        public RegexInfo(String pattern, String mimeType) {
            this.pattern = pattern;
            this.mimeType = mimeType;
        }
    }

    private JPanel regularExpressionPanel;
    private JTextField regexExampleTextField;
    private MirthTable regexTable;
    private JScrollPane regexTableScrollPane;
    private JButton regexNewButton;
    private JButton regexDeleteButton;

    private JPanel stringReplacementPanel;
    private JLabel stringReplacementLabel;
    private MirthTable inboundReplacementTable;
    private JScrollPane inboundScrollPane;
    private MirthTable outboundReplacementTable;
    private JScrollPane outboundScrollPane;

    private JPanel inboundButtonPanel;
    private JButton inboundNewButton;
    private JButton inboundDeleteButton;

    private JPanel outboundButtonPanel;
    private JButton outboundNewButton;
    private JButton outboundDeleteButton;

    private JSeparator separator;
    private JButton closeButton;
}

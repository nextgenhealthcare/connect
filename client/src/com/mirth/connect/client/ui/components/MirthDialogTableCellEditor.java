package com.mirth.connect.client.ui.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.ObjectUtils;

import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;

public class MirthDialogTableCellEditor extends AbstractCellEditor implements TableCellEditor {
    private JPanel panel;
    private JLabel label;
    private String text;
    private String originalValue;

    public MirthDialogTableCellEditor(final JTable table) {
        panel = new JPanel(new MigLayout("insets 0 1 0 0, novisualpadding, hidemode 3"));
        panel.setBackground(UIConstants.BACKGROUND_COLOR);

        label = new JLabel();
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                new ValueDialog(table);
                stopCellEditing();
            }
        });
        panel.add(label, "grow, pushx, h 19!");
    }

    @Override
    public boolean isCellEditable(EventObject evt) {
        if (evt == null) {
            return false;
        }
        if (evt instanceof MouseEvent) {
            return ((MouseEvent) evt).getClickCount() >= 2;
        }
        return true;
    }

    @Override
    public Object getCellEditorValue() {
        return text;
    }

    @Override
    public boolean stopCellEditing() {
        if (ObjectUtils.equals(getCellEditorValue(), originalValue)) {
            cancelCellEditing();
        } else {
            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
        }
        return super.stopCellEditing();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        panel.setBackground(table.getSelectionBackground());
        label.setBackground(panel.getBackground());
        label.setMaximumSize(new Dimension(table.getColumnModel().getColumn(column).getWidth(), 19));

        String text = (String) value;
        this.text = text;
        originalValue = text;
        label.setText(text);

        return panel;
    }

    private class ValueDialog extends MirthDialog {

        public ValueDialog(final JTable table) {
            super(PlatformUI.MIRTH_FRAME, true);
            setTitle("Value");
            setPreferredSize(new Dimension(600, 500));
            setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill", "", "[grow]7[]"));
            setBackground(UIConstants.BACKGROUND_COLOR);
            getContentPane().setBackground(getBackground());

            final MirthSyntaxTextArea textArea = new MirthSyntaxTextArea();
            textArea.setSaveEnabled(false);
            textArea.setText(text);
            textArea.setBorder(BorderFactory.createEtchedBorder());
            add(textArea, "grow");

            add(new JSeparator(), "newline, grow");

            JPanel buttonPanel = new JPanel(new MigLayout("insets 0, novisualpadding, hidemode 3"));
            buttonPanel.setBackground(getBackground());

            JButton openFileButton = new JButton("Open File...");
            openFileButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    String content = PlatformUI.MIRTH_FRAME.browseForFileString(null);
                    if (content != null) {
                        textArea.setText(content);
                    }
                }
            });
            buttonPanel.add(openFileButton);

            JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    text = textArea.getText();
                    label.setText(text);
                    table.getModel().setValueAt(text, table.getSelectedRow(), table.getSelectedColumn());
                    PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
                    dispose();
                }
            });
            buttonPanel.add(okButton);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    dispose();
                }
            });
            buttonPanel.add(cancelButton);

            add(buttonPanel, "newline, right");

            pack();
            setLocationRelativeTo(PlatformUI.MIRTH_FRAME);
            setVisible(true);
        }
    }
};
/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractButton;
import javax.swing.AbstractCellEditor;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import com.mirth.connect.client.ui.UIConstants;

public class MirthComboBoxTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    protected MirthComboBox comboBox;
    private int clickCount;
    private boolean editable;
    private JTable table;

    public MirthComboBoxTableCellEditor(JTable table, Object[] items, int clickCount, boolean focusable, final ActionListener actionListener) {
        this.clickCount = clickCount;
        this.table = table;

        comboBox = new MirthComboBox();
        comboBox.setModel(new DefaultComboBoxModel(items));
        comboBox.setFocusable(focusable);
        comboBox.setMaximumRowCount(20);
        comboBox.setForeground(table.getForeground());
        comboBox.setBackground(table.getBackground());
        comboBox.setRenderer(new DataTypeListCellRenderer());

        // Allow an action listener to be passed in. However, we need to fireEditingStopped after the action has finished so we wrap it in another listener
        comboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (actionListener != null) {
                    actionListener.actionPerformed(e);
                }
                fireEditingStopped();
            }

        });

        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4515838
        // Workaround to remove the border around the comboBox
        for (int i = 0; i < comboBox.getComponentCount(); i++) {
            if (comboBox.getComponent(i) instanceof AbstractButton) {
                ((AbstractButton) comboBox.getComponent(i)).setBorderPainted(false);
            }
        }
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent) anEvent).getClickCount() >= clickCount;
        }

        return false;
    }

    @Override
    public Object getCellEditorValue() {
        return editable ? comboBox.getEditor().getItem() : comboBox.getSelectedItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // Only the selected component will be edited, so always set the colors to the table's selection colors.
        comboBox.setForeground(table.getSelectionForeground());
        comboBox.setBackground(table.getSelectionBackground());

        for (int i = 0; i < comboBox.getComponentCount(); i++) {
            if (comboBox.getComponent(i) instanceof AbstractButton) {
                comboBox.getComponent(i).setBackground(comboBox.getBackground());
            }
        }

        comboBox.setSelectedItem(value);
        return comboBox;
    }
    
    public void setTooltips(String[] tooltips) {
        DataTypeListCellRenderer renderer = (DataTypeListCellRenderer)comboBox.getRenderer();
        renderer.setTooltips(tooltips);
    }

    @SuppressWarnings("serial")
    private class DataTypeListCellRenderer extends DefaultListCellRenderer {

        String[] tooltips;
        
        public DataTypeListCellRenderer() {}

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (index >= 0) {
                if (!isSelected) {
                    component.setBackground(UIConstants.BACKGROUND_COLOR);
                }
                if (value != null && tooltips != null && index < tooltips.length) {
                    list.setToolTipText(tooltips[index]);
                }
            }

            return component;
        }
        
        public void setTooltips(String[] tooltips) {
            this.tooltips = tooltips;
        }
    }

    public MirthComboBox getComboBox() {
        return comboBox;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;

        comboBox.setEditable(editable);

        if (editable) {
            comboBox.setEditor(new MirthComboBoxEditor(table));
        }
    }

    private class MirthComboBoxEditor implements ComboBoxEditor {
        private JTextField textfield;

        public MirthComboBoxEditor(final JTable table) {
            textfield = new JTextField();
            textfield.setEditable(true);
            textfield.setBackground(UIConstants.BACKGROUND_COLOR);

            textfield.addKeyListener(new KeyListener() {

                public void keyPressed(KeyEvent e) {}

                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        table.getCellEditor().stopCellEditing();
                    }
                }

                public void keyTyped(KeyEvent e) {}
            });
        }

        public void setItem(Object text) {
            textfield.setText("");

            if (text != null) {
                textfield.setText(text.toString());
            }
        }

        public Component getEditorComponent() {
            return textfield;
        }

        public Object getItem() {
            return textfield.getText();
        }

        public void selectAll() {
            textfield.selectAll();
        }

        public void addActionListener(ActionListener l) {
            textfield.addActionListener(l);
        }

        public void removeActionListener(ActionListener l) {
            textfield.removeActionListener(l);
        }
    }
}

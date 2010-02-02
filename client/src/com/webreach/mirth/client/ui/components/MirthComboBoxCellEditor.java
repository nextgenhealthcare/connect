package com.webreach.mirth.client.ui.components;

import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;

import javax.swing.JComboBox;

import com.webreach.mirth.client.ui.editors.MirthEditorPane;

public class MirthComboBoxCellEditor extends DefaultCellEditor {

    MirthEditorPane parent;

    public MirthComboBoxCellEditor(String[] items, MirthEditorPane pane) {
        super(new JComboBox(items));
        parent = pane;
    }

    /**
     * Enables the editor only for double-clicks.
     */
    public boolean isCellEditable(EventObject evt) {
        if (evt instanceof MouseEvent) {
            return ((MouseEvent) evt).getClickCount() >= 2;
        }

        return false;
    }
}

package com.webreach.mirth.client.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.webreach.mirth.client.ui.components.MirthSyntaxTextArea;

/** Allows for Undo in text components. */
public class UndoAction extends AbstractAction {

    MirthSyntaxTextArea comp;

    public UndoAction(MirthSyntaxTextArea comp) {
        super("Undo");
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
        comp.undo();
    }

    public boolean isEnabled() {
        return comp.isEnabled() && comp.canUndo();
    }
}

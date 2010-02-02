package com.webreach.mirth.client.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.webreach.mirth.client.ui.components.MirthSyntaxTextArea;

/** Allows for Redo in text components. */
public class RedoAction extends AbstractAction {

    MirthSyntaxTextArea comp;

    public RedoAction(MirthSyntaxTextArea comp) {
        super("Redo");
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
        comp.redo();
    }

    public boolean isEnabled() {
        return comp.isEnabled() && comp.canRedo();
    }
}

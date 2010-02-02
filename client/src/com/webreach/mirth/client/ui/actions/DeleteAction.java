package com.webreach.mirth.client.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.webreach.mirth.client.ui.components.MirthTextInterface;

/** Allows for Deleting in text components. */
public class DeleteAction extends AbstractAction {

    MirthTextInterface comp;

    public DeleteAction(MirthTextInterface comp) {
        super("Delete");
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
        comp.replaceSelection(null);
    }

    public boolean isEnabled() {
        return comp.isEditable() && comp.isEnabled() && comp.getSelectedText() != null;
    }
}

package com.webreach.mirth.client.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.webreach.mirth.client.ui.components.MirthTextInterface;

/** Allows for Cutting in text components. */
public class CutAction extends AbstractAction {

    MirthTextInterface comp;

    public CutAction(MirthTextInterface comp) {
        super("Cut");
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
        comp.cut();
    }

    public boolean isEnabled() {
        return comp.isEditable() && comp.isEnabled() && comp.getSelectedText() != null;
    }
}

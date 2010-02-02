package com.webreach.mirth.client.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.webreach.mirth.client.ui.components.MirthTextInterface;

/** Allows for Copying in text components. */
public class CopyAction extends AbstractAction {

    MirthTextInterface comp;

    public CopyAction(MirthTextInterface comp) {
        super("Copy");
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
        comp.copy();
    }

    public boolean isEnabled() {
        return comp.isEnabled() && comp.getSelectedText() != null;
    }
}

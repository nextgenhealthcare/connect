package com.webreach.mirth.client.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.webreach.mirth.client.ui.components.MirthTextInterface;

/** Allows for Selecting All in text components. */
public class SelectAllAction extends AbstractAction {

    MirthTextInterface comp;

    public SelectAllAction(MirthTextInterface comp) {
        super("Select All");
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
        comp.selectAll();
    }

    public boolean isEnabled() {
        return comp.isEnabled() && comp.getText().length() > 0;
    }
}

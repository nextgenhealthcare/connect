package com.webreach.mirth.client.ui.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.webreach.mirth.client.ui.components.MirthTextInterface;

/** Allows for Pasting in text components. */
public class PasteAction extends AbstractAction {

    MirthTextInterface comp;

    public PasteAction(MirthTextInterface comp) {
        super("Paste");
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
        comp.paste();
    }

    public boolean isEnabled() {
        if (comp.isVisible() && comp.isEditable() && comp.isEnabled()) {
            try {
                Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
                return contents.isDataFlavorSupported(DataFlavor.stringFlavor);
            } catch (IllegalStateException e) {
                return false;
            }
        } else {
            return false;
        }
    }
}

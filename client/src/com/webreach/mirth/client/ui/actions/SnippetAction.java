package com.webreach.mirth.client.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.webreach.mirth.client.ui.components.MirthSyntaxTextArea;

/** Allows for snippet insertion in code components. */
public class SnippetAction extends AbstractAction {

    MirthSyntaxTextArea comp;
    String snippet;

    public SnippetAction(MirthSyntaxTextArea comp, String label, String snippet) {
        super(label);
        this.comp = comp;
        this.snippet = snippet;
    }

    public void actionPerformed(ActionEvent e) {
        comp.setSelectedText(snippet);
    }

    public boolean isEnabled() {
        return comp.isEnabled() && comp.isEditable();
    }
}

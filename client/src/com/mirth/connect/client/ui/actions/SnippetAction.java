/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.mirth.connect.client.ui.components.MirthSyntaxTextArea;

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

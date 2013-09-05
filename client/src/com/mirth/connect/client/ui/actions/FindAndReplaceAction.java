/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.actions;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.syntax.jedit.JEditTextArea;

import com.mirth.connect.client.ui.FindRplDialog;

public class FindAndReplaceAction extends AbstractAction {

    JEditTextArea comp;
    FindRplDialog find;

    public FindAndReplaceAction(JEditTextArea comp) {
        super("Find/Replace");
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
        Window owner = SwingUtilities.windowForComponent(comp);

        if (owner instanceof Frame) {
            find = new FindRplDialog((Frame) owner, true, comp);
        } else { // window instanceof Dialog
            find = new FindRplDialog((Dialog) owner, true, comp);
        }

        find.setVisible(true);
    }

    public boolean isEnabled() {
        return comp.isEnabled();
    }
}

/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.syntax.jedit.JEditTextArea;

import com.mirth.connect.client.ui.Frame;

public class ShowLineEndingsAction extends AbstractAction {

    JEditTextArea textArea;
    Frame frame;

    public ShowLineEndingsAction(JEditTextArea textArea) {
        super("Show Line Endings");
        this.textArea = textArea;

    }

    public void actionPerformed(ActionEvent e) {
        if (this.textArea.isShowLineEndings()) {
            this.textArea.setShowLineEndings(false);

        } else {
            this.textArea.setShowLineEndings(true);
        }
    }

    public boolean isEnabled() {
        return this.textArea.isEnabled();
    }
}

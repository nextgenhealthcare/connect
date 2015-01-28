/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.actions;

import java.awt.event.ActionEvent;

import org.fife.ui.rtextarea.RTextArea;

import com.mirth.connect.client.ui.components.rsta.MirthRSyntaxTextArea;

public class InsertBreakAction extends org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit.InsertBreakAction {

    public InsertBreakAction(String lineBreak) {
        super(lineBreak);
        setProperties(MirthRSyntaxTextArea.getResourceBundle(), (lineBreak.equals("\r") ? ActionInfo.INSERT_CR_BREAK : ActionInfo.INSERT_LF_BREAK).toString());
    }

    @Override
    public void actionPerformedImpl(ActionEvent e, RTextArea textArea) {
        super.actionPerformedImpl(e, textArea);
        textArea.updateUI();
    }
}
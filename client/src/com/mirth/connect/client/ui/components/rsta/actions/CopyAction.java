/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.actions;

import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import com.mirth.connect.client.ui.components.rsta.MirthRSyntaxTextArea;

public class CopyAction extends MirthRecordableTextAction {

    public CopyAction(MirthRSyntaxTextArea textArea) {
        super(textArea, ActionInfo.COPY);
    }

    @Override
    public void actionPerformedImpl(ActionEvent evt) {
        if (textArea.getSelectionStart() != textArea.getSelectionEnd()) {
            textArea.getToolkit().getSystemClipboard().setContents(new StringSelection(textArea.getEOLFixedSelectedText()), null);
        }
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && textArea.getSelectedText() != null;
    }
}
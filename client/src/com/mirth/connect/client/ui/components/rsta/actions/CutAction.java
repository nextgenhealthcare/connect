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

public class CutAction extends MirthRecordableTextAction {

    public CutAction(MirthRSyntaxTextArea textArea) {
        super(textArea, ActionInfo.CUT);
    }

    @Override
    public void actionPerformedImpl(ActionEvent evt) {
        if (textArea.isEditable()) {
            textArea.getToolkit().getSystemClipboard().setContents(new StringSelection(textArea.getEOLFixedSelectedText()), null);
            textArea.replaceSelection("");
        }
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && textArea.isEditable() && textArea.getSelectedText() != null;
    }
}
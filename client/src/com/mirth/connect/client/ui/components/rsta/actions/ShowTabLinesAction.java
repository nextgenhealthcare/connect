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

import com.mirth.connect.client.ui.components.rsta.MirthRSyntaxTextArea;

public class ShowTabLinesAction extends MirthRecordableTextAction {

    public ShowTabLinesAction(MirthRSyntaxTextArea textArea) {
        super(textArea, ActionInfo.DISPLAY_SHOW_TAB_LINES);
    }

    @Override
    public void actionPerformedImpl(ActionEvent evt) {
        boolean result = !evt.getActionCommand().equals(FORCE_OFF_COMMAND) && (evt.getActionCommand().equals(FORCE_ON_COMMAND) || !textArea.getPaintTabLines());
        textArea.setPaintTabLines(result);
    }
}
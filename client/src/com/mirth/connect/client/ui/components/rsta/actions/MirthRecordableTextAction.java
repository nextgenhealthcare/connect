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
import org.fife.ui.rtextarea.RecordableTextAction;

import com.mirth.connect.client.ui.components.rsta.MirthRSyntaxTextArea;

public abstract class MirthRecordableTextAction extends RecordableTextAction {

    protected MirthRSyntaxTextArea textArea;

    public MirthRecordableTextAction(MirthRSyntaxTextArea textArea, String name) {
        super(name);
        this.textArea = textArea;
    }

    public abstract void actionPerformedImpl(ActionEvent evt);

    @Override
    public final void actionPerformedImpl(ActionEvent evt, RTextArea textArea) {
        actionPerformedImpl(evt);
    }

    @Override
    public String getMacroID() {
        return getName();
    }

    @Override
    public boolean isEnabled() {
        return textArea.isEnabled();
    }
}

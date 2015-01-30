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

    public static final String FORCE_ON_COMMAND = "forceOn";
    public static final String FORCE_OFF_COMMAND = "forceOff";

    protected MirthRSyntaxTextArea textArea;
    protected ActionInfo actionInfo;

    public MirthRecordableTextAction(MirthRSyntaxTextArea textArea, ActionInfo actionInfo) {
        super(null);
        this.textArea = textArea;
        this.actionInfo = actionInfo;
        setProperties(MirthRSyntaxTextArea.getResourceBundle(), actionInfo.toString());
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
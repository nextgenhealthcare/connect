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

import com.mirth.connect.client.ui.BareBonesBrowserLaunch;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.rsta.MirthRSyntaxTextArea;

public class ViewUserAPIAction extends MirthRecordableTextAction {

    public ViewUserAPIAction(MirthRSyntaxTextArea textArea) {
        super(textArea, ActionInfo.VIEW_USER_API);
    }

    @Override
    public void actionPerformedImpl(ActionEvent evt) {
        BareBonesBrowserLaunch.openURL(PlatformUI.SERVER_URL + UIConstants.USER_API_LOCATION);
    }
}
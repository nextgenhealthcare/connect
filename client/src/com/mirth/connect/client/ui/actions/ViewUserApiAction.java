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

import com.mirth.connect.client.ui.BareBonesBrowserLaunch;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthSyntaxTextArea;

public class ViewUserApiAction extends AbstractAction {

    MirthSyntaxTextArea comp;

    public ViewUserApiAction(MirthSyntaxTextArea comp) {
        super("View User API");
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
        BareBonesBrowserLaunch.openURL(PlatformUI.SERVER_NAME + UIConstants.USER_API_LOCATION);
    }

    public boolean isEnabled() {
        return comp.isEnabled();
    }
}

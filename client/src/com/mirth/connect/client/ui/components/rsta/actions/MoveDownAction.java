/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.actions;

import javax.swing.SwingConstants;

import com.mirth.connect.client.ui.components.rsta.MirthRSyntaxTextArea;

public class MoveDownAction extends org.fife.ui.rtextarea.RTextAreaEditorKit.NextVisualPositionAction {

    public MoveDownAction(boolean select) {
        super(null, select, SwingConstants.SOUTH);
        setProperties(MirthRSyntaxTextArea.getResourceBundle(), (select ? ActionInfo.MOVE_DOWN_SELECT : ActionInfo.MOVE_DOWN).toString());
    }
}
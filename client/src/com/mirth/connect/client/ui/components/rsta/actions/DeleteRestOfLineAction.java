/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.actions;

import com.mirth.connect.client.ui.components.rsta.MirthRSyntaxTextArea;

public class DeleteRestOfLineAction extends org.fife.ui.rtextarea.RTextAreaEditorKit.DeleteRestOfLineAction {

    public DeleteRestOfLineAction() {
        setProperties(MirthRSyntaxTextArea.getResourceBundle(), ActionInfo.DELETE_REST_OF_LINE.toString());
    }
}
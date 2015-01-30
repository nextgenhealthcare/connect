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

public class DocumentEndAction extends org.fife.ui.rtextarea.RTextAreaEditorKit.EndAction {

    public DocumentEndAction(boolean select) {
        super(null, select);
        setProperties(MirthRSyntaxTextArea.getResourceBundle(), (select ? ActionInfo.DOCUMENT_SELECT_END : ActionInfo.DOCUMENT_END).toString());
    }
}
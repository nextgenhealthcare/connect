/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import com.mirth.connect.client.ui.TextFieldCellEditor;

public class EditorTableCellEditor extends TextFieldCellEditor {
    MirthEditorPane parent;

    public EditorTableCellEditor(MirthEditorPane pane) {
        super();
        parent = pane;
    }

    protected boolean valueChanged(String value) {
        parent.modified = true;
        return true;
    }
}

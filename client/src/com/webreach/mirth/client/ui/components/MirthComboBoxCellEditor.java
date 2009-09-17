/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.client.ui.components;

import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;

import javax.swing.JComboBox;

import com.webreach.mirth.client.ui.editors.MirthEditorPane;

public class MirthComboBoxCellEditor extends DefaultCellEditor
{
    MirthEditorPane parent;

    public MirthComboBoxCellEditor(String[] items, MirthEditorPane pane)
    {
        super(new JComboBox(items));
        parent = pane;
    }

    /**
     * Enables the editor only for double-clicks.
     */
    public boolean isCellEditable(EventObject evt)
    {
        if (evt instanceof MouseEvent)
        {
            return ((MouseEvent) evt).getClickCount() >= 2;
        }

        return false;
    }
}

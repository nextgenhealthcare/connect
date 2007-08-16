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

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXDatePicker;

import com.webreach.mirth.client.ui.UIConstants;

/**
 * Mirth's implementation of the JXDatePicker. Sets the format, editor font, and
 * button image.
 */
public class MirthDatePicker extends JXDatePicker
{

    /**
     * Creates a new instance of MirthDatePicker
     */
    public MirthDatePicker()
    {
        super();
        this.setFocusable(true);
        setFormats(new String[] { "EEE MM-dd-yyyy" });
        getEditor().setFont(UIConstants.TEXTFIELD_PLAIN_FONT);
        UIManager.put("JXDatePicker.arrowDown.image", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/calendar_view_month.png")));
        // must call updateUI() so that the first mirthDatePicker uses this
        // button image.
        updateUI();
    }
}

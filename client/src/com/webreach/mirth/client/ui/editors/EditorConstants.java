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

package com.webreach.mirth.client.ui.editors;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;

public class EditorConstants
{

    // the default font for all editors
    public final static Font DEFAULT_FONT = new Font("Monospaced", Font.PLAIN, 12);

    public final static Font DEFAULT_FONT_BOLD = new Font("Monospaced", Font.BOLD, 12);

    // the colors for the line number margin
    public final static Color PANEL_BACKGROUND = (new JLabel()).getBackground();

    public final static Color LINENUMBER_FOREGROUND = new Color(119, 136, 153);

    // the default location of split pane dividers
    public final static int TABLE_DIVIDER_LOCATION = 200;

    public final static int TAB_PANEL_DIVIDER_LOCATION = 450;

}

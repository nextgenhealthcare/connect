/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;

public class EditorConstants {

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

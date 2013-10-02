/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Color;

import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;

import com.jgoodies.looks.plastic.PlasticScrollBarUI;
import com.jgoodies.looks.plastic.theme.SkyBluer;

public class MirthTheme extends SkyBluer {

    public String getName() {
        return "Mirth";
    }

    protected ColorUIResource getPrimary1() {
        return new ColorUIResource(Color.DARK_GRAY);
    }

    protected ColorUIResource getPrimary2() {
        return new ColorUIResource(0x9EB1C9);
    }

    protected ColorUIResource getPrimary3() {
        return new ColorUIResource(0xc0d2dc);
    }

    /*
     * protected ColorUIResource getSecondary1() { return
     * Colors.GRAY_MEDIUMDARK; }
     * 
     * protected ColorUIResource getSecondary2() { return Colors.GRAY_LIGHT; }
     * 
     * protected ColorUIResource getSecondary3() { return Colors.GRAY_LIGHTER; }
     * 
     * public ColorUIResource getMenuItemSelectedBackground() { return
     * getPrimary2(); }
     * 
     * public ColorUIResource getMenuItemSelectedForeground() { return
     * getWhite(); }
     * 
     * public ColorUIResource getMenuSelectedBackground() { return
     * getSecondary2(); }
     * 
     * public ColorUIResource getFocusColor() { return
     * PlasticLookAndFeel.useHighContrastFocusColors ? Colors.YELLOW_FOCUS :
     * super.getFocusColor(); }
     */
    /*
     * TODO: The following two lines are likely an improvement. However, they
     * require a rewrite of the PlasticInternalFrameTitlePanel. public
     * ColorUIResource getWindowTitleBackground() { return getPrimary1(); }
     * public ColorUIResource getWindowTitleForeground() { return WHITE; }
     */
    public void addCustomEntriesToTable(UIDefaults table) {
        super.addCustomEntriesToTable(table);
        Object[] uiDefaults = {PlasticScrollBarUI.MAX_BUMPS_WIDTH_KEY, new Integer(30),};
        table.putDefaults(uiDefaults);
    }
}

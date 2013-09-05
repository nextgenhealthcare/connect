/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXDatePicker;

import com.mirth.connect.client.ui.UIConstants;

/**
 * Mirth's implementation of the JXDatePicker. Sets the format, editor font, and
 * button image.
 */
public class MirthDatePicker extends JXDatePicker {

    /**
     * Creates a new instance of MirthDatePicker
     */
    public MirthDatePicker() {
        super();
        this.setFocusable(true);
        setFormats(new String[]{"EEE MM-dd-yyyy"});
        getEditor().setFont(UIConstants.TEXTFIELD_PLAIN_FONT);

        super.getMonthView().setMonthStringBackground(UIConstants.JX_CONTAINER_BACKGROUND_COLOR);
        super.getMonthView().setMonthStringForeground(UIConstants.HEADER_TITLE_TEXT_COLOR);
        UIManager.put("JXDatePicker.arrowIcon", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/calendar_view_month.png")));

        // old swingx 0.8 key
        // UIManager.put("JXDatePicker.arrowDown.image", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/calendar_view_month.png")));

        // must call updateUI() so that the first mirthDatePicker uses this
        // button image.
        updateUI();
    }
}

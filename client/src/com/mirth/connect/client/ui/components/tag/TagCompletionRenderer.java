/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.tag;

import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.util.ColorUtil;

public class TagCompletionRenderer extends DefaultListCellRenderer {
    private static String TAG_TYPE = "tag";
    private BufferedImage tagImage = ColorUtil.toBufferedImage(UIConstants.ICON_TAG_GRAY.getImage());

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        super.getListCellRendererComponent(list, value, index, selected, hasFocus);

        ImageIcon icon = null;
        if (value instanceof TagCompletion) {
            TagCompletion tagCompletion = (TagCompletion) value;
            icon = tagCompletion.getIcon();

            if (tagCompletion.getType().equals(TAG_TYPE)) {
                Color color = tagCompletion.getColor();
                icon = new ImageIcon(ColorUtil.tint(tagImage, color, Color.black));
            }
        }
        setIcon(icon);

        return this;
    }
}
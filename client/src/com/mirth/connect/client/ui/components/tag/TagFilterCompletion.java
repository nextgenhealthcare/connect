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

import javax.swing.ImageIcon;

import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.util.ColorUtil;

public class TagFilterCompletion implements FilterCompletion {
    private static ImageIcon icon = new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/tag_gray.png"));
    private static String type = "tag";
    private ChannelTag tag;

    public TagFilterCompletion(ChannelTag tag) {
        this.tag = tag;
    }

    @Override
    public String getName() {
        return tag.getName();
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Color getForegroundColor() {
        return ColorUtil.getForegroundColor(tag.getBackgroundColor());
    }

    @Override
    public Color getBackgroundColor() {
        return tag.getBackgroundColor();
    }

    @Override
    public ImageIcon getIcon() {
        return icon;
    }
}
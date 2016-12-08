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

import com.mirth.connect.client.ui.UIConstants;

public class ChannelNameFilterCompletion implements FilterCompletion {
    private static ImageIcon icon = new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/server.png"));
    private static String type = "channel";
    private String name;

    public ChannelNameFilterCompletion(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Color getForegroundColor() {
        return Color.black;
    }

    @Override
    public Color getBackgroundColor() {
        return UIConstants.JX_CONTAINER_BACKGROUND_COLOR;
    }

    @Override
    public ImageIcon getIcon() {
        return icon;
    }
}
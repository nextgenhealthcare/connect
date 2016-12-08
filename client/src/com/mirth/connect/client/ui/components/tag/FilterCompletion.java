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

public interface FilterCompletion {
    public String getName();

    public String getType();

    public Color getForegroundColor();

    public Color getBackgroundColor();

    public ImageIcon getIcon();
}
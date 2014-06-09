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
import java.awt.Component;

import javax.swing.ImageIcon;

public class ConnectorTypeDecoration {

    private String suffix;
    private ImageIcon icon;
    private String iconToolTipText;
    private Component iconPopupComponent;
    private Color highlightColor;

    public ConnectorTypeDecoration() {}

    public ConnectorTypeDecoration(String suffix, ImageIcon icon, String iconToolTipText, Component iconPopupComponent, Color highlightColor) {
        this.suffix = suffix;
        this.icon = icon;
        this.iconToolTipText = iconToolTipText;
        this.iconPopupComponent = iconPopupComponent;
        this.highlightColor = highlightColor;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public String getIconToolTipText() {
        return iconToolTipText;
    }

    public void setIconToolTipText(String iconToolTipText) {
        this.iconToolTipText = iconToolTipText;
    }

    public Component getIconPopupComponent() {
        return iconPopupComponent;
    }

    public void setIconPopupComponent(Component iconPopupComponent) {
        this.iconPopupComponent = iconPopupComponent;
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }
}

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

import com.mirth.connect.model.Connector.Mode;

public class ConnectorTypeDecoration {

    private Mode mode;
    private String suffix;
    private ImageIcon icon;
    private String iconToolTipText;
    private Component iconPopupComponent;
    private Color highlightColor;

    public ConnectorTypeDecoration(Mode mode) {
        this(mode, null, null, null, null, null);
    }

    public ConnectorTypeDecoration(Mode mode, String suffix, ImageIcon icon, String iconToolTipText, Component iconPopupComponent, Color highlightColor) {
        this.mode = mode;
        this.suffix = suffix;
        this.icon = icon;
        this.iconToolTipText = iconToolTipText;
        this.iconPopupComponent = iconPopupComponent;
        this.highlightColor = highlightColor;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
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

package com.webreach.mirth.client.ui;

import javax.swing.ImageIcon;

/**
 * Holds an ImageIcon and a String value. These are used for a cell that has an
 * image in it. This class has accessor methods to get and set these values.
 */
public class CellData {

    private ImageIcon icon;
    private String text;

    public CellData(ImageIcon icon, String text) {
        this.icon = icon;
        this.text = text;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString() {
        return text;
    }
}

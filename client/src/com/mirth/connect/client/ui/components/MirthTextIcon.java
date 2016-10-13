/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

public class MirthTextIcon implements Icon {

    private static final Color DEFAULT_BACKGROUND = new Color(220, 220, 220);

    private String text;
    private boolean includeSpace;
    private Color background;
    private Map<?, ?> desktopHints;
    private int width;
    private int widthWithSpace;
    private int height;

    public MirthTextIcon(JComponent component, String text) {
        this(component, text, false);
    }

    public MirthTextIcon(JComponent component, String text, boolean includeSpace) {
        this(component, text, includeSpace, null);
    }

    public MirthTextIcon(JComponent component, String text, boolean includeSpace, int background) {
        this(component, text, includeSpace, new Color(background));
    }

    public MirthTextIcon(JComponent component, String text, boolean includeSpace, Color background) {
        this.text = " " + text + " ";
        this.includeSpace = includeSpace;

        this.background = background != null ? background : component.getBackground();
        if (this.background == null) {
            this.background = DEFAULT_BACKGROUND;
        }

        this.desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");

        FontMetrics fm = component.getFontMetrics(component.getFont());
        this.width = fm.stringWidth(this.text);
        if (includeSpace) {
            this.widthWithSpace = fm.stringWidth(this.text + " ");
        }
        this.height = fm.getHeight();

        component.revalidate();
    }

    @Override
    public int getIconWidth() {
        return includeSpace ? widthWithSpace : width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        // Setup font
        Graphics2D g2 = (Graphics2D) g.create();
        if (desktopHints != null) {
            g2.addRenderingHints(desktopHints);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        g2.setFont(c.getFont());

        // Draw bubble
        if (background.equals(DEFAULT_BACKGROUND)) {
            g2.setColor(background);
        } else {
            g2.setColor(c.isEnabled() ? background : brighter(background));
        }
        g2.fillRoundRect(x, y, width, height + 1, 6, 6);

        // Draw text
        g2.translate(x, y + g2.getFontMetrics().getAscent());
        if (background.equals(DEFAULT_BACKGROUND) && c instanceof JTextComponent) {
            g2.setColor(c.isEnabled() ? c.getForeground() : ((JTextComponent) c).getDisabledTextColor());
        } else {
            g2.setColor(c.isEnabled() ? c.getForeground() : darker(background));
        }
        g2.drawString(text, 0, 0);

        g2.dispose();
    }

    private Color brighter(Color color) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        int alpha = color.getAlpha();

        float factor = 0.85f;
        int min = (int) (1.0 / (1.0 - factor));
        if (red == 0 && green == 0 && blue == 0) {
            red = min;
            green = min;
            blue = min;
        } else {
            red = transform(red, min, factor);
            green = transform(green, min, factor);
            blue = transform(blue, min, factor);
        }
        return new Color(red, green, blue, alpha);
    }

    private int transform(int value, int min, float factor) {
        if (value > 0 && value < min) {
            value = min;
        }
        return Math.min((int) (value / factor), 255);
    }

    private Color darker(Color color) {
        return color.darker().darker();
    }
}
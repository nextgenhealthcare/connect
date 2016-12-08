/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.tag;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;

public class TagLabel extends JLabel {
    private boolean decorate;

    public TagLabel() {
        decorate = false;
    }

    public void decorate(boolean decorate) {
        this.decorate = decorate;
    }

    @Override
    public void paintComponent(Graphics g) {
        if (decorate) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setFont(new Font("Tahoma", Font.PLAIN, 11));
            g2.setColor(getForeground());
            g2.drawString(getText(), 0, 12);
            g2.dispose();
        } else {
            super.paintComponent(g);
        }
    }
}

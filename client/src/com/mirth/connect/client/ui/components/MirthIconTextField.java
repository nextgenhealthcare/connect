/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

import org.apache.commons.lang3.StringUtils;

public class MirthIconTextField extends MirthTextField {

    private ImageIcon icon;
    private Insets insets;
    private String originalToolTipText;
    private String alternateToolTipText;
    private Component iconPopupMenuComponent;

    public MirthIconTextField() {
        this(null);
    }

    public MirthIconTextField(ImageIcon icon) {
        setIcon(icon);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (isIconActive(evt) && iconPopupMenuComponent != null) {
                    JPopupMenu iconPopupMenu = new JPopupMenu();
                    iconPopupMenu.insert(iconPopupMenuComponent, 0);
                    iconPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent evt) {
                if (isIconActive(evt)) {
                    if (StringUtils.isNotBlank(alternateToolTipText)) {
                        MirthIconTextField.super.setToolTipText(alternateToolTipText);
                    }

                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                } else {
                    if (StringUtils.isNotBlank(alternateToolTipText)) {
                        MirthIconTextField.super.setToolTipText(originalToolTipText);
                    }

                    setCursor(new Cursor(Cursor.TEXT_CURSOR));
                }
            }
        });
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    @Override
    public void setToolTipText(String text) {
        originalToolTipText = text;
        super.setToolTipText(text);
    }

    public void setAlternateToolTipText(String text) {
        alternateToolTipText = text;
    }

    public void setIconPopupMenuComponent(Component component) {
        iconPopupMenuComponent = component;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (insets == null) {
            insets = getMargin();
            if (insets == null) {
                insets = new Insets(0, 0, 0, 0);
            }
        }

        if (icon != null) {
            setMargin(new Insets(insets.top, insets.left + icon.getIconWidth(), insets.bottom, insets.right));
        } else {
            setMargin(insets);
        }

        super.paintComponent(g);
        if (icon != null) {
            g.drawImage(icon.getImage(), 2, (getHeight() - icon.getIconHeight()) / 2, this);
        }
    }

    private boolean isIconActive(MouseEvent evt) {
        return icon != null && evt.getX() < icon.getIconWidth();
    }
}

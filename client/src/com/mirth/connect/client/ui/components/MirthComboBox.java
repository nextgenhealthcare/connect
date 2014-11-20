/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;

/**
 * Mirth's implementation of the JComboBox. Adds enabling of the save button in
 * parent.
 */
public class MirthComboBox extends javax.swing.JComboBox {

    private Frame parent;
    private boolean autoResizeDropdown = false;
    private boolean canEnableSave = true;

    public MirthComboBox() {
        super();
        this.setFocusable(true);
        this.parent = PlatformUI.MIRTH_FRAME;
        this.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxChanged(evt);
            }
        });
        this.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                boolean isAccelerated = (((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) > 0) || ((e.getModifiers() & InputEvent.CTRL_MASK) > 0));
                if ((e.getKeyCode() == KeyEvent.VK_S) && isAccelerated) {
                    PlatformUI.MIRTH_FRAME.doContextSensitiveSave();
                }
            }

            public void keyReleased(KeyEvent e) {}

            public void keyTyped(KeyEvent e) {}
        });
    }

    public void comboBoxChanged(java.awt.event.ActionEvent evt) {
        if (canEnableSave) {
            parent.setSaveEnabled(true);
        }
    }

    public void setCanEnableSave(boolean canEnableSave) {
        this.canEnableSave = canEnableSave;
    }

    @Override
    public Dimension getSize() {
        Dimension dimension = super.getSize();

        if (autoResizeDropdown) {
            FontMetrics fontMetrics = getFontMetrics(this.getFont());
            int maxWidth = 0;

            for (int index = 0; index < this.getItemCount(); index++) {
                String entry = this.getItemAt(index).toString();
                int width = fontMetrics.stringWidth(entry);

                if (width > maxWidth) {
                    maxWidth = width;
                }
            }

            dimension.setSize(maxWidth > dimension.getWidth() ? maxWidth + 4 : dimension.getWidth(), dimension.getHeight());
        }

        return dimension;
    }

    public void setAutoResizeDropdown(boolean autoResizeDropdown) {
        this.autoResizeDropdown = autoResizeDropdown;
    }
}

/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

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

            public void keyReleased(KeyEvent e) {
                // TODO Auto-generated method stub
            }

            public void keyTyped(KeyEvent e) {
                // TODO Auto-generated method stub
            }
        });
    }

    public void comboBoxChanged(java.awt.event.ActionEvent evt) {
        parent.setSaveEnabled(true);
    }
}

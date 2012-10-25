/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.manager.components;

import com.mirth.connect.manager.ManagerController;
import com.mirth.connect.manager.PlatformUI;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Mirth's implementation of the JComboBox. Adds enabling of the apply button in
 * dialog.
 */
public class MirthComboBox extends javax.swing.JComboBox {

    public MirthComboBox() {
        super();
        this.setFocusable(true);
        this.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxChanged(evt);
            }
        });
        this.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                boolean isAccelerated = (e.getModifiers() & java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) > 0;
                if ((e.getKeyCode() == KeyEvent.VK_S) && isAccelerated) {
                    PlatformUI.MANAGER_DIALOG.saveProperties();
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
        ManagerController.getInstance().setApplyEnabled(true);
    }
}

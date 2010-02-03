/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.client.ui.components;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;

/**
 * Mirth's implementation of the JButton. Adds enabling of the save button in
 * parent.
 */
public class MirthButton extends javax.swing.JButton {

    private Frame parent;

    public MirthButton() {
        super();
        this.parent = PlatformUI.MIRTH_FRAME;
        this.setFocusable(true);
        this.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPressed(evt);
            }
        });
    }

    public void buttonPressed(java.awt.event.ActionEvent evt) {
        parent.enableSave();
    }
}

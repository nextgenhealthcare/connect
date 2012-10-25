/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;

/**
 * Mirth's implementation of the JCheckbox. Adds enabling of the save button in
 * parent.
 */
public class MirthCheckBox extends javax.swing.JCheckBox {

    private Frame parent;

    public MirthCheckBox() {
        super();
        this.setFocusable(true);
        this.parent = PlatformUI.MIRTH_FRAME;
        this.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxChanged(evt);
            }
        });
    }

    public void checkBoxChanged(java.awt.event.ActionEvent evt) {
        parent.setSaveEnabled(true);
    }
}

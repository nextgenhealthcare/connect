package com.webreach.mirth.client.ui.components;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;

/**
 * Mirth's implementation of the JRadioButton. Adds enabling of the save button
 * in parent.
 */
public class MirthRadioButton extends javax.swing.JRadioButton {

    private Frame parent;

    public MirthRadioButton() {
        super();
        this.setFocusable(true);
        this.parent = PlatformUI.MIRTH_FRAME;
        this.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonChanged(evt);
            }
        });
    }

    public void radioButtonChanged(java.awt.event.ActionEvent evt) {
        parent.enableSave();
    }
}

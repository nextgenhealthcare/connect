package com.webreach.mirth.client.ui.components;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;

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
        parent.enableSave();
    }
}

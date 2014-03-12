/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Dialog;
import java.awt.Frame;

import javax.swing.JDialog;

public class MirthDialog extends JDialog {

    public MirthDialog(Frame owner) {
        super(owner);
    }

    public MirthDialog(Dialog owner) {
        super(owner);
    }

    public MirthDialog(Frame owner, boolean modal) {
        super(owner, modal);
    }

    public MirthDialog(Dialog owner, boolean modal) {
        super(owner, modal);
    }

    @Override
    public void setVisible(boolean b) {
        PlatformUI.MIRTH_FRAME.setCanSave(!b);
        super.setVisible(b);
    }

    @Override
    public void dispose() {
        PlatformUI.MIRTH_FRAME.setCanSave(true);
        super.dispose();
    }
}

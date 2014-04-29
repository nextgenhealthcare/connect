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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

public class MirthDialog extends JDialog {
    public MirthDialog(Frame owner) {
        super(owner);
        registerCloseAction();
    }

    public MirthDialog(Dialog owner) {
        super(owner);
        registerCloseAction();
    }

    public MirthDialog(Frame owner, boolean modal) {
        super(owner, modal);
        registerCloseAction();
    }

    public MirthDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        registerCloseAction();
    }

    public MirthDialog(Dialog owner, boolean modal) {
        super(owner, modal);
        registerCloseAction();
    }
    
    public MirthDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
        registerCloseAction();
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
    
    public void registerCloseAction() {
        ActionListener closeAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    dispose();
            }
        };
        
        getRootPane().registerKeyboardAction(closeAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}

/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.manager.components;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import com.mirth.connect.manager.ManagerController;
import com.mirth.connect.manager.PlatformUI;

/**
 * Mirth's implementation of the JPasswordField. Adds enabling of the apply
 * button in dialog.
 */
public class MirthPasswordField extends javax.swing.JPasswordField {

    public MirthPasswordField() {
        super();
        this.setFocusable(true);
        this.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                boolean isAccelerated = (e.getModifiers() & java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) > 0;
                if ((e.getKeyCode() == KeyEvent.VK_S) && isAccelerated) {
                    PlatformUI.MANAGER_DIALOG.saveProperties();
                }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });
    }

    /**
     * Overrides setDocument(Document doc) so that a document listener is added
     * to the current document to listen for changes.
     */
    public void setDocument(Document doc) {
        super.setDocument(doc);

        this.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
            }

            public void removeUpdate(DocumentEvent e) {
                ManagerController.getInstance().setApplyEnabled(true);
            }

            public void insertUpdate(DocumentEvent e) {
                ManagerController.getInstance().setApplyEnabled(true);
            }
        });
    }

    /**
     * Overrides setText(String t) so that the save button is disabled when
     * Mirth sets the text of a field.
     */
    public void setText(String t) {
        super.setText(t);
        ManagerController.getInstance().setApplyEnabled(false);
    }
}

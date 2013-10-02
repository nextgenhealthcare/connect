/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPopupMenu;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.actions.CopyAction;
import com.mirth.connect.client.ui.actions.CutAction;
import com.mirth.connect.client.ui.actions.DeleteAction;
import com.mirth.connect.client.ui.actions.PasteAction;
import com.mirth.connect.client.ui.actions.SelectAllAction;

/**
 * Mirth's implementation of the JPasswordField. Adds enabling of the save
 * button in parent. Also adds a trigger button (right click) editor menu with
 * Cut, Copy, Paste, Delete, and Select All.
 */
public class MirthPasswordField extends javax.swing.JPasswordField implements MirthTextInterface {

    private Frame parent;
    private boolean visible = false;
    private JPopupMenu menu;
    private CutAction cutAction;
    private CopyAction copyAction;
    private PasteAction pasteAction;
    private DeleteAction deleteAction;
    private SelectAllAction selectAllAction;

    public boolean isVisible() {
        return visible;
    }

    public MirthPasswordField() {
        super();
        this.parent = PlatformUI.MIRTH_FRAME;
        this.setFocusable(true);

        cutAction = new CutAction(this);
        copyAction = new CopyAction(this);
        pasteAction = new PasteAction(this);
        deleteAction = new DeleteAction(this);
        selectAllAction = new SelectAllAction(this);

        menu = new JPopupMenu();
        menu.add(cutAction);
        menu.add(copyAction);
        menu.add(pasteAction);
        menu.add(deleteAction);
        menu.addSeparator();
        menu.add(selectAllAction);

        this.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                showPopupMenu(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                showPopupMenu(evt);
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
        visible = true;
    }

    /**
     * Shows the popup menu for the trigger button
     */
    private void showPopupMenu(java.awt.event.MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            menu.getComponent(0).setEnabled(cutAction.isEnabled());
            menu.getComponent(1).setEnabled(copyAction.isEnabled());
            menu.getComponent(2).setEnabled(pasteAction.isEnabled());
            menu.getComponent(3).setEnabled(deleteAction.isEnabled());
            menu.getComponent(5).setEnabled(selectAllAction.isEnabled());

            menu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
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
                parent.setSaveEnabled(true);
            }

            public void insertUpdate(DocumentEvent e) {
                parent.setSaveEnabled(true);
            }
        });
    }

    /**
     * Overrides setText(String t) so that the save button is disabled when
     * Mirth sets the text of a field.
     */
    public void setText(String t) {
        super.setText(t);
        parent.setSaveEnabled(false);
    }
}

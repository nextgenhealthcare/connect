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
import java.util.List;

import org.jdesktop.swingx.JXList;

import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.VariableListHandler;
import com.mirth.connect.client.ui.VariableListHandler.TransferMode;
import com.mirth.connect.model.Connector;

/**
 * An implementation of JXList that has mouse rollover selection implemented.
 */
public class MirthVariableList extends JXList {

    public MirthVariableList() {
        this(TransferMode.VELOCITY, null);
    }

    /**
     * Creates a new instance of MirthVariableList
     */
    public MirthVariableList(TransferMode transferMode, List<Connector> connectors) {
        super();
        this.setDragEnabled(true);
        this.setFocusable(false);
        this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {

            public void mouseMoved(java.awt.event.MouseEvent evt) {
                mirthListMouseMoved(evt);
            }
        });
        this.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseExited(java.awt.event.MouseEvent evt) {
                mirthListMouseExited(evt);
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

        setTransferHandler(new VariableListHandler(transferMode, connectors));
    }

    public void setTransferMode(TransferMode transferMode) {
        ((VariableListHandler) getTransferHandler()).setTransferMode(transferMode);
    }

    public void populateConnectors(List<Connector> connectors) {
        ((VariableListHandler) getTransferHandler()).populateConnectors(connectors);
    }

    /**
     * When leaving the variable list, the selection is cleared.
     */
    private void mirthListMouseExited(java.awt.event.MouseEvent evt) {
        this.clearSelection();
    }

    /**
     * When moving on the variable list, set the selection to whatever the mouse
     * is over.
     */
    private void mirthListMouseMoved(java.awt.event.MouseEvent evt) {
        int index = this.locationToIndex(evt.getPoint());

        if (index != -1) {
            this.setSelectedIndex(index);
        }
    }
}

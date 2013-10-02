/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.mirth.connect.client.ui.components.MirthTextInterface;

/** Allows for Selecting All in text components. */
public class SelectAllAction extends AbstractAction {

    MirthTextInterface comp;

    public SelectAllAction(MirthTextInterface comp) {
        super("Select All");
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
        comp.selectAll();
    }

    public boolean isEnabled() {
        return comp.isEnabled() && comp.getText().length() > 0;
    }
}

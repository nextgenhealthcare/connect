/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.plaf.InputMapUIResource;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaUI;

public class MirthRSyntaxTextAreaUI extends RSyntaxTextAreaUI {

    public MirthRSyntaxTextAreaUI(JComponent textArea) {
        super(textArea);
    }

    @Override
    protected InputMap getRTextAreaInputMap() {
        InputMap map = new InputMapUIResource();
        map.setParent(MirthInputMap.getInstance());
        return map;
    }
}
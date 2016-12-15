/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import java.awt.event.ActionListener;

import javax.swing.JPanel;

public abstract class EditorPanel<C> extends JPanel {

    public abstract C getDefaults();

    public abstract C getProperties();

    public abstract void setProperties(C properties);

    public abstract String checkProperties(C properties, boolean highlight);

    public abstract void resetInvalidProperties();

    public abstract void addNameActionListener(ActionListener actionListener);
}
/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import org.jdesktop.swingx.JXTaskPane;

import com.mirth.connect.client.ui.components.MirthTreeTable;

public abstract class TaskPlugin extends ClientPlugin {

    public TaskPlugin(String name) {
        super(name);
    }

    public abstract void onRowSelected(MirthTreeTable channelTable);

    public abstract void onRowDeselected();

    public abstract JXTaskPane getTaskPane();
}

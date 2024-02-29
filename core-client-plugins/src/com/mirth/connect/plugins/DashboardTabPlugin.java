/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import javax.swing.JComponent;

public abstract class DashboardTabPlugin extends DashboardPanelPlugin {
    public DashboardTabPlugin(String name) {
        super(name);
    }

    public abstract JComponent getTabComponent();
}

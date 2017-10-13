/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import com.mirth.connect.client.ui.components.MirthTreeTable;

public abstract class DashboardTablePlugin extends DashboardPanelPlugin {
    public DashboardTablePlugin(String name) {
        super(name);
    }

    /**
     * Creates a new MirthTreeTable object to use as the Dashboard table. This method will not be
     * invoked if the dashboard table was obtained from another DashboardTablePlugin with greater
     * weight.
     */
    public MirthTreeTable getTable() {
        return null;
    }

    /**
     * Returns a list of components to add to the toolbar below the Dashboard table. The table may
     * not be the same one returned by this plugin's getTable() method if it was obtained from
     * another DashboardTablePlugin with greater weight.
     */
    public List<JComponent> getToolbarComponents(MirthTreeTable table) {
        return new ArrayList<JComponent>();
    }

    /**
     * Invoked after the dashboard has been initialized. The table may not be the same one returned
     * by this plugin's getTable() method if it was obtained from another DashboardTablePlugin with
     * greater weight.
     */
    public void onDashboardInit(MirthTreeTable table) {}

    public String getServerId() {
        return null;
    }
}

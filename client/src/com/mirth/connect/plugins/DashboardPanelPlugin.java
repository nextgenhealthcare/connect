/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.util.List;

import javax.swing.JComponent;

import com.mirth.connect.model.DashboardStatus;

public abstract class DashboardPanelPlugin extends ClientPlugin {

    public DashboardPanelPlugin(String name) {
        super(name);
    }

    public abstract JComponent getComponent();

    // used for setting actions to be called for updating when there is no
    // status selected
    public abstract void update();

    // used for setting actions to be called for updating when there is a status
    // selected
    public abstract void update(List<DashboardStatus> statuses);

}

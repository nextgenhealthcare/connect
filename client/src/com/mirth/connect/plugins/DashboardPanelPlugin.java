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

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.model.DashboardStatus;

public abstract class DashboardPanelPlugin extends ClientPlugin {

    public DashboardPanelPlugin(String name) {
        super(name);
    }

    public void prepareData() throws ClientException {};

    public void prepareData(List<DashboardStatus> statuses) throws ClientException {};

    // used for setting actions to be called for updating when there is no status selected
    public abstract void update();

    // used for setting actions to be called for updating when there is a status selected
    public abstract void update(List<DashboardStatus> statuses);

}

/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.util.List;

import javax.swing.table.TableCellRenderer;

import com.mirth.connect.client.ui.DashboardPanel;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.model.ChannelStatus;

public abstract class DashboardColumnPlugin extends ClientPlugin {

    protected DashboardPanel parent;

    public DashboardColumnPlugin(String name) {
        super(name);
        this.parent = PlatformUI.MIRTH_FRAME.dashboardPanel;
    }

    public abstract String getColumnHeader();

    public abstract Object getTableData(ChannelStatus status);

    public abstract TableCellRenderer getCellRenderer();

    public abstract int getMaxWidth();

    public abstract int getMinWidth();

    public abstract boolean showBeforeStatusColumn();

    public abstract void tableUpdate(List<ChannelStatus> status);
}

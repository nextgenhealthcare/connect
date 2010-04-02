/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.plugins;

import java.util.List;

import javax.swing.table.TableCellRenderer;

import com.webreach.mirth.client.ui.DashboardPanel;
import com.webreach.mirth.model.ChannelStatus;

public abstract class DashboardColumnPlugin extends ClientPlugin {

    protected DashboardPanel parent;

    public DashboardColumnPlugin(String name) {
        super(name);
    }

    public DashboardColumnPlugin(String name, DashboardPanel parent) {
        super(name);
        this.parent = parent;
    }

    public abstract String getColumnHeader();

    public abstract Object getTableData(ChannelStatus status);

    public abstract TableCellRenderer getCellRenderer();

    public abstract int getMaxWidth();

    public abstract int getMinWidth();

    public abstract boolean showBeforeStatusColumn();

    public abstract void tableUpdate(List<ChannelStatus> status);
}

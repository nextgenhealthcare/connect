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

import com.mirth.connect.model.Channel;

public abstract class ChannelColumnPlugin extends ClientPlugin {

    public ChannelColumnPlugin(String name) {
        super(name);
    }

    public abstract String getColumnHeader();

    public abstract Object getTableData(Channel channel);

    public abstract TableCellRenderer getCellRenderer();

    public abstract int getMaxWidth();

    public abstract int getMinWidth();

    public abstract boolean isDisplayFirst();

    public abstract void tableUpdate(List<Channel> channels);
}

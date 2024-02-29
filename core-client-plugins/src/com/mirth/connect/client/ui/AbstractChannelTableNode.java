/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import com.mirth.connect.model.ChannelStatus;

public abstract class AbstractChannelTableNode extends AbstractSortableTreeTableNode {

    public abstract boolean isGroupNode();

    public abstract ChannelGroupStatus getGroupStatus();

    public abstract void setGroupStatus(ChannelGroupStatus groupStatus);

    public abstract ChannelStatus getChannelStatus();

    public abstract void setChannelStatus(ChannelStatus channelStatus);

    public abstract void updateStatusColumn();
}
/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import static com.mirth.connect.client.ui.ChannelPanel.DESCRIPTION_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.NAME_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.STATUS_COLUMN_NUMBER;

import javax.swing.ImageIcon;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelGroup;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.plugins.ChannelColumnPlugin;

public class ChannelTableNode extends AbstractChannelTableNode {

    private Object[] row;
    private ChannelGroupStatus groupStatus;
    private ChannelStatus channelStatus;

    public ChannelTableNode(ChannelGroupStatus groupStatus) {
        this(groupStatus, null);
    }

    public ChannelTableNode(ChannelStatus channelStatus) {
        this(null, channelStatus);
    }

    private ChannelTableNode(ChannelGroupStatus groupStatus, ChannelStatus channelStatus) {
        row = new Object[DashboardPanel.getNumberOfDefaultColumns() + LoadedExtensions.getInstance().getDashboardColumnPlugins().size()];

        if (groupStatus != null) {
            setGroupStatus(groupStatus);
        } else {
            setChannelStatus(channelStatus);
        }
    }

    @Override
    public boolean isGroupNode() {
        return groupStatus != null;
    }

    @Override
    public ChannelGroupStatus getGroupStatus() {
        return groupStatus;
    }

    @Override
    public void setGroupStatus(ChannelGroupStatus groupStatus) {
        this.groupStatus = groupStatus;
        this.channelStatus = null;
        ChannelGroup group = groupStatus.getGroup();

        int i = 0;

        for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
            if (plugin.isDisplayFirst()) {
                row[i++] = plugin.getTableData(group);
            }
        }

        updateStatusColumn();
        i++;

        row[i++] = null;
        row[i++] = new ChannelTableNameEntry(group.getName());
        row[i++] = group.getId();
        row[i++] = null;
        row[i++] = group.getDescription();
        row[i++] = null;
        row[i++] = null;
        row[i++] = group.getLastModified();

        for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
            if (!plugin.isDisplayFirst()) {
                row[i++] = plugin.getTableData(channelStatus.getChannel());
            }
        }
    }

    @Override
    public ChannelStatus getChannelStatus() {
        return channelStatus;
    }

    @Override
    public void setChannelStatus(ChannelStatus channelStatus) {
        this.groupStatus = null;
        this.channelStatus = channelStatus;
        Channel channel = channelStatus.getChannel();

        int i = 0;

        for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
            if (plugin.isDisplayFirst()) {
                row[i++] = plugin.getTableData(channelStatus.getChannel());
            }
        }

        if (channel.getExportData().getMetadata().isEnabled()) {
            row[i++] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_blue.png")), "Enabled");
        } else {
            row[i++] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), "Disabled");
        }
        row[i++] = PlatformUI.MIRTH_FRAME.dataTypeToDisplayName.get(channel.getSourceConnector().getTransformer().getInboundDataType());
        row[i++] = new ChannelTableNameEntry(channel.getName());
        row[i++] = channel.getId();
        row[i++] = channelStatus.getLocalChannelId();
        row[i++] = channel.getDescription();
        row[i++] = channelStatus.getDeployedRevisionDelta();
        row[i++] = channelStatus.getDeployedDate();
        row[i++] = channel.getExportData().getMetadata().getLastModified();

        for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
            if (!plugin.isDisplayFirst()) {
                row[i++] = plugin.getTableData(channelStatus.getChannel());
            }
        }
    }

    @Override
    public void updateStatusColumn() {
        if (isGroupNode()) {
            int offset = getColumnOffset();

            boolean allEnabled = true;
            boolean allDisabled = true;
            for (ChannelStatus channelStatus : groupStatus.getChannelStatuses()) {
                if (channelStatus.getChannel().getExportData().getMetadata().isEnabled()) {
                    allDisabled = false;
                } else {
                    allEnabled = false;
                }
            }

            if (groupStatus.getChannelStatuses().isEmpty()) {
                row[offset + STATUS_COLUMN_NUMBER] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), "N/A");
            } else if (allEnabled) {
                row[offset + STATUS_COLUMN_NUMBER] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_blue.png")), "Enabled");
            } else if (allDisabled) {
                row[offset + STATUS_COLUMN_NUMBER] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), "Disabled");
            } else {
                row[offset + STATUS_COLUMN_NUMBER] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_orange.png")), "Mixed");
            }
        }
    }

    @Override
    public int getColumnCount() {
        return row.length;
    }

    @Override
    public Object getValueAt(int i) {
        return row[i];
    }

    @Override
    public void setValueAt(Object value, int i) {
        row[i] = value;

        if (isGroupNode()) {
            int offset = getColumnOffset();

            if (i - offset == NAME_COLUMN_NUMBER) {
                groupStatus.getGroup().setName(((ChannelTableNameEntry) value).getName());
            } else if (i - offset == DESCRIPTION_COLUMN_NUMBER) {
                groupStatus.getGroup().setDescription((String) value);
            }
        }

        super.setValueAt(value, i);
    }

    @Override
    public String toString() {
        if (isGroupNode()) {
            return groupStatus.getGroup().getName();
        } else {
            return channelStatus.getChannel().getName();
        }
    }

    private int getColumnOffset() {
        int offset = 0;
        for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
            if (plugin.isDisplayFirst()) {
                offset++;
            }
        }

        return offset;
    }
}
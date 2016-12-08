/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import static com.mirth.connect.client.ui.ChannelPanel.DATA_TYPE_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.DEPLOYED_REVISION_DELTA_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.DESCRIPTION_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.ID_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.LAST_DEPLOYED_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.LAST_MODIFIED_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.LOCAL_CHANNEL_ID_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.NAME_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.STATUS_COLUMN_NUMBER;

import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingConstants;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

import com.mirth.connect.plugins.ChannelColumnPlugin;

public class ChannelTableColumnFactory extends ColumnFactory {

    private int colOffset;
    private Map<Integer, ChannelColumnPlugin> plugins = new HashMap<Integer, ChannelColumnPlugin>();

    public ChannelTableColumnFactory() {
        // map column indices to the appropriate plug-ins
        int i = 0;

        for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
            if (plugin.isDisplayFirst()) {
                plugins.put(i++, plugin);
            }
        }

        colOffset = i;

        i += ChannelPanel.getNumberOfDefaultColumns();

        for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
            if (!plugin.isDisplayFirst()) {
                plugins.put(i++, plugin);
            }
        }
    }

    @Override
    public TableColumnExt createAndConfigureTableColumn(TableModel model, int modelIndex) {
        TableColumnExt column = super.createAndConfigureTableColumn(model, modelIndex);

        // the colOffset tells us where the normal columns begin, after any columns added by plugin(s)
        int index = modelIndex - colOffset;

        switch (index) {
            case STATUS_COLUMN_NUMBER:
                column.setMaxWidth(UIConstants.MIN_WIDTH);
                column.setMinWidth(UIConstants.MIN_WIDTH);
                column.setCellRenderer(new ImageCellRenderer());
                column.setToolTipText("<html><body>The status of this channel. Possible values are enabled and disabled.<br>Only enabled channels can be deployed.</body></html>");
                break;

            case DATA_TYPE_COLUMN_NUMBER:
                column.setMaxWidth(UIConstants.MIN_WIDTH);
                column.setMinWidth(UIConstants.MIN_WIDTH);
                column.setToolTipText("<html><body>The inbound data type of this channel's source connector.</body></html>");
                break;

            case NAME_COLUMN_NUMBER:
                column.setMinWidth(150);
                column.setToolTipText("<html><body>The name of this channel.</body></html>");
                break;

            case ID_COLUMN_NUMBER:
                column.setMinWidth(215);
                column.setMaxWidth(215);
                column.setToolTipText("<html><body>The unique id of this channel.</body></html>");
                break;

            case LOCAL_CHANNEL_ID_COLUMN_NUMBER:
                column.setMinWidth(60);
                column.setMaxWidth(60);
                column.setCellRenderer(new NumberCellRenderer(SwingConstants.CENTER, false));
                column.setToolTipText("<html><body>The local id of this channel used as part of the names for the message tables.</body></html>");
                break;

            case DESCRIPTION_COLUMN_NUMBER:
                column.setMinWidth(UIConstants.MIN_WIDTH);
                column.setToolTipText("<html><body>The description of this channel.</body></html>");
                break;

            case DEPLOYED_REVISION_DELTA_COLUMN_NUMBER:
                column.setMaxWidth(50);
                column.setMinWidth(50);
                column.setCellRenderer(new NumberCellRenderer(SwingConstants.CENTER, false));
                column.setResizable(false);
                column.setToolTipText("<html><body>The number of times this channel was saved since it was deployed.<br>Rev \u0394 = Channel Revision - Deployed Revision<br>This value will be highlighted if it is greater than 0,<br/><b>or</b> if any code templates linked to this channel have changed.</body></html>");
                break;

            case LAST_DEPLOYED_COLUMN_NUMBER:
                column.setMinWidth(95);
                column.setMaxWidth(95);
                column.setCellRenderer(new DateCellRenderer());
                column.setResizable(false);
                column.setToolTipText("<html><body>The time this channel was last deployed.<br>This value will be highlighted if it is within the last two minutes.</body></html>");
                break;

            case LAST_MODIFIED_COLUMN_NUMBER:
                column.setMinWidth(95);
                column.setMaxWidth(95);
                column.setCellRenderer(new DateCellRenderer());
                column.setResizable(false);
                column.setToolTipText("<html><body>The time this channel was last modified.</body></html>");
                break;

            default:
                ChannelColumnPlugin plugin = plugins.get(modelIndex);

                if (plugin != null) {
                    column.setCellRenderer(plugin.getCellRenderer());
                    column.setMaxWidth(plugin.getMaxWidth());
                    column.setMinWidth(plugin.getMinWidth());
                }
                break;
        }

        return column;
    }
}
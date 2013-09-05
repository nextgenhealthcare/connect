/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingConstants;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

import com.mirth.connect.plugins.DashboardColumnPlugin;

public class DashboardTableColumnFactory extends ColumnFactory {
    private int colOffset;
    private Map<Integer, DashboardColumnPlugin> plugins = new HashMap<Integer, DashboardColumnPlugin>();

    public DashboardTableColumnFactory() {
        // map column indices to the appropriate plug-ins
        int i = 0;

        for (DashboardColumnPlugin plugin : LoadedExtensions.getInstance().getDashboardColumnPlugins().values()) {
            if (plugin.isDisplayFirst()) {
                plugins.put(i++, plugin);
            }
        }

        colOffset = i;

        i += DashboardPanel.getNumberOfDefaultColumns();

        for (DashboardColumnPlugin plugin : LoadedExtensions.getInstance().getDashboardColumnPlugins().values()) {
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
            case 0:
                column.setCellRenderer(new ImageCellRenderer());
                column.setMaxWidth(UIConstants.MIN_WIDTH);
                column.setMinWidth(UIConstants.MIN_WIDTH);
                column.setToolTipText("<html><body>The status of the deployed channel. Possible values are started, paused, and stopped.</body></html>");
                break;

            case 1:
                column.setMinWidth(150);
                column.setToolTipText("<html><body>The name of the deployed channel or connector.</body></html>");
                break;

            case 2:
                column.setCellRenderer(new NumberCellRenderer(SwingConstants.CENTER, false));
                column.setComparator(new NumberCellComparator());
                column.setMaxWidth(50);
                column.setMinWidth(50);
                column.setToolTipText("<html><body>The number of times this channel was saved since it was deployed.<br>Rev \u0394 = Channel Revision - Deployed Revision<br>This value will be highlighted if it is greater than 0.</body></html>");
                break;

            case 3:
                column.setCellRenderer(new DateCellRenderer());
                column.setMaxWidth(95);
                column.setMinWidth(95);
                column.setToolTipText("<html><body>The time this channel was last deployed.<br>This value will be highlighted if it is within the last two minutes.</body></html>");
                break;

            case 4:
                column.setCellRenderer(new NumberCellRenderer());
                column.setComparator(new NumberCellComparator());
                column.setMaxWidth(UIConstants.MIN_WIDTH);
                column.setMinWidth(UIConstants.MIN_WIDTH);
                column.setToolTipText("<html><body>The number of messages received and accepted by this channel's source connector.</body></html>");
                break;

            case 5:
                column.setCellRenderer(new NumberCellRenderer());
                column.setComparator(new NumberCellComparator());
                column.setMaxWidth(UIConstants.MIN_WIDTH);
                column.setMinWidth(UIConstants.MIN_WIDTH);
                column.setToolTipText("<html><body>The number of messages filtered out by this channel's source connector or any destination connector.</body></html>");
                break;

            case 6:
                column.setCellRenderer(new NumberCellRenderer());
                column.setComparator(new NumberCellComparator());
                column.setMaxWidth(UIConstants.MIN_WIDTH);
                column.setMinWidth(UIConstants.MIN_WIDTH);
                column.setToolTipText("<html><body>The number of messages currently queued by all destination connectors in this channel.</body></html>");
                break;

            case 7:
                column.setCellRenderer(new NumberCellRenderer());
                column.setComparator(new NumberCellComparator());
                column.setMaxWidth(UIConstants.MIN_WIDTH);
                column.setMinWidth(UIConstants.MIN_WIDTH);
                column.setToolTipText("<html><body>The numer of messages that have been sent by all of the destination connectors in this channel.</body></html>");
                break;

            case 8:
                column.setCellRenderer(new NumberCellRenderer());
                column.setComparator(new NumberCellComparator());
                column.setMaxWidth(UIConstants.MIN_WIDTH);
                column.setMinWidth(UIConstants.MIN_WIDTH);
                column.setToolTipText("<html><body>The number of messages that errored in this channel.<br>This value will be highlighted if it is greater than 0.</body></html>");
                break;

            default:
                DashboardColumnPlugin plugin = plugins.get(modelIndex);

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

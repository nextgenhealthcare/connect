/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dashboardstatus;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.table.TableCellRenderer;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.UnauthorizedException;
import com.mirth.connect.client.ui.CellData;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.ImageCellRenderer;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.plugins.DashboardColumnPlugin;

public class DashboardConnectorStatusColumn extends DashboardColumnPlugin {

    private static final String _SOURCE_CONNECTOR = "_0";
    private static final String GET_STATES = "getStates";
    private static final String DASHBOARD_SERVICE_PLUGINPOINT = "Dashboard Connector Service";
    private Map<String, Object[]> currentStates;
    private ImageIcon greenBullet;
    private ImageIcon yellowBullet;
    private ImageIcon redBullet;
    private ImageIcon blackBullet;
    private Map<Integer, ImageIcon> iconMap = new HashMap<Integer, ImageIcon>();

    public DashboardConnectorStatusColumn(String name) {
        super(name);
        greenBullet = new ImageIcon(Frame.class.getResource("images/bullet_green.png"));
        yellowBullet = new ImageIcon(Frame.class.getResource("images/bullet_yellow.png"));
        redBullet = new ImageIcon(Frame.class.getResource("images/bullet_red.png"));
        blackBullet = new ImageIcon(Frame.class.getResource("images/bullet_black.png"));
        iconMap.put(Color.green.getRGB(), greenBullet);
        iconMap.put(Color.yellow.getRGB(), yellowBullet);
        iconMap.put(Color.red.getRGB(), redBullet);
        iconMap.put(Color.black.getRGB(), blackBullet);
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new ImageCellRenderer();
    }

    @Override
    public String getColumnHeader() {
        return "Connection";
    }

    @Override
    public int getMaxWidth() {
        return 104;
    }

    @Override
    public int getMinWidth() {
        return 104;
    }

    @Override
    public Object getTableData(String channelId) {
        String connectorName = channelId + _SOURCE_CONNECTOR;

        if (currentStates != null && currentStates.containsKey(connectorName)) {
            Object[] stateData = currentStates.get(connectorName);
            ImageIcon icon = iconMap.get(((Color) stateData[0]).getRGB());
            return new CellData(icon, "<html>" + (String) stateData[1] + "</html>");
        } else {
            return new CellData(blackBullet, "Unknown");
        }
    }

    @Override
    public Object getTableData(String channelId, Integer metaDataId) {
        String connectorName = channelId + "_" + (metaDataId == null ? 0 : metaDataId);

        if (currentStates != null && currentStates.containsKey(connectorName)) {
            Object[] stateData = currentStates.get(connectorName);
            ImageIcon icon = iconMap.get(((Color) stateData[0]).getRGB());
            return new CellData(icon, "<html>" + (String) stateData[1] + "</html>");
        } else {
            return new CellData(blackBullet, "Unknown");
        }
    }

    @Override
    public boolean isDisplayFirst() {
        return false;
    }

    @Override
    public void tableUpdate(List<DashboardStatus> status) {
        // get states from server
        try {
            currentStates = (HashMap<String, Object[]>) PlatformUI.MIRTH_FRAME.mirthClient.invokePluginMethod(DASHBOARD_SERVICE_PLUGINPOINT, GET_STATES, null);
        } catch (ClientException e) {
            if (e.getCause() instanceof UnauthorizedException) {
                currentStates = null;
            } else {
                PlatformUI.MIRTH_FRAME.alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
            }
            // we can safely ignore this
            // e.printStackTrace();
        }
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}

    @Override
    public String getPluginPointName() {
        return "Dashboard Status Column";
    }
}

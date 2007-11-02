/*
 * DashboardConnectorStatusClient.java
 *
 * Created on October 10, 2007, 3:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.plugins.dashboardconnectorstatus;

import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.plugins.DashboardPanelPlugin;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.core.ClientException;

import java.util.LinkedList;

/**
 *
 * @author chrisr
 */
public class DashboardConnectorStatusClient extends DashboardPanelPlugin {

    private DashboardConnectorStatusPanel dcsp;
    private LinkedList<String[]> connectionInfoLogs;
    private static final String GET_CONNECTION_INFO_LOGS = "getConnectionInfoLogs";
    private static final String SERVER_PLUGIN_NAME = "Dashboard Status Column Server";

    
    /** Creates a new instance of DashboardConnectorStatusClient */
    public DashboardConnectorStatusClient(String name)
    {
        super(name);
        dcsp = new DashboardConnectorStatusPanel();
        setComponent(dcsp);

    }


    // used for setting actions to be called for updating when there is no status selected
    public void update() {

        // this method is called when no channel is selected.  Display no info.
        dcsp.updateTable(null);

    }

       
    // used for setting actions to be called for updating when there is a status selected    
    public void update(ChannelStatus status) {


        //get states from server
		try {

            this.connectionInfoLogs = (LinkedList<String[]>) PlatformUI.MIRTH_FRAME.mirthClient.invokePluginMethod(SERVER_PLUGIN_NAME, GET_CONNECTION_INFO_LOGS, status.getName());

        } catch (ClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // call updateLogTextArea.
        dcsp.updateTable(connectionInfoLogs);

    }

}

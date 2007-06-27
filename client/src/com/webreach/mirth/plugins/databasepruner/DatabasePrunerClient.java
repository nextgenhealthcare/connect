/*
 * DatabasePrunerClient.java
 *
 * Created on June 22, 2007, 5:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.plugins.databasepruner;

import java.util.Properties;

import javax.swing.ImageIcon;

import com.webreach.mirth.plugins.ClientPlugin;

/**
 *
 * @author brendanh
 */
public class DatabasePrunerClient extends ClientPlugin
{
    Properties properties;
    
    public DatabasePrunerClient(String name)
    {
        super(name);
        
        getTaskPane().setTitle("Chris");
        setComponent(new DatabasePrunerPanel());
        addTask("doRefresh", "Refresh", "Refresh pruner properties", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        getComponent().addMouseListener(getPopupMenuMouseAdapter());
    }
    
    public void doRefresh()
    {
        alertInformation("refreshed");
    }
    
    public void start()
    {
        properties = getPropertiesFromServer();
        ((DatabasePrunerPanel)getComponent()).setInterval(properties.getProperty("sleepInterval"));
    }

    public void stop()
    {
        properties.getProperty("sleepInterval", ((DatabasePrunerPanel)getComponent()).getInterval());
        setPropertiesToServer(properties);
    }
}

/*
 * DashboardPanelPlugin.java
 *
 * Created on August 1, 2007, 6:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.plugins;

import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.ChannelStatus;

/**
 *
 * @author brendanh
 */
public abstract class DashboardPanelPlugin extends ClientPlugin
{
    private JComponent component = new JPanel();
    
    public DashboardPanelPlugin(String name)
    {
        super(name);       
    }

    public void setComponent(JComponent component)
    {
        this.component = component;
    }

    public JComponent getComponent()
    {
        return component;
    }
            
    public void alertException(StackTraceElement[] strace, String message)
    {
        parent.alertException(strace, message);
    }

    public void alertWarning(String message)
    {
        parent.alertWarning(message);
    }

    public void alertInformation(String message)
    {
        parent.alertInformation(message);
    }

    public void alertError(String message)
    {
        parent.alertError(message);
    }

    public boolean alertOkCancel(String message)
    {
        return parent.alertOkCancel(message);
    }

    public boolean alertOption(String message)
    {
        return parent.alertOption(message);
    }
    
    public void setWorking(String message, boolean working)
    {
        parent.setWorking(message, working);
    }

    public Properties getPropertiesFromServer() throws ClientException
    {
        return parent.mirthClient.getPluginProperties(name);
    }

    public void setPropertiesToServer(Properties properties) throws ClientException
    {
        parent.mirthClient.setPluginProperties(name, properties);
    }
        
    // used for setting actions to be called for updating when there is no status selected
    public abstract void update();
    
    // used for setting actions to be called for updating when there is a status selected
    public abstract void update(ChannelStatus status);
    
    public Object invoke (String method, Object object) throws ClientException
    {
        return parent.mirthClient.invokePluginMethod(name, method, object);
    }

    // used for starting processes in the plugin when the program is started
    public abstract void start();

    // used for stopping processes in the plugin when the program is exited
    public abstract void stop();
}

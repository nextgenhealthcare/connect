/*
 * DashboardPanelPlugin.java
 *
 * Created on August 1, 2007, 6:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.plugins;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.model.ChannelStatus;
import java.util.Properties;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author brendanh
 */
public abstract class DashboardPanelPlugin
{
    private JComponent component = new JPanel();
    private String name;
    private Frame parent = PlatformUI.MIRTH_FRAME;
    
     public DashboardPanelPlugin(String name)
    {
        this.name = name;
    }

    public void setComponent(JComponent component)
    {
        this.component = component;
    }

    public JComponent getComponent()
    {
        return component;
    }
    
    public String getName()
    {
        return name;
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
    
    // used for starting processes in the plugin when the program is exited
    public abstract void start();
    
    // used for stopping processes in the plugin when the program is exited
    public abstract void stop();
    
    // used for setting actions to be called when the plugin tab is loaded
    public abstract void display();
    
    // used for setting actions to be called when the plugin tab is loaded
    public abstract void display(ChannelStatus status);
    
    public Object invoke (String method, Object object) throws ClientException
    {
        return parent.mirthClient.invokePluginMethod(name, method, object);
    }
}

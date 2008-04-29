/*
 * ClientPlugin.java
 *
 * Created on June 22, 2007, 4:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.plugins;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.action.ActionFactory;
import org.jdesktop.swingx.action.BoundAction;

import com.webreach.mirth.client.core.ClientException;

/**
 *
 * @author brendanh
 */
public abstract class ClientPanelPlugin extends ClientPlugin
{
    private JComponent component = new JPanel();
    private JXTaskPane pane = new JXTaskPane();
    private JPopupMenu menu = new JPopupMenu();
    
    public ClientPanelPlugin(String name)
    {
        this.name = name;
        getTaskPane().setTitle(name + " Tasks");
        getTaskPane().setFocusable(false);
    }
    
    public void setComponent(JComponent component)
    {
        this.component = component;
    }
    
    public void setTaskPane(JXTaskPane pane)
    {
        this.pane = pane;
    }
    
    public void setPopupMenu(JPopupMenu menu)
    {
        this.menu = menu;
    }
    
    public JComponent getComponent()
    {
        return component;
    }
    
    public JXTaskPane getTaskPane()
    {
        return pane;
    }
    
    public JPopupMenu getPopupMenu()
    {
        return menu;
    }
    
    public Object invoke(String method, Object object) throws ClientException
    {
        return parent.mirthClient.invokePluginMethod(name, method, object);
    }
    
    public MouseAdapter getPopupMenuMouseAdapter()
    {
        return new MouseAdapter()
        {
            public void mousePressed(MouseEvent evt)
            {
                if (evt.isPopupTrigger())
                    getPopupMenu().show(evt.getComponent(), evt.getX(), evt.getY());
            }
            
            public void mouseReleased(MouseEvent evt)
            {
                if (evt.isPopupTrigger())
                    getPopupMenu().show(evt.getComponent(), evt.getX(), evt.getY());
            }
        };
    }
    
    /**
     * Initializes the bound method call for the task pane actions and adds them
     * to the taskpane/popupmenu.
     */
    public void addTask(String callbackMethod, String displayName, String toolTip, String shortcutKey, ImageIcon icon)
    {
        BoundAction boundAction = ActionFactory.createBoundAction(callbackMethod, displayName, shortcutKey);
        
        if (icon != null)
            boundAction.putValue(Action.SMALL_ICON, icon);
        boundAction.putValue(Action.SHORT_DESCRIPTION, toolTip);
        boundAction.registerCallback(this, callbackMethod);
        
        getTaskPane().add(boundAction);
        getPopupMenu().add(boundAction);
    }
    
    public void setVisibleTasks(int start, int end, boolean visible)
    {
        parent.setVisibleTasks(getTaskPane(), getPopupMenu(), start, end, visible);
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
    
    // used for starting processes in the plugin when the program is started
    public abstract void start();

    // used for stopping processes in the plugin when the program is exited
    public abstract void stop();
    
    // used for setting actions to be called when the plugin tab is loaded
    public abstract void display();
}

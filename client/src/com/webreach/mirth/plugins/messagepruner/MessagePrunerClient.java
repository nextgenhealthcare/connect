/*
 * MessagePrunerClient.java
 *
 * Created on June 22, 2007, 5:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.plugins.messagepruner;

import java.util.LinkedList;
import java.util.Properties;

import javax.swing.ImageIcon;

import org.jdesktop.swingworker.SwingWorker;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.plugins.ClientPanelPlugin;

/**
 * 
 * @author brendanh
 */
public class MessagePrunerClient extends ClientPanelPlugin
{
    public MessagePrunerClient(String name)
    {
        super(name);

        getTaskPane().setTitle("Pruner Tasks");
        setComponent(new MessagePrunerPanel());
        addTask("doRefresh", "Refresh", "Refresh pruner properties", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        addTask("doSave", "Save", "Save pruner properties", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png")));
        getComponent().addMouseListener(getPopupMenuMouseAdapter());
    }

    public void doRefresh()
    {
        setWorking("Loading pruner properties...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                refresh();
                return null;
            }

            public void done()
            {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doSave()
    {
        setWorking("Saving pruner properties...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                save();
                return null;
            }

            public void done()
            {
                setWorking("", false);
            }
        };

        worker.execute();
    }
    
    public void refresh()
    {
        try
        {
            ((MessagePrunerPanel) getComponent()).setProperties(getPropertiesFromServer(), (LinkedList<String[]>) invoke("getLog", null));
        }
        catch (ClientException e)
        {
            //alertException(e.getStackTrace(), "Could not get " + getName() + " properties.");
        }
    }
    
    public void save()
    {
        try
        {
            setPropertiesToServer(((MessagePrunerPanel) getComponent()).getProperties());
        }
        catch (ClientException e)
        {
           //alertException(e.getStackTrace(), "Could not set " + getName() + " properties.");
        }
    }
    
    public void start()
    {
        refresh();
    }

    public void stop()
    {
        save();
    }
    
    public void display()
    {
        refresh();
    }
}

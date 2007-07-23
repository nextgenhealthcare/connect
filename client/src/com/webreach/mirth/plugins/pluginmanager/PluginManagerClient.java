/*
 * PluginManagerClient.java
 *
 * Created on June 22, 2007, 5:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.plugins.pluginmanager;


import javax.swing.ImageIcon;

import org.jdesktop.swingworker.SwingWorker;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.plugins.ClientPlugin;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import sun.misc.BASE64Encoder;

/**
 *
 * @author brendanh
 */
public class PluginManagerClient extends ClientPlugin
{
    public PluginManagerClient(String name)
    {
        super(name);
        
        getTaskPane().setTitle("Manager Tasks");
        setComponent(new PluginManagerPanel(this));
        addTask("doRefresh", "Refresh", "Refresh loaded plugins.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        addTask("doSave", "Save", "Save plugin settings.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png")));
        getComponent().addMouseListener(getPopupMenuMouseAdapter());
    }
    
    public void doRefresh()
    {
        setWorking("Loading plugin settings...", true);
        
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
        setWorking("Saving plugin settings...", true);
        
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
                alertInformation("A restart is required before your changes will take effect.");
            }
        };
        
        worker.execute();
    }
    
    public void refresh()
    {
        try
        {
            ((PluginManagerPanel) getComponent()).setPluginData(PlatformUI.MIRTH_FRAME.mirthClient.getPluginMetaData());
            ((PluginManagerPanel) getComponent()).setConnectorData(PlatformUI.MIRTH_FRAME.mirthClient.getConnectorMetaData());
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
            PlatformUI.MIRTH_FRAME.mirthClient.setPluginMetaData(((PluginManagerPanel) getComponent()).getPluginData());
            PlatformUI.MIRTH_FRAME.mirthClient.setConnectorMetaData(((PluginManagerPanel) getComponent()).getConnectorData());
        }
        catch (ClientException e)
        {
            //alertException(e.getStackTrace(), "Could not set " + getName() + " properties.");
        }
    }
    
    public boolean install(String location, File file)
    {
        try
        {
            byte[] bytes = getBytesFromFile(file);
            String contents = "";
            BASE64Encoder encoder = new BASE64Encoder();
            contents = encoder.encode(bytes);
            PlatformUI.MIRTH_FRAME.mirthClient.installExtension(location, contents);
        }
        catch(Exception e)
        {
            alertError("Invalid extenstion file.");
            return false;
        }
        return true;
    }
    
    // Returns the contents of the file in a byte array.
    private byte[] getBytesFromFile(File file) throws IOException
    {
        InputStream is = new FileInputStream(file);
        
        // Get the size of the file
        long length = file.length();
        
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE)
        {
            // File is too large
        }
        
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];
        
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
        {
            offset += numRead;
        }
        
        // Ensure all the bytes have been read in
        if (offset < bytes.length)
        {
            throw new IOException("Could not completely read file " + file.getName());
        }
        
        // Close the input stream and return bytes
        is.close();
        return bytes;
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

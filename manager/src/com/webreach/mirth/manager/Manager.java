package com.webreach.mirth.manager;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.InsetsUIResource;
import org.jdesktop.jdic.tray.*;

public class Manager
{
    private ManagerDialog dialog;
    private ManagerTray tray;    
    
    public static void main(String[] args)
    {
        if(args.length > 0)
            PlatformUI.MIRTH_PATH = args[0];
        else
            PlatformUI.MIRTH_PATH = "";
        
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("win.xpstyle.name", "metallic");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        Manager manager = new Manager();
        
        manager.setupDialog();
        manager.setupTray();
    }
    
    private void setupDialog()
    {
        dialog = new ManagerDialog(); 
        PlatformUI.MANAGER_DIALOG = dialog;
    }
    
    private void setupTray()
    {
        tray = new ManagerTray();
        PlatformUI.MANAGER_TRAY = tray;
    }
}

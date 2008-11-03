package com.webreach.mirth.manager;

import javax.swing.UIManager;

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
    
    public Manager() { 
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }
    
    private class ShutdownHook extends Thread {
        public void run() {
            shutdown();
        }
    }
    
    /**
     * Shuts down the manager.
     * 
     */
    public void shutdown() {
        System.exit(0);
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

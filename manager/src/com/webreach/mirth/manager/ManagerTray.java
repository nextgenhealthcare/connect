/*
 * ManagerTray.java
 *
 * Created on April 19, 2007, 12:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.webreach.mirth.manager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

/**
 *
 * @author brendanh
 */
public class ManagerTray {

    private JPopupMenu menu;
    private JMenuItem viewItem;
    /*
    private JMenuItem startItem;
    private JMenuItem stopItem;
    private JMenuItem restartItem;
     */
    private JMenuItem administratorItem;
    private JMenuItem quitItem;

    /** Creates a new instance of ManagerTray */
    public ManagerTray() {
        setupTray();
    }

    public void setupTray() {
        menu = new JPopupMenu("Mirth Server Manager");

        viewItem = new JMenuItem("Show Manager");
        //viewItem.setIcon(new ImageIcon(this.getClass().getResource("images/start.png")));
        viewItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                PlatformUI.MANAGER_DIALOG.open();
            }
        });
        menu.add(viewItem);

        menu.addSeparator();

        administratorItem = new JMenuItem("Launch Administrator");
        administratorItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                PlatformUI.MANAGER_DIALOG.launchAdministrator();
            }
        });
        menu.add(administratorItem);

        /*
         * Add these back when we start using Java 6.0
         *
        startItem = new JMenuItem("Start Mirth");
        //startItem.setIcon(new ImageIcon(this.getClass().getResource("images/start.png")));
        startItem.addActionListener(new ActionListener()
        {
        public void actionPerformed(ActionEvent e)
        {
        ManagerController.getInstance().startMirth(true);
        }
        });
        menu.add(startItem);
        
        stopItem = new JMenuItem("Stop Mirth");
        //stopItem.setIcon(new ImageIcon(this.getClass().getResource("images/stop.png")));
        stopItem.addActionListener(new ActionListener()
        {
        public void actionPerformed(ActionEvent e)
        {
        ManagerController.getInstance().stopMirth(true);
        }
        });
        menu.add(stopItem);
        
        restartItem = new JMenuItem("Restart Mirth");
        //restartItem.setIcon(new ImageIcon(this.getClass().getResource("images/restart.png")));
        restartItem.addActionListener(new ActionListener()
        {
        public void actionPerformed(ActionEvent e)
        {
        ManagerController.getInstance().restartMirth();
        }
        });
        menu.add(restartItem);
         */

        menu.addSeparator();

        quitItem = new JMenuItem("Close Manager");
        quitItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                PlatformUI.MANAGER_DIALOG.close();
                Manager.shutdown();
            }
        });
        menu.add(quitItem);

        // Resource file "duke.gif" must exist at the same directory
        // as this class file.
        ImageIcon icon = new ImageIcon(this.getClass().getResource("images/mirthlogo1616.png"));
        TrayIcon mirthTrayIcon = new TrayIcon(icon, "Mirth Server Manager", menu);

        // Action listener for left click.
        mirthTrayIcon.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                PlatformUI.MANAGER_DIALOG.open();
            }
        });

        SystemTray tray = SystemTray.getDefaultSystemTray();

        tray.addTrayIcon(mirthTrayIcon);
    }
    /*
     * For Java 6.0
     *
    public void setStartButtonActive(boolean active)
    {
    startItem.setEnabled(active);
    }
    
    public void setStopButtonActive(boolean active)
    {
    stopItem.setEnabled(active);
    }
    
    public void setRestartButtonActive(boolean active)
    {
    restartItem.setEnabled(active);
    }*/
}

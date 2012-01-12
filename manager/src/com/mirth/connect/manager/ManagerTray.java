/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.manager;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

public class ManagerTray {

    private TrayIcon mirthTrayIcon;
    private PopupMenu menu;
    private MenuItem viewItem;
    private MenuItem startItem;
    private MenuItem stopItem;
    private MenuItem restartItem;
    private MenuItem administratorItem;
    private MenuItem quitItem;
    public static final int STARTED = 1;
    public static final int STOPPED = 0;
    public static final int BUSY = -1;

    /** Creates a new instance of ManagerTray */
    public ManagerTray() {
    }

    public void setupTray() {
        menu = new PopupMenu("Mirth Connect Server Manager");

        viewItem = new MenuItem("Show Manager");
//        viewItem.setIcon(new ImageIcon(this.getClass().getResource("images/start.png")));
        viewItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                PlatformUI.MANAGER_DIALOG.open();
            }
        });
        menu.add(viewItem);

        menu.addSeparator();

        administratorItem = new MenuItem("Launch Administrator");
        administratorItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ManagerController.getInstance().launchAdministrator();
            }
        });
        menu.add(administratorItem);

        startItem = new MenuItem("Start Mirth");
//        startItem.setIcon(new ImageIcon(this.getClass().getResource("images/start.png")));
        startItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ManagerController.getInstance().startMirthWorker();
            }
        });
        menu.add(startItem);

        stopItem = new MenuItem("Stop Mirth");
//        stopItem.setIcon(new ImageIcon(this.getClass().getResource("images/stop.png")));
        stopItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ManagerController.getInstance().stopMirthWorker();
            }
        });
        menu.add(stopItem);

        restartItem = new MenuItem("Restart Mirth");
//        restartItem.setIcon(new ImageIcon(this.getClass().getResource("images/restart.png")));
        restartItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ManagerController.getInstance().restartMirthWorker();
            }
        });
        menu.add(restartItem);

        menu.addSeparator();

        quitItem = new MenuItem("Close Manager");
        quitItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                PlatformUI.MANAGER_DIALOG.close();
                Manager.shutdown();
            }
        });
        menu.add(quitItem);

        ImageIcon icon = new ImageIcon(this.getClass().getResource("images/mirth_32_ico_gray.png"));
        mirthTrayIcon = new TrayIcon(icon.getImage(), "Mirth Connect Server Manager", menu);
        mirthTrayIcon.setImageAutoSize(true);

        // Action listener for left click.
        mirthTrayIcon.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                PlatformUI.MANAGER_DIALOG.open();
            }
        });

        try {
            if (ServiceControllerFactory.getServiceController().isShowTrayIcon()) {
                SystemTray tray = null;
                try {
                    tray = SystemTray.getSystemTray();
                    tray.add(mirthTrayIcon);
                } catch (Throwable t) {
                    // Exit the manager in case of the following error:
                    // java.lang.UnsatisfiedLinkError: C:\Program Files (x86)\Mirth\lib\tray.dll: Can't load IA 32-bit .dll on a AMD 64-bit platform
                    t.printStackTrace();
                    System.exit(1);
                }
            } else {
            	// If no tray dialog is being shown, open the manager dialog automatically
            	PlatformUI.MANAGER_DIALOG.open();
            }
        } catch (Exception e) {
            // Ignore exceptions getting the service controller.
            // The tray icon will not be displayed if there was a problem.
        }
    }

    public void setStartButtonActive(boolean active) {
        startItem.setEnabled(active);
    }

    public void setStopButtonActive(boolean active) {
        stopItem.setEnabled(active);
    }

    public void setRestartButtonActive(boolean active) {
        restartItem.setEnabled(active);
    }

    public void setLaunchButtonActive(boolean active) {
        administratorItem.setEnabled(active);
    }

    public void alertError(final String text) {
        // MIRTH-2050: Use invokeLater so that the messages always appear
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mirthTrayIcon.displayMessage("Error", text, TrayIcon.MessageType.ERROR);
            }
        });
    }

    public void alertInfo(final String text) {
        // MIRTH-2050: Use invokeLater so that the messages always appear
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mirthTrayIcon.displayMessage("Information", text, TrayIcon.MessageType.INFO);
            }
        });
    }

    public void alertWarning(final String text) {
        // MIRTH-2050: Use invokeLater so that the messages always appear
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mirthTrayIcon.displayMessage("Warning", text, TrayIcon.MessageType.WARNING);
            }
        });
    }

    public void setTrayIcon(int icon) {
        final ImageIcon imageIcon;
        if (icon == STARTED) {
            imageIcon = new ImageIcon(this.getClass().getResource("images/mirth_32_ico.png"));
        } else if (icon == STOPPED) {
            imageIcon = new ImageIcon(this.getClass().getResource("images/mirth_32_ico_gray.png"));
        } else {
            imageIcon = new ImageIcon(this.getClass().getResource("images/mirth_32_ico_gray.png"));
        }

        // MIRTH-2050: Use invokeLater so that the messages always appear
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mirthTrayIcon.setImage(imageIcon.getImage());
            }
        });
    }
}

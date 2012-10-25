/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.manager;

import javax.swing.UIManager;

public class Manager {

    private ManagerDialog dialog;
    private ManagerTray tray;
    private static Thread shutdownHook;

    public static void main(String[] args) {

        if (args.length > 0) {
            PlatformUI.MIRTH_PATH = args[0];
        } else {
            PlatformUI.MIRTH_PATH = "";
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("win.xpstyle.name", "metallic");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Manager manager = new Manager();

        manager.setupDialog();
        manager.setupTray();

        ManagerController.getInstance().updateMirthServiceStatus();
    }

    public Manager() {
        shutdownHook = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
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
    public static void shutdown() {
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
        System.exit(0);
    }

    private void setupDialog() {
        dialog = new ManagerDialog();
        PlatformUI.MANAGER_DIALOG = dialog;
        dialog.setupDialog();
    }

    private void setupTray() {
        tray = new ManagerTray();
        PlatformUI.MANAGER_TRAY = tray;
        tray.setupTray();

    }
}

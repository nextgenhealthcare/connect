/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.extensionmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.plugins.ClientPanelPlugin;

public class ExtensionManagerClient extends ClientPanelPlugin {

    public ExtensionManagerClient(String name) {
        super(name, true, true);

        getTaskPane().setTitle("Manager Tasks");
        setComponent(new ExtensionManagerPanel(this));

        addTask("doCheckForUpdates", "Check for Updates", "Checks all extensions for updates.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/world_link.png")));

        addTask("doEnable", "Enable Extension", "Enable the currently selected extension.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_play_blue.png")));
        addTask("doDisable", "Disable Extension", "Disable the currently selected extension.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_stop_blue.png")));
        addTask("doShowProperties", "Show Properties", "Display the currently selected extension properties.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_view_list.png")));
        addTask("doUninstall", "Uninstall Extension", "Uninstall the currently selected extension", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/plugin_delete.png")));
        setVisibleTasks(getRefreshIndex(), getRefreshIndex(), true);
        setVisibleTasks(getSaveIndex(), getSaveIndex(), false);
        setVisibleTasks(2, 2, true);
        setVisibleTasks(3, -1, false);

        getComponent().addMouseListener(getPopupMenuMouseAdapter());
    }

    public void doShowProperties() {
        ((ExtensionManagerPanel) getComponent()).showExtensionProperties();
    }

    public void doCheckForUpdates() {
        try {
            new ExtensionUpdateDialog(this);
        } catch (ClientException e) {
            alertException(parent, e.getStackTrace(), e.getMessage());
        }
    }

    public void doRefresh() {
        setWorking("Loading plugin settings...", true);

        SwingWorker worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    if (!confirmLeave()) {
                        return null;
                    }
                    refresh();
                } catch (ClientException e) {
                    alertException(parent, e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done() {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doSave() {
        setWorking("Saving plugin settings...", true);

        SwingWorker worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    save();
                    alertInformation(parent, "A restart is required before your changes will take effect.");
                } catch (ClientException e) {
                    alertException(parent, e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done() {
                disableSave();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doEnable() {
        ((ExtensionManagerPanel) getComponent()).enableExtension();
        enableSave();
    }

    public void doDisable() {
        ((ExtensionManagerPanel) getComponent()).disableExtension();
        enableSave();
    }

    public void doUninstall() {
        setWorking("Uninstalling plugin...", true);

        SwingWorker worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                String packageName = ((ExtensionManagerPanel) getComponent()).getSelectedExtension().getPath();

                if (alertOkCancel(parent, "Uninstalling this extension will remove all plugins and/or connectors\nin the following extension folder: " + packageName)) {
                    try {
                        PlatformUI.MIRTH_FRAME.mirthClient.uninstallExtension(packageName);
                    } catch (ClientException e) {
                        alertException(parent, e.getStackTrace(), e.getMessage());
                    }

                    finishUninstall();
                }

                return null;
            }

            public void done() {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void finishUninstall() {
        Properties props = null;
        try {
            props = this.getPropertiesFromServer();
        } catch (ClientException e) {
            alertException(parent, e.getStackTrace(), e.getMessage());
        }

        if (props != null && Boolean.parseBoolean(props.getProperty("disableInstall"))) {
            alertInformation(parent, "Your extension(s) have been added to the \"uninstall\" file in your extensions\n"
                    + "location on the server.  To uninstall the extensions, manually shutdown the Mirth container\n"
                    + "(e.g. JBoss), delete the folders listed in the \"uninstall\" file, and restart the Mirth container.\n");
        } else {
            alertInformation(parent, "The Mirth server must be restarted for the extension(s) to be uninstalled.");
        }
    }

    public void refresh() throws ClientException {
        ((ExtensionManagerPanel) getComponent()).setPluginData(PlatformUI.MIRTH_FRAME.getPluginMetaData());
        ((ExtensionManagerPanel) getComponent()).setConnectorData(PlatformUI.MIRTH_FRAME.getConnectorMetaData());
    }

    public void save() throws ClientException {
        ((ExtensionManagerPanel) getComponent()).savePluginData();
        ((ExtensionManagerPanel) getComponent()).saveConnectorData();

        PlatformUI.MIRTH_FRAME.mirthClient.setPluginMetaData(PlatformUI.MIRTH_FRAME.getPluginMetaData());
        PlatformUI.MIRTH_FRAME.mirthClient.setConnectorMetaData(PlatformUI.MIRTH_FRAME.getConnectorMetaData());
    }

    public boolean install(File file) {
        try {
            if (file.exists()) {
                PlatformUI.MIRTH_FRAME.mirthClient.installExtension(file);
            } else {
                alertError(parent, "Invalid extension file.");
                return false;
            }
        } catch (Exception e) {
            String errorMessage = "Unable to install extension.";
            try {
                String tempErrorMessage = java.net.URLDecoder.decode(e.getMessage(), "UTF-8");
                String versionError = "VersionMismatchException: ";
                int messageIndex = tempErrorMessage.indexOf(versionError);

                if (messageIndex != -1) {
                    errorMessage = tempErrorMessage.substring(messageIndex + versionError.length());
                }

            } catch (UnsupportedEncodingException e1) {
                alertException(parent, e1.getStackTrace(), e1.getMessage());
            }

            alertError(parent, errorMessage);
            return false;
        }
        return true;
    }

    public void finishInstall() {
        Properties props = null;
        try {
            props = this.getPropertiesFromServer();
        } catch (ClientException e) {
            alertException(parent, e.getStackTrace(), e.getMessage());
        }

        if (props != null && Boolean.parseBoolean(props.getProperty("disableInstall"))) {
            alertInformation(parent, "Your extension(s) have been installed to the 'install_temp' directories in your extensions\n"
                    + "location on the server.  To load the new plugins, manually shutdown the Mirth container\n"
                    + "(e.g. JBoss), drag the plugins out of 'install_temp', and restart the Mirth container.\n");
        } else {
            alertInformation(parent, "The Mirth server must be restarted for the extension(s) to load.");
        }
    }

    // Returns the contents of the file in a byte array.
    private byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    public void start() {
    }

    public void stop() {
    }
    
    public void reset() {
    }

    public void display() {
        doRefresh();
    }
}

/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.manager;

import java.awt.Cursor;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FileUtils;

import com.mirth.connect.client.core.Client;
import com.mirth.connect.client.core.ClientException;

public class ManagerController {

    private static ManagerController managerController = null;
    private static ServiceController serviceController = null;

    private PropertiesConfiguration serverProperties;
    private PropertiesConfiguration log4jProperties;
    private PropertiesConfiguration versionProperties;
    private PropertiesConfiguration serverIdProperties;

    private boolean updating = false;

    public static ManagerController getInstance() {
        if (managerController == null) {
            managerController = new ManagerController();
            managerController.initialize();

            try {
                serviceController = ServiceControllerFactory.getServiceController();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        return managerController;
    }

    public void initialize() {
        serverProperties = initializeProperties(PlatformUI.MIRTH_PATH + ManagerConstants.PATH_SERVER_PROPERTIES, true);
        log4jProperties = initializeProperties(PlatformUI.MIRTH_PATH + ManagerConstants.PATH_LOG4J_PROPERTIES, true);
        serverIdProperties = initializeProperties(PlatformUI.MIRTH_PATH + getServerProperties().getString(ManagerConstants.DIR_APPDATA) + System.getProperty("file.separator") + ManagerConstants.PATH_SERVER_ID_FILE, false);

        InputStream is = getClass().getResourceAsStream(ManagerConstants.PATH_VERSION_FILE);
        if (is != null) {
            try {
                versionProperties = new PropertiesConfiguration();
                versionProperties.load(is);
            } catch (ConfigurationException e) {
                alertErrorDialog("Could not load resource: " + ManagerConstants.PATH_VERSION_FILE);
            }
        } else {
            versionProperties = initializeProperties(PlatformUI.MIRTH_PATH + ManagerConstants.PATH_VERSION_FILE, true);
        }
    }

    private PropertiesConfiguration initializeProperties(String path, boolean alert) {
        PropertiesConfiguration properties = new PropertiesConfiguration();

        // Auto reload changes
        FileChangedReloadingStrategy fileChangedReloadingStrategy = new FileChangedReloadingStrategy();
        fileChangedReloadingStrategy.setRefreshDelay(1000);
        properties.setReloadingStrategy(fileChangedReloadingStrategy);
        properties.setFile(new File(path));

        if (properties.isEmpty() && alert) {
            alertErrorDialog("Could not load properties from file: " + path);
        }

        return properties;
    }

    /**
     * Test a port to see if it is already in use.
     * 
     * @param port
     *            The port to test.
     * @param name
     *            A friendly name to display in case of an error.
     * @return An error message, or null if the port is not in use and there was
     *         no error.
     */
    private String testPort(String port, String name) {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(Integer.parseInt(port));
        } catch (NumberFormatException ex) {
            return name + " port is invalid: " + port;
        } catch (IOException ex) {
            return name + " port is already in use: " + port;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    return "Could not close test socket for " + name + ": " + port;
                }
            }
        }
        return null;
    }

    public void startMirthWorker() {
        PlatformUI.MANAGER_DIALOG.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ManagerController.getInstance().setEnabledOptions(false, false, false, false);

        SwingWorker worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                startMirth();
                return null;
            }

            public void done() {
                updateMirthServiceStatus();
                PlatformUI.MANAGER_DIALOG.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        };

        worker.execute();
    }

    private boolean startMirth() {
        String httpPort = getServerProperties().getString(ManagerConstants.SERVER_WEBSTART_PORT);
        String httpsPort = getServerProperties().getString(ManagerConstants.SERVER_ADMINISTRATOR_PORT);
        String jmxPort = getServerProperties().getString(ManagerConstants.SERVER_JMX_PORT);
        String httpPortResult = testPort(httpPort, "WebStart");
        String httpsPortResult = testPort(httpsPort, "Administrator");
        String jmxPortResult = testPort(jmxPort, "JMX");

        if (httpPortResult != null || httpsPortResult != null || jmxPortResult != null) {
            String errorMessage = "";
            if (httpPortResult != null) {
                errorMessage += httpPortResult + "\n";
            }
            if (httpsPortResult != null) {
                errorMessage += httpsPortResult + "\n";
            }
            if (jmxPortResult != null) {
                errorMessage += jmxPortResult + "\n"; // Remove the last \n
            }
            errorMessage.substring(0, errorMessage.length() - 1);
            PlatformUI.MANAGER_TRAY.alertError(errorMessage);

            return false;
        }

        try {
            updating = true;

            if (!serviceController.startService()) {
                PlatformUI.MANAGER_TRAY.alertError("The Mirth Connect Service could not be started.  Please verify that it is installed and not already started.");
            } else {
                // Load the context path property and remove the last char
                // if it is a '/'.
                String contextPath = getServerProperties().getString("context.path");
                if (contextPath.lastIndexOf('/') == (contextPath.length() - 1)) {
                    contextPath = contextPath.substring(0, contextPath.length() - 1);
                }
                Client client = new Client(ManagerConstants.CMD_TEST_JETTY_PREFIX + getServerProperties().getString("https.port") + contextPath);

                int retriesLeft = 30;
                long waitTime = 1000;
                boolean started = false;

                while (!started && retriesLeft > 0) {
                    Thread.sleep(waitTime);
                    retriesLeft--;

                    try {
                        if (client.getStatus() == 0) {
                            started = true;
                        }
                    } catch (ClientException e) {
                    }
                }

                if (!started) {
                    PlatformUI.MANAGER_TRAY.alertError("The Mirth Connect Service could not be started.");
                } else {
                    PlatformUI.MANAGER_TRAY.alertInfo("The Mirth Connect Service was started successfully.");
                    updating = false;
                    updateMirthServiceStatus();
                    return true;
                }
            }
        } catch (Throwable t) { // Need to catch Throwable in case Client fails
            // internally
            t.printStackTrace();
        }

        updating = false;
        updateMirthServiceStatus();
        return false;
    }

    public void stopMirthWorker() {
        PlatformUI.MANAGER_DIALOG.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setEnabledOptions(false, false, false, false);
        SwingWorker worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                stopMirth();
                return null;
            }

            public void done() {
                updateMirthServiceStatus();
                PlatformUI.MANAGER_DIALOG.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        };

        worker.execute();
    }

    private boolean stopMirth() {
        try {
            updating = true;
            if (!serviceController.stopService()) {
                PlatformUI.MANAGER_TRAY.alertError("The Mirth Connect Service could not be stopped.  Please verify that it is installed and started.");
            } else {
                PlatformUI.MANAGER_TRAY.alertInfo("The Mirth Connect Service was stopped successfully.");
                updating = false;
                updateMirthServiceStatus();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updating = false;
        updateMirthServiceStatus();
        return false;
    }

    public void restartMirthWorker() {
        PlatformUI.MANAGER_DIALOG.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setEnabledOptions(false, false, false, false);
        SwingWorker worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                ManagerController.getInstance().restartMirth();

                return null;
            }

            public void done() {
                updateMirthServiceStatus();
                PlatformUI.MANAGER_DIALOG.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        };

        worker.execute();
    }

    private void restartMirth() {
        if (stopMirth()) {
            if (startMirth()) {
                PlatformUI.MANAGER_TRAY.alertInfo("The Mirth Connect Service was restarted successfully.");
                updating = false;
                updateMirthServiceStatus();
            }
        }
    }

    public void launchAdministrator() {
        String port = getServerProperties().getString(ManagerConstants.SERVER_WEBSTART_PORT);
        try {
            String cmd = ManagerConstants.CMD_WEBSTART_PREFIX + port + ManagerConstants.CMD_WEBSTART_SUFFIX + "?time=" + new Date().getTime();

            if (CmdUtil.execCmd(new String[] { cmd }, false) != 0) {
                PlatformUI.MANAGER_TRAY.alertError("The Mirth Connect Administator could not be launched.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PropertiesConfiguration getServerProperties() {
        return serverProperties;
    }

    public PropertiesConfiguration getLog4jProperties() {
        return log4jProperties;
    }

    public String getServerVersion() {
        return versionProperties.getString("mirth.version");
    }

    public String getServerId() {
        return serverIdProperties.getString("server.id");
    }

    public List<String> getLogFiles(String path) {
        ArrayList<String> files = new ArrayList<String>();
        File dir = new File(path);

        String[] children = dir.list();
        if (children == null) {
            // Either dir does not exist or is not a directory
        } else {
            for (int i = 0; i < children.length; i++) {
                // Get filename of file or directory
                files.add(children[i]);
            }
        }

        return files;
    }

    public void openLogFile(String path) {
        File file = new File(path);

        try {
            Desktop.getDesktop().open(file);
        } catch (Exception e) {
            boolean editorOpened = false;

            String[] apps = new String[] { "notepad", "kate", "gedit", "gvim", "open -t" };

            for (int i = 0; (i < apps.length) && !editorOpened; i++) {
                try {
                    String output = CmdUtil.execCmdWithErrorOutput(new String[] { apps[i] + " \"" + path + "\"" });

                    if (output.length() == 0) {
                        editorOpened = true;
                    }
                } catch (Exception ex) {
                    // ignore exceptions
                }

            }

            if (!editorOpened) {
                e.printStackTrace();
                alertErrorDialog("Could not open file: " + path + "\nPlease make sure a text editor is associated with the log's file extension.");
            }
        }
    }

    public String getServiceXmx() {
        String match = "";

        File file = new File(PlatformUI.MIRTH_PATH + ManagerConstants.PATH_SERVICE_VMOPTIONS);
        String contents = "";
        try {
            contents = FileUtils.readFileToString(file);
        } catch (IOException e) {
            // Ignore error if file does not exist
        }

        Pattern pattern = Pattern.compile("-Xmx(.*?)m");
        Matcher matcher = pattern.matcher(contents);

        if (matcher.find()) {
            match = matcher.group(1);
        }

        return match;
    }

    public void setServiceXmx(String xmx) {
        File file = new File(PlatformUI.MIRTH_PATH + ManagerConstants.PATH_SERVICE_VMOPTIONS);
        String contents = "";

        try {
            contents = FileUtils.readFileToString(file);
        } catch (IOException e) {
            // Ignore error if file does not exist
        }

        Pattern pattern = Pattern.compile("-Xmx(.*?)m");
        Matcher matcher = pattern.matcher(contents);

        if (matcher.find()) {
            contents = matcher.replaceFirst("-Xmx" + xmx + "m");
        } else if (xmx.length() != 0) {
            contents += "-Xmx" + xmx + "m";
        }

        try {
            FileUtils.writeStringToFile(file, contents);
        } catch (IOException e) {
            alertErrorDialog("Error writing file to: " + file.getPath());
        }
    }

    public void updateMirthServiceStatus() {
        int status = serviceController.checkService();
        if (updating) {
            return;
        }
        switch (status) {
            case 0:
                setEnabledOptions(true, false, false, false);
                break;
            case 1:
                setEnabledOptions(false, true, true, true);
                break;
            default:
                setEnabledOptions(false, false, false, false);
                break;
        }
    }

    public void setEnabledOptions(boolean start, boolean stop, boolean restart, boolean launch) {
        PlatformUI.MANAGER_DIALOG.setStartButtonActive(start);
        PlatformUI.MANAGER_DIALOG.setStopButtonActive(stop);
        PlatformUI.MANAGER_DIALOG.setRestartButtonActive(restart);
        PlatformUI.MANAGER_DIALOG.setLaunchButtonActive(launch);
        PlatformUI.MANAGER_TRAY.setStartButtonActive(start);
        PlatformUI.MANAGER_TRAY.setStopButtonActive(stop);
        PlatformUI.MANAGER_TRAY.setRestartButtonActive(restart);
        PlatformUI.MANAGER_TRAY.setLaunchButtonActive(launch);

        if (start) {
            PlatformUI.MANAGER_TRAY.setTrayIcon(ManagerTray.STOPPED);
        } else if (stop) {
            PlatformUI.MANAGER_TRAY.setTrayIcon(ManagerTray.STARTED);
        } else {
            PlatformUI.MANAGER_TRAY.setTrayIcon(ManagerTray.BUSY);
        }
    }

    public void setApplyEnabled(boolean enabled) {
        PlatformUI.MANAGER_DIALOG.setApplyEnabled(enabled);
    }

    /**
     * Alerts the user with an error dialog with the passed in 'message'
     */
    public void alertErrorDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Alerts the user with an information dialog with the passed in 'message'
     */
    public void alertInformationDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Alerts the user with a yes/no option with the passed in 'message'
     */
    public boolean alertOptionDialog(String message) {
        int option = JOptionPane.showConfirmDialog(null, message, "Select an Option", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            return true;
        } else {
            return false;
        }
    }
}

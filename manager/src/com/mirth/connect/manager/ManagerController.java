/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import com.mirth.connect.client.core.Client;
import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.util.PropertyLoader;
import java.awt.Cursor;
import java.awt.Desktop;
import javax.swing.SwingWorker;

public class ManagerController {

    private static ManagerController assistantController = null;
    private boolean updating = false;

    // private final String CMD_QUERY_REGEX = ".*STATE.* :.(.)";
    public static ManagerController getInstance() {
        if (assistantController == null) {
            assistantController = new ManagerController();
        }

        return assistantController;
    }

    public void setStartup(boolean enabled) {
        if (enabled) {
            try {
                String absolutePath = new File(PlatformUI.MIRTH_PATH).getAbsolutePath();
                execCmd(ManagerConstants.CMD_REG_ADD + "\"" + absolutePath + System.getProperty("file.separator") + "MirthServerManager.exe\"", true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                execCmd(ManagerConstants.CMD_REG_DELETE, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isStartup() {
        int keyQueryResult = 1;
        try {
            keyQueryResult = execCmd(ManagerConstants.CMD_REG_QUERY, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (keyQueryResult == 0) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * Commands to be executed by both the UI and tray
     */
    /**
     * Get the current Mirth Service status
     * @return the status of the Mirth Service
     */
    public int checkMirth() {
        Pattern pattern = Pattern.compile(ManagerConstants.CMD_QUERY_REGEX);
        Matcher matcher;
        String key = "-1";
        do {
            try {
                matcher = pattern.matcher(execCmdWithErrorOutput(ManagerConstants.CMD_STATUS + ManagerConstants.SERVICE_NAME + "\"").replace('\n', ' ').replace('\r', ' '));
                while (matcher.find()) {
                    key = matcher.group(1);
                }

                if (key.equals(ManagerConstants.STATUS_CHANGING)) {
                    Thread.sleep(100);
                } else {
                    return Integer.parseInt(key);
                }
            } catch (Exception e) {
            }
        } while (key.equals(ManagerConstants.STATUS_CHANGING));

        return -1;
    }

    /**
     * Test a port to see if it is already in use.
     * 
     * @param port The port to test.
     * @param name A friendly name to display in case of an error.
     * @return An error message, or null if the port is not in use and there was no error.
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

        Properties serverProperties = ManagerController.getInstance().getProperties(PlatformUI.MIRTH_PATH + ManagerConstants.PATH_SERVER_PROPERTIES, true);
        String httpPort = serverProperties.getProperty(ManagerConstants.SERVER_WEBSTART_PORT);
        String httpsPort = serverProperties.getProperty(ManagerConstants.SERVER_ADMINISTRATOR_PORT);
        String jmxPort = serverProperties.getProperty(ManagerConstants.SERVER_JMX_PORT);
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
                errorMessage += jmxPortResult + "\n";            // Remove the last \n
            }
            errorMessage.substring(0, errorMessage.length() - 1);
            PlatformUI.MANAGER_TRAY.alertError(errorMessage);

            return false;
        }

        try {
            updating = true;

            if (execCmd(ManagerConstants.CMD_START + ManagerConstants.SERVICE_NAME + "\"", true) != 0) {
                PlatformUI.MANAGER_TRAY.alertError("The Mirth Connect Service could not be started.  Please verify that it is installed and not already started.");
            } else {
                // Load the context path property and remove the last char
                // if it is a '/'.
                String contextPath = PropertyLoader.getProperty(serverProperties, "context.path");
                if (contextPath.lastIndexOf('/') == (contextPath.length() - 1)) {
                    contextPath = contextPath.substring(0, contextPath.length() - 1);
                }
                Client client = new Client(ManagerConstants.CMD_TEST_JETTY_PREFIX + PropertyLoader.getProperty(serverProperties, "https.port") + contextPath);

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
        } catch (Throwable t) { // Need to catch Throwable in case Client fails internally
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
            if (execCmd(ManagerConstants.CMD_STOP + ManagerConstants.SERVICE_NAME + "\"", true) != 0) {
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
        Properties serverProperties = getProperties(PlatformUI.MIRTH_PATH + ManagerConstants.PATH_SERVER_PROPERTIES, true);
        String port = serverProperties.getProperty(ManagerConstants.SERVER_WEBSTART_PORT);
        try {
            if (execCmd(ManagerConstants.CMD_WEBSTART_PREFIX + port + ManagerConstants.CMD_WEBSTART_SUFFIX + "?time=" + new Date().getTime(), false) != 0) {
                PlatformUI.MANAGER_TRAY.alertError("The Mirth Connect Administator could not be launched.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Properties getProperties(String path, boolean alert) {
        Properties properties = new Properties();
        try {
            FileInputStream propertyFile = new FileInputStream(path);
            properties.load(propertyFile);
            propertyFile.close();
        } catch (IOException ex) {
            if (alert) {
                alertErrorDialog("Could not load file: " + path);
            }
        }
        return properties;
    }

    public boolean setProperties(Properties properties, String path) {
        try {
            FileOutputStream propertyFile = new FileOutputStream(path);
            properties.store(new FileOutputStream(path), null);
            propertyFile.close();
            return true;
        } catch (IOException e) {
            alertErrorDialog("Could not save file: " + path);
        }
        return false;
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
            try {
                Runtime.getRuntime().exec("notepad \"" + path + "\"");
            } catch (IOException ex) {
                alertErrorDialog("Could not open file: " + path);
                ex.printStackTrace();
            }
        }
    }

    public void updateMirthServiceStatus() {
        int status = checkMirth();
        if (updating) {
            return;
        }
        switch (status) {
            case ManagerConstants.STATUS_STOPPED:
                setEnabledOptions(true, false, false, false);
                break;
            case ManagerConstants.STATUS_RUNNING:
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

    private int execCmd(String cmdLine, boolean waitFor) throws Exception {
        Process process = Runtime.getRuntime().exec(cmdLine);

        if (!waitFor) {
            return 0;
        }
        StreamPumper outPumper = new StreamPumper(process.getInputStream(), System.out);
        StreamPumper errPumper = new StreamPumper(process.getErrorStream(), System.err);

        outPumper.start();
        errPumper.start();
        process.waitFor();
        outPumper.join();
        errPumper.join();

        return process.exitValue();
    }

    private String execCmdWithOutput(String cmdLine) throws Exception {
        Process process = Runtime.getRuntime().exec(cmdLine);
        StreamPumper outPumper = new StreamPumper(process.getInputStream(), System.out);
        StreamPumper errPumper = new StreamPumper(process.getErrorStream(), System.err);

        outPumper.start();
        errPumper.start();
        process.waitFor();
        outPumper.join();
        errPumper.join();

        return outPumper.getOutput();
    }

    private String execCmdWithErrorOutput(String cmdLine) throws Exception {
        Process process = Runtime.getRuntime().exec(cmdLine);
        StreamPumper outPumper = new StreamPumper(process.getInputStream(), System.out);
        StreamPumper errPumper = new StreamPumper(process.getErrorStream(), System.err);

        outPumper.start();
        errPumper.start();
        process.waitFor();
        outPumper.join();
        errPumper.join();

        return errPumper.getOutput();
    }

    private class StreamPumper extends Thread {

        private InputStream is;
        private PrintStream os;
        private StringBuffer output;

        public StreamPumper(InputStream is, PrintStream os) {
            this.is = is;
            this.os = os;

            output = new StringBuffer();
        }

        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;

                while ((line = br.readLine()) != null) {
                    output.append(line + "\n");
                    os.println(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String getOutput() {
            return output.toString();
        }
    }
}

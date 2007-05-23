/*
 * ManagerController.java
 *
 * Created on April 16, 2007, 2:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.DesktopException;

/**
 *
 * @author brendanh
 */
public class ManagerController
{
    private static ManagerController assistantController = null;
    
    private final String serviceName = "Mirth";
    private final String CMD_START = "cmd /c net start \"";
    private final String CMD_STOP = "cmd /c net stop \"";
    private final String CMD_WEBSTART = "cmd /c javaws http://localhost:8080/webstart";
    private final String CMD_STATUS = "sc query \"";    
    
    private final String CMD_QUERY_REGEX = ".*STATE.* :.(.)";
    
    private static final int STATUS_STOPPED = 1;
    private static final int STATUS_START_PENDING = 2;
    private static final int STATUS_STOP_PENDING = 3;
    private static final int STATUS_RUNNING = 4;    
    
    public static ManagerController getInstance()
    {
        if(assistantController == null)
        {
            assistantController = new ManagerController();
        }
        
        return assistantController;
    }
    
    /*
     *  Commands to be executed by both the UI and tray
     */
    
    public int checkMirth()
    {
        Pattern pattern = Pattern.compile(CMD_QUERY_REGEX);
        Matcher matcher;
        String key = "-1";
        
        try
        {
            matcher = pattern.matcher(execCmdWithOutput(CMD_STATUS + serviceName + "\""));
            
            while (matcher.find())
            {
                key = matcher.group(1);
            }
        }
        catch (Exception ex)
        {
        }
        
        return Integer.valueOf(key);
    }
    
    public boolean startMirth(boolean alert)
    {
        try
        {
            if(execCmd(CMD_START + serviceName + "\"", true) != 0)
            {
                alertError("The Mirth service could not be started.  Please verify that it is installed and not already started.");
                updateMirthServiceStatus();
            }
            else
            {
                if(alert)
                {    
                    alertInformation("The Mirth service was started successfully.");
                    updateMirthServiceStatus();
                }
                return true;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        return false;
    }
    
    public boolean stopMirth(boolean alert)
    {
        try
        {
            if(execCmd(CMD_STOP + serviceName + "\"", true) != 0)
            {
                alertError("The Mirth service could not be stopped.  Please verify that it is installed and started.");
                updateMirthServiceStatus();
            }
            else
            {
                if(alert)
                {
                    alertInformation("The Mirth service was stopped successfully.");
                    updateMirthServiceStatus();
                }
                return true;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        return false;
    }
    
    public void restartMirth()
    {
        if(stopMirth(false))
        {
            if(startMirth(false))
            {
                alertInformation("The Mirth service was restarted successfully.");
                updateMirthServiceStatus();
            }
        }
    }
    
    public void launchAdministrator()
    {
        try
        {
            if(execCmd(CMD_WEBSTART, false) != 0)
            {
                alertError("The Mirth Administator could not be launched.");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public Properties getProperties(String path)
    {
        Properties properties = new Properties();
        try
        {
            FileInputStream propertyFile = new FileInputStream(path);
            properties.load(propertyFile);
            propertyFile.close();
        }
        catch (IOException ex)
        {
            alertError("Could not load file: " + path);
        }
        return properties;
    }
    
    public boolean setProperties(Properties properties, String path)
    {
        try
        {
            FileOutputStream propertyFile = new FileOutputStream(path);
            properties.store(new FileOutputStream(path), null);
            propertyFile.close();
            return true;
        }
        catch (IOException e)
        {
            alertError("Could not save file: " + path);
        }
        return false;
    }
    
    public List<String> getLogFiles(String path)
    {
        ArrayList<String> files = new ArrayList<String>();
        File dir = new File(path);
        
        String[] children = dir.list();
        if (children == null)
        {
            // Either dir does not exist or is not a directory
        }
        else
        {
            for (int i=0; i<children.length; i++)
            {
                // Get filename of file or directory
                files.add(children[i]);
            }
        }
        
        return files;
    }
    
    public void openLogFile(String path)
    {
        File file = new File(path);
        try
        {
            Desktop.open(file);
        } 
        catch (DesktopException e)
        {
            try
            {
                Runtime.getRuntime().exec("notepad \"" + path + "\"");
            }
            catch (IOException ex)
            {
                alertError("Could not open file: " + path);
                ex.printStackTrace();
            }
        }
    }
    
    /** A method to compare two properties file to check if they are the same. */
    public boolean compareProps(Properties p1, Properties p2)
    {
        Enumeration<?> propertyKeys = p1.propertyNames();
        while (propertyKeys.hasMoreElements())
        {
            String key = (String) propertyKeys.nextElement();

            if (p1.getProperty(key) == null)
            {
                if (p2.getProperty(key) != null)
                    return false;
            }
            else if (!p1.getProperty(key).equals(p2.getProperty(key)))
                return false;
        }
        return true;
    }
    
    public void updateMirthServiceStatus()
    {
        int status = checkMirth();
        
        switch(status)
        {
            case STATUS_STOPPED:
                ManagerController.getInstance().setEnabledOptions(true,false,false);
                break;
            case STATUS_START_PENDING:
                ManagerController.getInstance().setEnabledOptions(false,true,true);
                break;
            case STATUS_STOP_PENDING:
                ManagerController.getInstance().setEnabledOptions(true,false,false);
                break;
            case STATUS_RUNNING:
                ManagerController.getInstance().setEnabledOptions(false,true,true);
                break;
            default:
                ManagerController.getInstance().setEnabledOptions(false,false,false);
                break;
        }
    }
    
    public void setEnabledOptions(boolean start, boolean stop, boolean restart)
    {
        PlatformUI.MANAGER_DIALOG.setStartButtonActive(start);
        PlatformUI.MANAGER_DIALOG.setStopButtonActive(stop);
        PlatformUI.MANAGER_DIALOG.setRestartButtonActive(restart);
        
        /*
         * For Java 6.0
         *
        PlatformUI.MANAGER_TRAY.setStartButtonActive(start);
        PlatformUI.MANAGER_TRAY.setStopButtonActive(stop);
        PlatformUI.MANAGER_TRAY.setRestartButtonActive(restart);
         */
    }
    
    /**
     * Alerts the user with an error dialog with the passed in 'message'
     */
    public void alertError(String message)
    {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Alerts the user with an information dialog with the passed in 'message'
     */
    public void alertInformation(String message)
    {
        JOptionPane.showMessageDialog(null, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Alerts the user with a yes/no option with the passed in 'message'
     */
    public boolean alertOption(String message)
    {
        int option = JOptionPane.showConfirmDialog(null, message, "Select an Option", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION)
            return true;
        else
            return false;
    }
    
    private int execCmd(String cmdLine, boolean waitFor) throws Exception
    {
        Process process = Runtime.getRuntime().exec(cmdLine);
        
        if(!waitFor)
            return 0;
        
        StreamPumper outPumper = new StreamPumper(process.getInputStream(), System.out);
        StreamPumper errPumper = new StreamPumper(process.getErrorStream(), System.err);
        
        outPumper.start();
        errPumper.start();    
        process.waitFor();
        outPumper.join();
        errPumper.join();
        
        return process.exitValue();
    }
    
    private String execCmdWithOutput(String cmdLine) throws Exception
    {
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
    
    private class StreamPumper extends Thread
    {
        private InputStream is;
        private PrintStream os;
        
        private StringBuffer output;
        
        public StreamPumper(InputStream is, PrintStream os)
        {
            this.is = is;
            this.os = os;
            
            output = new StringBuffer();
        }
        
        public void run()
        {
            try
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                
                while ((line = br.readLine()) != null)
                {
                    output.append(line + "\n");
                    os.println(line);
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        public String getOutput()
        {
            return output.toString();
        }
    }
}

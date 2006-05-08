package com.webreach.mirth.client;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.action.*;
import org.jdesktop.swingx.auth.*;
import org.jdesktop.swingx.decorator.*;

/**
 * <p>Title: Mirth Beta Prototype</p>
 *
 * <p>Description: Mirth Beta Prototype</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: WebReach, Inc.</p>
 *
 * @author Gary Teichrow
 * @version 1.0
 */

public class Frame extends JXFrame {
    HashMap passwordMap = new HashMap();
    ActionManager manager = ActionManager.getInstance();
    JPanel contentPane;
    BorderLayout borderLayout1 = new BorderLayout();
    JMenuBar jMenuBar1 = new JMenuBar();
    JMenu jMenuFile = new JMenu();
    JMenuItem jMenuFileExit = new JMenuItem();
    JMenu jMenuHelp = new JMenu();
    JMenuItem jMenuHelpAbout = new JMenuItem();
    JLabel statusBar = new JLabel();
    JSplitPane jSplitPane1 = new JSplitPane();
    JScrollPane jScrollPane1 = new JScrollPane();
    JScrollPane jScrollPane2 = new JScrollPane();
    Component currentContentPage = null;
    StatusPanel statusListPage = new StatusPanel(this);
    ChannelPanel channelListPage = new ChannelPanel(this);
    AdminPanel adminPanel = new AdminPanel();
    JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();
    JXTaskPane viewPane;
    JXTaskPane otherPane;
    JXTaskPane adminTasks;
    JXTaskPane channelTasks;
    JXTaskPane statusTasks;
    JXTaskPane details;
    
    public Frame()
    {
        try
        {
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            jbInit();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Component initialization.
     *
     * @throws java.lang.Exception
     */
    private void jbInit() throws Exception {
        contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.setBorder(null);
        setSize(new Dimension(800, 450));
        setTitle("Mirth Client Prototype");
        statusBar.setText(" ");
        jSplitPane1.setEnabled(false);
        jSplitPane1.setDividerSize(3);
        contentPane.add(statusBar, BorderLayout.SOUTH);
        contentPane.add(jSplitPane1, java.awt.BorderLayout.CENTER);
        jSplitPane1.add(jScrollPane2, JSplitPane.RIGHT);
        jSplitPane1.add(jScrollPane1, JSplitPane.LEFT);
        jSplitPane1.setDividerLocation(170);
        jScrollPane2.getViewport().add(channelListPage);
        jScrollPane2.getViewport().add(adminPanel);
        jScrollPane2.getViewport().add(statusListPage);
        setCurrentContentPage(statusListPage);
        makePaneContainer();
        jScrollPane1.getViewport().add(taskPaneContainer);
    }

    /**
     * setCurrentContentPage
     *
     * @param statusListPage JXTable
     */
    private void setCurrentContentPage(Component contentPageObject) {
        if (contentPageObject==currentContentPage)
            return;
        if (currentContentPage!=null) {
            jScrollPane2.getViewport().remove(currentContentPage);
        }
        jScrollPane2.getViewport().add(contentPageObject);
        currentContentPage = contentPageObject;
    }

    /**
     * makePaneContainer
     *
     * @return Component
     */
    private void makePaneContainer() {
               
        // Create Action pane
        viewPane = new JXTaskPane();
        viewPane.setTitle("Mirth Views");
        viewPane.setFocusable(false);
        viewPane.add(initActionCallback("doShowStatusPanel",ActionFactory.createBoundAction("showStatusPanel","Status","S"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/status.png"))));
        viewPane.add(initActionCallback("doShowChannel",ActionFactory.createBoundAction("showChannelPannel","Channels","C"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/channel.png"))));
        viewPane.add(initActionCallback("doShowAdminPage",ActionFactory.createBoundAction("adminPage","Administration","A"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/admin.png"))));
        for (int i=0; i<viewPane.getContentPane().getComponentCount(); i++)
            viewPane.getContentPane().getComponent(i).setFocusable(false);
        taskPaneContainer.add(viewPane);
        
        // Create Admininstration Tasks Pane
        adminTasks = new JXTaskPane();
        adminTasks.setTitle("Administration Tasks");
        adminTasks.setFocusable(false);
        adminTasks.add(initActionCallback("doNewUser",ActionFactory.createBoundAction("doNewUser","New User", "N"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/add.png"))));
        adminTasks.add(initActionCallback("doSettings",ActionFactory.createBoundAction("doSettings","Settings", "S"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/edit.png"))));
        adminTasks.add(initActionCallback("doMonitor",ActionFactory.createBoundAction("doMonitor","Monitor","M"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/application.png"))));
        adminTasks.add(initActionCallback("doXML",ActionFactory.createBoundAction("doXML","XML","X"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/messages.png"))));
        for (int i=0; i<adminTasks.getContentPane().getComponentCount(); i++)
            adminTasks.getContentPane().getComponent(i).setFocusable(false);
        taskPaneContainer.add(adminTasks);
                
        // Create Channel Tasks Pane
        channelTasks = new JXTaskPane();
        channelTasks.setTitle("Channel Tasks");
        channelTasks.setFocusable(false);
        channelTasks.add(initActionCallback("doDeployAll",ActionFactory.createBoundAction("doDeployAll","Deploy All", "D"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/deployall.png"))));
        channelTasks.add(initActionCallback("doNewChannel",ActionFactory.createBoundAction("doNewChannel","New Channel", "N"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/add.png"))));
        
        channelTasks.add(initActionCallback("doEditChannel",ActionFactory.createBoundAction("doEditChannel","Edit Channel", "E"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/edit.png"))));
        channelTasks.add(initActionCallback("doDeleteChannel",ActionFactory.createBoundAction("doDeleteChannel","Delete Channel","D"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/delete.png"))));
        channelTasks.add(initActionCallback("doEnable",ActionFactory.createBoundAction("doEnable","Enable", "D"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/start.png"))));
        channelTasks.add(initActionCallback("doDisable",ActionFactory.createBoundAction("doDisable","Disable", "D"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/stop.png"))));
        for (int i=0; i<channelTasks.getContentPane().getComponentCount(); i++)
            channelTasks.getContentPane().getComponent(i).setFocusable(false);
        for (int i=2; i<channelTasks.getContentPane().getComponentCount(); i++)
            channelTasks.getContentPane().getComponent(i).setVisible(false);
        taskPaneContainer.add(channelTasks);
        
        // Create Status Tasks Pane
        statusTasks = new JXTaskPane();
        statusTasks.setTitle("Status Tasks");
        statusTasks.setFocusable(false);
        statusTasks.add(initActionCallback("doRefresh",ActionFactory.createBoundAction("doRefresh","Refresh View", "R"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/refresh.png"))));
        
        statusTasks.add(initActionCallback("doStart",ActionFactory.createBoundAction("doStart","Start", "P"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/start.png"))));
        statusTasks.add(initActionCallback("doStop",ActionFactory.createBoundAction("doStop","Stop", "P"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/stop.png"))));
        statusTasks.add(initActionCallback("doShowStats",ActionFactory.createBoundAction("doShowStats","Stats", "T"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/stats.png"))));
        statusTasks.add(initActionCallback("doShowLogs",ActionFactory.createBoundAction("doShowLogs","Logs", "L"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/logs.png"))));
        statusTasks.add(initActionCallback("doShowMessages",ActionFactory.createBoundAction("doShowMessages","Messages", "M"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/messages.png"))));
        for (int i=0; i<statusTasks.getContentPane().getComponentCount(); i++)
            statusTasks.getContentPane().getComponent(i).setFocusable(false);
        for (int i=1; i<statusTasks.getContentPane().getComponentCount(); i++)
            statusTasks.getContentPane().getComponent(i).setVisible(false);
        taskPaneContainer.add(statusTasks);
        
        //Create Other Pane
        otherPane = new JXTaskPane();
        otherPane.setTitle("Other");
        otherPane.setFocusable(false);
        otherPane.add(initActionCallback("doDisconnect",ActionFactory.createBoundAction("doDisconnect","Disconnect","D"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/disconnect.png"))));
        otherPane.add(initActionCallback("goToAbout",ActionFactory.createBoundAction("goToAbout","About Mirth","B"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/about.png"))));
        otherPane.add(initActionCallback("goToMirth",ActionFactory.createBoundAction("goToMirth","Visit MirthProject.org","I"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/home.png"))));
        for (int i=0; i<otherPane.getContentPane().getComponentCount(); i++)
            otherPane.getContentPane().getComponent(i).setFocusable(false);
        taskPaneContainer.add(otherPane);
 
        // Create Details Pane
        details = new JXTaskPane();
        details.setTitle("Details");
        taskPaneContainer.add(details);
        details.setVisible(false);
        
        doShowStatusPanel();
    }

    /**
     * initActionCallback
     *
     * @param boundAction BoundAction
     * @return Object
     */
    private BoundAction initActionCallback(String callbackMethod,BoundAction boundAction, ImageIcon icon) {
        if(icon != null)
            boundAction.putValue(Action.SMALL_ICON, icon);
        boundAction.registerCallback(this,callbackMethod);
        return boundAction;
    }
    
    public void goToMirth()
    {
        BareBonesBrowserLaunch.openURL("http://www.mirthproject.org/");
    }
    
    public void goToAbout()
    {
        //new About(this).setVisible(true);
        Frame_AboutBox dlg = new Frame_AboutBox(this);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);
    }
    
    public void doShowStatusPanel()
    {
        for (int i=0; i<viewPane.getContentPane().getComponentCount(); i++)
            viewPane.getContentPane().getComponent(i).setEnabled(true);
        viewPane.getContentPane().getComponent(0).setEnabled(false);
        setCurrentContentPage(statusListPage);
        adminTasks.setVisible(false);
        channelTasks.setVisible(false);
        statusTasks.setVisible(true);
    }
    
    public void doShowChannel()
    {
        for (int i=0; i<viewPane.getContentPane().getComponentCount(); i++)
            viewPane.getContentPane().getComponent(i).setEnabled(true);
        viewPane.getContentPane().getComponent(1).setEnabled(false);
        setCurrentContentPage(channelListPage);  
        adminTasks.setVisible(false);
        statusTasks.setVisible(false);
        channelTasks.setVisible(true);
    }

    public void doShowAdminPage()
    {
        for (int i=0; i<viewPane.getContentPane().getComponentCount(); i++)
            viewPane.getContentPane().getComponent(i).setEnabled(true);
        viewPane.getContentPane().getComponent(2).setEnabled(false);
        setCurrentContentPage(adminPanel);
        channelTasks.setVisible(false);
        statusTasks.setVisible(false); 
        adminTasks.setVisible(true);
    }
    
    public void doDisconnect()
    {
        this.dispose();
        Mirth.main(new String[0]);
        //JXLoginPanel.Status status = JXLoginPanel.showLoginDialog(null, new SimpleLoginService(passwordMap));
    }
}


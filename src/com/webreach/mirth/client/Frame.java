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

public class Frame extends JXFrame 
{
    ArrayList<ChannelInfo> channels = new ArrayList<ChannelInfo>();
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
    ChannelSetup channelEditPage;
    JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();
    JXTaskPane viewPane;
    JXTaskPane otherPane;
    JXTaskPane adminTasks;
    JXTaskPane channelTasks;
    JXTaskPane statusTasks;
    JXTaskPane details;
    JXTaskPane channelEditTasks;
    
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
    private void jbInit() throws Exception 
    {
        channelEditPage = new ChannelSetup(this);
        contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.setBorder(null);
        setSize(new Dimension(800, 450));
        setTitle("Mirth Client Prototype");
        statusBar.setText(" ");
        jSplitPane1.setDividerSize(3);
        contentPane.add(statusBar, BorderLayout.SOUTH);
        contentPane.add(jSplitPane1, java.awt.BorderLayout.CENTER);
        jSplitPane1.add(jScrollPane2, JSplitPane.RIGHT);
        jSplitPane1.add(jScrollPane1, JSplitPane.LEFT);
        jScrollPane1.setMinimumSize(new Dimension(170,0));
        jSplitPane1.setDividerLocation(170);
        setCurrentContentPage(statusListPage);
        makePaneContainer();
        jScrollPane1.getViewport().add(taskPaneContainer);
    }

    /**
     * setCurrentContentPage
     *
     * @param statusListPage JXTable
     */
    private void setCurrentContentPage(Component contentPageObject) 
    {
        if (contentPageObject==currentContentPage)
            return;
        if (currentContentPage!=null) 
        {
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
    private void makePaneContainer() 
    {
        // Create Action pane
        viewPane = new JXTaskPane();
        viewPane.setTitle("Mirth Views");
        viewPane.setFocusable(false);
        viewPane.add(initActionCallback("doShowStatusPanel",ActionFactory.createBoundAction("showStatusPanel","Status Panel","S"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/status.png"))));
        viewPane.add(initActionCallback("doShowChannel",ActionFactory.createBoundAction("showChannelPannel","Channels","C"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/channel.png"))));
        viewPane.add(initActionCallback("doShowAdminPage",ActionFactory.createBoundAction("adminPage","Administration","A"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/admin.png"))));
        setNonFocusable(viewPane);
        taskPaneContainer.add(viewPane);
        
        // Create Admininstration Tasks Pane
        adminTasks = new JXTaskPane();
        adminTasks.setTitle("Administration Tasks");
        adminTasks.setFocusable(false);
        adminTasks.add(initActionCallback("doNewUser",ActionFactory.createBoundAction("doNewUser","New User", "N"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/add.png"))));
        adminTasks.add(initActionCallback("doSettings",ActionFactory.createBoundAction("doSettings","Settings", "S"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/edit.png"))));
        adminTasks.add(initActionCallback("doMonitor",ActionFactory.createBoundAction("doMonitor","Monitor","M"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/application.png"))));
        adminTasks.add(initActionCallback("doXML",ActionFactory.createBoundAction("doXML","XML","X"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/messages.png"))));
        setNonFocusable(adminTasks);
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
        setNonFocusable(channelTasks);
        setVisibleTasks(channelTasks, 2, false);
        taskPaneContainer.add(channelTasks);
        
        // Create Channel Edit Tasks Pane
        channelEditTasks = new JXTaskPane();
        channelEditTasks.setTitle("Channel Tasks");
        channelEditTasks.setFocusable(false);
 //       channelEditTasks.add(initActionCallback("doShowChannel",ActionFactory.createBoundAction("doShowChannel","Back to Channels", "B"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/deployall.png"))));
        channelEditTasks.add(initActionCallback("doSaveChanges",ActionFactory.createBoundAction("doSaveChanges","Save Changes", "H"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/add.png"))));
//        channelEditTasks.add(initActionCallback("doApplyChanges",ActionFactory.createBoundAction("doApplyChanges","Apply Changes", "A"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/stop.png"))));
        setNonFocusable(channelEditTasks);
        setVisibleTasks(channelEditTasks, 0, false);
        taskPaneContainer.add(channelEditTasks);
        
        // Create Status Tasks Pane
        statusTasks = new JXTaskPane();
        statusTasks.setTitle("Status Tasks");
        statusTasks.setFocusable(false);
        statusTasks.add(initActionCallback("doRefresh",ActionFactory.createBoundAction("doRefresh","Refresh View", "R"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/refresh.png"))));
        statusTasks.add(initActionCallback("doStart",ActionFactory.createBoundAction("doStart","Deploy Channel", "P"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/start.png"))));
        statusTasks.add(initActionCallback("doStop",ActionFactory.createBoundAction("doStop","Stop Channel", "P"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/stop.png"))));
        statusTasks.add(initActionCallback("doShowStats",ActionFactory.createBoundAction("doShowStats","Stats", "T"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/stats.png"))));
        statusTasks.add(initActionCallback("doShowLogs",ActionFactory.createBoundAction("doShowLogs","Logs", "L"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/logs.png"))));
        statusTasks.add(initActionCallback("doShowMessages",ActionFactory.createBoundAction("doShowMessages","Messages", "M"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/messages.png"))));
        setNonFocusable(statusTasks);
        setVisibleTasks(statusTasks, 1, false);
        taskPaneContainer.add(statusTasks);
        
        //Create Other Pane
        otherPane = new JXTaskPane();
        otherPane.setTitle("Other");
        otherPane.setFocusable(false);
        otherPane.add(initActionCallback("doDisconnect",ActionFactory.createBoundAction("doDisconnect","Disconnect","D"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/disconnect.png"))));
        otherPane.add(initActionCallback("goToAbout",ActionFactory.createBoundAction("goToAbout","About Mirth","B"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/about.png"))));
        otherPane.add(initActionCallback("goToMirth",ActionFactory.createBoundAction("goToMirth","Visit MirthProject.org","I"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/home.png"))));
        setNonFocusable(otherPane);
        taskPaneContainer.add(otherPane);
 
        // Create Details Pane
        details = new JXTaskPane();
        details.setTitle("Details");
        taskPaneContainer.add(details);
        setNonFocusable(details);
        details.setVisible(false);
        
        doShowStatusPanel();
    }

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
        if (!confirmLeaveChannelEditor())
            return;
        setBold(viewPane, 0);
        setCurrentContentPage(statusListPage);
        setFocus(statusTasks);
    }
    
    public void doShowChannel()
    {
        if (!confirmLeaveChannelEditor())
            return;
        setBold(viewPane, 1);
        setCurrentContentPage(channelListPage);  
        setFocus(channelTasks);
        channelListPage.deselectRows();
    }

    public void doShowAdminPage()
    {
        if (!confirmLeaveChannelEditor())
            return;
        setBold(viewPane, 2);
        setCurrentContentPage(adminPanel);
        setFocus(adminTasks);
    }
    
    public void doDisconnect()
    {
        this.dispose();
        Mirth.main(new String[0]);
        //JXLoginPanel.Status status = JXLoginPanel.showLoginDialog(null, new SimpleLoginService(passwordMap));
    }
    
    public void doNewChannel()
    {
        setBold(viewPane, -1);
        setCurrentContentPage(channelEditPage);
        setFocus(channelEditTasks);
        setVisibleTasks(channelEditTasks, 0, false);
        channelEditPage.addChannel();
    }
    
    public void doEditChannel()
    {
        setBold(viewPane, -1);
        setCurrentContentPage(channelEditPage);
        setFocus(channelEditTasks);
        setVisibleTasks(channelEditTasks, 0, false);
        channelEditPage.editChannel(channelListPage.getSelectedChannel());
    }
    
    public void doDeleteChannel()
    {
        channelEditPage.removeChannel(channelListPage.getSelectedChannel());
        doShowChannel();
    }
    
/*    public void doSaveChanges()
    {
        if (channelEditPage.saveChanges())
        {
            setVisibleTasks(channelEditTasks, 1, false);
            doShowChannel();
        }
    }
*/        
    public void doDeployAll()
    {
        
    }
    
    public void doSaveChanges()
    {
        if (channelEditPage.saveChanges())
            setVisibleTasks(channelEditTasks, 0, false);
    }
    
    public void doShowMessages()
    {
        new Messages(this, statusListPage.getSelectedRow()).setVisible(true);
    }
    
    public void doShowLogs()
    {
        new Logs(this, statusListPage.getSelectedRow()).setVisible(true);
    }
    
    public void doShowStats()
    {
        new Stats(this, statusListPage.getSelectedRow()).setVisible(true);
    }
    
    public void setBold(JXTaskPane pane, int index)
    {
        for (int i=0; i<pane.getContentPane().getComponentCount(); i++)
            pane.getContentPane().getComponent(i).setFont(new Font("Tahoma",Font.PLAIN,11));
        if (index != -1)
            pane.getContentPane().getComponent(index).setFont(new Font("Tahoma",Font.BOLD,11));
    }
    public void setFocus(JXTaskPane pane)
    {
        channelTasks.setVisible(false);
        channelEditTasks.setVisible(false);
        statusTasks.setVisible(false);
        adminTasks.setVisible(false);
        pane.setVisible(true);
    }
    
    public void setNonFocusable(JXTaskPane pane)
    {
        for (int i=0; i<pane.getContentPane().getComponentCount(); i++)
            pane.getContentPane().getComponent(i).setFocusable(false);
    }
    
    public void setVisibleTasks(JXTaskPane pane, int startIndex, boolean visible)
    {
        for (int i=startIndex; i<pane.getContentPane().getComponentCount(); i++)
            pane.getContentPane().getComponent(i).setVisible(visible);
    }
    
    public boolean confirmLeaveChannelEditor()
    {
        if (channelEditTasks.getContentPane().getComponent(0).isVisible())
        {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the channel changes?");
            if (option == JOptionPane.YES_OPTION)
            {
                if (!channelEditPage.saveChanges())
                    return false;
            }
            else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION)
                return false;
        }
        setVisibleTasks(channelEditTasks,0,false);
        return true;
    }
}


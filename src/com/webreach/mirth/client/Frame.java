package com.webreach.mirth.client;

import com.sun.corba.se.spi.orbutil.fsm.State;
import com.webreach.mirth.client.core.Client;
import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.User;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.action.*;
import org.jdesktop.swingx.auth.*;
import org.jdesktop.swingx.decorator.*;

public class Frame extends JXFrame
{
    java.util.List<Channel> channels;
    java.util.List<User> users;
    java.util.List<ChannelStatus> status;
    Client mirthClient;
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
    StatusPanel statusListPage;
    ChannelPanel channelListPage;
    AdminPanel adminPanel;
    ChannelSetup channelEditPage;
    JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();
    JXTaskPane viewPane;
    JXTaskPane otherPane;
    JXTaskPane settingsTasks;
    JXTaskPane channelTasks;
    JXTaskPane statusTasks;
    JXTaskPane details;
    JXTaskPane channelEditTasks;
    JXTaskPane userTasks;
    ArrayList<ConnectorClass> sourceConnectors = new ArrayList<ConnectorClass>();
    ArrayList<ConnectorClass> destinationConnectors = new ArrayList<ConnectorClass>();
    
    public Frame(Client mirthClient)
    {
        this.mirthClient = mirthClient;
        
        try
        {
            channels = this.mirthClient.getChannels();
            users = this.mirthClient.getUsers();
            status = this.mirthClient.getChannelStatusList();
        }
        catch (ClientException ex)
        {
            ex.printStackTrace();
        }           
        statusListPage = new StatusPanel(this);
        channelListPage = new ChannelPanel(this);
        adminPanel = new AdminPanel(this);

        sourceConnectors.add(new DatabaseReader());
        sourceConnectors.add(new DatabaseWriter());
        sourceConnectors.add(new EmailSender());
        sourceConnectors.add(new FileWriter());
        sourceConnectors.add(new HTTPListener());
        sourceConnectors.add(new HTTPSListener());
        sourceConnectors.add(new LLPListener());
        sourceConnectors.add(new LLPSender());
        
        destinationConnectors.add(new DatabaseReader());
        destinationConnectors.add(new DatabaseWriter());
        destinationConnectors.add(new EmailSender());
        destinationConnectors.add(new FileWriter());
        destinationConnectors.add(new HTTPListener());
        destinationConnectors.add(new HTTPSListener());
        destinationConnectors.add(new LLPListener());
        destinationConnectors.add(new LLPSender());

        try
        {
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            jbInit();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

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

    public void setupChannel(Channel channel)
    {
        setCurrentContentPage(channelEditPage);
        setFocus(channelEditTasks);
        setVisibleTasks(channelEditTasks, 0, false);
        channelEditPage.addChannel(channel);
    }

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

        // Create Settings Tasks Pane
        settingsTasks = new JXTaskPane();
        settingsTasks.setTitle("Settings Tasks");
        settingsTasks.setFocusable(false);
        setNonFocusable(settingsTasks);
        taskPaneContainer.add(settingsTasks);

        // Create Channel Tasks Pane
        channelTasks = new JXTaskPane();
        channelTasks.setTitle("Channel Tasks");
        channelTasks.setFocusable(false);
        channelTasks.add(initActionCallback("doRefreshChannels",ActionFactory.createBoundAction("doRefreshChannels","Refresh", "D"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/refresh.png"))));
        channelTasks.add(initActionCallback("doDeployAll",ActionFactory.createBoundAction("doDeployAll","Deploy All", "D"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/deployall.png"))));
        channelTasks.add(initActionCallback("doNewChannel",ActionFactory.createBoundAction("doNewChannel","New Channel", "N"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/add.png"))));
        channelTasks.add(initActionCallback("doEditChannel",ActionFactory.createBoundAction("doEditChannel","Edit Channel", "E"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/edit.png"))));
        channelTasks.add(initActionCallback("doDeleteChannel",ActionFactory.createBoundAction("doDeleteChannel","Delete Channel","D"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/delete.png"))));
        channelTasks.add(initActionCallback("doEnable",ActionFactory.createBoundAction("doEnable","Enable", "D"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/start.png"))));
        channelTasks.add(initActionCallback("doDisable",ActionFactory.createBoundAction("doDisable","Disable", "D"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/stop.png"))));
        setNonFocusable(channelTasks);
        setVisibleTasks(channelTasks, 3, false);
        taskPaneContainer.add(channelTasks);

        // Create Channel Edit Tasks Pane
        channelEditTasks = new JXTaskPane();
        channelEditTasks.setTitle("Channel Tasks");
        channelEditTasks.setFocusable(false);
//      channelEditTasks.add(initActionCallback("doShowChannel",ActionFactory.createBoundAction("doShowChannel","Back to Channels", "B"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/deployall.png"))));
        channelEditTasks.add(initActionCallback("doSaveChanges",ActionFactory.createBoundAction("doSaveChanges","Save Changes", "H"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/start.png"))));
//      channelEditTasks.add(initActionCallback("doApplyChanges",ActionFactory.createBoundAction("doApplyChanges","Apply Changes", "A"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/stop.png"))));
        channelEditTasks.add(initActionCallback("doNewDestination",ActionFactory.createBoundAction("doNewDestination","New Destination", "N"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/add.png"))));
        channelEditTasks.add(initActionCallback("doDeleteDestination",ActionFactory.createBoundAction("doDeleteDestination","Delete Destination", "D"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/delete.png"))));
        channelEditTasks.add(initActionCallback("doEditTransformer",ActionFactory.createBoundAction("doEditTransformer","Edit Transformer", "D"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/edit.png"))));
        channelEditTasks.add(initActionCallback("doEditFilter",ActionFactory.createBoundAction("doEditFilter","Edit Filter", "D"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/edit.png"))));
        channelEditTasks.add(initActionCallback("doEditValidator",ActionFactory.createBoundAction("doEditValidator","Edit Validator", "D"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/edit.png"))));
        setNonFocusable(channelEditTasks);
        setVisibleTasks(channelEditTasks, 0, false);
        taskPaneContainer.add(channelEditTasks);

        // Create Status Tasks Pane
        statusTasks = new JXTaskPane();
        statusTasks.setTitle("Status Tasks");
        statusTasks.setFocusable(false);
        statusTasks.add(initActionCallback("doRefresh",ActionFactory.createBoundAction("doRefresh","Refresh", "R"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/refresh.png"))));
        statusTasks.add(initActionCallback("doStartAll",ActionFactory.createBoundAction("doStartAll","Start All Channels", "P"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/start1.png"))));
        statusTasks.add(initActionCallback("doStart",ActionFactory.createBoundAction("doStart","Start Channel", "P"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/start.png"))));
        statusTasks.add(initActionCallback("doPause",ActionFactory.createBoundAction("doPause","Pause Channel", "P"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/pause.png"))));
        statusTasks.add(initActionCallback("doStop",ActionFactory.createBoundAction("doStop","Stop Channel", "P"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/stop.png"))));
        statusTasks.add(initActionCallback("doShowStats",ActionFactory.createBoundAction("doShowStats","Stats", "T"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/stats.png"))));
        statusTasks.add(initActionCallback("doShowLogs",ActionFactory.createBoundAction("doShowLogs","Logs", "L"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/logs.png"))));
        statusTasks.add(initActionCallback("doShowMessages",ActionFactory.createBoundAction("doShowMessages","Messages", "M"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/messages.png"))));
        setNonFocusable(statusTasks);
        setVisibleTasks(statusTasks, 2, false);
        taskPaneContainer.add(statusTasks);

        // Create User Tasks Pane
        userTasks = new JXTaskPane();
        userTasks.setTitle("User Tasks");
        userTasks.setFocusable(false);
        userTasks.add(initActionCallback("doRefreshUser",ActionFactory.createBoundAction("doRefreshUser","Refresh", "R"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/refresh.png"))));
        userTasks.add(initActionCallback("doNewUser",ActionFactory.createBoundAction("doNewChannel","New User", "N"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/add.png"))));
        userTasks.add(initActionCallback("doEditUser",ActionFactory.createBoundAction("doEditChannel","Edit User", "E"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/edit.png"))));
        userTasks.add(initActionCallback("doDeleteUser",ActionFactory.createBoundAction("doDeleteChannel","Delete User","D"), new ImageIcon(com.webreach.mirth.client.Frame.class.getResource("images/delete.png"))));
        setNonFocusable(userTasks);
        setVisibleTasks(userTasks, 2, false);
        taskPaneContainer.add(userTasks);

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

    private BoundAction initActionCallback(String callbackMethod,BoundAction boundAction, ImageIcon icon)
    {
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
        doRefresh();
        setBold(viewPane, 0);
        setCurrentContentPage(statusListPage);
        setFocus(statusTasks);
    }

    public void doShowChannel()
    {
        if (!confirmLeaveChannelEditor())
            return;
        doRefreshChannels();
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
        adminPanel.showTasks();
    }

    public void doDisconnect()
    {
        this.dispose();
        Mirth.main(new String[0]);
    }

    public void doNewChannel()
    {
        ChannelWizard channelWizard = new ChannelWizard(this);
        Dimension channelWizardSize = channelWizard.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        channelWizard.setLocation((frmSize.width - channelWizardSize.width) / 2 + loc.x,
                        (frmSize.height - channelWizardSize.height) / 2 + loc.y);
        channelWizard.setModal(true);
        channelWizard.setResizable(false);
        channelWizard.setVisible(true);
    }

    public void doEditChannel()
    {
        doRefreshChannels();

        if (channelListPage.getSelectedChannel() == -1)
            JOptionPane.showMessageDialog(this, "Channel no longer exists.");
        else
        {
            setBold(viewPane, -1);
            setCurrentContentPage(channelEditPage);
            setFocus(channelEditTasks);
            setVisibleTasks(channelEditTasks, 0, false);
            channelEditPage.editChannel(channelListPage.getSelectedChannel());
        }
    }

    public void doDeleteChannel()
    {
        try
        {
            mirthClient.removeChannel(channels.get(channelListPage.getSelectedChannel()).getId());
            channels = mirthClient.getChannels();
            channelListPage.makeChannelTable();
        }
        catch (ClientException e)
        {
            e.printStackTrace();
        }
        doShowChannel();
    }

    public void doRefreshChannels()
    {
        int channelId = -1;
        String channelName = null;

        if(channelListPage.getSelectedChannel() != -1)
            channelId = channels.get(channelListPage.getSelectedChannel()).getId();

        try
        {
            channels = mirthClient.getChannels();
            channelListPage.makeChannelTable();

            for(int i = 0; i<channels.size(); i++)
            {
                if(channelId == channels.get(i).getId())
                    channelName = channels.get(i).getName();
            }
        }
        catch (ClientException ex)
        {
            ex.printStackTrace();
        }

        // as long as the channel was not deleted
        if (channelName != null)
            channelListPage.setSelectedChannel(channelName);
    }
    
    public void doRefresh()
    {
        //dummy code
        
        status = new ArrayList<ChannelStatus>();
        ChannelStatus status1 = new ChannelStatus();
        status1.setChannelId(1);
        status1.setName("YO");
        status1.setState(ChannelStatus.State.PAUSED);
        ChannelStatus status2 = new ChannelStatus();
        status2.setChannelId(2);
        status2.setName("YO1");
        status2.setState(ChannelStatus.State.STOPPED);  
        ChannelStatus status3 = new ChannelStatus();
        status3.setChannelId(3);
        status3.setName("YO2");
        status3.setState(ChannelStatus.State.STARTED);  
        status.add(status1);
        status.add(status2);
        status.add(status3);
        statusListPage.makeStatusTable();
        
        //end dummy code
        
        //real code below
        /*try
        {
            status = mirthClient.getStatusList();
            statusListPage.makeStatusTable();
        }
        catch (ClientException ex)
        {
            ex.printStackTrace();
        }*/
    }
    
    public void doStartAll()
    {
        try
        {
            for(int i = 0; i<status.size(); i++)
            {
                if(status.get(i).getState() == ChannelStatus.State.STOPPED)
                    mirthClient.startChannel(status.get(i).getChannelId());
                else if(status.get(i).getState() == ChannelStatus.State.PAUSED)
                    mirthClient.resumeChannel(status.get(i).getChannelId());
            }
        } 
        catch (ClientException ex)
        {
            ex.printStackTrace();
        }
        doRefresh();
    }
    
    public void doStart()
    {
        try
        {
            if(status.get(statusListPage.getSelectedStatus()).getState() == ChannelStatus.State.STOPPED)
                mirthClient.startChannel(status.get(statusListPage.getSelectedStatus()).getChannelId());
            else if(status.get(statusListPage.getSelectedStatus()).getState() == ChannelStatus.State.PAUSED)
                mirthClient.resumeChannel(status.get(statusListPage.getSelectedStatus()).getChannelId());
        } 
        catch (ClientException ex)
        {
            ex.printStackTrace();
        }
        doRefresh();
    }
    
    public void doStop()
    {
        try
        {
            mirthClient.stopChannel(status.get(statusListPage.getSelectedStatus()).getChannelId());
        } 
        catch (ClientException ex)
        {
            ex.printStackTrace();
        }
        doRefresh();
    }
    
    public void doPause()
    {
        try
        {
            mirthClient.pauseChannel(status.get(statusListPage.getSelectedStatus()).getChannelId());
        } 
        catch (ClientException ex)
        {
            ex.printStackTrace();
        }
        doRefresh();
    }
    
    public void doNewDestination()
    {
        channelEditPage.addNewDestination();
    }
    
    public void doDeleteDestination()
    {
        channelEditPage.deleteDestination();
    }
    
    public void doEnable()
    {
       doRefreshChannels();

        if (channelListPage.getSelectedChannel() == -1)
            JOptionPane.showMessageDialog(this, "Channel no longer exists.");
        else
        {
            Channel channel = channels.get(channelListPage.getSelectedChannel());
            channel.setEnabled(true);
            updateChannel(channel);
            channelListPage.deselectRows();
            channelListPage.setSelectedChannel(channel.getName());
        }
    }
    
    public void doDisable()
    {
        doRefreshChannels();

        if (channelListPage.getSelectedChannel() == -1)
            JOptionPane.showMessageDialog(this, "Channel no longer exists.");
        else
        {
            Channel channel = channels.get(channelListPage.getSelectedChannel());
            channel.setEnabled(false);
            updateChannel(channel);
            channelListPage.deselectRows();
            channelListPage.setSelectedChannel(channel.getName());
        }
    }

    public void doNewUser()
    {
        UserWizard userWizard = new UserWizard(this, -1);
        Dimension userWizardSize = userWizard.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        userWizard.setLocation((frmSize.width - userWizardSize.width) / 2 + loc.x,
                        (frmSize.height - userWizardSize.height) / 2 + loc.y);
        userWizard.setModal(true);
        userWizard.setResizable(false);
        userWizard.setVisible(true);
    }

    public void doEditUser()
    {
        doRefreshUser();

        if (adminPanel.u.getUserIndex() == -1)
            JOptionPane.showMessageDialog(this, "Users no longer exists.");
        else
        {
            UserWizard userDialog = new UserWizard(this, adminPanel.u.getSelectedRow());
            Dimension dialogSize = userDialog.getPreferredSize();
            Dimension frmSize = getSize();
            Point loc = getLocation();
            userDialog.setLocation((frmSize.width - dialogSize.width) / 2 + loc.x,
                            (frmSize.height - dialogSize.height) / 2 + loc.y);
            userDialog.setResizable(false);
            userDialog.setVisible(true);
        }
    }

    public void doDeleteUser()
    {
        int userToDelete = adminPanel.u.getUserIndex();
        try
        {
           if(userToDelete != -1) 
           {
                mirthClient.removeUser(users.get(userToDelete).getId());
                users = mirthClient.getUsers();
                adminPanel.u.makeUsersTable();
           }
        }
        catch (ClientException ex)
        {
            ex.printStackTrace();
        }
    }

    public void doRefreshUser()
    {
        int userId = -1;
        String userName = null;

        if(adminPanel.u.getUserIndex() != -1)
            userId = users.get(adminPanel.u.getUserIndex()).getId();

        try
        {
            users = mirthClient.getUsers();
            adminPanel.u.makeUsersTable();

            for(int i = 0; i<users.size(); i++)
            {
                if(userId == users.get(i).getId())
                    userName = users.get(i).getUsername();
            }
        }
        catch (ClientException ex)
        {
            ex.printStackTrace();
        }

        // as long as the channel was not deleted
        if (userName != null)
            adminPanel.u.setSelectedUser(userName);
    }

/*  public void doSaveChanges()
    {
        if (channelEditPage.saveChanges())
        {
            setVisibleTasks(channelEditTasks, 0, false);
            doShowChannel();
        }
    }
*/
    public void doDeployAll()
    {
        doRefreshChannels();
        try
        {
            mirthClient.deployChannels();
        }
        catch (ClientException ex)
        {
            ex.printStackTrace();
        }
    }

    public void doSaveChanges()
    {
        if (channelEditPage.saveChanges())
            setVisibleTasks(channelEditTasks, 0, false);
    }

    public void doShowMessages()
    {
        new Messages(this, statusListPage.statusTable.getSelectedRow());
    }

    public void doShowLogs()
    {
        new Logs(this, statusListPage.statusTable.getSelectedRow());
    }

    public void doShowStats()
    {
        new Stats(this, statusListPage.statusTable.getSelectedRow());
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
        settingsTasks.setVisible(false);
        userTasks.setVisible(false);
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

    public void updateChannel(Channel curr)
    {
        try
        {
            mirthClient.updateChannel(curr);
            channels = mirthClient.getChannels();
            channelListPage.makeChannelTable();
        }
        catch (ClientException ex)
        {
            ex.printStackTrace();
        }
    }

    public void updateUser(User curr)
    {
        try
        {
            mirthClient.updateUser(curr);
            users = mirthClient.getUsers();
            adminPanel.u.makeUsersTable();
        }
        catch (ClientException ex)
        {
            ex.printStackTrace();
        }
    }
}


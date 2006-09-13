/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.client.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.action.ActionFactory;
import org.jdesktop.swingx.action.ActionManager;
import org.jdesktop.swingx.action.BoundAction;
import org.jdesktop.swingx.border.DropShadowBorder;

import com.webreach.mirth.client.core.Client;
import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.browsers.event.EventBrowser;
import com.webreach.mirth.client.ui.browsers.message.MessageBrowser;
import com.webreach.mirth.client.ui.connectors.ConnectorClass;
import com.webreach.mirth.client.ui.connectors.DatabaseReader;
import com.webreach.mirth.client.ui.connectors.DatabaseWriter;
import com.webreach.mirth.client.ui.connectors.EmailSender;
import com.webreach.mirth.client.ui.connectors.FileReader;
import com.webreach.mirth.client.ui.connectors.FileWriter;
import com.webreach.mirth.client.ui.connectors.HTTPListener;
import com.webreach.mirth.client.ui.connectors.HTTPSListener;
import com.webreach.mirth.client.ui.connectors.JMSWriter;
import com.webreach.mirth.client.ui.connectors.LLPListener;
import com.webreach.mirth.client.ui.connectors.LLPSender;
import com.webreach.mirth.client.ui.util.FileUtil;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

/**
 * The main conent frame for the Mirth Client Application.
 * Extends JXFrame and sets up all content.
 */
public class Frame extends JXFrame
{
    public Client mirthClient;

    public StatusPanel statusListPage;
    public ChannelPanel channelListPage;
    public AdminPanel adminPanel;
    public ChannelSetup channelEditPage;
    public EventBrowser eventBrowser;
    public MessageBrowser messageBrowser;
    public JXTaskPaneContainer taskPaneContainer;
    public List<ChannelStatus> status;

    public List<Channel> channels;
    public List<User> users;

    public ActionManager manager = ActionManager.getInstance();
    public JPanel contentPanel;
    public BorderLayout borderLayout1 = new BorderLayout();
    public StatusBar statusBar;
    public JSplitPane splitPane = new JSplitPane();
    public JScrollPane taskPane = new JScrollPane();
    public JScrollPane contentPane = new JScrollPane();
    public Component currentContentPage = null;
    public JXTaskPaneContainer currentTaskPaneContainer = null;

    public JXTaskPane viewPane;
    public JXTaskPane otherPane;
    public JXTaskPane settingsTasks;
    public JPopupMenu settingsPopupMenu;
    public JXTaskPane channelTasks;
    public JPopupMenu channelPopupMenu;
    public JXTaskPane statusTasks;
    public JPopupMenu statusPopupMenu;
    public JXTaskPane eventTasks;
    public JPopupMenu eventPopupMenu;
    public JXTaskPane messageTasks;
    public JPopupMenu messagePopupMenu;
    public JXTaskPane details;
    public JXTaskPane channelEditTasks;
    public JPopupMenu channelEditPopupMenu;
    public JXTaskPane userTasks;
    public JPopupMenu userPopupMenu;

    public JXTitledPanel rightContainer;
    public JXTitledPanel leftContainer;

    public ArrayList<ConnectorClass> sourceConnectors;
    public ArrayList<ConnectorClass> destinationConnectors;

    private Thread statusUpdater;
    private DropShadowBorder dsb;
    private static Preferences userPreferences;
    private StatusUpdater su;
    
    /**
     * Builds the content panel with a title bar and settings.
     */
    private void buildContentPanel(JXTitledPanel container, JScrollPane component, boolean opaque)
    {
        container.getContentContainer().setLayout(new BorderLayout());
        container.setBorder(dsb);
        container.setTitleFont(new Font("Tahoma",Font.BOLD,12));
        container.setTitleForeground(UIManager.getColor("windowText"));
        container.getContentContainer().add(component);
        if(UIManager.getColor("TaskPaneContainer.backgroundGradientStart") != null)
            container.setTitleDarkBackground(UIManager.getColor("TaskPaneContainer.backgroundGradientStart"));
        else
            container.setTitleDarkBackground(UIManager.getColor("InternalFrame.activeTitleBackground"));

        if(UIManager.getColor("TaskPaneContainer.backgroundGradientEnd") != null)
            container.setTitleLightBackground(UIManager.getColor("TaskPaneContainer.backgroundGradientEnd"));
        else
            container.setTitleDarkBackground(UIManager.getColor("InternalFrame.inactiveTitleBackground"));
    }

    /**
     * Set the main content panel title to a String
     */
    public void setPanelName(String name)
    {
        rightContainer.setTitle(name);
    }

    /**
     * Called to set up this main window frame.  Calls jbInit() as well.
     */
    public void setupFrame(Client mirthClient)
    {
        dsb = new DropShadowBorder(UIManager.getColor("Control"), 0, 3, .3f, 12, true, true, true, true);
        leftContainer = new JXTitledPanel();
        rightContainer = new JXTitledPanel();

        this.mirthClient = mirthClient;
        this.setIconImage(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/emoticon_smile.png")).getImage());
        
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        userPreferences = Preferences.systemNodeForPackage(Mirth.class);
        userPreferences.put("defaultServer", PlatformUI.SERVER_NAME);
        
        try
        {
            channels = this.mirthClient.getChannels();
            users = this.mirthClient.getUsers();
            status = this.mirthClient.getChannelStatusList();
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        sourceConnectors = new ArrayList<ConnectorClass>();
        sourceConnectors.add(new DatabaseReader());
        sourceConnectors.add(new HTTPListener());
        sourceConnectors.add(new HTTPSListener());
        sourceConnectors.add(new LLPListener());
        sourceConnectors.add(new FileReader());

        destinationConnectors = new ArrayList<ConnectorClass>();
        destinationConnectors.add(new DatabaseWriter());
        destinationConnectors.add(new EmailSender());
        destinationConnectors.add(new FileWriter());
        destinationConnectors.add(new LLPSender());
        destinationConnectors.add(new JMSWriter());

        taskPaneContainer = new JXTaskPaneContainer();

        statusListPage = new StatusPanel();
        channelListPage = new ChannelPanel();
        adminPanel = new AdminPanel();
        channelEditPage = new ChannelSetup();
        eventBrowser = new EventBrowser();
        messageBrowser = new MessageBrowser();
        
        /*
        FOR DEBUGGING THE UIDefaults:
 
        UIDefaults uiDefaults = UIManager.getDefaults();
        Enumeration enum1 = uiDefaults.keys();
        while (enum1.hasMoreElements())
        {
            Object key = enum1.nextElement();
            Object val = uiDefaults.get(key);
            if(key.toString().indexOf("font") != -1)
                System.out.println("UIManager.put(\"" + key.toString() + "\",\"" +
                    (null != val ? val.toString() : "(null)") +
                    "\");");
        } 
        */
        try
        {
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            jbInit();
        }
        catch (Exception e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
        su = new StatusUpdater();
        statusUpdater = new Thread(su);
        statusUpdater.start();
    }

    /**
     * Sets up the layout information and calls makePaneContainer() to make the task panes.
     */
    private void jbInit() throws Exception
    {
        contentPanel = (JPanel) getContentPane();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(null);
        setTitle(UIConstants.TITLE_TEXT);
        statusBar = new StatusBar();
        splitPane.setDividerSize(0);
        contentPanel.add(statusBar, BorderLayout.SOUTH);
        contentPanel.add(splitPane, java.awt.BorderLayout.CENTER);

        ///buildContentPanel(leftContainer, taskPane, false, "User Options");
        buildContentPanel(rightContainer, contentPane, false);

        splitPane.add(rightContainer, JSplitPane.RIGHT);
        splitPane.add(taskPane, JSplitPane.LEFT);
        taskPane.setMinimumSize(new Dimension(UIConstants.TASK_PANE_WIDTH,0));
        splitPane.setDividerLocation(UIConstants.TASK_PANE_WIDTH);
        setCurrentContentPage(statusListPage);
        makePaneContainer();
        setCurrentTaskPaneContainer(taskPaneContainer);
        adminPanel.loadSettings();

        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                if (!confirmLeave())
                    return;
                
                userPreferences = Preferences.systemNodeForPackage(Mirth.class);
                userPreferences.putInt("maximizedState", getExtendedState());
                userPreferences.putInt("width", getWidth());
                userPreferences.putInt("height", getHeight());
                doLogout();
                System.exit(0);
            }
        });
    }

    /**
     * Changes the current content page to the Channel Editor with the new
     * channel specified as the loaded one.
     */
    public void setupChannel(Channel channel)
    {
        setCurrentContentPage(channelEditPage);
        setBold(viewPane,UIConstants.ERROR_CONSTANT);
        setFocus(channelEditTasks);
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 0, 0, false);
        channelEditPage.addChannel(channel);
    }

    /**
     * Edits a channel at a specified index, setting that channel
     * as the current channel in the editor.
     */
    public void editChannel(int index)
    {
        setBold(viewPane, UIConstants.ERROR_CONSTANT);
        setCurrentContentPage(channelEditPage);
        setFocus(channelEditTasks);
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 0, 4, false);
        channelEditPage.editChannel(index);
    }

    /**
     * Sets the current content page to the passed in page.
     */
    public void setCurrentContentPage(Component contentPageObject)
    {
        if (contentPageObject==currentContentPage)
            return;

        if (currentContentPage!=null)
            contentPane.getViewport().remove(currentContentPage);

        contentPane.getViewport().add(contentPageObject);
        currentContentPage = contentPageObject;
    }

    /**
     * Sets the current task pane container
     */
    public void setCurrentTaskPaneContainer(JXTaskPaneContainer container)
    {
        if (container==currentTaskPaneContainer)
            return;

        if (currentTaskPaneContainer!=null)
            taskPane.getViewport().remove(currentTaskPaneContainer);

        taskPane.getViewport().add(container);
        currentTaskPaneContainer = container;
    }

    /**
     * Makes all of the task panes and shows the status panel.
     */
    private void makePaneContainer()
    {
        createViewPane();
        createSettingsPane();
        createChannelPane();
        createChannelEditPane();
        createStatusPane();
        createEventPane();
        createMessagePane();
        createUserPane();
        createOtherPane();
        createDetailsPane();

        doShowStatusPanel();
    }

    /**
     * Creates the view task pane.
     */
    private void createViewPane()
    {
        // Create View pane
        viewPane = new JXTaskPane();
        viewPane.setTitle("Mirth");
        viewPane.setFocusable(false);
        viewPane.add(initActionCallback("doShowStatusPanel", "Contains information about your currently deployed channels.", ActionFactory.createBoundAction("showStatusPanel","Status","S"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/status.png"))));
        viewPane.add(initActionCallback("doShowChannel", "Contains various operations to perform on your channels.", ActionFactory.createBoundAction("showChannelPannel","Channels","C"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/channel.png"))));
        viewPane.add(initActionCallback("doShowAdminPage", "Contains user and system settings.", ActionFactory.createBoundAction("adminPage","Administration","A"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/admin.png"))));
        setNonFocusable(viewPane);
        taskPaneContainer.add(viewPane);
    }

     /**
     * Creates the settings task pane.
     */
    private void createSettingsPane()
    {
        // Create Settings Tasks Pane
        settingsTasks = new JXTaskPane();
        settingsPopupMenu = new JPopupMenu();
        settingsTasks.setTitle("Settings Tasks");
        settingsTasks.setFocusable(false);

        settingsTasks.add(initActionCallback("doRefreshSettings", "Refresh settings.", ActionFactory.createBoundAction("doRefreshSettings","Refresh", "R"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png"))));
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        refresh.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doRefreshSettings();
            }
        });
        settingsPopupMenu.add(refresh);

        settingsTasks.add(initActionCallback("doSaveSettings", "Save settings.", ActionFactory.createBoundAction("doSaveSettings","Save Settings", "E"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png"))));
        JMenuItem saveSettings = new JMenuItem("Save Settings");
        saveSettings.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png")));
        saveSettings.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doSaveSettings();
            }
        });
        settingsPopupMenu.add(saveSettings);

        setNonFocusable(settingsTasks);
        setVisibleTasks(settingsTasks, settingsPopupMenu, 0, 0, true);
        setVisibleTasks(settingsTasks, settingsPopupMenu, 1, 1, false);
        taskPaneContainer.add(settingsTasks);
    }

    /**
     * Creates the channel task pane.
     */
    private void createChannelPane()
    {
        // Create Channel Tasks Pane
        channelTasks = new JXTaskPane();
        channelPopupMenu = new JPopupMenu();
        channelTasks.setTitle("Channel Tasks");
        channelTasks.setFocusable(false);

        channelTasks.add(initActionCallback("doRefreshChannels", "Refresh the list of channels.", ActionFactory.createBoundAction("doRefreshChannels","Refresh", "R"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png"))));
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        refresh.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doRefreshChannels();
            }
        });
        channelPopupMenu.add(refresh);

        channelTasks.add(initActionCallback("doDeployAll", "Deploy all currently enabled channels.", ActionFactory.createBoundAction("doDeployAll","Deploy All", "P"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/deployall.png"))));
        JMenuItem deployAll = new JMenuItem("Deploy All");
        deployAll.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/deployall.png")));
        deployAll.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doDeployAll();
            }
        });
        channelPopupMenu.add(deployAll);

        channelTasks.add(initActionCallback("doNewChannel", "Create a new channel.", ActionFactory.createBoundAction("doNewChannel","New Channel", "N"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/add.png"))));
        JMenuItem newChannel = new JMenuItem("New Channel");
        newChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/add.png")));
        newChannel.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doNewChannel();
            }
        });
        channelPopupMenu.add(newChannel);

        channelTasks.add(initActionCallback("doImport", "Import a channel from an XML file.", ActionFactory.createBoundAction("doImport","Import Channel", "M"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/import.png"))));
        JMenuItem importChannel = new JMenuItem("Import Channel");
        importChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/import.png")));
        importChannel.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doImport();
            }
        });
        channelPopupMenu.add(importChannel);
        
        channelTasks.add(initActionCallback("doExportAll", "Export all of the channels to XML files.", ActionFactory.createBoundAction("doExportAll","Export All Channels", "O"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png"))));
        JMenuItem exportAllChannels = new JMenuItem("Export All Channels");
        exportAllChannels.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png")));
        exportAllChannels.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doExportAll();
            }
        });
        channelPopupMenu.add(exportAllChannels);
        
        channelTasks.add(initActionCallback("doExport", "Export the currently selected channel to an XML file.", ActionFactory.createBoundAction("doExport","Export Channel", "X"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png"))));
        JMenuItem exportChannel = new JMenuItem("Export Channel");
        exportChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png")));
        exportChannel.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doExport();
            }
        });
        channelPopupMenu.add(exportChannel);

        channelTasks.add(initActionCallback("doEditChannel", "Edit the currently selected channel.", ActionFactory.createBoundAction("doEditChannel","Edit Channel", "E"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/edit.png"))));
        JMenuItem editChannel = new JMenuItem("Edit Channel");
        editChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/edit.png")));
        editChannel.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doEditChannel();
            }
        });
        channelPopupMenu.add(editChannel);

        channelTasks.add(initActionCallback("doDeleteChannel", "Delete the currently selected channel.", ActionFactory.createBoundAction("doDeleteChannel","Delete Channel","D"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png"))));
        JMenuItem deleteChannel = new JMenuItem("Delete Channel");
        deleteChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")));
        deleteChannel.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doDeleteChannel();
            }
        });
        channelPopupMenu.add(deleteChannel);

        channelTasks.add(initActionCallback("doEnable", "Enable the currently selected channel.", ActionFactory.createBoundAction("doEnable","Enable Channel", "B"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start.png"))));
        JMenuItem enableChannel = new JMenuItem("Enable Channel");
        enableChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start.png")));
        enableChannel.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doEnable();
            }
        });
        channelPopupMenu.add(enableChannel);

        channelTasks.add(initActionCallback("doDisable", "Disable the currently selected channel.", ActionFactory.createBoundAction("doDisable","Disable Channel", "L"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stop.png"))));
        JMenuItem disableChannel = new JMenuItem("Disable Channel");
        disableChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stop.png")));
        disableChannel.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doDisable();
            }
        });
        channelPopupMenu.add(disableChannel);

        setNonFocusable(channelTasks);
        setVisibleTasks(channelTasks, channelPopupMenu, 1, 1, false);
        setVisibleTasks(channelTasks, channelPopupMenu, 5, -1, false);
        taskPaneContainer.add(channelTasks);
    }

    /**
     * Creates the channel edit task pane.
     */
    private void createChannelEditPane()
    {
        // Create Channel Edit Tasks Pane
        channelEditTasks = new JXTaskPane();
        channelEditPopupMenu = new JPopupMenu();
        channelEditTasks.setTitle("Channel Tasks");
        channelEditTasks.setFocusable(false);

        channelEditTasks.add(initActionCallback("doSaveChanges", "Save all changes made to this channel.", ActionFactory.createBoundAction("doSaveChanges","Save Changes", "E"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png"))));
        JMenuItem saveChanges = new JMenuItem("Save Changes");
        saveChanges.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png")));
        saveChanges.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doSaveChanges();
            }
        });
        channelEditPopupMenu.add(saveChanges);
        
        channelEditTasks.add(initActionCallback("doValidate", "Validate the currently visible form.", ActionFactory.createBoundAction("doValidate","Validate Form", "V"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/accept.png"))));
        JMenuItem validateForm = new JMenuItem("Validate Form");
        validateForm.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/accept.png")));
        validateForm.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doValidate();
            }
        });
        channelEditPopupMenu.add(validateForm);
        
        channelEditTasks.add(initActionCallback("doNewDestination", "Create a new destination.", ActionFactory.createBoundAction("doNewDestination","New Destination", "N"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/add.png"))));
        JMenuItem newDestination = new JMenuItem("New Destination");
        newDestination.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/add.png")));
        newDestination.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doNewDestination();
            }
        });
        channelEditPopupMenu.add(newDestination);
        
        channelEditTasks.add(initActionCallback("doDeleteDestination", "Delete the currently selected destination.", ActionFactory.createBoundAction("doDeleteDestination","Delete Destination", "D"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png"))));
        JMenuItem deleteDestination = new JMenuItem("Delete Destination");
        deleteDestination.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")));
        deleteDestination.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doDeleteDestination();
            }
        });
        channelEditPopupMenu.add(deleteDestination);
        
        channelEditTasks.add(initActionCallback("doMoveDestinationUp", "Move the currently selected destination up.", ActionFactory.createBoundAction("doMoveDestinationUp","Move Dest. Up", "M"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/arrow_up.png"))));
        JMenuItem moveDestinationUp = new JMenuItem("Move Destination Up");
        moveDestinationUp.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/arrow_up.png")));
        moveDestinationUp.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doMoveDestinationUp();
            }
        });
        channelEditPopupMenu.add(moveDestinationUp);
        
        channelEditTasks.add(initActionCallback("doMoveDestinationDown", "Move the currently selected destination down.", ActionFactory.createBoundAction("doMoveDestinationDown","Move Dest. Down", "W"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/arrow_down.png"))));
        JMenuItem moveDestinationDown = new JMenuItem("Move Destination Down");
        moveDestinationDown.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/arrow_down.png")));
        moveDestinationDown.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doMoveDestinationDown();
            }
        });
        channelEditPopupMenu.add(moveDestinationDown);

        channelEditTasks.add(initActionCallback("doEditTransformer", "Edit the transformer for the currently selected destination.", ActionFactory.createBoundAction("doEditTransformer","Edit Transformer", "T"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/edit.png"))));
        JMenuItem editTransformer = new JMenuItem("Edit Transformer");
        editTransformer.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/edit.png")));
        editTransformer.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doEditTransformer();
            }
        });
        channelEditPopupMenu.add(editTransformer);

        channelEditTasks.add(initActionCallback("doEditFilter", "Edit the filter for the currently selected destination.", ActionFactory.createBoundAction("doEditFilter","Edit Filter", "F"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/edit.png"))));
        JMenuItem editFilter = new JMenuItem("Edit Filter");
        editFilter.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/edit.png")));
        editFilter.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doEditFilter();
            }
        });
        channelEditPopupMenu.add(editFilter);

        channelEditTasks.add(initActionCallback("doExport", "Export the currently selected channel to an XML file.", ActionFactory.createBoundAction("doExport","Export Channel", "X"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png"))));
        JMenuItem exportChannel = new JMenuItem("Export Channel");
        exportChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png")));
        exportChannel.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doExport();
            }
        });
        channelEditPopupMenu.add(exportChannel);

        setNonFocusable(channelEditTasks);
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 0, 6, false);
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 7, 7, true);
        taskPaneContainer.add(channelEditTasks);
    }

    /**
     * Creates the status task pane.
     */
    private void createStatusPane()
    {
        // Create Status Tasks Pane
        statusTasks = new JXTaskPane();
        statusPopupMenu = new JPopupMenu();
        statusTasks.setTitle("Status Tasks");
        statusTasks.setFocusable(false);

        statusTasks.add(initActionCallback("doRefresh", "Refresh the list of statuses.", ActionFactory.createBoundAction("doRefresh","Refresh", "R"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png"))));
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        refresh.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doRefresh();
            }
        });
        statusPopupMenu.add(refresh);

        statusTasks.add(initActionCallback("doStartAll", "Start all channels that are currently deployed.", ActionFactory.createBoundAction("doStartAll","Start All Channels", "T"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start1.png"))));
        JMenuItem startAllChannels = new JMenuItem("Start All Channels");
        startAllChannels.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start1.png")));
        startAllChannels.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doStartAll();
            }
        });
        statusPopupMenu.add(startAllChannels);

        statusTasks.add(initActionCallback("doShowEvents", "Show the event logs for the system.", ActionFactory.createBoundAction("doShowEvents","View System Events", "Y"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/logs.png"))));
        JMenuItem showEvents = new JMenuItem("View System Events");
        showEvents.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/logs.png")));
        showEvents.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doShowEvents();
            }
        });
        statusPopupMenu.add(showEvents);

        statusTasks.add(initActionCallback("doShowMessages", "Show the messages for the currently selected channel.", ActionFactory.createBoundAction("doShowMessages","View Messages", "M"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/messages.png"))));
        JMenuItem showMessages = new JMenuItem("View Messages");
        showMessages.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/messages.png")));
        showMessages.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doShowMessages();
            }
        });
        statusPopupMenu.add(showMessages);
        
        statusTasks.add(initActionCallback("doClearStats", "Clear the statistics for a selected channel.", ActionFactory.createBoundAction("doClearStats","Clear Statistics", "C"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png"))));
        JMenuItem clearStats = new JMenuItem("Clear Statistics");
        clearStats.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")));
        clearStats.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doClearStats();
            }
        });
        statusPopupMenu.add(clearStats);

        statusTasks.add(initActionCallback("doStart", "Start the currently selected channel.", ActionFactory.createBoundAction("doStart","Start Channel", "N"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start.png"))));
        JMenuItem startChannel = new JMenuItem("Start Channel");
        startChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start.png")));
        startChannel.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doStart();
            }
        });
        statusPopupMenu.add(startChannel);

        statusTasks.add(initActionCallback("doPause", "Pause the currently selected channel.", ActionFactory.createBoundAction("doPause","Pause Channel", "P"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/pause.png"))));
        JMenuItem pauseChannel = new JMenuItem("Pause Channel");
        pauseChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/pause.png")));
        pauseChannel.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doPause();
            }
        });
        statusPopupMenu.add(pauseChannel);

        statusTasks.add(initActionCallback("doStop", "Stop the currently selected channel.", ActionFactory.createBoundAction("doStop","Stop Channel", "O"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stop.png"))));
        JMenuItem stopChannel = new JMenuItem("Stop Channel");
        stopChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stop.png")));
        stopChannel.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doStop();
            }
        });
        statusPopupMenu.add(stopChannel);

        setNonFocusable(statusTasks);
        setVisibleTasks(statusTasks, statusPopupMenu, 1, 1, false);
        setVisibleTasks(statusTasks, statusPopupMenu, 3, -1, false);
        taskPaneContainer.add(statusTasks);
    }

    /**
     * Creates the event task pane.
     */
    private void createEventPane()
    {
        // Create Event Tasks Pane
        eventTasks = new JXTaskPane();
        eventPopupMenu = new JPopupMenu();
        eventTasks.setTitle("Event Tasks");
        eventTasks.setFocusable(false);

        eventTasks.add(initActionCallback("doRefreshEvents", "Refresh the list of events with the given filter.", ActionFactory.createBoundAction("doRefreshEvents","Refresh", "R"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png"))));
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        refresh.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doRefreshEvents();
            }
        });
        eventPopupMenu.add(refresh);

        eventTasks.add(initActionCallback("doClearEvents", "Clear the System Events.", ActionFactory.createBoundAction("doClearEvents","Clear Events", "L"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png"))));
        JMenuItem clearEvents = new JMenuItem("Clear Events");
        clearEvents.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")));
        clearEvents.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doClearEvents();
            }
        });
        eventPopupMenu.add(clearEvents);

        setNonFocusable(eventTasks);
        taskPaneContainer.add(eventTasks);
    }

    /**
     * Creates the message task pane.
     */
    private void createMessagePane()
    {
        // Create Message Tasks Pane
        messageTasks = new JXTaskPane();
        messagePopupMenu = new JPopupMenu();
        messageTasks.setTitle("Message Tasks");
        messageTasks.setFocusable(false);

        messageTasks.add(initActionCallback("doRefreshMessages", "Refresh the list of messages with the given filter.", ActionFactory.createBoundAction("doRefreshMessages","Refresh", "R"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png"))));
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        refresh.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doRefreshMessages();
            }
        });
        messagePopupMenu.add(refresh);

        messageTasks.add(initActionCallback("doClearAllMessages", "Clear all Message Events in this channel.", ActionFactory.createBoundAction("doClearAllMessages","Clear All Messages", "L"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png"))));
        JMenuItem clearAllMessages = new JMenuItem("Clear All Messages");
        clearAllMessages.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")));
        clearAllMessages.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doClearAllMessages();
            }
        });
        messagePopupMenu.add(clearAllMessages);

        messageTasks.add(initActionCallback("doRemoveMessage", "Remove the selected Message Event.", ActionFactory.createBoundAction("doRemoveMessages","Remove Message", "E"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png"))));
        JMenuItem removeMessage = new JMenuItem("Remove Message");
        removeMessage.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")));
        removeMessage.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doRemoveMessage();
            }
        });
        messagePopupMenu.add(removeMessage);

        setNonFocusable(messageTasks);
        setVisibleTasks(messageTasks, messagePopupMenu, 2, -1, false);
        taskPaneContainer.add(messageTasks);
    }

    /**
     * Creates the users task pane.
     */
    private void createUserPane()
    {
        // Create User Tasks Pane
        userTasks = new JXTaskPane();
        userPopupMenu = new JPopupMenu();
        userTasks.setTitle("User Tasks");
        userTasks.setFocusable(false);

        userTasks.add(initActionCallback("doRefreshUser", "Refresh the list of users.", ActionFactory.createBoundAction("doRefreshUser","Refresh", "R"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png"))));
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        refresh.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doRefreshUser();
            }
        });
        userPopupMenu.add(refresh);

        userTasks.add(initActionCallback("doNewUser", "Create a new user.", ActionFactory.createBoundAction("doNewChannel","New User", "N"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/add.png"))));
        JMenuItem newUser = new JMenuItem("New User");
        newUser.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/add.png")));
        newUser.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doNewUser();
            }
        });
        userPopupMenu.add(newUser);

        userTasks.add(initActionCallback("doEditUser", "Edit the currently selected user.", ActionFactory.createBoundAction("doEditChannel","Edit User", "E"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/edit.png"))));
        JMenuItem editUser = new JMenuItem("Edit User");
        editUser.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/edit.png")));
        editUser.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doEditUser();
            }
        });
        userPopupMenu.add(editUser);

        userTasks.add(initActionCallback("doDeleteUser", "Delete the currently selected user.", ActionFactory.createBoundAction("doDeleteChannel","Delete User","D"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png"))));
        JMenuItem deleteUser = new JMenuItem("Delete User");
        deleteUser.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")));
        deleteUser.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doDeleteUser();
            }
        });
        userPopupMenu.add(deleteUser);

        setNonFocusable(userTasks);
        setVisibleTasks(userTasks, userPopupMenu, 2, -1, false);
        taskPaneContainer.add(userTasks);
    }

    /**
     * Creates the other task pane.
     */
    private void createOtherPane()
    {
        //Create Other Pane
        otherPane = new JXTaskPane();
        otherPane.setTitle("Other");
        otherPane.setFocusable(false);
        otherPane.add(initActionCallback("doHelp", "Open browser for help on this topic.", ActionFactory.createBoundAction("doHelp","Help on this topic","H"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/help.png"))));
        otherPane.add(initActionCallback("goToAbout", "View the about page for Mirth.", ActionFactory.createBoundAction("goToAbout","About Mirth","U"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/about.png"))));
        otherPane.add(initActionCallback("goToMirth", "View Mirth's homepage.", ActionFactory.createBoundAction("goToMirth","Visit MirthProject.org","V"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/home.png"))));
        otherPane.add(initActionCallback("doLogout", "Logout and return to the login screen.", ActionFactory.createBoundAction("doLogout","Logout","G"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/disconnect.png"))));
        setNonFocusable(otherPane);
        taskPaneContainer.add(otherPane);
    }
    
    public JXTaskPane getOtherPane()
    {
        return otherPane;
    }

    /**
     * Creates the details task pane.
     */
    private void createDetailsPane()
    {
        // Create Details Pane
        details = new JXTaskPane();
        details.setTitle("Details");
        taskPaneContainer.add(details);
        setNonFocusable(details);
        details.setVisible(false);
    }

    /**
     * Initializes the bound method call for the task pane actions.
     */
    private BoundAction initActionCallback(String callbackMethod, String toolTip, BoundAction boundAction, ImageIcon icon)
    {
        if(icon != null)
            boundAction.putValue(Action.SMALL_ICON, icon);
        boundAction.putValue(Action.SHORT_DESCRIPTION, toolTip);
        boundAction.registerCallback(this,callbackMethod);
        return boundAction;
    }

    /**
     * Alerts the user with a yes/no option with the passed in 'message'
     */
    public boolean alertOption(String message)
    {
        int option = JOptionPane.showConfirmDialog(this, message , "Select an Option", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION)
            return true;
        else
            return false;
    }

    /**
     * Alerts the user with an information dialog with the passed in 'message'
     */
    public void alertInformation(String message)
    {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Alerts the user with a warning dialog with the passed in 'message'
     */
    public void alertWarning(String message)
    {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Alerts the user with an error dialog with the passed in 'message'
     */
    public void alertError(String message)
    {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Alerts the user with an exception dialog with the passed in stack trace.
     */
    public void alertException(StackTraceElement[] strace, String message)
    {
        if(message.indexOf("Unauthorized") != -1)
        {
            if(currentContentPage == statusListPage)
                su.interruptThread();
            alertWarning("Sorry your connection to Mirth has either timed out or there was an error in the connection.  Please login again.");
            if(!exportChannelOnError())
                return;
            this.dispose();
            Mirth.main(new String []{PlatformUI.SERVER_NAME});
            return;
        }
        
        if(message.indexOf("Connection refused") != -1)
        {
            if(currentContentPage == statusListPage)
                su.interruptThread();
            alertWarning("The Mirth server " + PlatformUI.SERVER_NAME + " is no longer running.  Please start it and login again.");
            if(!exportChannelOnError())
                return;
            this.dispose();
            Mirth.main(new String []{PlatformUI.SERVER_NAME});
            return;
        }
            
        String stackTrace = message + "\n";
        for (int i = 0; i < strace.length; i++)
            stackTrace += strace[i].toString() + "\n";
            
        JScrollPane errorScrollPane = new JScrollPane();
        JTextArea errorTextArea = new JTextArea();
        errorTextArea.setBackground(UIManager.getColor("Control"));
        errorTextArea.setColumns(60);
        errorTextArea.setRows(20);
        errorTextArea.setText(stackTrace);
        errorTextArea.setEditable(false);
        errorTextArea.setCaretPosition(0);
        errorScrollPane.setViewportView(errorTextArea);
        JOptionPane.showMessageDialog(this, errorScrollPane, "Critical Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Sets the 'index' in 'pane' to be bold
     */
    public void setBold(JXTaskPane pane, int index)
    {
        for (int i=0; i<pane.getContentPane().getComponentCount(); i++)
            pane.getContentPane().getComponent(i).setFont(UIConstants.TEXTFIELD_PLAIN_FONT);

        if (index != UIConstants.ERROR_CONSTANT)
            pane.getContentPane().getComponent(index).setFont(UIConstants.TEXTFIELD_BOLD_FONT);
    }

    /**
     * Sets the visible task pane to the specified 'pane'
     */
    public void setFocus(JXTaskPane pane)
    {
        channelTasks.setVisible(false);
        channelEditTasks.setVisible(false);
        statusTasks.setVisible(false);
        eventTasks.setVisible(false);
        messageTasks.setVisible(false);
        settingsTasks.setVisible(false);
        userTasks.setVisible(false);
        pane.setVisible(true);
    }

    /**
     * Sets all components in pane to be non-focusable.
     */
    public void setNonFocusable(JXTaskPane pane)
    {
        for (int i=0; i<pane.getContentPane().getComponentCount(); i++)
            pane.getContentPane().getComponent(i).setFocusable(false);
    }


    /**
     * Sets the visibible tasks in the given 'pane' and 'menu'.  The method takes an
     * interval of indicies (end index should be -1 to go to the end), as well as a
     * whether they should be set to visible or not-visible.
     */
    public void setVisibleTasks(JXTaskPane pane, JPopupMenu menu, int startIndex, int endIndex, boolean visible)
    {
        if(endIndex == -1)
        {
            for (int i=startIndex; i < pane.getContentPane().getComponentCount(); i++)
            {
                pane.getContentPane().getComponent(i).setVisible(visible);
                menu.getComponent(i).setVisible(visible);
            }
        }
        else
        {
            for (int i=startIndex; (i <= endIndex) && (i < pane.getContentPane().getComponentCount()); i++)
            {
                pane.getContentPane().getComponent(i).setVisible(visible);
                menu.getComponent(i).setVisible(visible);
            }
        }
    }

    /**
     * A prompt to ask the user if he would like to save the changes
     * made before leaving the page.
     */
    public boolean confirmLeave()
    {
        if (channelEditTasks.getContentPane().getComponent(0).isVisible() || (currentContentPage == channelEditPage.transformerPane && channelEditPage.transformerPane.modified) || (currentContentPage == channelEditPage.filterPane && channelEditPage.filterPane.modified))
        {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the channel changes?");
            if (option == JOptionPane.YES_OPTION)
            {
                if (!channelEditPage.saveChanges(true,false))
                    return false;
            }
            else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION)
                return false;

            channelEditTasks.getContentPane().getComponent(0).setVisible(false);
        }
        else if (settingsTasks.getContentPane().getComponent(1).isVisible())
        {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the settings?");

            if (option == JOptionPane.YES_OPTION)
                adminPanel.saveSettings();
            else if (option == JOptionPane.NO_OPTION)
                adminPanel.loadSettings();
            else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION)
                return false;

            settingsTasks.getContentPane().getComponent(1).setVisible(false);
        }
        return true;
    }

    /**
     * Sends the channel passed in to the server, updating it or adding it.
     */
    public boolean updateChannel(Channel curr)
    {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
        {
            if(!mirthClient.updateChannel(curr, false))
            {
                if(alertOption("This channel has been modified since you first opened it.  Would you like to overwrite it?"))
                    mirthClient.updateChannel(curr, true);
                else
                    return false;
            }
            channels = mirthClient.getChannels();
            channelListPage.makeChannelTable();
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
            return false;
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        
        return true;
    }

    /**
     * Sends the passed in user to the server, updating it or adding it.
     */
    public void updateUser(User curr)
    {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
        {
            mirthClient.updateUser(curr);
            users = mirthClient.getUsers();
            adminPanel.userPane.makeUsersTable();
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Checks to see if the passed in channel name already exists
     */
    public boolean checkChannelName(String name)
    {
        for (int i = 0; i < channels.size(); i++)
        {
            if (channels.get(i).getName().equalsIgnoreCase(name))
            {
                alertWarning("Channel name already exists. Please choose a unique name.");
                return false;
            }
        }
        return true;
    }

    /**
     * Enables the save button for needed page.
     */
    public void enableSave()
    {
        if(currentContentPage == channelEditPage)
            channelEditTasks.getContentPane().getComponent(0).setVisible(true);
        else if (currentContentPage == adminPanel)
            settingsTasks.getContentPane().getComponent(1).setVisible(true);
    }
    
    /** 
     * Disables the save button for the needed page.
     */
    public void disableSave()
    {
        if(currentContentPage == channelEditPage)
            channelEditTasks.getContentPane().getComponent(0).setVisible(false);
        else if (currentContentPage == adminPanel)
            settingsTasks.getContentPane().getComponent(1).setVisible(false);
    }


//////////////////////////////////////////////////////////////
//     --- All bound actions are beneath this point ---     //
//////////////////////////////////////////////////////////////

    public void goToMirth()
    {
        BareBonesBrowserLaunch.openURL("http://www.mirthproject.org/");
    }

    public void goToAbout()
    {
        AboutMirth dlg = new AboutMirth();
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
        if (!confirmLeave())
            return;

        doRefresh();
        setBold(viewPane, 0);
        setPanelName("Status");
        setCurrentContentPage(statusListPage);
        setFocus(statusTasks);
    }

    public void doShowChannel()
    {
        if (!confirmLeave())
            return;

        doRefreshChannels();
        setBold(viewPane, 1);
        setPanelName("Channels");
        setCurrentContentPage(channelListPage);
        setFocus(channelTasks);
        channelListPage.deselectRows();
    }

    public void doShowAdminPage()
    {
        if (!confirmLeave())
            return;

        setBold(viewPane, 2);
        setPanelName("Administration");
        setCurrentContentPage(adminPanel);
        doRefreshUser();
        adminPanel.showTasks();
        adminPanel.showFirstTab();
    }

    public void doLogout()
    {
        if(currentContentPage == statusListPage)
            su.interruptThread();
        try
        {
            mirthClient.logout();
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
        this.dispose();
        Mirth.main(new String []{PlatformUI.SERVER_NAME});
    }
    
    public void doMoveDestinationDown()
    {
        channelEditPage.moveDestinationDown();
    }

    public void doMoveDestinationUp()
    {
        channelEditPage.moveDestinationUp();
    }
    
    public void doNewChannel()
    {
        ChannelWizard channelWizard = new ChannelWizard();
        Dimension channelWizardSize = channelWizard.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        channelWizard.setLocation((frmSize.width - channelWizardSize.width) / 2 + loc.x, (frmSize.height - channelWizardSize.height) / 2 + loc.y);
        channelWizard.setModal(true);
        channelWizard.setResizable(false);
        channelWizard.setVisible(true);
    }

    public void doEditChannel()
    {
        doRefreshChannels();

        if (channelListPage.getSelectedChannel() == UIConstants.ERROR_CONSTANT)
            JOptionPane.showMessageDialog(this, "Channel no longer exists.");
        else
        {
            editChannel(channelListPage.getSelectedChannel());
            setPanelName("Edit Channel :: " +  channelEditPage.currentChannel.getName());
        }
    }

    public void doDeleteChannel()
    {
        try
        {
            status = mirthClient.getChannelStatusList();
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
            return;
        }
        String channelId = channels.get(channelListPage.getSelectedChannel()).getId();
        for (int i = 0; i < status.size(); i ++)
        {
            if (status.get(i).getChannelId().equals(channelId));
            {
                alertWarning("You may not delete a deployed channel.\nPlease re-deploy without it enabled first.");
                return;
            }
        }
        
        
        if(!alertOption("Are you sure you want to delete this channel?"))
            return;
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
        {
            mirthClient.removeChannel(channels.get(channelListPage.getSelectedChannel()).getId());
            channels = mirthClient.getChannels();
            channelListPage.makeChannelTable();
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        doShowChannel();
    }

    public void doRefreshChannels()
    {
        String channelId = "";
        String channelName = null;

        if(channelListPage.getSelectedChannel() != UIConstants.ERROR_CONSTANT)
            channelId = channels.get(channelListPage.getSelectedChannel()).getId();

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
        {
            channels = mirthClient.getChannels();
            channelListPage.makeChannelTable();

            if(channels.size() > 0)
            {
                setVisibleTasks(channelTasks, channelPopupMenu, 1, 1, true);
                setVisibleTasks(channelTasks, channelPopupMenu, 4, 4, true);
            }
            else
            {
                setVisibleTasks(channelTasks, channelPopupMenu, 1, 1, false);
                setVisibleTasks(channelTasks, channelPopupMenu, 4, 4, false);
            }
           
            for(int i = 0; i<channels.size(); i++)
            {
                if(channelId == channels.get(i).getId())
                    channelName = channels.get(i).getName();
            }
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        // as long as the channel was not deleted
        if (channelName != null)
            channelListPage.setSelectedChannel(channelName);
    }

    public void doRefresh()
    {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
        {
            status = mirthClient.getChannelStatusList();
            statusListPage.makeStatusTable();
            if(status.size() > 0)
                setVisibleTasks(statusTasks, statusPopupMenu, 1, 1, true);
            else
                setVisibleTasks(statusTasks, statusPopupMenu, 1, 1, false);
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void doStartAll()
    {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        doRefresh();
    }

    public void doStart()
    {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
        {
            if(status.get(statusListPage.getSelectedStatus()).getState() == ChannelStatus.State.STOPPED)
                mirthClient.startChannel(status.get(statusListPage.getSelectedStatus()).getChannelId());
            else if(status.get(statusListPage.getSelectedStatus()).getState() == ChannelStatus.State.PAUSED)
                mirthClient.resumeChannel(status.get(statusListPage.getSelectedStatus()).getChannelId());
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        doRefresh();
    }

    public void doStop()
    {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
        {
            mirthClient.stopChannel(status.get(statusListPage.getSelectedStatus()).getChannelId());
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        doRefresh();
    }

    public void doPause()
    {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
        {
            mirthClient.pauseChannel(status.get(statusListPage.getSelectedStatus()).getChannelId());
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        doRefresh();
    }

    public void doNewDestination()
    {
        channelEditPage.addNewDestination();
    }

    public void doDeleteDestination()
    {
        if(!alertOption("Are you sure you want to delete this destination?"))
            return;

        channelEditPage.deleteDestination();
    }

    public void doEnable()
    {
       doRefreshChannels();

        if (channelListPage.getSelectedChannel() == UIConstants.ERROR_CONSTANT)
            alertWarning("Channel no longer exists.");
        else
        {
            Channel channel = channels.get(channelListPage.getSelectedChannel());
            if(channelEditPage.checkAllForms(channel))
            {
                alertWarning("Channel was not configured properly.  Please fix the problems in the forms before trying to enable it again.");
                return;
            }
            channel.setEnabled(true);
            updateChannel(channel);
            channelListPage.deselectRows();
            channelListPage.setSelectedChannel(channel.getName());
        }
    }

    public void doDisable()
    {
        doRefreshChannels();

        if (channelListPage.getSelectedChannel() == UIConstants.ERROR_CONSTANT)
            alertWarning("Channel no longer exists.");
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
        UserWizard userWizard = new UserWizard(UIConstants.ERROR_CONSTANT);
        Dimension userWizardSize = userWizard.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        userWizard.setLocation((frmSize.width - userWizardSize.width) / 2 + loc.x, (frmSize.height - userWizardSize.height) / 2 + loc.y);
        userWizard.setModal(true);
        userWizard.setResizable(false);
        userWizard.setVisible(true);
    }

    public void doEditUser()
    {
        doRefreshUser();

        if (adminPanel.userPane.getUserIndex() == UIConstants.ERROR_CONSTANT)
            JOptionPane.showMessageDialog(this, "User no longer exists.");
        else
        {
            UserWizard userDialog = new UserWizard(adminPanel.userPane.getSelectedRow());
            Dimension dialogSize = userDialog.getPreferredSize();
            Dimension frmSize = getSize();
            Point loc = getLocation();
            userDialog.setLocation((frmSize.width - dialogSize.width) / 2 + loc.x, (frmSize.height - dialogSize.height) / 2 + loc.y);
            userDialog.setResizable(false);
            userDialog.setVisible(true);
        }
    }

    public void doDeleteUser()
    {
        if(!alertOption("Are you sure you want to delete this user?"))
            return;
        doRefreshUser();
        
        if(users.size() == 1)
        {
            alertWarning("You must have at least one user account.");
            return;
        }
        
        int userToDelete = adminPanel.userPane.getUserIndex();
        String userName = ((CellData)adminPanel.userPane.usersTable.getValueAt(adminPanel.userPane.getSelectedRow(), adminPanel.userPane.getColumnNumber("Username"))).getText();

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
        {
           if(userToDelete != UIConstants.ERROR_CONSTANT)
           {
                mirthClient.removeUser(users.get(userToDelete).getId());
                users = mirthClient.getUsers();
                adminPanel.userPane.makeUsersTable();
                adminPanel.userPane.deselectRows();
           }
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void doRefreshUser()
    {
        int userId = UIConstants.ERROR_CONSTANT;
        String userName = null;

        if(adminPanel.userPane.getUserIndex() != UIConstants.ERROR_CONSTANT)
            userId = users.get(adminPanel.userPane.getUserIndex()).getId();

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
        {
            users = mirthClient.getUsers();
            adminPanel.userPane.makeUsersTable();

            for(int i = 0; i<users.size(); i++)
            {
                if(userId == users.get(i).getId())
                    userName = users.get(i).getUsername();
            }
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        // as long as the channel was not deleted
        if (userName != null)
            adminPanel.userPane.setSelectedUser(userName);
    }

    public void doDeployAll()
    {
        doRefreshChannels();

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
        {
            mirthClient.deployChannels();
            statusListPage.deselectRows();
            doShowStatusPanel();
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void doSaveChanges()
    {
        if (channelEditPage.saveChanges(true, false))
            channelEditTasks.getContentPane().getComponent(0).setVisible(false);
    }

    public void doShowMessages()
    {
        setBold(viewPane, UIConstants.ERROR_CONSTANT);
        setPanelName("Channel Messages :: " + status.get(statusListPage.getSelectedStatus()).getName());
        messageBrowser.loadNew();
        setCurrentContentPage(messageBrowser);
        setFocus(messageTasks);
    }

    public void doShowEvents()
    {
        setBold(viewPane, UIConstants.ERROR_CONSTANT);
        setPanelName("System Events");
        eventBrowser.loadNew();
        setCurrentContentPage(eventBrowser);
        setFocus(eventTasks);
    }

    public void doEditTransformer()
    {
        setPanelName("Edit Channel :: " + channelEditPage.currentChannel.getName() + " :: Edit Transformer");
        channelEditPage.editTransformer();
    }

    public void doEditFilter()
    {
        setPanelName("Edit Channel :: " + channelEditPage.currentChannel.getName() + " :: Edit Filter");
        channelEditPage.editFilter();
    }

    public void doSaveSettings()
    {
        adminPanel.saveSettings();
    }
    
    public void doValidate()
    {
        channelEditPage.validateForm();
    }
    
    public void doImport()
    {
        JFileChooser importFileChooser = new JFileChooser();
        importFileChooser.setFileFilter(new XMLFileFilter());
        int returnVal = importFileChooser.showOpenDialog(this);
        File importFile = null;

        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            importFile = importFileChooser.getSelectedFile();
            String channelXML = "";

            try
            {
                channelXML = FileUtil.read(importFile);
            }
            catch (IOException e)
            {
                alertError("File could not be read.");
                    return;
            }

            ObjectXMLSerializer serializer = new ObjectXMLSerializer();
            try
            {
                Channel importChannel = (Channel)serializer.fromXML(channelXML);

                if(!checkChannelName(importChannel.getName()))
                    return;

                try
                {
                    importChannel.setId(mirthClient.getGuid());
                    importChannel.setRevision(0);
                    channels.add(importChannel);
                    editChannel(channels.size()-1);
                    channelEditTasks.getContentPane().getComponent(0).setVisible(true);
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
            }
            catch (Exception e)
            {
                alertError("Invalid channel file.");
            }
        }
    }

    public boolean doExport()
    {
        if (channelEditTasks.getContentPane().getComponent(0).isVisible())
        {
            if(alertOption("This channel has been modified. You must save the channel changes before you can export. Would you like to save them now?"))
            {
                if (!channelEditPage.saveChanges(true, false))
                    return false;
            }
            else
                return false;

            channelEditTasks.getContentPane().getComponent(0).setVisible(false);
        }
        
        Channel channel;
        if (currentContentPage == channelEditPage || currentContentPage == channelEditPage.filterPane || currentContentPage == channelEditPage.transformerPane)
            channel = channelEditPage.currentChannel;
        else
            channel = channels.get(channelListPage.getSelectedChannel());

        JFileChooser exportFileChooser = new JFileChooser();
        exportFileChooser.setSelectedFile(new File(channel.getName()));
        exportFileChooser.setFileFilter(new XMLFileFilter());
        int returnVal = exportFileChooser.showSaveDialog(this);
        File exportFile = null;

        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            ObjectXMLSerializer serializer = new ObjectXMLSerializer();
            String channelXML = serializer.toXML(channel);
            exportFile = exportFileChooser.getSelectedFile();
            
            int length = exportFile.getName().length();
            
            if (length < 4 || !exportFile.getName().substring(length-4, length).equals(".xml"))
                exportFile = new File(exportFile.getAbsolutePath() + ".xml");
            
            if(exportFile.exists())
                if(!alertOption("This file already exists.  Would you like to overwrite it?"))
                    return false;
            
            try
            {
                FileUtil.write(exportFile, channelXML);
                alertInformation(channel.getName() + " was written to " + exportFile.getPath() + ".");
            }
            catch (IOException ex)
            {
                alertError("File could not be written.");
                return false;
            }
            return true;
        }
        else
            return false;
        
    }
    
    public void doExportAll()
    {
        JFileChooser exportFileChooser = new JFileChooser();
        exportFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = exportFileChooser.showSaveDialog(this);
        File exportFile = null;
        File exportDirectory = null;
        
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                exportDirectory = exportFileChooser.getSelectedFile();

                for(int i = 0; i < channels.size(); i++)
                {
                    Channel channel = channels.get(i);
                    ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                    String channelXML = serializer.toXML(channel);
                    
                    exportFile = new File(exportDirectory.getAbsolutePath() + "/" + channel.getName() + ".xml");
                    
                    if(exportFile.exists())
                        if(!alertOption("The file " + channel.getName() + ".xml already exists.  Would you like to overwrite it?"))
                            continue;

                    FileUtil.write(exportFile, channelXML);
                }
                alertInformation("All files were written successfully to " + exportDirectory.getPath() + ".");
            }
            catch (IOException ex)
            {
                alertError("File could not be written.");
            }
        }
    }
    
    public void doRefreshMessages()
    {
        messageBrowser.refresh();
    }

    public void doClearAllMessages()
    {
        if (alertOption("Are you sure you would like to clear all messages?"))
        {
            try
            {
                mirthClient.clearMessages(status.get(statusListPage.getSelectedStatus()).getChannelId());
            }
            catch (ClientException e)
            {
                alertException(e.getStackTrace(), e.getMessage());
            }
            messageBrowser.refresh();
        }
    }

    public void doRemoveMessage()
    {
        if (alertOption("Are you sure you would like to remove the selected message?"))
        {
            try
            {
                mirthClient.removeMessage(messageBrowser.getSelectedMessageID());
            }
            catch (ClientException e)
            {
                alertException(e.getStackTrace(), e.getMessage());
            }
            messageBrowser.refresh();
        }
    }

    public void doRefreshEvents()
    {
        eventBrowser.refresh();
    }

    public void doClearEvents()
    {
        if (alertOption("Are you sure you would like to clear all system events?"))
        {
            try
            {
                mirthClient.clearSystemEvents();
            }
            catch (ClientException e)
            {
                alertException(e.getStackTrace(), e.getMessage());
            }
            eventBrowser.refresh();
        }
    }
    
    public void doClearStats()
    {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
        {
            mirthClient.clearStatistics(status.get(statusListPage.getSelectedStatus()).getChannelId());
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        doRefresh();
    }

    public void doRefreshSettings()
    {
        adminPanel.loadSettings();
    }
    
    public boolean exportChannelOnError()
    {
        if (channelEditTasks.getContentPane().getComponent(0).isVisible() || channelEditPage.transformerPane.modified || channelEditPage.filterPane.modified)
        {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the channel changes locally to your computer?");
            if (option == JOptionPane.YES_OPTION)
            {
                if (!channelEditPage.saveChanges(true, true))
                    return false;
                
                boolean visible = channelEditTasks.getContentPane().getComponent(0).isVisible();
                channelEditTasks.getContentPane().getComponent(0).setVisible(false);
                if(!doExport())
                {
                    channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
                    return false;
                }
            }
            else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION)
                return false;
            else
                channelEditTasks.getContentPane().getComponent(0).setVisible(false);
        }
        return true;
    }
    
    public void doHelp()
    {
        if(currentContentPage == channelEditPage)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.CHANNEL_HELP_LOCATION);
        else if(currentContentPage == channelListPage)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.CHANNELS_HELP_LOCATION);
        else if(currentContentPage == statusListPage)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.STATUS_HELP_LOCATION);
        else if(currentContentPage == adminPanel)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.ADMIN_HELP_LOCATION);
        else if(currentContentPage == channelEditPage.transformerPane)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.TRANFORMER_HELP_LOCATION);
        else if(currentContentPage == channelEditPage.filterPane)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.FILTER_HELP_LOCATION);
        else
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION);
    }
}


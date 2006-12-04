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

import com.webreach.mirth.client.ui.editors.filter.FilterPane;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.client.ui.util.ImportConverter;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import org.jdesktop.swingworker.SwingWorker;

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
    private Logger logger = Logger.getLogger(this.getClass());
    
    public Client mirthClient;

    public StatusPanel statusPanel;
    public ChannelPanel channelPanel;
    public AdminPanel adminPanel;
    public ChannelSetup channelEditPanel;
    public EventBrowser eventBrowser;
    public MessageBrowser messageBrowser;
    public JXTaskPaneContainer taskPaneContainer;
    
    public List<ChannelStatus> status = null;
    public List<Channel> channels = null;
    public List<User> users = null;

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
    private boolean connectionError;
    
    public Frame()
    {
        dsb = new DropShadowBorder(UIManager.getColor("Control"), 0, 3, .3f, 12, true, true, true, true);
        leftContainer = new JXTitledPanel();
        rightContainer = new JXTitledPanel();
        
        taskPaneContainer = new JXTaskPaneContainer();
        sourceConnectors = new ArrayList<ConnectorClass>();
        destinationConnectors = new ArrayList<ConnectorClass>();
        
        setTitle(UIConstants.TITLE_TEXT);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setIconImage(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/emoticon_smile.png")).getImage());
        makePaneContainer();
        
        connectionError = false;
        
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                if (!confirmLeave())
                    return;
                
                doLogout();
                System.exit(0);
            }
        });
    }
    
    /**
     * Called to set up this main window frame.  Calls jbInit() as well.
     */
    public void setupFrame(Client mirthClient)
    {
        this.mirthClient = mirthClient;

        userPreferences = Preferences.systemNodeForPackage(Mirth.class);
        userPreferences.put("defaultServer", PlatformUI.SERVER_NAME);

        splitPane.setDividerSize(0);
        contentPanel = (JPanel) getContentPane();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(null);
        
        statusBar = new StatusBar();

        buildContentPanel(rightContainer, contentPane, false);

        splitPane.add(rightContainer, JSplitPane.RIGHT);
        splitPane.add(taskPane, JSplitPane.LEFT);
        taskPane.setMinimumSize(new Dimension(UIConstants.TASK_PANE_WIDTH,0));
        splitPane.setDividerLocation(UIConstants.TASK_PANE_WIDTH);
        
        contentPanel.add(statusBar, BorderLayout.SOUTH);
        contentPanel.add(splitPane, java.awt.BorderLayout.CENTER);
        
        setCurrentTaskPaneContainer(taskPaneContainer);
        doShowStatusPanel();
        channelEditPanel = new ChannelSetup();
        
        su = new StatusUpdater();
        statusUpdater = new Thread(su);
        statusUpdater.start();
              
        /* DEBUGGING THE UIDefaults:
 
        UIDefaults uiDefaults = UIManager.getDefaults();
        Enumeration enum1 = uiDefaults.keys();
        while (enum1.hasMoreElements())
        {
            Object key = enum1.nextElement();
            Object val = uiDefaults.get(key);
            if(key.toString().indexOf("image") != -1)
                System.out.println("UIManager.put(\"" + key.toString() + "\",\"" +
                    (null != val ? val.toString() : "(null)") +
                    "\");");
        } */
    }
    
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
        
    public void setWorking(final boolean working)
    {
        if(statusBar != null)
            statusBar.setWorking(working);
    }

    /**
     * Changes the current content page to the Channel Editor with the new
     * channel specified as the loaded one.
     */
    public void setupChannel(Channel channel)
    {
        setCurrentContentPage(channelEditPanel);
        setBold(viewPane,UIConstants.ERROR_CONSTANT);
        setFocus(channelEditTasks);
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 0, 0, false);
        channelEditPanel.addChannel(channel);
    }

    /**
     * Edits a channel at a specified index, setting that channel
     * as the current channel in the editor.
     */
    public void editChannel(int index)
    {
        setBold(viewPane, UIConstants.ERROR_CONSTANT);
        setCurrentContentPage(channelEditPanel);
        setFocus(channelEditTasks);
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 0, 4, false);
        channelEditPanel.editChannel(index);
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
        viewPane.add(initActionCallback("doShowStatusPanel", "Contains information about your currently deployed channels.", ActionFactory.createBoundAction("showStatusPanel","Status","U"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/status.png"))));
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

        settingsTasks.add(initActionCallback("doSaveSettings", "Save settings.", ActionFactory.createBoundAction("doSaveSettings","Save Settings", "S"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png"))));
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

        channelTasks.add(initActionCallback("doImport", "Import a channel from an XML file.", ActionFactory.createBoundAction("doImport","Import Channel", "I"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/import.png"))));
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

        channelEditTasks.add(initActionCallback("doSaveChanges", "Save all changes made to this channel.", ActionFactory.createBoundAction("doSaveChanges","Save Changes", "S"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png"))));
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
        
        channelEditTasks.add(initActionCallback("doMoveDestinationUp", "Move the currently selected destination up.", ActionFactory.createBoundAction("doMoveDestinationUp","Move Dest. Up", "P"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/arrow_up.png"))));
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

        statusTasks.add(initActionCallback("doRefreshStatuses", "Refresh the list of statuses.", ActionFactory.createBoundAction("doRefresh","Refresh", "R"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png"))));
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        refresh.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doRefreshStatuses();
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
        
        messageTasks.add(initActionCallback("doExportMessages", "Export all currently viewed messages.", ActionFactory.createBoundAction("doExportMessages","Export Messages", "X"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png"))));
        JMenuItem exportMessages = new JMenuItem("Export Messages");
        exportMessages.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png")));
        exportMessages.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doExportMessages();
            }
        });
        messagePopupMenu.add(exportMessages);
        
        messageTasks.add(initActionCallback("doRemoveAllMessages", "Remove all Message Events in this channel.", ActionFactory.createBoundAction("doRemoveAllMessages","Remove All Messages", "L"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png"))));
        JMenuItem removeAllMessages = new JMenuItem("Remove All Messages");
        removeAllMessages.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")));
        removeAllMessages.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doRemoveAllMessages();
            }
        });
        messagePopupMenu.add(removeAllMessages);
        
        messageTasks.add(initActionCallback("doRemoveFilteredMessages", "Remove all Message Events in the current filter.", ActionFactory.createBoundAction("doRemoveFilteredMessages","Remove Filtered Messages", "F"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png"))));
        JMenuItem removeFilteredMessages = new JMenuItem("Remove Filtered Messages");
        removeFilteredMessages.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")));
        removeFilteredMessages.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doRemoveFilteredMessages();
            }
        });
        messagePopupMenu.add(removeFilteredMessages);

        messageTasks.add(initActionCallback("doRemoveMessage", "Remove the selected Message Event.", ActionFactory.createBoundAction("doRemoveMessage","Remove Message", "E"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png"))));
        JMenuItem removeMessage = new JMenuItem("Remove Message");
        removeMessage.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")));
        removeMessage.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doRemoveMessage();
            }
        });
        messagePopupMenu.add(removeMessage);
        
        messageTasks.add(initActionCallback("doReprocessFilteredMessages", "Reprocess all Message Events in the current filter.", ActionFactory.createBoundAction("doReprocessFilteredMessages","Reprocess Filtered Messages", "G"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/deployall.png"))));
        JMenuItem reprocessFilteredMessages = new JMenuItem("Reprocess Filtered Messages");
        reprocessFilteredMessages.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/deployall.png")));
        reprocessFilteredMessages.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doReprocessFilteredMessages();
            }
        });
        messagePopupMenu.add(reprocessFilteredMessages);

        messageTasks.add(initActionCallback("doReprocessMessage", "Reprocess the selected Message.", ActionFactory.createBoundAction("doReprocessMessage","Reprocess Message", "C"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/deploy.png"))));
        JMenuItem reprocessMessage = new JMenuItem("Reprocess Message");
        reprocessMessage.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/deploy.png")));
        reprocessMessage.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                doReprocessMessage();
            }
        });
        messagePopupMenu.add(reprocessMessage);

        setNonFocusable(messageTasks);
        setVisibleTasks(messageTasks, messagePopupMenu, 4, -1, false);
        setVisibleTasks(messageTasks, messagePopupMenu, 5, 5, true);
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
        otherPane.add(initActionCallback("goToAbout", "View the about page for Mirth.", ActionFactory.createBoundAction("goToAbout","About Mirth","M"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/about.png"))));
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
     * Alerts the user with a Ok/cancel option with the passed in 'message'
     */
    public boolean alertOkCancel(String message)
    {
        int option = JOptionPane.showConfirmDialog(this, message , "Select an Option", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION)
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
        if(connectionError)
            return;
        
        if(message.indexOf("Unauthorized") != -1 || message.indexOf("reset") != -1)
        {
            connectionError = true;
            if(currentContentPage == statusPanel)
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
            connectionError = true;
            if(currentContentPage == statusPanel)
                su.interruptThread();
            alertWarning("The Mirth server " + PlatformUI.SERVER_NAME + " is no longer running.  Please start it and login again.");
            if(!exportChannelOnError())
                return;
            this.dispose();
            Mirth.main(new String []{PlatformUI.SERVER_NAME});
            return;
        }
        
        logger.error(strace);
        
        String stackTrace = message + "\n";
        for (int i = 0; i < strace.length; i++)
            stackTrace += strace[i].toString() + "\n";
        
        ErrorDialog dlg = new ErrorDialog(stackTrace);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);
    }
    
    /* 
     * Send the message to MirthProject.org
     */
    public void sendError(String message)
    {
        mirthClient.submitError(message);
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
        if (channelEditPanel != null && (channelEditTasks.getContentPane().getComponent(0).isVisible() || (currentContentPage == channelEditPanel.transformerPane && channelEditPanel.transformerPane.modified) || (currentContentPage == channelEditPanel.filterPane && channelEditPanel.filterPane.modified)))
        {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the channel changes?");
            if (option == JOptionPane.YES_OPTION)
            {
                if (!channelEditPanel.saveChanges(false, true,false))
                    return false;
            }
            else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION)
                return false;

            channelEditTasks.getContentPane().getComponent(0).setVisible(false);
        }
        else if (adminPanel != null && settingsTasks.getContentPane().getComponent(1).isVisible())
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
        try
        {
            if(!mirthClient.updateChannel(curr, false))
            {
                if(alertOption("This channel has been modified since you first opened it.  Would you like to overwrite it?"))
                    mirthClient.updateChannel(curr, true);
                else
                    return false;
            }
            channels = mirthClient.getChannel(null);
            channelPanel.makeChannelTable();
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
            return false;
        }
        
        return true;
    }

    /**
     * Sends the passed in user to the server, updating it or adding it.
     */
    public void updateUser(User curr)
    {
        try
        {
            mirthClient.updateUser(curr);
            users = mirthClient.getUser(null);
            adminPanel.userPane.makeUsersTable();
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
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
        if(channelEditPanel != null && currentContentPage == channelEditPanel)
            channelEditTasks.getContentPane().getComponent(0).setVisible(true);
        else if (channelEditPanel != null && currentContentPage == channelEditPanel.transformerPane)
            channelEditPanel.transformerPane.modified = true;
        else if (channelEditPanel != null && currentContentPage == channelEditPanel.filterPane)
            channelEditPanel.filterPane.modified = true;
        else if (adminPanel != null && currentContentPage == adminPanel)
            settingsTasks.getContentPane().getComponent(1).setVisible(true);
    }
    
    /** 
     * Disables the save button for the needed page.
     */
    public void disableSave()
    {
        if(currentContentPage == channelEditPanel)
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
        if(statusPanel == null)
            statusPanel = new StatusPanel();
        
        if (!confirmLeave())
            return;
        
        setBold(viewPane, 0);
        setPanelName("Status");
        setCurrentContentPage(statusPanel);
        setFocus(statusTasks);
        
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                refreshStatuses();
                return null;
            }
            
            public void done()
            {
                setWorking(false);
            }
        };
        
        worker.execute();
    }

    public void doShowChannel()
    {
        if(channelPanel == null)
            channelPanel = new ChannelPanel();
        
        if (!confirmLeave())
            return;
        
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                refreshChannels();
                return null;
            }
            
            public void done()
            {
                setBold(viewPane, 1);
                setPanelName("Channels");
                setCurrentContentPage(channelPanel);
                setFocus(channelTasks);
                channelPanel.deselectRows();
                setWorking(false);
            }
        };
        
        worker.execute();
    }

    public void doShowAdminPage()
    {        
        if(adminPanel == null)
            adminPanel = new AdminPanel();
        
        if (!confirmLeave())
            return;
        
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {
                setBold(viewPane, 2);
                setPanelName("Administration");
                setCurrentContentPage(adminPanel);
                doRefreshUser();
                adminPanel.showTasks();
                adminPanel.showFirstTab();
                return null;
            }
            
            public void done()
            {
                setWorking(false);
            }
        };
        
        worker.execute();
    }

    public void doLogout()
    {
        if (!confirmLeave())
            return;
        
        if(currentContentPage == statusPanel)
            su.interruptThread();
        
            userPreferences = Preferences.systemNodeForPackage(Mirth.class);
            userPreferences.putInt("maximizedState", getExtendedState());
            userPreferences.putInt("width", getWidth());
            userPreferences.putInt("height", getHeight());
        
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
        channelEditPanel.moveDestinationDown();
    }

    public void doMoveDestinationUp()
    {
        channelEditPanel.moveDestinationUp();
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
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                refreshChannels();
                return null;
            }
            
            public void done()
            {
                if (channelPanel.getSelectedChannel() == UIConstants.ERROR_CONSTANT)
                    JOptionPane.showMessageDialog(getThis(), "Channel no longer exists.");
                else
                    editChannel(channelPanel.getSelectedChannel());
                
                setWorking(false);
            }
        };
        
        worker.execute();
        
    }
    
    public void doDeleteChannel()
    {
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                try
                {
                    status = mirthClient.getChannelStatusList();
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                    return null;
                }

                String channelId = channels.get(channelPanel.getSelectedChannel()).getId();
                for (int i = 0; i < status.size(); i ++)
                {
                    if (status.get(i).getChannelId().equals(channelId))
                    {
                        alertWarning("You may not delete a deployed channel.\nPlease re-deploy without it enabled first.");
                        return null;
                    }
                }

                if(!alertOption("Are you sure you want to delete this channel?"))
                    return null;

                try
                {
                    mirthClient.removeChannel(channels.get(channelPanel.getSelectedChannel()));
                    channels = mirthClient.getChannel(null);
                    channelPanel.makeChannelTable();
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }
            
            public void done()
            {
                doShowChannel();
                setWorking(false);
            }
        };
        
        worker.execute();
    }
    
    public void doRefreshChannels()
    {
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                refreshChannels();
                return null;
            }
            
            public void done()
            {
                setWorking(false);
            }
        };
        
        worker.execute();
    }
    
    public void refreshChannels()
    {
        String channelId = "";
        String channelName = null;

        if(channelPanel.getSelectedChannel() != UIConstants.ERROR_CONSTANT)
            channelId = channels.get(channelPanel.getSelectedChannel()).getId();
        
        try
        {
            channels = mirthClient.getChannel(null);
            channelPanel.makeChannelTable();

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

        // as long as the channel was not deleted
        if (channelName != null)
            channelPanel.setSelectedChannel(channelName);
    }
    
    public void doRefreshStatuses()
    {
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                refreshStatuses();
                return null;
            }
            
            public void done()
            {
                setWorking(false);
            }
        };
        
        worker.execute();
    }
    
    public void refreshStatuses()
    {
        try
        {
            status = mirthClient.getChannelStatusList();
            statusPanel.makeStatusTable();
            if(status.size() > 0)
                setVisibleTasks(statusTasks, statusPopupMenu, 1, 1, true);
            else
                setVisibleTasks(statusTasks, statusPopupMenu, 1, 1, false);
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
    }

    public void doStartAll()
    {
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
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
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }
            
            public void done()
            {
                refreshStatuses();
                setWorking(false);
            }
        };
        
        worker.execute();
    }

    public void doStart()
    {
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                try
                {
                    if(status.get(statusPanel.getSelectedStatus()).getState() == ChannelStatus.State.STOPPED)
                        mirthClient.startChannel(status.get(statusPanel.getSelectedStatus()).getChannelId());
                    else if(status.get(statusPanel.getSelectedStatus()).getState() == ChannelStatus.State.PAUSED)
                        mirthClient.resumeChannel(status.get(statusPanel.getSelectedStatus()).getChannelId());
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }
            
            public void done()
            {
                refreshStatuses();
                setWorking(false);
            }
        };
        
        worker.execute();
    }

    public void doStop()
    {
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                try
                {
                    mirthClient.stopChannel(status.get(statusPanel.getSelectedStatus()).getChannelId());
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }
            
            public void done()
            {
                refreshStatuses();
                setWorking(false);
            }
        };
        
        worker.execute();
    }

    public void doPause()
    {
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                try
                {
                    mirthClient.pauseChannel(status.get(statusPanel.getSelectedStatus()).getChannelId());
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }
            
            public void done()
            {
                refreshStatuses();
                setWorking(false);
            }
        };
        
        worker.execute();
    }

    public void doNewDestination()
    {
        channelEditPanel.addNewDestination();
    }

    public void doDeleteDestination()
    {
        if(!alertOption("Are you sure you want to delete this destination?"))
            return;

        channelEditPanel.deleteDestination();
    }

    public void doEnable()
    {
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                refreshChannels();
                if (channelPanel.getSelectedChannel() == UIConstants.ERROR_CONSTANT)
                    alertWarning("Channel no longer exists.");
                else
                {
                    Channel channel = channels.get(channelPanel.getSelectedChannel());
                    if(channelEditPanel.checkAllForms(channel))
                    {
                        alertWarning("Channel was not configured properly.  Please fix the problems in the forms before trying to enable it again.");
                        return null;
                    }
                    channel.setEnabled(true);
                    updateChannel(channel);
                    channelPanel.deselectRows();
                    channelPanel.setSelectedChannel(channel.getName());
                }
                return null;
            }
            
            public void done()
            {
                setWorking(false);
            }
        };
        
        worker.execute();
    }

    public void doDisable()
    {
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                refreshChannels();
                if (channelPanel.getSelectedChannel() == UIConstants.ERROR_CONSTANT)
                    alertWarning("Channel no longer exists.");
                else
                {
                    Channel channel = channels.get(channelPanel.getSelectedChannel());
                    channel.setEnabled(false);
                    updateChannel(channel);
                    channelPanel.deselectRows();
                    channelPanel.setSelectedChannel(channel.getName());
                }
                return null;
            }
            
            public void done()
            {
                setWorking(false);
            }
        };
        
        worker.execute();
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
        
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                 doRefreshUser();
        
                if(users.size() == 1)
                {
                    alertWarning("You must have at least one user account.");
                    return null;
                }

                int userToDelete = adminPanel.userPane.getUserIndex();
                String userName = ((CellData)adminPanel.userPane.usersTable.getValueAt(adminPanel.userPane.getSelectedRow(), adminPanel.userPane.getColumnNumber("Username"))).getText();

                setWorking(true);
                try
                {
                   if(userToDelete != UIConstants.ERROR_CONSTANT)
                   {
                        mirthClient.removeUser(users.get(userToDelete));
                        users = mirthClient.getUser(null);
                        adminPanel.userPane.makeUsersTable();
                        adminPanel.userPane.deselectRows();
                   }
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }
            
            public void done()
            {
                setWorking(false);
            }
        };
        
        worker.execute();
    }

    public void doRefreshUser()
    {
        setWorking(true);

        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                User user = null;
                String userName = null;

                if(adminPanel.userPane.getUserIndex() != UIConstants.ERROR_CONSTANT)
                    user = users.get(adminPanel.userPane.getUserIndex());

                try
                {
                    users = mirthClient.getUser(null);
                    adminPanel.userPane.makeUsersTable();

                    if(user != null)
                    {
                        for(int i = 0; i<users.size(); i++)
                        {
                            if(user.equals(users.get(i)))
                                userName = users.get(i).getUsername();
                        }
                    }
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }

                // as long as the channel was not deleted
                if (userName != null)
                    adminPanel.userPane.setSelectedUser(userName);
                return null;
            }
            
            public void done()
            {
                setWorking(false);
            }
        };
        
        worker.execute();
    }

    public void doDeployAll()
    {
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                refreshChannels();
                try
                {
                    mirthClient.deployChannels();
                    statusPanel.deselectRows();
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }
            
            public void done()
            {
                doShowStatusPanel();
                setWorking(false);
            }
        };
        
        worker.execute();
    }

    public void doSaveChanges()
    {
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
            	if (channelEditTasks.getContentPane().getComponent(0).isVisible() || currentContentPage == channelEditPanel.transformerPane || currentContentPage == channelEditPanel.filterPane){
	                if (channelEditPanel.saveChanges(false, true, false)){
	                    channelEditTasks.getContentPane().getComponent(0).setVisible(false);
	                    if (currentContentPage == channelEditPanel.transformerPane){
	                    	channelEditPanel.transformerPane.modified = false;
	                    }else if (currentContentPage == channelEditPanel.filterPane){
	                    	channelEditPanel.filterPane.modified = false;
	                    }
	                }
	                return null;
            	}else{
            		return null;
            	}
            }
            
            public void done()
            {
                setWorking(false);
            }
        };
        
        worker.execute();
    }

    public void doShowMessages()
    {
        if(messageBrowser == null)
            messageBrowser = new MessageBrowser();
        
        setBold(viewPane, -1);
        setPanelName("Channel Messages :: " + status.get(statusPanel.getSelectedStatus()).getName());
        
        setCurrentContentPage(messageBrowser);
        messageBrowser.loadNew();       
        setFocus(messageTasks);
    
        

    }

    public void doShowEvents()
    {
        if(eventBrowser == null)
            eventBrowser = new EventBrowser();
        
        setBold(viewPane, -1);
        setPanelName("System Events");
        eventBrowser.loadNew();
        setCurrentContentPage(eventBrowser);
        setFocus(eventTasks);
    }

    public void doEditTransformer()
    {
        if(channelEditPanel.transformerPane == null)
            channelEditPanel.transformerPane = new TransformerPane();
        
        setPanelName("Edit Channel :: " + channelEditPanel.currentChannel.getName() + " :: Edit Transformer");
        channelEditPanel.editTransformer();
    }

    public void doEditFilter()
    {
        if(channelEditPanel.filterPane == null)
            channelEditPanel.filterPane = new FilterPane();
        
        setPanelName("Edit Channel :: " + channelEditPanel.currentChannel.getName() + " :: Edit Filter");
        channelEditPanel.editFilter();
    }

    public void doSaveSettings()
    {
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                adminPanel.saveSettings();
                return null;
            }
            
            public void done()
            {
                setWorking(false);
            }
        };
        
        worker.execute();
    }
    
    public void doValidate()
    {
        channelEditPanel.validateForm();
    }
    
    public void doImport()
    {
        JFileChooser importFileChooser = new JFileChooser();
        importFileChooser.setFileFilter(new MirthFileFilter("XML"));
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
            Channel importChannel;
            
            try
            {
                 importChannel = (Channel)serializer.fromXML(channelXML.replaceAll("\\&\\#x0D","\\&\\#x0A"));
             }
            catch (Exception e)
            {
                alertError("Invalid channel file.");
                return;
            }   
            
            /**
             * Checks to see if the passed in channel version is current,
             * and prompts the user if it is not.
             */
            int option;
            
            try
            {
                option = JOptionPane.YES_OPTION;
                if (importChannel.getVersion() == null)
                {
                    option = JOptionPane.showConfirmDialog(this, "The channel being imported is from an unknown version of Mirth." +
                            "\nSome channel properties may not be the same.  Would you like to automatically convert the properties?", "Select an Option", JOptionPane.YES_NO_CANCEL_OPTION);
                }
                else if (!importChannel.getVersion().equals(mirthClient.getVersion()))
                {
                    option = JOptionPane.showConfirmDialog(this, "The channel being imported is from Mirth version " + importChannel.getVersion() + ". You are using Mirth version " + mirthClient.getVersion() + 
                            ".\nSome channel properties may not be the same.  Would you like to automatically convert the properties?", "Select an Option", JOptionPane.YES_NO_CANCEL_OPTION);
                }
            }
            catch (ClientException e)
            {
                alertError("Could not get the clients version.");
                return;
            }
            
            
            if (option == JOptionPane.YES_OPTION)
            {
                ImportConverter converter = new ImportConverter();
                importChannel = converter.convertChannel(importChannel);
            }
            else if(option != JOptionPane.NO_OPTION)
                return;

            if(!checkChannelName(importChannel.getName()))
                return;
            
            importChannel.setRevision(0);
            channels.add(importChannel);
            
            try
            {
                editChannel(channels.size()-1);
                channelEditTasks.getContentPane().getComponent(0).setVisible(true);
            }
            catch (Exception e)
            {
                alertError("Channel had an unknown problem. Channel import aborted.");
                channels.remove(channels.size() -1);
                channelEditPanel = new ChannelSetup();
                this.doShowChannel();
            }
        }
    }

    public boolean doExport()
    {
        if (channelEditTasks.getContentPane().getComponent(0).isVisible())
        {
            if(alertOption("This channel has been modified. You must save the channel changes before you can export. Would you like to save them now?"))
            {
                if (!channelEditPanel.saveChanges(false, true, false))
                    return false;
            }
            else
                return false;

            channelEditTasks.getContentPane().getComponent(0).setVisible(false);
        }
        
        Channel channel;
        if (currentContentPage == channelEditPanel || currentContentPage == channelEditPanel.filterPane || currentContentPage == channelEditPanel.transformerPane)
            channel = channelEditPanel.currentChannel;
        else
            channel = channels.get(channelPanel.getSelectedChannel());

        JFileChooser exportFileChooser = new JFileChooser();
        exportFileChooser.setSelectedFile(new File(channel.getName()));
        exportFileChooser.setFileFilter(new MirthFileFilter("XML"));
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
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                messageBrowser.refresh();
                return null;
            }
            
            public void done()
            {
                setWorking(false);
            }
        };
        
        worker.execute();
    }
    
    public void doExportMessages()
    {
        messageBrowser.export();
    }

    public void doRemoveAllMessages()
    {
        if (alertOption("Are you sure you would like to remove all messages in this channel?"))
        {
            setWorking(true);
        
            SwingWorker worker = new SwingWorker <Void, Void> ()
            {
                public Void doInBackground() 
                {        
                    try
                    {
                        mirthClient.clearMessages(status.get(statusPanel.getSelectedStatus()).getChannelId());
                    }
                    catch (ClientException e)
                    {
                        alertException(e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done()
                {
                    messageBrowser.refresh();
                    setWorking(false);
                }
            };

            worker.execute();
        }
    }
    
    public void doRemoveFilteredMessages()
    {
        if (alertOption("Are you sure you would like to remove all currently filtered messages in this channel?"))
        {
            setWorking(true);
        
            SwingWorker worker = new SwingWorker <Void, Void> ()
            {
                public Void doInBackground() 
                {        
                    try
                    {
                        mirthClient.removeMessages(messageBrowser.getCurrentFilter());
                    }
                    catch (ClientException e)
                    {
                        alertException(e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done()
                {
                    messageBrowser.refresh();
                    setWorking(false);
                }
            };

            worker.execute();
            
        }
    }

    public void doRemoveMessage()
    {
        if (alertOption("Are you sure you would like to remove the selected message?"))
        {
            setWorking(true);
        
            SwingWorker worker = new SwingWorker <Void, Void> ()
            {
                public Void doInBackground() 
                {        
                    try
                    {
                        MessageObjectFilter filter = new MessageObjectFilter();
                        filter.setId(messageBrowser.getSelectedMessageID());
                        mirthClient.removeMessages(filter);
                    }
                    catch (ClientException e)
                    {
                        alertException(e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done()
                {
                    messageBrowser.refresh();
                    setWorking(false);
                }
            };

            worker.execute();
        }
    }
    
    public void doReprocessFilteredMessages()
    {
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                try
                {
                    mirthClient.reprocessMessages(messageBrowser.getCurrentFilter());
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }
            
            public void done()
            {
                messageBrowser.refresh();
                setWorking(false);
            }
        };
        
        worker.execute();
    }
    
    public void doReprocessMessage()
    {
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                try
                {
                    MessageObjectFilter filter = new MessageObjectFilter();
                    filter.setId(messageBrowser.getSelectedMessageID());
                    mirthClient.reprocessMessages(filter);
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }
            
            public void done()
            {
                messageBrowser.refresh();
                setWorking(false);
            }
        };
        
        worker.execute();
    }

    public void doRefreshEvents()
    {
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                eventBrowser.refresh();
                return null;
            }
            
            public void done()
            {
                setWorking(false);
            }
        };
        
        worker.execute();
    }

    public void doClearEvents()
    {
        if (alertOption("Are you sure you would like to clear all system events?"))
        {
            setWorking(true);
        
            SwingWorker worker = new SwingWorker <Void, Void> ()
            {
                public Void doInBackground() 
                {        
                    try
                    {
                        mirthClient.clearSystemEvents();
                    }
                    catch (ClientException e)
                    {
                        alertException(e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done()
                {
                    eventBrowser.refresh();
                    setWorking(false);
                }
            };

            worker.execute();
        }
    }
    
    public void doClearStats()
    {
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                try
                {
                    mirthClient.clearStatistics(status.get(statusPanel.getSelectedStatus()).getChannelId());
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }
            
            public void done()
            {
                refreshStatuses();
                setWorking(false);
            }
        };
        
        worker.execute(); 
    }

    public void doRefreshSettings()
    {
        setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground() 
            {        
                adminPanel.loadSettings();
                return null;
            }
            
            public void done()
            {
                setWorking(false);
            }
        };
        
        worker.execute(); 
        
    }
    
    public boolean exportChannelOnError()
    {
        if (channelEditPanel != null && (channelEditTasks.getContentPane().getComponent(0).isVisible() || (channelEditPanel.transformerPane != null && channelEditPanel.transformerPane.modified) || (channelEditPanel.filterPane != null && channelEditPanel.filterPane.modified)))
        {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the channel changes locally to your computer?");
            if (option == JOptionPane.YES_OPTION)
            {
                if (!channelEditPanel.saveChanges(false, true, true))
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
        if(currentContentPage == channelEditPanel)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.CHANNEL_HELP_LOCATION);
        else if(currentContentPage == channelPanel)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.CHANNELS_HELP_LOCATION);
        else if(currentContentPage == statusPanel)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.STATUS_HELP_LOCATION);
        else if(currentContentPage == messageBrowser)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.MESSAGE_BROWSER_HELP_LOCATION);
        else if(currentContentPage == eventBrowser)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.SYSTEM_EVENT_HELP_LOCATION);
        else if(currentContentPage == adminPanel)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.ADMIN_HELP_LOCATION);
        else if(currentContentPage == channelEditPanel.transformerPane)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.TRANFORMER_HELP_LOCATION);
        else if(currentContentPage == channelEditPanel.filterPane)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.FILTER_HELP_LOCATION);
        else
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION);
    }
    
    public Frame getThis()
    {
        return this;
    }

}


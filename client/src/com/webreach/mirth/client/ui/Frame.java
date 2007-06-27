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

import com.webreach.mirth.model.ChannelProperties;
import com.webreach.mirth.util.PropertyVerifier;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;
import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.action.ActionFactory;
import org.jdesktop.swingx.action.ActionManager;
import org.jdesktop.swingx.action.BoundAction;

import com.webreach.mirth.client.core.Client;
import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.browsers.event.EventBrowser;
import com.webreach.mirth.client.ui.browsers.message.MessageBrowser;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.client.ui.editors.filter.FilterPane;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.client.ui.util.FileUtil;
import com.webreach.mirth.model.util.ImportConverter;
import com.webreach.mirth.model.Alert;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.ChannelSummary;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.converters.ObjectCloner;
import com.webreach.mirth.model.converters.ObjectClonerException;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.filters.MessageObjectFilter;

/**
 * The main conent frame for the Mirth Client Application. Extends JXFrame and
 * sets up all content.
 */
public class Frame extends JXFrame
{
    private Logger logger = Logger.getLogger(this.getClass());
    public Client mirthClient;
    public DashboardPanel dashboardPanel = null;
    public ChannelPanel channelPanel  = null;
    public SettingsPanel settingsPanel  = null;
    public UserPanel userPanel = null;
    public ChannelSetup channelEditPanel = null;
    public EventBrowser eventBrowser = null;
    public MessageBrowser messageBrowser = null;
    public AlertPanel alertPanel = null;
    public PluginPanel pluginPanel = null;
    public JXTaskPaneContainer taskPaneContainer;
    public List<ChannelStatus> status = null;
    public Map<String, Channel> channels = null;
    public List<User> users = null;
    public List<Alert> alerts = null;
    public ActionManager manager = ActionManager.getInstance();
    public JPanel contentPanel;
    public BorderLayout borderLayout1 = new BorderLayout();
    public StatusBar statusBar;
    public JSplitPane splitPane = new JSplitPane();
    public JScrollPane taskPane = new JScrollPane();
    public JScrollPane contentPane = new JScrollPane();
    public Component currentContentPage = null;
    public JXTaskPaneContainer currentTaskPaneContainer = null;
    public JScrollPane container;
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
    public JXTaskPane alertTasks;
    public JPopupMenu alertPopupMenu;
    public JXTitledPanel rightContainer;
    public JXTitledPanel leftContainer;
    public ArrayList<ConnectorClass> sourceConnectors;
    public ArrayList<ConnectorClass> destinationConnectors;
    private Thread statusUpdater;
    private Border dsb;
    private static Preferences userPreferences;
    private StatusUpdater su;
    private boolean connectionError;
    private ArrayList<CharsetEncodingInformation> avaiableCharsetEncodings = null;
    private List<String> charsetEncodings = null;
    private boolean highlightersSet = false;
    private boolean isEditingChannel = false;
    private Stack<String> workingStack = new Stack<String>();
    public LinkedHashMap<MessageObject.Protocol, String> protocols;
   
    public Frame()
    {
        dsb = BorderFactory.createEmptyBorder();
        leftContainer = new JXTitledPanel();
        rightContainer = new JXTitledPanel();
        channels = new HashMap<String, Channel>();

        taskPaneContainer = new JXTaskPaneContainer();
        sourceConnectors = new ArrayList<ConnectorClass>();
        destinationConnectors = new ArrayList<ConnectorClass>();

        protocols = new LinkedHashMap<MessageObject.Protocol, String>();
        protocols.put(MessageObject.Protocol.HL7V2, "HL7 v2.x");
        protocols.put(MessageObject.Protocol.HL7V3, "HL7 v3.0");
        protocols.put(MessageObject.Protocol.X12, "X12");
        protocols.put(MessageObject.Protocol.EDI, "EDI");
        protocols.put(MessageObject.Protocol.XML, "XML");

        setTitle(UIConstants.TITLE_TEXT);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setIconImage(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/mirthlogo1616.png")).getImage());
        makePaneContainer();

        connectionError = false;
        DragSource ds = DragSource.getDefaultDragSource();
        ds.addDragSourceListener(new DragSourceListener()
        {
            public void dragEnter(DragSourceDragEvent arg0)
            {
            }

            public void dragOver(DragSourceDragEvent arg0)
            {
            }

            public void dropActionChanged(DragSourceDragEvent arg0)
            {
            }

            public void dragExit(DragSourceEvent arg0)
            {
            }

            public void dragDropEnd(DragSourceDropEvent arg0)
            {
                if (highlightersSet)
                    unsetHighlighters();
            }
        });
        this.addComponentListener(new ComponentListener()
        {
            public void componentResized(ComponentEvent e)
            {
                if (channelEditPanel != null && channelEditPanel.filterPane != null)
                {
                    channelEditPanel.filterPane.resizePanes();
                }
                if (channelEditPanel != null && channelEditPanel.transformerPane != null)
                {
                    channelEditPanel.transformerPane.resizePanes();
                }
            }

            public void componentHidden(ComponentEvent e)
            {
            }

            public void componentShown(ComponentEvent e)
            {
            }

            public void componentMoved(ComponentEvent e)
            {
            }

        });

        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                if (!confirmLeave())
                    return;
                
                logout();
                System.exit(0);
            }
        });
    }

    /**
     * Prepares the list of the encodings This method is called from the
     * ChannelSetup class
     * 
     */
    public void setCharsetEncodings()
    {
        if (this.avaiableCharsetEncodings != null)
            return;
        try
        {
            this.charsetEncodings = this.mirthClient.getAvaiableCharsetEncodings();
            this.avaiableCharsetEncodings = new ArrayList();
            this.avaiableCharsetEncodings.add(new CharsetEncodingInformation(UIConstants.DEFAULT_ENCODING_OPTION, "Default"));
            for (int i = 0; i < charsetEncodings.size(); i++)
            {
                String canonical = (String) charsetEncodings.get(i);
                this.avaiableCharsetEncodings.add(new CharsetEncodingInformation(canonical, canonical));
            }
        }
        catch (Exception e)
        {
            alertError("Error getting the charset list:\n " + e);
        }
    }

    /**
     * * Creates all the items in the combo box for the channels
     * 
     * This method is called from each channel
     */
    public void setupCharsetEncodingForChannel(javax.swing.JComboBox channelCombo)
    {
        if (this.avaiableCharsetEncodings == null)
        {
            this.setCharsetEncodings();
        }
        if (this.avaiableCharsetEncodings == null)
        {
            logger.error("Error, the are no encodings detected ");
            return;
        }
        channelCombo.removeAllItems();
        for (int i = 0; i < this.avaiableCharsetEncodings.size(); i++)
        {
            channelCombo.addItem(this.avaiableCharsetEncodings.get(i));
        }
    }

    /**
     * stes the combobox for the string previously selected If the server can't
     * support the encoding, the default one is selectd This method is called
     * from each channel
     */
    public void sePreviousSelectedEncodingForChannel(javax.swing.JComboBox channelCombo, String selectedCharset)
    {
        if (this.avaiableCharsetEncodings == null)
        {
            this.setCharsetEncodings();
        }
        if (this.avaiableCharsetEncodings == null)
        {
            logger.error("Error, there are no encodings detected.");
            return;
        }
        if ((selectedCharset == null) || (selectedCharset.equalsIgnoreCase(UIConstants.DEFAULT_ENCODING_OPTION)))
        {
            channelCombo.setSelectedIndex(0);
        }
        else if (this.charsetEncodings.contains(selectedCharset))
        {
            int index = this.avaiableCharsetEncodings.indexOf(new CharsetEncodingInformation(selectedCharset, selectedCharset));
            if (index < 0)
            {
                logger.error("Syncro lost in the list of the encoding characters");
                index = 0;
            }
            channelCombo.setSelectedIndex(index);
        }
        else
        {
            alertInformation("Sorry, the JVM of the server can't support the previously selected " + selectedCharset + " encoding. Please choose another one or install more encodings in the server");
            channelCombo.setSelectedIndex(0);
        }
    }

    /**
     * Get the strings which identifies the encoding selected by the user
     * 
     * This method is called from each channel
     */
    public String getSelectedEncodingForChannel(javax.swing.JComboBox channelCombo)
    {
        try
        {
            return ((CharsetEncodingInformation) channelCombo.getSelectedItem()).getCanonicalName();
        }
        catch (Throwable t)
        {
            alertInformation("Error " + t);
            return UIConstants.DEFAULT_ENCODING_OPTION;
        }
    }

    /**
     * Called to set up this main window frame.
     */
    public void setupFrame(Client mirthClient)
    {
        this.mirthClient = mirthClient;

        userPreferences = Preferences.systemNodeForPackage(Mirth.class);
        userPreferences.put("defaultServer", PlatformUI.SERVER_NAME);

        splitPane.setDividerSize(0);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        contentPanel = (JPanel) getContentPane();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder());
        taskPane.setBorder(BorderFactory.createEmptyBorder());

        statusBar = new StatusBar();
        statusBar.setBorder(BorderFactory.createEmptyBorder());
        contentPane.setBorder(BorderFactory.createEmptyBorder());

        buildContentPanel(rightContainer, contentPane, false);

        splitPane.add(rightContainer, JSplitPane.RIGHT);
        splitPane.add(taskPane, JSplitPane.LEFT);
        taskPane.setMinimumSize(new Dimension(UIConstants.TASK_PANE_WIDTH, 0));
        splitPane.setDividerLocation(UIConstants.TASK_PANE_WIDTH);

        contentPanel.add(statusBar, BorderLayout.SOUTH);
        contentPanel.add(splitPane, java.awt.BorderLayout.CENTER);
        
        try
        {
            PlatformUI.SERVER_ID = mirthClient.getServerId();
            PlatformUI.SERVER_VERSION = mirthClient.getVersion();
            PlatformUI.BUILD_DATE = mirthClient.getBuildDate();
        }
        catch(ClientException e)
        {
            alertError("Could not get server information.");
        }
        
        setCurrentTaskPaneContainer(taskPaneContainer);
        doShowDashboard();
        channelEditPanel = new ChannelSetup();
        messageBrowser = new MessageBrowser();
        pluginPanel = new PluginPanel();
        su = new StatusUpdater();
        statusUpdater = new Thread(su);
        statusUpdater.start();

        // DEBUGGING THE UIDefaults:
        /*
         UIDefaults uiDefaults = UIManager.getDefaults(); 
         Enumeration enum1 = uiDefaults.keys(); 
         while (enum1.hasMoreElements()) { 
             Object key = enum1.nextElement(); 
             Object val = uiDefaults.get(key);
             if(key.toString().indexOf("ComboBox") != -1)
                System.out.println("UIManager.put(\"" + key.toString() + "\",\"" + (null != val ? val.toString() : "(null)") + "\");");
         }*/
         
    }

    /**
     * Builds the content panel with a title bar and settings.
     */
    private void buildContentPanel(JXTitledPanel container, JScrollPane component, boolean opaque)
    {
        container.getContentContainer().setLayout(new BorderLayout());
        container.setBorder(null);
        container.setTitleFont(new Font("Tahoma", Font.BOLD, 18));
        container.setTitleForeground(UIConstants.HEADER_TITLE_TEXT_COLOR);

        component.setBorder(new LineBorder(Color.GRAY, 1));
        component.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        component.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        container.getContentContainer().add(component);

        if (UIManager.getColor("TaskPaneContainer.backgroundGradientStart") != null)
            container.setTitleDarkBackground(UIManager.getColor("TaskPaneContainer.backgroundGradientStart"));
        else
            container.setTitleDarkBackground(UIManager.getColor("InternalFrame.activeTitleBackground"));

        if (UIManager.getColor("TaskPaneContainer.backgroundGradientEnd") != null)
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

    public void setWorking(final String displayText, final boolean working)
    {
        if (statusBar != null)
        {
            String text = displayText;
            
            if(working)
                workingStack.push(statusBar.getText());
            else if(workingStack.size() > 0)
                text = workingStack.pop();
            
            if(workingStack.size() > 0)
                statusBar.setWorking(true);
            else
                statusBar.setWorking(false);
            
            statusBar.setText(text);
        }
    }

    /**
     * Changes the current content page to the Channel Editor with the new
     * channel specified as the loaded one.
     */
    public void setupChannel(Channel channel)
    {
        setCurrentContentPage(channelEditPanel);
        setBold(viewPane, UIConstants.ERROR_CONSTANT);
        setFocus(channelEditTasks);
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 0, 0, false);
        channelEditPanel.addChannel(channel);
    }

    /**
     * Edits a channel at a specified index, setting that channel as the current
     * channel in the editor.
     */
    public void editChannel(Channel channel)
    {
        setBold(viewPane, UIConstants.ERROR_CONSTANT);
        setCurrentContentPage(channelEditPanel);
        setFocus(channelEditTasks);
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 0, 4, false);
        channelEditPanel.editChannel(channel);
    }

    /**
     * Sets the current content page to the passed in page.
     */
    public void setCurrentContentPage(Component contentPageObject)
    {
        if (contentPageObject == currentContentPage)
            return;

        if (currentContentPage != null)
            contentPane.getViewport().remove(currentContentPage);

        contentPane.getViewport().add(contentPageObject);
        currentContentPage = contentPageObject;
    }

    /**
     * Sets the current task pane container
     */
    public void setCurrentTaskPaneContainer(JXTaskPaneContainer container)
    {
        if (container == currentTaskPaneContainer)
            return;
            
        if (currentTaskPaneContainer != null)
            taskPane.getViewport().remove(currentTaskPaneContainer);

        taskPane.getViewport().add(container);
        currentTaskPaneContainer = container;
    }

    /**
     * Makes all of the task panes and shows the dashboard panel.
     */
    private void makePaneContainer()
    {
        createViewPane();
        createSettingsPane();
        createChannelPane();
        createChannelEditPane();
        createDashboardPane();
        createEventPane();
        createMessagePane();
        createUserPane();
        createAlertPane();
        createOtherPane();
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
        viewPane.add(initActionCallback("doShowDashboard", "Contains information about your currently deployed channels.", ActionFactory.createBoundAction("showDashboard", "Dashboard", "D"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/status.png"))));
        viewPane.add(initActionCallback("doShowChannel", "Contains various operations to perform on your channels.", ActionFactory.createBoundAction("showChannel", "Channels", "C"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/channel.png"))));
        viewPane.add(initActionCallback("doShowUsers", "Contains information on users.", ActionFactory.createBoundAction("showUsers", "Users", "U"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/admin.png"))));
        viewPane.add(initActionCallback("doShowSettings", "Contains local and system settings.", ActionFactory.createBoundAction("showSettings", "Settings", "S"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/settings.png"))));
        viewPane.add(initActionCallback("doShowAlerts", "Contains alert settings.", ActionFactory.createBoundAction("showAlerts", "Alerts", "A"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/alerts.png"))));
        viewPane.add(initActionCallback("doShowEvents", "Show the event logs for the system.", ActionFactory.createBoundAction("doShowEvents", "Events", "E"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/logs.png"))));
        viewPane.add(initActionCallback("doShowPlugins", "Show the plugins loaded for the system.", ActionFactory.createBoundAction("doShowPlugins", "Plugins", "P"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/plugin.png"))));
        setNonFocusable(viewPane);
        taskPaneContainer.add(viewPane);
        viewPane.setVisible(true);
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

        settingsTasks.add(initActionCallback("doRefreshSettings", "Refresh settings.", ActionFactory.createBoundAction("doRefreshSettings", "Refresh", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png"))));
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        refresh.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doRefreshSettings();
            }
        });
        settingsPopupMenu.add(refresh);

        settingsTasks.add(initActionCallback("doSaveSettings", "Save settings.", ActionFactory.createBoundAction("doSaveSettings", "Save Settings", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png"))));
        JMenuItem saveSettings = new JMenuItem("Save Settings");
        saveSettings.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png")));
        saveSettings.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
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

        channelTasks.add(initActionCallback("doRefreshChannels", "Refresh the list of channels.", ActionFactory.createBoundAction("doRefreshChannels", "Refresh", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png"))));
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        refresh.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doRefreshChannels();
            }
        });
        channelPopupMenu.add(refresh);

        channelTasks.add(initActionCallback("doDeployAll", "Deploy all currently enabled channels.", ActionFactory.createBoundAction("doDeployAll", "Deploy All", "A"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/deployall.png"))));
        JMenuItem deployAll = new JMenuItem("Deploy All");
        deployAll.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/deployall.png")));
        deployAll.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doDeployAll();
            }
        });
        channelPopupMenu.add(deployAll);

        channelTasks.add(initActionCallback("doNewChannel", "Create a new channel.", ActionFactory.createBoundAction("doNewChannel", "New Channel", "N"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/channel_add.png"))));
        JMenuItem newChannel = new JMenuItem("New Channel");
        newChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/channel_add.png")));
        newChannel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doNewChannel();
            }
        });
        channelPopupMenu.add(newChannel);

        channelTasks.add(initActionCallback("doImport", "Import a channel from an XML file.", ActionFactory.createBoundAction("doImport", "Import Channel", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/import.png"))));
        JMenuItem importChannel = new JMenuItem("Import Channel");
        importChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/import.png")));
        importChannel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doImport();
            }
        });
        channelPopupMenu.add(importChannel);

        channelTasks.add(initActionCallback("doExportAll", "Export all of the channels to XML files.", ActionFactory.createBoundAction("doExportAll", "Export All Channels", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png"))));
        JMenuItem exportAllChannels = new JMenuItem("Export All Channels");
        exportAllChannels.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png")));
        exportAllChannels.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doExportAll();
            }
        });
        channelPopupMenu.add(exportAllChannels);

        channelTasks.add(initActionCallback("doExport", "Export the currently selected channel to an XML file.", ActionFactory.createBoundAction("doExport", "Export Channel", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png"))));
        JMenuItem exportChannel = new JMenuItem("Export Channel");
        exportChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png")));
        exportChannel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doExport();
            }
        });
        channelPopupMenu.add(exportChannel);

        channelTasks.add(initActionCallback("doCloneChannel", "Clone the currently selected channel.", ActionFactory.createBoundAction("doCloneChannel", "Clone Channel", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/clone.png"))));
        JMenuItem cloneChannel = new JMenuItem("Clone Channel");
        cloneChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/clone.png")));
        cloneChannel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doCloneChannel();
            }
        });
        channelPopupMenu.add(cloneChannel);

        channelTasks.add(initActionCallback("doEditChannel", "Edit the currently selected channel.", ActionFactory.createBoundAction("doEditChannel", "Edit Channel", "I"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/channel_edit.png"))));
        JMenuItem editChannel = new JMenuItem("Edit Channel");
        editChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/channel_edit.png")));
        editChannel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doEditChannel();
            }
        });
        channelPopupMenu.add(editChannel);

        channelTasks.add(initActionCallback("doDeleteChannel", "Delete the currently selected channel.", ActionFactory.createBoundAction("doDeleteChannel", "Delete Channel", "L"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/channel_delete.png"))));
        JMenuItem deleteChannel = new JMenuItem("Delete Channel");
        deleteChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/channel_delete.png")));
        deleteChannel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doDeleteChannel();
            }
        });
        channelPopupMenu.add(deleteChannel);

        channelTasks.add(initActionCallback("doEnableChannel", "Enable the currently selected channel.", ActionFactory.createBoundAction("doEnableChannel", "Enable Channel", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start.png"))));
        JMenuItem enableChannel = new JMenuItem("Enable Channel");
        enableChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start.png")));
        enableChannel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doEnableChannel();
            }
        });
        channelPopupMenu.add(enableChannel);

        channelTasks.add(initActionCallback("doDisableChannel", "Disable the currently selected channel.", ActionFactory.createBoundAction("doDisableChannel", "Disable Channel", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stop.png"))));
        JMenuItem disableChannel = new JMenuItem("Disable Channel");
        disableChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stop.png")));
        disableChannel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doDisableChannel();
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

        channelEditTasks.add(initActionCallback("doSaveChannel", "Save all changes made to this channel.", ActionFactory.createBoundAction("doSaveChannel", "Save Channel", "S"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png"))));
        JMenuItem saveChanges = new JMenuItem("Save Changes");
        saveChanges.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png")));
        saveChanges.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doSaveChannel();
            }
        });
        channelEditPopupMenu.add(saveChanges);

        channelEditTasks.add(initActionCallback("doValidate", "Validate the currently visible form.", ActionFactory.createBoundAction("doValidate", "Validate Form", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/accept.png"))));
        JMenuItem validateForm = new JMenuItem("Validate Form");
        validateForm.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/accept.png")));
        validateForm.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doValidate();
            }
        });
        channelEditPopupMenu.add(validateForm);

        channelEditTasks.add(initActionCallback("doNewDestination", "Create a new destination.", ActionFactory.createBoundAction("doNewDestination", "New Destination", "N"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/add.png"))));
        JMenuItem newDestination = new JMenuItem("New Destination");
        newDestination.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/add.png")));
        newDestination.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doNewDestination();
            }
        });
        channelEditPopupMenu.add(newDestination);

        channelEditTasks.add(initActionCallback("doDeleteDestination", "Delete the currently selected destination.", ActionFactory.createBoundAction("doDeleteDestination", "Delete Destination", "L"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png"))));
        JMenuItem deleteDestination = new JMenuItem("Delete Destination");
        deleteDestination.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")));
        deleteDestination.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doDeleteDestination();
            }
        });
        channelEditPopupMenu.add(deleteDestination);

        channelEditTasks.add(initActionCallback("doCloneDestination", "Clones the currently selected destination.", ActionFactory.createBoundAction("doCloneDestination", "Clone Destination", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/clone.png"))));
        JMenuItem cloneDestination = new JMenuItem("Clone Destination");
        cloneDestination.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/clone.png")));
        cloneDestination.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doCloneDestination();
            }
        });
        channelEditPopupMenu.add(cloneDestination);

        channelEditTasks.add(initActionCallback("doMoveDestinationUp", "Move the currently selected destination up.", ActionFactory.createBoundAction("doMoveDestinationUp", "Move Dest. Up", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/arrow_up.png"))));
        JMenuItem moveDestinationUp = new JMenuItem("Move Destination Up");
        moveDestinationUp.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/arrow_up.png")));
        moveDestinationUp.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doMoveDestinationUp();
            }
        });
        channelEditPopupMenu.add(moveDestinationUp);

        channelEditTasks.add(initActionCallback("doMoveDestinationDown", "Move the currently selected destination down.", ActionFactory.createBoundAction("doMoveDestinationDown", "Move Dest. Down", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/arrow_down.png"))));
        JMenuItem moveDestinationDown = new JMenuItem("Move Destination Down");
        moveDestinationDown.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/arrow_down.png")));
        moveDestinationDown.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doMoveDestinationDown();
            }
        });
        channelEditPopupMenu.add(moveDestinationDown);

        channelEditTasks.add(initActionCallback("doEditFilter", "Edit the filter for the currently selected destination.", ActionFactory.createBoundAction("doEditFilter", "Edit Filter", "F"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/edit.png"))));
        JMenuItem editFilter = new JMenuItem("Edit Filter");
        editFilter.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/edit.png")));
        editFilter.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doEditFilter();
            }
        });
        channelEditPopupMenu.add(editFilter);

        channelEditTasks.add(initActionCallback("doEditTransformer", "Edit the transformer for the currently selected destination.", ActionFactory.createBoundAction("doEditTransformer", "Edit Transformer", "T"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/edit.png"))));
        JMenuItem editTransformer = new JMenuItem("Edit Transformer");
        editTransformer.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/edit.png")));
        editTransformer.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doEditTransformer();
            }
        });
        channelEditPopupMenu.add(editTransformer);

        channelEditTasks.add(initActionCallback("doExport", "Export the currently selected channel to an XML file.", ActionFactory.createBoundAction("doExport", "Export Channel", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png"))));
        JMenuItem exportChannel = new JMenuItem("Export Channel");
        exportChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png")));
        exportChannel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doExport();
            }
        });
        channelEditPopupMenu.add(exportChannel);

        setNonFocusable(channelEditTasks);
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 0, 8, false);
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 9, 9, true);
        taskPaneContainer.add(channelEditTasks);
    }

    /**
     * Creates the status task pane.
     */
    private void createDashboardPane()
    {
        // Create Status Tasks Pane
        statusTasks = new JXTaskPane();
        statusPopupMenu = new JPopupMenu();
        statusTasks.setTitle("Status Tasks");
        statusTasks.setFocusable(false);

        statusTasks.add(initActionCallback("doRefreshStatuses", "Refresh the list of statuses.", ActionFactory.createBoundAction("doRefresh", "Refresh", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png"))));
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        refresh.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doRefreshStatuses();
            }
        });
        statusPopupMenu.add(refresh);

        statusTasks.add(initActionCallback("doStartAll", "Start all channels that are currently deployed.", ActionFactory.createBoundAction("doStartAll", "Start All Channels", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start1.png"))));
        JMenuItem startAllChannels = new JMenuItem("Start All Channels");
        startAllChannels.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start1.png")));
        startAllChannels.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doStartAll();
            }
        });
        statusPopupMenu.add(startAllChannels);
        
        statusTasks.add(initActionCallback("doSendMessage", "Send messages to the currently selected channel.", ActionFactory.createBoundAction("doSendMessage", "Send Message", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/messages.png"))));
        JMenuItem sendMessages = new JMenuItem("Send Message");
        sendMessages.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/messages.png")));
        sendMessages.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doSendMessage();
            }
        });
        statusPopupMenu.add(sendMessages);
        
        statusTasks.add(initActionCallback("doShowMessages", "Show the messages for the currently selected channel.", ActionFactory.createBoundAction("doShowMessages", "View Messages", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/messages2.png"))));
        JMenuItem showMessages = new JMenuItem("View Messages");
        showMessages.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/messages2.png")));
        showMessages.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doShowMessages();
            }
        });
        statusPopupMenu.add(showMessages);

        statusTasks.add(initActionCallback("doRemoveAllMessages", "Remove all Messages in this channel.", ActionFactory.createBoundAction("doRemoveAllMessages", "Remove All Messages", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png"))));
        JMenuItem removeAllMessages = new JMenuItem("Remove All Messages");
        removeAllMessages.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")));
        removeAllMessages.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doRemoveAllMessages();
            }
        });
        statusPopupMenu.add(removeAllMessages);
        
        statusTasks.add(initActionCallback("doClearStats", "Reset the statistics for this channel.", ActionFactory.createBoundAction("doClearStats", "Clear Statistics", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stats.png"))));
        JMenuItem clearStats = new JMenuItem("Clear Statistics");
        clearStats.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stats.png")));
        clearStats.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doClearStats();
            }
        });
        statusPopupMenu.add(clearStats);

        statusTasks.add(initActionCallback("doStart", "Start the currently selected channel.", ActionFactory.createBoundAction("doStart", "Start Channel", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start.png"))));
        JMenuItem startChannel = new JMenuItem("Start Channel");
        startChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start.png")));
        startChannel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doStart();
            }
        });
        statusPopupMenu.add(startChannel);

        statusTasks.add(initActionCallback("doPause", "Pause the currently selected channel.", ActionFactory.createBoundAction("doPause", "Pause Channel", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/pause.png"))));
        JMenuItem pauseChannel = new JMenuItem("Pause Channel");
        pauseChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/pause.png")));
        pauseChannel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doPause();
            }
        });
        statusPopupMenu.add(pauseChannel);

        statusTasks.add(initActionCallback("doStop", "Stop the currently selected channel.", ActionFactory.createBoundAction("doStop", "Stop Channel", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stop.png"))));
        JMenuItem stopChannel = new JMenuItem("Stop Channel");
        stopChannel.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stop.png")));
        stopChannel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doStop();
            }
        });
        statusPopupMenu.add(stopChannel);

        setNonFocusable(statusTasks);
        setVisibleTasks(statusTasks, statusPopupMenu, 1, -1, false);
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

        eventTasks.add(initActionCallback("doRefreshEvents", "Refresh the list of events with the given filter.", ActionFactory.createBoundAction("doRefreshEvents", "Refresh", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png"))));
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        refresh.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doRefreshEvents();
            }
        });
        eventPopupMenu.add(refresh);

        eventTasks.add(initActionCallback("doClearEvents", "Clear the System Events.", ActionFactory.createBoundAction("doClearEvents", "Clear Events", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png"))));
        JMenuItem clearEvents = new JMenuItem("Clear Events");
        clearEvents.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")));
        clearEvents.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
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

        messageTasks.add(initActionCallback("doRefreshMessages", "Refresh the list of messages with the given filter.", ActionFactory.createBoundAction("doRefreshMessages", "Refresh", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png"))));
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        refresh.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doRefreshMessages();
            }
        });
        messagePopupMenu.add(refresh);
        
        messageTasks.add(initActionCallback("doSendMessage", "Send a message to the channel.", ActionFactory.createBoundAction("doSendMessage", "Send Message", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/messages.png"))));
        JMenuItem sendMessage = new JMenuItem("Send Message");
        sendMessage.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/messages.png")));
        sendMessage.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doSendMessage();
            }
        });
        messagePopupMenu.add(sendMessage);
        
        messageTasks.add(initActionCallback("doImportMessages", "Import messages from a file.", ActionFactory.createBoundAction("doImportMessages", "Import Messages", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/import.png"))));
        JMenuItem importMessages = new JMenuItem("Import Messages");
        importMessages.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/import.png")));
        importMessages.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doImportMessages();
            }
        });
        messagePopupMenu.add(importMessages);
        
        messageTasks.add(initActionCallback("doExportMessages", "Export all currently viewed messages.", ActionFactory.createBoundAction("doExportMessages", "Export Filtered Messages", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png"))));
        JMenuItem exportMessages = new JMenuItem("Export Filtered Messages");
        exportMessages.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png")));
        exportMessages.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doExportMessages();
            }
        });
        messagePopupMenu.add(exportMessages);

        messageTasks.add(initActionCallback("doRemoveAllMessages", "Remove all Message Events in this channel.", ActionFactory.createBoundAction("doRemoveAllMessages", "Remove All Messages", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png"))));
        JMenuItem removeAllMessages = new JMenuItem("Remove All Messages");
        removeAllMessages.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")));
        removeAllMessages.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doRemoveAllMessages();
            }
        });
        messagePopupMenu.add(removeAllMessages);

        messageTasks.add(initActionCallback("doRemoveFilteredMessages", "Remove all Message Events in the current filter.", ActionFactory.createBoundAction("doRemoveFilteredMessages", "Remove Filtered Messages", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png"))));
        JMenuItem removeFilteredMessages = new JMenuItem("Remove Filtered Messages");
        removeFilteredMessages.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")));
        removeFilteredMessages.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doRemoveFilteredMessages();
            }
        });
        messagePopupMenu.add(removeFilteredMessages);

        messageTasks.add(initActionCallback("doRemoveMessage", "Remove the selected Message Event.", ActionFactory.createBoundAction("doRemoveMessage", "Remove Message", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png"))));
        JMenuItem removeMessage = new JMenuItem("Remove Message");
        removeMessage.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")));
        removeMessage.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doRemoveMessage();
            }
        });
        messagePopupMenu.add(removeMessage);

        messageTasks.add(initActionCallback("doReprocessFilteredMessages", "Reprocess all Message Events in the current filter.", ActionFactory.createBoundAction("doReprocessFilteredMessages", "Reprocess Filtered Messages", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/deployall.png"))));
        JMenuItem reprocessFilteredMessages = new JMenuItem("Reprocess Filtered Messages");
        reprocessFilteredMessages.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/deployall.png")));
        reprocessFilteredMessages.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doReprocessFilteredMessages();
            }
        });
        messagePopupMenu.add(reprocessFilteredMessages);

        messageTasks.add(initActionCallback("doReprocessMessage", "Reprocess the selected Message.", ActionFactory.createBoundAction("doReprocessMessage", "Reprocess Message", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/deploy.png"))));
        JMenuItem reprocessMessage = new JMenuItem("Reprocess Message");
        reprocessMessage.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/deploy.png")));
        reprocessMessage.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doReprocessMessage();
            }
        });
        messagePopupMenu.add(reprocessMessage);

        setNonFocusable(messageTasks);
        setVisibleTasks(messageTasks, messagePopupMenu, 6, -1, false);
        setVisibleTasks(messageTasks, messagePopupMenu, 7, 7, true);
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

        userTasks.add(initActionCallback("doRefreshUser", "Refresh the list of users.", ActionFactory.createBoundAction("doRefreshUser", "Refresh", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png"))));
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        refresh.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doRefreshUser();
            }
        });
        userPopupMenu.add(refresh);

        userTasks.add(initActionCallback("doNewUser", "Create a new user.", ActionFactory.createBoundAction("doNewChannel", "New User", "N"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/user_add.png"))));
        JMenuItem newUser = new JMenuItem("New User");
        newUser.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/user_add.png")));
        newUser.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doNewUser();
            }
        });
        userPopupMenu.add(newUser);

        userTasks.add(initActionCallback("doEditUser", "Edit the currently selected user.", ActionFactory.createBoundAction("doEditChannel", "Edit User", "I"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/user_edit.png"))));
        JMenuItem editUser = new JMenuItem("Edit User");
        editUser.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/user_edit.png")));
        editUser.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doEditUser();
            }
        });
        userPopupMenu.add(editUser);

        userTasks.add(initActionCallback("doDeleteUser", "Delete the currently selected user.", ActionFactory.createBoundAction("doDeleteChannel", "Delete User", "L"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/user_delete.png"))));
        JMenuItem deleteUser = new JMenuItem("Delete User");
        deleteUser.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/user_delete.png")));
        deleteUser.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doDeleteUser();
            }
        });
        userPopupMenu.add(deleteUser);

        setNonFocusable(userTasks);
        setVisibleTasks(userTasks, userPopupMenu, 2, -1, false);
        taskPaneContainer.add(userTasks);
    }

    /**
     * Creates the channel edit task pane.
     */
    private void createAlertPane()
    {
        // Create Alert Edit Tasks Pane
        alertTasks = new JXTaskPane();
        alertPopupMenu = new JPopupMenu();
        alertTasks.setTitle("Alert Tasks");
        alertTasks.setFocusable(false);

        alertTasks.add(initActionCallback("doRefreshAlerts", "Refresh the list of alerts.", ActionFactory.createBoundAction("doRefreshAlerts", "Refresh", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png"))));
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        refresh.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doRefreshAlerts();
            }
        });
        alertPopupMenu.add(refresh);

        alertTasks.add(initActionCallback("doSaveAlerts", "Save all changes made to all alerts.", ActionFactory.createBoundAction("doSaveAlerts", "Save Alerts", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png"))));
        JMenuItem saveChanges = new JMenuItem("Save Alerts");
        saveChanges.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png")));
        saveChanges.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doSaveChannel();
            }
        });
        alertPopupMenu.add(saveChanges);

        alertTasks.add(initActionCallback("doNewAlert", "Create a new alert.", ActionFactory.createBoundAction("doNewAlert", "New Alert", "N"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/alert_add.png"))));
        JMenuItem newAlert = new JMenuItem("New Alert");
        newAlert.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/alert_add.png")));
        newAlert.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doNewAlert();
            }
        });
        alertPopupMenu.add(newAlert);

        alertTasks.add(initActionCallback("doDeleteAlert", "Delete the currently selected alert.", ActionFactory.createBoundAction("doDeleteAlert", "Delete Alert", "L"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/alert_delete.png"))));
        JMenuItem deleteAlert = new JMenuItem("Delete Alert");
        deleteAlert.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/alert_delete.png")));
        deleteAlert.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doDeleteAlert();
            }
        });
        alertPopupMenu.add(deleteAlert);

        alertTasks.add(initActionCallback("doEnableAlert", "Enable the currently selected alert.", ActionFactory.createBoundAction("doEnableAlert", "Enable Alert", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start.png"))));
        JMenuItem enableAlert = new JMenuItem("Enable Alert");
        enableAlert.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start.png")));
        enableAlert.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doEnableAlert();
            }
        });
        alertPopupMenu.add(enableAlert);

        alertTasks.add(initActionCallback("doDisableAlert", "Disable the currently selected alert.", ActionFactory.createBoundAction("doDisableAlert", "Disable Alert", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stop.png"))));
        JMenuItem disableAlert = new JMenuItem("Disable Alert");
        disableAlert.setIcon(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stop.png")));
        disableAlert.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doDisableAlert();
            }
        });
        alertPopupMenu.add(disableAlert);
        setVisibleTasks(alertTasks, alertPopupMenu, 0, 0, false);
        setVisibleTasks(alertTasks, alertPopupMenu, 1, 1, false);
        setVisibleTasks(alertTasks, alertPopupMenu, 2, 2, true);
        setVisibleTasks(alertTasks, alertPopupMenu, 3, 5, false);
        setNonFocusable(alertTasks);
        taskPaneContainer.add(alertTasks);
    }

    /**
     * Creates the other task pane.
     */
    private void createOtherPane()
    {
        // Create Other Pane
        otherPane = new JXTaskPane();
        otherPane.setTitle("Other");
        otherPane.setFocusable(false);
        otherPane.add(initActionCallback("doHelp", "Open browser for help on this topic.", ActionFactory.createBoundAction("doHelp", "Help on this topic", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/help.png"))));
        otherPane.add(initActionCallback("goToAbout", "View the about page for Mirth.", ActionFactory.createBoundAction("goToAbout", "About Mirth", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/about.png"))));
        otherPane.add(initActionCallback("goToMirth", "View Mirth's homepage.", ActionFactory.createBoundAction("goToMirth", "Visit MirthProject.org", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/home.png"))));
        otherPane.add(initActionCallback("doLogout", "Logout and return to the login screen.", ActionFactory.createBoundAction("doLogout", "Logout", ""), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/disconnect.png"))));
        setNonFocusable(otherPane);
        taskPaneContainer.add(otherPane);
        otherPane.setVisible(true);
    }

    public JXTaskPane getOtherPane()
    {
        return otherPane;
    }

    /**
     * Initializes the bound method call for the task pane actions.
     */
    private BoundAction initActionCallback(String callbackMethod, String toolTip, BoundAction boundAction, ImageIcon icon)
    {
        if (icon != null)
            boundAction.putValue(Action.SMALL_ICON, icon);
        boundAction.putValue(Action.SHORT_DESCRIPTION, toolTip);
        boundAction.registerCallback(this, callbackMethod);
        return boundAction;
    }

    /**
     * Alerts the user with a yes/no option with the passed in 'message'
     */
    public boolean alertOption(String message)
    {
        int option = JOptionPane.showConfirmDialog(this, message, "Select an Option", JOptionPane.YES_NO_OPTION);
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
        int option = JOptionPane.showConfirmDialog(this, message, "Select an Option", JOptionPane.OK_CANCEL_OPTION);
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
        if (connectionError)
            return;

        if (message.indexOf("Unauthorized") != -1 || message.indexOf("reset") != -1)
        {
            connectionError = true;
            if (currentContentPage == dashboardPanel)
                su.interruptThread();
            alertWarning("Sorry your connection to Mirth has either timed out or there was an error in the connection.  Please login again.");
            if (!exportChannelOnError())
                return;
            this.dispose();
            Mirth.main(new String[] { PlatformUI.SERVER_NAME, PlatformUI.CLIENT_VERSION });
            return;
        }
        else if (message.indexOf("Connection refused") != -1)
        {
            connectionError = true;
            if (currentContentPage == dashboardPanel)
                su.interruptThread();
            alertWarning("The Mirth server " + PlatformUI.SERVER_NAME + " is no longer running.  Please start it and login again.");
            if (!exportChannelOnError())
                return;
            this.dispose();
            Mirth.main(new String[] { PlatformUI.SERVER_NAME, PlatformUI.CLIENT_VERSION });
            return;
        }

        logger.error(strace);

        String stackTrace = message + "\n";
        for (int i = 0; i < strace.length; i++)
            stackTrace += strace[i].toString() + "\n";

        ErrorDialog dlg = new ErrorDialog(stackTrace);
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
        for (int i = 0; i < pane.getContentPane().getComponentCount(); i++)
            pane.getContentPane().getComponent(i).setFont(UIConstants.TEXTFIELD_PLAIN_FONT);

        if (index != UIConstants.ERROR_CONSTANT)
            pane.getContentPane().getComponent(index).setFont(UIConstants.TEXTFIELD_BOLD_FONT);
    }

    /**
     * Sets the visible task pane to the specified 'pane'
     */
    public void setFocus(JXTaskPane pane)
    {
        // ignore the first and last components
        for(int i = 1; i < taskPaneContainer.getComponentCount()-1; i++)
        {
            taskPaneContainer.getComponent(i).setVisible(false);
        }
        
        if(pane != null)
            pane.setVisible(true);
    }

    /**
     * Sets all components in pane to be non-focusable.
     */
    public void setNonFocusable(JXTaskPane pane)
    {
        for (int i = 0; i < pane.getContentPane().getComponentCount(); i++)
            pane.getContentPane().getComponent(i).setFocusable(false);
    }

    /**
     * Sets the visibible tasks in the given 'pane' and 'menu'. The method takes
     * an interval of indicies (end index should be -1 to go to the end), as
     * well as a whether they should be set to visible or not-visible.
     */
    public void setVisibleTasks(JXTaskPane pane, JPopupMenu menu, int startIndex, int endIndex, boolean visible)
    {
        if (endIndex == -1)
        {
            for (int i = startIndex; i < pane.getContentPane().getComponentCount(); i++)
            {
                pane.getContentPane().getComponent(i).setVisible(visible);
                menu.getComponent(i).setVisible(visible);
            }
        }
        else
        {
            for (int i = startIndex; (i <= endIndex) && (i < pane.getContentPane().getComponentCount()); i++)
            {
                pane.getContentPane().getComponent(i).setVisible(visible);
                menu.getComponent(i).setVisible(visible);
            }
        }
    }

    /**
     * A prompt to ask the user if he would like to save the changes made before
     * leaving the page.
     */
    public boolean confirmLeave()
    {
        if (channelEditPanel != null && (currentContentPage == channelEditPanel && channelEditTasks.getContentPane().getComponent(0).isVisible() || (currentContentPage == channelEditPanel.transformerPane && channelEditPanel.transformerPane.modified) || (currentContentPage == channelEditPanel.filterPane && channelEditPanel.filterPane.modified)))
        {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the channel changes?");
            if (option == JOptionPane.YES_OPTION)
            {
                if (!channelEditPanel.saveChanges())
                    return false;
            }
            else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION)
                return false;

            channelEditTasks.getContentPane().getComponent(0).setVisible(false);
        }
        else if (settingsPanel != null && currentContentPage == settingsPanel && settingsTasks.getContentPane().getComponent(1).isVisible())
        {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the settings?");

            if (option == JOptionPane.YES_OPTION)
                settingsPanel.saveSettings();
            else if (option == JOptionPane.NO_OPTION)
                settingsPanel.loadSettings();
            else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION)
                return false;

            settingsTasks.getContentPane().getComponent(1).setVisible(false);
        }
        else if (alertPanel != null && currentContentPage == alertPanel && alertTasks.getContentPane().getComponent(1).isVisible())
        {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the alerts?");

            if (option == JOptionPane.YES_OPTION)
                doSaveAlerts();
            else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION)
                return false;

            alertTasks.getContentPane().getComponent(1).setVisible(false);
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
            if (!mirthClient.updateChannel(curr, false))
            {
                if (alertOption("This channel has been modified since you first opened it.  Would you like to overwrite it?"))
                    mirthClient.updateChannel(curr, true);
                else
                    return false;
            }
            retrieveChannels();
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
    public void updateUser(final User curr, final String password)
    {
        setWorking("Saving user...", true);
               
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                try
                {
                    mirthClient.updateUser(curr, password);
                    users = mirthClient.getUser(null);
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }
            
            public void done()
            {
                userPanel.updateUserTable();
                setWorking("", false);
            }
        };
        
        worker.execute();
    }

    /**
     * Checks to see if the passed in channel name already exists
     */
    public boolean checkChannelName(String name, String id)
    {
        if (name.equals(""))
        {
            alertWarning("Channel name cannot be empty.");
            return false;
        }
        
        if(name.length() > 40)
        {
            alertWarning("Channel name cannot be longer than 40 characters.");
            return false;
        }
        
        // Following code copied from MirthFieldConstaints, must be the same to check for valid channel names the same way.
        char[] chars = name.toCharArray();
        for (char c : chars)
        {
            int cVal = (int)c;
            if ((cVal < 65 || cVal > 90) && (cVal < 97 || cVal > 122) && (cVal != 32) && (cVal != 45) && (cVal != 95))
            {
                try
                {
                    if (Double.isNaN(Double.parseDouble(c + "")))
                    {
                        alertWarning("Channel name cannot have special characters besides hyphen, underscore, and space.");
                        return false;
                    }
                }
                catch (Exception e)
                {
                    alertWarning("Channel name cannot have special characters besides hyphen, underscore, and space.");
                    return false;
                }
            }
        }

        for (Channel channel : channels.values())
        {
            if (channel.getName().equalsIgnoreCase(name) && !channel.getId().equals(id))
            {
                alertWarning("Channel \"" + name + "\" already exists. Please choose a unique name.");
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
        if (channelEditPanel != null && currentContentPage == channelEditPanel)
            channelEditTasks.getContentPane().getComponent(0).setVisible(true);
        else if (channelEditPanel != null && currentContentPage == channelEditPanel.transformerPane)
            channelEditPanel.transformerPane.modified = true;
        else if (channelEditPanel != null && currentContentPage == channelEditPanel.filterPane)
            channelEditPanel.filterPane.modified = true;
        else if (settingsPanel != null && currentContentPage == settingsPanel)
            settingsTasks.getContentPane().getComponent(1).setVisible(true);
        else if (alertPanel != null && currentContentPage == alertPanel)
            alertTasks.getContentPane().getComponent(1).setVisible(true);
    }

    /**
     * Disables the save button for the needed page.
     */
    public void disableSave()
    {
        if (currentContentPage == channelEditPanel)
            channelEditTasks.getContentPane().getComponent(0).setVisible(false);
        else if (channelEditPanel != null && currentContentPage == channelEditPanel.transformerPane)
            channelEditPanel.transformerPane.modified = false;
        else if (channelEditPanel != null && currentContentPage == channelEditPanel.filterPane)
            channelEditPanel.filterPane.modified = false;
        else if (currentContentPage == settingsPanel)
            settingsTasks.getContentPane().getComponent(1).setVisible(false);
        else if (alertPanel != null && currentContentPage == alertPanel)
            alertTasks.getContentPane().getComponent(1).setVisible(false);
    }

    // ////////////////////////////////////////////////////////////
    // --- All bound actions are beneath this point --- //
    // ////////////////////////////////////////////////////////////

    public void goToMirth()
    {
        BareBonesBrowserLaunch.openURL("http://www.mirthproject.org/");
    }

    public void goToAbout()
    {
        new AboutMirth();
    }

    public void doShowDashboard()
    {
        if (dashboardPanel == null)
            dashboardPanel = new DashboardPanel();

        if (!confirmLeave())
            return;

        setBold(viewPane, 0);
        setPanelName("Dashboard");
        setCurrentContentPage(dashboardPanel);
        setFocus(statusTasks);
        
        doRefreshStatuses();
    }

    public void doShowChannel()
    {
        if (channelPanel == null)
            channelPanel = new ChannelPanel();

        if (!confirmLeave())
            return;
                    
        setBold(viewPane, 1);
        setPanelName("Channels");
        setCurrentContentPage(channelPanel);
        setFocus(channelTasks);
        
        doRefreshChannels();
    }

    public void doShowUsers()
    {
        if (userPanel == null)
            userPanel = new UserPanel();

        if (!confirmLeave())
            return;

        setWorking("Loading users...", true);
        
        setBold(viewPane, 2);
        setPanelName("Users");
        setCurrentContentPage(userPanel);
        
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                refreshUser();
                return null;
            }

            public void done()
            {
                setFocus(userTasks);
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doShowSettings()
    {
        if (settingsPanel == null)
            settingsPanel = new SettingsPanel();

        if (!confirmLeave())
            return;

        setWorking("Loading settings...", true);
        
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                settingsPanel.loadSettings();
                return null;
            }

            public void done()
            {
                setBold(viewPane, 3);
                setPanelName("Settings");
                setCurrentContentPage(settingsPanel);
                setFocus(settingsTasks);
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doShowAlerts()
    {
        if (alertPanel == null)
            alertPanel = new AlertPanel();

        if (!confirmLeave())
            return;

        setWorking("Loading alerts...", true);
                
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                retrieveChannels();
                refreshAlerts();
                return null;
            }

            public void done()
            {
                alertPanel.updateAlertTable(false);
                setBold(viewPane, 4);
                setPanelName("Alerts");
                setCurrentContentPage(alertPanel);
                alertPanel.setDefaultAlert();
                setFocus(alertTasks);
                disableSave();
                setWorking("", false);
            }
        };

        worker.execute();
    }
    
    public void doShowPlugins()
    {
        if (!confirmLeave())
            return;

        setWorking("Loading plugins...", true);
                
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                return null;
            }

            public void done()
            {
                setBold(viewPane, 6);
                setPanelName("Plugins");
                setCurrentContentPage(pluginPanel);
                pluginPanel.loadDefaultPanel();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doLogout()
    {
        if (!confirmLeave())
            return;

        logout();
    }

    public void logout()
    {
        if (currentContentPage == dashboardPanel)
            su.interruptThread();

        userPreferences = Preferences.systemNodeForPackage(Mirth.class);
        userPreferences.putInt("maximizedState", getExtendedState());
        userPreferences.putInt("width", getWidth());
        userPreferences.putInt("height", getHeight());
        
        pluginPanel.stopPlugins();
        
        try
        {
            mirthClient.logout();
            this.dispose();
            Mirth.main(new String[] { PlatformUI.SERVER_NAME, PlatformUI.CLIENT_VERSION });
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
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
        Channel channel = new Channel();

        try
        {
            channel.setId(mirthClient.getGuid());
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }

        channel.setName("");
        channel.setEnabled(true);
        channel.getProperties().setProperty("initialState", "Started");
        setupChannel(channel);
    }

    public void doEditChannel()
    {
        if (isEditingChannel)
            return;
        else
            isEditingChannel = true;

        if (channelPanel.getSelectedChannel() == null)
            JOptionPane.showMessageDialog(Frame.this, "Channel no longer exists.");
        else
        {
            try
            {
                editChannel((Channel) ObjectCloner.deepCopy(channelPanel.getSelectedChannel()));
            }
            catch (ObjectClonerException e)
            {
                alertException(e.getStackTrace(), e.getMessage());
            }
        }
        isEditingChannel = false;
    }

    public void doDeleteChannel()
    {
        if (!alertOption("Are you sure you want to delete this channel?"))
            return;
        
        setWorking("Deleting channel...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
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
                Channel channel = channelPanel.getSelectedChannel();
                if (channel == null)
                {
                    return null;
                }
                
                String channelId = channel.getId();
                for (int i = 0; i < status.size(); i++)
                {
                    if (status.get(i).getChannelId().equals(channelId))
                    {
                        alertWarning("You may not delete a deployed channel.\nPlease re-deploy without it enabled first.");
                        return null;
                    }
                }

                try
                {
                    mirthClient.removeChannel(channel);
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done()
            {
                doRefreshChannels();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doRefreshChannels()
    {
        setWorking("Loading channels...", true);
        
        final String channelId;

        if (channelPanel.getSelectedChannel() != null)
            channelId = channelPanel.getSelectedChannel().getId();
        else
            channelId = null;
        
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                retrieveChannels();
                return null;
            }

            public void done()
            {
                channelPanel.updateChannelTable();

                if (channels.size() > 0)
                {
                    setVisibleTasks(channelTasks, channelPopupMenu, 1, 1, true);
                    setVisibleTasks(channelTasks, channelPopupMenu, 4, 4, true);
                }
                else
                {
                    setVisibleTasks(channelTasks, channelPopupMenu, 1, 1, false);
                    setVisibleTasks(channelTasks, channelPopupMenu, 4, 4, false);
                }

                // as long as the channel was not deleted
                if (channels.containsKey(channelId))
                    channelPanel.setSelectedChannel(channelId);
                else
                    channelPanel.deselectRows();
                
                setWorking("", false);
            }
        };

        worker.execute(); 
    }

    public void retrieveChannels()
    {
        try
        {
            List<ChannelSummary> changedChannels = mirthClient.getChannelSummary(getChannelHeaders());

            if (changedChannels.size() == 0)
                return;
            else
            {
                for (int i = 0; i < changedChannels.size(); i++)
                {
                    if (changedChannels.get(i).isAdded())
                    {
                        Channel filterChannel = new Channel();
                        filterChannel.setId(changedChannels.get(i).getId());
                        Channel channelToAdd = mirthClient.getChannel(filterChannel).get(0);
                        channels.put(channelToAdd.getId(), channelToAdd);
                    }
                    else
                    {
                        Channel matchingChannel = channels.get(changedChannels.get(i).getId());

                        if (changedChannels.get(i).isDeleted())
                            channels.remove(matchingChannel.getId());
                        else
                        {
                            Channel filterChannel = new Channel();
                            filterChannel.setId(matchingChannel.getId());
                            Channel channelToUpdate = mirthClient.getChannel(filterChannel).get(0);
                            channels.put(matchingChannel.getId(), channelToUpdate);
                        }
                    }
                }
            }
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
    }

    public Map<String, Integer> getChannelHeaders()
    {
        HashMap<String, Integer> channelHeaders = new HashMap<String, Integer>();

        for (Channel channel : channels.values())
        {
            channelHeaders.put(channel.getId(), channel.getRevision());
        }

        return channelHeaders;
    }

    public void doRefreshStatuses()
    {
        setWorking("Loading statistics...", true);
               
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                refreshStatuses();
                return null;
            }

            public void done()
            {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void refreshStatuses()
    {        
        try
        {
            status = mirthClient.getChannelStatusList();
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
        
        dashboardPanel.updateTable();
        if (status.size() > 0)
            setVisibleTasks(statusTasks, statusPopupMenu, 1, 1, true);
        else
            setVisibleTasks(statusTasks, statusPopupMenu, 1, 1, false);
    }

    public void doStartAll()
    {
        setWorking("Starting all channels...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                try
                {
                    for (int i = 0; i < status.size(); i++)
                    {
                        if (status.get(i).getState() == ChannelStatus.State.STOPPED)
                            mirthClient.startChannel(status.get(i).getChannelId());
                        else if (status.get(i).getState() == ChannelStatus.State.PAUSED)
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
                doRefreshStatuses();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doStart()
    {
        if (dashboardPanel.getSelectedStatus() == -1)
            return;
        
        setWorking("Starting channel...", true);
        
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                try
                {
                    if (status.get(dashboardPanel.getSelectedStatus()).getState() == ChannelStatus.State.STOPPED)
                        mirthClient.startChannel(status.get(dashboardPanel.getSelectedStatus()).getChannelId());
                    else if (status.get(dashboardPanel.getSelectedStatus()).getState() == ChannelStatus.State.PAUSED)
                        mirthClient.resumeChannel(status.get(dashboardPanel.getSelectedStatus()).getChannelId());
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done()
            {
                doRefreshStatuses();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doStop()
    {
        if (dashboardPanel.getSelectedStatus() == -1)
            return;
        
        setWorking("Stopping channel...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                try
                {
                    mirthClient.stopChannel(status.get(dashboardPanel.getSelectedStatus()).getChannelId());
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done()
            {
                doRefreshStatuses();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doPause()
    {
        if (dashboardPanel.getSelectedStatus() == -1)
            return;
        
        setWorking("Pausing channel...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                try
                {
                    mirthClient.pauseChannel(status.get(dashboardPanel.getSelectedStatus()).getChannelId());
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done()
            {
                doRefreshStatuses();
                setWorking("", false);
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
        if (!alertOption("Are you sure you want to delete this destination?"))
            return;

        channelEditPanel.deleteDestination();
    }

    public void doCloneDestination()
    {
        int index = channelEditPanel.getSelectedDestinationIndex();
        channelEditPanel.cloneDestination(index);

    }

    public void doEnableChannel()
    {               
        final Channel channel = channelPanel.getSelectedChannel();
        if (channel == null)
        {
            alertWarning("Channel no longer exists.");
            return;
        }
        if (channelEditPanel.checkAllForms(channel))
        {
            alertWarning("Channel was not configured properly.  Please fix the problems in the forms before trying to enable it again.");
            return;
        }
        
        setWorking("Enabling channel...", true);
        
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {        
                
                channel.setEnabled(true);
                updateChannel(channel);
                return null;
            }

            public void done()
            {
                doRefreshChannels();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doDisableChannel()
    {        
        final Channel channel = channelPanel.getSelectedChannel();
        if (channel == null)
        {
            alertWarning("Channel no longer exists.");
            return;
        }
        
        setWorking("Disabling channel...", true);
        
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {   
                channel.setEnabled(false);
                updateChannel(channel);
                return null;
            }

            public void done()
            {
                doRefreshChannels();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doNewUser()
    {
        new UserWizard(null);
    }

    public void doEditUser()
    {       
        int index = userPanel.getUserIndex();

        if (index == UIConstants.ERROR_CONSTANT)
            JOptionPane.showMessageDialog(this, "User no longer exists.");
        else
        {
            new UserWizard(users.get(index));
        }
    }

    public void doDeleteUser()
    {
        if (!alertOption("Are you sure you want to delete this user?"))
            return;

        setWorking("Deleting user...", true);
               
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                if (users.size() == 1)
                {
                    alertWarning("You must have at least one user account.");
                    return null;
                }

                int userToDelete = userPanel.getUserIndex();

                try
                {
                    if (userToDelete != UIConstants.ERROR_CONSTANT)
                    {
                        if (mirthClient.isUserLoggedIn(users.get(userToDelete)))
                        {
                            alertWarning("You cannot delete a user that is currently logged in.");
                            return null;
                        }
                        
                        mirthClient.removeUser(users.get(userToDelete));
                        users = mirthClient.getUser(null);
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
                userPanel.updateUserTable();
                userPanel.deselectRows();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doRefreshUser()
    {
        setWorking("Loading users...", true);
               
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                refreshUser();
                return null;
            }
            
            public void done()
            {
                setWorking("",false);
            }
        };
        
        worker.execute();
    }
    
    public void refreshUser()
    {
        User user = null;
        String userName = null;
        int index = userPanel.getUserIndex();

        if (index != UIConstants.ERROR_CONSTANT)
            user = users.get(index);

        try
        {
            users = mirthClient.getUser(null);
            userPanel.updateUserTable();

            if (user != null)
            {
                for (int i = 0; i < users.size(); i++)
                {
                    if (user.equals(users.get(i)))
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
            userPanel.setSelectedUser(userName);
    }

    public void doDeployAll()
    {
        setWorking("Deploying channels...", true);
        dashboardPanel.deselectRows();
        
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                try
                {
                    mirthClient.deployChannels();
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done()
            {
                setWorking("", false);
                doShowDashboard();
            }
        };

        worker.execute();
    }

    public void doSaveChannel()
    {
        setWorking("Saving channel...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                if (changesHaveBeenMade() || currentContentPage == channelEditPanel.transformerPane || currentContentPage == channelEditPanel.filterPane)
                {
                    if (channelEditPanel.saveChanges())
                    {
                        channelEditTasks.getContentPane().getComponent(0).setVisible(false);
                        if (currentContentPage == channelEditPanel.transformerPane)
                        {
                            channelEditPanel.transformerPane.modified = false;
                        }
                        else if (currentContentPage == channelEditPanel.filterPane)
                        {
                            channelEditPanel.filterPane.modified = false;
                        }
                    }
                    return null;
                }
                else
                {
                    return null;
                }
            }

            public void done()
            {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public boolean changesHaveBeenMade()
    {
        if (channelEditPanel != null && currentContentPage == channelEditPanel)
            return channelEditTasks.getContentPane().getComponent(0).isVisible();
        else if (channelEditPanel != null && currentContentPage == channelEditPanel.transformerPane)
            return channelEditPanel.transformerPane.modified;
        else if (channelEditPanel != null && currentContentPage == channelEditPanel.filterPane)
            return channelEditPanel.filterPane.modified;
        else if (settingsPanel != null && currentContentPage == settingsPanel)
            return settingsTasks.getContentPane().getComponent(1).isVisible();
        else if (alertPanel != null && currentContentPage == alertPanel)
            return alertTasks.getContentPane().getComponent(1).isVisible();
        else
            return false;
    }

    public void doShowMessages()
    {        
        if (messageBrowser == null)
            messageBrowser = new MessageBrowser();

        if (dashboardPanel.getSelectedStatus() == -1)
            return;
        
        setBold(viewPane, -1);
        setPanelName("Channel Messages - " + status.get(dashboardPanel.getSelectedStatus()).getName());
        setCurrentContentPage(messageBrowser);
        setFocus(messageTasks);
        
        messageBrowser.loadNew();
    }

    public void doShowEvents()
    {
         if (!confirmLeave())
            return;
        
        if (eventBrowser == null)
            eventBrowser = new EventBrowser();

        setBold(viewPane, 5);
        setPanelName("System Events");
        setCurrentContentPage(eventBrowser);
        setFocus(eventTasks);
        
        eventBrowser.loadNew();
    }

    public void doEditTransformer()
    {
        if (channelEditPanel.transformerPane == null)
            channelEditPanel.transformerPane = new TransformerPane();

        String name = channelEditPanel.editTransformer();
        setPanelName("Edit Channel - " + channelEditPanel.currentChannel.getName() + " - " + name + " Transformer");
    }

    public void doEditFilter()
    {
        if (channelEditPanel.filterPane == null)
            channelEditPanel.filterPane = new FilterPane();

        String name = channelEditPanel.editFilter();
        setPanelName("Edit Channel - " + channelEditPanel.currentChannel.getName() + " - " + name + " Filter");
    }

    public void doSaveSettings()
    {
        setWorking("Saving settings...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                settingsPanel.saveSettings();
                return null;
            }

            public void done()
            {
                setWorking("", false);
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
        
        File currentDir = new File(userPreferences.get("currentDirectory", ""));
        if (currentDir.exists())
            importFileChooser.setCurrentDirectory(currentDir);
        
        int returnVal = importFileChooser.showOpenDialog(this);
        File importFile = null;

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            userPreferences.put("currentDirectory", importFileChooser.getCurrentDirectory().getPath());
            importFile = importFileChooser.getSelectedFile();
            importChannel(importFile, true);
        }
    }
        
    public void importChannel(File importFile, boolean showAlerts)
    {
        String channelXML = "";

        try
        {
            channelXML = ImportConverter.convertChannel(importFile);
        }
        catch (Exception e1)
        {
            if(showAlerts)
                alertException(e1.getStackTrace(),"Invalid channel file. " + e1.getMessage());
        }

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        Channel importChannel;

        try
        {
            importChannel = (Channel) serializer.fromXML(channelXML.replaceAll("\\&\\#x0D;\\n", "\n").replaceAll("\\&\\#x0D;", "\n"));
        }
        catch (Exception e)
        {
            if(showAlerts)
                alertException(e.getStackTrace(),"Invalid channel file. " + e.getMessage());
            return;
        }

       /**
         * Checks to see if the passed in channel version is current, and
         * prompts the user if it is not.
         */
        if(showAlerts)
        {
            int option;

            option = JOptionPane.YES_OPTION;
            if (importChannel.getVersion() == null)
            {
                option = JOptionPane.showConfirmDialog(this, "The channel being imported is from an unknown version of Mirth." + "\nSome channel properties may not be the same.  Would you like to automatically convert the properties?", "Select an Option", JOptionPane.YES_NO_CANCEL_OPTION);
            }
            else if (!importChannel.getVersion().equals(PlatformUI.SERVER_VERSION))
            {
                option = JOptionPane.showConfirmDialog(this, "The channel being imported is from Mirth version " + importChannel.getVersion() + ". You are using Mirth version " + PlatformUI.SERVER_VERSION + ".\nSome channel properties may not be the same.  Would you like to automatically convert the properties?", "Select an Option", JOptionPane.YES_NO_CANCEL_OPTION);
            }

            if(option != JOptionPane.YES_OPTION)
                return;
        }

        try
        {
            importChannel.setRevision(0);
            importChannel.setId(mirthClient.getGuid());

            String channelName = importChannel.getName();
            while (!checkChannelName(channelName, importChannel.getId()))
            {
                channelName = JOptionPane.showInputDialog(this, "Please enter a new name for the channel.");
                if (channelName == null)
                    return;
            }

            importChannel.setName(channelName);

            importChannel.setVersion(mirthClient.getVersion());
            channels.put(importChannel.getId(), importChannel);
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }

        try
        {
            if(showAlerts)
            {
                editChannel(importChannel);
                channelEditTasks.getContentPane().getComponent(0).setVisible(true);
            }
            else
            {
                PropertyVerifier.checkChannelProperties(importChannel);
                PropertyVerifier.checkConnectorProperties(importChannel, channelEditPanel.transports);
                updateChannel(importChannel);
                doShowChannel();
            }
        }
        catch (Exception e)
        {
            channels.remove(importChannel.getId());
            
            if(showAlerts)
            {
                alertError("Channel had an unknown problem. Channel import aborted.");
                channelEditPanel = new ChannelSetup();
            }
            
            doShowChannel();
        }
    }

    public boolean doExport()
    {
        if (channelEditTasks.getContentPane().getComponent(0).isVisible())
        {
            if (alertOption("This channel has been modified. You must save the channel changes before you can export. Would you like to save them now?"))
            {
                if (!channelEditPanel.saveChanges())
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
            channel = channelPanel.getSelectedChannel();

        JFileChooser exportFileChooser = new JFileChooser();
        exportFileChooser.setSelectedFile(new File(channel.getName()));
        exportFileChooser.setFileFilter(new MirthFileFilter("XML"));
        
        File currentDir = new File(userPreferences.get("currentDirectory", ""));
        if (currentDir.exists())
            exportFileChooser.setCurrentDirectory(currentDir);
        
        int returnVal = exportFileChooser.showSaveDialog(this);
        File exportFile = null;

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            userPreferences.put("currentDirectory", exportFileChooser.getCurrentDirectory().getPath());
            ObjectXMLSerializer serializer = new ObjectXMLSerializer();
            String channelXML = serializer.toXML(channel);
            exportFile = exportFileChooser.getSelectedFile();

            int length = exportFile.getName().length();

            if (length < 4 || !exportFile.getName().substring(length - 4, length).equals(".xml"))
                exportFile = new File(exportFile.getAbsolutePath() + ".xml");

            if (exportFile.exists())
                if (!alertOption("This file already exists.  Would you like to overwrite it?"))
                    return false;

            try
            {
                FileUtil.write(exportFile, channelXML, false);
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
        
        File currentDir = new File(userPreferences.get("currentDirectory", ""));
        if (currentDir.exists())
            exportFileChooser.setCurrentDirectory(currentDir);
        
        int returnVal = exportFileChooser.showSaveDialog(this);
        File exportFile = null;
        File exportDirectory = null;

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            
            userPreferences.put("currentDirectory", exportFileChooser.getCurrentDirectory().getPath());
            try
            {
                exportDirectory = exportFileChooser.getSelectedFile();

                for (Channel channel : channels.values())
                {
                    ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                    String channelXML = serializer.toXML(channel);

                    exportFile = new File(exportDirectory.getAbsolutePath() + "/" + channel.getName() + ".xml");

                    if (exportFile.exists())
                        if (!alertOption("The file " + channel.getName() + ".xml already exists.  Would you like to overwrite it?"))
                            continue;

                    FileUtil.write(exportFile, channelXML, false);
                }
                alertInformation("All files were written successfully to " + exportDirectory.getPath() + ".");
            }
            catch (IOException ex)
            {
                alertError("File could not be written.");
            }
        }
    }

    public void doCloneChannel()
    {
        Channel channel = null;
        try
        {
            channel = (Channel) ObjectCloner.deepCopy(channelPanel.getSelectedChannel());
        }
        catch (ObjectClonerException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
            return;
        }

        try
        {
            channel.setRevision(0);
            channel.setId(mirthClient.getGuid());
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }

        String channelName = null;
        do
        {
            channelName = JOptionPane.showInputDialog(this, "Please enter a new name for the channel.");
            if (channelName == null)
                return;
        } while (!checkChannelName(channelName, channel.getId()));

        channel.setName(channelName);
        channels.put(channel.getId(), channel);
        
        editChannel(channel);
        channelEditTasks.getContentPane().getComponent(0).setVisible(true);
    }

    public void doRefreshMessages()
    {
        setWorking("Loading messages...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                messageBrowser.refresh();
                return null;
            }

            public void done()
            {
                setWorking("", false);
            }
        };

        worker.execute();
    }
    
    public void doSendMessage()
    {
        try
        {
            retrieveChannels();
            
            Channel channel = channels.get(status.get(dashboardPanel.getSelectedStatus()).getChannelId());
            
            if(channel == null)
            {
                alertError("Channel no longer exists!");
                return;
            }
            
            MessageObject messageObject = new MessageObject();
            messageObject.setId(mirthClient.getGuid());        
            messageObject.setServerId(PlatformUI.SERVER_ID);
            messageObject.setChannelId(channel.getId());
            messageObject.setRawDataProtocol(channel.getSourceConnector().getTransformer().getInboundProtocol());
            messageObject.setDateCreated(Calendar.getInstance());
            messageObject.setConnectorName("Source");
            messageObject.setEncrypted(Boolean.valueOf(channel.getProperties().getProperty(ChannelProperties.ENCRYPT_DATA)).booleanValue());
            messageObject.setRawData("");
            
            new EditMessageDialog(messageObject);
        } 
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
    }
    
    public void doImportMessages()
    {
        setWorking("Importing messages...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                messageBrowser.importMessages();
                return null;
            }

            public void done()
            {
                setWorking("", false);
            }
        };

        worker.execute();
    }
    
    public void doExportMessages()
    {
        setWorking("Exporting messages...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                messageBrowser.exportMessages();
                return null;
            }

            public void done()
            {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doRemoveAllMessages()
    {
        if (alertOption("Are you sure you would like to remove all messages in this channel?"))
        {
            setWorking("Removing messages...", true);

            SwingWorker worker = new SwingWorker<Void, Void>()
            {
                public Void doInBackground()
                {
                    try
                    {
                        if (dashboardPanel.getSelectedStatus() > -1)
                            mirthClient.clearMessages(status.get(dashboardPanel.getSelectedStatus()).getChannelId());
                    }
                    catch (ClientException e)
                    {
                        alertException(e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done()
                {
                    if(currentContentPage == dashboardPanel)
                        doRefreshStatuses();
                    else if(currentContentPage == messageBrowser)
                        messageBrowser.refresh();
                    setWorking("", false);
                }
            };

            worker.execute();
        }
    }
    
    public void doClearStats()
    {
        int selectedStatus = dashboardPanel.getSelectedStatus();
        
        if(selectedStatus != -1)
            new DeleteStatisticsDialog(selectedStatus);
        else
            dashboardPanel.deselectRows();
    }
    
    public void clearStats(final int statusToClear, final boolean deleteReceived, final boolean deleteFiltered, final boolean deleteQueued, final boolean deleteSent, final boolean deleteErrored)
    {
        setWorking("Clearing statistics...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                try
                {
                    mirthClient.clearStatistics(status.get(statusToClear).getChannelId(), deleteReceived, deleteFiltered, deleteQueued, deleteSent, deleteErrored);
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done()
            {
                doRefreshStatuses();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doRemoveFilteredMessages()
    {
        if (alertOption("Are you sure you would like to remove all currently filtered messages in this channel?"))
        {
            setWorking("Removing messages...", true);

            SwingWorker worker = new SwingWorker<Void, Void>()
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
                    if(currentContentPage == dashboardPanel)
                        doRefreshStatuses();
                    else if(currentContentPage == messageBrowser)
                        messageBrowser.refresh();
                    setWorking("", false);
                }
            };

            worker.execute();
        }
    }

    public void doRemoveMessage()
    {
        if (alertOption("Are you sure you would like to remove the selected message?"))
        {
            setWorking("Removing message...", true);

            SwingWorker worker = new SwingWorker<Void, Void>()
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
                    if(currentContentPage == dashboardPanel)
                        doRefreshStatuses();
                    else if(currentContentPage == messageBrowser)
                        messageBrowser.refresh();
                    setWorking("", false);
                }
            };

            worker.execute();
        }
    }

    public void doReprocessFilteredMessages()
    {
        setWorking("Reprocessing messages...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
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
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doReprocessMessage()
    {
        setWorking("Reprocessing message...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
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
                setWorking("", false);
            }
        };

        worker.execute();
    }
    
   public void processMessage(final MessageObject message)
   {
        setWorking("Processing message...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                try
                {
                    mirthClient.processMessage(message);
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
                setWorking("", false);
            }
        };

        worker.execute();
   }

    public void doRefreshEvents()
    {
        eventBrowser.refresh();
    }

    public void doClearEvents()
    {
        if (alertOption("Are you sure you would like to clear all system events?"))
        {
            setWorking("Clearing events...", true);

            SwingWorker worker = new SwingWorker<Void, Void>()
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
                    setWorking("", false);
                }
            };

            worker.execute();
        }
    }

    public void doRefreshSettings()
    {
        if(changesHaveBeenMade())
        {
            if(!alertOption("Are you sure you would like to reload the settings from the server and lose your changes?"))
                return;
            else
                disableSave();
        }
        
        setWorking("Loading settings...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                settingsPanel.loadSettings();
                return null;
            }

            public void done()
            {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doRefreshAlerts()
    {
        setWorking("Loading alerts...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                refreshAlerts();
                return null;
            }

            public void done()
            {
                alertPanel.updateAlertTable(false);
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void refreshAlerts()
    {
        try
        {
            alerts = mirthClient.getAlert(null);
        }
        catch (ClientException e)
        {
            alertException(e.getStackTrace(), e.getMessage());
        }
    }

    public void doSaveAlerts()
    {
        setWorking("Saving alerts...", true);
        
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                try
                {
                    Properties serverProperties = mirthClient.getServerProperties();
                    if(!(serverProperties.getProperty("smtp.host") != null && ((String)serverProperties.getProperty("smtp.host")).length() > 0) || !(serverProperties.getProperty("smtp.port") != null && ((String)serverProperties.getProperty("smtp.port")).length() > 0))
                        alertWarning("The SMTP server on the settings page is not specified or is incomplete.  An SMTP server is required to send alerts.");

                    alertPanel.saveAlert();
                    mirthClient.updateAlerts(alerts);
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done()
            {
                disableSave();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doDeleteAlert()
    {
        alertPanel.deleteAlert();
    }

    public void doNewAlert()
    {
        alertPanel.addAlert();
    }

    public void doEnableAlert()
    {
        alertPanel.enableAlert();
    }

    public void doDisableAlert()
    {
        alertPanel.disableAlert();
    }

    public boolean exportChannelOnError()
    {
        if (channelEditPanel != null && (channelEditTasks.getContentPane().getComponent(0).isVisible() || (channelEditPanel.transformerPane != null && channelEditPanel.transformerPane.modified) || (channelEditPanel.filterPane != null && channelEditPanel.filterPane.modified)))
        {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the channel changes locally to your computer?");
            if (option == JOptionPane.YES_OPTION)
            {
                if (!channelEditPanel.saveChanges())
                    return false;

                boolean visible = channelEditTasks.getContentPane().getComponent(0).isVisible();
                channelEditTasks.getContentPane().getComponent(0).setVisible(false);
                if (!doExport())
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
        if (currentContentPage == channelEditPanel)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.CHANNEL_HELP_LOCATION);
        else if (currentContentPage == channelPanel)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.CHANNELS_HELP_LOCATION);
        else if (currentContentPage == dashboardPanel)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.DASHBOARD_HELP_LOCATION);
        else if (currentContentPage == messageBrowser)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.MESSAGE_BROWSER_HELP_LOCATION);
        else if (currentContentPage == eventBrowser)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.SYSTEM_EVENT_HELP_LOCATION);
        else if (currentContentPage == settingsPanel)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.CONFIGURATION_HELP_LOCATION);
        else if (currentContentPage == channelEditPanel.transformerPane)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.TRANFORMER_HELP_LOCATION);
        else if (currentContentPage == channelEditPanel.filterPane)
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION + UIConstants.FILTER_HELP_LOCATION);
        else
            BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION);
    }

    // ast: class for encoding information
    /*
     * class CharsetEncodingInformation gets all the information we need for an
     * encoding class
     */
    public class CharsetEncodingInformation
    {
        protected String canonicalName = "";

        protected String description = "";

        public CharsetEncodingInformation(String name, String descp)
        {
            this.canonicalName = name;
            this.description = descp;
        }

        public CharsetEncodingInformation(String name)
        {
            this.canonicalName = name;
            this.description = "";
        }

        /**
         * Overloaded method to show the description in the combo box
         */
        public String toString()
        {
            return new String(this.description);
        }

        /**
         * Overloaded method to show the description in the combo box
         */
        public boolean equals(Object obj)
        {
            if (obj instanceof String)
            {
                return canonicalName.equalsIgnoreCase((String) obj);
            }
            else if (obj instanceof CharsetEncodingInformation)
            {
                return canonicalName.equalsIgnoreCase(((CharsetEncodingInformation) obj).getCanonicalName());
            }
            else
            {
                return this.equals(obj);
            }
        }

        public String getCanonicalName()
        {
            return this.canonicalName;
        }

        public void setCanonicalName(String c)
        {
            this.canonicalName = c;
        }

        public String getDescription()
        {
            return this.description;
        }

        public void setDescription(String d)
        {
            this.description = d;
        }
    }

    public void setHighlighters()
    {

        if (currentContentPage == channelEditPanel.filterPane)
        {
            channelEditPanel.filterPane.setHighlighters();
            this.highlightersSet = true;
        }
        else if (currentContentPage == channelEditPanel.transformerPane)
        {
            channelEditPanel.transformerPane.setHighlighters();
            this.highlightersSet = true;
        }
    }

    public void unsetHighlighters()
    {
        if (currentContentPage == channelEditPanel.filterPane)
            channelEditPanel.filterPane.unsetHighlighters();
        else if (currentContentPage == channelEditPanel.transformerPane)
            channelEditPanel.transformerPane.unsetHighlighters();
        this.highlightersSet = false;
    }
}

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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
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
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;
import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.action.ActionFactory;
import org.jdesktop.swingx.action.ActionManager;
import org.jdesktop.swingx.action.BoundAction;
import org.syntax.jedit.JEditTextArea;

import com.webreach.mirth.client.core.Client;
import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.core.IgnoredComponent;
import com.webreach.mirth.client.core.UpdateClient;
import com.webreach.mirth.client.ui.browsers.event.EventBrowser;
import com.webreach.mirth.client.ui.browsers.message.MessageBrowser;
import com.webreach.mirth.client.ui.panels.reference.ReferenceListFactory;
import com.webreach.mirth.client.ui.util.FileUtil;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.model.Alert;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelProperties;
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.ChannelSummary;
import com.webreach.mirth.model.CodeTemplate;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.UpdateInfo;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.CodeTemplate.CodeSnippetType;
import com.webreach.mirth.model.Connector.Mode;
import com.webreach.mirth.model.converters.ObjectCloner;
import com.webreach.mirth.model.converters.ObjectClonerException;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.model.filters.SystemEventFilter;
import com.webreach.mirth.model.util.ImportConverter;
import com.webreach.mirth.plugins.DashboardColumnPlugin;
import com.webreach.mirth.plugins.DashboardPanelPlugin;
import com.webreach.mirth.plugins.extensionmanager.ExtensionManagerClient;
import com.webreach.mirth.plugins.extensionmanager.ExtensionUpdateDialog;
import com.webreach.mirth.util.PropertyVerifier;

/**
 * The main content frame for the Mirth Client Application. Extends JXFrame and
 * sets up all content.
 */
public class Frame extends JXFrame
{
    private Logger logger = Logger.getLogger(this.getClass());
    public Client mirthClient;
    public DashboardPanel dashboardPanel = null;
    public ChannelPanel channelPanel = null;
    public SettingsPane settingsPane = null;
    public UserPanel userPanel = null;
    public ChannelSetup channelEditPanel = null;
    public EventBrowser eventBrowser = null;
    public MessageBrowser messageBrowser = null;
    public AlertPanel alertPanel = null;
    public CodeTemplatePanel codeTemplatePanel = null;
    public GlobalScriptsPanel globalScriptsPanel = null;
    public PluginPanel pluginPanel = null;
    public JXTaskPaneContainer taskPaneContainer;
    public List<ChannelStatus> status = null;
    public Map<String, Channel> channels = null;
    public List<User> users = null;
    public List<Alert> alerts = null;
    public List<CodeTemplate> codeTemplates = null;
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
    public JXTaskPane codeTemplateTasks;
    public JPopupMenu codeTemplatePopupMenu;
    public JXTaskPane globalScriptsTasks;
    public JPopupMenu globalScriptsPopupMenu;
    public JXTitledPanel rightContainer;
    public ArrayList<ConnectorClass> sourceConnectors;
    public ArrayList<ConnectorClass> destinationConnectors;
    private Thread statusUpdater;
    private Border dsb;
    public static Preferences userPreferences;
    private StatusUpdater su;
    private boolean connectionError;
    private ArrayList<CharsetEncodingInformation> availableCharsetEncodings = null;
    private List<String> charsetEncodings = null;
    private boolean highlightersSet = false;
    private boolean isEditingChannel = false;
    private Stack<String> workingStack = new Stack<String>();
    public LinkedHashMap<MessageObject.Protocol, String> protocols;
    private Map<String, PluginMetaData> loadedPlugins;
    private Map<String, ConnectorMetaData> loadedConnectors;
    private UpdateClient updateClient = null;
    private boolean refreshingStatuses = false;

    public Frame()
    {
        dsb = BorderFactory.createEmptyBorder();
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
        protocols.put(MessageObject.Protocol.NCPDP, "NCPDP");
        protocols.put(MessageObject.Protocol.DICOM, "DICOM");
        protocols.put(MessageObject.Protocol.DELIMITED, "Delimited Text");

        setTitle(UIConstants.TITLE_TEXT + " - " + PlatformUI.SERVER_NAME);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setIconImage(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/mirth_32_ico.png")).getImage());
        makePaneContainer();

        connectionError = false;

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
                if (logout())
                    System.exit(0);
            }
        });
    }

    /**
     * Prepares the list of the encodings. This method is called from the Frame
     * class.
     * 
     */
    public void setCharsetEncodings()
    {
        if (this.availableCharsetEncodings != null)
            return;
        try
        {
            this.charsetEncodings = this.mirthClient.getAvaiableCharsetEncodings();
            this.availableCharsetEncodings = new ArrayList<CharsetEncodingInformation>();
            this.availableCharsetEncodings.add(new CharsetEncodingInformation(UIConstants.DEFAULT_ENCODING_OPTION, "Default"));
            for (int i = 0; i < charsetEncodings.size(); i++)
            {
                String canonical = (String) charsetEncodings.get(i);
                this.availableCharsetEncodings.add(new CharsetEncodingInformation(canonical, canonical));
            }
        }
        catch (Exception e)
        {
            alertError(this, "Error getting the charset list:\n " + e);
        }
    }

    /**
     * Creates all the items in the combo box for the connectors.
     * 
     * This method is called from each connector.
     */
    public void setupCharsetEncodingForConnector(javax.swing.JComboBox charsetEncodingCombobox)
    {
        if (this.availableCharsetEncodings == null)
        {
            this.setCharsetEncodings();
        }
        if (this.availableCharsetEncodings == null)
        {
            logger.error("Error, the are no encodings detected.");
            return;
        }
        charsetEncodingCombobox.removeAllItems();
        for (int i = 0; i < this.availableCharsetEncodings.size(); i++)
        {
            charsetEncodingCombobox.addItem(this.availableCharsetEncodings.get(i));
        }
    }

    /**
     * Sets the combobox for the string previously selected. If the server can't
     * support the encoding, the default one is selected. This method is called
     * from each connector.
     */
    public void setPreviousSelectedEncodingForConnector(javax.swing.JComboBox charsetEncodingCombobox, String selectedCharset)
    {
        if (this.availableCharsetEncodings == null)
        {
            this.setCharsetEncodings();
        }
        if (this.availableCharsetEncodings == null)
        {
            logger.error("Error, there are no encodings detected.");
            return;
        }
        if ((selectedCharset == null) || (selectedCharset.equalsIgnoreCase(UIConstants.DEFAULT_ENCODING_OPTION)))
        {
            charsetEncodingCombobox.setSelectedIndex(0);
        }
        else if (this.charsetEncodings.contains(selectedCharset))
        {
            int index = this.availableCharsetEncodings.indexOf(new CharsetEncodingInformation(selectedCharset, selectedCharset));
            if (index < 0)
            {
                logger.error("Synchronization lost in the list of the encoding characters.");
                index = 0;
            }
            charsetEncodingCombobox.setSelectedIndex(index);
        }
        else
        {
            alertInformation(this, "Sorry, the JVM of the server can't support the previously selected " + selectedCharset + " encoding. Please choose another one or install more encodings in the server.");
            charsetEncodingCombobox.setSelectedIndex(0);
        }
    }

    /**
     * Get the strings which identifies the encoding selected by the user.
     * 
     * This method is called from each connector.
     */
    public String getSelectedEncodingForConnector(javax.swing.JComboBox charsetEncodingCombobox)
    {
        try
        {
            return ((CharsetEncodingInformation) charsetEncodingCombobox.getSelectedItem()).getCanonicalName();
        }
        catch (Throwable t)
        {
            alertInformation(this, "Error " + t);
            return UIConstants.DEFAULT_ENCODING_OPTION;
        }
    }

    /**
     * Called to set up this main window frame.
     */
    public void setupFrame(Client mirthClient, LoginPanel login)
    {

        this.mirthClient = mirthClient;
        refreshCodeTemplates();
        login.setStatus("Loading plugins...");
        loadPlugins();
        login.setStatus("Loading preferences...");
        userPreferences = Preferences.systemNodeForPackage(Mirth.class);
        userPreferences.put("defaultServer", PlatformUI.SERVER_NAME);
        login.setStatus("Loading GUI components...");
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
            String version = PlatformUI.SERVER_VERSION;
    		int majorVersion = Integer.parseInt(version.split("\\.")[0]);
    		int minorVersion = Integer.parseInt(version.split("\\.")[1]);
    		int patchVersion = Integer.parseInt(version.split("\\.")[2]);
            PlatformUI.HELP_LOCATION += "v" + majorVersion + "r" + minorVersion + "p" + patchVersion + "/";
            PlatformUI.BUILD_DATE = mirthClient.getBuildDate();
        }
        catch (ClientException e)
        {
            alertError(this, "Could not get server information.");
        }

        setCurrentTaskPaneContainer(taskPaneContainer);
        login.setStatus("Loading dashboard...");
        doShowDashboard();
        login.setStatus("Loading channel editor...");
        channelEditPanel = new ChannelSetup();
        login.setStatus("Loading message browser...");
        messageBrowser = new MessageBrowser();
        su = new StatusUpdater();
        statusUpdater = new Thread(su);
        statusUpdater.start();

        // DEBUGGING THE UIDefaults:
        /*
         * UIDefaults uiDefaults = UIManager.getDefaults(); Enumeration enum1 =
         * uiDefaults.keys(); while (enum1.hasMoreElements()) { Object key =
         * enum1.nextElement(); Object val = uiDefaults.get(key);
         * if(key.toString().indexOf("ComboBox") != -1)
         * System.out.println("UIManager.put(\"" + key.toString() + "\",\"" +
         * (null != val ? val.toString() : "(null)") + "\");"); }
         */

    }

    public void loadPlugins()
    {
        try
        {
            loadedPlugins = mirthClient.getPluginMetaData();
            loadedConnectors = mirthClient.getConnectorMetaData();
        }
        catch (ClientException e)
        {
            alertException(this, e.getStackTrace(), "Unable to load plugins");
        }
        pluginPanel = new PluginPanel();
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
        JLabel mirthConnectImage = new JLabel();
        mirthConnectImage.setIcon(UIConstants.MIRTHCONNECT_LOGO_GRAY);
        mirthConnectImage.setText(" ");
        mirthConnectImage.setToolTipText(UIConstants.MIRTHCONNECT_TOOLTIP);
        mirthConnectImage.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        mirthConnectImage.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
            	BareBonesBrowserLaunch.openURL(UIConstants.MIRTHCONNECT_URL);
            }
        });
        
        ((JPanel)container.getComponent(0)).add(mirthConnectImage);

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

            if (working)
                workingStack.push(statusBar.getText());
            else if (workingStack.size() > 0)
                text = workingStack.pop();

            if (workingStack.size() > 0)
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
     * Edit global scripts
     */
    public void editGlobalScripts()
    {
        setBold(viewPane, UIConstants.ERROR_CONSTANT);
        setCurrentContentPage(globalScriptsPanel);
        setFocus(globalScriptsTasks);
        setVisibleTasks(globalScriptsTasks, globalScriptsPopupMenu, 0, 0, false);
        setPanelName("Global Scripts");
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
    private void setCurrentTaskPaneContainer(JXTaskPaneContainer container)
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
        createGlobalScriptsPane();
        createCodeTemplatePane();
        createOtherPane();
    }

    /**
     * Creates the view task pane.
     */
    private void createViewPane()
    {
        // Create View pane
        viewPane = new JXTaskPane();
        viewPane.setTitle("Mirth Connect");
        viewPane.setFocusable(false);

        addTask("doShowDashboard", "Dashboard", "Contains information about your currently deployed channels.", "D", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/status.png")), viewPane, null);
        addTask("doShowChannel", "Channels", "Contains various operations to perform on your channels.", "C", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/channel.png")), viewPane, null);
        addTask("doShowUsers", "Users", "Contains information on users.", "U", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/admin.png")), viewPane, null);
        addTask("doShowSettings", "Settings", "Contains local and system settings.", "S", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/settings.png")), viewPane, null);
        addTask("doShowAlerts", "Alerts", "Contains alert settings.", "A", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/alerts.png")), viewPane, null);
        addTask("doShowEvents", "Events", "Show the event logs for the system.", "E", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/logs.png")), viewPane, null);
        addTask("doShowPlugins", "Plugins", "Show the plugins loaded for the system.", "P", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/plugin.png")), viewPane, null);

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

        addTask("doRefreshSettings", "Refresh", "Refresh settings.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")), settingsTasks, settingsPopupMenu);
        addTask("doSaveSettings", "Save Settings", "Save settings.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png")), settingsTasks, settingsPopupMenu);

        setNonFocusable(settingsTasks);
        setVisibleTasks(settingsTasks, settingsPopupMenu, 0, 0, true);
        setVisibleTasks(settingsTasks, settingsPopupMenu, 1, 1, false);
        taskPaneContainer.add(settingsTasks);
    }

    /**
     * Creates the tempalte task pane.
     */
    private void createAlertPane()
    {
        // Create Alert Edit Tasks Pane
        alertTasks = new JXTaskPane();
        alertPopupMenu = new JPopupMenu();
        alertTasks.setTitle("Alert Tasks");
        alertTasks.setFocusable(false);

        addTask("doRefreshAlerts", "Refresh", "Refresh the list of alerts.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")), alertTasks, alertPopupMenu);
        addTask("doSaveAlerts", "Save Alerts", "Save all changes made to all alerts.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png")), alertTasks, alertPopupMenu);
        addTask("doNewAlert", "New Alert", "Create a new alert.", "N", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/alert_add.png")), alertTasks, alertPopupMenu);
        addTask("doImportAlerts", "Import Alerts", "Import list of alerts from an XML file.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/import.png")), alertTasks, alertPopupMenu);
        addTask("doExportAlerts", "Export Alerts", "Export the list of alerts to an XML file.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png")), alertTasks, alertPopupMenu);
        addTask("doDeleteAlert", "Delete Alert", "Delete the currently selected alert.", "L", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/alert_delete.png")), alertTasks, alertPopupMenu);
        addTask("doEnableAlert", "Enable Alert", "Enable the currently selected alert.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start.png")), alertTasks, alertPopupMenu);
        addTask("doDisableAlert", "Disable Alert", "Disable the currently selected alert.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stop.png")), alertTasks, alertPopupMenu);

        setVisibleTasks(alertTasks, alertPopupMenu, 0, 0, false);
        setVisibleTasks(alertTasks, alertPopupMenu, 1, 1, false);
        setVisibleTasks(alertTasks, alertPopupMenu, 2, 4, true);
        setVisibleTasks(alertTasks, alertPopupMenu, 5, 7, false);
        setNonFocusable(alertTasks);
        taskPaneContainer.add(alertTasks);
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

        addTask("doRefreshChannels", "Refresh", "Refresh the list of channels.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")), channelTasks, channelPopupMenu);
        addTask("doDeployAll", "Deploy All", "Deploy all currently enabled channels.", "A", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/deployall.png")), channelTasks, channelPopupMenu);
        addTask("doEditGlobalScripts", "Edit Global Scripts", "Edit scripts that are not channel specific.", "G", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/channel_edit.png")), channelTasks, channelPopupMenu);
        addTask("doEditCodeTemplates", "Edit Code Templates", "Create and manage templates to be used in JavaScript throughout Mirth.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/channel_edit.png")), channelTasks, channelPopupMenu);
        addTask("doNewChannel", "New Channel", "Create a new channel.", "N", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/channel_add.png")), channelTasks, channelPopupMenu);
        addTask("doImport", "Import Channel", "Import a channel from an XML file.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/import.png")), channelTasks, channelPopupMenu);
        addTask("doExportAll", "Export All Channels", "Export all of the channels to XML files.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png")), channelTasks, channelPopupMenu);
        addTask("doExport", "Export Channel", "Export the currently selected channel to an XML file.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png")), channelTasks, channelPopupMenu);
        addTask("doCloneChannel", "Clone Channel", "Clone the currently selected channel.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/clone.png")), channelTasks, channelPopupMenu);
        addTask("doEditChannel", "Edit Channel", "Edit the currently selected channel.", "I", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/channel_edit.png")), channelTasks, channelPopupMenu);
        addTask("doDeleteChannel", "Delete Channel", "Delete the currently selected channel.", "L", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/channel_delete.png")), channelTasks, channelPopupMenu);
        addTask("doEnableChannel", "Enable Channel", "Enable the currently selected channel.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start.png")), channelTasks, channelPopupMenu);
        addTask("doDisableChannel", "Disable Channel", "Disable the currently selected channel.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stop.png")), channelTasks, channelPopupMenu);

        setNonFocusable(channelTasks);
        setVisibleTasks(channelTasks, channelPopupMenu, 1, 1, false);
        setVisibleTasks(channelTasks, channelPopupMenu, 7, -1, false);
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

        addTask("doSaveChannel", "Save Changes", "Save all changes made to this channel.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png")), channelEditTasks, channelEditPopupMenu);
        addTask("doValidate", "Validate Connector", "Validate the currently visible connector.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/accept.png")), channelEditTasks, channelEditPopupMenu);
        addTask("doNewDestination", "New Destination", "Create a new destination.", "N", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/add.png")), channelEditTasks, channelEditPopupMenu);
        addTask("doDeleteDestination", "Delete Destination", "Delete the currently selected destination.", "L", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")), channelEditTasks, channelEditPopupMenu);
        addTask("doCloneDestination", "Clone Destination", "Clones the currently selected destination.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/clone.png")), channelEditTasks, channelEditPopupMenu);
        addTask("doEnableDestination", "Enable Destination", "Enable the currently selected destination.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start.png")), channelEditTasks, channelEditPopupMenu);
        addTask("doDisableDestination", "Disable Destination", "Disable the currently selected destination.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stop.png")), channelEditTasks, channelEditPopupMenu);
        addTask("doMoveDestinationUp", "Move Dest. Up", "Move the currently selected destination up.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/arrow_up.png")), channelEditTasks, channelEditPopupMenu);
        addTask("doMoveDestinationDown", "Move Dest. Down", "Move the currently selected destination down.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/arrow_down.png")), channelEditTasks, channelEditPopupMenu);
        addTask("doEditFilter", UIConstants.EDIT_FILTER, "Edit the filter for the currently selected destination.", "F", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/edit.png")), channelEditTasks, channelEditPopupMenu);
        addTask("doEditTransformer", UIConstants.EDIT_TRANSFORMER, "Edit the transformer for the currently selected destination.", "T", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/edit.png")), channelEditTasks, channelEditPopupMenu);
        addTask("doImportConnector", "Import Connector", "Import the currently displayed connector from an XML file.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/import.png")), channelEditTasks, channelEditPopupMenu);
        addTask("doExportConnector", "Export Connector", "Export the currently displayed connector to an XML file.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png")), channelEditTasks, channelEditPopupMenu);
        addTask("doExport", "Export Channel", "Export the currently selected channel to an XML file.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png")), channelEditTasks, channelEditPopupMenu);
        addTask("doValidateChannelScripts", "Validate Script", "Validate the currently viewed script.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/accept.png")), channelEditTasks, channelEditPopupMenu);

        setNonFocusable(channelEditTasks);
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 0, 14, false);
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 13, 13, true);
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

        addTask("doRefreshStatuses", "Refresh", "Refresh the list of statuses.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")), statusTasks, statusPopupMenu);
        addTask("doStartAll", "Start All Channels", "Start all channels that are currently deployed.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start1.png")), statusTasks, statusPopupMenu);
        addTask("doStopAll", "Stop All Channels", "Stop all channels that are currently deployed.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stop1.png")), statusTasks, statusPopupMenu);
        addTask("doRemoveAllMessagesAllChannels", "Reset All Channels", "Remove all messages and statistics in all channels.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")), statusTasks, statusPopupMenu);

        addTask("doSendMessage", "Send Message", "Send messages to the currently selected channel.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/email_go.png")), statusTasks, statusPopupMenu);
        addTask("doShowMessages", "View Messages", "Show the messages for the currently selected channel.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/messages2.png")), statusTasks, statusPopupMenu);
        addTask("doRemoveAllMessages", "Remove All Messages", "Remove all Messages in this channel.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/email_delete.png")), statusTasks, statusPopupMenu);
        //
        addTask("doClearStats", "Clear Statistics", "Reset the statistics for this channel.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/chart_curve_delete.png")), statusTasks, statusPopupMenu);
        // addTask("doClearStatsAllChannels","Clear Stats in All
        // Channels","Reset the statistics for all channels.","", new
        // ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/chart_curve_delete.png")),
        // statusTasks, statusPopupMenu);

        addTask("doStart", "Start Channel", "Start the currently selected channel.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start.png")), statusTasks, statusPopupMenu);
        addTask("doPause", "Pause Channel", "Pause the currently selected channel.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/pause.png")), statusTasks, statusPopupMenu);
        addTask("doStop", "Stop Channel", "Stop the currently selected channel.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stop.png")), statusTasks, statusPopupMenu);

        setNonFocusable(statusTasks);
        setVisibleTasks(statusTasks, statusPopupMenu, 4, -1, false);
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

        addTask("doRefreshEvents", "Refresh", "Refresh the list of events with the given filter.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")), eventTasks, eventPopupMenu);
        addTask("doRemoveAllEvents", "Remove All Events", "Remove all the System Events.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/table_delete.png")), eventTasks, eventPopupMenu);
        addTask("doRemoveFilteredEvents", "Remove Results", "Remove all System Events in the current search.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/table_delete.png")), eventTasks, eventPopupMenu);
        addTask("doRemoveEvent", "Remove Event", "Remove the selected Event.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")), eventTasks, eventPopupMenu);

        setNonFocusable(eventTasks);
        setVisibleTasks(eventTasks, eventPopupMenu, 3, 3, false);
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

        addTask("doRefreshMessages", "Refresh", "Refresh the list of messages with the given filter.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")), messageTasks, messagePopupMenu);
        addTask("doSendMessage", "Send Message", "Send a message to the channel.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/email_go.png")), messageTasks, messagePopupMenu);
        addTask("doImportMessages", "Import Messages", "Import messages from a file.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/import.png")), messageTasks, messagePopupMenu);
        addTask("doExportMessages", "Export Results", "Export all messages in the current search.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png")), messageTasks, messagePopupMenu);
        addTask("doRemoveAllMessages", "Remove All Messages", "Remove all messages in this channel.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/email_delete.png")), messageTasks, messagePopupMenu);
        addTask("doRemoveFilteredMessages", "Remove Results", "Remove all messages in the current search.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/email_delete.png")), messageTasks, messagePopupMenu);
        addTask("doRemoveMessage", "Remove Message", "Remove the selected Message.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")), messageTasks, messagePopupMenu);
        addTask("doReprocessFilteredMessages", "Reprocess Results", "Reprocess all messages in the current search.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/deployall.png")), messageTasks, messagePopupMenu);
        addTask("doReprocessMessage", "Reprocess Message", "Reprocess the selected message.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/deploy.png")), messageTasks, messagePopupMenu);
        addTask("viewImage", "View Attachment", "View Attachment", "View the attachment for the selected message.", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/attach.png")), messageTasks, messagePopupMenu);

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

        addTask("doRefreshUser", "Refresh", "Refresh the list of users.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")), userTasks, userPopupMenu);
        addTask("doNewUser", "New User", "Create a new user.", "N", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/user_add.png")), userTasks, userPopupMenu);
        addTask("doEditUser", "Edit User", "Edit the currently selected user.", "I", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/user_edit.png")), userTasks, userPopupMenu);
        addTask("doDeleteUser", "Delete User", "Delete the currently selected user.", "L", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/user_delete.png")), userTasks, userPopupMenu);

        setNonFocusable(userTasks);
        setVisibleTasks(userTasks, userPopupMenu, 2, -1, false);
        taskPaneContainer.add(userTasks);
    }

    /**
     * Creates the codeTemplate task pane.
     */
    private void createCodeTemplatePane()
    {
        // Create CodeTemplate Edit Tasks Pane
        codeTemplateTasks = new JXTaskPane();
        codeTemplatePopupMenu = new JPopupMenu();
        codeTemplateTasks.setTitle("CodeTemplate Tasks");
        codeTemplateTasks.setFocusable(false);

        addTask("doRefreshCodeTemplates", "Refresh", "Refresh the list of codeTemplates.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")), codeTemplateTasks, codeTemplatePopupMenu);
        addTask("doSaveCodeTemplates", "Save CodeTemplates", "Save all changes made to all codeTemplates.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png")), codeTemplateTasks, codeTemplatePopupMenu);
        addTask("doNewCodeTemplate", "New CodeTemplate", "Create a new codeTemplate.", "N", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/add.png")), codeTemplateTasks, codeTemplatePopupMenu);
        addTask("doImportCodeTemplates", "Import Code Templates", "Import list of code templates from an XML file.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/import.png")), codeTemplateTasks, codeTemplatePopupMenu);
        addTask("doExportCodeTemplates", "Export Code Templates", "Export the list of code templates to an XML file.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png")), codeTemplateTasks, codeTemplatePopupMenu);
        addTask("doDeleteCodeTemplate", "Delete CodeTemplate", "Delete the currently selected codeTemplate.", "L", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/delete.png")), codeTemplateTasks, codeTemplatePopupMenu);
        addTask("doValidateCodeTemplate", "Validate Script", "Validate the currently viewed script.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/accept.png")), codeTemplateTasks, codeTemplatePopupMenu);

        setVisibleTasks(codeTemplateTasks, codeTemplatePopupMenu, 0, 1, false);
        setVisibleTasks(codeTemplateTasks, codeTemplatePopupMenu, 2, 4, true);
        setVisibleTasks(codeTemplateTasks, codeTemplatePopupMenu, 5, 6, false);
        setNonFocusable(codeTemplateTasks);
        taskPaneContainer.add(codeTemplateTasks);
    }

    /**
     * Creates the global scripts edit task pane.
     */
    private void createGlobalScriptsPane()
    {
        // Create Alert Edit Tasks Pane
        globalScriptsTasks = new JXTaskPane();
        globalScriptsPopupMenu = new JPopupMenu();
        globalScriptsTasks.setTitle("Script Tasks");
        globalScriptsTasks.setFocusable(false);

        addTask("doSaveGlobalScripts", "Save Scripts", "Save all changes made to all scripts.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png")), globalScriptsTasks, globalScriptsPopupMenu);
        addTask("doValidateCurrentGlobalScript", "Validate Script", "Validate the currently viewed script.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/accept.png")), globalScriptsTasks, globalScriptsPopupMenu);
        addTask("doImportGlobalScripts", "Import Scripts", "Import all global scripts from an XML file.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/import.png")), globalScriptsTasks, globalScriptsPopupMenu);
        addTask("doExportGlobalScripts", "Export Scripts", "Export all global scripts to an XML file.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/export.png")), globalScriptsTasks, globalScriptsPopupMenu);

        setVisibleTasks(globalScriptsTasks, globalScriptsPopupMenu, 0, 0, false);
        setNonFocusable(globalScriptsTasks);
        taskPaneContainer.add(globalScriptsTasks);
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
        addTask("doHelp", "Help on this topic", "Open browser for help on this topic.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/help.png")), otherPane, null);
        addTask("goToAbout", "About Mirth", "View the about page for Mirth.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/about.png")), otherPane, null);
        addTask("goToMirth", "Visit mirthcorp.com", "View Mirth's homepage.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/home.png")), otherPane, null);
        addTask("doReportIssue", "Report Issue", "Visit Mirth's issue tracker.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bug.png")), otherPane, null);
        addTask("doLogout", "Logout", "Logout and return to the login screen.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/disconnect.png")), otherPane, null);
        setNonFocusable(otherPane);
        taskPaneContainer.add(otherPane);
        otherPane.setVisible(true);
    }

    public JXTaskPane getOtherPane()
    {
        return otherPane;
    }

    /**
     * Initializes the bound method call for the task pane actions and adds them
     * to the taskpane/popupmenu.
     */
    public void addTask(String callbackMethod, String displayName, String toolTip, String shortcutKey, ImageIcon icon, JXTaskPane pane, JPopupMenu menu)
    {
        BoundAction boundAction = ActionFactory.createBoundAction(callbackMethod, displayName, shortcutKey);

        if (icon != null)
            boundAction.putValue(Action.SMALL_ICON, icon);
        boundAction.putValue(Action.SHORT_DESCRIPTION, toolTip);
        boundAction.registerCallback(this, callbackMethod);

        pane.add(boundAction);
        if (menu != null)
            menu.add(boundAction);
    }

    /**
     * Alerts the user with a yes/no option with the passed in 'message'
     */
    public boolean alertOption(Component parentComponent, String message)
    {    	
        int option = JOptionPane.showConfirmDialog(getVisibleComponent(parentComponent), message, "Select an Option", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION)
            return true;
        else
            return false;
    }

    /**
     * Alerts the user with a Ok/cancel option with the passed in 'message'
     */
    public boolean alertOkCancel(Component parentComponent, String message)
    {
        int option = JOptionPane.showConfirmDialog(getVisibleComponent(parentComponent), message, "Select an Option", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION)
            return true;
        else
            return false;
    }

    /**
     * Alerts the user with an information dialog with the passed in 'message'
     */
    public void alertInformation(Component parentComponent, String message)
    {
        JOptionPane.showMessageDialog(getVisibleComponent(parentComponent), message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Alerts the user with a warning dialog with the passed in 'message'
     */
    public void alertWarning(Component parentComponent, String message)
    {
        JOptionPane.showMessageDialog(getVisibleComponent(parentComponent), message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Alerts the user with an error dialog with the passed in 'message'
     */
    public void alertError(Component parentComponent, String message)
    {
        JOptionPane.showMessageDialog(getVisibleComponent(parentComponent), message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Alerts the user with an error dialog with the passed in 'message' and a
     * 'question'.
     */
    public void alertCustomError(Component parentComponent, String message, String question)
    {
    	parentComponent = getVisibleComponent(parentComponent);
    	
        Window owner = getWindowForComponent(parentComponent);
        
    	if (owner instanceof java.awt.Frame) {
    		new CustomErrorDialog((java.awt.Frame)owner, message, question);
    	} else { // window instanceof Dialog
    		new CustomErrorDialog((java.awt.Dialog)owner, message, question);
    	}
    }

    /**
     * Alerts the user with an exception dialog with the passed in stack trace.
     */
    public void alertException(Component parentComponent, StackTraceElement[] strace, String message)
    {    	
        if (connectionError)
            return;
        
        parentComponent = getVisibleComponent(parentComponent);

        if (message != null)
        {
            if (message.indexOf("Received close_notify during handshake") != -1)
            {
                return;
            }

            if (message.indexOf("Unauthorized") != -1 || message.indexOf("reset") != -1)
            {
                connectionError = true;
                if (currentContentPage == dashboardPanel)
                    su.interruptThread();
                alertWarning(parentComponent, "Sorry your connection to Mirth has either timed out or there was an error in the connection.  Please login again.");
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
                alertWarning(parentComponent, "The Mirth server " + PlatformUI.SERVER_NAME + " is no longer running.  Please start it and login again.");
                if (!exportChannelOnError())
                    return;
                this.dispose();
                Mirth.main(new String[] { PlatformUI.SERVER_NAME, PlatformUI.CLIENT_VERSION });
                return;
            }
        }

        String stackTrace = message + "\n";
        for (int i = 0; i < strace.length; i++)
            stackTrace += strace[i].toString() + "\n";

        logger.error(stackTrace);

        Window owner = getWindowForComponent(parentComponent);
        
        if (owner instanceof java.awt.Frame) {
    		new ErrorDialog((java.awt.Frame)owner, stackTrace);
    	} else { // window instanceof Dialog
    		new ErrorDialog((java.awt.Dialog)owner, stackTrace);
    	}
    }
    
    private Component getVisibleComponent(Component component) {
    	if (component != null && component.isVisible()) {
    		return component;
    	} else if (this.isVisible()) {
    		return this;
    	} else {
    		return null;
    	}
    }
    
    private Window getWindowForComponent(Component parentComponent) {
        Window owner = null;
        
        if (parentComponent == null) {
        	owner = this;
        } else if (parentComponent instanceof java.awt.Frame || parentComponent instanceof java.awt.Dialog) {
        	owner = (Window)parentComponent;
        } else {
        	owner = SwingUtilities.windowForComponent(parentComponent);
        	
        	if (owner == null) {
        		owner = this;
        	}
        }
        
        return owner;
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
    	setFocus(new JXTaskPane[]{pane}, true, true);
    }
    
    /**
     * Sets the visible task panes to the specified 'panes'.
     * Also allows setting the 'Mirth' and 'Other' panes.
     */
    public void setFocus(JXTaskPane[] panes, boolean mirthPane, boolean otherPane)
    {
    	taskPaneContainer.getComponent(0).setVisible(mirthPane);
    	
        // ignore the first and last components
        for (int i = 1; i < taskPaneContainer.getComponentCount() - 1; i++)
        {
            taskPaneContainer.getComponent(i).setVisible(false);
        }
        
        taskPaneContainer.getComponent(taskPaneContainer.getComponentCount() - 1).setVisible(otherPane);

        if (panes != null)
        {
        	for (JXTaskPane pane : panes)
        	{
        		if (pane != null)
        			pane.setVisible(true);
        	}
        }
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
        }
        else if (settingsPane != null && currentContentPage == settingsPane && settingsTasks.getContentPane().getComponent(1).isVisible())
        {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the settings?");

            if (option == JOptionPane.YES_OPTION)
                settingsPane.getSettingsPanel().saveSettings();
            else if (option == JOptionPane.NO_OPTION)
                settingsPane.getSettingsPanel().loadSettings();
            else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION)
                return false;
        }
        else if (alertPanel != null && currentContentPage == alertPanel && alertTasks.getContentPane().getComponent(1).isVisible())
        {
        	alertPanel.stopAlertEditing();
        	alertPanel.stopEmailEditing();
        	
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the alerts?");

            if (option == JOptionPane.YES_OPTION)
                doSaveAlerts();
            else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION)
                return false;
        }
        else if (globalScriptsPanel != null && currentContentPage == globalScriptsPanel && globalScriptsTasks.getContentPane().getComponent(0).isVisible())
        {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the scripts?");

            if (option == JOptionPane.YES_OPTION)
                doSaveGlobalScripts();
            else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION)
                return false;
        }
        else if (codeTemplatePanel != null && currentContentPage == codeTemplatePanel && codeTemplateTasks.getContentPane().getComponent(1).isVisible())
        {
        	codeTemplatePanel.stopCodeTemplateEditing();
        	
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the code templates?");

            if (option == JOptionPane.YES_OPTION)
            {
                if (!saveCodeTemplates())
                    return false;
            }
            else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION)
                return false;
        }
        else if (pluginPanel != null && currentContentPage == pluginPanel && pluginPanel.getCurrentPanelPlugin() != null)
        {
        	int saveIndex = pluginPanel.getCurrentPanelPlugin().getSaveIndex();
        	
        	if (saveIndex != -1 && pluginPanel.getCurrentPanelPlugin().getTaskPane().getContentPane().getComponent(saveIndex).isVisible())
        	{
        		if (!pluginPanel.getCurrentPanelPlugin().pluginConfirmLeave())
        		{
        			return false;
        		}
        	}
        }

        disableSave();
        return true;
    }

    /**
     * Sends the channel passed in to the server, updating it or adding it.
     */
    public boolean updateChannel(Channel curr, boolean override)
    {
        try
        {
            if (!mirthClient.updateChannel(curr, override))
            {
                if (alertOption(this, "This channel has been modified since you first opened it, or you have imported\nan older version of the channel.  Would you like to overwrite it?"))
                    mirthClient.updateChannel(curr, true);
                else
                    return false;
            }
            retrieveChannels();
        }
        catch (ClientException e)
        {
            alertException(this, e.getStackTrace(), e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Sends the passed in user to the server, updating it or adding it.
     */
    public void updateUser(final Component parentComponent, final User curr, final String password)
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
                    alertException(parentComponent, e.getStackTrace(), e.getMessage());
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
    
    public void updateAndSwitchUser(Component parentComponent, final User curr, String newUsername, String newPassword)
    {
    	setWorking("Saving user...", true);
    	
    	try
    	{
            mirthClient.updateUser(curr, newPassword);
            users = mirthClient.getUser(null);
        }
    	catch (ClientException e)
    	{
            alertException(parentComponent, e.getStackTrace(), e.getMessage());
        }
    	finally
    	{
        	// The userPanel will be null if the user panel has not been viewed (i.e. registration).
        	if (userPanel != null)
        		userPanel.updateUserTable();
        	
            setWorking("", false);
        }
    	
    	setWorking("Switching User...", true);
    	
    	try
    	{
    		mirthClient.logout();
    		mirthClient.login(newUsername, newPassword, PlatformUI.CLIENT_VERSION);
    		PlatformUI.USER_NAME = newUsername;
    		updateClient = null; // Reset the update client so it uses the new user next time it is called.
    	}
    	catch (ClientException e)
    	{
    		alertException(parentComponent, e.getStackTrace(), e.getMessage());
    	}
    	finally
    	{
    		setWorking("", false);
    	}
    }
    
    public User getCurrentUser(Component parentComponent) {
		User currentUser = null;
		
        try {
			users = mirthClient.getUser(null);
			for (User user : users) {
				if (user.getUsername().equals(PlatformUI.USER_NAME)) {
					currentUser = user;
				}
			}
		} catch (ClientException e) {
			alertException(parentComponent, e.getStackTrace(), e.getMessage());
		}
		
		return currentUser;
    }
    
    public UpdateClient getUpdateClient(Component parentComponent) {
       	if (updateClient == null) {
    		User currentUser = PlatformUI.MIRTH_FRAME.getCurrentUser(parentComponent);
            updateClient = PlatformUI.MIRTH_FRAME.mirthClient.getUpdateClient(currentUser);
    	}
       	
    	return updateClient;
    }
    
    public void registerUser(final User user) {
    	setWorking("Registering user...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
    	    	try {
    				getUpdateClient(PlatformUI.MIRTH_FRAME).registerUser(user);
    			} catch (ClientException e) {
    				// ignore errors connecting to update/stats server
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
    
    public void checkForUpdates() {
    	setWorking("Checking for updates...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
            	Properties serverProperties = null;
            	try {
        			serverProperties = mirthClient.getServerProperties();
        		} catch (ClientException e) {
        			alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
        		}
        		
        		// Only check if updates are enabled
            	if ((serverProperties != null) && (serverProperties.getProperty("update.enabled") != null) && serverProperties.getProperty("update.enabled").equals(UIConstants.YES_OPTION)) {
        	    	try {
        				List<UpdateInfo> updateInfoList = getUpdateClient(PlatformUI.MIRTH_FRAME).getUpdates();
        				
        				boolean newUpdates = false;
        				boolean extensionManager = mirthClient.isExtensionEnabled(PluginPanel.EXTENSION_MANAGER);
        				
        				String serverUrl = "";
        				String serverName = "";
        				String serverVersion = "";
        				for (UpdateInfo updateInfo : updateInfoList) {
        					// If the extension manager exists or it's a server update, set to true...
        					// as long as the update is not ignored and not optional.
    						if ((extensionManager || updateInfo.getType().equals(UpdateInfo.Type.SERVER)) && !updateInfo.isIgnored() && !updateInfo.isOptional()) {
    							newUpdates = true;
    							
    							if (updateInfo.getType().equals(UpdateInfo.Type.SERVER)) {
    								serverUrl = updateInfo.getUri();
    								serverName = updateInfo.getName();
    								serverVersion = updateInfo.getVersion();
    							}
    						}
    					}
        				
        				if (newUpdates) {
        					if (extensionManager) {
        						new ExtensionUpdateDialog((ExtensionManagerClient)pluginPanel.loadedPlugins.get(PluginPanel.EXTENSION_MANAGER), updateInfoList);
        					} else {
        						String[] options = {"Download", "Ignore", "Remind Me Later"};
        						String question = "A Mirth update is available. You can download it now from:\n" + serverUrl;
        						int result = JOptionPane.showOptionDialog(PlatformUI.MIRTH_FRAME, question, "Server Update", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        						
        						if (result == 0) {
                    				BareBonesBrowserLaunch.openURL(serverUrl);
                    			} else if (result == 1) {
									try {
										List<IgnoredComponent> ignoredComponents = getUpdateClient(PlatformUI.MIRTH_FRAME).getIgnoredComponents();
										ignoredComponents.add(new IgnoredComponent(serverName, serverVersion));
										getUpdateClient(PlatformUI.MIRTH_FRAME).setIgnoredComponents(ignoredComponents);
                    				} catch (ClientException e) {
                    					alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                    				}
                    			}
        					}
        				}
        			} catch (ClientException e) {
        				// ignore errors connecting to update/stats server
        			}
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
    
    public void sendUsageStatistics() {
    	setWorking("Sending usage statistics...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
            	Properties serverProperties = null;
            	try {
        			serverProperties = mirthClient.getServerProperties();
        		} catch (ClientException e) {
        			alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
        		}
        		
            	if ((serverProperties != null) && (serverProperties.getProperty("stats.enabled") != null) && serverProperties.getProperty("stats.enabled").equals(UIConstants.YES_OPTION)) {
        	    	try {
        				getUpdateClient(PlatformUI.MIRTH_FRAME).sendUsageStatistics();
        			} catch (ClientException e) {
        				// ignore errors connecting to update/stats server
        			}
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

    /**
     * Checks to see if the passed in channel id already exists
     */
    public boolean checkChannelId(String id)
    {
        for (Channel channel : channels.values())
        {
            if (channel.getId().equalsIgnoreCase(id))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks to see if the passed in channel name already exists
     */
    public boolean checkChannelName(String name, String id)
    {
        if (name.equals(""))
        {
            alertWarning(this, "Channel name cannot be empty.");
            return false;
        }

        if (name.length() > 40)
        {
            alertWarning(this, "Channel name cannot be longer than 40 characters.");
            return false;
        }

        // Following code copied from MirthFieldConstaints, must be the same to
        // check for valid channel names the same way.
        char[] chars = name.toCharArray();
        for (char c : chars)
        {
            int cVal = (int) c;
            if ((cVal < 65 || cVal > 90) && (cVal < 97 || cVal > 122) && (cVal != 32) && (cVal != 45) && (cVal != 95))
            {
                try
                {
                    if (Double.isNaN(Double.parseDouble(c + "")))
                    {
                        alertWarning(this, "Channel name cannot have special characters besides hyphen, underscore, and space.");
                        return false;
                    }
                }
                catch (Exception e)
                {
                    alertWarning(this, "Channel name cannot have special characters besides hyphen, underscore, and space.");
                    return false;
                }
            }
        }

        for (Channel channel : channels.values())
        {
            if (channel.getName().equalsIgnoreCase(name) && !channel.getId().equals(id))
            {
                alertWarning(this, "Channel \"" + name + "\" already exists.");
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
        else if (settingsPane != null && currentContentPage == settingsPane)
            settingsTasks.getContentPane().getComponent(1).setVisible(true);
        else if (alertPanel != null && currentContentPage == alertPanel)
            alertTasks.getContentPane().getComponent(1).setVisible(true);
        else if (globalScriptsPanel != null && currentContentPage == globalScriptsPanel)
            globalScriptsTasks.getContentPane().getComponent(0).setVisible(true);
        else if (codeTemplatePanel != null && currentContentPage == codeTemplatePanel)
            codeTemplateTasks.getContentPane().getComponent(1).setVisible(true);
        else if (pluginPanel != null && currentContentPage == pluginPanel && pluginPanel.getCurrentPanelPlugin() != null)
        {
        	int saveIndex = pluginPanel.getCurrentPanelPlugin().getSaveIndex();
        	
        	if (saveIndex != -1)
        		pluginPanel.getCurrentPanelPlugin().getTaskPane().getContentPane().getComponent(saveIndex).setVisible(true);
        }
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
        else if (currentContentPage == settingsPane)
            settingsTasks.getContentPane().getComponent(1).setVisible(false);
        else if (alertPanel != null && currentContentPage == alertPanel)
            alertTasks.getContentPane().getComponent(1).setVisible(false);
        else if (globalScriptsPanel != null && currentContentPage == globalScriptsPanel)
            globalScriptsTasks.getContentPane().getComponent(0).setVisible(false);
        else if (codeTemplatePanel != null && currentContentPage == codeTemplatePanel)
            codeTemplateTasks.getContentPane().getComponent(1).setVisible(false);
        else if (pluginPanel != null && currentContentPage == pluginPanel && pluginPanel.getCurrentPanelPlugin() != null)
        {
        	int saveIndex = pluginPanel.getCurrentPanelPlugin().getSaveIndex();
        	
        	if (saveIndex != -1)
        		pluginPanel.getCurrentPanelPlugin().getTaskPane().getContentPane().getComponent(saveIndex).setVisible(false);
        }
    }

    // ////////////////////////////////////////////////////////////
    // --- All bound actions are beneath this point --- //
    // ////////////////////////////////////////////////////////////

    public void goToMirth()
    {
        BareBonesBrowserLaunch.openURL("http://www.mirthcorp.com/");
    }

    public void goToAbout()
    {
        new AboutMirth();
    }
    
    public void doReportIssue()
    {
    	BareBonesBrowserLaunch.openURL(UIConstants.ISSUE_TRACKER_LOCATION);
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
        if (settingsPane == null)
            settingsPane = new SettingsPane();

        if (!confirmLeave())
            return;

        setWorking("Loading settings...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                settingsPane.getSettingsPanel().loadSettings();
                return null;
            }

            public void done()
            {
                setBold(viewPane, 3);
                setPanelName("Settings");
                setCurrentContentPage(settingsPane);
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
        logout();
    }

    public boolean logout()
    {
        if (!confirmLeave())
            return false;

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
            alertException(this, e.getStackTrace(), e.getMessage());
        }

        return true;
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
        if (sourceConnectors.size() == 0 || destinationConnectors.size() == 0)
        {
            alertError(this, "You must have at least one source connector and one destination connector installed.");
            return;
        }

        // The channel wizard will call createNewChannel() or create a channel
        // from a wizard.
        new ChannelWizard();
    }

    public void createNewChannel()
    {
        Channel channel = new Channel();

        try
        {
            channel.setId(mirthClient.getGuid());
        }
        catch (ClientException e)
        {
            alertException(this, e.getStackTrace(), e.getMessage());
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
                Channel channel = channelPanel.getSelectedChannel();

                if (checkInstalledConnectors(channel))
                    editChannel((Channel) ObjectCloner.deepCopy(channel));
            }
            catch (ObjectClonerException e)
            {
                alertException(this, e.getStackTrace(), e.getMessage());
            }
        }
        isEditingChannel = false;
    }

    public boolean checkInstalledConnectors(Channel channel)
    {
        Connector source = channel.getSourceConnector();
        List<Connector> destinations = channel.getDestinationConnectors();
        ArrayList<String> missingConnectors = new ArrayList<String>();

        boolean missingSourceConnector = true, missingDestinationConnector;
        boolean allDesintionConnectorsFound = true;

        for (int i = 0; i < sourceConnectors.size(); i++)
        {
            if (source.getTransportName().equals(sourceConnectors.get(i).getName()))
                missingSourceConnector = false;
        }

        if (missingSourceConnector)
            missingConnectors.add(source.getTransportName());

        for (int i = 0; i < destinations.size(); i++)
        {
            missingDestinationConnector = true;

            for (int j = 0; j < destinationConnectors.size(); j++)
            {
                if (destinations.get(i).getTransportName().equals(destinationConnectors.get(j).getName()))
                {
                    missingDestinationConnector = false;
                }
            }

            if (missingDestinationConnector)
            {
                missingConnectors.add(destinations.get(i).getTransportName());
                allDesintionConnectorsFound = false;
            }
        }

        if (missingSourceConnector || !allDesintionConnectorsFound)
        {
            String errorText = "Your Mirth installation is missing required connectors for this channel:\n";
            for (String s : missingConnectors)
                errorText += s + "\n";
            alertError(this, errorText);
            return false;
        }
        else
        {
            return true;
        }
    }

    public void doEditGlobalScripts()
    {
        if (globalScriptsPanel == null)
            globalScriptsPanel = new GlobalScriptsPanel();

        setWorking("Loading global scripts...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                globalScriptsPanel.edit();
                return null;
            }

            public void done()
            {
                editGlobalScripts();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doEditCodeTemplates()
    {

        if (codeTemplatePanel == null)
            codeTemplatePanel = new CodeTemplatePanel();

        setWorking("Loading code templates...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                refreshCodeTemplates();
                return null;
            }

            public void done()
            {
                codeTemplatePanel.updateCodeTemplateTable(false);
                setBold(viewPane, UIConstants.ERROR_CONSTANT);
                setPanelName("Code Templates");
                setCurrentContentPage(codeTemplatePanel);
                codeTemplatePanel.setDefaultCodeTemplate();
                setFocus(codeTemplateTasks);
                disableSave();
                setWorking("", false);
            }
        };

        worker.execute();

    }

    public void doValidateCurrentGlobalScript()
    {
        globalScriptsPanel.validateCurrentScript();
    }

    public void doImportGlobalScripts()
    {
    	File importFile = importFile("XML");
    	
    	if (importFile != null)
    	{
            try
            {
                String scriptsXML = FileUtil.read(importFile);

                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                Map<String, String> importScripts = (Map<String, String>) serializer.fromXML(scriptsXML);

                globalScriptsPanel.importAllScripts(importScripts);
            }
            catch (Exception e)
            {
                alertException(this, e.getStackTrace(), "Invalid scripts file. " + e.getMessage());
                return;
            }
    	}
    }

    public void doExportGlobalScripts()
    {
        if (changesHaveBeenMade())
        {
            if (alertOption(this, "You must save your global scripts before exporting.  Would you like to save them now?"))
            {
                globalScriptsPanel.save();
                disableSave();
            }
            else
                return;
        }
        
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        String globalScriptsXML = serializer.toXML(globalScriptsPanel.exportAllScripts());
        
        exportFile(globalScriptsXML, null, "XML", "Global Scripts export");
    }

    public void doValidateChannelScripts()
    {
        channelEditPanel.validateScripts();
    }

    public void doSaveGlobalScripts()
    {
        setWorking("Saving global scripts...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                globalScriptsPanel.save();
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

    public void doDeleteChannel()
    {
        if (!alertOption(this, "Are you sure you want to delete this channel?"))
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
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
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
                        alertWarning(PlatformUI.MIRTH_FRAME, "You may not delete a deployed channel.\nPlease re-deploy without it enabled first.");
                        return null;
                    }
                }

                try
                {
                    mirthClient.removeChannel(channel);
                }
                catch (ClientException e)
                {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
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
                    setVisibleTasks(channelTasks, channelPopupMenu, 6, 6, true);
                }
                else
                {
                    setVisibleTasks(channelTasks, channelPopupMenu, 1, 1, false);
                    setVisibleTasks(channelTasks, channelPopupMenu, 6, 6, false);
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
            alertException(this, e.getStackTrace(), e.getMessage());
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
    
    public void setRefreshingStatuses(boolean refreshingStatuses) {
    	synchronized(this)
    	{
    		this.refreshingStatuses = refreshingStatuses;
    	}
    }
    
    public boolean isRefreshingStatuses() {
    	synchronized(this)
    	{
    		return refreshingStatuses;
    	}
    }
    
    public void doRefreshStatuses()
    {
    	// Don't allow anything to be getting or setting refreshingStatuses
    	// while this block is being executed.
    	synchronized(this)
    	{
	    	if (isRefreshingStatuses())
	    		return;
	    	
	    	setRefreshingStatuses(true);
    	}
        setWorking("Loading statistics...", true);

        // moving SwingWorker into the refreshStatuses() method...
        // ArrayIndexOutOfBound exception occurs due to updateTable method on
        // the UI executed concurrently on multiple threads in the background.
        // and they share a global 'parent.status' variable that changes its
        // state between threads.
        // updateTable() method should be called in done(), not in the
        // background only when the 'status' object is done assessed in the
        // background.
    	
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            Object[][] tableData = null;

            public Void doInBackground()
            {
                try
                {
                    status = mirthClient.getChannelStatusList();

                    if (status != null)
                    {
                        Map<String, DashboardColumnPlugin> loadedColumnPluginsBeforeStatus = dashboardPanel.getLoadedColumnPluginsBeforeStatus();
                        Map<String, DashboardColumnPlugin> loadedColumnPluginsAfterStats = dashboardPanel.getLoadedColumnPluginsAfterStats();

                        for (DashboardColumnPlugin plugin : loadedColumnPluginsBeforeStatus.values())
                        {
                            plugin.tableUpdate(status);
                        }
                        for (DashboardColumnPlugin plugin : loadedColumnPluginsAfterStats.values())
                        {
                            plugin.tableUpdate(status);
                        }

                        tableData = new Object[status.size()][8 + loadedColumnPluginsAfterStats.size() + loadedColumnPluginsBeforeStatus.size()];
                        for (int i = 0; i < status.size(); i++)
                        {
                            ChannelStatus tempStatus = status.get(i);
                            int statusColumn = 0;
                            try
                            {
                                ChannelStatistics tempStats = mirthClient.getStatistics(tempStatus.getChannelId());
                                int j = 0;
                                for (DashboardColumnPlugin plugin : loadedColumnPluginsBeforeStatus.values())
                                {
                                    tableData[i][j] = plugin.getTableData(tempStatus);
                                    j++;
                                }
                                statusColumn = j;
                                j += 2;
                                tableData[i][j] = tempStats.getReceived();
                                tableData[i][++j] = tempStats.getFiltered();
                                tableData[i][++j] = tempStats.getQueued();
                                tableData[i][++j] = tempStats.getSent();
                                tableData[i][++j] = tempStats.getError();
                                tableData[i][++j] = tempStats.getAlerted();
                                j++;
                                for (DashboardColumnPlugin plugin : loadedColumnPluginsAfterStats.values())
                                {
                                    tableData[i][j] = plugin.getTableData(tempStatus);
                                    j++;
                                }
                            }
                            catch (ClientException ex)
                            {
                                alertException(PlatformUI.MIRTH_FRAME, ex.getStackTrace(), ex.getMessage());
                            }

                            if (tempStatus.getState() == ChannelStatus.State.STARTED)
                                tableData[i][statusColumn] = new CellData(UIConstants.GREEN_BULLET, "Started");
                            else if (tempStatus.getState() == ChannelStatus.State.STOPPED)
                                tableData[i][statusColumn] = new CellData(UIConstants.RED_BULLET, "Stopped");
                            else if (tempStatus.getState() == ChannelStatus.State.PAUSED)
                                tableData[i][statusColumn] = new CellData(UIConstants.YELLOW_BULLET, "Paused");

                            tableData[i][statusColumn + 1] = tempStatus.getName();
                        }
                    }
                }
                catch (ClientException e)
                {
                	status = null;
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                }
                
                return null;
            }

            public void done()
            {
                setWorking("", false);
                if (status != null) {
	                dashboardPanel.updateTable(tableData);
	                dashboardPanel.updateCurrentPluginPanel();
	                if (status.size() > 0)
	                    setVisibleTasks(statusTasks, statusPopupMenu, 1, 3, true);
	                else
	                    setVisibleTasks(statusTasks, statusPopupMenu, 1, 3, false);
                }
                setRefreshingStatuses(false);
            }
        };
        worker.execute();
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
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
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

    public void doStopAll()
    {
        setWorking("Stopping all channels...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                try
                {
                    for (int i = 0; i < status.size(); i++)
                    {
                        if (status.get(i).getState() == ChannelStatus.State.STARTED || status.get(i).getState() == ChannelStatus.State.PAUSED)
                            mirthClient.stopChannel(status.get(i).getChannelId());
                    }
                }
                catch (ClientException e)
                {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
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
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
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
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
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
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
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
        if (!alertOption(this, "Are you sure you want to delete this destination?"))
            return;

        channelEditPanel.deleteDestination();
    }

    public void doCloneDestination()
    {
        channelEditPanel.cloneDestination();
    }

    public void doEnableDestination()
    {
        channelEditPanel.enableDestination();
    }

    public void doDisableDestination()
    {
        channelEditPanel.disableDestination();
    }

    public void doEnableChannel()
    {
        final Channel channel = channelPanel.getSelectedChannel();
        if (channel == null)
        {
            alertWarning(this, "Channel no longer exists.");
            return;
        }

        String validationMessage = channelEditPanel.checkAllForms(channel);
        if (validationMessage != null)
        {
            alertCustomError(this, validationMessage, CustomErrorDialog.ERROR_ENABLING_CHANNEL);
            return;
        }

        setWorking("Enabling channel...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {

                channel.setEnabled(true);
                updateChannel(channel, false);
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
            alertWarning(this, "Channel no longer exists.");
            return;
        }

        setWorking("Disabling channel...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                channel.setEnabled(false);
                updateChannel(channel, false);
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
        new UserDialog(null);
    }

    public void doEditUser()
    {
        int index = userPanel.getUserIndex();

        if (index == UIConstants.ERROR_CONSTANT)
            alertWarning(this, "User no longer exists.");
        else
        {
            new UserDialog(users.get(index));
        }
    }

    public void doDeleteUser()
    {
        if (!alertOption(this, "Are you sure you want to delete this user?"))
            return;

        setWorking("Deleting user...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                if (users.size() == 1)
                {
                    alertWarning(PlatformUI.MIRTH_FRAME, "You must have at least one user account.");
                    return null;
                }

                int userToDelete = userPanel.getUserIndex();

                try
                {
                    if (userToDelete != UIConstants.ERROR_CONSTANT)
                    {
                        if (mirthClient.isUserLoggedIn(users.get(userToDelete)))
                        {
                            alertWarning(PlatformUI.MIRTH_FRAME, "You cannot delete a user that is currently logged in.");
                            return null;
                        }

                        mirthClient.removeUser(users.get(userToDelete));
                        users = mirthClient.getUser(null);
                    }
                }
                catch (ClientException e)
                {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
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
                setWorking("", false);
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
            alertException(this, e.getStackTrace(), e.getMessage());
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
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
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
                        disableSave();
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
        else if (settingsPane != null && currentContentPage == settingsPane)
            return settingsTasks.getContentPane().getComponent(1).isVisible();
        else if (alertPanel != null && currentContentPage == alertPanel)
            return alertTasks.getContentPane().getComponent(1).isVisible();
        else if (globalScriptsPanel != null && currentContentPage == globalScriptsPanel)
            return globalScriptsTasks.getContentPane().getComponent(0).isVisible();
        else if (codeTemplatePanel != null && currentContentPage == codeTemplatePanel)
            return codeTemplateTasks.getContentPane().getComponent(1).isVisible();
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
        channelEditPanel.transformerPane.resizePanes();
        String name = channelEditPanel.editTransformer();
        setPanelName("Edit Channel - " + channelEditPanel.currentChannel.getName() + " - " + name + " Transformer");
    }

    public void doEditFilter()
    {
        channelEditPanel.filterPane.resizePanes();
        String name = channelEditPanel.editFilter();
        setPanelName("Edit Channel - " + channelEditPanel.currentChannel.getName() + " - " + name + " Filter");
    }
    
    public void updateFilterTaskName(int rules) {
    	updateFilterOrTransformerTaskName(UIConstants.EDIT_FILTER, UIConstants.EDIT_FILTER_TASK_NUMBER, rules);
    }
    
    public void updateTransformerTaskName(int steps) {
    	updateFilterOrTransformerTaskName(UIConstants.EDIT_TRANSFORMER, UIConstants.EDIT_TRANSFORMER_TASK_NUMBER, steps);
    }
    
    private void updateFilterOrTransformerTaskName(String taskName, int componentIndex, int rulesOrSteps) {
    	if (rulesOrSteps > 0) {
    		taskName += " (" + rulesOrSteps + ")";
    	}
    	
        ((JXHyperlink)channelEditTasks.getContentPane().getComponent(componentIndex)).setText(taskName);
        ((JMenuItem)channelEditPopupMenu.getComponent(componentIndex)).setText(taskName);
    }

    public void doSaveSettings()
    {
        setWorking("Saving settings...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                settingsPane.getSettingsPanel().saveSettings();
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
        channelEditPanel.doValidate();
    }

    public void doImport()
    {
    	File importFile = importFile("XML");
    	
    	if (importFile != null)
    		importChannel(importFile, true);
    }

    public void importChannel(File importFile, boolean showAlerts)
    {
        String channelXML = "";
        boolean overwrite = false;

        try
        {
            channelXML = ImportConverter.convertChannelFile(importFile);
        }
        catch (Exception e1)
        {
            if (showAlerts)
                alertException(this, e1.getStackTrace(), "Invalid channel file. " + e1.getMessage());
        }

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        Channel importChannel;

        try
        {
            importChannel = (Channel) serializer.fromXML(channelXML.replaceAll("\\&\\#x0D;\\n", "\n").replaceAll("\\&\\#x0D;", "\n"));
        }
        catch (Exception e)
        {
            if (showAlerts)
                alertException(this, e.getStackTrace(), "Invalid channel file. " + e.getMessage());
            return;
        }

        /**
         * Checks to see if the passed in channel version is current, and
         * prompts the user if it is not.
         */
        if (showAlerts)
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

            if (option != JOptionPane.YES_OPTION)
                return;
        }

        try
        {
            String channelName = importChannel.getName();
            String tempId = mirthClient.getGuid();

            // Check to see that the channel name doesn't already exist.
            if (!checkChannelName(channelName, tempId))
            {
                if (!alertOption(this, "Would you like to overwrite the existing channel?  Choose 'No' to create a new channel."))
                {
                    importChannel.setRevision(0);

                    do
                    {
                        channelName = JOptionPane.showInputDialog(this, "Please enter a new name for the channel.", channelName);
                        if (channelName == null)
                            return;
                    } while (!checkChannelName(channelName, tempId));

                    importChannel.setName(channelName);
                    importChannel.setId(tempId);
                }
                else
                {
                	overwrite = true;
                	
                    for (Channel channel : channels.values())
                    {
                        if (channel.getName().equalsIgnoreCase(channelName))
                        {
                            importChannel.setId(channel.getId());
                        }
                    }
                }
            }
            // If the channel name didn't already exist, make sure the id
            // doesn't exist either.
            else if (!checkChannelId(importChannel.getId()))
            {
                importChannel.setId(tempId);
            }

            importChannel.setVersion(mirthClient.getVersion());
            channels.put(importChannel.getId(), importChannel);
        }
        catch (ClientException e)
        {
            alertException(this, e.getStackTrace(), e.getMessage());
        }

        try
        {
            if (showAlerts)
            {
                if (checkInstalledConnectors(importChannel))
                {
                    editChannel(importChannel);
                    channelEditTasks.getContentPane().getComponent(0).setVisible(true);
                }
            }
            else
            {
                PropertyVerifier.checkChannelProperties(importChannel);
                PropertyVerifier.checkConnectorProperties(importChannel, channelEditPanel.transports);
                updateChannel(importChannel, overwrite);
                doShowChannel();
            }
        }
        catch (Exception e)
        {
            channels.remove(importChannel.getId());

            if (showAlerts)
            {
                alertError(this, "Channel had an unknown problem. Channel import aborted.");
                channelEditPanel = new ChannelSetup();
            }

            doShowChannel();
        }
    }

    public boolean doExport()
    {
        if (changesHaveBeenMade())
        {
            if (alertOption(this, "This channel has been modified. You must save the channel changes before you can export. Would you like to save them now?"))
            {
                if (!channelEditPanel.saveChanges())
                    return false;
            }
            else
                return false;

            disableSave();
        }

        Channel channel;
        if (currentContentPage == channelEditPanel || currentContentPage == channelEditPanel.filterPane || currentContentPage == channelEditPanel.transformerPane)
            channel = channelEditPanel.currentChannel;
        else
            channel = channelPanel.getSelectedChannel();
        
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        String channelXML = serializer.toXML(channel);
        
        return exportFile(channelXML, channel.getName(), "XML", "Channel");
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
                        if (!alertOption(this, "The file " + channel.getName() + ".xml already exists.  Would you like to overwrite it?"))
                            continue;

                    FileUtil.write(exportFile, channelXML, false);
                }
                alertInformation(this, "All files were written successfully to " + exportDirectory.getPath() + ".");
            }
            catch (IOException ex)
            {
                alertError(this, "File could not be written.");
            }
        }
    }
    
    /**
     * Import a file with the default defined file filter type.
     * @return
     */
    public File importFile(String type)
    {
        JFileChooser importFileChooser = new JFileChooser();
        if (type != null)
        	importFileChooser.setFileFilter(new MirthFileFilter(type));

        File currentDir = new File(userPreferences.get("currentDirectory", ""));
        if (currentDir.exists())
            importFileChooser.setCurrentDirectory(currentDir);

        int returnVal = importFileChooser.showOpenDialog(this);
        File importFile = null;

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            userPreferences.put("currentDirectory", importFileChooser.getCurrentDirectory().getPath());
            importFile = importFileChooser.getSelectedFile();
        }
        
        return importFile;
    }
    
    /**
     * Export a file with the default defined file filter type.
     * @param fileContents
     * @param fileName
     * @return
     */
    public boolean exportFile(String fileContents, String defaultFileName, String type, String name)
    {
        JFileChooser exportFileChooser = new JFileChooser();
        
        if (defaultFileName != null)
        	exportFileChooser.setSelectedFile(new File(defaultFileName));
        if (type != null)
        	exportFileChooser.setFileFilter(new MirthFileFilter(type));

        File currentDir = new File(userPreferences.get("currentDirectory", ""));
        if (currentDir.exists())
            exportFileChooser.setCurrentDirectory(currentDir);

        int returnVal = exportFileChooser.showSaveDialog(this);
        File exportFile = null;

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            userPreferences.put("currentDirectory", exportFileChooser.getCurrentDirectory().getPath());
            exportFile = exportFileChooser.getSelectedFile();

            int length = exportFile.getName().length();

            if (length < 4 || !exportFile.getName().substring(length - 4, length).equalsIgnoreCase("." + type.toLowerCase()))
                exportFile = new File(exportFile.getAbsolutePath() + "." + type.toLowerCase());

            if (exportFile.exists())
                if (!alertOption(this, "This file already exists.  Would you like to overwrite it?"))
                    return false;

            try
            {
                FileUtil.write(exportFile, fileContents, false);
                alertInformation(this, name + " was written to " + exportFile.getPath() + ".");
            }
            catch (IOException ex)
            {
                alertError(this, "File could not be written.");
                return false;
            }
            return true;
        }
        else
            return false;
    }
    
    public void doImportConnector()
    {
    	File connectorFile = importFile("XML");
    	
    	if (connectorFile != null) {
	    	try {
				String connectorXML = ImportConverter.convertConnector(connectorFile);
				ObjectXMLSerializer serializer = new ObjectXMLSerializer();
				Connector connector = (Connector) serializer.fromXML(connectorXML);
				channelEditPanel.importConnector(connector);
			} catch (Exception e) {
				alertException(this, e.getStackTrace(), e.getMessage());
			}
    	}
    }
    
    public void doExportConnector()
    {
        if (changesHaveBeenMade())
        {
            if (alertOption(this, "This channel has been modified. You must save the channel changes before you can export. Would you like to save them now?"))
            {
                if (!channelEditPanel.saveChanges())
                    return;
            }
            else
                return;

            disableSave();
        }

        Connector connector = channelEditPanel.exportSelectedConnector();
        
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        String connectorXML = serializer.toXML(connector);
        
        String fileName = channelEditPanel.currentChannel.getName();
        if (connector.getMode().equals(Mode.SOURCE))
        	fileName += " Source";
        else
        	fileName += " " + connector.getName();
        
        exportFile(connectorXML, fileName, "XML", "Connector");
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
            alertException(this, e.getStackTrace(), e.getMessage());
            return;
        }

        try
        {
            channel.setRevision(0);
            channel.setId(mirthClient.getGuid());
        }
        catch (ClientException e)
        {
            alertException(this, e.getStackTrace(), e.getMessage());
        }

        String channelName = channel.getName();
        do
        {
            channelName = JOptionPane.showInputDialog(this, "Please enter a new name for the channel.", channelName);
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

            if (channel == null)
            {
                alertError(this, "Channel no longer exists!");
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
            alertException(this, e.getStackTrace(), e.getMessage());
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

    public void doRemoveAllMessagesAllChannels()
    {
        if (alertOption(this, "Are you sure you would like to remove all messages and all statistics for all channels?"))
        {
            setWorking("Removing messages...", true);

            SwingWorker worker = new SwingWorker<Void, Void>()
            {
                public Void doInBackground()
                {
                    for (int i = 0; i < status.size(); i++)
                    {
                        try
                        {
                            mirthClient.clearMessages(status.get(i).getChannelId());
                        }
                        catch (ClientException e)
                        {
                            alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                        }

                    }
                    setWorking("", false);
                    setWorking("Clearing statistics...", true);
                    clearStatsAllChannels(true, true, true, true, true, true);
                    setWorking("", false);

                    return null;
                }

                public void done()
                {
                    doRefreshStatuses();

                }
            };

            worker.execute();
        }

    }

    public void clearStatsAllChannels(final boolean deleteReceived, final boolean deleteFiltered, final boolean deleteQueued, final boolean deleteSent, final boolean deleteErrored, final boolean deleteAlerted)
    {

        setWorking("Clearing statistics...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {

                for (int i = 0; i < status.size(); i++)
                {
                    try
                    {
                        mirthClient.clearStatistics(status.get(i).getChannelId(), deleteReceived, deleteFiltered, deleteQueued, deleteSent, deleteErrored, deleteAlerted);
                    }
                    catch (ClientException e)
                    {
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                    }

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

    public void doRemoveAllMessages()
    {
        if (alertOption(this, "Are you sure you would like to remove all messages in this channel?"))
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
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done()
                {
                    if (currentContentPage == dashboardPanel)
                    {
                        if (alertOption(PlatformUI.MIRTH_FRAME, "Would you also like to clear all statistics?"))
                            clearStats(dashboardPanel.getSelectedStatus(), true, true, true, true, true, true);
                        doRefreshStatuses();
                    }
                    else if (currentContentPage == messageBrowser)
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

        if (selectedStatus != -1)
            new DeleteStatisticsDialog(selectedStatus, false);
        else
            dashboardPanel.deselectRows();
    }

    public void doClearStatsAllChannels()
    {

        new DeleteStatisticsDialog(-1, true);

    }

    public void clearStats(final int statusToClear, final boolean deleteReceived, final boolean deleteFiltered, final boolean deleteQueued, final boolean deleteSent, final boolean deleteErrored, final boolean deleteAlerted)
    {
        setWorking("Clearing statistics...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                try
                {
                    mirthClient.clearStatistics(status.get(statusToClear).getChannelId(), deleteReceived, deleteFiltered, deleteQueued, deleteSent, deleteErrored, deleteAlerted);
                }
                catch (ClientException e)
                {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
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
        if (alertOption(this, "Are you sure you would like to remove all currently filtered messages in this channel?"))
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
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done()
                {
                    if (currentContentPage == dashboardPanel)
                        doRefreshStatuses();
                    else if (currentContentPage == messageBrowser)
                        messageBrowser.refresh();
                    setWorking("", false);
                }
            };

            worker.execute();
        }
    }

    public void doRemoveMessage()
    {
        if (alertOption(this, "Are you sure you would like to remove the selected message?"))
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
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done()
                {
                    if (currentContentPage == dashboardPanel)
                        doRefreshStatuses();
                    else if (currentContentPage == messageBrowser)
                        messageBrowser.refresh();
                    setWorking("", false);
                }
            };

            worker.execute();
        }
    }
    
    public void doReprocessFilteredMessages()
    {	
    	setWorking("Retrieving Channels...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
            	if(channels == null || channels.values().size() == 0) { 
            		retrieveChannels();
            	}
            	
                return null;
            }

            public void done()
            { 
            	setWorking("", false);
                new ReprocessMessagesDialog(messageBrowser.getCurrentFilter());
            }
        };
        worker.execute();
    }

    public void doReprocessMessage()
    {
    	setWorking("Retrieving Channels...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
            	if(channels == null || channels.values().size() == 0) { 
            		retrieveChannels();
            	}
            	
                return null;
            }

            public void done()
            {
            	setWorking("", false);
            	MessageObjectFilter filter = new MessageObjectFilter();
                filter.setChannelId(getSelectedChannelIdFromDashboard());
                filter.setId(messageBrowser.getSelectedMessageID());
                new ReprocessMessagesDialog(filter);
            }
        };
        worker.execute();
    }
    
    public void reprocessMessage(final MessageObjectFilter filter, final boolean replace, final List<String> destinations) { 
        setWorking("Reprocessing messages...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                try
                {
                     mirthClient.reprocessMessages(filter, replace, destinations);
                }
                catch (ClientException e)
                {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
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
    
    public void viewImage()
    {
        setWorking("Opening attachment...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                messageBrowser.viewAttachment();
                setWorking("", false);
                return null;
            }

            public void done()
            {
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
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
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

    public void doRemoveAllEvents()
    {
        if (alertOption(this, "Are you sure you would like to clear all system events?"))
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
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
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

    public void doRemoveFilteredEvents()
    {
        if (alertOption(this, "Are you sure you would like to remove all currently filtered system events?"))
        {
            setWorking("Removing events...", true);

            SwingWorker worker = new SwingWorker<Void, Void>()
            {
                public Void doInBackground()
                {
                    try
                    {
                        mirthClient.removeSystemEvents(eventBrowser.getCurrentFilter());
                    }
                    catch (ClientException e)
                    {
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done()
                {
                    if (currentContentPage == eventBrowser)
                        eventBrowser.refresh();
                    setWorking("", false);
                }
            };

            worker.execute();
        }
    }

    public void doRemoveEvent()
    {
        if (alertOption(this, "Are you sure you would like to remove the selected system event?"))
        {
            setWorking("Removing event...", true);

            SwingWorker worker = new SwingWorker<Void, Void>()
            {
                public Void doInBackground()
                {
                    try
                    {
                        SystemEventFilter filter = new SystemEventFilter();
                        filter.setId(eventBrowser.getSelectedEventID());
                        mirthClient.removeSystemEvents(filter);
                    }
                    catch (ClientException e)
                    {
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done()
                {
                    if (currentContentPage == eventBrowser)
                        eventBrowser.refresh();
                    setWorking("", false);
                }
            };

            worker.execute();
        }
    }

    public void doRefreshSettings()
    {
        if (changesHaveBeenMade())
        {
            if (!alertOption(this, "Are you sure you would like to reload the settings from the server and lose your changes?"))
                return;
            else
                disableSave();
        }

        setWorking("Loading settings...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                settingsPane.getSettingsPanel().loadSettings();
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
            alertException(this, e.getStackTrace(), e.getMessage());
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
                    if (!(serverProperties.getProperty("smtp.host") != null && ((String) serverProperties.getProperty("smtp.host")).length() > 0) || !(serverProperties.getProperty("smtp.port") != null && ((String) serverProperties.getProperty("smtp.port")).length() > 0))
                        alertWarning(PlatformUI.MIRTH_FRAME, "The SMTP server on the settings page is not specified or is incomplete.  An SMTP server is required to send alerts.");

                    alertPanel.saveAlert();
                    mirthClient.updateAlerts(alerts);
                }
                catch (ClientException e)
                {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
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
    
    public void doExportAlerts()
    {
        if (changesHaveBeenMade())
        {
            if (alertOption(this, "Would you like to save the changes made to the alerts?"))
                alertPanel.saveAlert();
            else
                return;
        }
        
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        String alertXML = serializer.toXML(alerts);
        
        exportFile(alertXML, null, "XML", "Alerts export");
    }

    public void doImportAlerts()
    {
    	File importFile = importFile("XML");
    	
    	if (importFile != null)
    	{
    		try
            {
                String alertXML = FileUtil.read(importFile);
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                alerts = (List<Alert>) serializer.fromXML(alertXML);
                
                alertInformation(this, "All alerts imported sucessfully.");
                
                enableSave();
                alertPanel.loadAlert();
                alertPanel.updateAlertTable(false);
                
            }
            catch (Exception e)
            {
                alertError(this, "Invalid alert file.");
            }
    	}
    }


    public void doRefreshCodeTemplates()
    {
        setWorking("Loading code templates...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                refreshCodeTemplates();
                return null;
            }

            public void done()
            {
                codeTemplatePanel.updateCodeTemplateTable(false);
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void refreshCodeTemplates()
    {
        try
        {
            codeTemplates = mirthClient.getCodeTemplate(null);
        }
        catch (ClientException e)
        {
            alertException(this, e.getStackTrace(), e.getMessage());
        }
    }

    public void doSaveCodeTemplates()
    {
        setWorking("Saving codeTemplates...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                saveCodeTemplates();
                return null;
            }

            public void done()
            {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public boolean saveCodeTemplates()
    {
        try
        {
            codeTemplatePanel.saveCodeTemplate();

            for (CodeTemplate template : codeTemplates)
            {
                if (template.getType() == CodeSnippetType.FUNCTION)
                {
                    if (!codeTemplatePanel.validateCodeTemplate(template.getCode(), false, template.getName()))
                        return false;
                }
            }
            mirthClient.updateCodeTemplates(codeTemplates);
            ReferenceListFactory.getInstance().updateUserTemplates();
            disableSave();
        }
        catch (ClientException e)
        {
            alertException(this, e.getStackTrace(), e.getMessage());
            return false;
        }
        return true;
    }

    public void doDeleteCodeTemplate()
    {
        codeTemplatePanel.deleteCodeTemplate();
    }

    public void doValidateCodeTemplate()
    {
        codeTemplatePanel.validateCodeTemplate();
    }

    public void doNewCodeTemplate()
    {
        codeTemplatePanel.addCodeTemplate();
    }

    public void doExportCodeTemplates()
    {
        if (changesHaveBeenMade())
        {
            if (alertOption(this, "Would you like to save the changes made to the code templates?"))
                codeTemplatePanel.saveCodeTemplate();
            else
                return;
        }
        
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        String codeTemplateXML = serializer.toXML(codeTemplates);
        
        exportFile(codeTemplateXML, null, "XML", "Code templates export");
    }

    public void doImportCodeTemplates()
    {
    	File importFile = importFile("XML");
    	
    	if (importFile != null)
    	{
    		try
            {
                String codeTemplatesXML = FileUtil.read(importFile);
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                codeTemplates = (List<CodeTemplate>) serializer.fromXML(codeTemplatesXML);
                
                alertInformation(this, "All code templates imported sucessfully.");
                
                enableSave();
                codeTemplatePanel.loadCodeTemplate();
                codeTemplatePanel.updateCodeTemplateTable(false);
            }
            catch (Exception e)
            {
                alertError(this, "Invalid code template file.");
            }
    	}
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

    public void doContextSensitiveSave()
    {
        if (currentContentPage == channelEditPanel)
        {
            doSaveChannel();
        }
        else if (currentContentPage == channelEditPanel.filterPane)
        {
            // channelEditPanel.filterPane.accept(false);
            doSaveChannel();
        }
        else if (currentContentPage == channelEditPanel.transformerPane)
        {
            // channelEditPanel.transformerPane.accept(false);
            doSaveChannel();
        }
        else if (currentContentPage == globalScriptsPanel)
        {
            doSaveGlobalScripts();
        }
        else if (currentContentPage == codeTemplatePanel)
        {
            doSaveCodeTemplates();
        }
        else if (currentContentPage == settingsPane)
        {
            doSaveSettings();
        }
        else if (currentContentPage == alertPanel)
        {
            doSaveAlerts();
        }
        else if (currentContentPage == pluginPanel && pluginPanel.getCurrentPanelPlugin() != null)
        {
        	int saveIndex = pluginPanel.getCurrentPanelPlugin().getSaveIndex();
        	
        	// Make sure the save button is actually visible for plugins.
        	if (saveIndex != -1 && pluginPanel.getCurrentPanelPlugin().getTaskPane().getContentPane().getComponent(saveIndex).isVisible())
        		pluginPanel.getCurrentPanelPlugin().doSave();
        }
    }

    public void doFind(JEditTextArea text)
    {
        FindRplDialog find;
        Window owner = getWindowForComponent(text);
        
    	if (owner instanceof java.awt.Frame) {
    		find = new FindRplDialog((java.awt.Frame)owner,true,text);
    	} else { // window instanceof Dialog
    		find = new FindRplDialog((java.awt.Dialog)owner,true,text);
    	}
    	
        find.setVisible(true);
    }

    public void doHelp()
    {
        if (currentContentPage == channelEditPanel)
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.CHANNEL_HELP_LOCATION);
        else if (currentContentPage == channelPanel)
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.CHANNELS_HELP_LOCATION);
        else if (currentContentPage == dashboardPanel)
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.DASHBOARD_HELP_LOCATION);
        else if (currentContentPage == messageBrowser)
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.MESSAGE_BROWSER_HELP_LOCATION);
        else if (currentContentPage == eventBrowser)
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.SYSTEM_EVENT_HELP_LOCATION);
        else if (currentContentPage == settingsPane)
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.SETTINGS_HELP_LOCATION);
        else if (currentContentPage == channelEditPanel.transformerPane)
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.TRANFORMER_HELP_LOCATION);
        else if (currentContentPage == channelEditPanel.filterPane)
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.FILTER_HELP_LOCATION);
        else if (currentContentPage == pluginPanel)
        	BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.PLUGINS_HELP_LOCATION);
        else if (currentContentPage == alertPanel)
        	BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.ALERTS_HELP_LOCATION);
        else if (currentContentPage == userPanel)
        	BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.USERS_HELP_LOCATION);
        else
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION);
    }

    public Map<String, PluginMetaData> getPluginMetaData()
    {
        return this.loadedPlugins;
    }

    public Map<String, ConnectorMetaData> getConnectorMetaData()
    {
        return this.loadedConnectors;
    }

    public Map<String, DashboardPanelPlugin> getDashboardPanelPlugins()
    {
        return this.dashboardPanel.getLoadedPanelPlugins();
    }
    
    public String getSelectedChannelIdFromDashboard() {
    	return status.get(dashboardPanel.getSelectedStatus()).getChannelId();
    }
    
}

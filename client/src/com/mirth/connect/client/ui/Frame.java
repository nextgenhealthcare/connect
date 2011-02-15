/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.UUID;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.action.ActionFactory;
import org.jdesktop.swingx.action.ActionManager;
import org.jdesktop.swingx.action.BoundAction;
import org.jdesktop.swingx.painter.MattePainter;
import org.syntax.jedit.JEditTextArea;

import com.mirth.connect.client.core.Client;
import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.TaskConstants;
import com.mirth.connect.client.core.UnauthorizedException;
import com.mirth.connect.client.core.UpdateClient;
import com.mirth.connect.client.ui.browsers.event.EventBrowser;
import com.mirth.connect.client.ui.browsers.message.MessageBrowser;
import com.mirth.connect.client.ui.extensionmanager.ExtensionManagerPanel;
import com.mirth.connect.client.ui.extensionmanager.ExtensionUpdateDialog;
import com.mirth.connect.client.ui.panels.reference.ReferenceListFactory;
import com.mirth.connect.model.Alert;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelProperties;
import com.mirth.connect.model.ChannelStatistics;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.UpdateInfo;
import com.mirth.connect.model.User;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.MessageObjectFilter;
import com.mirth.connect.model.util.ImportConverter;
import com.mirth.connect.plugins.DashboardColumnPlugin;
import com.mirth.connect.util.PropertyVerifier;

/**
 * The main content frame for the Mirth Client Application. Extends JXFrame and
 * sets up all content.
 */
public class Frame extends JXFrame {

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
    public ExtensionManagerPanel extensionsPanel = null;
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
    
    // Task panes and popup menus
    public JXTaskPane viewPane;
    public JXTaskPane otherPane;
    public JXTaskPane channelTasks;
    public JPopupMenu channelPopupMenu;
    public JXTaskPane dashboardTasks;
    public JPopupMenu dashboardPopupMenu;
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
    public JXTaskPane extensionsTasks;
    public JPopupMenu extensionsPopupMenu;
    
    public JXTitledPanel rightContainer;
    private Thread statusUpdater;
    public static Preferences userPreferences;
    private StatusUpdater su;
    private boolean connectionError;
    private ArrayList<CharsetEncodingInformation> availableCharsetEncodings = null;
    private List<String> charsetEncodings = null;
    private boolean isEditingChannel = false;
    private Stack<String> workingStack = new Stack<String>();
    public LinkedHashMap<MessageObject.Protocol, String> protocols;
    private Map<String, PluginMetaData> loadedPlugins;
    private Map<String, ConnectorMetaData> loadedConnectors;
    private UpdateClient updateClient = null;
    private boolean refreshingStatuses = false;
    private Map<String, Integer> safeErrorFailCountMap = new HashMap<String, Integer>();
    private Map<Component, String> componentTaskMap = new HashMap<Component, String>();
    private boolean isAcceleratorKeyPressed = false;
    
    public Frame() {
        rightContainer = new JXTitledPanel();
        channels = new HashMap<String, Channel>();

        taskPaneContainer = new JXTaskPaneContainer();

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
        setIconImage(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/mirth_32_ico.png")).getImage());
        makePaneContainer();

        connectionError = false;

        this.addComponentListener(new ComponentListener() {

            public void componentResized(ComponentEvent e) {
                if (channelEditPanel != null && channelEditPanel.filterPane != null) {
                    channelEditPanel.filterPane.resizePanes();
                }
                if (channelEditPanel != null && channelEditPanel.transformerPane != null) {
                    channelEditPanel.transformerPane.resizePanes();
                }
            }

            public void componentHidden(ComponentEvent e) {
            }

            public void componentShown(ComponentEvent e) {
            }

            public void componentMoved(ComponentEvent e) {
            }
        });

        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                if (logout()) {
                    System.exit(0);
                }
            }
        });
        
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                isAcceleratorKeyPressed = (e.getModifiers() & java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) > 0;
                return false;
            }
        });
    }

    /**
     * Prepares the list of the encodings. This method is called from the Frame
     * class.
     * 
     */
    public void setCharsetEncodings() {
        if (this.availableCharsetEncodings != null) {
            return;
        }
        try {
            this.charsetEncodings = this.mirthClient.getAvailableCharsetEncodings();
            this.availableCharsetEncodings = new ArrayList<CharsetEncodingInformation>();
            this.availableCharsetEncodings.add(new CharsetEncodingInformation(UIConstants.DEFAULT_ENCODING_OPTION, "Default"));
            for (int i = 0; i < charsetEncodings.size(); i++) {
                String canonical = charsetEncodings.get(i);
                this.availableCharsetEncodings.add(new CharsetEncodingInformation(canonical, canonical));
            }
        } catch (Exception e) {
            alertError(this, "Error getting the charset list:\n " + e);
        }
    }

    /**
     * Creates all the items in the combo box for the connectors.
     * 
     * This method is called from each connector.
     */
    public void setupCharsetEncodingForConnector(javax.swing.JComboBox charsetEncodingCombobox) {
        if (this.availableCharsetEncodings == null) {
            this.setCharsetEncodings();
        }
        if (this.availableCharsetEncodings == null) {
            logger.error("Error, the are no encodings detected.");
            return;
        }
        charsetEncodingCombobox.removeAllItems();
        for (int i = 0; i < this.availableCharsetEncodings.size(); i++) {
            charsetEncodingCombobox.addItem(this.availableCharsetEncodings.get(i));
        }
    }

    /**
     * Sets the combobox for the string previously selected. If the server can't
     * support the encoding, the default one is selected. This method is called
     * from each connector.
     */
    public void setPreviousSelectedEncodingForConnector(javax.swing.JComboBox charsetEncodingCombobox, String selectedCharset) {
        if (this.availableCharsetEncodings == null) {
            this.setCharsetEncodings();
        }
        if (this.availableCharsetEncodings == null) {
            logger.error("Error, there are no encodings detected.");
            return;
        }
        if ((selectedCharset == null) || (selectedCharset.equalsIgnoreCase(UIConstants.DEFAULT_ENCODING_OPTION))) {
            charsetEncodingCombobox.setSelectedIndex(0);
        } else if (this.charsetEncodings.contains(selectedCharset)) {
            int index = this.availableCharsetEncodings.indexOf(new CharsetEncodingInformation(selectedCharset, selectedCharset));
            if (index < 0) {
                logger.error("Synchronization lost in the list of the encoding characters.");
                index = 0;
            }
            charsetEncodingCombobox.setSelectedIndex(index);
        } else {
            alertInformation(this, "Sorry, the JVM of the server can't support the previously selected " + selectedCharset + " encoding. Please choose another one or install more encodings in the server.");
            charsetEncodingCombobox.setSelectedIndex(0);
        }
    }

    /**
     * Get the strings which identifies the encoding selected by the user.
     * 
     * This method is called from each connector.
     */
    public String getSelectedEncodingForConnector(javax.swing.JComboBox charsetEncodingCombobox) {
        try {
            return ((CharsetEncodingInformation) charsetEncodingCombobox.getSelectedItem()).getCanonicalName();
        } catch (Throwable t) {
            alertInformation(this, "Error " + t);
            return UIConstants.DEFAULT_ENCODING_OPTION;
        }
    }

    /**
     * Called to set up this main window frame.
     */
    public void setupFrame(Client mirthClient, LoginPanel login) {

        this.mirthClient = mirthClient;
        // Re-initialize the controller every time the frame is setup
        AuthorizationControllerFactory.getAuthorizationController().initialize();
        refreshCodeTemplates();
        login.setStatus("Loading extensions...");
        loadExtensions();
        setInitialVisibleTasks();
        login.setStatus("Loading preferences...");
        userPreferences = Preferences.userNodeForPackage(Mirth.class);
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

        // Set task pane container background painter
        MattePainter taskPanePainter = new MattePainter(new GradientPaint(0f, 0f, UIConstants.JX_CONTAINER_BACKGROUND_COLOR, 0f, 1f, UIConstants.JX_CONTAINER_BACKGROUND_COLOR));
        taskPanePainter.setPaintStretched(true);
        taskPaneContainer.setBackgroundPainter(taskPanePainter);

        // Set main content container title painter
        MattePainter contentTitlePainter = new MattePainter(new GradientPaint(0f, 0f, UIConstants.JX_CONTAINER_BACKGROUND_COLOR, 0f, 1f, UIConstants.JX_CONTAINER_BACKGROUND_COLOR));
        contentTitlePainter.setPaintStretched(true);
        rightContainer.setTitlePainter(contentTitlePainter);

        splitPane.add(rightContainer, JSplitPane.RIGHT);
        splitPane.add(taskPane, JSplitPane.LEFT);
        taskPane.setMinimumSize(new Dimension(UIConstants.TASK_PANE_WIDTH, 0));
        splitPane.setDividerLocation(UIConstants.TASK_PANE_WIDTH);

        contentPanel.add(statusBar, BorderLayout.SOUTH);
        contentPanel.add(splitPane, java.awt.BorderLayout.CENTER);

        try {
            PlatformUI.SERVER_ID = mirthClient.getServerId();
            PlatformUI.SERVER_VERSION = mirthClient.getVersion();
            PlatformUI.SERVER_TIMEZONE = mirthClient.getServerTimezone();

            setTitle(getTitle() + " - (" + PlatformUI.SERVER_VERSION + ")");

            String version = PlatformUI.SERVER_VERSION;
            int majorVersion = Integer.parseInt(version.split("\\.")[0]);
            int minorVersion = Integer.parseInt(version.split("\\.")[1]);
            int patchVersion = Integer.parseInt(version.split("\\.")[2]);
            PlatformUI.HELP_LOCATION += "v" + majorVersion + "r" + minorVersion + "p" + patchVersion + "/";
            PlatformUI.BUILD_DATE = mirthClient.getBuildDate();
        } catch (ClientException e) {
            alertError(this, "Could not get server information.");
        }
        
        // Display the server timezone information
        statusBar.setTimezoneText(PlatformUI.SERVER_TIMEZONE);

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

//         UIDefaults uiDefaults = UIManager.getDefaults(); Enumeration enum1 =
//         uiDefaults.keys(); while (enum1.hasMoreElements()) { Object key =
//         enum1.nextElement(); Object val = uiDefaults.get(key);
////         if(key.toString().indexOf("ComboBox") != -1)
//         System.out.println("UIManager.put(\"" + key.toString() + "\",\"" +
//         (null != val ? val.toString() : "(null)") + "\");"); }

    }

    public void loadExtensions() {
        try {
            loadedPlugins = mirthClient.getPluginMetaData();
            loadedConnectors = mirthClient.getConnectorMetaData();
        } catch (ClientException e) {
            alertException(this, e.getStackTrace(), "Unable to load extensions");
        }
        
        // Initialize all of the extensions now that the metadata has been retrieved
        LoadedExtensions.getInstance().initialize();
        
        LoadedExtensions.getInstance().startPlugins();
    }

    /**
     * Builds the content panel with a title bar and settings.
     */
    private void buildContentPanel(JXTitledPanel container, JScrollPane component, boolean opaque) {
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

        ((JPanel) container.getComponent(0)).add(mirthConnectImage);

        component.setBorder(new LineBorder(Color.GRAY, 1));
        component.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        component.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        container.getContentContainer().add(component);
    }

    /**
     * Set the main content panel title to a String
     */
    public void setPanelName(String name) {
        rightContainer.setTitle(name);
        statusBar.setStatusText("");
    }

    public void setWorking(final String displayText, final boolean working) {
        if (statusBar != null) {
            String text = displayText;

            if (working) {
                workingStack.push(statusBar.getText());
            } else if (workingStack.size() > 0) {
                text = workingStack.pop();
            }

            if (workingStack.size() > 0) {
                statusBar.setWorking(true);
            } else {
                statusBar.setWorking(false);
            }

            statusBar.setText(text);
        }
    }

    /**
     * Changes the current content page to the Channel Editor with the new
     * channel specified as the loaded one.
     */
    public void setupChannel(Channel channel) {
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
    public void editChannel(Channel channel) {
        setBold(viewPane, UIConstants.ERROR_CONSTANT);
        setCurrentContentPage(channelEditPanel);
        setFocus(channelEditTasks);
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 0, 4, false);
        channelEditPanel.editChannel(channel);
    }

    /**
     * Edit global scripts
     */
    public void editGlobalScripts() {
        setBold(viewPane, UIConstants.ERROR_CONSTANT);
        setCurrentContentPage(globalScriptsPanel);
        setFocus(globalScriptsTasks);
        setVisibleTasks(globalScriptsTasks, globalScriptsPopupMenu, 0, 0, false);
        setPanelName("Global Scripts");
    }

    /**
     * Sets the current content page to the passed in page.
     */
    public void setCurrentContentPage(Component contentPageObject) {
        if (contentPageObject == currentContentPage) {
            return;
        }

        if (currentContentPage != null) {
            contentPane.getViewport().remove(currentContentPage);
        }

        contentPane.getViewport().add(contentPageObject);
        currentContentPage = contentPageObject;
    }

    /**
     * Sets the current task pane container
     */
    private void setCurrentTaskPaneContainer(JXTaskPaneContainer container) {
        if (container == currentTaskPaneContainer) {
            return;
        }

        if (currentTaskPaneContainer != null) {
            taskPane.getViewport().remove(currentTaskPaneContainer);
        }

        taskPane.getViewport().add(container);
        currentTaskPaneContainer = container;
    }

    /**
     * Makes all of the task panes and shows the dashboard panel.
     */
    private void makePaneContainer() {
        createViewPane();
        createChannelPane();
        createChannelEditPane();
        createDashboardPane();
        createEventPane();
        createMessagePane();
        createUserPane();
        createAlertPane();
        createGlobalScriptsPane();
        createCodeTemplatePane();
        createExtensionsPane();
        createOtherPane();
    }
    
    private void setInitialVisibleTasks() {
        // View Pane
        setVisibleTasks(viewPane, null, 0, -1, true);
        
        // Alert Pane
        setVisibleTasks(alertTasks, alertPopupMenu, 0, 7, false);
        setVisibleTasks(alertTasks, alertPopupMenu, 2, 4, true);
        
        // Channel Pane
        setVisibleTasks(channelTasks, channelPopupMenu, 0, -1, true);
        setVisibleTasks(channelTasks, channelPopupMenu, 1, 2, false);
        setVisibleTasks(channelTasks, channelPopupMenu, 7, -1, false);
        
        // Channel Edit Pane
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 0, 14, false);
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 13, 13, true);
        
        // Dashboard Pane
        setVisibleTasks(dashboardTasks, dashboardPopupMenu, 0, 0, true);
        setVisibleTasks(dashboardTasks, dashboardPopupMenu, 1, -1, false);
        
        // Event Pane
        setVisibleTasks(eventTasks, eventPopupMenu, 0, 1, true);
        
        // Message Pane
        setVisibleTasks(messageTasks, messagePopupMenu, 0, -1, true);
        setVisibleTasks(messageTasks, messagePopupMenu, 6, -1, false);
        setVisibleTasks(messageTasks, messagePopupMenu, 7, 7, true);
        
        // User Pane
        setVisibleTasks(userTasks, userPopupMenu, 0, 1, true);
        setVisibleTasks(userTasks, userPopupMenu, 2, -1, false);
        
        // Code Template Pane
        setVisibleTasks(codeTemplateTasks, codeTemplatePopupMenu, 0, 1, false);
        setVisibleTasks(codeTemplateTasks, codeTemplatePopupMenu, 2, 4, true);
        setVisibleTasks(codeTemplateTasks, codeTemplatePopupMenu, 5, 6, false);
        
        // Global Scripts Pane
        setVisibleTasks(globalScriptsTasks, globalScriptsPopupMenu, 0, 0, false);
        setVisibleTasks(globalScriptsTasks, globalScriptsPopupMenu, 1, -1, true);
        
        // Extensions Pane
        setVisibleTasks(extensionsTasks, extensionsPopupMenu, 0, 0, true);
        setVisibleTasks(extensionsTasks, extensionsPopupMenu, 1, 1, false);
        setVisibleTasks(extensionsTasks, extensionsPopupMenu, 2, 2, true);
        setVisibleTasks(extensionsTasks, extensionsPopupMenu, 3, -1, false);
        
        // Other Pane
        setVisibleTasks(otherPane, null, 0, -1, true);
    }

    /**
     * Creates the view task pane.
     */
    private void createViewPane() {
        // Create View pane
        viewPane = new JXTaskPane();
        viewPane.setTitle("Mirth Connect");
        viewPane.setName(TaskConstants.VIEW_KEY);
        viewPane.setFocusable(false);

        addTask(TaskConstants.VIEW_DASHBOARD, "Dashboard", "Contains information about your currently deployed channels.", "D", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_view_detail.png")), viewPane, null);
        addTask(TaskConstants.VIEW_CHANNEL, "Channels", "Contains various operations to perform on your channels.", "C", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_form.png")), viewPane, null);
        addTask(TaskConstants.VIEW_USERS, "Users", "Contains information on users.", "U", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/group.png")), viewPane, null);
        addTask(TaskConstants.VIEW_SETTINGS, "Settings", "Contains local and system settings.", "S", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/wrench.png")), viewPane, null);
        addTask(TaskConstants.VIEW_ALERTS, "Alerts", "Contains alert settings.", "A", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/error.png")), viewPane, null);
        addTask(TaskConstants.VIEW_EVENTS, "Events", "Show the event logs for the system.", "E", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/table.png")), viewPane, null);
        addTask(TaskConstants.VIEW_EXTENSIONS, "Extensions", "View and manage Mirth Connect extensions", "X", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/plugin.png")), viewPane, null);

        setNonFocusable(viewPane);
        taskPaneContainer.add(viewPane);
        viewPane.setVisible(true);
    }

    /**
     * Creates the template task pane.
     */
    private void createAlertPane() {
        // Create Alert Edit Tasks Pane
        alertTasks = new JXTaskPane();
        alertPopupMenu = new JPopupMenu();
        alertTasks.setTitle("Alert Tasks");
        alertTasks.setName(TaskConstants.ALERT_KEY);
        alertTasks.setFocusable(false);

        addTask(TaskConstants.ALERT_REFRESH, "Refresh", "Refresh the list of alerts.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_refresh.png")), alertTasks, alertPopupMenu);
        addTask(TaskConstants.ALERT_SAVE, "Save Alerts", "Save all changes made to all alerts.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/disk.png")), alertTasks, alertPopupMenu);
        addTask(TaskConstants.ALERT_NEW, "New Alert", "Create a new alert.", "N", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/error_add.png")), alertTasks, alertPopupMenu);
        addTask(TaskConstants.ALERT_IMPORT, "Import Alerts", "Import list of alerts from an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")), alertTasks, alertPopupMenu);
        addTask(TaskConstants.ALERT_EXPORT, "Export Alerts", "Export the list of alerts to an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), alertTasks, alertPopupMenu);
        addTask(TaskConstants.ALERT_DELETE, "Delete Alert", "Delete the currently selected alert.", "L", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/error_delete.png")), alertTasks, alertPopupMenu);
        addTask(TaskConstants.ALERT_ENABLE, "Enable Alert", "Enable the currently selected alert.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_play_blue.png")), alertTasks, alertPopupMenu);
        addTask(TaskConstants.ALERT_DISABLE, "Disable Alert", "Disable the currently selected alert.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_stop_blue.png")), alertTasks, alertPopupMenu);

        setNonFocusable(alertTasks);
        taskPaneContainer.add(alertTasks);
    }

    /**
     * Creates the channel task pane.
     */
    private void createChannelPane() {
        // Create Channel Tasks Pane
        channelTasks = new JXTaskPane();
        channelPopupMenu = new JPopupMenu();
        channelTasks.setTitle("Channel Tasks");
        channelTasks.setName(TaskConstants.CHANNEL_KEY);
        channelTasks.setFocusable(false);

        addTask(TaskConstants.CHANNEL_REFRESH, "Refresh", "Refresh the list of channels.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_refresh.png")), channelTasks, channelPopupMenu);
        addTask(TaskConstants.CHANNEL_REDEPLOY_ALL, "Redeploy All", "Undeploy all channels and deploy all currently enabled channels.", "A", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_rotate_clockwise.png")), channelTasks, channelPopupMenu);
        addTask(TaskConstants.CHANNEL_DEPLOY, "Deploy Channel", "Deploys the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_redo.png")), channelTasks, channelPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_GLOBAL_SCRIPTS, "Edit Global Scripts", "Edit scripts that are not channel specific.", "G", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/script_edit.png")), channelTasks, channelPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_CODE_TEMPLATES, "Edit Code Templates", "Create and manage templates to be used in JavaScript throughout Mirth.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/page_edit.png")), channelTasks, channelPopupMenu);
        addTask(TaskConstants.CHANNEL_NEW, "New Channel", "Create a new channel.", "N", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_form_add.png")), channelTasks, channelPopupMenu);
        addTask(TaskConstants.CHANNEL_IMPORT, "Import Channel", "Import a channel from an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")), channelTasks, channelPopupMenu);
        addTask(TaskConstants.CHANNEL_EXPORT_ALL, "Export All Channels", "Export all of the channels to XML files.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), channelTasks, channelPopupMenu);
        addTask(TaskConstants.CHANNEL_EXPORT, "Export Channel", "Export the currently selected channel to an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), channelTasks, channelPopupMenu);
        addTask(TaskConstants.CHANNEL_DELETE, "Delete Channel", "Delete the currently selected channel.", "L", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_form_delete.png")), channelTasks, channelPopupMenu);
        addTask(TaskConstants.CHANNEL_CLONE, "Clone Channel", "Clone the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/page_copy.png")), channelTasks, channelPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT, "Edit Channel", "Edit the currently selected channel.", "I", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_form_edit.png")), channelTasks, channelPopupMenu);
        addTask(TaskConstants.CHANNEL_ENABLE, "Enable Channel", "Enable the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_play_blue.png")), channelTasks, channelPopupMenu);
        addTask(TaskConstants.CHANNEL_DISABLE, "Disable Channel", "Disable the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_stop_blue.png")), channelTasks, channelPopupMenu);

        setNonFocusable(channelTasks);
        taskPaneContainer.add(channelTasks);
    }

    /**
     * Creates the channel edit task pane.
     */
    private void createChannelEditPane() {
        // Create Channel Edit Tasks Pane
        channelEditTasks = new JXTaskPane();
        channelEditPopupMenu = new JPopupMenu();
        channelEditTasks.setTitle("Channel Tasks");
        channelEditTasks.setName(TaskConstants.CHANNEL_EDIT_KEY);
        channelEditTasks.setFocusable(false);

        addTask(TaskConstants.CHANNEL_EDIT_SAVE, "Save Changes", "Save all changes made to this channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/disk.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_VALIDATE, "Validate Connector", "Validate the currently visible connector.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/accept.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_NEW_DESTINATION, "New Destination", "Create a new destination.", "N", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/add.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_DELETE_DESTINATION, "Delete Destination", "Delete the currently selected destination.", "L", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/delete.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_CLONE_DESTINATION, "Clone Destination", "Clones the currently selected destination.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/page_copy.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_ENABLE_DESTINATION, "Enable Destination", "Enable the currently selected destination.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_play_blue.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_DISABLE_DESTINATION, "Disable Destination", "Disable the currently selected destination.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_stop_blue.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_MOVE_DESTINATION_UP, "Move Dest. Up", "Move the currently selected destination up.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_up.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_MOVE_DESTINATION_DOWN, "Move Dest. Down", "Move the currently selected destination down.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_down.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_FILTER, UIConstants.EDIT_FILTER, "Edit the filter for the currently selected destination.", "F", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/table_edit.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_TRANSFORMER, UIConstants.EDIT_TRANSFORMER, "Edit the transformer for the currently selected destination.", "T", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/table_edit.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_IMPORT_CONNECTOR, "Import Connector", "Import the currently displayed connector from an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_EXPORT_CONNECTOR, "Export Connector", "Export the currently displayed connector to an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_EXPORT, "Export Channel", "Export the currently selected channel to an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_VALIDATE_SCRIPT, "Validate Script", "Validate the currently viewed script.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/accept.png")), channelEditTasks, channelEditPopupMenu);

        setNonFocusable(channelEditTasks);
        taskPaneContainer.add(channelEditTasks);
    }

    /**
     * Creates the status task pane.
     */
    private void createDashboardPane() {
        // Create Status Tasks Pane
        dashboardTasks = new JXTaskPane();
        dashboardPopupMenu = new JPopupMenu();
        dashboardTasks.setTitle("Dashboard Tasks");
        dashboardTasks.setName(TaskConstants.DASHBOARD_KEY);
        dashboardTasks.setFocusable(false);

        addTask(TaskConstants.DASHBOARD_REFRESH, "Refresh", "Refresh the list of statuses.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_refresh.png")), dashboardTasks, dashboardPopupMenu);

        addTask(TaskConstants.DASHBOARD_SEND_MESSAGE, "Send Message", "Send messages to the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/email_go.png")), dashboardTasks, dashboardPopupMenu);
        addTask(TaskConstants.DASHBOARD_SHOW_MESSAGES, "View Messages", "Show the messages for the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/page_white_stack.png")), dashboardTasks, dashboardPopupMenu);
        addTask(TaskConstants.DASHBOARD_REMOVE_ALL_MESSAGES, "Remove All Messages", "Remove all Messages in this channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/email_delete.png")), dashboardTasks, dashboardPopupMenu);
        addTask(TaskConstants.DASHBOARD_CLEAR_STATS, "Clear Statistics", "Reset the statistics for this channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/chart_bar_delete.png")), dashboardTasks, dashboardPopupMenu);

        addTask(TaskConstants.DASHBOARD_START, "Start Channel", "Start the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_play_blue.png")), dashboardTasks, dashboardPopupMenu);
        addTask(TaskConstants.DASHBOARD_PAUSE, "Pause Channel", "Pause the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_pause_blue.png")), dashboardTasks, dashboardPopupMenu);
        addTask(TaskConstants.DASHBOARD_STOP, "Stop Channel", "Stop the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_stop_blue.png")), dashboardTasks, dashboardPopupMenu);

        addTask(TaskConstants.DASHBOARD_UNDEPLOY, "Undeploy Channel", "Undeploys the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_undo.png")), dashboardTasks, dashboardPopupMenu);

        setNonFocusable(dashboardTasks);
        taskPaneContainer.add(dashboardTasks);
    }

    /**
     * Creates the event task pane.
     */
    private void createEventPane() {
        // Create Event Tasks Pane
        eventTasks = new JXTaskPane();
        eventPopupMenu = new JPopupMenu();
        eventTasks.setTitle("Event Tasks");
        eventTasks.setName(TaskConstants.EVENT_KEY);
        eventTasks.setFocusable(false);

        addTask(TaskConstants.EVENT_REFRESH, "Refresh", "Refresh the list of events with the given filter.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_refresh.png")), eventTasks, eventPopupMenu);
        addTask(TaskConstants.EVENT_REMOVE_ALL, "Remove All Events", "Remove all the events.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/table_delete.png")), eventTasks, eventPopupMenu);

        setNonFocusable(eventTasks);
        taskPaneContainer.add(eventTasks);
    }

    /**
     * Creates the message task pane.
     */
    private void createMessagePane() {
        // Create Message Tasks Pane
        messageTasks = new JXTaskPane();
        messagePopupMenu = new JPopupMenu();
        messageTasks.setTitle("Message Tasks");
        messageTasks.setName(TaskConstants.MESSAGE_KEY);
        messageTasks.setFocusable(false);

        addTask(TaskConstants.MESSAGE_REFRESH, "Refresh", "Refresh the list of messages with the given filter.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_refresh.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_SEND, "Send Message", "Send a message to the channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/email_go.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_IMPORT, "Import Messages", "Import messages from a file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_EXPORT, "Export Results", "Export all messages in the current search.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_REMOVE_ALL, "Remove All Messages", "Remove all messages in this channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/email_delete.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_REMOVE_FILTERED, "Remove Results", "Remove all messages in the current search.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/email_delete.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_REMOVE, "Remove Message", "Remove the selected Message.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/delete.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_REPROCESS_FILTERED, "Reprocess Results", "Reprocess all messages in the current search.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_rotate_clockwise.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_REPROCESS, "Reprocess Message", "Reprocess the selected message.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_redo.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_VIEW_IMAGE, "View Attachment", "View Attachment", "View the attachment for the selected message.", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/attach.png")), messageTasks, messagePopupMenu);

        setNonFocusable(messageTasks);
        taskPaneContainer.add(messageTasks);
    }

    /**
     * Creates the users task pane.
     */
    private void createUserPane() {
        // Create User Tasks Pane
        userTasks = new JXTaskPane();
        userPopupMenu = new JPopupMenu();
        userTasks.setTitle("User Tasks");
        userTasks.setName(TaskConstants.USER_KEY);
        userTasks.setFocusable(false);

        addTask(TaskConstants.USER_REFRESH, "Refresh", "Refresh the list of users.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_refresh.png")), userTasks, userPopupMenu);
        addTask(TaskConstants.USER_NEW, "New User", "Create a new user.", "N", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/user_add.png")), userTasks, userPopupMenu);
        addTask(TaskConstants.USER_EDIT, "Edit User", "Edit the currently selected user.", "I", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/user_edit.png")), userTasks, userPopupMenu);
        addTask(TaskConstants.USER_DELETE, "Delete User", "Delete the currently selected user.", "L", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/user_delete.png")), userTasks, userPopupMenu);

        setNonFocusable(userTasks);
        taskPaneContainer.add(userTasks);
    }

    /**
     * Creates the codeTemplate task pane.
     */
    private void createCodeTemplatePane() {
        // Create CodeTemplate Edit Tasks Pane
        codeTemplateTasks = new JXTaskPane();
        codeTemplatePopupMenu = new JPopupMenu();
        codeTemplateTasks.setTitle("Code Template Tasks");
        codeTemplateTasks.setName(TaskConstants.CODE_TEMPLATE_KEY);
        codeTemplateTasks.setFocusable(false);

        addTask(TaskConstants.CODE_TEMPLATE_REFRESH, "Refresh", "Refresh the list of code templates.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_refresh.png")), codeTemplateTasks, codeTemplatePopupMenu);
        addTask(TaskConstants.CODE_TEMPLATE_SAVE, "Save CodeTemplates", "Save all changes made to all code templates.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/disk.png")), codeTemplateTasks, codeTemplatePopupMenu);
        addTask(TaskConstants.CODE_TEMPLATE_NEW, "New CodeTemplate", "Create a new code template.", "N", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/add.png")), codeTemplateTasks, codeTemplatePopupMenu);
        addTask(TaskConstants.CODE_TEMPLATE_IMPORT, "Import Code Templates", "Import list of code templates from an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")), codeTemplateTasks, codeTemplatePopupMenu);
        addTask(TaskConstants.CODE_TEMPLATE_EXPORT, "Export Code Templates", "Export the list of code templates to an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), codeTemplateTasks, codeTemplatePopupMenu);
        addTask(TaskConstants.CODE_TEMPLATE_DELETE, "Delete CodeTemplate", "Delete the currently selected code template.", "L", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/delete.png")), codeTemplateTasks, codeTemplatePopupMenu);
        addTask(TaskConstants.CODE_TEMPLATE_VALIDATE, "Validate Script", "Validate the currently viewed script.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/accept.png")), codeTemplateTasks, codeTemplatePopupMenu);

        setNonFocusable(codeTemplateTasks);
        taskPaneContainer.add(codeTemplateTasks);
    }

    /**
     * Creates the global scripts edit task pane.
     */
    private void createGlobalScriptsPane() {
        globalScriptsTasks = new JXTaskPane();
        globalScriptsPopupMenu = new JPopupMenu();
        globalScriptsTasks.setTitle("Script Tasks");
        globalScriptsTasks.setName(TaskConstants.GLOBAL_SCRIPT_KEY);
        globalScriptsTasks.setFocusable(false);

        addTask(TaskConstants.GLOBAL_SCRIPT_SAVE, "Save Scripts", "Save all changes made to all scripts.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/disk.png")), globalScriptsTasks, globalScriptsPopupMenu);
        addTask(TaskConstants.GLOBAL_SCRIPT_VALIDATE, "Validate Script", "Validate the currently viewed script.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/accept.png")), globalScriptsTasks, globalScriptsPopupMenu);
        addTask(TaskConstants.GLOBAL_SCRIPT_IMPORT, "Import Scripts", "Import all global scripts from an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")), globalScriptsTasks, globalScriptsPopupMenu);
        addTask(TaskConstants.GLOBAL_SCRIPT_EXPORT, "Export Scripts", "Export all global scripts to an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), globalScriptsTasks, globalScriptsPopupMenu);

        setNonFocusable(globalScriptsTasks);
        taskPaneContainer.add(globalScriptsTasks);
    }
    
    /**
     * Creates the extensions task pane.
     */
    private void createExtensionsPane() {
        extensionsTasks = new JXTaskPane();
        extensionsPopupMenu = new JPopupMenu();
        extensionsTasks.setTitle("Extension Tasks");
        extensionsTasks.setName(TaskConstants.EXTENSIONS_KEY);
        extensionsTasks.setFocusable(false);

        addTask(TaskConstants.EXTENSIONS_REFRESH, "Refresh", "Refresh loaded plugins.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_refresh.png")), extensionsTasks, extensionsPopupMenu);
        addTask(TaskConstants.EXTENSIONS_SAVE, "Save", "Save plugin settings.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/disk.png")), extensionsTasks, extensionsPopupMenu);
        addTask(TaskConstants.EXTENSIONS_CHECK_FOR_UPDATES, "Check for Updates", "Checks all extensions for updates.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/world_link.png")), extensionsTasks, extensionsPopupMenu);
        addTask(TaskConstants.EXTENSIONS_ENABLE, "Enable Extension", "Enable the currently selected extension.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_play_blue.png")), extensionsTasks, extensionsPopupMenu);
        addTask(TaskConstants.EXTENSIONS_DISABLE, "Disable Extension", "Disable the currently selected extension.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_stop_blue.png")), extensionsTasks, extensionsPopupMenu);
        addTask(TaskConstants.EXTENSIONS_SHOW_PROPERTIES, "Show Properties", "Display the currently selected extension properties.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_view_list.png")), extensionsTasks, extensionsPopupMenu);
        addTask(TaskConstants.EXTENSIONS_UNINSTALL, "Uninstall Extension", "Uninstall the currently selected extension", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/plugin_delete.png")), extensionsTasks, extensionsPopupMenu);
        
        setNonFocusable(extensionsTasks);
        taskPaneContainer.add(extensionsTasks);
    }

    /**
     * Creates the other task pane.
     */
    private void createOtherPane() {
        // Create Other Pane
        otherPane = new JXTaskPane();
        otherPane.setTitle("Other");
        otherPane.setName(TaskConstants.OTHER_KEY);
        otherPane.setFocusable(false);
        addTask(TaskConstants.OTHER_HELP, "Help on this topic", "Open browser for help on this topic.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/help.png")), otherPane, null);
        addTask(TaskConstants.OTHER_ABOUT, "About Mirth Connect", "View the about page for Mirth Connect.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/information.png")), otherPane, null);
        addTask(TaskConstants.OTHER_VISIT_MIRTH, "Visit mirthcorp.com", "View Mirth's homepage.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/house.png")), otherPane, null);
        addTask(TaskConstants.OTHER_REPORT_ISSUE, "Report Issue", "Visit Mirth's issue tracker.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bug.png")), otherPane, null);
        addTask(TaskConstants.OTHER_LOGOUT, "Logout", "Logout and return to the login screen.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/disconnect.png")), otherPane, null);
        setNonFocusable(otherPane);
        taskPaneContainer.add(otherPane);
        otherPane.setVisible(true);
    }

    public JXTaskPane getOtherPane() {
        return otherPane;
    }

    /**
     * Initializes the bound method call for the task pane actions and adds them
     * to the taskpane/popupmenu.
     */
    public void addTask(String callbackMethod, String displayName, String toolTip, String shortcutKey, ImageIcon icon, JXTaskPane pane, JPopupMenu menu) {
        BoundAction boundAction = ActionFactory.createBoundAction(callbackMethod, displayName, shortcutKey);

        if (icon != null) {
            boundAction.putValue(Action.SMALL_ICON, icon);
        }
        boundAction.putValue(Action.SHORT_DESCRIPTION, toolTip);
        boundAction.registerCallback(this, callbackMethod);

        Component component = pane.add(boundAction);
        getComponentTaskMap().put(component, callbackMethod);
        
        if (menu != null) {
            menu.add(boundAction);
        }
    }

    public Map<Component, String> getComponentTaskMap() {
        return componentTaskMap;
    }

    /**
     * Alerts the user with a yes/no option with the passed in 'message'
     */
    public boolean alertOption(Component parentComponent, String message) {
        int option = JOptionPane.showConfirmDialog(getVisibleComponent(parentComponent), message, "Select an Option", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Alerts the user with a Ok/cancel option with the passed in 'message'
     */
    public boolean alertOkCancel(Component parentComponent, String message) {
        int option = JOptionPane.showConfirmDialog(getVisibleComponent(parentComponent), message, "Select an Option", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Alerts the user with an information dialog with the passed in 'message'
     */
    public void alertInformation(Component parentComponent, String message) {
        JOptionPane.showMessageDialog(getVisibleComponent(parentComponent), message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Alerts the user with a warning dialog with the passed in 'message'
     */
    public void alertWarning(Component parentComponent, String message) {
        JOptionPane.showMessageDialog(getVisibleComponent(parentComponent), message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Alerts the user with an error dialog with the passed in 'message'
     */
    public void alertError(Component parentComponent, String message) {
        JOptionPane.showMessageDialog(getVisibleComponent(parentComponent), message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Alerts the user with an error dialog with the passed in 'message' and a
     * 'question'.
     */
    public void alertCustomError(Component parentComponent, String message, String question) {
        parentComponent = getVisibleComponent(parentComponent);

        Window owner = getWindowForComponent(parentComponent);

        if (owner instanceof java.awt.Frame) {
            new CustomErrorDialog((java.awt.Frame) owner, message, question);
        } else { // window instanceof Dialog
            new CustomErrorDialog((java.awt.Dialog) owner, message, question);
        }
    }

    /**
     * Alerts the user with an exception dialog with the passed in stack trace.
     */
    public void alertException(Component parentComponent, StackTraceElement[] strace, String message) {
        alertException(parentComponent, strace, message, null);
    }

    /**
     * Alerts the user with an exception dialog with the passed in stack trace.
     */
    public void alertException(Component parentComponent, StackTraceElement[] strace, String message, String safeErrorKey) {
        if (connectionError) {
            return;
        }

        if (safeErrorKey != null) {
            increaseSafeErrorFailCount(safeErrorKey);

            if (getSafeErrorFailCount(safeErrorKey) < 3) {
                return;
            }
        }

        parentComponent = getVisibleComponent(parentComponent);

        if (message != null) {
            if (message.indexOf("Received close_notify during handshake") != -1) {
                return;
            }

            if (message.indexOf("Forbidden") != -1 || message.indexOf("reset") != -1) {
                connectionError = true;
                if (currentContentPage == dashboardPanel) {
                    su.interruptThread();
                }
                alertWarning(parentComponent, "Sorry your connection to Mirth has either timed out or there was an error in the connection.  Please login again.");
                if (!exportChannelOnError()) {
                    return;
                }
                mirthClient.cleanup();
                this.dispose();
                Mirth.main(new String[]{PlatformUI.SERVER_NAME, PlatformUI.CLIENT_VERSION});
                return;
            } else if (message.indexOf("Connection refused") != -1) {
                connectionError = true;
                if (currentContentPage == dashboardPanel) {
                    su.interruptThread();
                }
                alertWarning(parentComponent, "The Mirth server " + PlatformUI.SERVER_NAME + " is no longer running.  Please start it and login again.");
                if (!exportChannelOnError()) {
                    return;
                }
                mirthClient.cleanup();
                this.dispose();
                Mirth.main(new String[]{PlatformUI.SERVER_NAME, PlatformUI.CLIENT_VERSION});
                return;
            }
        }

        if (message.indexOf("Unauthorized") != -1) {
            message = "You are not authorized to peform this action.\n\n" + message;
        }
        
        String stackTrace = message + "\n";
        for (int i = 0; i < strace.length; i++) {
            stackTrace += strace[i].toString() + "\n";
        }

        logger.error(stackTrace);

        Window owner = getWindowForComponent(parentComponent);

        if (owner instanceof java.awt.Frame) {
            new ErrorDialog((java.awt.Frame) owner, stackTrace);
        } else { // window instanceof Dialog
            new ErrorDialog((java.awt.Dialog) owner, stackTrace);
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
            owner = (Window) parentComponent;
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
    public void setBold(JXTaskPane pane, int index) {
        for (int i = 0; i < pane.getContentPane().getComponentCount(); i++) {
            pane.getContentPane().getComponent(i).setFont(UIConstants.TEXTFIELD_PLAIN_FONT);
        }

        if (index != UIConstants.ERROR_CONSTANT) {
            pane.getContentPane().getComponent(index).setFont(UIConstants.TEXTFIELD_BOLD_FONT);
        }
    }

    /**
     * Sets the visible task pane to the specified 'pane'
     */
    public void setFocus(JXTaskPane pane) {
        setFocus(new JXTaskPane[]{pane}, true, true);
    }

    /**
     * Sets the visible task panes to the specified 'panes'.
     * Also allows setting the 'Mirth' and 'Other' panes.
     */
    public void setFocus(JXTaskPane[] panes, boolean mirthPane, boolean otherPane) {
        taskPaneContainer.getComponent(0).setVisible(mirthPane);

        // ignore the first and last components
        for (int i = 1; i < taskPaneContainer.getComponentCount() - 1; i++) {
            taskPaneContainer.getComponent(i).setVisible(false);
        }

        taskPaneContainer.getComponent(taskPaneContainer.getComponentCount() - 1).setVisible(otherPane);

        if (panes != null) {
            for (JXTaskPane pane : panes) {
                if (pane != null) {
                    pane.setVisible(true);
                }
            }
        }
    }

    /**
     * Sets all components in pane to be non-focusable.
     */
    public void setNonFocusable(JXTaskPane pane) {
        for (int i = 0; i < pane.getContentPane().getComponentCount(); i++) {
            pane.getContentPane().getComponent(i).setFocusable(false);
        }
    }

    /**
     * Sets the visible tasks in the given 'pane' and 'menu'. The method takes
     * an interval of indices (end index should be -1 to go to the end), as
     * well as a whether they should be set to visible or not-visible.
     */
    public void setVisibleTasks(JXTaskPane pane, JPopupMenu menu, int startIndex, int endIndex, boolean visible) {
        // If the endIndex is -1, disregard it, otherwise stop there.
        for (int i = startIndex; (endIndex == -1 ? true : i <= endIndex) && (i < pane.getContentPane().getComponentCount()); i++) {
            // If the component being set visible is in the security list, don't allow it.
            
            boolean componentVisible = visible;
            String componentTask = getComponentTaskMap().get(pane.getContentPane().getComponent(i));
            if (componentTask != null) {
                if (!AuthorizationControllerFactory.getAuthorizationController().checkTask(pane.getName(), componentTask)) {
                    componentVisible = false;
                }
            }
            
            pane.getContentPane().getComponent(i).setVisible(componentVisible);
            
            if (menu != null) {
                menu.getComponent(i).setVisible(componentVisible);
            }
        }
    }

    /**
     * A prompt to ask the user if he would like to save the changes made before
     * leaving the page.
     */
    public boolean confirmLeave() {
        if ((currentContentPage == channelEditPanel || currentContentPage == channelEditPanel.transformerPane || currentContentPage == channelEditPanel.filterPane) && isSaveEnabled()) {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the channel changes?");
            if (option == JOptionPane.YES_OPTION) {
                if (!channelEditPanel.saveChanges()) {
                    return false;
                }
            } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return false;
            }
        } else if (currentContentPage == settingsPane && isSaveEnabled()) {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the " + settingsPane.getCurrentSettingsPanel().getTabName() + " settings changes?");

            if (option == JOptionPane.YES_OPTION) {
                settingsPane.getCurrentSettingsPanel().doSave();
            } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return false;
            }
        } else if (currentContentPage == alertPanel && isSaveEnabled()) {
            alertPanel.stopAlertEditing();
            alertPanel.stopEmailEditing();

            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the alerts?");

            if (option == JOptionPane.YES_OPTION) {
                doSaveAlerts();
            } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return false;
            }
        } else if (currentContentPage == globalScriptsPanel && isSaveEnabled()) {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the scripts?");

            if (option == JOptionPane.YES_OPTION) {
                doSaveGlobalScripts();
            } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return false;
            }
        } else if (currentContentPage == codeTemplatePanel && isSaveEnabled()) {
            codeTemplatePanel.stopCodeTemplateEditing();

            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the code templates?");

            if (option == JOptionPane.YES_OPTION) {
                if (!saveCodeTemplates()) {
                    return false;
                }
            } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return false;
            }
        } else if (currentContentPage == extensionsPanel && isSaveEnabled()) {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the extensions?");

            if (option == JOptionPane.YES_OPTION) {
                doSaveExtensions();
            } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return false;
            }
        }

        setSaveEnabled(false);
        return true;
    }

    /**
     * Sends the channel passed in to the server, updating it or adding it.
     * @throws ClientException 
     */
    public boolean updateChannel(Channel curr, boolean override) throws ClientException {
        if (!mirthClient.updateChannel(curr, override)) {
            if (alertOption(this, "This channel has been modified since you first opened it, or you have imported\nan older version of the channel.  Would you like to overwrite it?")) {
                mirthClient.updateChannel(curr, true);
            } else {
                return false;
            }
        }
        retrieveChannels();

        return true;
    }

    /**
     * Sends the passed in user to the server, updating it or adding it.
     */
    public void updateUser(final Component parentComponent, final User curr, final String password) {
        setWorking("Saving user...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    mirthClient.updateUser(curr, password);
                    retrieveUsers();
                } catch (ClientException e) {
                    alertException(parentComponent, e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done() {
                userPanel.updateUserTable();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void updateAndSwitchUser(Component parentComponent, final User curr, String newUsername, String newPassword) {
        setWorking("Saving user...", true);

        try {
            mirthClient.updateUser(curr, newPassword);
            retrieveUsers();
        } catch (ClientException e) {
            alertException(parentComponent, e.getStackTrace(), e.getMessage());
        } finally {
            // The userPanel will be null if the user panel has not been viewed (i.e. registration).
            if (userPanel != null) {
                userPanel.updateUserTable();
            }

            setWorking("", false);
        }

        setWorking("Switching User...", true);

        try {
            LoadedExtensions.getInstance().resetPlugins();
            mirthClient.logout();
            mirthClient.login(newUsername, newPassword, PlatformUI.CLIENT_VERSION);
            PlatformUI.USER_NAME = newUsername;
            updateClient = null; // Reset the update client so it uses the new user next time it is called.
        } catch (ClientException e) {
            alertException(parentComponent, e.getStackTrace(), e.getMessage());
        } finally {
            setWorking("", false);
        }
    }

    public User getCurrentUser(Component parentComponent) {
        User currentUser = null;

        try {
            retrieveUsers();
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

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    getUpdateClient(PlatformUI.MIRTH_FRAME).registerUser(user);
                } catch (ClientException e) {
                    // ignore errors connecting to update/stats server
                }

                return null;
            }

            public void done() {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void checkForUpdates() {
        setWorking("Checking for updates...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
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

                        for (UpdateInfo updateInfo : updateInfoList) {
                            // Set to true as long as the update is not ignored and not optional.
                            if (!updateInfo.isIgnored() && !updateInfo.isOptional()) {
                                newUpdates = true;
                            }
                        }

                        if (newUpdates) {
                            new ExtensionUpdateDialog(updateInfoList);
                        }
                    } catch (ClientException e) {
                        // ignore errors connecting to update/stats server
                    }
                }

                return null;
            }

            public void done() {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void sendUsageStatistics() {
        setWorking("Sending usage statistics...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
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

            public void done() {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    /**
     * Checks to see if the passed in channel id already exists
     */
    public boolean checkChannelId(String id) {
        for (Channel channel : channels.values()) {
            if (channel.getId().equalsIgnoreCase(id)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks to see if the passed in channel name already exists
     */
    public boolean checkChannelName(String name, String id) {
        if (name.equals("")) {
            alertWarning(this, "Channel name cannot be empty.");
            return false;
        }

        if (name.length() > 40) {
            alertWarning(this, "Channel name cannot be longer than 40 characters.");
            return false;
        }

        Pattern alphaNumericPattern = Pattern.compile("^[a-zA-Z_0-9\\-\\s]*$");
        Matcher matcher = alphaNumericPattern.matcher(name);

        if (!matcher.find()) {
            alertWarning(this, "Channel name cannot have special characters besides hyphen, underscore, and space.");
            return false;
        }

        for (Channel channel : channels.values()) {
            if (channel.getName().equalsIgnoreCase(name) && !channel.getId().equals(id)) {
                alertWarning(this, "Channel \"" + name + "\" already exists.");
                return false;
            }
        }
        return true;
    }
    
    /**
     * Enables the save button for needed page.
     */
    public void setSaveEnabled(boolean enabled) {
        if (currentContentPage == channelEditPanel) {
            setVisibleTasks(channelEditTasks, channelEditPopupMenu, 0, 0, enabled);
        } else if (channelEditPanel != null && currentContentPage == channelEditPanel.transformerPane) {
            channelEditPanel.transformerPane.modified = enabled;
        } else if (channelEditPanel != null && currentContentPage == channelEditPanel.filterPane) {
            channelEditPanel.filterPane.modified = enabled;
        } else if (currentContentPage == settingsPane) {
            settingsPane.getCurrentSettingsPanel().setSaveEnabled(enabled);
        } else if (alertPanel != null && currentContentPage == alertPanel) {
            setVisibleTasks(alertTasks, alertPopupMenu, 1, 1, enabled);
        } else if (globalScriptsPanel != null && currentContentPage == globalScriptsPanel) {
            setVisibleTasks(globalScriptsTasks, globalScriptsPopupMenu, 0, 0, enabled);
        } else if (codeTemplatePanel != null && currentContentPage == codeTemplatePanel) {
            setVisibleTasks(codeTemplateTasks, codeTemplatePopupMenu, 1, 1, enabled);
        } else if (extensionsPanel != null && currentContentPage == extensionsPanel) {
            setVisibleTasks(extensionsTasks, extensionsPopupMenu, 1, 1, enabled);
        }
    }
    
    /**
     * Enables the save button for needed page.
     */
    public boolean isSaveEnabled() {
        boolean enabled = false;
        
        if (currentContentPage == channelEditPanel) {
            enabled = channelEditTasks.getContentPane().getComponent(0).isVisible();
        } else if (channelEditPanel != null && currentContentPage == channelEditPanel.transformerPane) {
            enabled = channelEditTasks.getContentPane().getComponent(0).isVisible() || channelEditPanel.transformerPane.modified;
        } else if (channelEditPanel != null && currentContentPage == channelEditPanel.filterPane) {
            enabled = channelEditTasks.getContentPane().getComponent(0).isVisible() || channelEditPanel.filterPane.modified;
        } else if (currentContentPage == settingsPane) {
            enabled = settingsPane.getCurrentSettingsPanel().isSaveEnabled();
        } else if (alertPanel != null && currentContentPage == alertPanel) {
            enabled = alertTasks.getContentPane().getComponent(1).isVisible();
        } else if (globalScriptsPanel != null && currentContentPage == globalScriptsPanel) {
            enabled = globalScriptsTasks.getContentPane().getComponent(0).isVisible();
        } else if (codeTemplatePanel != null && currentContentPage == codeTemplatePanel) {
            enabled = codeTemplateTasks.getContentPane().getComponent(1).isVisible();
        } else if (extensionsPanel != null && currentContentPage == extensionsPanel) {
            enabled = extensionsTasks.getContentPane().getComponent(1).isVisible();
        }
        
        return enabled;
    }

    // ////////////////////////////////////////////////////////////
    // --- All bound actions are beneath this point --- //
    // ////////////////////////////////////////////////////////////
    public void goToMirth() {
        BareBonesBrowserLaunch.openURL("http://www.mirthcorp.com/");
    }

    public void goToAbout() {
        new AboutMirth();
    }

    public void doReportIssue() {
        BareBonesBrowserLaunch.openURL(UIConstants.ISSUE_TRACKER_LOCATION);
    }

    public void doShowDashboard() {
        if (dashboardPanel == null) {
            dashboardPanel = new DashboardPanel();
        }

        if (!confirmLeave()) {
            return;
        }

        setBold(viewPane, 0);
        setPanelName("Dashboard");
        setCurrentContentPage(dashboardPanel);
        setFocus(dashboardTasks);

        doRefreshStatuses();
    }

    public void doShowChannel() {
        if (channelPanel == null) {
            channelPanel = new ChannelPanel();
        }

        if (!confirmLeave()) {
            return;
        }

        setBold(viewPane, 1);
        setPanelName("Channels");
        setCurrentContentPage(channelPanel);
        setFocus(channelTasks);

        doRefreshChannels();
    }

    public void doShowUsers() {
        if (userPanel == null) {
            userPanel = new UserPanel();
        }

        if (!confirmLeave()) {
            return;
        }

        setWorking("Loading users...", true);

        setBold(viewPane, 2);
        setPanelName("Users");
        setCurrentContentPage(userPanel);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                refreshUser();
                return null;
            }

            public void done() {
                setFocus(userTasks);
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doShowSettings() {
        if (settingsPane == null) {
            settingsPane = new SettingsPane();
        }

        if (!confirmLeave()) {
            return;
        }

        setWorking("Loading settings...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                settingsPane.setSelectedSettingsPanel(0);
                return null;
            }

            public void done() {
                setBold(viewPane, 3);
                setPanelName("Settings");
                setCurrentContentPage(settingsPane);
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doShowAlerts() {
        if (alertPanel == null) {
            alertPanel = new AlertPanel();
        }

        if (!confirmLeave()) {
            return;
        }

        setWorking("Loading alerts...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                retrieveChannels();
                refreshAlerts();
                return null;
            }

            public void done() {
                alertPanel.updateAlertTable();
                setBold(viewPane, 4);
                setPanelName("Alerts");
                setCurrentContentPage(alertPanel);
                alertPanel.setDefaultAlert();
                setFocus(alertTasks);
                setSaveEnabled(false);
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doShowExtensions() {
        if (extensionsPanel == null) {
            extensionsPanel = new ExtensionManagerPanel();
        }
        
        setWorking("Loading extensions...", true);
        if (confirmLeave()) {
            setBold(viewPane, 6);
            setPanelName("Extensions");
            setCurrentContentPage(extensionsPanel);
            setFocus(extensionsTasks);
            refreshExtensions();
            setWorking("", false);
        }
    }

    public void doLogout() {
        logout();
    }

    public boolean logout() {
        if (!confirmLeave()) {
            return false;
        }

        if (currentContentPage == dashboardPanel) {
            su.interruptThread();
        }

        userPreferences = Preferences.userNodeForPackage(Mirth.class);
        userPreferences.putInt("maximizedState", getExtendedState());
        userPreferences.putInt("width", getWidth());
        userPreferences.putInt("height", getHeight());

        LoadedExtensions.getInstance().stopPlugins();

        try {
            mirthClient.cleanup();
            mirthClient.logout();
            this.dispose();
            Mirth.main(new String[]{PlatformUI.SERVER_NAME, PlatformUI.CLIENT_VERSION});
        } catch (ClientException e) {
            alertException(this, e.getStackTrace(), e.getMessage());
        }

        return true;
    }

    public void doMoveDestinationDown() {
        channelEditPanel.moveDestinationDown();
    }

    public void doMoveDestinationUp() {
        channelEditPanel.moveDestinationUp();
    }

    public void doNewChannel() {
        if (LoadedExtensions.getInstance().getSourceConnectors().size() == 0 || LoadedExtensions.getInstance().getDestinationConnectors().size() == 0) {
            alertError(this, "You must have at least one source connector and one destination connector installed.");
            return;
        }

        // The channel wizard will call createNewChannel() or create a channel
        // from a wizard.
        new ChannelWizard();
    }

    public void createNewChannel() {
        Channel channel = new Channel();

        try {
            channel.setId(mirthClient.getGuid());
        } catch (ClientException e) {
            alertException(this, e.getStackTrace(), e.getMessage());
        }

        channel.setName("");
        channel.setEnabled(true);
        channel.getProperties().setProperty("initialState", "Started");
        setupChannel(channel);
    }

    public void doEditChannel() {
        if (isEditingChannel) {
            return;
        } else {
            isEditingChannel = true;
        }

        List<Channel> selectedChannels = channelPanel.getSelectedChannels();
        if (selectedChannels.size() > 1) {
            JOptionPane.showMessageDialog(Frame.this, "This operation can only be performed on a single Channel.");
        } else if (selectedChannels.size() == 0) {
            JOptionPane.showMessageDialog(Frame.this, "Channel no longer exists.");
        } else {
            try {
                Channel channel = selectedChannels.get(0);

                if (checkInstalledConnectors(channel)) {
                    editChannel((Channel) SerializationUtils.clone(channel));
                }
            } catch (SerializationException e) {
                alertException(this, e.getStackTrace(), e.getMessage());
            }
        }
        isEditingChannel = false;
    }

    public boolean checkInstalledConnectors(Channel channel) {
        Connector source = channel.getSourceConnector();
        List<Connector> destinations = channel.getDestinationConnectors();
        ArrayList<String> missingConnectors = new ArrayList<String>();

        if (!LoadedExtensions.getInstance().getSourceConnectors().containsKey(source.getTransportName())) {
            missingConnectors.add(source.getTransportName());
        }

        for (int i = 0; i < destinations.size(); i++) {
            if (!LoadedExtensions.getInstance().getDestinationConnectors().containsKey(destinations.get(i).getTransportName())) {
                missingConnectors.add(destinations.get(i).getTransportName());
            }
        }

        if (missingConnectors.size() > 0) {
            String errorText = "Your Mirth installation is missing required connectors for this channel:\n";
            for (String s : missingConnectors) {
                errorText += s + "\n";
            }
            alertError(this, errorText);
            return false;
        } else {
            return true;
        }
    }

    public void doEditGlobalScripts() {
        if (globalScriptsPanel == null) {
            globalScriptsPanel = new GlobalScriptsPanel();
        }

        setWorking("Loading global scripts...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                globalScriptsPanel.edit();
                return null;
            }

            public void done() {
                editGlobalScripts();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doEditCodeTemplates() {

        if (codeTemplatePanel == null) {
            codeTemplatePanel = new CodeTemplatePanel();
        }

        setWorking("Loading code templates...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                refreshCodeTemplates();
                return null;
            }

            public void done() {
                codeTemplatePanel.updateCodeTemplateTable();
                setBold(viewPane, UIConstants.ERROR_CONSTANT);
                setPanelName("Code Templates");
                setCurrentContentPage(codeTemplatePanel);
                codeTemplatePanel.setDefaultCodeTemplate();
                setFocus(codeTemplateTasks);
                setSaveEnabled(false);
                setWorking("", false);
            }
        };

        worker.execute();

    }

    public void doValidateCurrentGlobalScript() {
        globalScriptsPanel.validateCurrentScript();
    }

    public void doImportGlobalScripts() {
        File importFile = importFile("XML");

        if (importFile != null) {
            try {
                String scriptsXml = FileUtils.readFileToString(importFile, UIConstants.CHARSET);
                scriptsXml = ImportConverter.convertGlobalScripts(scriptsXml);
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                
                Map<String, String> importScripts = (Map<String, String>) serializer.fromXML(scriptsXml);

                globalScriptsPanel.importAllScripts(importScripts);
            } catch (Exception e) {
                alertException(this, e.getStackTrace(), "Invalid scripts file. " + e.getMessage());
                return;
            }
        }
    }

    public void doExportGlobalScripts() {
        if (changesHaveBeenMade()) {
            if (alertOption(this, "You must save your global scripts before exporting.  Would you like to save them now?")) {
                globalScriptsPanel.save();
                setSaveEnabled(false);
            } else {
                return;
            }
        }

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        String globalScriptsXML = serializer.toXML(globalScriptsPanel.exportAllScripts());

        exportFile(globalScriptsXML, null, "XML", "Global Scripts export");
    }

    public void doValidateChannelScripts() {
        channelEditPanel.validateScripts();
    }

    public void doSaveGlobalScripts() {
        setWorking("Saving global scripts...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                globalScriptsPanel.save();
                return null;
            }

            public void done() {
                setSaveEnabled(false);
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doDeleteChannel() {
        if (!alertOption(this, "Are you sure you want to delete the selected channel(s)?\nAny selected deployed channel(s) will first be undeployed.")) {
            return;
        }

        setWorking("Deleting channel...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    status = mirthClient.getChannelStatusList();
                } catch (ClientException e) {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                    return null;
                }
                List<Channel> selectedChannels = channelPanel.getSelectedChannels();
                if (selectedChannels.size() == 0) {
                    return null;
                }

                List<String> undeployChannelIds = new ArrayList<String>();

                for (Channel channel : selectedChannels) {

                    String channelId = channel.getId();
                    for (int i = 0; i < status.size(); i++) {
                        if (status.get(i).getChannelId().equals(channelId)) {
                            undeployChannelIds.add(channelId);
                        }
                    }
                }

                if (undeployChannelIds.size() > 0) {
                    try {
                        mirthClient.undeployChannels(undeployChannelIds);
                    } catch (ClientException e) {
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                        return null;
                    }
                }

                for (Channel channel : selectedChannels) {
                    try {
                        mirthClient.removeChannel(channel);
                    } catch (ClientException e) {
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                        return null;
                    }
                }

                return null;
            }

            public void done() {
                doRefreshChannels();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doRefreshChannels() {
        setWorking("Loading channels...", true);
        
        final List<String> selectedChannelIds = new ArrayList<String>();

        for (Channel channel : channelPanel.getSelectedChannels()) {
            selectedChannelIds.add(channel.getId());
        }

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    status = mirthClient.getChannelStatusList();
                } catch (ClientException e) {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                }

                retrieveChannels();
                return null;
            }

            public void done() {
                int enabled = 0;
                for (Channel channel : channels.values()) {
                    if (channel.isEnabled()) {
                        enabled++;
                    }
                }
                
                statusBar.setStatusText(channels.size() + " Channels, " + enabled + " Enabled");
                
                channelPanel.updateChannelTable();

                setVisibleTasks(channelTasks, channelPopupMenu, 1, 2, false);
                setVisibleTasks(channelTasks, channelPopupMenu, 7, -1, false);

                if (channels.size() > 0) {
                    setVisibleTasks(channelTasks, channelPopupMenu, 1, 1, true);
                    setVisibleTasks(channelTasks, channelPopupMenu, 7, 7, true);
                }

                channelPanel.setSelectedChannels(selectedChannelIds);

                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void retrieveChannels() {
        try {
            List<ChannelSummary> changedChannels = mirthClient.getChannelSummary(getChannelHeaders());

            if (changedChannels.size() == 0) {
                return;
            } else {
                for (int i = 0; i < changedChannels.size(); i++) {
                    if (changedChannels.get(i).isAdded()) {
                        Channel filterChannel = new Channel();
                        filterChannel.setId(changedChannels.get(i).getId());
                        Channel channelToAdd = mirthClient.getChannel(filterChannel).get(0);
                        channels.put(channelToAdd.getId(), channelToAdd);
                    } else {
                        Channel matchingChannel = channels.get(changedChannels.get(i).getId());

                        if (changedChannels.get(i).isDeleted()) {
                            channels.remove(matchingChannel.getId());
                        } else {
                            Channel filterChannel = new Channel();
                            filterChannel.setId(matchingChannel.getId());
                            Channel channelToUpdate = mirthClient.getChannel(filterChannel).get(0);
                            channels.put(matchingChannel.getId(), channelToUpdate);
                        }
                    }
                }
            }
        } catch (ClientException e) {
            alertException(this, e.getStackTrace(), e.getMessage());
        }
    }

    public Map<String, Integer> getChannelHeaders() {
        HashMap<String, Integer> channelHeaders = new HashMap<String, Integer>();

        for (Channel channel : channels.values()) {
            channelHeaders.put(channel.getId(), channel.getRevision());
        }

        return channelHeaders;
    }

    public void clearChannelCache() {
        channels = new HashMap<String, Channel>();
    }

    public void setRefreshingStatuses(boolean refreshingStatuses) {
        synchronized (this) {
            this.refreshingStatuses = refreshingStatuses;
        }
    }

    public boolean isRefreshingStatuses() {
        synchronized (this) {
            return refreshingStatuses;
        }
    }

    public synchronized void increaseSafeErrorFailCount(String safeErrorKey) {
        int safeErrorFailCount = getSafeErrorFailCount(safeErrorKey) + 1;
        this.safeErrorFailCountMap.put(safeErrorKey, safeErrorFailCount);
    }

    public synchronized void resetSafeErrorFailCount(String safeErrorKey) {
        this.safeErrorFailCountMap.put(safeErrorKey, 0);
    }

    public synchronized int getSafeErrorFailCount(String safeErrorKey) {
        if (safeErrorFailCountMap.containsKey(safeErrorKey)) {
            return safeErrorFailCountMap.get(safeErrorKey);
        } else {
            return 0;
        }
    }

    public void doRefreshStatuses() {
        // Don't allow anything to be getting or setting refreshingStatuses
        // while this block is being executed.
        synchronized (this) {
            if (isRefreshingStatuses()) {
                return;
            }

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

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            Object[][] tableData = null;

            public Void doInBackground() {
                try {
                    status = mirthClient.getChannelStatusList();
                    resetSafeErrorFailCount(TaskConstants.DASHBOARD_REFRESH);

                    if (status != null) {
                        for (DashboardColumnPlugin plugin : LoadedExtensions.getInstance().getDashboardColumnPlugins().values()) {
                            plugin.tableUpdate(status);
                        }

                        tableData = new Object[status.size()][10 + LoadedExtensions.getInstance().getDashboardColumnPlugins().size()];
                        for (int i = 0; i < status.size(); i++) {
                            ChannelStatus tempStatus = status.get(i);
                            int statusColumn = 0;
                            try {
                                ChannelStatistics tempStats = mirthClient.getStatistics(tempStatus.getChannelId());
                                int j = 0;
                                for (DashboardColumnPlugin plugin : LoadedExtensions.getInstance().getDashboardColumnPlugins().values()) {
                                    if (plugin.showBeforeStatusColumn()){
                                        tableData[i][j] = plugin.getTableData(tempStatus);
                                        j++;
                                    }
                                }
                                statusColumn = j;
                                j += 2;

                                tableData[i][j] = tempStatus.getDeployedRevisionDelta();
                                tableData[i][++j] = tempStatus.getDeployedDate();
                                tableData[i][++j] = tempStats.getReceived();
                                tableData[i][++j] = tempStats.getFiltered();
                                tableData[i][++j] = tempStats.getQueued();
                                tableData[i][++j] = tempStats.getSent();
                                tableData[i][++j] = tempStats.getError();
                                tableData[i][++j] = tempStats.getAlerted();
                                j++;
                                for (DashboardColumnPlugin plugin : LoadedExtensions.getInstance().getDashboardColumnPlugins().values()) {
                                    if (!plugin.showBeforeStatusColumn()) {
                                        tableData[i][j] = plugin.getTableData(tempStatus);
                                        j++;
                                    }
                                }

                            } catch (ClientException ex) {
                                alertException(PlatformUI.MIRTH_FRAME, ex.getStackTrace(), ex.getMessage());
                            }

                            if (tempStatus.getState() == ChannelStatus.State.STARTED) {
                                tableData[i][statusColumn] = new CellData(UIConstants.ICON_BULLET_GREEN, "Started");
                            } else if (tempStatus.getState() == ChannelStatus.State.STOPPED) {
                                tableData[i][statusColumn] = new CellData(UIConstants.ICON_BULLET_RED, "Stopped");
                            } else if (tempStatus.getState() == ChannelStatus.State.PAUSED) {
                                tableData[i][statusColumn] = new CellData(UIConstants.ICON_BULLET_YELLOW, "Paused");
                            }

                            tableData[i][statusColumn + 1] = tempStatus.getName();
                        }
                    }
                } catch (ClientException e) {
                    status = null;
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage(), TaskConstants.DASHBOARD_REFRESH);
                }

                return null;
            }

            public void done() {
                setWorking("", false);
                if (status != null) {
                    statusBar.setStatusText(status.size() + " Deployed Channels");
                    dashboardPanel.updateTable(tableData);
                    dashboardPanel.updateCurrentPluginPanel();
                }
                setRefreshingStatuses(false);
            }
        };
        worker.execute();
    }

    public void doStartAll() {
        setWorking("Starting all channels...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    for (int i = 0; i < status.size(); i++) {
                        if (status.get(i).getState() == ChannelStatus.State.STOPPED) {
                            mirthClient.startChannel(status.get(i).getChannelId());
                        } else if (status.get(i).getState() == ChannelStatus.State.PAUSED) {
                            mirthClient.resumeChannel(status.get(i).getChannelId());
                        }
                    }
                } catch (ClientException e) {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done() {
                doRefreshStatuses();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doStopAll() {
        setWorking("Stopping all channels...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    for (int i = 0; i < status.size(); i++) {
                        if (status.get(i).getState() == ChannelStatus.State.STARTED || status.get(i).getState() == ChannelStatus.State.PAUSED) {
                            mirthClient.stopChannel(status.get(i).getChannelId());
                        }
                    }
                } catch (ClientException e) {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done() {
                doRefreshStatuses();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doStart() {
        List<ChannelStatus> selectedChannelStatuses = dashboardPanel.getSelectedStatuses();

        if (selectedChannelStatuses.size() == 0) {
            return;
        }

        for (final ChannelStatus channelStatus : selectedChannelStatuses) {
            setWorking("Starting channel...", true);

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                public Void doInBackground() {
                    try {
                        if (channelStatus.getState() == ChannelStatus.State.STOPPED) {
                            mirthClient.startChannel(channelStatus.getChannelId());
                        } else if (channelStatus.getState() == ChannelStatus.State.PAUSED) {
                            mirthClient.resumeChannel(channelStatus.getChannelId());
                        }
                    } catch (ClientException e) {
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done() {
                    doRefreshStatuses();
                    setWorking("", false);
                }
            };

            worker.execute();
        }
    }

    public void doStop() {
        List<ChannelStatus> selectedChannelStatuses = dashboardPanel.getSelectedStatuses();

        if (selectedChannelStatuses.size() == 0) {
            return;
        }

        for (final ChannelStatus channelStatus : selectedChannelStatuses) {
            setWorking("Stopping channel...", true);

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                public Void doInBackground() {
                    try {
                        mirthClient.stopChannel(channelStatus.getChannelId());
                    } catch (ClientException e) {
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done() {
                    doRefreshStatuses();
                    setWorking("", false);
                }
            };

            worker.execute();
        }
    }

    public void doPause() {
        List<ChannelStatus> selectedChannelStatuses = dashboardPanel.getSelectedStatuses();

        if (selectedChannelStatuses.size() == 0) {
            return;
        }

        for (final ChannelStatus channelStatus : selectedChannelStatuses) {
            setWorking("Pausing channel...", true);

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                public Void doInBackground() {
                    try {
                        mirthClient.pauseChannel(channelStatus.getChannelId());
                    } catch (ClientException e) {
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done() {
                    doRefreshStatuses();
                    setWorking("", false);
                }
            };

            worker.execute();
        }
    }

    public void doNewDestination() {
        channelEditPanel.addNewDestination();
    }

    public void doDeleteDestination() {
        if (!alertOption(this, "Are you sure you want to delete this destination?")) {
            return;
        }

        channelEditPanel.deleteDestination();
    }

    public void doCloneDestination() {
        channelEditPanel.cloneDestination();
    }

    public void doEnableDestination() {
        channelEditPanel.enableDestination();
    }

    public void doDisableDestination() {
        channelEditPanel.disableDestination();
    }

    public void doEnableChannel() {
        List<Channel> selectedChannels = channelPanel.getSelectedChannels();
        if (selectedChannels.size() == 0) {
            alertWarning(this, "Channel no longer exists.");
            return;
        }

        for (final Channel channel : selectedChannels) {

            String validationMessage = channelEditPanel.checkAllForms(channel);
            if (validationMessage != null) {
                alertCustomError(this, validationMessage, CustomErrorDialog.ERROR_ENABLING_CHANNEL);
                return;
            }

            setWorking("Enabling channel...", true);

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                public Void doInBackground() {

                    channel.setEnabled(true);
                    try {
                        updateChannel(channel, false);
                    } catch (ClientException e) {
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done() {
                    doRefreshChannels();
                    setWorking("", false);
                }
            };

            worker.execute();

        }
    }

    public void doDisableChannel() {
        List<Channel> selectedChannels = channelPanel.getSelectedChannels();
        if (selectedChannels.size() == 0) {
            alertWarning(this, "Channel no longer exists.");
            return;
        }

        for (final Channel channel : selectedChannels) {
            setWorking("Disabling channel...", true);

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                public Void doInBackground() {
                    channel.setEnabled(false);
                    try {
                        updateChannel(channel, false);
                    } catch (ClientException e) {
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done() {
                    doRefreshChannels();
                    setWorking("", false);
                }
            };

            worker.execute();
        }
    }

    public void doNewUser() {
        new UserDialog(null);
    }

    public void doEditUser() {
        int index = userPanel.getUserIndex();

        if (index == UIConstants.ERROR_CONSTANT) {
            alertWarning(this, "User no longer exists.");
        } else {
            new UserDialog(users.get(index));
        }
    }

    public void doDeleteUser() {
        if (!alertOption(this, "Are you sure you want to delete this user?")) {
            return;
        }

        setWorking("Deleting user...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                if (users.size() == 1) {
                    alertWarning(PlatformUI.MIRTH_FRAME, "You must have at least one user account.");
                    return null;
                }

                int userToDelete = userPanel.getUserIndex();

                try {
                    if (userToDelete != UIConstants.ERROR_CONSTANT) {
                        mirthClient.removeUser(users.get(userToDelete));
                        retrieveUsers();
                    }
                } catch (ClientException e) {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done() {
                userPanel.updateUserTable();
                userPanel.deselectRows();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doRefreshUser() {
        setWorking("Loading users...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                refreshUser();
                return null;
            }

            public void done() {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void refreshUser() {
        User user = null;
        String userName = null;
        int index = userPanel.getUserIndex();

        if (index != UIConstants.ERROR_CONSTANT) {
            user = users.get(index);
        }

        try {
            retrieveUsers();
            userPanel.updateUserTable();

            if (user != null) {
                for (int i = 0; i < users.size(); i++) {
                    if (user.equals(users.get(i))) {
                        userName = users.get(i).getUsername();
                    }
                }
            }
        } catch (ClientException e) {
            alertException(this, e.getStackTrace(), e.getMessage());
        }

        // as long as the channel was not deleted
        if (userName != null) {
            userPanel.setSelectedUser(userName);
        }
    }

    public void doRedeployAll() {
        setWorking("Deploying channels...", true);
        dashboardPanel.deselectRows();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    mirthClient.redeployAllChannels();
                } catch (ClientException e) {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done() {
                setWorking("", false);
                doShowDashboard();
            }
        };

        worker.execute();
    }

    public void doDeployChannel() {
        List<Channel> selectedChannels = channelPanel.getSelectedChannels();
        if (selectedChannels.size() == 0) {
            alertWarning(this, "Channel no longer exists.");
            return;
        }

        // Only deploy enabled channels
        final List<Channel> selectedEnabledChannels = new ArrayList<Channel>();
        boolean channelDisabled = false;
        for (Channel channel : selectedChannels) {
            if (channel.isEnabled()) {
                selectedEnabledChannels.add(channel);
            } else {
                channelDisabled = true;
            }
        }
        
        if (channelDisabled) {
            alertWarning(this, "Disabled channels will not be deployed.");
        }

        String plural = (selectedChannels.size() > 1) ? "s" : "";
        setWorking("Deploying channel" + plural + "...", true);

        dashboardPanel.deselectRows();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    mirthClient.deployChannels(selectedEnabledChannels);
                } catch (ClientException e) {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done() {
                setWorking("", false);
                doShowDashboard();
            }
        };

        worker.execute();
    }

    public void doUndeployChannel() {
        final List<ChannelStatus> selectedChannelStatuses = dashboardPanel.getSelectedStatuses();

        if (selectedChannelStatuses.size() == 0) {
            return;
        }

        dashboardPanel.deselectRows();

        String plural = (selectedChannelStatuses.size() > 1) ? "s" : "";
        setWorking("Undeploying channel" + plural + "...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    List<String> channelIds = new ArrayList<String>();
                    
                    for (ChannelStatus channelStatus : selectedChannelStatuses) {
                        channelIds.add(channelStatus.getChannelId());
                    }
                    
                    mirthClient.undeployChannels(channelIds);
                } catch (ClientException e) {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done() {
                setWorking("", false);
                doRefreshStatuses();
            }
        };

        worker.execute();
    }

    public void doSaveChannel() {
        setWorking("Saving channel...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                if (changesHaveBeenMade() || currentContentPage == channelEditPanel.transformerPane || currentContentPage == channelEditPanel.filterPane) {
                    if (channelEditPanel.saveChanges()) {
                        setSaveEnabled(false);
                    }
                    return null;
                } else {
                    return null;
                }
            }

            public void done() {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public boolean changesHaveBeenMade() {
        if (channelEditPanel != null && currentContentPage == channelEditPanel) {
            return channelEditTasks.getContentPane().getComponent(0).isVisible();
        } else if (channelEditPanel != null && currentContentPage == channelEditPanel.transformerPane) {
            return channelEditPanel.transformerPane.modified;
        } else if (channelEditPanel != null && currentContentPage == channelEditPanel.filterPane) {
            return channelEditPanel.filterPane.modified;
        } else if (settingsPane != null && currentContentPage == settingsPane) {
            return settingsPane.getCurrentSettingsPanel().isSaveEnabled();
        } else if (alertPanel != null && currentContentPage == alertPanel) {
            return alertTasks.getContentPane().getComponent(1).isVisible();
        } else if (globalScriptsPanel != null && currentContentPage == globalScriptsPanel) {
            return globalScriptsTasks.getContentPane().getComponent(0).isVisible();
        } else if (codeTemplatePanel != null && currentContentPage == codeTemplatePanel) {
            return codeTemplateTasks.getContentPane().getComponent(1).isVisible();
        } else {
            return false;
        }
    }

    public void doShowMessages() {
        if (messageBrowser == null) {
            messageBrowser = new MessageBrowser();
        }

        List<ChannelStatus> selectedChannelStatuses = dashboardPanel.getSelectedStatuses();

        if (selectedChannelStatuses.size() == 0) {
            return;
        }

        if (selectedChannelStatuses.size() > 1) {
            JOptionPane.showMessageDialog(Frame.this, "This operation can only be performed on a single Channel.");
            return;
        }

        setBold(viewPane, -1);
        setPanelName("Channel Messages - " + selectedChannelStatuses.get(0).getName());
        setCurrentContentPage(messageBrowser);
        setFocus(messageTasks);

        messageBrowser.loadNew();
    }

    public void doShowEvents() {
        if (!confirmLeave()) {
            return;
        }

        if (eventBrowser == null) {
            eventBrowser = new EventBrowser();
        }

        setBold(viewPane, 5);
        setPanelName("System Events");
        setCurrentContentPage(eventBrowser);
        setFocus(eventTasks);

        eventBrowser.loadNew();
    }

    public void doEditTransformer() {
        channelEditPanel.transformerPane.resizePanes();
        String name = channelEditPanel.editTransformer();
        setPanelName("Edit Channel - " + channelEditPanel.currentChannel.getName() + " - " + name + " Transformer");
    }

    public void doEditFilter() {
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

        ((JXHyperlink) channelEditTasks.getContentPane().getComponent(componentIndex)).setText(taskName);
        ((JMenuItem) channelEditPopupMenu.getComponent(componentIndex)).setText(taskName);
    }

    public void doValidate() {
        channelEditPanel.doValidate();
    }

    public void doImport() {
        File importFile = importFile("XML");

        if (importFile != null) {
            importChannel(importFile, true);
        }
    }

    public void importChannel(File importFile, boolean showAlerts) {
        String channelXML = "";
        boolean overwrite = false;

        try {
            channelXML = ImportConverter.convertChannelFile(importFile);
        } catch (Exception e1) {
            if (showAlerts) {
                alertException(this, e1.getStackTrace(), "Invalid channel file. " + e1.getMessage());
            }
        }

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        Channel importChannel;

        try {
            importChannel = (Channel) serializer.fromXML(channelXML.replaceAll("\\&\\#x0D;\\n", "\n").replaceAll("\\&\\#x0D;", "\n"));
        } catch (Exception e) {
            if (showAlerts) {
                alertException(this, e.getStackTrace(), "Invalid channel file. " + e.getMessage());
            }
            return;
        }

        /**
         * Checks to see if the passed in channel version is current, and
         * prompts the user if it is not.
         */
        if (showAlerts) {
            int option;

            option = JOptionPane.YES_OPTION;
            if (importChannel.getVersion() == null) {
                option = JOptionPane.showConfirmDialog(this, "The channel being imported is from an unknown version of Mirth." + "\nSome channel properties may not be the same.  Would you like to automatically convert the properties?", "Select an Option", JOptionPane.YES_NO_CANCEL_OPTION);
            } else if (!importChannel.getVersion().equals(PlatformUI.SERVER_VERSION)) {
                option = JOptionPane.showConfirmDialog(this, "The channel being imported is from Mirth version " + importChannel.getVersion() + ". You are using Mirth version " + PlatformUI.SERVER_VERSION + ".\nSome channel properties may not be the same.  Would you like to automatically convert the properties?", "Select an Option", JOptionPane.YES_NO_CANCEL_OPTION);
            }

            if (option != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            String channelName = importChannel.getName();
            String tempId = mirthClient.getGuid();

            // Check to see that the channel name doesn't already exist.
            if (!checkChannelName(channelName, tempId)) {
                if (!alertOption(this, "Would you like to overwrite the existing channel?  Choose 'No' to create a new channel.")) {
                    importChannel.setRevision(0);

                    do {
                        channelName = JOptionPane.showInputDialog(this, "Please enter a new name for the channel.", channelName);
                        if (channelName == null) {
                            return;
                        }
                    } while (!checkChannelName(channelName, tempId));

                    importChannel.setName(channelName);
                    importChannel.setId(tempId);
                } else {
                    overwrite = true;

                    for (Channel channel : channels.values()) {
                        if (channel.getName().equalsIgnoreCase(channelName)) {
                            importChannel.setId(channel.getId());
                        }
                    }
                }
            } // If the channel name didn't already exist, make sure the id
            // doesn't exist either.
            else if (!checkChannelId(importChannel.getId())) {
                importChannel.setId(tempId);
            }

            importChannel.setVersion(mirthClient.getVersion());
            channels.put(importChannel.getId(), importChannel);
        } catch (ClientException e) {
            alertException(this, e.getStackTrace(), e.getMessage());
        }

        try {
            if (showAlerts) {
                if (checkInstalledConnectors(importChannel)) {
                    editChannel(importChannel);
                    setSaveEnabled(true);
                }
            } else {
                PropertyVerifier.checkChannelProperties(importChannel);
                PropertyVerifier.checkConnectorProperties(importChannel, getConnectorMetaData());
                updateChannel(importChannel, overwrite);
                doShowChannel();
            }
        } catch (Exception e) {
            channels.remove(importChannel.getId());

            if (showAlerts) {
                alertError(this, "Channel had an unknown problem. Channel import aborted.");
                channelEditPanel = new ChannelSetup();
            }

            doShowChannel();
        }
    }

    public boolean doExport() {
        if (changesHaveBeenMade()) {
            if (alertOption(this, "This channel has been modified. You must save the channel changes before you can export. Would you like to save them now?")) {
                if (!channelEditPanel.saveChanges()) {
                    return false;
                }
            } else {
                return false;
            }

            setSaveEnabled(false);
        }

        Channel channel;
        if (currentContentPage == channelEditPanel || currentContentPage == channelEditPanel.filterPane || currentContentPage == channelEditPanel.transformerPane) {
            channel = channelEditPanel.currentChannel;
        } else {
            List<Channel> selectedChannels = channelPanel.getSelectedChannels();
            if (selectedChannels.size() > 1) {
                JOptionPane.showMessageDialog(Frame.this, "This operation can only be performed on a single Channel.");
                return false;
            }
            channel = selectedChannels.get(0);
        }

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        String channelXML = serializer.toXML(channel);

        return exportFile(channelXML, channel.getName(), "XML", "Channel");
    }

    public void doExportAll() {
        JFileChooser exportFileChooser = new JFileChooser();
        exportFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        File currentDir = new File(userPreferences.get("currentDirectory", ""));
        if (currentDir.exists()) {
            exportFileChooser.setCurrentDirectory(currentDir);
        }

        int returnVal = exportFileChooser.showSaveDialog(this);
        File exportFile = null;
        File exportDirectory = null;

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            userPreferences.put("currentDirectory", exportFileChooser.getCurrentDirectory().getPath());
            try {
                exportDirectory = exportFileChooser.getSelectedFile();

                for (Channel channel : channels.values()) {
                    ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                    String channelXML = serializer.toXML(channel);

                    exportFile = new File(exportDirectory.getAbsolutePath() + "/" + channel.getName() + ".xml");

                    if (exportFile.exists()) {
                        if (!alertOption(this, "The file " + channel.getName() + ".xml already exists.  Would you like to overwrite it?")) {
                            continue;
                        }
                    }

                    FileUtils.writeStringToFile(exportFile, channelXML, UIConstants.CHARSET);
                }
                alertInformation(this, "All files were written successfully to " + exportDirectory.getPath() + ".");
            } catch (IOException ex) {
                alertError(this, "File could not be written.");
            }
        }
    }

    /**
     * Import a file with the default defined file filter type.
     * @return
     */
    public File importFile(String type) {
        JFileChooser importFileChooser = new JFileChooser();
        if (type != null) {
            importFileChooser.setFileFilter(new MirthFileFilter(type));
        }

        File currentDir = new File(userPreferences.get("currentDirectory", ""));
        if (currentDir.exists()) {
            importFileChooser.setCurrentDirectory(currentDir);
        }

        int returnVal = importFileChooser.showOpenDialog(this);
        File importFile = null;

        if (returnVal == JFileChooser.APPROVE_OPTION) {
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
    public boolean exportFile(String fileContents, String defaultFileName, String type, String name) {
        JFileChooser exportFileChooser = new JFileChooser();

        if (defaultFileName != null) {
            exportFileChooser.setSelectedFile(new File(defaultFileName));
        }
        if (type != null) {
            exportFileChooser.setFileFilter(new MirthFileFilter(type));
        }

        File currentDir = new File(userPreferences.get("currentDirectory", ""));
        if (currentDir.exists()) {
            exportFileChooser.setCurrentDirectory(currentDir);
        }

        int returnVal = exportFileChooser.showSaveDialog(this);
        File exportFile = null;

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            userPreferences.put("currentDirectory", exportFileChooser.getCurrentDirectory().getPath());
            exportFile = exportFileChooser.getSelectedFile();

            int length = exportFile.getName().length();

            if (length < 4 || !exportFile.getName().substring(length - 4, length).equalsIgnoreCase("." + type.toLowerCase())) {
                exportFile = new File(exportFile.getAbsolutePath() + "." + type.toLowerCase());
            }

            if (exportFile.exists()) {
                if (!alertOption(this, "This file already exists.  Would you like to overwrite it?")) {
                    return false;
                }
            }

            try {
                FileUtils.writeStringToFile(exportFile, fileContents, UIConstants.CHARSET);
                alertInformation(this, name + " was written to " + exportFile.getPath() + ".");
            } catch (IOException ex) {
                alertError(this, "File could not be written.");
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public void doImportConnector() {
        File connectorFile = importFile("XML");

        if (connectorFile != null) {
            try {
                String connectorXML = ImportConverter.convertConnector(FileUtils.readFileToString(connectorFile, UIConstants.CHARSET));
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                Connector connector = (Connector) serializer.fromXML(connectorXML);
                PropertyVerifier.checkConnectorProperties(connector, getConnectorMetaData());
                channelEditPanel.importConnector(connector);
            } catch (Exception e) {
                alertException(this, e.getStackTrace(), e.getMessage());
            }
        }
    }

    public void doExportConnector() {
        if (changesHaveBeenMade()) {
            if (alertOption(this, "This channel has been modified. You must save the channel changes before you can export. Would you like to save them now?")) {
                if (!channelEditPanel.saveChanges()) {
                    return;
                }
            } else {
                return;
            }

            setSaveEnabled(false);
        }

        Connector connector = channelEditPanel.exportSelectedConnector();

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        String connectorXML = serializer.toXML(connector);

        String fileName = channelEditPanel.currentChannel.getName();
        if (connector.getMode().equals(Mode.SOURCE)) {
            fileName += " Source";
        } else {
            fileName += " " + connector.getName();
        }

        exportFile(connectorXML, fileName, "XML", "Connector");
    }

    public void doCloneChannel() {
        List<Channel> selectedChannels = channelPanel.getSelectedChannels();
        if (selectedChannels.size() > 1) {
            JOptionPane.showMessageDialog(Frame.this, "This operation can only be performed on a single Channel.");
            return;
        }

        Channel channel = null;
        try {
            channel = (Channel) SerializationUtils.clone(selectedChannels.get(0));
        } catch (SerializationException e) {
            alertException(this, e.getStackTrace(), e.getMessage());
            return;
        }

        try {
            channel.setRevision(0);
            channel.setId(mirthClient.getGuid());
        } catch (ClientException e) {
            alertException(this, e.getStackTrace(), e.getMessage());
        }

        String channelName = channel.getName();
        do {
            channelName = JOptionPane.showInputDialog(this, "Please enter a new name for the channel.", channelName);
            if (channelName == null) {
                return;
            }
        } while (!checkChannelName(channelName, channel.getId()));

        channel.setName(channelName);
        channels.put(channel.getId(), channel);

        editChannel(channel);
        setSaveEnabled(true);
    }

    public void doRefreshMessages() {
        setWorking("Loading messages...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                messageBrowser.refresh();
                return null;
            }

            public void done() {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doSendMessage() {
        try {
            retrieveChannels();

            List<ChannelStatus> selectedChannelStatuses = dashboardPanel.getSelectedStatuses();

            if (selectedChannelStatuses.size() == 0) {
                return;
            }

            if (selectedChannelStatuses.size() > 1) {
                JOptionPane.showMessageDialog(Frame.this, "This operation can only be performed on a single Channel.");
                return;
            }

            Channel channel = channels.get(selectedChannelStatuses.get(0).getChannelId());

            if (channel == null) {
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
        } catch (ClientException e) {
            alertException(this, e.getStackTrace(), e.getMessage());
        }
    }

    public void doImportMessages() {
        setWorking("Importing messages...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                messageBrowser.importMessages();
                return null;
            }

            public void done() {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doExportMessages() {
        setWorking("Exporting messages...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                messageBrowser.exportMessages();
                return null;
            }

            public void done() {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doRemoveAllMessages() {
        if (alertOption(this, "Are you sure you would like to remove all messages in the selected channel(s)?")) {
            final boolean clearStats = alertOption(PlatformUI.MIRTH_FRAME, "Would you also like to clear all statistics?");

            List<ChannelStatus> selectedChannelStatuses = dashboardPanel.getSelectedStatuses();

            for (final ChannelStatus channelStatus : selectedChannelStatuses) {

                setWorking("Removing messages...", true);

                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                    public Void doInBackground() {
                        try {
                            mirthClient.clearMessages(channelStatus.getChannelId());
                        } catch (ClientException e) {
                            alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                        }
                        return null;
                    }

                    public void done() {
                        if (currentContentPage == dashboardPanel) {
                            if (clearStats) {
                                List<ChannelStatus> channelStatuses = new ArrayList<ChannelStatus>();
                                channelStatuses.add(channelStatus);
                                clearStats(channelStatuses, true, true, true, true, true, true);
                            }
                            doRefreshStatuses();
                        } else if (currentContentPage == messageBrowser) {
                            messageBrowser.refresh();
                        }
                        setWorking("", false);
                    }
                };

                worker.execute();
            }
        }
    }

    public void doClearStats() {
        List<ChannelStatus> channelStatuses = dashboardPanel.getSelectedStatuses();

        if (channelStatuses.size() != 0) {
            new DeleteStatisticsDialog(channelStatuses);
        } else {
            dashboardPanel.deselectRows();
        }
    }

    public void clearStats(final List<ChannelStatus> statusesToClear, final boolean deleteReceived, final boolean deleteFiltered, final boolean deleteQueued, final boolean deleteSent, final boolean deleteErrored, final boolean deleteAlerted) {
        setWorking("Clearing statistics...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    for (ChannelStatus channelStatus : statusesToClear) {
                        mirthClient.clearStatistics(channelStatus.getChannelId(), deleteReceived, deleteFiltered, deleteQueued, deleteSent, deleteErrored, deleteAlerted);
                    }
                } catch (ClientException e) {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done() {
                doRefreshStatuses();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doRemoveFilteredMessages() {
        if (alertOption(this, "Are you sure you would like to remove all currently filtered messages in this channel?")) {
            setWorking("Removing messages...", true);

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                public Void doInBackground() {
                    try {
                        mirthClient.removeMessages(messageBrowser.getCurrentFilter());
                    } catch (ClientException e) {
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done() {
                    if (currentContentPage == dashboardPanel) {
                        doRefreshStatuses();
                    } else if (currentContentPage == messageBrowser) {
                        messageBrowser.refresh();
                    }
                    setWorking("", false);
                }
            };

            worker.execute();
        }
    }

    public void doRemoveMessage() {
        if (alertOption(this, "Are you sure you would like to remove the selected message?")) {
            setWorking("Removing message...", true);

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                public Void doInBackground() {
                    try {
                        MessageObjectFilter filter = new MessageObjectFilter();
                        filter.setId(messageBrowser.getSelectedMessageID());
                        mirthClient.removeMessages(filter);
                    } catch (ClientException e) {
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done() {
                    if (currentContentPage == dashboardPanel) {
                        doRefreshStatuses();
                    } else if (currentContentPage == messageBrowser) {
                        messageBrowser.refresh();
                    }
                    setWorking("", false);
                }
            };

            worker.execute();
        }
    }

    public void doReprocessFilteredMessages() {
        setWorking("Retrieving Channels...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                if (channels == null || channels.values().size() == 0) {
                    retrieveChannels();
                }

                return null;
            }

            public void done() {
                setWorking("", false);
                new ReprocessMessagesDialog(messageBrowser.getCurrentFilter());
            }
        };
        worker.execute();
    }

    public void doReprocessMessage() {
        setWorking("Retrieving Channels...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                if (channels == null || channels.values().size() == 0) {
                    retrieveChannels();
                }

                return null;
            }

            public void done() {
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

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    mirthClient.reprocessMessages(filter, replace, destinations);
                } catch (ClientException e) {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done() {
                messageBrowser.refresh();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void viewImage() {
        setWorking("Opening attachment...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                messageBrowser.viewAttachment();
                setWorking("", false);
                return null;
            }

            public void done() {
                setWorking("", false);
            }
        };
        worker.execute();
    }

    public void processMessage(final MessageObject message) {
        setWorking("Processing message...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    mirthClient.processMessage(message);
                } catch (ClientException e) {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done() {
                messageBrowser.refresh();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doRefreshEvents() {
        eventBrowser.refresh();
    }

    public void doRemoveAllEvents() {
        if (alertOption(this, "Are you sure you would like to clear all system events?")) {
            setWorking("Clearing events...", true);

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                public Void doInBackground() {
                    try {
                        mirthClient.removeAllEvents();
                    } catch (ClientException e) {
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                public void done() {
                    eventBrowser.refresh();
                    setWorking("", false);
                }
            };

            worker.execute();
        }
    }

    public void doRefreshAlerts() {
        setWorking("Loading alerts...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                refreshAlerts();
                return null;
            }

            public void done() {
                alertPanel.updateAlertTable();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void refreshAlerts() {
        try {
            alerts = mirthClient.getAlert(null);
        } catch (ClientException e) {
            alertException(this, e.getStackTrace(), e.getMessage());
        }
    }

    public void doSaveAlerts() {
        setWorking("Saving alerts...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                saveAlerts();
                return null;
            }

            public void done() {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public boolean saveAlerts() {
        try {
            Properties serverProperties = mirthClient.getServerProperties();
            if (!(serverProperties.getProperty("smtp.host") != null && (serverProperties.getProperty("smtp.host")).length() > 0) || !(serverProperties.getProperty("smtp.port") != null && (serverProperties.getProperty("smtp.port")).length() > 0)) {
                alertWarning(PlatformUI.MIRTH_FRAME, "The SMTP server on the settings page is not specified or is incomplete.  An SMTP server is required to send alerts.");
            }

            alertPanel.saveAlert();
            mirthClient.updateAlerts(alerts);
            setSaveEnabled(false);
        } catch (ClientException e) {
            alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
            return false;
        }
        return true;
    }

    public void doDeleteAlert() {
        alertPanel.deleteAlert();
    }

    public void doNewAlert() {
        alertPanel.addAlert();
    }

    public void doEnableAlert() {
        alertPanel.enableAlert();
    }

    public void doDisableAlert() {
        alertPanel.disableAlert();
    }

    public void doExportAlerts() {
        if (changesHaveBeenMade()) {
            if (alertOption(this, "Would you like to save the changes made to the alerts?")) {
                saveAlerts();
            } else {
                return;
            }
        }

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        String alertXML = serializer.toXML(alerts);

        exportFile(alertXML, null, "XML", "Alerts export");
    }

    public void doImportAlerts() {
        File importFile = importFile("XML");

        if (importFile != null) {
            try {
                String alertsXml = FileUtils.readFileToString(importFile, UIConstants.CHARSET);
                alertsXml = ImportConverter.convertAlerts(alertsXml);
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                
                boolean append = false;

                List<Alert> newAlerts = (List<Alert>) serializer.fromXML(alertsXml);
                if (alerts != null && alerts.size() > 0) {
                    if (alertOption(this, "Would you like to append these alerts to the existing alerts?")) {
                        append = true;
                    }
                }

                if (append) {
                    for (Alert newAlert : newAlerts) {
                        newAlert.setId(UUID.randomUUID().toString());

                        // make sure the name doesn't already exist
                        for (Alert alert : alerts) {
                            // If the name already exists, generate a new unique name
                            if (alert.getName().equalsIgnoreCase(newAlert.getName())) {
                                String newAlertName = "Alert ";

                                boolean uniqueName = false;
                                int i = 0;
                                while (!uniqueName) {
                                    i++;
                                    uniqueName = true;
                                    for (Alert alertLookup : alerts) {
                                        if (alertLookup.getName().equalsIgnoreCase(newAlertName + i)) {
                                            uniqueName = false;
                                        }
                                    }
                                }

                                newAlert.setName(newAlertName + i);
                            }
                        }

                        alerts.add(newAlert);
                    }
                } else {
                    alerts = newAlerts;
                }

                alertInformation(this, "All alerts imported successfully.");

                setSaveEnabled(true);
                
                // If appending, just deselect the rows, which saves 
                // the state of the last selected row.
                // If replacing, set isDeletingAlert so the state is 
                // not saved while the alert is being removed.
                if (append) {
                    alertPanel.deselectAlertRows();
                } else {
                    alertPanel.isDeletingAlert = true;
                    alertPanel.deselectAlertRows();
                    alertPanel.isDeletingAlert = false;
                }
                
                alertPanel.updateAlertTable();

            } catch (Exception e) {
                alertError(this, "Invalid alert file.");
            }
        }
    }

    public void doRefreshCodeTemplates() {
        setWorking("Loading code templates...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                refreshCodeTemplates();
                return null;
            }

            public void done() {
                codeTemplatePanel.updateCodeTemplateTable();
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void refreshCodeTemplates() {
        try {
            codeTemplates = mirthClient.getCodeTemplate(null);
        } catch (ClientException e) {
            // If the user is unauthorized and it's the first time (startup, when
            // codeTemplates is null), then initialize the code templates.
            if (e.getCause() instanceof UnauthorizedException && codeTemplates == null) {
                codeTemplates = new ArrayList<CodeTemplate>();
            } else {
                alertException(this, e.getStackTrace(), e.getMessage());
            }
        }
    }

    public void doSaveCodeTemplates() {
        setWorking("Saving codeTemplates...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                saveCodeTemplates();
                return null;
            }

            public void done() {
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public boolean saveCodeTemplates() {
        try {
            codeTemplatePanel.saveCodeTemplate();

            for (CodeTemplate template : codeTemplates) {
                if (template.getType() == CodeSnippetType.FUNCTION) {
                    if (!codeTemplatePanel.validateCodeTemplate(template.getCode(), false, template.getName())) {
                        return false;
                    }
                }
            }
            mirthClient.updateCodeTemplates(codeTemplates);
            ReferenceListFactory.getInstance().updateUserTemplates();
            setSaveEnabled(false);
        } catch (ClientException e) {
            alertException(this, e.getStackTrace(), e.getMessage());
            return false;
        }
        return true;
    }

    public void doDeleteCodeTemplate() {
        codeTemplatePanel.deleteCodeTemplate();
    }

    public void doValidateCodeTemplate() {
        codeTemplatePanel.validateCodeTemplate();
    }

    public void doNewCodeTemplate() {
        codeTemplatePanel.addCodeTemplate();
    }

    public void doExportCodeTemplates() {
        if (changesHaveBeenMade()) {
            if (alertOption(this, "Would you like to save the changes made to the code templates?")) {
                saveCodeTemplates();
            } else {
                return;
            }
        }

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        String codeTemplateXML = serializer.toXML(codeTemplates);

        exportFile(codeTemplateXML, null, "XML", "Code templates export");
    }

    public void doImportCodeTemplates() {
        File importFile = importFile("XML");

        if (importFile != null) {
            try {
                String codeTemplatesXML = FileUtils.readFileToString(importFile, UIConstants.CHARSET);
                codeTemplatesXML = ImportConverter.convertCodeTemplates(codeTemplatesXML);
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();

                boolean append = false;

                List<CodeTemplate> newCodeTemplates = (List<CodeTemplate>) serializer.fromXML(codeTemplatesXML);
                if (codeTemplates != null && codeTemplates.size() > 0) {
                    if (alertOption(this, "Would you like to append these code templates to the existing code templates?")) {
                        append = true;
                    }
                }

                if (append) {
                    for (CodeTemplate newCodeTemplate : newCodeTemplates) {
                        newCodeTemplate.setId(UUID.randomUUID().toString());

                        // make sure the name doesn't already exist
                        for (CodeTemplate codeTemplate : codeTemplates) {
                            // If the name already exists, generate a new unique name
                            if (codeTemplate.getName().equalsIgnoreCase(newCodeTemplate.getName())) {
                                String newCodeTemplateName = "Template ";

                                boolean uniqueName = false;
                                int i = 0;
                                while (!uniqueName) {
                                    i++;
                                    uniqueName = true;
                                    for (CodeTemplate codeTemplateLookup : codeTemplates) {
                                        if (codeTemplateLookup.getName().equalsIgnoreCase(newCodeTemplateName + i)) {
                                            uniqueName = false;
                                        }
                                    }
                                }

                                newCodeTemplate.setName(newCodeTemplateName + i);
                            }
                        }

                        codeTemplates.add(newCodeTemplate);
                    }
                } else {
                    codeTemplates = newCodeTemplates;
                }

                alertInformation(this, "All code templates imported successfully.");

                setSaveEnabled(true);
                
                // If appending, just deselect the rows, which saves 
                // the state of the last selected row.
                // If replacing, set isDeletingAlert so the state is 
                // not saved while the alert is being removed.
                if (append) {
                    codeTemplatePanel.deselectCodeTemplateRows();
                } else {
                    codeTemplatePanel.isDeleting = true;
                    codeTemplatePanel.deselectCodeTemplateRows();
                    codeTemplatePanel.isDeleting = false;
                }
                
                codeTemplatePanel.updateCodeTemplateTable();
            } catch (Exception e) {
                alertError(this, "Invalid code template file.");
            }
        }
    }

    ///// Start Extension Tasks /////
    public void doRefreshExtensions() {
        setWorking("Loading extension settings...", true);

        if (confirmLeave()) {
            refreshExtensions();
        }

        setWorking("", false);
    }
    
    public void refreshExtensions() {
        extensionsPanel.setPluginData(getPluginMetaData());
        extensionsPanel.setConnectorData(getConnectorMetaData());
    }
    
    public void doSaveExtensions() {
        setWorking("Saving extension settings...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    // Save the settings on the extensions panel
                    extensionsPanel.savePluginData();
                    extensionsPanel.saveConnectorData();

                    // Save the meta data to the server
                    mirthClient.setPluginMetaData(getPluginMetaData());
                    mirthClient.setConnectorMetaData(getConnectorMetaData());
                } catch (ClientException e) {
                    alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done() {
                setSaveEnabled(false);
                setWorking("", false);
                alertInformation(PlatformUI.MIRTH_FRAME, "A restart is required before your changes will take effect.");
            }
        };

        worker.execute();
    }
    
    public void doCheckForUpdates() {
        try {
            new ExtensionUpdateDialog();
        } catch (ClientException e) {
            alertException(this, e.getStackTrace(), e.getMessage());
        }
    }
    
    public void doEnableExtension() {
        extensionsPanel.setSelectedExtensionEnabled(true);
        setSaveEnabled(true);
    }
    
    public void doDisableExtension() {
        extensionsPanel.setSelectedExtensionEnabled(false);
        setSaveEnabled(true);
    }
    
    public void doShowExtensionProperties() {
        extensionsPanel.showExtensionProperties();
    }
    
    public void doUninstallExtension() {
        setWorking("Uninstalling extension...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                String packageName = extensionsPanel.getSelectedExtension().getPath();

                if (alertOkCancel(PlatformUI.MIRTH_FRAME, "Uninstalling this extension will remove all plugins and/or connectors\nin the following extension folder: " + packageName)) {
                    try {
                        mirthClient.uninstallExtension(packageName);
                    } catch (ClientException e) {
                        alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
                    }
                    
                    alertInformation(PlatformUI.MIRTH_FRAME, "The Mirth Connect server must be restarted for the extension(s) to be uninstalled.");
                }

                return null;
            }

            public void done() {
                setWorking("", false);
            }
        };

        worker.execute();
    }
    
    public boolean installExtension(File file) {
        try {
            if (file.exists()) {
                mirthClient.installExtension(file);
            } else {
                alertError(this, "Invalid extension file.");
                return false;
            }
        } catch (Exception e) {
            String errorMessage = "Unable to install extension.";
            try {
                String tempErrorMessage = java.net.URLDecoder.decode(e.getMessage(), "UTF-8");
                String versionError = "VersionMismatchException: ";
                int messageIndex = tempErrorMessage.indexOf(versionError);

                if (messageIndex != -1) {
                    errorMessage = tempErrorMessage.substring(messageIndex + versionError.length());
                }

            } catch (UnsupportedEncodingException e1) {
                alertException(this, e1.getStackTrace(), e1.getMessage());
            }

            alertError(this, errorMessage);

            return false;
        }
        return true;
    }

    public void finishExtensionInstall() {
        alertInformation(this, "The Mirth Connect server must be restarted for the extension(s) to load.");
    }
    ///// End Extension Tasks /////
    
    public boolean exportChannelOnError() {
        if (isSaveEnabled()) {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the channel changes locally to your computer?");
            if (option == JOptionPane.YES_OPTION) {
                if (!channelEditPanel.saveChanges()) {
                    return false;
                }

                boolean enabled = isSaveEnabled();
                setSaveEnabled(false);
                if (!doExport()) {
                    setSaveEnabled(enabled);
                    return false;
                }
            } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return false;
            } else {
                setSaveEnabled(false);
            }
        }
        return true;
    }

    public void doContextSensitiveSave() {
        if (currentContentPage == channelEditPanel) {
            doSaveChannel();
        } else if (currentContentPage == channelEditPanel.filterPane) {
            doSaveChannel();
        } else if (currentContentPage == channelEditPanel.transformerPane) {
            doSaveChannel();
        } else if (currentContentPage == globalScriptsPanel) {
            doSaveGlobalScripts();
        } else if (currentContentPage == codeTemplatePanel) {
            doSaveCodeTemplates();
        } else if (currentContentPage == settingsPane) {
            settingsPane.getCurrentSettingsPanel().doSave();
        } else if (currentContentPage == alertPanel) {
            doSaveAlerts();
        } else if (currentContentPage == extensionsPanel) {
            doSaveExtensions();
        }
    }
    
    public void doFind(JEditTextArea text) {
        FindRplDialog find;
        Window owner = getWindowForComponent(text);

        if (owner instanceof java.awt.Frame) {
            find = new FindRplDialog((java.awt.Frame) owner, true, text);
        } else { // window instanceof Dialog
            find = new FindRplDialog((java.awt.Dialog) owner, true, text);
        }

        find.setVisible(true);
    }

    public void doHelp() {
        if (currentContentPage == channelEditPanel) {
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.CHANNEL_HELP_LOCATION);
        } else if (currentContentPage == channelPanel) {
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.CHANNELS_HELP_LOCATION);
        } else if (currentContentPage == dashboardPanel) {
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.DASHBOARD_HELP_LOCATION);
        } else if (currentContentPage == messageBrowser) {
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.MESSAGE_BROWSER_HELP_LOCATION);
        } else if (currentContentPage == eventBrowser) {
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.SYSTEM_EVENT_HELP_LOCATION);
        } else if (currentContentPage == settingsPane) {
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.SETTINGS_HELP_LOCATION);
        } else if (currentContentPage == channelEditPanel.transformerPane) {
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.TRANFORMER_HELP_LOCATION);
        } else if (currentContentPage == channelEditPanel.filterPane) {
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.FILTER_HELP_LOCATION);
        } else if (currentContentPage == extensionsPanel) {
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.EXTENSIONS_HELP_LOCATION);
        } else if (currentContentPage == alertPanel) {
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.ALERTS_HELP_LOCATION);
        } else if (currentContentPage == userPanel) {
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION + UIConstants.USERS_HELP_LOCATION);
        } else {
            BareBonesBrowserLaunch.openURL(PlatformUI.HELP_LOCATION);
        }
    }

    public Map<String, PluginMetaData> getPluginMetaData() {
        return this.loadedPlugins;
    }

    public Map<String, ConnectorMetaData> getConnectorMetaData() {
        return this.loadedConnectors;
    }

    public String getSelectedChannelIdFromDashboard() {
        return dashboardPanel.getSelectedStatuses().get(0).getChannelId();
    }
    
    public Channel getSelectedChannelFromDashboard() {
        retrieveChannels();
        return channels.get(getSelectedChannelIdFromDashboard());
    }

    public PasswordRequirements getPasswordRequirements() throws ClientException {
        return mirthClient.getPasswordRequirements();
    }

    public void retrieveUsers() throws ClientException {
        users = mirthClient.getUser(null);
    }

    public boolean isAcceleratorKeyPressed() {
        return isAcceleratorKeyPressed;
    }
}

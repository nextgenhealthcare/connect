/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
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
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.log4j.Logger;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
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
import com.mirth.connect.client.core.ConnectServiceUtil;
import com.mirth.connect.client.core.ForbiddenException;
import com.mirth.connect.client.core.RequestAbortedException;
import com.mirth.connect.client.core.TaskConstants;
import com.mirth.connect.client.core.UnauthorizedException;
import com.mirth.connect.client.core.Version;
import com.mirth.connect.client.core.VersionMismatchException;
import com.mirth.connect.client.ui.DashboardPanel.TableState;
import com.mirth.connect.client.ui.alert.AlertEditPanel;
import com.mirth.connect.client.ui.alert.AlertPanel;
import com.mirth.connect.client.ui.alert.DefaultAlertEditPanel;
import com.mirth.connect.client.ui.alert.DefaultAlertPanel;
import com.mirth.connect.client.ui.browsers.event.EventBrowser;
import com.mirth.connect.client.ui.browsers.message.MessageBrowser;
import com.mirth.connect.client.ui.codetemplate.CodeTemplatePanel;
import com.mirth.connect.client.ui.components.rsta.ac.js.MirthJavaScriptLanguageSupport;
import com.mirth.connect.client.ui.dependencies.ChannelDependenciesWarningDialog;
import com.mirth.connect.client.ui.extensionmanager.ExtensionManagerPanel;
import com.mirth.connect.client.ui.tag.SettingsPanelTags;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.SourceConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.model.ApiProvider;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.DashboardChannelInfo;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.DashboardStatus.StatusType;
import com.mirth.connect.model.EncryptionSettings;
import com.mirth.connect.model.InvalidChannel;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.ResourceProperties;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.User;
import com.mirth.connect.model.alert.AlertInfo;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.alert.AlertStatus;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.plugins.DashboardColumnPlugin;
import com.mirth.connect.plugins.DataTypeClientPlugin;
import com.mirth.connect.util.ChannelDependencyException;
import com.mirth.connect.util.ChannelDependencyGraph;
import com.mirth.connect.util.DirectedAcyclicGraphNode;
import com.mirth.connect.util.MigrationUtil;

/**
 * The main content frame for the Mirth Client Application. Extends JXFrame and sets up all content.
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
    public AlertEditPanel alertEditPanel = null;
    public CodeTemplatePanel codeTemplatePanel = null;
    public GlobalScriptsPanel globalScriptsPanel = null;
    public ExtensionManagerPanel extensionsPanel = null;
    public JXTaskPaneContainer taskPaneContainer;
    public List<DashboardStatus> status = null;
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
    public JScrollPane container;
    public EditMessageDialog editMessageDialog = null;

    // Task panes and popup menus
    public JXTaskPane viewPane;
    public JXTaskPane otherPane;
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
    public JXTaskPane alertEditTasks;
    public JPopupMenu alertEditPopupMenu;
    public JXTaskPane globalScriptsTasks;
    public JPopupMenu globalScriptsPopupMenu;
    public JXTaskPane extensionsTasks;
    public JPopupMenu extensionsPopupMenu;

    public JXTitledPanel rightContainer;
    private ExecutorService statusUpdaterExecutor = Executors.newSingleThreadExecutor();
    private Future<?> statusUpdaterJob = null;
    public static Preferences userPreferences;
    private boolean connectionError;
    private ArrayList<CharsetEncodingInformation> availableCharsetEncodings = null;
    private List<String> charsetEncodings = null;
    public boolean isEditingChannel = false;
    private boolean isEditingAlert = false;
    private LinkedHashMap<String, String> workingStatuses = new LinkedHashMap<String, String>();
    public LinkedHashMap<String, String> dataTypeToDisplayName;
    public LinkedHashMap<String, String> displayNameToDataType;
    private Map<String, PluginMetaData> loadedPlugins;
    private Map<String, ConnectorMetaData> loadedConnectors;
    private Map<String, Integer> safeErrorFailCountMap = new HashMap<String, Integer>();
    private Map<Component, String> componentTaskMap = new HashMap<Component, String>();
    private boolean acceleratorKeyPressed = false;
    private boolean canSave = true;
    private RemoveMessagesDialog removeMessagesDialog;
    private MessageExportDialog messageExportDialog;
    private MessageImportDialog messageImportDialog;
    private AttachmentExportDialog attachmentExportDialog;
    private KeyEventDispatcher keyEventDispatcher = null;
    private int deployedChannelCount;

    private static final int REFRESH_BLOCK_SIZE = 100;

    public Frame() {
        Platform.setImplicitExit(false);

        // Load RSyntaxTextArea language support
        LanguageSupportFactory.get().addLanguageSupport(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT, MirthJavaScriptLanguageSupport.class.getName());

        rightContainer = new JXTitledPanel();

        taskPaneContainer = new JXTaskPaneContainer();

        StringBuilder titleText = new StringBuilder();

        if (!StringUtils.isBlank(PlatformUI.SERVER_NAME)) {
            titleText.append(PlatformUI.SERVER_NAME);
        } else {
            titleText.append(PlatformUI.SERVER_URL);
        }

        titleText.append(" - " + UIConstants.TITLE_TEXT);

        setTitle(titleText.toString());
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

            public void componentHidden(ComponentEvent e) {}

            public void componentShown(ComponentEvent e) {}

            public void componentMoved(ComponentEvent e) {}
        });

        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                if (logout(true)) {
                    System.exit(0);
                }
            }
        });

        keyEventDispatcher = new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                // Update the state of the accelerator key (CTRL on Windows)
                updateAcceleratorKeyPressed(e);
                return false;
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher);
    }

    /**
     * Prepares the list of the encodings. This method is called from the Frame class.
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
     * Sets the combobox for the string previously selected. If the server can't support the
     * encoding, the default one is selected. This method is called from each connector.
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
    public void setupFrame(Client mirthClient) throws ClientException {

        LoginPanel login = LoginPanel.getInstance();

        // Initialize the send message dialog
        editMessageDialog = new EditMessageDialog();

        this.mirthClient = mirthClient;
        login.setStatus("Loading extensions...");
        try {
            loadExtensionMetaData();
        } catch (ClientException e) {
            alertError(this, "Unable to load extensions.");
            throw e;
        }

        // Re-initialize the controller every time the frame is setup
        AuthorizationControllerFactory.getAuthorizationController().initialize();
        channelPanel = new ChannelPanel();
        channelPanel.retrieveGroups();
        channelPanel.retrieveDependencies();

        // Initialize all of the extensions now that the metadata has been retrieved
        // Make sure to initialize before the code template panel is created because it needs extensions
        LoadedExtensions.getInstance().initialize();

        codeTemplatePanel = new CodeTemplatePanel(this);

        // Now it's okay to start the plugins
        LoadedExtensions.getInstance().startPlugins();

        mirthClient.setRecorder(LoadedExtensions.getInstance().getRecorder());

        statusBar = new StatusBar();
        statusBar.setBorder(BorderFactory.createEmptyBorder());

        channelPanel.initPanelPlugins();

        // Load the data type/display name maps now that the extensions have been loaded.
        dataTypeToDisplayName = new LinkedHashMap<String, String>();
        displayNameToDataType = new LinkedHashMap<String, String>();
        for (Entry<String, DataTypeClientPlugin> entry : LoadedExtensions.getInstance().getDataTypePlugins().entrySet()) {
            dataTypeToDisplayName.put(entry.getKey(), entry.getValue().getDisplayName());
            displayNameToDataType.put(entry.getValue().getDisplayName(), entry.getKey());
        }

        setInitialVisibleTasks();
        login.setStatus("Loading preferences...");
        userPreferences = Preferences.userNodeForPackage(Mirth.class);
        userPreferences.put("defaultServer", PlatformUI.SERVER_URL);
        login.setStatus("Loading GUI components...");
        splitPane.setDividerSize(0);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        contentPanel = (JPanel) getContentPane();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder());
        taskPane.setBorder(BorderFactory.createEmptyBorder());

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
            PlatformUI.SERVER_TIME = mirthClient.getServerTime();

            setTitle(getTitle() + " - (" + PlatformUI.SERVER_VERSION + ")");

            PlatformUI.BUILD_DATE = mirthClient.getBuildDate();

            // Initialize ObjectXMLSerializer once we know the server version
            try {
                ObjectXMLSerializer.getInstance().init(PlatformUI.SERVER_VERSION);
            } catch (Exception e) {
            }
        } catch (ClientException e) {
            alertError(this, "Could not get server information.");
        }

        // Display the server timezone information
        statusBar.setTimezoneText(PlatformUI.SERVER_TIMEZONE);
        statusBar.setServerTime(PlatformUI.SERVER_TIME);

        // Refresh resources and tags
        if (settingsPane == null) {
            settingsPane = new SettingsPane();
        }

        SettingsPanelResources resourcesPanel = (SettingsPanelResources) settingsPane.getSettingsPanel(SettingsPanelResources.TAB_NAME);
        if (resourcesPanel != null) {
            resourcesPanel.doRefresh();
        }

        SettingsPanelTags tagsPanel = (SettingsPanelTags) settingsPane.getSettingsPanel(SettingsPanelTags.TAB_NAME);
        if (tagsPanel != null) {
            tagsPanel.doRefresh();
        }

        setCurrentTaskPaneContainer(taskPaneContainer);
        login.setStatus("Loading dashboard...");
        doShowDashboard();
        login.setStatus("Loading channel editor...");
        channelEditPanel = new ChannelSetup();
        login.setStatus("Loading alert editor...");
        if (alertEditPanel == null) {
            alertEditPanel = new DefaultAlertEditPanel();
        }
        login.setStatus("Loading message browser...");
        messageBrowser = new MessageBrowser();

        // Refresh code templates after extensions have been loaded
        codeTemplatePanel.doRefreshCodeTemplates(false);

        // DEBUGGING THE UIDefaults:

//         UIDefaults uiDefaults = UIManager.getDefaults(); Enumeration enum1 =
//         uiDefaults.keys(); while (enum1.hasMoreElements()) { Object key =
//         enum1.nextElement(); Object val = uiDefaults.get(key);
////         if(key.toString().indexOf("ComboBox") != -1)
//         System.out.println("UIManager.put(\"" + key.toString() + "\",\"" +
//         (null != val ? val.toString() : "(null)") + "\");"); }

    }

    @Override
    public void dispose() {
        super.dispose();
        if (statusBar != null) {
            statusBar.shutdown();
        }
    }

    private void loadExtensionMetaData() throws ClientException {
        loadedPlugins = mirthClient.getPluginMetaData();
        loadedConnectors = mirthClient.getConnectorMetaData();

        // Register extension JAX-RS providers with the client
        Set<String> apiProviderPackages = new HashSet<String>();
        Set<String> apiProviderClasses = new HashSet<String>();

        for (Object extensionMetaData : CollectionUtils.union(loadedPlugins.values(), loadedConnectors.values())) {
            MetaData metaData = (MetaData) extensionMetaData;
            for (ApiProvider provider : metaData.getApiProviders(Version.getLatest())) {
                switch (provider.getType()) {
                    case SERVLET_INTERFACE_PACKAGE:
                    case CORE_PACKAGE:
                        apiProviderPackages.add(provider.getName());
                        break;
                    case SERVLET_INTERFACE:
                    case CORE_CLASS:
                        apiProviderClasses.add(provider.getName());
                        break;
                    default:
                }
            }
        }

        mirthClient.registerApiProviders(apiProviderPackages, apiProviderClasses);
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

    public String startWorking(final String displayText) {
        String id = null;

        synchronized (workingStatuses) {
            if (statusBar != null) {
                id = UUID.randomUUID().toString();
                workingStatuses.put(id, displayText);
                statusBar.setWorking(true);
                statusBar.setText(displayText);
            }
        }

        return id;
    }

    public void stopWorking(final String workingId) {
        synchronized (workingStatuses) {
            if ((statusBar != null) && (workingId != null)) {
                workingStatuses.remove(workingId);

                if (workingStatuses.size() > 0) {
                    statusBar.setWorking(true);
                    statusBar.setText(new LinkedList<String>(workingStatuses.values()).getLast());
                } else {
                    statusBar.setWorking(false);
                    statusBar.setText("");
                }
            }
        }
    }

    /**
     * Changes the current content page to the Channel Editor with the new channel specified as the
     * loaded one.
     */
    public void setupChannel(Channel channel, String groupId) {
        setBold(viewPane, UIConstants.ERROR_CONSTANT);
        setCurrentContentPage(channelEditPanel);
        setFocus(channelEditTasks);
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 0, 0, false);
        confirmLeave();
        channelEditPanel.addChannel(channel, groupId);
    }

    /**
     * Edits a channel at a specified index, setting that channel as the current channel in the
     * editor.
     */
    public void editChannel(Channel channel) {
        String alertMessage = channelEditPanel.checkInvalidPluginProperties(channel);
        if (StringUtils.isNotBlank(alertMessage)) {
            if (!alertOption(this, alertMessage + "\nWhen this channel is saved, those properties will be lost. You can choose to import/edit\nthis channel at a later time after verifying that all necessary extensions are properly loaded.\nAre you sure you wish to continue?")) {
                return;
            }
        }

        confirmLeave();
        setBold(viewPane, UIConstants.ERROR_CONSTANT);
        setCurrentContentPage(channelEditPanel);
        setFocus(channelEditTasks);
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 0, 4, false);
        channelEditPanel.editChannel(channel);
    }

    /**
     * Changes the current content page to the Alert Editor with the new alert specified as the
     * loaded one.
     */
    public void setupAlert(Map<String, Map<String, String>> protocolOptions) {
        setBold(viewPane, UIConstants.ERROR_CONSTANT);
        setCurrentContentPage(alertEditPanel);
        setFocus(alertEditTasks);
        setVisibleTasks(alertEditTasks, alertEditPopupMenu, 0, 0, false);
        alertEditPanel.addAlert(protocolOptions);
    }

    /**
     * Edits an alert at a specified index, setting that alert as the current alert in the editor.
     */
    public void editAlert(AlertModel alertModel, Map<String, Map<String, String>> protocolOptions) {
        if (alertEditPanel.editAlert(alertModel, protocolOptions)) {
            setBold(viewPane, UIConstants.ERROR_CONSTANT);
            setCurrentContentPage(alertEditPanel);
            setFocus(alertEditTasks);
            setVisibleTasks(alertEditTasks, alertEditPopupMenu, 0, 0, false);
        }
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

        // Always cancel the current job if it is still running.
        if (statusUpdaterJob != null && !statusUpdaterJob.isDone()) {
            statusUpdaterJob.cancel(true);
        }

        // Start a new status updater job if the current content page is the dashboard
        if (currentContentPage == dashboardPanel || currentContentPage == alertPanel) {
            statusUpdaterJob = statusUpdaterExecutor.submit(new StatusUpdater());
        }
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
        createChannelEditPane();
        createDashboardPane();
        createEventPane();
        createMessagePane();
        createUserPane();
        createAlertPane();
        createAlertEditPane();
        createGlobalScriptsPane();
        createExtensionsPane();
        createOtherPane();
    }

    private void setInitialVisibleTasks() {
        // View Pane
        setVisibleTasks(viewPane, null, 0, -1, true);

        // Alert Pane
        setVisibleTasks(alertTasks, alertPopupMenu, 0, -1, true);
        setVisibleTasks(alertTasks, alertPopupMenu, 4, -1, false);

        // Alert Edit Pane
        setVisibleTasks(alertEditTasks, alertEditPopupMenu, 0, 0, false);
        setVisibleTasks(alertEditTasks, alertEditPopupMenu, 1, 1, true);

        // Channel Edit Pane
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 0, 15, false);
        setVisibleTasks(channelEditTasks, channelEditPopupMenu, 14, 14, true);

        // Dashboard Pane
        setVisibleTasks(dashboardTasks, dashboardPopupMenu, 0, 0, true);
        setVisibleTasks(dashboardTasks, dashboardPopupMenu, 1, -1, false);

        // Event Pane
        setVisibleTasks(eventTasks, eventPopupMenu, 0, 2, true);

        // Message Pane
        setVisibleTasks(messageTasks, messagePopupMenu, 0, -1, true);
        setVisibleTasks(messageTasks, messagePopupMenu, 6, -1, false);
        setVisibleTasks(messageTasks, messagePopupMenu, 7, 7, true);

        // User Pane
        setVisibleTasks(userTasks, userPopupMenu, 0, 1, true);
        setVisibleTasks(userTasks, userPopupMenu, 2, -1, false);

        // Global Scripts Pane
        setVisibleTasks(globalScriptsTasks, globalScriptsPopupMenu, 0, 0, false);
        setVisibleTasks(globalScriptsTasks, globalScriptsPopupMenu, 1, -1, true);

        // Extensions Pane
        setVisibleTasks(extensionsTasks, extensionsPopupMenu, 0, 0, true);
        setVisibleTasks(extensionsTasks, extensionsPopupMenu, 1, -1, false);

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
        addTask(TaskConstants.ALERT_NEW, "New Alert", "Create a new alert.", "N", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/error_add.png")), alertTasks, alertPopupMenu);
        addTask(TaskConstants.ALERT_IMPORT, "Import Alert", "Import an alert from an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")), alertTasks, alertPopupMenu);
        addTask(TaskConstants.ALERT_EXPORT_ALL, "Export All Alerts", "Export all of the alerts to an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), alertTasks, alertPopupMenu);
        addTask(TaskConstants.ALERT_EXPORT, "Export Alert", "Export the currently selected alert to an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), alertTasks, alertPopupMenu);
        addTask(TaskConstants.ALERT_DELETE, "Delete Alert", "Delete the currently selected alert.", "L", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/error_delete.png")), alertTasks, alertPopupMenu);
        addTask(TaskConstants.ALERT_EDIT, "Edit Alert", "Edit the currently selected alert.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_form_edit.png")), alertTasks, alertPopupMenu);
        addTask(TaskConstants.ALERT_ENABLE, "Enable Alert", "Enable the currently selected alert.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_play_blue.png")), alertTasks, alertPopupMenu);
        addTask(TaskConstants.ALERT_DISABLE, "Disable Alert", "Disable the currently selected alert.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_stop_blue.png")), alertTasks, alertPopupMenu);

        setNonFocusable(alertTasks);
        taskPaneContainer.add(alertTasks);
    }

    /**
     * Creates the template task pane.
     */
    private void createAlertEditPane() {
        // Create Alert Edit Tasks Pane
        alertEditTasks = new JXTaskPane();
        alertEditPopupMenu = new JPopupMenu();
        alertEditTasks.setTitle("Alert Edit Tasks");
        alertEditTasks.setName(TaskConstants.ALERT_EDIT_KEY);
        alertEditTasks.setFocusable(false);

        addTask(TaskConstants.ALERT_EDIT_SAVE, "Save Alert", "Save all changes made to this alert.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/disk.png")), alertEditTasks, alertEditPopupMenu);
        addTask(TaskConstants.ALERT_EDIT_EXPORT, "Export Alert", "Export the currently selected alert to an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), alertEditTasks, alertEditPopupMenu);

        setNonFocusable(alertEditTasks);
        taskPaneContainer.add(alertEditTasks);
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
        addTask(TaskConstants.CHANNEL_EDIT_FILTER, UIConstants.EDIT_FILTER, "Edit the filter for the current connector.", "F", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/table_edit.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_TRANSFORMER, UIConstants.EDIT_TRANSFORMER, "Edit the transformer for the current connector.", "T", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/table_edit.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_RESPONSE_TRANSFORMER, UIConstants.EDIT_RESPONSE_TRANSFORMER, "Edit the response transformer for the current connector.", "R", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/table_edit.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_IMPORT_CONNECTOR, "Import Connector", "Import the currently displayed connector from an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_EXPORT_CONNECTOR, "Export Connector", "Export the currently displayed connector to an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_EXPORT, "Export Channel", "Export the currently selected channel to an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_VALIDATE_SCRIPT, "Validate Script", "Validate the currently viewed script.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/accept.png")), channelEditTasks, channelEditPopupMenu);
        addTask(TaskConstants.CHANNEL_EDIT_DEPLOY, "Deploy Channel", "Deploy the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_redo.png")), channelEditTasks, channelEditPopupMenu);

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

        addTask(TaskConstants.DASHBOARD_START, "Start", "Start the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_play_blue.png")), dashboardTasks, dashboardPopupMenu);
        addTask(TaskConstants.DASHBOARD_PAUSE, "Pause", "Pause the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_pause_blue.png")), dashboardTasks, dashboardPopupMenu);
        addTask(TaskConstants.DASHBOARD_STOP, "Stop", "Stop the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_stop_blue.png")), dashboardTasks, dashboardPopupMenu);
        addTask(TaskConstants.DASHBOARD_HALT, "Halt", "Halt the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/stop.png")), dashboardTasks, dashboardPopupMenu);

        addTask(TaskConstants.DASHBOARD_UNDEPLOY, "Undeploy Channel", "Undeploys the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_undo.png")), dashboardTasks, dashboardPopupMenu);

        addTask(TaskConstants.DASHBOARD_START_CONNECTOR, "Start", "Start the currently selected connector.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_play_blue.png")), dashboardTasks, dashboardPopupMenu);
        addTask(TaskConstants.DASHBOARD_STOP_CONNECTOR, "Stop", "Stop the currently selected connector.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_stop_blue.png")), dashboardTasks, dashboardPopupMenu);

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
        addTask(TaskConstants.EVENT_EXPORT_ALL, "Export All Events", "Export all events to a file on the server.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), eventTasks, eventPopupMenu);
        addTask(TaskConstants.EVENT_REMOVE_ALL, "Remove All Events", "Remove all events and optionally export them to a file on the server.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/table_delete.png")), eventTasks, eventPopupMenu);

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

        addTask(TaskConstants.MESSAGE_REFRESH, "Refresh", "Refresh the list of messages with the current search criteria.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_refresh.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_SEND, "Send Message", "Send a message to the channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/email_go.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_IMPORT, "Import Messages", "Import messages from a file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_EXPORT, "Export Results", "Export all messages in the current search.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_REMOVE_ALL, "Remove All Messages", "Remove all messages in this channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/email_delete.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_REMOVE_FILTERED, "Remove Results", "Remove all messages in the current search.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/email_delete.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_REMOVE, "Remove Message", "Remove the selected Message.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/delete.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_REPROCESS_FILTERED, "Reprocess Results", "Reprocess all messages in the current search.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/reprocess_results.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_REPROCESS, "Reprocess Message", "Reprocess the selected message.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/reprocess_message.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_VIEW_IMAGE, "View Attachment", "View Attachment", "View the attachment for the selected message.", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/attach.png")), messageTasks, messagePopupMenu);
        addTask(TaskConstants.MESSAGE_EXPORT_ATTACHMENT, "Export Attachment", "Export Attachment", "Export the selected attachment to a file.", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), messageTasks, messagePopupMenu);
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
        addTask(TaskConstants.OTHER_NOTIFICATIONS, UIConstants.VIEW_NOTIFICATIONS, "View notifications from Mirth.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/flag_orange.png")), otherPane, null);
        addTask(TaskConstants.OTHER_VIEW_USER_API, "View User API", "View documentation for the Mirth Connect User API.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/page_white_text.png")), otherPane, null);
        addTask(TaskConstants.OTHER_VIEW_CLIENT_API, "View Client API", "View documentation for the Mirth Connect Client API.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/page_white_text.png")), otherPane, null);
        addTask(TaskConstants.OTHER_HELP, "Help", "View the Mirth Connect wiki.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/help.png")), otherPane, null);
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

    public void updateNotificationTaskName(int notifications) {
        String taskName = UIConstants.VIEW_NOTIFICATIONS;
        if (notifications > 0) {
            taskName += " (" + notifications + ")";
        }
        ((JXHyperlink) otherPane.getContentPane().getComponent(UIConstants.VIEW_NOTIFICATIONS_TASK_NUMBER)).setText(taskName);
    }

    public int addTask(String callbackMethod, String displayName, String toolTip, String shortcutKey, ImageIcon icon, JXTaskPane pane, JPopupMenu menu) {
        return addTask(callbackMethod, displayName, toolTip, shortcutKey, icon, pane, menu, this);
    }

    /**
     * Initializes the bound method call for the task pane actions and adds them to the
     * taskpane/popupmenu.
     */
    public int addTask(String callbackMethod, String displayName, String toolTip, String shortcutKey, ImageIcon icon, JXTaskPane pane, JPopupMenu menu, Object handler) {
        BoundAction boundAction = ActionFactory.createBoundAction(callbackMethod, displayName, shortcutKey);

        if (icon != null) {
            boundAction.putValue(Action.SMALL_ICON, icon);
        }
        boundAction.putValue(Action.SHORT_DESCRIPTION, toolTip);
        boundAction.registerCallback(handler, callbackMethod);

        Component component = pane.add(boundAction);
        getComponentTaskMap().put(component, callbackMethod);

        if (menu != null) {
            menu.add(boundAction);
        }

        return (pane.getContentPane().getComponentCount() - 1);
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

    public enum ConflictOption {
        YES, YES_APPLY_ALL, NO, NO_APPLY_ALL;
    }

    /**
     * Alerts the user with a conflict resolution dialog
     */
    public ConflictOption alertConflict(Component parentComponent, String message, int count) {
        final JCheckBox conflictCheckbox = new JCheckBox("Do this for the next " + String.valueOf(count - 1) + " conflicts");
        conflictCheckbox.setSelected(false);

        Object[] params = { message, conflictCheckbox };

        int jOption = JOptionPane.showConfirmDialog(getVisibleComponent(parentComponent), params, "Select an Option", JOptionPane.YES_NO_OPTION);
        boolean isSelected = conflictCheckbox.isSelected();

        ConflictOption conflictOption = null;
        if (jOption == JOptionPane.YES_OPTION) {
            if (isSelected) {
                conflictOption = ConflictOption.YES_APPLY_ALL;
            } else {
                conflictOption = ConflictOption.YES;
            }
        } else {
            if (isSelected || jOption == -1) {
                conflictOption = ConflictOption.NO_APPLY_ALL;
            } else {
                conflictOption = ConflictOption.NO;
            }
        }

        return conflictOption;
    }

    public boolean alertRefresh() {
        boolean cancelRefresh = false;

        if (PlatformUI.MIRTH_FRAME.isSaveEnabled()) {
            int option = JOptionPane.showConfirmDialog(PlatformUI.MIRTH_FRAME, "<html>Any unsaved changes will be lost.<br>Would you like to continue?</html>", "Warning", JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.NO_OPTION || option == JOptionPane.CLOSED_OPTION) {
                cancelRefresh = true;
            } else {
                setSaveEnabled(false);
            }
        }

        return cancelRefresh;
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
     * Alerts the user with an error dialog with the passed in 'message' and a 'question'.
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

    public void alertThrowable(Component parentComponent, Throwable t) {
        alertThrowable(parentComponent, t, null);
    }

    /**
     * Alerts the user with an exception dialog with the passed in stack trace.
     */
    public void alertThrowable(Component parentComponent, Throwable t, String customMessage) {
        alertThrowable(parentComponent, t, customMessage, true);
    }

    /**
     * Alerts the user with an exception dialog with the passed in stack trace.
     */
    public void alertThrowable(Component parentComponent, Throwable t, boolean showMessageOnForbidden) {
        alertThrowable(parentComponent, t, null, showMessageOnForbidden);
    }

    /**
     * Alerts the user with an exception dialog with the passed in stack trace.
     */
    public void alertThrowable(Component parentComponent, Throwable t, String customMessage, boolean showMessageOnForbidden) {
        alertThrowable(parentComponent, t, customMessage, showMessageOnForbidden, null);
    }

    /**
     * Alerts the user with an exception dialog with the passed in stack trace.
     */
    public void alertThrowable(Component parentComponent, Throwable t, String customMessage, String safeErrorKey) {
        alertThrowable(parentComponent, t, customMessage, true, safeErrorKey);
    }

    /**
     * Alerts the user with an exception dialog with the passed in stack trace.
     */
    public void alertThrowable(Component parentComponent, Throwable t, String customMessage, boolean showMessageOnForbidden, String safeErrorKey) {
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
        String message = StringUtils.trimToEmpty(customMessage);
        boolean showDialog = true;

        if (t != null) {
            // Always print the stacktrace for troubleshooting purposes
            t.printStackTrace();

            if (t instanceof ExecutionException && t.getCause() != null) {
                t = t.getCause();
            }
            if (t.getCause() != null && t.getCause() instanceof ClientException) {
                t = t.getCause();
            }

            if (StringUtils.isBlank(message) && StringUtils.isNotBlank(t.getMessage())) {
                message = t.getMessage();
            }

            /*
             * Logout if an exception occurs that indicates the server is no longer running or
             * accessible. We only want to do this if a ClientException was passed in, indicating it
             * was actually due to a request to the server. Other places in the application could
             * call this method with an exception that happens to contain the string
             * "Connection reset", for example.
             */
            if (t instanceof ClientException) {
                if (t instanceof ForbiddenException || t.getCause() != null && t.getCause() instanceof ForbiddenException) {
                    message = "You are not authorized to perform this action.\n\n" + message;
                    if (!showMessageOnForbidden) {
                        showDialog = false;
                    }
                } else if (StringUtils.contains(t.getMessage(), "Received close_notify during handshake")) {
                    return;
                } else if (t.getCause() != null && t.getCause() instanceof IllegalStateException && mirthClient.isClosed()) {
                    return;
                } else if (t instanceof UnauthorizedException || t.getCause() != null && t.getCause() instanceof UnauthorizedException) {
                    connectionError = true;
                    statusUpdaterExecutor.shutdownNow();

                    alertWarning(parentComponent, "Sorry your connection to Mirth has either timed out or there was an error in the connection.  Please login again.");
                    if (!exportChannelOnError()) {
                        return;
                    }
                    mirthClient.close();
                    this.dispose();
                    LoginPanel.getInstance().initialize(PlatformUI.SERVER_URL, PlatformUI.CLIENT_VERSION, "", "");
                    return;
                } else if (t.getCause() != null && t.getCause() instanceof HttpHostConnectException && (StringUtils.contains(t.getCause().getMessage(), "Connection refused") || StringUtils.contains(t.getCause().getMessage(), "Host is down"))) {
                    connectionError = true;
                    statusUpdaterExecutor.shutdownNow();

                    String server;
                    if (!StringUtils.isBlank(PlatformUI.SERVER_NAME)) {
                        server = PlatformUI.SERVER_NAME + "(" + PlatformUI.SERVER_URL + ")";
                    } else {
                        server = PlatformUI.SERVER_URL;
                    }
                    alertWarning(parentComponent, "The Mirth Connect server " + server + " is no longer running.  Please start it and log in again.");
                    if (!exportChannelOnError()) {
                        return;
                    }
                    mirthClient.close();
                    this.dispose();
                    LoginPanel.getInstance().initialize(PlatformUI.SERVER_URL, PlatformUI.CLIENT_VERSION, "", "");
                    return;
                }
            }

            for (String stackFrame : ExceptionUtils.getStackFrames(t)) {
                if (StringUtils.isNotEmpty(message)) {
                    message += '\n';
                }
                message += StringUtils.trim(stackFrame);
            }
        }

        logger.error(message);

        if (showDialog) {
            Window owner = getWindowForComponent(parentComponent);

            if (owner instanceof java.awt.Frame) {
                new ErrorDialog((java.awt.Frame) owner, message);
            } else { // window instanceof Dialog
                new ErrorDialog((java.awt.Dialog) owner, message);
            }
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
        setFocus(new JXTaskPane[] { pane }, true, true);
    }

    /**
     * Sets the visible task panes to the specified 'panes'. Also allows setting the 'Mirth' and
     * 'Other' panes.
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
     * Sets the visible tasks in the given 'pane' and 'menu'. The method takes an interval of
     * indices (end index should be -1 to go to the end), as well as a whether they should be set to
     * visible or not-visible.
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
     * A prompt to ask the user if he would like to save the changes made before leaving the page.
     */
    public boolean confirmLeave() {
        if (dashboardPanel != null) {
            dashboardPanel.closePopupWindow();
        }

        if (channelPanel != null) {
            channelPanel.closePopupWindow();
        }

        if (channelEditPanel != null) {
            channelEditPanel.closePopupWindow();
        }

        if (currentContentPage == channelPanel && isSaveEnabled()) {
            if (!channelPanel.confirmLeave()) {
                return false;
            }
        } else if ((currentContentPage == channelEditPanel || currentContentPage == channelEditPanel.transformerPane || currentContentPage == channelEditPanel.filterPane) && isSaveEnabled()) {
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
                if (!settingsPane.getCurrentSettingsPanel().doSave()) {
                    return false;
                }
            } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return false;
            }
        } else if (currentContentPage == alertEditPanel && isSaveEnabled()) {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the alerts?");

            if (option == JOptionPane.YES_OPTION) {
                if (!alertEditPanel.saveAlert()) {
                    return false;
                }
            } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return false;
            }
        } else if (currentContentPage == globalScriptsPanel && isSaveEnabled()) {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the scripts?");

            if (option == JOptionPane.YES_OPTION) {
                if (!doSaveGlobalScripts()) {
                    return false;
                }
            } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return false;
            }
        } else if (currentContentPage == codeTemplatePanel && isSaveEnabled()) {
            if (!codeTemplatePanel.confirmLeave()) {
                return false;
            }
        }

        setSaveEnabled(false);
        return true;
    }

    /**
     * Sends the channel passed in to the server, updating it or adding it.
     * 
     * @throws ClientException
     */
    public boolean updateChannel(Channel curr, boolean overwriting) throws ClientException {
        if (overwriting ? !mirthClient.updateChannel(curr, false) : !mirthClient.createChannel(curr)) {
            if (alertOption(this, "This channel has been modified since you first opened it.\nWould you like to overwrite it?")) {
                mirthClient.updateChannel(curr, true);
            } else {
                return false;
            }
        }
        channelPanel.retrieveChannels();

        return true;
    }

    /**
     * Sends the passed in user to the server, updating it or adding it.
     */
    public boolean updateUser(final Component parentComponent, final User updateUser, final String newPassword) {
        final String workingId = startWorking("Saving user...");

        try {
            if (StringUtils.isNotEmpty(newPassword)) {
                /*
                 * If a new user is being passed in (null user id), the password will only be
                 * checked right now.
                 */
                if (!checkOrUpdateUserPassword(parentComponent, updateUser, newPassword)) {
                    return false;
                }
            }

            try {
                if (updateUser.getId() == null) {
                    mirthClient.createUser(updateUser);
                } else {
                    mirthClient.updateUser(updateUser);
                }
            } catch (ClientException e) {
                if (e.getMessage() != null && e.getMessage().contains("username must be unique")) {
                    alertWarning(parentComponent, "This username already exists. Please choose another one.");
                } else {
                    alertThrowable(parentComponent, e);
                }

                return false;
            }

            try {
                retrieveUsers();

                /*
                 * If the user id was null, a new user was being created and the password was only
                 * checked. Get the created user with the id and then update the password.
                 */
                if (updateUser.getId() == null) {
                    User newUser = null;
                    for (User user : users) {
                        if (user.getUsername().equals(updateUser.getUsername())) {
                            newUser = user;
                        }
                    }
                    checkOrUpdateUserPassword(parentComponent, newUser, newPassword);
                }
            } catch (ClientException e) {
                alertThrowable(parentComponent, e);
            } finally {
                // The userPanel will be null if the user panel has not been viewed (i.e. registration).
                if (userPanel != null) {
                    userPanel.updateUserTable();
                }
            }
        } finally {
            stopWorking(workingId);
        }

        return true;
    }

    /**
     * If the current user is being updated, it needs to be done in the main thread so that the
     * username can be changed, re-logged in, and the current user information can be updated.
     * 
     * @param parentComponent
     * @param currentUser
     * @param newPassword
     * @return
     */
    public boolean updateCurrentUser(Component parentComponent, final User currentUser, String newPassword) {
        // Find out if the username is being changed so that we can login again.
        boolean changingUsername = !currentUser.getUsername().equals(PlatformUI.USER_NAME);

        final String workingId = startWorking("Saving user...");

        try {

            /*
             * If there is a new password, update it. If not, make sure that the username is not
             * being changed, since the password must be updated when the username is changed.
             */
            if (StringUtils.isNotEmpty(newPassword)) {
                if (!checkOrUpdateUserPassword(parentComponent, currentUser, newPassword)) {
                    return false;
                }
            } else if (changingUsername) {
                alertWarning(parentComponent, "If you are changing your username, you must also update your password.");
                return false;
            }

            try {
                mirthClient.updateUser(currentUser);
            } catch (ClientException e) {
                if (e.getMessage() != null && e.getMessage().contains("username must be unique")) {
                    alertWarning(parentComponent, "This username already exists. Please choose another one.");
                } else {
                    alertThrowable(parentComponent, e);
                }

                return false;
            }

            try {
                retrieveUsers();
            } catch (ClientException e) {
                alertThrowable(parentComponent, e);
            } finally {
                // The userPanel will be null if the user panel has not been viewed (i.e. registration).
                if (userPanel != null) {
                    userPanel.updateUserTable();
                }
            }
        } finally {
            stopWorking(workingId);
        }

        // If the username is being changed, login again.
        if (changingUsername) {
            final String workingId2 = startWorking("Switching User...");

            try {
                LoadedExtensions.getInstance().resetPlugins();
                mirthClient.logout();
                mirthClient.login(currentUser.getUsername(), newPassword);
                PlatformUI.USER_NAME = currentUser.getUsername();
            } catch (ClientException e) {
                alertThrowable(parentComponent, e);
            } finally {
                stopWorking(workingId2);
            }
        }

        return true;
    }

    public boolean checkOrUpdateUserPassword(Component parentComponent, final User currentUser, String newPassword) {
        try {
            List<String> responses;
            if (currentUser.getId() == null) {
                responses = mirthClient.checkUserPassword(newPassword);
            } else {
                responses = mirthClient.updateUserPassword(currentUser.getId(), newPassword);
            }

            if (CollectionUtils.isNotEmpty(responses)) {
                String responseString = "Your password is not valid. Please fix the following:\n";
                for (String response : responses) {
                    responseString += (" - " + response + "\n");
                }
                alertError(this, responseString);
                return false;
            }
        } catch (ClientException e) {
            alertThrowable(this, e);
            return false;
        }

        return true;
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
            alertThrowable(parentComponent, e);
        }

        return currentUser;
    }

    public void registerUser(final User user) {
        final String workingId = startWorking("Registering user...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    ConnectServiceUtil.registerUser(PlatformUI.SERVER_ID, PlatformUI.SERVER_VERSION, user, PlatformUI.HTTPS_PROTOCOLS, PlatformUI.HTTPS_CIPHER_SUITES);
                } catch (ClientException e) {
                    // ignore errors connecting to update/stats server
                }

                return null;
            }

            public void done() {
                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void sendUsageStatistics() {
        UpdateSettings updateSettings = null;
        try {
            updateSettings = mirthClient.getUpdateSettings();
        } catch (Exception e) {
        }

        if (updateSettings != null && updateSettings.getStatsEnabled()) {
            final String workingId = startWorking("Sending usage statistics...");

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                public Void doInBackground() {
                    try {
                        String usageData = mirthClient.getUsageData(getClientStats());
                        if (usageData != null) {
                            boolean isSent = ConnectServiceUtil.sendStatistics(PlatformUI.SERVER_ID, PlatformUI.SERVER_VERSION, false, usageData, PlatformUI.HTTPS_PROTOCOLS, PlatformUI.HTTPS_CIPHER_SUITES);
                            if (isSent) {
                                UpdateSettings settings = new UpdateSettings();
                                settings.setLastStatsTime(System.currentTimeMillis());
                                mirthClient.setUpdateSettings(settings);
                            }
                        }
                    } catch (ClientException e) {
                        // ignore errors connecting to update/stats server
                    }

                    return null;
                }

                public void done() {
                    stopWorking(workingId);
                }
            };

            worker.execute();
        }
    }

    private Map<String, Object> getClientStats() {
        Map<String, Object> clientStats = new HashMap<String, Object>();
        clientStats.put("javaVersion", System.getProperty("java.version"));
        return clientStats;
    }

    /**
     * Enables the save button for needed page.
     */
    public void setSaveEnabled(boolean enabled) {
        if (currentContentPage == channelPanel) {
            channelPanel.setSaveEnabled(enabled);
        } else if (currentContentPage == channelEditPanel) {
            setVisibleTasks(channelEditTasks, channelEditPopupMenu, 0, 0, enabled);
        } else if (currentContentPage == settingsPane) {
            settingsPane.getCurrentSettingsPanel().setSaveEnabled(enabled);
        } else if (alertEditPanel != null && currentContentPage == alertEditPanel) {
            setVisibleTasks(alertEditTasks, alertEditPopupMenu, 0, 0, enabled);
        } else if (globalScriptsPanel != null && currentContentPage == globalScriptsPanel) {
            setVisibleTasks(globalScriptsTasks, globalScriptsPopupMenu, 0, 0, enabled);
        } else if (currentContentPage == codeTemplatePanel) {
            codeTemplatePanel.setSaveEnabled(enabled);
        }
    }

    /**
     * Enables the save button for needed page.
     */
    public boolean isSaveEnabled() {
        boolean enabled = false;

        if (currentContentPage != null) {
            if (currentContentPage == channelPanel) {
                enabled = channelPanel.isSaveEnabled();
            } else if (currentContentPage == channelEditPanel) {
                enabled = channelEditTasks.getContentPane().getComponent(0).isVisible();
            } else if (channelEditPanel != null && currentContentPage == channelEditPanel.transformerPane) {
                enabled = channelEditTasks.getContentPane().getComponent(0).isVisible() || channelEditPanel.transformerPane.isModified();
            } else if (channelEditPanel != null && currentContentPage == channelEditPanel.filterPane) {
                enabled = channelEditTasks.getContentPane().getComponent(0).isVisible() || channelEditPanel.filterPane.isModified();
            } else if (currentContentPage == settingsPane) {
                enabled = settingsPane.getCurrentSettingsPanel().isSaveEnabled();
            } else if (alertEditPanel != null && currentContentPage == alertEditPanel) {
                enabled = alertEditTasks.getContentPane().getComponent(0).isVisible();
            } else if (globalScriptsPanel != null && currentContentPage == globalScriptsPanel) {
                enabled = globalScriptsTasks.getContentPane().getComponent(0).isVisible();
            } else if (currentContentPage == codeTemplatePanel) {
                enabled = codeTemplatePanel.isSaveEnabled();
            }
        }

        return enabled;
    }

    // ////////////////////////////////////////////////////////////
    // --- All bound actions are beneath this point --- //
    // ////////////////////////////////////////////////////////////
    public void goToMirth() {
        BareBonesBrowserLaunch.openURL("http://www.mirthcorp.com/");
    }

    public void goToUserAPI() {
        BareBonesBrowserLaunch.openURL(PlatformUI.SERVER_URL + UIConstants.USER_API_LOCATION);
    }

    public void goToClientAPI() {
        BareBonesBrowserLaunch.openURL(PlatformUI.SERVER_URL + UIConstants.CLIENT_API_LOCATION);
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

        dashboardPanel.switchPanel();

        setBold(viewPane, 0);
        setPanelName("Dashboard");
        setCurrentContentPage(dashboardPanel);
        setFocus(dashboardTasks);

        doRefreshStatuses(true);
    }

    public void doShowChannel() {
        if (!confirmLeave()) {
            return;
        }

        channelPanel.switchPanel();
    }

    public void doShowUsers() {
        if (userPanel == null) {
            userPanel = new UserPanel();
        }

        if (!confirmLeave()) {
            return;
        }

        final String workingId = startWorking("Loading users...");

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
                stopWorking(workingId);
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

        final String workingId = startWorking("Loading settings...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                settingsPane.setSelectedSettingsPanel(0);
                return null;
            }

            public void done() {
                setBold(viewPane, 3);
                setPanelName("Settings");
                setCurrentContentPage(settingsPane);
                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doShowAlerts() {
        if (alertPanel == null) {
            alertPanel = new DefaultAlertPanel();
        }

        if (!confirmLeave()) {
            return;
        }

        setBold(viewPane, 4);
        setPanelName("Alerts");
        setCurrentContentPage(alertPanel);
        setFocus(alertTasks);
        setSaveEnabled(false);
        doRefreshAlerts(true);
    }

    public void doShowExtensions() {
        if (extensionsPanel == null) {
            extensionsPanel = new ExtensionManagerPanel();
        }

        final String workingId = startWorking("Loading extensions...");
        if (confirmLeave()) {
            setBold(viewPane, 6);
            setPanelName("Extensions");
            setCurrentContentPage(extensionsPanel);
            setFocus(extensionsTasks);
            refreshExtensions();
            stopWorking(workingId);
        }
    }

    public void doLogout() {
        logout(false);
    }

    public boolean logout(boolean quit) {
        if (!confirmLeave()) {
            return false;
        }

        // MIRTH-3074 Remove the keyEventDispatcher to prevent memory leak.
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyEventDispatcher);

        statusUpdaterExecutor.shutdownNow();

        if (currentContentPage == messageBrowser) {
            mirthClient.getServerConnection().abort(messageBrowser.getAbortOperations());
        }

        userPreferences = Preferences.userNodeForPackage(Mirth.class);
        userPreferences.putInt("maximizedState", getExtendedState());
        userPreferences.putInt("width", getWidth());
        userPreferences.putInt("height", getHeight());

        LoadedExtensions.getInstance().stopPlugins();

        final Properties tagUserProperties = new Properties();
        tagUserProperties.put("initialTagsDashboard", dashboardPanel.getUserTags());
        tagUserProperties.put("initialTagsChannels", channelPanel.getUserTags());

        try {
            User currentUser = getCurrentUser(this);
            if (currentUser != null) {
                mirthClient.setUserPreferences(currentUser.getId(), tagUserProperties);
            }
        } catch (ClientException e) {
            alertThrowable(this, e);
        }

        try {
            mirthClient.logout();
        } catch (ClientException e) {
            alertThrowable(this, e);
        }

        mirthClient.close();
        this.dispose();

        if (!quit) {
            LoginPanel.getInstance().initialize(PlatformUI.SERVER_URL, PlatformUI.CLIENT_VERSION, "", "");
        }

        return true;
    }

    public void doMoveDestinationDown() {
        channelEditPanel.moveDestinationDown();
    }

    public void doMoveDestinationUp() {
        channelEditPanel.moveDestinationUp();
    }

    public void doEditGlobalScripts() {
        if (globalScriptsPanel == null) {
            globalScriptsPanel = new GlobalScriptsPanel();
        }

        final String workingId = startWorking("Loading global scripts...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                globalScriptsPanel.edit();
                return null;
            }

            public void done() {
                editGlobalScripts();
                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doEditCodeTemplates() {
        codeTemplatePanel.switchPanel();
    }

    public void doValidateCurrentGlobalScript() {
        globalScriptsPanel.validateCurrentScript();
    }

    public void doImportGlobalScripts() {
        String content = browseForFileString("XML");

        if (content != null) {
            try {
                ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
                @SuppressWarnings("unchecked")
                Map<String, String> importScripts = serializer.deserialize(content, Map.class);

                for (Entry<String, String> globalScriptEntry : importScripts.entrySet()) {
                    importScripts.put(globalScriptEntry.getKey(), globalScriptEntry.getValue().replaceAll("com.webreach.mirth", "com.mirth.connect"));
                }

                if (importScripts.containsKey("Shutdown") && !importScripts.containsKey("Undeploy")) {
                    importScripts.put("Undeploy", importScripts.get("Shutdown"));
                    importScripts.remove("Shutdown");
                }

                globalScriptsPanel.importAllScripts(importScripts);
            } catch (Exception e) {
                alertThrowable(this, e, "Invalid scripts file. " + e.getMessage());
            }
        }
    }

    public void doExportGlobalScripts() {
        if (changesHaveBeenMade()) {
            if (alertOption(this, "You must save your global scripts before exporting.  Would you like to save them now?")) {
                String validationMessage = globalScriptsPanel.validateAllScripts();
                if (validationMessage != null) {
                    alertCustomError(this, validationMessage, CustomErrorDialog.ERROR_VALIDATING_GLOBAL_SCRIPTS);
                    return;
                }

                globalScriptsPanel.save();
                setSaveEnabled(false);
            } else {
                return;
            }
        }

        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        String globalScriptsXML = serializer.serialize(globalScriptsPanel.exportAllScripts());

        exportFile(globalScriptsXML, null, "XML", "Global Scripts export");
    }

    public void doValidateChannelScripts() {
        channelEditPanel.validateScripts();
    }

    public boolean doSaveGlobalScripts() {
        String validationMessage = globalScriptsPanel.validateAllScripts();
        if (validationMessage != null) {
            alertCustomError(this, validationMessage, CustomErrorDialog.ERROR_VALIDATING_GLOBAL_SCRIPTS);
            return false;
        }

        final String workingId = startWorking("Saving global scripts...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                globalScriptsPanel.save();
                return null;
            }

            public void done() {
                setSaveEnabled(false);
                stopWorking(workingId);
            }
        };

        worker.execute();

        return true;
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
        doRefreshStatuses(true);
    }

    public void doRefreshStatuses(boolean queue) {
        QueuingSwingWorkerTask<Void, DashboardStatus> task = new QueuingSwingWorkerTask<Void, DashboardStatus>("doRefreshStatuses", "Loading statistics...") {
            @Override
            public Void doInBackground() {
                try {
                    channelPanel.retrieveGroups();
                    channelPanel.retrieveDependencies();

                    SettingsPanelTags tagsPanel = getTagsPanel();
                    if (tagsPanel != null) {
                        tagsPanel.refresh();
                    }

                    for (DashboardColumnPlugin plugin : LoadedExtensions.getInstance().getDashboardColumnPlugins().values()) {
                        plugin.tableUpdate(status);
                    }

                    String filter = dashboardPanel.getUserTags();
                    DashboardChannelInfo dashboardStatusList = mirthClient.getDashboardChannelInfo(REFRESH_BLOCK_SIZE, filter);
                    status = dashboardStatusList.getDashboardStatuses();
                    Set<String> remainingIds = dashboardStatusList.getRemainingChannelIds();
                    deployedChannelCount = dashboardStatusList.getDeployedChannelCount();

                    if (status != null) {
                        publish(status.toArray(new DashboardStatus[status.size()]));

                        if (CollectionUtils.isNotEmpty(remainingIds)) {
                            Set<String> statusChannelIds = new HashSet<String>(Math.min(remainingIds.size(), REFRESH_BLOCK_SIZE));

                            for (Iterator<String> it = remainingIds.iterator(); it.hasNext();) {
                                statusChannelIds.add(it.next());

                                if (!it.hasNext() || statusChannelIds.size() == REFRESH_BLOCK_SIZE) {
                                    // Processing a new block, retrieve dashboard statuses from server
                                    List<DashboardStatus> intermediateStatusList = mirthClient.getChannelStatusList(statusChannelIds, filter);
                                    // Publish the intermediate statuses
                                    publish(intermediateStatusList.toArray(new DashboardStatus[intermediateStatusList.size()]));
                                    // Add the statuses to the master list
                                    status.addAll(intermediateStatusList);
                                    // Clear the set of channel IDs
                                    statusChannelIds.clear();
                                }
                            }
                        }
                    }
                } catch (ClientException e) {
                    status = null;
                    SwingUtilities.invokeLater(() -> {
                        alertThrowable(PlatformUI.MIRTH_FRAME, e, e.getMessage(), false, TaskConstants.DASHBOARD_REFRESH);
                    });
                }

                return null;
            }

            @Override
            public void process(List<DashboardStatus> chunks) {
                logger.debug("Processing chunk: " + (chunks != null ? chunks.size() : "null"));
                if (chunks != null) {
                    TableState tableState = dashboardPanel.getCurrentTableState();
                    dashboardPanel.updateTableChannelNodes(chunks);
                    dashboardPanel.updateTableState(tableState);
                }
            }

            @Override
            public void done() {
                if (status != null) {
                    TableState tableState = dashboardPanel.getCurrentTableState();
                    /*
                     * The channel group cache could be out of date, so after we have the completed
                     * list of statuses, make sure any previously unknown channel IDs are added to
                     * the default group.
                     */
                    channelPanel.updateDefaultChannelGroup(status);
                    dashboardPanel.finishUpdatingTable(status, channelPanel.getCachedGroupStatuses().values(), deployedChannelCount);
                    dashboardPanel.updateTableState(tableState);
                }
            }
        };

        new QueuingSwingWorker<Void, DashboardStatus>(task, queue).executeDelegate();
    }

    public int getDeployedChannelCount() {
        return deployedChannelCount;
    }

    public void doStart() {
        final Set<DashboardStatus> selectedStatuses = dashboardPanel.getSelectedChannelStatuses();

        if (selectedStatuses.size() == 0) {
            return;
        }

        if (!getStatusesWithDependencies(selectedStatuses, ChannelTask.START_RESUME)) {
            return;
        }

        final String workingId = startWorking("Starting or resuming channels...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            public Void doInBackground() {
                Set<String> startChannelIds = new HashSet<String>();
                Set<String> resumeChannelIds = new HashSet<String>();

                for (DashboardStatus dashboardStatus : selectedStatuses) {
                    if (dashboardStatus.getState() == DeployedState.PAUSED) {
                        resumeChannelIds.add(dashboardStatus.getChannelId());
                    } else {
                        startChannelIds.add(dashboardStatus.getChannelId());
                    }
                }

                try {
                    if (!startChannelIds.isEmpty()) {
                        mirthClient.startChannels(startChannelIds);
                    }

                    if (!resumeChannelIds.isEmpty()) {
                        mirthClient.resumeChannels(resumeChannelIds);
                    }
                } catch (ClientException e) {
                    SwingUtilities.invokeLater(() -> {
                        alertThrowable(PlatformUI.MIRTH_FRAME, e);
                    });
                }

                return null;
            }

            public void done() {
                doRefreshStatuses(true);
                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doStop() {
        final Set<DashboardStatus> selectedStatuses = dashboardPanel.getSelectedChannelStatuses();

        if (selectedStatuses.size() == 0) {
            return;
        }

        if (!getStatusesWithDependencies(selectedStatuses, ChannelTask.STOP)) {
            return;
        }

        final String workingId = startWorking("Stopping channel...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            public Void doInBackground() {
                Set<String> channelIds = new HashSet<String>();
                for (DashboardStatus dashboardStatus : selectedStatuses) {
                    channelIds.add(dashboardStatus.getChannelId());
                }

                try {
                    if (!channelIds.isEmpty()) {
                        mirthClient.stopChannels(channelIds);
                    }
                } catch (ClientException e) {
                    SwingUtilities.invokeLater(() -> {
                        alertThrowable(PlatformUI.MIRTH_FRAME, e);
                    });
                }

                return null;
            }

            public void done() {
                doRefreshStatuses(true);
                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doHalt() {
        final Set<DashboardStatus> selectedStatuses = dashboardPanel.getSelectedChannelStatuses();

        int size = selectedStatuses.size();
        if (size == 0 || !alertOption(this, "Are you sure you want to halt " + (size == 1 ? "this channel" : "these channels") + "?")) {
            return;
        }

        final String workingId = startWorking("Halting channels...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            public Void doInBackground() {
                Set<String> channelIds = new HashSet<String>();
                for (DashboardStatus dashboardStatus : selectedStatuses) {
                    channelIds.add(dashboardStatus.getChannelId());
                }

                try {
                    if (!channelIds.isEmpty()) {
                        mirthClient.haltChannels(channelIds);
                    }
                } catch (ClientException e) {
                    SwingUtilities.invokeLater(() -> {
                        alertThrowable(PlatformUI.MIRTH_FRAME, e);
                    });
                }

                return null;
            }

            public void done() {
                doRefreshStatuses(true);
                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doPause() {
        final Set<DashboardStatus> selectedChannelStatuses = dashboardPanel.getSelectedChannelStatuses();

        if (selectedChannelStatuses.size() == 0) {
            return;
        }

        if (!getStatusesWithDependencies(selectedChannelStatuses, ChannelTask.PAUSE)) {
            return;
        }

        final String workingId = startWorking("Pausing channels...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                Set<String> channelIds = new HashSet<String>();
                for (DashboardStatus dashboardStatus : selectedChannelStatuses) {
                    channelIds.add(dashboardStatus.getChannelId());
                }

                try {
                    if (!channelIds.isEmpty()) {
                        mirthClient.pauseChannels(channelIds);
                    }
                } catch (ClientException e) {
                    SwingUtilities.invokeLater(() -> {
                        alertThrowable(PlatformUI.MIRTH_FRAME, e);
                    });
                }
                return null;
            }

            public void done() {
                doRefreshStatuses(true);
                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doStartConnector() {
        final List<DashboardStatus> selectedStatuses = dashboardPanel.getSelectedStatuses();

        if (selectedStatuses.size() == 0) {
            return;
        }

        final String workingId = startWorking("Starting connectors...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            public Void doInBackground() {
                Map<String, List<Integer>> connectorInfo = new HashMap<String, List<Integer>>();

                for (DashboardStatus dashboardStatus : selectedStatuses) {
                    String channelId = dashboardStatus.getChannelId();
                    Integer metaDataId = dashboardStatus.getMetaDataId();

                    if (metaDataId != null) {
                        if (!connectorInfo.containsKey(channelId)) {
                            connectorInfo.put(channelId, new ArrayList<Integer>());
                        }
                        connectorInfo.get(channelId).add(metaDataId);
                    }
                }

                try {
                    if (!connectorInfo.isEmpty()) {
                        mirthClient.startConnectors(connectorInfo);
                    }
                } catch (ClientException e) {
                    SwingUtilities.invokeLater(() -> {
                        alertThrowable(PlatformUI.MIRTH_FRAME, e);
                    });
                }

                return null;
            }

            public void done() {
                doRefreshStatuses(true);
                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doStopConnector() {
        final List<DashboardStatus> selectedStatuses = dashboardPanel.getSelectedStatuses();

        if (selectedStatuses.size() == 0) {
            return;
        }

        boolean warnQueueDisabled = false;
        for (Iterator<DashboardStatus> it = selectedStatuses.iterator(); it.hasNext();) {
            DashboardStatus dashboardStatus = it.next();
            if (dashboardStatus.getMetaDataId() != 0 && !dashboardStatus.isQueueEnabled()) {
                warnQueueDisabled = true;
                it.remove();
            }
        }

        final String workingId = startWorking("Stopping connectors...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            public Void doInBackground() {
                Map<String, List<Integer>> connectorInfo = new HashMap<String, List<Integer>>();

                for (DashboardStatus dashboardStatus : selectedStatuses) {
                    String channelId = dashboardStatus.getChannelId();
                    Integer metaDataId = dashboardStatus.getMetaDataId();

                    if (metaDataId != null) {
                        if (!connectorInfo.containsKey(channelId)) {
                            connectorInfo.put(channelId, new ArrayList<Integer>());
                        }
                        connectorInfo.get(channelId).add(metaDataId);
                    }
                }

                try {
                    if (!connectorInfo.isEmpty()) {
                        mirthClient.stopConnectors(connectorInfo);
                    }
                } catch (ClientException e) {
                    SwingUtilities.invokeLater(() -> {
                        alertThrowable(PlatformUI.MIRTH_FRAME, e);
                    });
                }

                return null;
            }

            public void done() {
                doRefreshStatuses(true);
                stopWorking(workingId);
            }
        };

        worker.execute();

        if (warnQueueDisabled) {
            alertWarning(this, "<html>One or more destination connectors were not stopped because queueing was not enabled.<br>Queueing must be enabled for a destination connector to be stopped individually.</html>");
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

        final String workingId = startWorking("Deleting user...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                if (users.size() == 1) {
                    alertWarning(PlatformUI.MIRTH_FRAME, "You must have at least one user account.");
                    return null;
                }

                int userToDelete = userPanel.getUserIndex();

                try {
                    if (userToDelete != UIConstants.ERROR_CONSTANT) {
                        mirthClient.removeUser(users.get(userToDelete).getId());
                        retrieveUsers();
                    }
                } catch (ClientException e) {
                    SwingUtilities.invokeLater(() -> {
                        alertThrowable(PlatformUI.MIRTH_FRAME, e);
                    });
                }
                return null;
            }

            public void done() {
                userPanel.updateUserTable();
                userPanel.deselectRows();
                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doRefreshUser() {
        final String workingId = startWorking("Loading users...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                refreshUser();
                return null;
            }

            public void done() {
                stopWorking(workingId);
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
            alertThrowable(this, e);
        }

        // as long as the channel was not deleted
        if (userName != null) {
            userPanel.setSelectedUser(userName);
        }
    }

    public void doDeployFromChannelView() {
        String channelId = channelEditPanel.currentChannel.getId();

        if (isSaveEnabled()) {
            if (alertOption(PlatformUI.MIRTH_FRAME, "<html>This channel will be saved before it is deployed.<br/>Are you sure you want to save and deploy this channel?</html>")) {
                if (channelEditPanel.saveChanges()) {
                    setSaveEnabled(false);
                } else {
                    return;
                }
            } else {
                return;
            }
        } else {
            if (!alertOption(PlatformUI.MIRTH_FRAME, "Are you sure you want to deploy this channel?")) {
                return;
            }
        }

        ChannelStatus channelStatus = channelPanel.getCachedChannelStatuses().get(channelId);
        if (channelStatus == null) {
            alertWarning(this, "The channel cannot be found and will not be deployed.");
            return;
        }

        if (!channelStatus.getChannel().getExportData().getMetadata().isEnabled()) {
            alertWarning(this, "The channel is disabled and will not be deployed.");
            return;
        }

        deployChannel(Collections.singleton(channelId));
    }

    public void deployChannel(final Set<String> selectedChannelIds) {
        if (CollectionUtils.isNotEmpty(selectedChannelIds)) {
            String plural = (selectedChannelIds.size() > 1) ? "s" : "";
            final String workingId = startWorking("Deploying channel" + plural + "...");

            dashboardPanel.deselectRows(false);
            doShowDashboard();

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                public Void doInBackground() {
                    try {
                        mirthClient.deployChannels(selectedChannelIds);
                    } catch (ClientException e) {
                        SwingUtilities.invokeLater(() -> {
                            alertThrowable(PlatformUI.MIRTH_FRAME, e);
                        });
                    }
                    return null;
                }

                public void done() {
                    stopWorking(workingId);
                    doRefreshStatuses(true);
                }
            };

            worker.execute();
        }
    }

    public enum ChannelTask {
        DEPLOY("deploy", "(re)deployed", true), UNDEPLOY("undeploy", "undeployed",
                false), START_RESUME("start/resume", "started or resumed",
                        true), STOP("stop", "stopped", false), PAUSE("pause", "paused", false);

        private String value;
        private String futurePassive;
        private boolean forwardOrder;

        private ChannelTask(String value, String futurePassive, boolean forwardOrder) {
            this.value = value;
            this.futurePassive = futurePassive;
            this.forwardOrder = forwardOrder;
        }

        public String getFuturePassive() {
            return futurePassive;
        }

        public boolean isForwardOrder() {
            return forwardOrder;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * Adds dependent/dependency dashboard statuses to the given set, based on the type of task
     * being performed.
     */
    private boolean getStatusesWithDependencies(Set<DashboardStatus> selectedDashboardStatuses, ChannelTask task) {
        try {
            ChannelDependencyGraph channelDependencyGraph = new ChannelDependencyGraph(channelPanel.getCachedChannelDependencies());
            Set<String> selectedChannelIds = new HashSet<String>();
            Set<String> channelIdsToHandle = new HashSet<String>();

            Map<String, DashboardStatus> statusMap = new HashMap<String, DashboardStatus>();
            for (DashboardStatus dashboardStatus : status) {
                statusMap.put(dashboardStatus.getChannelId(), dashboardStatus);
            }

            // For each selected channel, add any dependent/dependency channels as necessary
            for (DashboardStatus dashboardStatus : selectedDashboardStatuses) {
                selectedChannelIds.add(dashboardStatus.getChannelId());
                addChannelToTaskSet(dashboardStatus.getChannelId(), channelDependencyGraph, statusMap, channelIdsToHandle, task);
            }

            // If additional channels were added to the set, we need to prompt the user
            if (!CollectionUtils.subtract(channelIdsToHandle, selectedChannelIds).isEmpty()) {
                ChannelDependenciesWarningDialog dialog = new ChannelDependenciesWarningDialog(task, channelPanel.getCachedChannelDependencies(), selectedChannelIds, channelIdsToHandle);
                if (dialog.getResult() == JOptionPane.OK_OPTION) {
                    if (dialog.isIncludeOtherChannels()) {
                        for (String channelId : channelIdsToHandle) {
                            selectedDashboardStatuses.add(statusMap.get(channelId));
                        }
                    }
                } else {
                    return false;
                }
            }
        } catch (ChannelDependencyException e) {
            // Should never happen
            e.printStackTrace();
        }

        return true;
    }

    private void addChannelToTaskSet(String channelId, ChannelDependencyGraph channelDependencyGraph, Map<String, DashboardStatus> statusMap, Set<String> channelIdsToHandle, ChannelTask task) {
        if (!channelIdsToHandle.add(channelId)) {
            return;
        }

        DirectedAcyclicGraphNode<String> node = channelDependencyGraph.getNode(channelId);

        if (node != null) {
            if (task.isForwardOrder()) {
                // Add dependency channels for the start/resume task.
                for (String dependencyChannelId : node.getDirectDependencyElements()) {
                    // Only add the dependency channel if it's currently deployed and not already started.
                    if (statusMap.containsKey(dependencyChannelId)) {
                        DashboardStatus dashboardStatus = statusMap.get(dependencyChannelId);
                        if (dashboardStatus.getState() != DeployedState.STARTED) {
                            addChannelToTaskSet(dependencyChannelId, channelDependencyGraph, statusMap, channelIdsToHandle, task);
                        }
                    }
                }
            } else {
                // Add dependent channels for the undeploy/stop/pause tasks.
                for (String dependentChannelId : node.getDirectDependentElements()) {
                    /*
                     * Only add the dependent channel if it's currently deployed, and it's not
                     * already stopped/paused (depending on the task being performed).
                     */
                    if (statusMap.containsKey(dependentChannelId)) {
                        DashboardStatus dashboardStatus = statusMap.get(dependentChannelId);
                        if (task == ChannelTask.UNDEPLOY || task == ChannelTask.STOP && dashboardStatus.getState() != DeployedState.STOPPED || task == ChannelTask.PAUSE && dashboardStatus.getState() != DeployedState.PAUSED && dashboardStatus.getState() != DeployedState.STOPPED) {
                            addChannelToTaskSet(dependentChannelId, channelDependencyGraph, statusMap, channelIdsToHandle, task);
                        }
                    }
                }
            }
        }
    }

    public void doUndeployChannel() {
        final Set<DashboardStatus> selectedChannelStatuses = dashboardPanel.getSelectedChannelStatuses();

        if (selectedChannelStatuses.size() == 0) {
            return;
        }

        if (!getStatusesWithDependencies(selectedChannelStatuses, ChannelTask.UNDEPLOY)) {
            return;
        }

        dashboardPanel.deselectRows(false);

        String plural = (selectedChannelStatuses.size() > 1) ? "s" : "";
        final String workingId = startWorking("Undeploying channel" + plural + "...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    Set<String> channelIds = new LinkedHashSet<String>();

                    for (DashboardStatus channelStatus : selectedChannelStatuses) {
                        channelIds.add(channelStatus.getChannelId());
                    }

                    mirthClient.undeployChannels(channelIds);
                } catch (ClientException e) {
                    SwingUtilities.invokeLater(() -> {
                        alertThrowable(PlatformUI.MIRTH_FRAME, e);
                    });
                }
                return null;
            }

            public void done() {
                stopWorking(workingId);
                doRefreshStatuses(true);
            }
        };

        worker.execute();
    }

    public void doSaveChannel() {
        final String workingId = startWorking("Saving channel...");

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
                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public boolean changesHaveBeenMade() {
        if (currentContentPage == channelPanel) {
            return channelPanel.changesHaveBeenMade();
        } else if (channelEditPanel != null && currentContentPage == channelEditPanel) {
            return channelEditTasks.getContentPane().getComponent(0).isVisible();
        } else if (channelEditPanel != null && currentContentPage == channelEditPanel.transformerPane) {
            return channelEditPanel.transformerPane.isModified();
        } else if (channelEditPanel != null && currentContentPage == channelEditPanel.filterPane) {
            return channelEditPanel.filterPane.isModified();
        } else if (settingsPane != null && currentContentPage == settingsPane) {
            return settingsPane.getCurrentSettingsPanel().isSaveEnabled();
        } else if (alertEditPanel != null && currentContentPage == alertEditPanel) {
            return alertEditTasks.getContentPane().getComponent(0).isVisible();
        } else if (globalScriptsPanel != null && currentContentPage == globalScriptsPanel) {
            return globalScriptsTasks.getContentPane().getComponent(0).isVisible();
        } else if (currentContentPage == codeTemplatePanel) {
            return codeTemplatePanel.changesHaveBeenMade();
        } else {
            return false;
        }
    }

    public void doShowMessages() {
        if (messageBrowser == null) {
            messageBrowser = new MessageBrowser();
        }

        String id = "";
        String channelName = "";
        boolean channelDeployed = true;
        Integer channelRevision = null;

        final List<Integer> metaDataIds = new ArrayList<Integer>();
        if (currentContentPage == dashboardPanel) {
            List<DashboardStatus> selectedStatuses = dashboardPanel.getSelectedStatuses();
            Set<DashboardStatus> selectedChannelStatuses = dashboardPanel.getSelectedChannelStatuses();

            if (selectedStatuses.size() == 0) {
                return;
            }

            if (selectedChannelStatuses.size() > 1) {
                JOptionPane.showMessageDialog(Frame.this, "This operation can only be performed on a single channel.");
                return;
            }

            for (DashboardStatus status : selectedStatuses) {
                metaDataIds.add(status.getMetaDataId());
            }

            id = selectedStatuses.get(0).getChannelId();
            channelName = selectedChannelStatuses.iterator().next().getName();
            channelRevision = 0;
        } else if (currentContentPage == channelPanel) {
            Channel selectedChannel = channelPanel.getSelectedChannels().get(0);

            metaDataIds.add(null);

            id = selectedChannel.getId();
            channelName = selectedChannel.getName();
            channelRevision = selectedChannel.getRevision();

            channelDeployed = false;
            for (DashboardStatus dashStatus : status) {
                if (dashStatus.getChannelId().equals(id)) {
                    channelDeployed = true;
                }
            }
        }

        setBold(viewPane, -1);
        setPanelName("Channel Messages - " + channelName);
        setCurrentContentPage(messageBrowser);
        setFocus(messageTasks);

        final String channelId = id;
        final boolean isChannelDeployed = channelDeployed;

        final String workingId = startWorking("Retrieving channel metadata...");
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            Map<Integer, String> connectors;
            List<MetaDataColumn> metaDataColumns;

            public Void doInBackground() {
                try {
                    connectors = mirthClient.getConnectorNames(channelId);
                    metaDataColumns = mirthClient.getMetaDataColumns(channelId);
                } catch (ClientException e) {
                    SwingUtilities.invokeLater(() -> {
                        alertThrowable(PlatformUI.MIRTH_FRAME, e);
                    });
                }
                return null;
            }

            public void done() {
                stopWorking(workingId);

                if (connectors == null || metaDataColumns == null) {
                    alertError(PlatformUI.MIRTH_FRAME, "Could not retrieve metadata for channel.");
                } else {
                    messageBrowser.loadChannel(channelId, connectors, metaDataColumns, metaDataIds, isChannelDeployed);
                }
            }
        };

        worker.execute();
    }

    public void doShowEvents() {
        doShowEvents(null);
    }

    public void doShowEvents(String eventNameFilter) {
        if (!confirmLeave()) {
            return;
        }

        if (eventBrowser == null) {
            eventBrowser = new EventBrowser();
        }

        setBold(viewPane, 5);
        setPanelName("Events");
        setCurrentContentPage(eventBrowser);
        setFocus(eventTasks);

        eventBrowser.loadNew(eventNameFilter);
    }

    public void doEditTransformer() {
        channelEditPanel.transformerPane.resizePanes();
        String name = channelEditPanel.editTransformer();
        setPanelName("Edit Channel - " + channelEditPanel.currentChannel.getName() + " - " + name + " Transformer");
    }

    public void doEditResponseTransformer() {
        channelEditPanel.transformerPane.resizePanes();
        String name = channelEditPanel.editResponseTransformer();
        setPanelName("Edit Channel - " + channelEditPanel.currentChannel.getName() + " - " + name + " Response Transformer");
    }

    public void doEditFilter() {
        channelEditPanel.filterPane.resizePanes();
        String name = channelEditPanel.editFilter();
        setPanelName("Edit Channel - " + channelEditPanel.currentChannel.getName() + " - " + name + " Filter");
    }

    public void updateFilterTaskName(int rules) {
        updateFilterOrTransformerTaskName(UIConstants.EDIT_FILTER, UIConstants.EDIT_FILTER_TASK_NUMBER, rules, false);
    }

    public void updateTransformerTaskName(int steps, boolean outboundTemplate) {
        updateFilterOrTransformerTaskName(UIConstants.EDIT_TRANSFORMER, UIConstants.EDIT_TRANSFORMER_TASK_NUMBER, steps, outboundTemplate);
    }

    public void updateResponseTransformerTaskName(int steps, boolean outboundTemplate) {
        updateFilterOrTransformerTaskName(UIConstants.EDIT_RESPONSE_TRANSFORMER, UIConstants.EDIT_RESPONSE_TRANSFORMER_TASK_NUMBER, steps, outboundTemplate);
    }

    private void updateFilterOrTransformerTaskName(String taskName, int componentIndex, int rulesOrSteps, boolean outboundTemplate) {
        if (rulesOrSteps > 0) {
            taskName += " (" + rulesOrSteps + ")";
        } else if (outboundTemplate) {
            taskName += " (0)";
        }

        ((JXHyperlink) channelEditTasks.getContentPane().getComponent(componentIndex)).setText(taskName);
        ((JMenuItem) channelEditPopupMenu.getComponent(componentIndex)).setText(taskName);
    }

    public void doValidate() {
        channelEditPanel.doValidate();
    }

    public boolean doExportChannel() {
        return channelPanel.doExportChannel();
    }

    /**
     * Import a file with the default defined file filter type.
     * 
     * @return
     */
    public String browseForFileString(String fileExtension) {
        File file = browseForFile(fileExtension);

        if (file != null) {
            return readFileToString(file);
        }

        return null;
    }

    /**
     * Read the bytes from a file with the default defined file filter type.
     * 
     * @return
     */
    public byte[] browseForFileBytes(String fileExtension) {
        File file = browseForFile(fileExtension);

        if (file != null) {
            try {
                return FileUtils.readFileToByteArray(file);
            } catch (IOException e) {
                alertError(this, "Unable to read file.");
            }
        }

        return null;
    }

    public String readFileToString(File file) {
        try {
            String content = FileUtils.readFileToString(file, UIConstants.CHARSET);

            if (StringUtils.startsWith(content, EncryptionSettings.ENCRYPTION_PREFIX)) {
                return mirthClient.getEncryptor().decrypt(StringUtils.removeStart(content, EncryptionSettings.ENCRYPTION_PREFIX));
            } else {
                return content;
            }
        } catch (IOException e) {
            alertError(this, "Unable to read file.");
        }

        return null;
    }

    public File browseForFile(String fileExtension) {
        JFileChooser importFileChooser = new JFileChooser();

        if (fileExtension != null) {
            importFileChooser.setFileFilter(new MirthFileFilter(fileExtension));
        }

        File currentDir = new File(userPreferences.get("currentDirectory", ""));

        if (currentDir.exists()) {
            importFileChooser.setCurrentDirectory(currentDir);
        }

        if (importFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            userPreferences.put("currentDirectory", importFileChooser.getCurrentDirectory().getPath());
            return importFileChooser.getSelectedFile();
        }

        return null;
    }

    /**
     * Creates a File with the default defined file filter type, but does not yet write to it.
     * 
     * @param defaultFileName
     * @param fileExtension
     * @return
     */
    public File createFileForExport(String defaultFileName, String fileExtension) {
        JFileChooser exportFileChooser = new JFileChooser();

        if (defaultFileName != null) {
            exportFileChooser.setSelectedFile(new File(defaultFileName));
        }

        if (fileExtension != null) {
            exportFileChooser.setFileFilter(new MirthFileFilter(fileExtension));
        }

        File currentDir = new File(userPreferences.get("currentDirectory", ""));

        if (currentDir.exists()) {
            exportFileChooser.setCurrentDirectory(currentDir);
        }

        if (exportFileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            userPreferences.put("currentDirectory", exportFileChooser.getCurrentDirectory().getPath());
            File exportFile = exportFileChooser.getSelectedFile();

            if ((exportFile.getName().length() < 4) || !FilenameUtils.getExtension(exportFile.getName()).equalsIgnoreCase(fileExtension)) {
                exportFile = new File(exportFile.getAbsolutePath() + "." + fileExtension.toLowerCase());
            }

            if (exportFile.exists()) {
                if (!alertOption(this, "This file already exists.  Would you like to overwrite it?")) {
                    return null;
                }
            }

            return exportFile;
        } else {
            return null;
        }
    }

    /**
     * Export a file with the default defined file filter type.
     * 
     * @param fileContents
     * @param fileName
     * @return
     */
    public boolean exportFile(String fileContents, String defaultFileName, String fileExtension, String name) {
        return exportFile(fileContents, createFileForExport(defaultFileName, fileExtension), name);
    }

    public boolean exportFile(String fileContents, File exportFile, String name) {
        if (exportFile != null) {
            try {
                String contentToWrite = null;

                if (mirthClient.isEncryptExport()) {
                    contentToWrite = EncryptionSettings.ENCRYPTION_PREFIX + mirthClient.getEncryptor().encrypt(fileContents);
                } else {
                    contentToWrite = fileContents;
                }

                FileUtils.writeStringToFile(exportFile, contentToWrite, UIConstants.CHARSET);
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
        String content = browseForFileString("XML");

        if (content != null) {
            try {
                channelEditPanel.importConnector(ObjectXMLSerializer.getInstance().deserialize(content, Connector.class));
            } catch (Exception e) {
                alertThrowable(this, e);
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

        // Update resource names
        updateResourceNames(connector);

        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        String connectorXML = serializer.serialize(connector);

        String fileName = channelEditPanel.currentChannel.getName();
        if (connector.getMode().equals(Mode.SOURCE)) {
            fileName += " Source";
        } else {
            fileName += " " + connector.getName();
        }

        exportFile(connectorXML, fileName, "XML", "Connector");
    }

    public void doRefreshMessages() {
        messageBrowser.refresh(null, true);
    }

    public void doSendMessage() {
        String channelId = null;
        List<Integer> selectedMetaDataIds = null;

        if (currentContentPage == dashboardPanel) {
            List<DashboardStatus> selectedStatuses = dashboardPanel.getSelectedStatuses();
            channelId = selectedStatuses.get(0).getChannelId();
            selectedMetaDataIds = new ArrayList<Integer>();

            for (DashboardStatus status : selectedStatuses) {
                if (status.getChannelId() != channelId) {
                    JOptionPane.showMessageDialog(Frame.this, "This operation can only be performed on a single channel.");
                    return;
                }

                if (status.getStatusType() == StatusType.CHANNEL) {
                    selectedMetaDataIds = null;
                } else if (selectedMetaDataIds != null) {
                    Integer metaDataId = status.getMetaDataId();

                    if (metaDataId != null) {
                        selectedMetaDataIds.add(metaDataId);
                    }
                }
            }
        } else if (currentContentPage == messageBrowser) {
            channelId = messageBrowser.getChannelId();
        }

        /*
         * If the user has not yet navigated to channels at this point, the cache (channelStatuses
         * object) will return null, and the resulting block will pull down the channelStatus for
         * the given id.
         */
        ChannelStatus channelStatus = channelPanel.getCachedChannelStatuses().get(channelId);
        if (channelStatus == null) {
            try {
                Map<String, ChannelHeader> channelHeaders = new HashMap<String, ChannelHeader>();
                channelHeaders.put(channelId, new ChannelHeader(0, null, true));
                channelPanel.updateChannelStatuses(mirthClient.getChannelSummary(channelHeaders, true));
                channelStatus = channelPanel.getCachedChannelStatuses().get(channelId);
            } catch (ClientException e) {
                alertThrowable(PlatformUI.MIRTH_FRAME, e);
            }
        }

        if (channelId == null || channelStatus == null) {
            alertError(this, "Channel no longer exists!");
            return;
        }

        editMessageDialog.setPropertiesAndShow("", channelStatus.getChannel().getSourceConnector().getTransformer().getInboundDataType(), channelStatus.getChannel().getId(), dashboardPanel.getDestinationConnectorNames(channelId), selectedMetaDataIds, new HashMap<String, Object>());
    }

    public void doExportMessages() {
        if (messageExportDialog == null) {
            messageExportDialog = new MessageExportDialog();
        }

        messageExportDialog.setEncryptor(mirthClient.getEncryptor());
        messageExportDialog.setMessageFilter(messageBrowser.getMessageFilter());
        messageExportDialog.setPageSize(messageBrowser.getPageSize());
        messageExportDialog.setChannelId(messageBrowser.getChannelId());
        messageExportDialog.setLocationRelativeTo(this);
        messageExportDialog.setVisible(true);
    }

    public void doImportMessages() {
        if (messageImportDialog == null) {
            messageImportDialog = new MessageImportDialog();
        }

        messageImportDialog.setChannelId(messageBrowser.getChannelId());
        messageImportDialog.setMessageBrowser(messageBrowser);
        messageImportDialog.setLocationRelativeTo(this);
        messageImportDialog.setVisible(true);
    }

    public void doRemoveAllMessages() {
        if (removeMessagesDialog == null) {
            removeMessagesDialog = new RemoveMessagesDialog(this, true);
        }

        Set<String> channelIds = new HashSet<String>();
        boolean restartCheckboxEnabled = false;

        if (currentContentPage instanceof MessageBrowser) {
            String channelId = ((MessageBrowser) currentContentPage).getChannelId();
            channelIds.add(channelId);

            for (DashboardStatus channelStatus : status) {
                if (channelStatus.getChannelId().equals(channelId)) {
                    if (!channelStatus.getState().equals(DeployedState.STOPPED) && !restartCheckboxEnabled) {
                        restartCheckboxEnabled = true;
                    }
                }
            }
        } else {
            for (DashboardStatus channelStatus : dashboardPanel.getSelectedChannelStatuses()) {
                channelIds.add(channelStatus.getChannelId());

                if (!channelStatus.getState().equals(DeployedState.STOPPED) && !restartCheckboxEnabled) {
                    restartCheckboxEnabled = true;
                }
            }
        }

        removeMessagesDialog.init(channelIds, restartCheckboxEnabled);
        removeMessagesDialog.setLocationRelativeTo(this);
        removeMessagesDialog.setVisible(true);
    }

    public void doClearStats() {
        List<DashboardStatus> channelStatuses = dashboardPanel.getSelectedStatusesRecursive();

        if (channelStatuses.size() != 0) {
            new DeleteStatisticsDialog(channelStatuses);
        } else {
            dashboardPanel.deselectRows(false);
        }
    }

    public void clearStats(List<DashboardStatus> statusesToClear, final boolean deleteReceived, final boolean deleteFiltered, final boolean deleteSent, final boolean deleteErrored) {
        final String workingId = startWorking("Clearing statistics...");
        Map<String, List<Integer>> channelConnectorMap = new HashMap<String, List<Integer>>();

        for (DashboardStatus status : statusesToClear) {
            String channelId = status.getChannelId();
            Integer metaDataId = status.getMetaDataId();

            List<Integer> metaDataIds = channelConnectorMap.get(channelId);

            if (metaDataIds == null) {
                metaDataIds = new ArrayList<Integer>();
                channelConnectorMap.put(channelId, metaDataIds);
            }

            metaDataIds.add(metaDataId);
        }

        final Map<String, List<Integer>> channelConnectorMapFinal = channelConnectorMap;

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    mirthClient.clearStatistics(channelConnectorMapFinal, deleteReceived, deleteFiltered, deleteSent, deleteErrored);
                } catch (ClientException e) {
                    SwingUtilities.invokeLater(() -> {
                        alertThrowable(PlatformUI.MIRTH_FRAME, e);
                    });
                }
                return null;
            }

            public void done() {
                doRefreshStatuses(true);
                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doRemoveFilteredMessages() {
        if (alertOption(this, "<html><font color=\"red\"><b>Warning:</b></font> This will remove <b>all</b> results for the current search criteria,<br/>including those not listed on the current page. To see how many messages will<br/>be removed, close this dialog and click the Count button in the upper-right.<br/><font size='1'><br/></font><font color=\"red\"><b>Warning:</b></font> Removing a Source message will remove all of its destinations.<br/><font size='1'><br/></font>Are you sure you would like to remove all messages that match<br/>the current search criteria (including QUEUED) in this channel?<br/>Channel must be stopped for unfinished messages to be removed.</html>")) {
            if (userPreferences.getBoolean("showReprocessRemoveMessagesWarning", true)) {
                String result = JOptionPane.showInputDialog(this, "<html>This will remove all messages that match the current search criteria.<br/>To see how many messages will be removed, close this dialog and<br/>click the Count button in the upper-right.<br><font size='1'><br></font>Type REMOVEALL and click the OK button to continue.</html>", "Remove Results", JOptionPane.WARNING_MESSAGE);
                if (!StringUtils.equals(result, "REMOVEALL")) {
                    alertWarning(this, "You must type REMOVEALL to remove results.");
                    return;
                }
            }

            final String workingId = startWorking("Removing messages...");

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                public Void doInBackground() {
                    try {
                        mirthClient.removeMessages(messageBrowser.getChannelId(), messageBrowser.getMessageFilter());
                    } catch (ClientException e) {
                        if (e instanceof RequestAbortedException) {
                            // The client is no longer waiting for the delete request
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                alertThrowable(PlatformUI.MIRTH_FRAME, e);
                            });
                        }
                    }
                    return null;
                }

                public void done() {
                    if (currentContentPage == dashboardPanel) {
                        doRefreshStatuses(true);
                    } else if (currentContentPage == messageBrowser) {
                        messageBrowser.refresh(1, true);
                    }
                    stopWorking(workingId);
                }
            };

            worker.execute();
        }
    }

    public void doRemoveMessage() {
        final Integer metaDataId = messageBrowser.getSelectedMetaDataId();
        final Long messageId = messageBrowser.getSelectedMessageId();
        final String channelId = messageBrowser.getChannelId();

        if (alertOption(this, "<html>Are you sure you would like to remove the selected message?<br>Channel must be stopped for an unfinished message to be removed.<br><font size='1'><br></font>WARNING: Removing a Source message will remove all of its destinations.</html>")) {
            final String workingId = startWorking("Removing message...");

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                public Void doInBackground() {
                    try {
                        mirthClient.removeMessage(channelId, messageId, metaDataId);
                    } catch (ClientException e) {
                        SwingUtilities.invokeLater(() -> {
                            alertThrowable(PlatformUI.MIRTH_FRAME, e);
                        });
                    }
                    return null;
                }

                public void done() {
                    if (currentContentPage == dashboardPanel) {
                        doRefreshStatuses(true);
                    } else if (currentContentPage == messageBrowser) {
                        messageBrowser.refresh(null, false);
                    }
                    stopWorking(workingId);
                }
            };

            worker.execute();
        }
    }

    public void doReprocessFilteredMessages() {
        doReprocess(messageBrowser.getMessageFilter(), null, null, true);
    }

    public void doReprocessMessage() {
        Long messageId = messageBrowser.getSelectedMessageId();

        if (messageBrowser.canReprocessMessage(messageId)) {
            doReprocess(null, messageId, messageBrowser.getSelectedMetaDataId(), false);
        } else {
            alertError(this, "Message " + messageId + " cannot be reprocessed because no source raw content was found.");
        }
    }

    private void doReprocess(final MessageFilter filter, final Long messageId, final Integer selectedMetaDataId, final boolean showWarning) {
        final String workingId = startWorking("Retrieving Channels...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            public Void doInBackground() {
                if (channelPanel.getCachedChannelStatuses() == null || channelPanel.getCachedChannelStatuses().values().size() == 0) {
                    channelPanel.retrieveChannels();
                }

                return null;
            }

            public void done() {
                stopWorking(workingId);
                Map<Integer, String> destinationConnectors = new LinkedHashMap<Integer, String>();
                destinationConnectors.putAll(dashboardPanel.getDestinationConnectorNames(messageBrowser.getChannelId()));
                new ReprocessMessagesDialog(messageBrowser.getChannelId(), filter, messageId, destinationConnectors, selectedMetaDataId, showWarning);
            }
        };

        worker.execute();
    }

    public void reprocessMessage(final String channelId, final MessageFilter filter, final Long messageId, final boolean replace, final Collection<Integer> reprocessMetaDataIds) {
        final String workingId = startWorking("Reprocessing messages...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            public Void doInBackground() {
                try {
                    if (filter != null) {
                        mirthClient.reprocessMessages(channelId, filter, replace, reprocessMetaDataIds);
                    } else if (messageId != null) {
                        mirthClient.reprocessMessage(channelId, messageId, replace, reprocessMetaDataIds);
                    }
                } catch (ClientException e) {
                    SwingUtilities.invokeLater(() -> {
                        alertThrowable(PlatformUI.MIRTH_FRAME, e);
                    });
                }
                return null;
            }

            public void done() {
                messageBrowser.updateFilterButtonFont(Font.BOLD);
                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void viewImage() {
        final String workingId = startWorking("Opening attachment...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                messageBrowser.viewAttachment();
                stopWorking(workingId);
                return null;
            }

            public void done() {
                stopWorking(workingId);
            }
        };
        worker.execute();
    }

    public void doExportAttachment() {
        if (attachmentExportDialog == null) {
            attachmentExportDialog = new AttachmentExportDialog();
        }

        attachmentExportDialog.setLocationRelativeTo(this);
        attachmentExportDialog.setVisible(true);
    }

    public void processMessage(final String channelId, final RawMessage rawMessage) {
        final String workingId = startWorking("Processing message...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    mirthClient.processMessage(channelId, rawMessage);
                } catch (ClientException e) {
                    SwingUtilities.invokeLater(() -> {
                        alertThrowable(PlatformUI.MIRTH_FRAME, e);
                    });
                }
                return null;
            }

            public void done() {
                if (currentContentPage == messageBrowser) {
                    messageBrowser.updateFilterButtonFont(Font.BOLD);
                }

                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doRefreshEvents() {
        eventBrowser.refresh(null);
    }

    public void doRemoveAllEvents() {
        int option = JOptionPane.showConfirmDialog(this, "All events will be removed. Would you also like them to be\n" + "exported to a file on the server?");
        if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
            return;
        }

        final boolean export = (option == JOptionPane.YES_OPTION);

        final String workingId = startWorking("Clearing events...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            private String exportPath = null;

            public Void doInBackground() {
                try {
                    if (export) {
                        exportPath = mirthClient.exportAndRemoveAllEvents();
                    } else {
                        mirthClient.removeAllEvents();
                    }
                } catch (ClientException e) {
                    SwingUtilities.invokeLater(() -> {
                        alertThrowable(PlatformUI.MIRTH_FRAME, e);
                    });
                }
                return null;
            }

            public void done() {
                eventBrowser.runSearch();

                if (exportPath != null) {
                    alertInformation(PlatformUI.MIRTH_FRAME, "Events have been exported to the following server path:\n" + exportPath);
                }

                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doExportAllEvents() {
        if (alertOption(this, "Are you sure you would like to export all events? An export\n" + "file will be placed in the exports directory on the server.")) {
            final String workingId = startWorking("Exporting events...");

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                private String exportPath = null;

                public Void doInBackground() {
                    try {
                        exportPath = mirthClient.exportAllEvents();
                    } catch (ClientException e) {
                        SwingUtilities.invokeLater(() -> {
                            alertThrowable(PlatformUI.MIRTH_FRAME, e);
                        });
                    }
                    return null;
                }

                public void done() {
                    if (exportPath != null) {
                        alertInformation(PlatformUI.MIRTH_FRAME, "Events have been exported to the following server path:\n" + exportPath);
                    }

                    stopWorking(workingId);
                }
            };

            worker.execute();
        }
    }

    public void doRefreshAlerts() {
        doRefreshAlerts(true);
    }

    public void doRefreshAlerts(boolean queue) {
        final List<String> selectedAlertIds = alertPanel.getSelectedAlertIds();

        QueuingSwingWorkerTask<Void, Void> task = new QueuingSwingWorkerTask<Void, Void>("doRefreshAlerts", "Loading alerts...") {

            private List<AlertStatus> alertStatusList;

            public Void doInBackground() {
                try {
                    alertStatusList = mirthClient.getAlertStatusList();
                } catch (ClientException e) {
                    SwingUtilities.invokeLater(() -> {
                        alertThrowable(PlatformUI.MIRTH_FRAME, e);
                    });
                }
                return null;
            }

            public void done() {
                alertPanel.updateAlertTable(alertStatusList);
                alertPanel.setSelectedAlertIds(selectedAlertIds);
            }
        };

        new QueuingSwingWorker<Void, Void>(task, queue).executeDelegate();
    }

    public void doSaveAlerts() {
        if (changesHaveBeenMade()) {
            try {
                ServerSettings serverSettings = mirthClient.getServerSettings();
                if (StringUtils.isBlank(serverSettings.getSmtpHost()) || StringUtils.isBlank(serverSettings.getSmtpPort())) {
                    alertWarning(PlatformUI.MIRTH_FRAME, "The SMTP server on the settings page is not specified or is incomplete.  An SMTP server is required to send email alerts.");
                }
            } catch (ClientException e) {
                alertThrowable(PlatformUI.MIRTH_FRAME, e, false);
            }

            final String workingId = startWorking("Saving alerts...");

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                public Void doInBackground() {
                    if (alertEditPanel.saveAlert()) {
                        setSaveEnabled(false);
                    }

                    return null;
                }

                public void done() {
                    stopWorking(workingId);
                }
            };

            worker.execute();
        }
    }

    public void doDeleteAlert() {
        if (!alertOption(this, "Are you sure you want to delete the selected alert(s)?")) {
            return;
        }

        final String workingId = startWorking("Deleting alert...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                List<String> selectedAlertIds = alertPanel.getSelectedAlertIds();

                for (String alertId : selectedAlertIds) {
                    try {
                        mirthClient.removeAlert(alertId);
                    } catch (ClientException e) {
                        SwingUtilities.invokeLater(() -> {
                            alertThrowable(PlatformUI.MIRTH_FRAME, e);
                        });
                        return null;
                    }
                }
                alertPanel.updateAlertDetails(new HashSet<String>(selectedAlertIds));

                return null;
            }

            public void done() {
                doRefreshAlerts(true);
                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doNewAlert() throws ClientException {
        AlertInfo alertInfo = mirthClient.getAlertInfo(channelPanel.getChannelHeaders());
        channelPanel.updateChannelStatuses(alertInfo.getChangedChannels());
        setupAlert(alertInfo.getProtocolOptions());
    }

    public void doEditAlert() {
        if (isEditingAlert) {
            return;
        } else {
            isEditingAlert = true;
        }

        List<String> selectedAlertIds = alertPanel.getSelectedAlertIds();
        if (selectedAlertIds.size() > 1) {
            JOptionPane.showMessageDialog(Frame.this, "This operation can only be performed on a single alert.");
        } else if (selectedAlertIds.size() == 0) {
            JOptionPane.showMessageDialog(Frame.this, "Alert no longer exists.");
        } else {
            try {
                AlertInfo alertInfo = mirthClient.getAlertInfo(selectedAlertIds.get(0), channelPanel.getChannelHeaders());

                if (alertInfo.getModel() == null) {
                    JOptionPane.showMessageDialog(Frame.this, "Alert no longer exists.");
                    doRefreshAlerts(true);
                } else {
                    channelPanel.updateChannelStatuses(alertInfo.getChangedChannels());
                    editAlert(alertInfo.getModel(), alertInfo.getProtocolOptions());
                }
            } catch (ClientException e) {
                alertThrowable(this, e);
            }
        }
        isEditingAlert = false;
    }

    public void doEnableAlert() {
        final String workingId = startWorking("Enabling alert...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                List<String> selectedAlertIds = alertPanel.getSelectedAlertIds();

                for (String alertId : selectedAlertIds) {
                    try {
                        mirthClient.enableAlert(alertId);
                    } catch (ClientException e) {
                        SwingUtilities.invokeLater(() -> {
                            alertThrowable(PlatformUI.MIRTH_FRAME, e);
                        });
                        return null;
                    }
                }

                return null;
            }

            public void done() {
                doRefreshAlerts(true);
                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doDisableAlert() {
        final String workingId = startWorking("Enabling alert...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                List<String> selectedAlertIds = alertPanel.getSelectedAlertIds();

                for (String alertId : selectedAlertIds) {
                    try {
                        mirthClient.disableAlert(alertId);
                    } catch (ClientException e) {
                        SwingUtilities.invokeLater(() -> {
                            alertThrowable(PlatformUI.MIRTH_FRAME, e);
                        });
                        return null;
                    }
                }

                return null;
            }

            public void done() {
                doRefreshAlerts(true);
                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doExportAlert() {
        if (changesHaveBeenMade()) {
            if (alertOption(this, "This alert has been modified. You must save the alert changes before you can export. Would you like to save them now?")) {
                if (!alertEditPanel.saveAlert()) {
                    return;
                }
            } else {
                return;
            }

            setSaveEnabled(false);
        }

        List<String> selectedAlertIds;

        if (currentContentPage == alertEditPanel) {
            selectedAlertIds = new ArrayList<String>();

            String alertId = alertEditPanel.getAlertId();
            if (alertId != null) {
                selectedAlertIds.add(alertId);
            }
        } else {
            selectedAlertIds = alertPanel.getSelectedAlertIds();
        }

        if (CollectionUtils.isEmpty(selectedAlertIds)) {
            return;
        }

        AlertModel alert;
        try {
            alert = mirthClient.getAlert(selectedAlertIds.get(0));
        } catch (ClientException e) {
            alertThrowable(this, e);
            return;
        }

        if (alert == null) {
            JOptionPane.showMessageDialog(Frame.this, "Alert no longer exists.");
            doRefreshAlerts(true);
        } else {
            ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
            String alertXML = serializer.serialize(alert);

            exportFile(alertXML, alert.getName(), "XML", "Alert");
        }
    }

    public void doExportAlerts() {
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

                List<AlertModel> alerts;
                try {
                    alerts = mirthClient.getAllAlerts();
                } catch (ClientException e) {
                    alertThrowable(this, e);
                    return;
                }

                for (AlertModel alert : alerts) {
                    ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
                    String channelXML = serializer.serialize(alert);

                    exportFile = new File(exportDirectory.getAbsolutePath() + "/" + alert.getName() + ".xml");

                    if (exportFile.exists()) {
                        if (!alertOption(this, "The file " + alert.getName() + ".xml already exists.  Would you like to overwrite it?")) {
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

    public void doImportAlert() {
        String content = browseForFileString("XML");

        if (content != null) {
            importAlert(content, true);
        }
    }

    public void importAlert(String alertXML, boolean showAlerts) {
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        List<AlertModel> alertList;

        try {
            alertList = (List<AlertModel>) serializer.deserializeList(alertXML.replaceAll("\\&\\#x0D;\\n", "\n").replaceAll("\\&\\#x0D;", "\n"), AlertModel.class);
        } catch (Exception e) {
            if (showAlerts) {
                alertThrowable(this, e, "Invalid alert file:\n" + e.getMessage());
            }
            return;
        }

        removeInvalidItems(alertList, AlertModel.class);

        for (AlertModel importAlert : alertList) {
            try {
                String alertName = importAlert.getName();
                String tempId = mirthClient.getGuid();

                // Check to see that the alert name doesn't already exist.
                if (!checkAlertName(alertName)) {
                    if (!alertOption(this, "Would you like to overwrite the existing alert?  Choose 'No' to create a new alert.")) {
                        do {
                            alertName = JOptionPane.showInputDialog(this, "Please enter a new name for the channel.", alertName);
                            if (alertName == null) {
                                return;
                            }
                        } while (!checkAlertName(alertName));

                        importAlert.setName(alertName);
                        importAlert.setId(tempId);
                    } else {
                        for (Entry<String, String> entry : alertPanel.getAlertNames().entrySet()) {
                            String id = entry.getKey();
                            String name = entry.getValue();
                            if (name.equalsIgnoreCase(alertName)) {
                                // If overwriting, use the old id
                                importAlert.setId(id);
                            }
                        }
                    }
                }

                mirthClient.updateAlert(importAlert);
            } catch (Exception e) {
                alertThrowable(this, e, "Error importing alert:\n" + e.getMessage());
            }
        }

        doRefreshAlerts(true);
    }

    /**
     * Checks to see if the passed in channel name already exists
     */
    public boolean checkAlertName(String name) {
        if (name.equals("")) {
            alertWarning(this, "Alert name cannot be empty.");
            return false;
        }

        Pattern alphaNumericPattern = Pattern.compile("^[a-zA-Z_0-9\\-\\s]*$");
        Matcher matcher = alphaNumericPattern.matcher(name);

        if (!matcher.find()) {
            alertWarning(this, "Alert name cannot have special characters besides hyphen, underscore, and space.");
            return false;
        }

        for (String alertName : alertPanel.getAlertNames().values()) {
            if (alertName.equalsIgnoreCase(name)) {
                alertWarning(this, "Alert \"" + name + "\" already exists.");
                return false;
            }
        }
        return true;
    }

    ///// Start Extension Tasks /////
    public void doRefreshExtensions() {
        final String workingId = startWorking("Loading extension settings...");

        if (confirmLeave()) {
            refreshExtensions();
        }

        stopWorking(workingId);
    }

    public void refreshExtensions() {
        extensionsPanel.setPluginData(getPluginMetaData());
        extensionsPanel.setConnectorData(getConnectorMetaData());
    }

    public void doEnableExtension() {
        final String workingId = startWorking("Enabling extension...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private boolean success = true;

            public Void doInBackground() {
                try {
                    mirthClient.setExtensionEnabled(extensionsPanel.getSelectedExtension().getName(), true);
                } catch (ClientException e) {
                    success = false;
                    SwingUtilities.invokeLater(() -> {
                        alertThrowable(PlatformUI.MIRTH_FRAME, e);
                    });
                }

                return null;
            }

            public void done() {
                if (success) {
                    extensionsPanel.setSelectedExtensionEnabled(true);
                    extensionsPanel.setRestartRequired(true);
                }
                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doDisableExtension() {
        final String workingId = startWorking("Disabling extension...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private boolean success = true;

            public Void doInBackground() {
                try {
                    mirthClient.setExtensionEnabled(extensionsPanel.getSelectedExtension().getName(), false);
                } catch (ClientException e) {
                    success = false;
                    SwingUtilities.invokeLater(() -> {
                        alertThrowable(PlatformUI.MIRTH_FRAME, e);
                    });
                }

                return null;
            }

            public void done() {
                if (success) {
                    extensionsPanel.setSelectedExtensionEnabled(false);
                    extensionsPanel.setRestartRequired(true);
                }
                stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doShowExtensionProperties() {
        extensionsPanel.showExtensionProperties();
    }

    public void doUninstallExtension() {
        final String workingId = startWorking("Uninstalling extension...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private boolean success = true;

            public Void doInBackground() {
                String packageName = extensionsPanel.getSelectedExtension().getPath();

                if (alertOkCancel(PlatformUI.MIRTH_FRAME, "Uninstalling this extension will remove all plugins and/or connectors\nin the following extension folder: " + packageName)) {
                    try {
                        mirthClient.uninstallExtension(packageName);
                    } catch (ClientException e) {
                        success = false;
                        SwingUtilities.invokeLater(() -> {
                            alertThrowable(PlatformUI.MIRTH_FRAME, e);
                        });
                    }
                }

                return null;
            }

            public void done() {
                if (success) {
                    extensionsPanel.setRestartRequired(true);
                }
                stopWorking(workingId);
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
        } catch (ClientException e) {
            if (e.getCause() != null && e.getCause() instanceof VersionMismatchException) {
                alertError(this, e.getCause().getMessage());
            } else {
                alertThrowable(this, e, "Unable to install extension: " + e.getMessage());
            }

            return false;
        }
        return true;
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
                if (!doExportChannel()) {
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

    public void setCanSave(boolean canSave) {
        this.canSave = canSave;
    }

    public void doContextSensitiveSave() {
        if (canSave) {
            if (currentContentPage == channelPanel) {
                channelPanel.doContextSensitiveSave();
            } else if (currentContentPage == channelEditPanel) {
                doSaveChannel();
            } else if (currentContentPage == channelEditPanel.filterPane) {
                doSaveChannel();
            } else if (currentContentPage == channelEditPanel.transformerPane) {
                doSaveChannel();
            } else if (currentContentPage == globalScriptsPanel) {
                doSaveGlobalScripts();
            } else if (currentContentPage == codeTemplatePanel) {
                codeTemplatePanel.doContextSensitiveSave();
            } else if (currentContentPage == settingsPane) {
                settingsPane.getCurrentSettingsPanel().doSave();
            } else if (currentContentPage == alertEditPanel) {
                doSaveAlerts();
            }
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
        BareBonesBrowserLaunch.openURL(UIConstants.HELP_LOCATION);
    }

    public void goToNotifications() {
        new NotificationDialog();
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

    public List<Integer> getSelectedMetaDataIdsFromDashboard(String channelId) {
        List<DashboardStatus> selectedStatuses = dashboardPanel.getSelectedStatuses();
        List<Integer> metaDataIds = new ArrayList<Integer>();

        if (selectedStatuses.size() == 0) {
            return metaDataIds;
        }

        for (DashboardStatus status : selectedStatuses) {
            if (status.getChannelId() == channelId) {
                Integer metaDataId = status.getMetaDataId();

                if (metaDataId != null) {
                    metaDataIds.add(metaDataId);
                }
            }
        }

        return metaDataIds;
    }

    public void retrieveUsers() throws ClientException {
        users = mirthClient.getAllUsers();
    }

    public synchronized void updateAcceleratorKeyPressed(InputEvent e) {
        this.acceleratorKeyPressed = (((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) > 0) || ((e.getModifiers() & InputEvent.CTRL_MASK) > 0) || ((e.getModifiers() & InputEvent.ALT_MASK) > 0));
    }

    public synchronized boolean isAcceleratorKeyPressed() {
        return acceleratorKeyPressed;
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

        for (ChannelStatus channelStatus : channelPanel.getCachedChannelStatuses().values()) {
            if (channelStatus.getChannel().getName().equalsIgnoreCase(name) && !channelStatus.getChannel().getId().equals(id)) {
                alertWarning(this, "Channel \"" + name + "\" already exists.");
                return false;
            }
        }
        return true;
    }

    public SettingsPanelTags getTagsPanel() {
        if (settingsPane != null) {
            return (SettingsPanelTags) settingsPane.getSettingsPanel(SettingsPanelTags.TAB_NAME);
        }
        return null;
    }

    public Set<ChannelTag> getCachedChannelTags() {
        SettingsPanelTags tagsPanel = getTagsPanel();
        if (tagsPanel != null) {
            return tagsPanel.getCachedChannelTags();
        }
        return new HashSet<ChannelTag>();
    }

    /**
     * Checks to see if the serialized object version is current, and prompts the user if it is not.
     */
    public boolean promptObjectMigration(String content, String objectName) {
        String version = null;

        try {
            version = MigrationUtil.normalizeVersion(MigrationUtil.getSerializedObjectVersion(content), 3);
        } catch (Exception e) {
            logger.error("Failed to read version information", e);
        }

        StringBuilder message = new StringBuilder();

        if (version == null) {
            message.append("The " + objectName + " being imported is from an older or unknown version of Mirth Connect.\n");
        } else {
            int comparison = MigrationUtil.compareVersions(version, PlatformUI.SERVER_VERSION);

            if (comparison == 0) {
                return true;
            }

            if (comparison > 0) {
                alertInformation(this, "The " + objectName + " being imported originated from Mirth version " + version + ".\nYou are using Mirth Connect version " + PlatformUI.SERVER_VERSION + ".\nThe " + objectName + " cannot be imported, because it originated from a newer version of Mirth Connect.");
                return false;
            }

            if (comparison < 0) {
                message.append("The " + objectName + " being imported originated from Mirth version " + version + ".\n");
            }
        }

        message.append("You are using Mirth Connect version " + PlatformUI.SERVER_VERSION + ".\nWould you like to automatically convert the " + objectName + " to the " + PlatformUI.SERVER_VERSION + " format?");
        return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, message.toString(), "Select an Option", JOptionPane.YES_NO_OPTION);
    }

    /**
     * Removes items from the list that are not of the expected class.
     */
    public void removeInvalidItems(List<?> list, Class<?> expectedClass) {
        if (list == null) {
            return;
        }

        int originalSize = list.size();

        for (int i = 0; i < list.size(); i++) {
            if (!expectedClass.isInstance(list.get(i))) {
                list.remove(i--);
            }
        }

        if (list.size() < originalSize) {
            if (list.size() == 0) {
                alertError(this, "The imported object(s) are not of the expected class: " + expectedClass.getSimpleName());
            } else {
                alertError(this, "One or more imported objects were skipped, because they are not of the expected class: " + expectedClass.getSimpleName());
            }
        }
    }

    public List<ResourceProperties> getResources() {
        if (settingsPane == null) {
            settingsPane = new SettingsPane();
        }
        SettingsPanelResources resourcesPanel = (SettingsPanelResources) settingsPane.getSettingsPanel(SettingsPanelResources.TAB_NAME);
        List<ResourceProperties> resourceProperties = resourcesPanel.getCachedResources();
        if (resourceProperties == null) {
            resourcesPanel.refresh();
            resourceProperties = resourcesPanel.getCachedResources();
        }
        return resourceProperties;
    }

    public void updateResourceNames(Channel channel) {
        updateResourceNames(channel, getResources());
    }

    public void updateResourceNames(Channel channel, List<ResourceProperties> resourceProperties) {
        if (!(channel instanceof InvalidChannel)) {
            updateResourceNames(channel.getProperties().getResourceIds(), resourceProperties);
            updateResourceNames(channel.getSourceConnector(), resourceProperties);
            for (Connector destinationConnector : channel.getDestinationConnectors()) {
                updateResourceNames(destinationConnector, resourceProperties);
            }
        }
    }

    public void updateResourceNames(Connector connector) {
        updateResourceNames(connector, getResources());
    }

    private void updateResourceNames(Connector connector, List<ResourceProperties> resourceProperties) {
        if (connector.getProperties() instanceof SourceConnectorPropertiesInterface) {
            updateResourceNames(((SourceConnectorPropertiesInterface) connector.getProperties()).getSourceConnectorProperties().getResourceIds(), resourceProperties);
        } else {
            updateResourceNames(((DestinationConnectorPropertiesInterface) connector.getProperties()).getDestinationConnectorProperties().getResourceIds(), resourceProperties);
        }
    }

    private void updateResourceNames(Map<String, String> resourceIds, List<ResourceProperties> resourceProperties) {
        if (resourceProperties != null) {
            Set<String> invalidIds = new HashSet<String>(resourceIds.keySet());

            // First update the names of all resources currently in the map
            for (ResourceProperties resource : resourceProperties) {
                if (resourceIds.containsKey(resource.getId())) {
                    resourceIds.put(resource.getId(), resource.getName());
                    // If the resource ID was found it's not invalid
                    invalidIds.remove(resource.getId());
                }
            }

            /*
             * Iterate through all resource IDs that weren't found in the current list of resources.
             * If there's a resource with a different ID but the same name as a particular entry,
             * then replace the entry with the correct ID/name.
             */
            for (String invalidId : invalidIds) {
                String resourceName = resourceIds.get(invalidId);
                if (StringUtils.isNotBlank(resourceName)) {
                    for (ResourceProperties resource : resourceProperties) {
                        // Replace if the names are equal and the resource ID isn't already contained in the map
                        if (resource.getName().equals(resourceName) && !resourceIds.containsKey(resource.getId())) {
                            resourceIds.put(resource.getId(), resourceName);
                            resourceIds.remove(invalidId);
                        }
                    }
                }
            }
        }
    }
}
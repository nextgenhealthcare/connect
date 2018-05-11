/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.manager;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.ArrayUtils;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.windows.WindowsLookAndFeelAddons;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.mirth.connect.manager.components.MirthComboBox;
import com.mirth.connect.manager.components.MirthFieldConstraints;
import com.mirth.connect.manager.components.MirthPasswordField;
import com.mirth.connect.manager.components.MirthTextField;

public class ManagerDialog extends JDialog {

    private ServiceController serviceController = null;
    private ManagerController managerController = null;
    private boolean loading = false;
    private String lastSelectedDatabaseType;
    private Map<String, String> databaseUrls = new HashMap<String, String>();

    private String heapSize = "512m";
    private ImageIcon icon = new ImageIcon(this.getClass().getResource("images/wrench.png"));

    public ManagerDialog() {
        try {
            PlasticLookAndFeel.setPlasticTheme(new MirthTheme());
            PlasticXPLookAndFeel look = new PlasticXPLookAndFeel();
            UIManager.setLookAndFeel(look);
            UIManager.put("win.xpstyle.name", "metallic");

            LookAndFeelAddons.setAddon(WindowsLookAndFeelAddons.class);

            getContentPane().setBackground(Color.WHITE);
            setTitle("Mirth Connect Server Manager");
            setResizable(false);

            serviceController = ServiceControllerFactory.getServiceController();
            managerController = ManagerController.getInstance();
            heapSize = (String) managerController.getServerProperties().getProperty(ManagerConstants.ADMINISTRATOR_MAX_HEAP_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void setupDialog() {
        initComponents();
        initLayout();

        heapSizeButton.setIcon(icon);

        databaseUrls.put("derby", "jdbc:derby:${dir.appdata}/mirthdb;create=true");
        databaseUrls.put("postgres", "jdbc:postgresql://localhost:5432/mirthdb");
        databaseUrls.put("mysql", "jdbc:mysql://localhost:3306/mirthdb");
        databaseUrls.put("oracle", "jdbc:oracle:thin:@localhost:1521:DB");
        databaseUrls.put("sqlserver", "jdbc:jtds:sqlserver://localhost:1433/mirthdb");

        // Remove the service tab if it's not supported
        if (!serviceController.isShowServiceTab()) {
            tabPanel.removeTabAt(0);
        }

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                close();
            }
        });

        serverMemoryField.setDocument(new MirthFieldConstraints(0, false, false, true));

        loadProperties();
    }

    public void open() {
        managerController.updateMirthServiceStatus();
        loadProperties();
        if (serviceController.isStartupPossible()) {
            startup.setEnabled(true);

            if (serviceController.isStartup()) {
                startup.setSelected(true);
            } else {
                startup.setSelected(false);
            }
        } else {
            startup.setEnabled(false);
        }
        setVisible(true);
    }

    public void close() {
        setVisible(false);

        // if there is no tray icon, shutdown and exit the manager
        if (!serviceController.isShowTrayIcon()) {
            Manager.shutdown();
        }
    }

    public void setApplyEnabled(boolean enabled) {
        applyButton.setEnabled(enabled);
    }

    public boolean isApplyEnabled() {
        return applyButton.isEnabled();
    }

    private void initServicePanel() {
        servicePanel = new JPanel(new MigLayout("insets 8, novisualpadding, hidemode 3", "24[][][]", "[]12[]12[]12[]12[]"));
        servicePanel.setBackground(new Color(255, 255, 255));
        servicePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        servicePanel.setFocusable(false);

        startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });
        startLabel = new JLabel("Starts the Mirth Connect service");

        restartButton = new JButton("Restart");
        restartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                restartButtonActionPerformed(evt);
            }
        });
        restartLabel = new JLabel("Restarts the Mirth Connect service");

        stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });
        stopLabel = new JLabel("Stops the Mirth Connect service");

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        refreshLabel = new JLabel("Refreshes the Mirth Connect service status");

        startup = new JCheckBox("Start Mirth Connect Server Manager on system startup");
        startup.setFocusable(false);
        startup.setToolTipText("Starts this application when logging into the operating system. Currently only enabled for Windows.");
        startup.setBackground(new Color(255, 255, 255));
        startup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                startupActionPerformed(evt);
            }
        });
    }

    private void initServerPanel() {
        serverPanel = new JPanel(new MigLayout("insets 8, novisualpadding, hidemode 3, fill", "[right][left][right][left]"));
        serverPanel.setFocusable(false);
        serverPanel.setBackground(new Color(255, 255, 255));
        serverPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        httpPortLabel = new JLabel("HTTP Port:");
        httpPortField = new MirthTextField();

        httpsPortLabel = new JLabel("HTTPS Port:");
        httpsPortField = new MirthTextField();

        serverMemoryLabel = new JLabel("Server Memory (mb):");
        serverMemoryField = new MirthTextField();

        serverLogFiles = new JList();
        serverLogFiles.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                serverLogFilesValueChanged(evt);
            }
        });

        serverLogFiles.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {}

            public void mouseReleased(MouseEvent evt) {}

            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() >= 2) {
                    if (serverLogFiles.getSelectedIndex() != -1) {
                        viewFileButtonActionPerformed(null);
                    }
                }
            }
        });

        serverLogsScrollPane = new JScrollPane();
        serverLogsScrollPane.setViewportView(serverLogFiles);

        mainLogLevelLabel = new JLabel("Main Log Level:");
        mainLogLevelCombobox = new MirthComboBox();
        mainLogLevelCombobox.setModel(new DefaultComboBoxModel(ManagerConstants.LOG4J_ERROR_CODES)); // This can be combined with the above as a new ctor

        databaseLogLevelLabel = new JLabel("Database Log Level:");
        databaseLogLevelCombobox = new MirthComboBox();
        databaseLogLevelCombobox.setModel(new DefaultComboBoxModel(ManagerConstants.LOG4J_ERROR_CODES));

        channelLogLevelLabel = new JLabel("Channel Log Level:");
        channelLogLevelCombobox = new MirthComboBox();
        channelLogLevelCombobox.setModel(new DefaultComboBoxModel(ManagerConstants.LOG4J_ERROR_CODES_WITH_BLANK));

        refreshServiceButton = new JButton("Refresh");
        refreshServiceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                refreshServiceButtonActionPerformed(evt);
            }
        });

        viewFileButton = new JButton("View File");
        viewFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                viewFileButtonActionPerformed(evt);
            }
        });
    }

    private void initDatabasePanel() {
        databasePanel = new JPanel(new MigLayout("insets 8, novisualpadding, hidemode 3", "[right][left]"));
        databasePanel.setFocusable(false);
        databasePanel.setBackground(new Color(255, 255, 255));
        databasePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        databaseTypeLabel = new JLabel("Type:");
        databaseTypeCombobox = new MirthComboBox();
        databaseTypeCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "derby",
                "postgres", "mysql", "oracle", "sqlserver" }));
        databaseTypeCombobox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                databaseTypeActionPerformed(evt);
            }
        });

        databaseUrlLabel = new JLabel("URL:");
        databaseUrlField = new MirthTextField();

        databaseUsernameLabel = new JLabel("Username:");
        databaseUsernameField = new MirthTextField();

        passwordLabel = new JLabel("Password:");
        databasePasswordField = new MirthPasswordField();
    }

    private void initInfoPanel() {
        infoPanel = new JPanel(new MigLayout("insets 8, novisualpadding, hidemode 3", "[right][left]"));
        infoPanel.setBackground(new Color(255, 255, 255));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        serverVersionLabel = new JLabel("Server Version:");
        serverVersionField = new JLabel("version");

        serverIdLabel = new JLabel("Server ID:");
        serverIdField = new JLabel("serverId");

        javaVersionLabel = new JLabel("Java Version:");
        javaVersionField = new JLabel("javaVersion");

        contactPrefixLabel = new JLabel("Need Help?  Contact");
        mirthSupportLink = new JLabel("<html><font color=blue><u>Mirth Corporation</u></font></html>");
        mirthSupportLink.setToolTipText("Visit Mirth Corporation's website.");
        mirthSupportLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mirthSupportLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                mirthSupportLinkMouseClicked(evt);
            }
        });

        contactPostfixLabel = new JLabel("for professional support.");
    }

    private void initComponents() {
        initServicePanel();
        initServerPanel();
        initDatabasePanel();
        initInfoPanel();

        tabPanel = new JTabbedPane();
        tabPanel.setBackground(new Color(255, 255, 255));
        tabPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tabPanel.setFocusable(false);
        tabPanel.addTab("Service", servicePanel);
        tabPanel.addTab("Server", serverPanel);
        tabPanel.addTab("Database", databasePanel);
        tabPanel.addTab("Info", infoPanel);

        launchButton = new JButton("Administrator");
        launchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                launchButtonActionPerformed(evt);
            }
        });

        heapSizeButton = new JButton();
        heapSizeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                heapSizeButtonActionPerformed(evt);
            }
        });

        applyButton = new JButton("Apply");
        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });

        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0 0 8 0, novisualpadding, hidemode 3"));

        add(new MirthHeadingPanel(), "wrap, grow");

        servicePanel.add(startButton, "w 80!, h 22!, split");
        servicePanel.add(startLabel, "wrap");
        servicePanel.add(restartButton, "w 80!, h 22!, split");
        servicePanel.add(restartLabel, "wrap");
        servicePanel.add(stopButton, "w 80!, h 22!, split");
        servicePanel.add(stopLabel, "wrap");
        servicePanel.add(refreshServiceButton, "w 80!, h 22!, split");
        servicePanel.add(refreshLabel, "wrap");
        servicePanel.add(startup, "span, push");

        serverPanel.add(httpPortLabel);
        serverPanel.add(httpPortField, "w 55!, h 22!");
        serverPanel.add(mainLogLevelLabel);
        serverPanel.add(mainLogLevelCombobox, "w 80!, wrap");

        serverPanel.add(httpsPortLabel);
        serverPanel.add(httpsPortField, "w 55!, h 22!");
        serverPanel.add(databaseLogLevelLabel);
        serverPanel.add(databaseLogLevelCombobox, "w 80!, wrap");

        serverPanel.add(serverMemoryLabel);
        serverPanel.add(serverMemoryField, "w 55!, h 22!");
        serverPanel.add(channelLogLevelLabel);
        serverPanel.add(channelLogLevelCombobox, "w 80!, wrap");

        serverPanel.add(new JLabel("Log Files:"));
        serverPanel.add(serverLogsScrollPane, "w 205!, h 70!, span 2 2");
        serverPanel.add(refreshButton, "w 80!, h 22!, cell 3 3");
        serverPanel.add(viewFileButton, "w 80!, h 22!, cell 3 4");

        databasePanel.add(databaseTypeLabel);
        databasePanel.add(databaseTypeCombobox, "wrap");
        databasePanel.add(databaseUrlLabel);
        databasePanel.add(databaseUrlField, "wrap, w 350!");
        databasePanel.add(databaseUsernameLabel);
        databasePanel.add(databaseUsernameField, "wrap, w 145!");
        databasePanel.add(passwordLabel);
        databasePanel.add(databasePasswordField, "w 145!");

        infoPanel.add(serverVersionLabel);
        infoPanel.add(serverVersionField, "wrap");
        infoPanel.add(serverIdLabel);
        infoPanel.add(serverIdField, "wrap");
        infoPanel.add(javaVersionLabel);
        infoPanel.add(javaVersionField, "wrap");
        infoPanel.add(contactPrefixLabel, "split 3, span, gaptop 90");
        infoPanel.add(mirthSupportLink, "gaptop 90");
        infoPanel.add(contactPostfixLabel, "gaptop 90");

        JPanel tabContainer = new JPanel(new MigLayout("insets 8 8 0 8, novisualpadding, hidemode 3"));
        tabContainer.setBackground(Color.white);
        tabContainer.add(tabPanel);

        add(tabContainer, "wrap");
        add(new JSeparator(), "newline, growx, sx, gapleft 8, gapright 8");
        add(launchButton, "split, gapleft 8");
        add(heapSizeButton, "w 22!, h 22!, left");
        add(okButton, "w 56!, gapleft 145");
        add(cancelButton, "w 56!");
        add(applyButton, "w 56!");
    }

    private void mirthSupportLinkMouseClicked(MouseEvent evt) {
        BareBonesBrowserLaunch.openURL("http://www.mirthcorp.com/services/support");
    }

    private void startupActionPerformed(ActionEvent evt) {
        setApplyEnabled(true);
    }

    private void refreshServiceButtonActionPerformed(ActionEvent evt) {
        managerController.updateMirthServiceStatus();
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        if (saveProperties()) {
            close();
        }
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        close();
    }

    private void applyButtonActionPerformed(ActionEvent evt) {
        if (startup.isSelected()) {
            serviceController.setStartup(true);
        } else {
            serviceController.setStartup(false);
        }
        saveProperties();
    }

    private void serverLogFilesValueChanged(ListSelectionEvent evt) {
        if (serverLogFiles.getSelectedIndex() != -1) {
            viewFileButton.setEnabled(true);
        } else {
            viewFileButton.setEnabled(false);
        }
    }

    private void refreshButtonActionPerformed(ActionEvent evt) {
        refreshLogs();
    }

    private void databaseTypeActionPerformed(ActionEvent evt) {
        // If the properties are loading, don't do anything here.  If the database 
        // value is changing, then set the default values.
        if (loading) {
            lastSelectedDatabaseType = (String) databaseTypeCombobox.getSelectedItem();
        } else if (!databaseTypeCombobox.getSelectedItem().equals(lastSelectedDatabaseType)) {

            // If the last selection was not using the default values, then prompt
            // to see if the user wants to continue.
            if (!databaseUrlField.getText().equals(databaseUrls.get(lastSelectedDatabaseType)) || !databaseUsernameField.getText().equals("") || !new String(databasePasswordField.getPassword()).equals("")) {
                if (!managerController.alertOptionDialog(this, "Changing your database type will clear your database URL, username, and password.\nAre you sure you want to continue?")) {
                    databaseTypeCombobox.setSelectedItem(lastSelectedDatabaseType);
                    return;
                }
            }
            lastSelectedDatabaseType = (String) databaseTypeCombobox.getSelectedItem();

            databaseUrlField.setText(databaseUrls.get(lastSelectedDatabaseType));
            databaseUsernameField.setText("");
            databasePasswordField.setText("");

        }
    }

    private void launchButtonActionPerformed(ActionEvent evt) {
        managerController.launchAdministrator(heapSize);
    }

    private void viewFileButtonActionPerformed(ActionEvent evt) {
        managerController.openLogFile(managerController.getLog4jProperties().getString(ManagerConstants.DIR_LOGS) + System.getProperty("file.separator") + (String) serverLogFiles.getSelectedValue());
    }

    private void restartButtonActionPerformed(ActionEvent evt) {
        managerController.restartMirthWorker();
    }

    private void stopButtonActionPerformed(ActionEvent evt) {
        managerController.stopMirthWorker();
    }

    private void startButtonActionPerformed(ActionEvent evt) {
        managerController.startMirthWorker();
    }

    private void heapSizeButtonActionPerformed(ActionEvent evt) {
        HeapSizeDialog dialog = new HeapSizeDialog(heapSize);
        heapSize = dialog.getHeapSize();
    }

    private void loadProperties() {
        loading = true;

        serverIdField.setText(managerController.getServerId());
        serverMemoryField.setText(managerController.getServiceXmx());
        serverVersionField.setText(managerController.getServerVersion());
        javaVersionField.setText(System.getProperty("java.version"));
        httpPortField.setText(managerController.getServerProperties().getString(ManagerConstants.SERVER_HTTP_PORT));
        httpsPortField.setText(managerController.getServerProperties().getString(ManagerConstants.SERVER_HTTPS_PORT));

        boolean applyEnabled = isApplyEnabled();

        databaseTypeCombobox.setSelectedItem(managerController.getServerProperties().getString(ManagerConstants.DATABASE_TYPE));
        databaseUrlField.setText((String) managerController.getServerProperties().getProperty(ManagerConstants.DATABASE_URL));
        databaseUsernameField.setText(managerController.getServerProperties().getString(ManagerConstants.DATABASE_USERNAME));
        databasePasswordField.setText(managerController.getServerProperties().getString(ManagerConstants.DATABASE_PASSWORD));

        String rootLogCode = managerController.getLog4jProperties().getStringArray(ManagerConstants.LOG4J_MIRTH_LOG_LEVEL)[0];
        if (ArrayUtils.contains(ManagerConstants.LOG4J_ERROR_CODES, rootLogCode)) {
            mainLogLevelCombobox.setSelectedItem(rootLogCode);
        }

        databaseLogLevelCombobox.setSelectedItem(managerController.getLog4jProperties().getString(ManagerConstants.LOG4J_DATABASE_LOG_LEVEL));

        String channelLogCode = null;
        for (int i = 0; (i < ManagerConstants.LOG4J_CHANNEL_LOG_LEVELS.length) && !ManagerConstants.LOG4J_ERROR_CODES_WITH_BLANK[0].equals(channelLogCode); i++) {
            String tempLogCode = managerController.getLog4jProperties().getString(ManagerConstants.LOG4J_CHANNEL_LOG_LEVELS[i]);

            if (tempLogCode == null) { // log code not found, stop
                channelLogCode = ManagerConstants.LOG4J_ERROR_CODES_WITH_BLANK[0];
            } else if (channelLogCode == null) { // first code found, save and continue
                channelLogCode = tempLogCode;
            } else if (!channelLogCode.equalsIgnoreCase(tempLogCode)) { // different code, stop
                channelLogCode = ManagerConstants.LOG4J_ERROR_CODES_WITH_BLANK[0];
            }
        }

        channelLogLevelCombobox.setSelectedItem(channelLogCode);

        setApplyEnabled(applyEnabled);

        refreshLogs();
        serverLogFilesValueChanged(null);

        loading = false;
    }

    public boolean saveProperties() {
        if (managerController.getServerProperties().getReloadingStrategy().reloadingRequired()) {
            if (!managerController.alertOptionDialog(this, "Server properties have changed on disk since the manager was opened. Are you sure you wish to overwrite them?")) {
                return false;
            }
            managerController.getServerProperties().reload();
        }

        if (managerController.getLog4jProperties().getReloadingStrategy().reloadingRequired()) {
            if (!managerController.alertOptionDialog(this, "Log4j properties have changed on disk since the manager was opened. Are you sure you wish to overwrite them?")) {
                return false;
            }
            managerController.getLog4jProperties().reload();
        }

        managerController.getServerProperties().setProperty(ManagerConstants.SERVER_HTTP_PORT, httpPortField.getText());
        managerController.getServerProperties().setProperty(ManagerConstants.SERVER_HTTPS_PORT, httpsPortField.getText());

        managerController.getServerProperties().setProperty(ManagerConstants.DATABASE_TYPE, ((String) databaseTypeCombobox.getSelectedItem()));
        managerController.getServerProperties().setProperty(ManagerConstants.DATABASE_URL, databaseUrlField.getText());
        managerController.getServerProperties().setProperty(ManagerConstants.DATABASE_USERNAME, databaseUsernameField.getText());
        managerController.getServerProperties().setProperty(ManagerConstants.DATABASE_PASSWORD, new String(databasePasswordField.getPassword()));

        try {
            managerController.getServerProperties().save();
        } catch (ConfigurationException e) {
            managerController.alertErrorDialog(this, "Error saving " + managerController.getServerProperties().getFile().getPath() + ":\n" + e.getMessage());
        }

        String[] logLevel = managerController.getLog4jProperties().getStringArray(ManagerConstants.LOG4J_MIRTH_LOG_LEVEL);
        logLevel[0] = (String) mainLogLevelCombobox.getSelectedItem();
        managerController.getLog4jProperties().setProperty(ManagerConstants.LOG4J_MIRTH_LOG_LEVEL, logLevel);

        managerController.getLog4jProperties().setProperty(ManagerConstants.LOG4J_DATABASE_LOG_LEVEL, (String) databaseLogLevelCombobox.getSelectedItem());

        String channelLogLevelCode = (String) channelLogLevelCombobox.getSelectedItem();

        // Only set all of the log levels if the selected value is not blank
        if (!ManagerConstants.LOG4J_ERROR_CODES_WITH_BLANK[0].equals(channelLogLevelCode)) {
            for (String channelLogLevel : ManagerConstants.LOG4J_CHANNEL_LOG_LEVELS) {
                managerController.getLog4jProperties().setProperty(channelLogLevel, channelLogLevelCode);
            }
        }

        try {
            managerController.getLog4jProperties().save();
        } catch (ConfigurationException e) {
            managerController.alertErrorDialog(this, "Error saving " + managerController.getLog4jProperties().getFile().getPath() + ":\n" + e.getMessage());
        }

        managerController.setServiceXmx(serverMemoryField.getText());

        setApplyEnabled(false);
        return true;
    }

    private void refreshLogs() {
        String logPath = managerController.getLog4jProperties().getString(ManagerConstants.DIR_LOGS);
        serverLogFiles.setListData(managerController.getLogFiles(logPath).toArray());
    }

    public void setStartButtonActive(boolean active) {
        startButton.setEnabled(active);
        startLabel.setEnabled(active);
    }

    public void setStopButtonActive(boolean active) {
        stopButton.setEnabled(active);
        stopLabel.setEnabled(active);
    }

    public void setRestartButtonActive(boolean active) {
        restartButton.setEnabled(active);
        restartLabel.setEnabled(active);
    }

    public void setLaunchButtonActive(boolean active) {
        launchButton.setEnabled(active);
    }

    // Service Panel
    private JPanel servicePanel;
    private JButton startButton;
    private JLabel startLabel;
    private JButton restartButton;
    private JLabel restartLabel;
    private JButton stopButton;
    private JLabel stopLabel;
    private JLabel refreshLabel;
    private JButton refreshServiceButton;
    private JCheckBox startup;

    // Server Panel, a lot of the labels can probably just be added to the layout...
    private JPanel serverPanel;
    private JLabel httpPortLabel;
    private MirthTextField httpPortField;
    private JLabel httpsPortLabel;
    private MirthTextField httpsPortField;
    private JLabel serverMemoryLabel;
    private MirthTextField serverMemoryField;
    private JList serverLogFiles;
    private JScrollPane serverLogsScrollPane;
    private JLabel mainLogLevelLabel;
    private MirthComboBox mainLogLevelCombobox;
    private JLabel databaseLogLevelLabel;
    private MirthComboBox databaseLogLevelCombobox;
    private JLabel channelLogLevelLabel;
    private MirthComboBox channelLogLevelCombobox;
    private JButton refreshButton;
    private JButton viewFileButton;

    // Database Panel
    private JPanel databasePanel;
    private JLabel databaseTypeLabel;
    private MirthComboBox databaseTypeCombobox;
    private JLabel databaseUrlLabel;
    private MirthTextField databaseUrlField;
    private JLabel databaseUsernameLabel;
    private MirthTextField databaseUsernameField;
    private JLabel passwordLabel;
    private MirthPasswordField databasePasswordField;

    // Info Panel
    private JPanel infoPanel;
    private JLabel serverVersionLabel;
    private JLabel serverVersionField;
    private JLabel serverIdLabel;
    private JLabel serverIdField;
    private JLabel javaVersionLabel;
    private JLabel javaVersionField;
    private JLabel contactPrefixLabel;
    private JLabel mirthSupportLink;
    private JLabel contactPostfixLabel;

    // Bottom Panel
    private JButton launchButton;
    private JButton okButton;
    private JButton cancelButton;
    private JButton applyButton;
    private JButton heapSizeButton;
    private JTabbedPane tabPanel;
}

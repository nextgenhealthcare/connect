/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.mirth.connect.client.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.editors.filter.FilterPane;
import com.mirth.connect.client.ui.editors.transformer.TransformerPane;
import com.mirth.connect.client.ui.util.PropertiesUtil;
import com.mirth.connect.client.ui.util.VariableListUtil;
import com.mirth.connect.connectors.ConnectorClass;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.CodeTemplate.ContextType;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.MessageObject.Protocol;
import com.mirth.connect.model.Rule;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.model.converters.DefaultSerializerPropertiesFactory;
import com.mirth.connect.model.converters.ObjectCloner;
import com.mirth.connect.model.converters.ObjectClonerException;
import com.mirth.connect.util.PropertyVerifier;

/** The channel editor panel. Majority of the client application */
public class ChannelSetup extends javax.swing.JPanel {

    private static final String DESTINATION_DEFAULT = "File Writer";
    private static final String SOURCE_DEFAULT = "LLP Listener";
    private static final String DATABASE_READER = "Database Reader";
    private static final String HTTP_LISTENER = "HTTP Listener";
    private static final String HTTP_BODY_ONLY = "receiverBodyOnly";
    private final String STATUS_COLUMN_NAME = "Status";
    private final String DESTINATION_COLUMN_NAME = "Destination";
    private final String CONNECTOR_TYPE_COLUMN_NAME = "Connector Type";
    private final int SOURCE_TAB_INDEX = 1;
    private final int DESTINATIONS_TAB_INDEX = 2;
    private final String DATA_TYPE_KEY = "DataType";
    public Channel currentChannel;
    public int lastModelIndex = -1;
    public TransformerPane transformerPane = new TransformerPane();
    public FilterPane filterPane = new FilterPane();
    private Frame parent;
    private boolean isDeleting = false;
    private boolean loadingChannel = false;
    private boolean channelValidationFailed = false;
    public Map<String, ConnectorMetaData> transports;
    private ArrayList<String> sourceConnectors;
    private ArrayList<String> destinationConnectors;

    /**
     * Creates the Channel Editor panel. Calls initComponents() and sets up the
     * model, dropdowns, and mouse listeners.
     */
    public ChannelSetup() {
        this.parent = PlatformUI.MIRTH_FRAME;

        initComponents();

        numDays.setDocument(new MirthFieldConstraints(3, false, false, true));
        summaryNameField.setDocument(new MirthFieldConstraints(40, false, true, true));

        channelView.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                showChannelEditPopupMenu(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                showChannelEditPopupMenu(evt);
            }
        });


        transports = this.parent.getConnectorMetaData();
        sourceConnectors = new ArrayList<String>();
        destinationConnectors = new ArrayList<String>();
        Iterator<Entry<String, ConnectorMetaData>> i = transports.entrySet().iterator();
        while (i.hasNext()) {
            Entry<String, ConnectorMetaData> entry = i.next();
            try {

                ConnectorMetaData metaData = transports.get(entry.getKey());

                if (metaData.getType() == ConnectorMetaData.Type.SOURCE && metaData.isEnabled()) {
                    sourceConnectors.add(metaData.getName());

                    try {
                        parent.sourceConnectors.add((ConnectorClass) Class.forName(metaData.getClientClassName()).newInstance());
                    } catch (Exception e) {
                        parent.alertError(this.parent, "Could not load class: " + metaData.getClientClassName());
                    }
                }
                if (metaData.getType() == ConnectorMetaData.Type.DESTINATION && metaData.isEnabled()) {
                    try {
                        if (!(metaData.getName().equals("IHE Sender") && !parent.mirthClient.isExtensionEnabled("IHE Configuration"))) {
                            destinationConnectors.add(metaData.getName());
                            parent.destinationConnectors.add((ConnectorClass) Class.forName(metaData.getClientClassName()).newInstance());
                        } else {
                            parent.alertError(this.parent, "Cannot load IHE Sender connector because required IHE Configuration plugin is not installed or enabled");
                        }
                    } catch (Exception e) {
                        parent.alertError(this.parent, "Could not load class: " + metaData.getClientClassName());
                    }
                }
            } catch (ClassCastException castException) {
                System.out.println("Unable to load plugin");
            }
        }

        Collections.sort(sourceConnectors);
        Collections.sort(destinationConnectors);

        channelView.setMaximumSize(new Dimension(450, 3000));
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been
     * pushed.
     */
    private void checkSelectionAndPopupMenu(java.awt.event.MouseEvent evt) {
        int row = destinationTable.rowAtPoint(new Point(evt.getX(), evt.getY()));

        if (evt.isPopupTrigger()) {
            if (row != -1) {
                destinationTable.setRowSelectionInterval(row, row);
            }
            showChannelEditPopupMenu(evt);
        }
    }

    /**
     * Shows the trigger-button popup menu.
     */
    private void showChannelEditPopupMenu(java.awt.event.MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            parent.channelEditPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    /**
     * Is called to load the transformer pane on either the source or
     * destination
     */
    public String editTransformer() {
        String name = "";
        boolean changed = parent.changesHaveBeenMade();
        boolean transformerPaneLoaded = false;
        if (channelView.getSelectedIndex() == SOURCE_TAB_INDEX) {
            name = "Source";
            transformerPaneLoaded = transformerPane.load(currentChannel.getSourceConnector(), currentChannel.getSourceConnector().getTransformer(), changed);
        } else if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX) {
            Connector destination = currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex());
            transformerPaneLoaded = transformerPane.load(destination, destination.getTransformer(), changed);
            name = destination.getName();
        }
        if (!transformerPaneLoaded) {
            parent.taskPaneContainer.add(parent.getOtherPane());
            parent.setCurrentContentPage(parent.channelEditPanel);
            parent.setFocus(parent.channelEditTasks);
            name = "Edit Channel - " + parent.channelEditPanel.currentChannel.getName();
            parent.channelEditPanel.updateComponentShown();
        }
        return name;
    }

    /** Is called to load the filter pane on either the source or destination */
    public String editFilter() {
        String name = "";
        boolean changed = parent.changesHaveBeenMade();
        boolean filterPaneLoaded = false;

        if (channelView.getSelectedIndex() == SOURCE_TAB_INDEX) {
            name = "Source";
            filterPaneLoaded = filterPane.load(currentChannel.getSourceConnector(), currentChannel.getSourceConnector().getFilter(), currentChannel.getSourceConnector().getTransformer(), changed);
        } else if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX) {
            Connector destination = currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex());
            filterPaneLoaded = filterPane.load(destination, destination.getFilter(), destination.getTransformer(), changed);
            name = destination.getName();
        }

        if (!filterPaneLoaded) {
            parent.taskPaneContainer.add(parent.getOtherPane());
            parent.setCurrentContentPage(parent.channelEditPanel);
            parent.setFocus(parent.channelEditTasks);
            name = "Edit Channel - " + parent.channelEditPanel.currentChannel.getName();
            parent.channelEditPanel.updateComponentShown();
        }
        return name;
    }

    /**
     * Makes the destination table with a parameter that is true if a new
     * destination should be added as well.
     */
    public void makeDestinationTable(boolean addNew) {
        List<Connector> destinationConnectors;
        Object[][] tableData;
        int tableSize;

        destinationConnectors = currentChannel.getDestinationConnectors();
        tableSize = destinationConnectors.size();
        if (addNew) {
            tableSize++;
        }
        tableData = new Object[tableSize][3];
        for (int i = 0; i < tableSize; i++) {
            if (tableSize - 1 == i && addNew) {
                Connector connector = makeNewConnector(true);

                // Set the default inbound and outbound protocol and properties
                Protocol protocol = currentChannel.getSourceConnector().getTransformer().getOutboundProtocol();
                Properties defaultProperties = PropertiesUtil.convertMapToProperties(DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(protocol));

                connector.getTransformer().setInboundProtocol(protocol);
                connector.getTransformer().setInboundProperties(defaultProperties);
                connector.getTransformer().setOutboundProtocol(protocol);
                connector.getTransformer().setOutboundProperties(defaultProperties);

                connector.setName(getNewDestinationName(tableSize));
                connector.setTransportName(DESTINATION_DEFAULT);

                if (connector.isEnabled()) {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_blue.png")), UIConstants.ENABLED_STATUS);
                } else {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), UIConstants.DISABLED_STATUS);
                }
                tableData[i][1] = connector.getName();
                tableData[i][2] = connector.getTransportName();

                destinationConnectors.add(connector);
            } else {

                if (destinationConnectors.get(i).isEnabled()) {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_blue.png")), UIConstants.ENABLED_STATUS);
                } else {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), UIConstants.DISABLED_STATUS);
                }
                tableData[i][1] = destinationConnectors.get(i).getName();
                tableData[i][2] = destinationConnectors.get(i).getTransportName();
            }
        }

        destinationTable = new MirthTable();

        destinationTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[]{STATUS_COLUMN_NAME, DESTINATION_COLUMN_NAME, CONNECTOR_TYPE_COLUMN_NAME}) {

            boolean[] canEdit = new boolean[]{false, true, false};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        destinationTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // Set the custom cell editor for the Destination Name column.
        destinationTable.getColumnModel().getColumn(destinationTable.getColumnModel().getColumnIndex(DESTINATION_COLUMN_NAME)).setCellEditor(new DestinationTableCellEditor());

        // Must set the maximum width on columns that should be packed.
        destinationTable.getColumnExt(STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        destinationTable.getColumnExt(STATUS_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);

        // Set the cell renderer for the status column.
        destinationTable.getColumnExt(STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());

        destinationTable.setSelectionMode(0);
        destinationTable.setRowSelectionAllowed(true);
        destinationTable.setRowHeight(UIConstants.ROW_HEIGHT);
        destinationTable.setFocusable(true);
        destinationTable.setSortable(false);
        destinationTable.getTableHeader().setReorderingAllowed(false);

        destinationTable.setOpaque(true);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            destinationTable.setHighlighters(highlighter);
        }

        // This action is called when a new selection is made on the destination
        // table.
        destinationTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (!evt.getValueIsAdjusting()) {
                    if (lastModelIndex != -1 && lastModelIndex != destinationTable.getRowCount() && !isDeleting) {
                        Connector destinationConnector = currentChannel.getDestinationConnectors().get(lastModelIndex);
                        destinationConnector.setProperties(destinationConnectorClass.getProperties());
                    }

                    if (!loadConnector()) {
                        if (lastModelIndex == destinationTable.getRowCount()) {
                            destinationTable.setRowSelectionInterval(lastModelIndex - 1, lastModelIndex - 1);
                        } else {
                            destinationTable.setRowSelectionInterval(lastModelIndex, lastModelIndex);
                        }
                    } else {
                        lastModelIndex = destinationTable.getSelectedModelIndex();
                    }

                    checkVisibleDestinationTasks();
                }
            }
        });
        destinationTable.requestFocus();
        // Mouse listener for trigger-button popup on the table.
        destinationTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }
        });

        // Checks to see what to set the new row selection to based on
        // last index and if a new destination was added.
        int last = lastModelIndex;
        if (addNew) {
            destinationTable.setRowSelectionInterval(destinationTable.getRowCount() - 1, destinationTable.getRowCount() - 1);
        } else if (last == -1) {
            destinationTable.setRowSelectionInterval(0, 0); // Makes sure the
        } // event is called
        // when the table is
        // created.
        else if (last == destinationTable.getRowCount()) {
            destinationTable.setRowSelectionInterval(last - 1, last - 1);
        } else {
            destinationTable.setRowSelectionInterval(last, last);
        }

        destinationTablePane.setViewportView(destinationTable);
        destinationTablePane.setWheelScrollingEnabled(true);

        // Key Listener trigger for CTRL-S and DEL
        destinationTable.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                boolean isAccelerated = (e.getModifiers() & java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) > 0;
                if ((e.getKeyCode() == KeyEvent.VK_S) && isAccelerated) {
                    PlatformUI.MIRTH_FRAME.doContextSensitiveSave();
                } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    parent.doDeleteDestination();
                }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });
        destinationTable.addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
                destinationTablePane.getMouseWheelListeners()[0].mouseWheelMoved(e);
            }
        });
    }

    /**
     * Get the name that should be used for a new destination so that it is
     * unique.
     */
    private String getNewDestinationName(int size) {
        String temp = "Destination ";

        for (int i = 1; i <= size; i++) {
            boolean exists = false;
            for (int j = 0; j < size - 1; j++) {
                if (((String) destinationTable.getModel().getValueAt(j, destinationTable.getColumnModelIndex(DESTINATION_COLUMN_NAME))).equalsIgnoreCase(temp + i)) {
                    exists = true;
                }
            }
            if (!exists) {
                return temp + i;
            }
        }
        return "";
    }

    /** Loads a selected connector and returns true on success. */
    public boolean loadConnector() {
        List<Connector> destinationConnectors;
        String destinationName;

        if (destinationTable.getSelectedModelIndex() != -1) {
            destinationName = (String) destinationTable.getModel().getValueAt(destinationTable.getSelectedModelIndex(), destinationTable.getColumnModelIndex(DESTINATION_COLUMN_NAME));
        } else {
            return false;
        }

        if (currentChannel != null && currentChannel.getDestinationConnectors() != null) {
            destinationConnectors = currentChannel.getDestinationConnectors();
            for (int i = 0; i < destinationConnectors.size(); i++) {
                if (destinationConnectors.get(i).getName().equalsIgnoreCase(destinationName)) {
                    boolean enabled = parent.isSaveEnabled();
                    destinationSourceDropdown.setSelectedItem(destinationConnectors.get(i).getTransportName());
                    parent.setSaveEnabled(enabled);

                    return true;
                }
            }
        }
        return false;
    }

    /** Sets the overall panel to edit the channel with the given channel index. */
    public void editChannel(Channel channel) {
        loadingChannel = true;
        channelValidationFailed = false;
        lastModelIndex = -1;
        currentChannel = channel;

        PropertyVerifier.checkConnectorProperties(currentChannel, transports);

        sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(sourceConnectors.toArray()));
        destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(destinationConnectors.toArray()));

        loadChannelInfo();
        makeDestinationTable(false);
        setDestinationVariableList();
        loadingChannel = false;
        channelView.setSelectedIndex(0);
    }

    /**
     * Adds a new channel that is passed in and then sets the overall panel to
     * edit that channel.
     */
    public void addChannel(Channel channel) {
        loadingChannel = true;
        lastModelIndex = -1;
        currentChannel = channel;

        sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(sourceConnectors.toArray()));
        destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(destinationConnectors.toArray()));

        Connector sourceConnector = makeNewConnector(false);
        sourceConnector.setName("sourceConnector");
        sourceConnector.setTransportName(SOURCE_DEFAULT);
        Transformer sourceTransformer = new Transformer();

        // Set the default inbound and outbound protocol and properties
        Protocol defaultProtocol = Protocol.HL7V2;
        Properties defaultProperties = PropertiesUtil.convertMapToProperties(DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(defaultProtocol));

        sourceTransformer.setInboundProtocol(defaultProtocol);
        sourceTransformer.setInboundProperties(defaultProperties);
        sourceTransformer.setOutboundProtocol(defaultProtocol);
        sourceTransformer.setOutboundProperties(defaultProperties);

        sourceConnector.setTransformer(sourceTransformer);

        currentChannel.setSourceConnector(sourceConnector);
        setLastModified();
        loadChannelInfo();
        makeDestinationTable(true);
        setDestinationVariableList();
        loadingChannel = false;
        channelView.setSelectedIndex(0);
        summaryNameField.requestFocus();
        parent.setSaveEnabled(true);
    }

    private void setLastModified() {
        currentChannel.setLastModified(Calendar.getInstance());
    }

    private void updateRevision() {
        summaryRevision.setText("Revision: " + currentChannel.getRevision());
    }

    private void updateLastModified() {
        lastModified.setText("Last Modified: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentChannel.getLastModified().getTime()));
    }

    /** Load all of the saved channel information into the channel editor */
    private void loadChannelInfo() {
        parent.setPanelName("Edit Channel - " + currentChannel.getName());
        summaryNameField.setText(currentChannel.getName());
        summaryDescriptionText.setText(currentChannel.getDescription());
        updateRevision();
        updateLastModified();

        if (currentChannel.isEnabled()) {
            summaryEnabledCheckbox.setSelected(true);
        } else {
            summaryEnabledCheckbox.setSelected(false);
        }

        LinkedHashMap<String, String> scriptMap = new LinkedHashMap<String, String>();
        if (currentChannel.getPreprocessingScript() != null) {
            scriptMap.put(ScriptPanel.PREPROCESSOR_SCRIPT, currentChannel.getPreprocessingScript());
        } else {
            scriptMap.put(ScriptPanel.PREPROCESSOR_SCRIPT, "// Modify the message variable below to pre process data\nreturn message;");
        }

        if (currentChannel.getDeployScript() != null) {
            scriptMap.put(ScriptPanel.DEPLOY_SCRIPT, currentChannel.getDeployScript());
        } else {
            scriptMap.put(ScriptPanel.DEPLOY_SCRIPT, "// This script executes once when the channel is deployed\n// You only have access to the globalMap and globalChannelMap here to persist data\nreturn;");
        }

        if (currentChannel.getShutdownScript() != null) {
            scriptMap.put(ScriptPanel.SHUTDOWN_SCRIPT, currentChannel.getShutdownScript());
        } else {
            scriptMap.put(ScriptPanel.SHUTDOWN_SCRIPT, "// This script executes once when the channel is undeployed\n// You only have access to the globalMap and globalChannelMap here to persist data\nreturn;");
        }

        if (currentChannel.getPostprocessingScript() != null) {
            scriptMap.put(ScriptPanel.POSTPROCESSOR_SCRIPT, currentChannel.getPostprocessingScript());
        } else {
            scriptMap.put(ScriptPanel.POSTPROCESSOR_SCRIPT, "// This script executes once after a message has been processed\nreturn;");
        }

        scripts.setScripts(scriptMap);

        PropertyVerifier.checkChannelProperties(currentChannel);

        if (((String) currentChannel.getProperties().get("transactional")).equalsIgnoreCase("true")) {
            transactionalCheckBox.setSelected(true);
        } else {
            transactionalCheckBox.setSelected(false);
        }

        if (((String) currentChannel.getProperties().get("synchronous")).equalsIgnoreCase("false")) {
            synchronousCheckBox.setSelected(false);
        } else {
            synchronousCheckBox.setSelected(true);
        }

        if (((String) currentChannel.getProperties().get("clearGlobalChannelMap")).equalsIgnoreCase("false")) {
            clearGlobalChannelMapCheckBox.setSelected(false);
        } else {
            clearGlobalChannelMapCheckBox.setSelected(true);
        }

        if (((String) currentChannel.getProperties().get("encryptData")).equalsIgnoreCase("true")) {
            encryptMessagesCheckBox.setSelected(true);
        } else {
            encryptMessagesCheckBox.setSelected(false);
        }

        // Fix protocols and properties not set by previous versions of Mirth Connect
        fixNullProtocolsAndProperties();

        if (((String) currentChannel.getProperties().get("store_messages")).equalsIgnoreCase("false")) {
            storeMessages.setSelected(false);
            storeMessagesAll.setEnabled(false);
            storeMessagesAll.setSelected(true);
            storeMessagesDays.setEnabled(false);
            storeMessagesErrors.setEnabled(false);
            storeFiltered.setEnabled(false);
            numDays.setText("");
            numDays.setEnabled(false);
            days.setEnabled(false);
        } else {
            storeMessages.setSelected(true);

            if (((String) currentChannel.getProperties().get("error_messages_only")).equalsIgnoreCase("true")) {
                storeMessagesErrors.setSelected(true);
            } else {
                storeMessagesErrors.setSelected(false);
            }

            storeFiltered.setEnabled(true);

            if (((String) currentChannel.getProperties().get("dont_store_filtered")).equalsIgnoreCase("true")) {
                storeFiltered.setSelected(true);
            } else {
                storeFiltered.setSelected(false);
            }

            if (!((String) currentChannel.getProperties().get("max_message_age")).equalsIgnoreCase("-1")) {
                numDays.setText((String) currentChannel.getProperties().get("max_message_age"));
                storeMessagesDays.setSelected(true);
                numDays.setEnabled(true);
            } else {
                storeMessagesAll.setSelected(true);
                numDays.setText("");
                numDays.setEnabled(false);
            }
        }

        boolean enabled = parent.isSaveEnabled();

        sourceSourceDropdown.setSelectedItem(currentChannel.getSourceConnector().getTransportName());

        if (((String) currentChannel.getProperties().get("initialState")).equalsIgnoreCase("started")) {
            initialState.setSelectedItem("Started");
        } else {
            initialState.setSelectedItem("Stopped");
        }

        parent.setSaveEnabled(enabled);
    }

    /**
     * Update channels scripts
     */
    public void updateScripts() {
        currentChannel.setPreprocessingScript(scripts.getScripts().get(ScriptPanel.PREPROCESSOR_SCRIPT));
        currentChannel.setDeployScript(scripts.getScripts().get(ScriptPanel.DEPLOY_SCRIPT));
        currentChannel.setShutdownScript(scripts.getScripts().get(ScriptPanel.SHUTDOWN_SCRIPT));
        currentChannel.setPostprocessingScript(scripts.getScripts().get(ScriptPanel.POSTPROCESSOR_SCRIPT));
    }

    /**
     * Save all of the current channel information in the editor to the actual
     * channel
     */
    public boolean saveChanges() {
        if (!parent.checkChannelName(summaryNameField.getText(), currentChannel.getId())) {
            return false;
        }

        if (storeMessages.isSelected() && storeMessagesDays.isSelected() && numDays.getText().equals("")) {
            parent.alertWarning(parent, "If message pruning is enabled, the age of messages to prune cannot be blank.");
            return false;
        }

        boolean enabled = summaryEnabledCheckbox.isSelected();

        currentChannel.getSourceConnector().setProperties(sourceConnectorClass.getProperties());

        if (parent.currentContentPage == transformerPane) {
            transformerPane.accept(false);
            transformerPane.modified = false; // TODO: Check this. Fix to
            // prevent double save on
            // confirmLeave
        }
        if (parent.currentContentPage == filterPane) {
            filterPane.accept(false);
            filterPane.modified = false; // TODO: Check this. Fix to prevent
            // double save on confirmLeave
        }

        Connector temp;

        temp = currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex());
        temp.setProperties(destinationConnectorClass.getProperties());

        currentChannel.setName(summaryNameField.getText());
        currentChannel.setDescription(summaryDescriptionText.getText());

        updateScripts();

        setLastModified();

        if (transactionalCheckBox.isSelected()) {
            currentChannel.getProperties().put("transactional", "true");
        } else {
            currentChannel.getProperties().put("transactional", "false");
        }

        if (synchronousCheckBox.isSelected()) {
            currentChannel.getProperties().put("synchronous", "true");
        } else {
            currentChannel.getProperties().put("synchronous", "false");
        }

        if (clearGlobalChannelMapCheckBox.isSelected()) {
            currentChannel.getProperties().put("clearGlobalChannelMap", "true");
        } else {
            currentChannel.getProperties().put("clearGlobalChannelMap", "false");
        }

        if (encryptMessagesCheckBox.isSelected()) {
            currentChannel.getProperties().put("encryptData", "true");
        } else {
            currentChannel.getProperties().put("encryptData", "false");
        }

        if (storeFiltered.isSelected()) {
            currentChannel.getProperties().put("dont_store_filtered", "true");
        } else {
            currentChannel.getProperties().put("dont_store_filtered", "false");
        }

        if (storeMessages.isSelected()) {
            currentChannel.getProperties().put("store_messages", "true");
            if (storeMessagesAll.isSelected()) {
                currentChannel.getProperties().put("max_message_age", "-1");
            } else {
                currentChannel.getProperties().put("max_message_age", numDays.getText());
            }

            if (storeMessagesErrors.isSelected()) {
                currentChannel.getProperties().put("error_messages_only", "true");
            } else {
                currentChannel.getProperties().put("error_messages_only", "false");
            }
        } else {
            currentChannel.getProperties().put("store_messages", "false");
            currentChannel.getProperties().put("max_message_age", "-1");
        }

        if (((String) initialState.getSelectedItem()).equalsIgnoreCase("Stopped")) {
            currentChannel.getProperties().put("initialState", "stopped");
        } else {
            currentChannel.getProperties().put("initialState", "started");
        }

        String validationMessage = checkAllForms(currentChannel);
        if (validationMessage != null) {
            enabled = false;

            // If there is an error on one of the forms, then run the
            // validation on the current form to display any errors.
            if (channelView.getSelectedComponent() == destination) {
                // If the destination is enabled...
                if (currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex()).isEnabled()) {
                    destinationConnectorClass.checkProperties(destinationConnectorClass.getProperties(), true);
                }
            } else if (channelView.getSelectedComponent() == source) {
                sourceConnectorClass.checkProperties(sourceConnectorClass.getProperties(), true);
            }

            summaryEnabledCheckbox.setSelected(false);

            parent.alertCustomError(this.parent, validationMessage, CustomErrorDialog.ERROR_SAVING_CHANNEL);
        }

        // Set the channel to enabled or disabled after it has been validated
        currentChannel.setEnabled(enabled);

        boolean updated = true;

        try {
            if (!parent.channels.containsKey(currentChannel.getId())) {
                currentChannel.setId(parent.mirthClient.getGuid());
            }

            // Will throw exception if the connection died or there was an exception
            // saving the channel, skipping the rest of this code.
            updated = parent.updateChannel(currentChannel, false);

            try {
                currentChannel = (Channel) ObjectCloner.deepCopy(parent.channels.get(currentChannel.getId()));
                if (parent.currentContentPage == transformerPane) {
                    if (channelView.getSelectedIndex() == SOURCE_TAB_INDEX) {
                        transformerPane.reload(currentChannel.getSourceConnector(), currentChannel.getSourceConnector().getTransformer());
                    } else if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX) {
                        int destination = destinationTable.getSelectedModelIndex();
                        transformerPane.reload(currentChannel.getDestinationConnectors().get(destination), currentChannel.getDestinationConnectors().get(destination).getTransformer());
                    }
                }
                if (parent.currentContentPage == filterPane) {
                    if (channelView.getSelectedIndex() == SOURCE_TAB_INDEX) {
                        filterPane.reload(currentChannel.getSourceConnector(), currentChannel.getSourceConnector().getFilter());
                    } else if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX) {
                        Connector destination = currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex());
                        filterPane.reload(destination, destination.getFilter());
                    }
                }
                updateRevision();
                updateLastModified();
            } catch (ObjectClonerException e) {
                parent.alertException(this.parent, e.getStackTrace(), e.getMessage());
            }
        } catch (ClientException e) {
            parent.alertException(this.parent, e.getStackTrace(), e.getMessage());
        }

        return updated;
    }

    /**
     * Set all the protocols and properties to proper values if they are null.
     * This is only necessary for channels from before version 2.0
     */
    public void fixNullProtocolsAndProperties() {
        Transformer sourceTransformer = currentChannel.getSourceConnector().getTransformer();

        Protocol defaultProtocol = Protocol.HL7V2;

        if (sourceTransformer.getInboundProtocol() == null) {
            sourceTransformer.setInboundProtocol(defaultProtocol);
        }

        if (sourceTransformer.getInboundProperties() == null) {
            Properties defaultProperties = PropertiesUtil.convertMapToProperties(DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(sourceTransformer.getInboundProtocol()));
            sourceTransformer.setInboundProperties(defaultProperties);
        }

        if (sourceTransformer.getOutboundProtocol() == null) {
            sourceTransformer.setOutboundProtocol(sourceTransformer.getInboundProtocol());
        }

        if (sourceTransformer.getOutboundProperties() == null) {
            Properties defaultProperties = PropertiesUtil.convertMapToProperties(DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(sourceTransformer.getOutboundProtocol()));
            sourceTransformer.setOutboundProperties(defaultProperties);
        }

        for (Connector c : currentChannel.getDestinationConnectors()) {
            Transformer destinationTransformer = c.getTransformer();

            if (destinationTransformer.getInboundProtocol() == null) {
                destinationTransformer.setInboundProtocol(sourceTransformer.getOutboundProtocol());
            }

            if (destinationTransformer.getInboundProperties() == null) {
                destinationTransformer.setInboundProperties(sourceTransformer.getOutboundProperties());
            }

            if (destinationTransformer.getOutboundProtocol() == null) {
                destinationTransformer.setOutboundProtocol(destinationTransformer.getInboundProtocol());
            }

            if (destinationTransformer.getOutboundProperties() == null) {
                Properties defaultProperties = PropertiesUtil.convertMapToProperties(DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(destinationTransformer.getOutboundProtocol()));
                destinationTransformer.setOutboundProperties(defaultProperties);
            }
        }
    }

    /** Adds a new destination. */
    public void addNewDestination() {
        makeDestinationTable(true);
        destinationTablePane.getViewport().setViewPosition(new Point(0, destinationTable.getRowHeight() * destinationTable.getRowCount()));
        parent.setSaveEnabled(true);
    }

    public void cloneDestination() {
        if (parent.changesHaveBeenMade()) {
            if (parent.alertOption(this.parent, "You must save your channel before cloning.  Would you like to save your channel now?")) {
                saveChanges();
            } else {
                return;
            }
        }
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();

        Connector destination = null;
        try {
            destination = (Connector) ObjectCloner.deepCopy(destinationConnectors.get(destinationTable.getSelectedModelIndex()));
        } catch (ObjectClonerException e) {
            parent.alertException(this.parent, e.getStackTrace(), e.getMessage());
            return;
        }

        destination.setName(getNewDestinationName(destinationConnectors.size() + 1));
        destinationConnectors.add(destination);
        makeDestinationTable(false);
        parent.setSaveEnabled(true);
    }

    public void enableDestination() {
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        Connector destination = destinationConnectors.get(destinationTable.getSelectedModelIndex());
        destination.setEnabled(true);
        makeDestinationTable(false);
        parent.setSaveEnabled(true);

        // If validation has failed, then highlight any errors on this form.
        if (channelValidationFailed) {
            destinationConnectorClass.checkProperties(destinationConnectorClass.getProperties(), true);
        }
    }

    public void disableDestination() {
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();

        // Check to make sure at least two destinations are currently enabled.
        int enabledCount = 0;
        for (int i = 0; i < destinationConnectors.size(); i++) {
            if (destinationConnectors.get(i).isEnabled()) {
                enabledCount++;
            }
        }
        if (enabledCount <= 1) {
            parent.alertError(this.parent, "You must have at least one destination enabled.");
            return;
        }

        Connector destination = destinationConnectors.get(destinationTable.getSelectedModelIndex());
        destination.setEnabled(false);
        makeDestinationTable(false);
        parent.setSaveEnabled(true);

        // If validation has failed then errors might be highlighted.
        // Remove highlights on this form.
        if (channelValidationFailed) {
            destinationConnectorClass.checkProperties(destinationConnectorClass.getProperties(), false);
        }
    }

    /** Deletes the selected destination. */
    public void deleteDestination() {
        isDeleting = true;
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        if (destinationConnectors.size() == 1) {
            JOptionPane.showMessageDialog(parent, "You must have at least one destination.");
            return;
        }

        boolean enabledDestination = false;
        // if there is an enabled destination besides the one being deleted, set enabledDestination to true.
        for (int i = 0; i < destinationConnectors.size(); i++) {
            if (destinationConnectors.get(i).isEnabled() && (i != destinationTable.getSelectedModelIndex())) {
                enabledDestination = true;
            }
        }

        if (!enabledDestination) {
            JOptionPane.showMessageDialog(parent, "You must have at least one destination enabled.");
            return;
        }

        destinationConnectors.remove(destinationTable.getSelectedModelIndex());

        makeDestinationTable(false);
        parent.setSaveEnabled(true);
        isDeleting = false;
    }

    /**
     * Checks to see which tasks (move up, move down, enable, and disable) should be available for
     * destinations and enables or disables them.  Also sets the number of filter/transformer steps
     * to the task names.
     */
    public void checkVisibleDestinationTasks() {
        if (channelView.getSelectedComponent() == destination) {
            // enable and disable
            List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
            Connector destination = destinationConnectors.get(destinationTable.getSelectedModelIndex());
            if (destination.isEnabled()) {
                parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 5, 5, false);
                parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 6, 6, true);
            } else {
                parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 5, 5, true);
                parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 6, 6, false);
            }

            // move up and move down
            if (destinationTable.getSelectedModelIndex() == 0) {
                parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 7, 7, false);
            } else {
                parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 7, 7, true);
            }

            if (destinationTable.getSelectedModelIndex() == destinationTable.getRowCount() - 1) {
                parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 8, 8, false);
            } else {
                parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 8, 8, true);
            }

            // Update number of rules and steps on the filter and transformer
            parent.updateFilterTaskName(destination.getFilter().getRules().size());
            parent.updateTransformerTaskName(destination.getTransformer().getSteps().size());
        }
    }

    /**
     * Moves the selected destination to the previous spot in the array list.
     */
    public void moveDestinationUp() {
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        int destinationIndex = destinationTable.getSelectedModelIndex();

        destinationConnectors.add(destinationIndex - 1, destinationConnectors.get(destinationIndex));
        destinationConnectors.remove(destinationIndex + 1);
        lastModelIndex--;

        makeDestinationTable(false);
        setDestinationVariableList();
        parent.setSaveEnabled(true);
    }

    /**
     * Moves the selected destination to the next spot in the array list.
     */
    public void moveDestinationDown() {
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        int destinationIndex = destinationTable.getSelectedModelIndex();

        destinationConnectors.add(destinationIndex + 2, destinationConnectors.get(destinationIndex));
        destinationConnectors.remove(destinationIndex);
        lastModelIndex++;

        makeDestinationTable(false);
        setDestinationVariableList();
        parent.setSaveEnabled(true);
    }

    /**
     * Checks all of the connectors in this channel and returns the errors found.
     * 
     * @param channel
     * @return
     */
    public String checkAllForms(Channel channel) {
        String errors = "";
        ConnectorClass tempConnector = null;
        Properties tempProps = null;

        // Check source connector
        for (int i = 0; i < parent.sourceConnectors.size(); i++) {
            if (parent.sourceConnectors.get(i).getName().equalsIgnoreCase(channel.getSourceConnector().getTransportName())) {
                tempConnector = parent.sourceConnectors.get(i);
                tempProps = channel.getSourceConnector().getProperties();

                errors += validateFilterRules(channel.getSourceConnector());
                errors += validateTransformerSteps(channel.getSourceConnector());
            }
        }
        if (tempConnector != null) {
            String validationMessage = tempConnector.doValidate(tempProps, false);
            if (validationMessage != null) {
                errors += validationMessage;
            }
        }

        // Check destination connector
        for (int i = 0; i < channel.getDestinationConnectors().size(); i++) {
            // Only check the destination connector if it is enabled.
            if (channel.getDestinationConnectors().get(i).isEnabled()) {
                for (int j = 0; j < parent.destinationConnectors.size(); j++) {
                    if (parent.destinationConnectors.get(j).getName().equalsIgnoreCase(channel.getDestinationConnectors().get(i).getTransportName())) {
                        tempConnector = parent.destinationConnectors.get(j);
                        tempProps = channel.getDestinationConnectors().get(i).getProperties();

                        errors += validateFilterRules(channel.getDestinationConnectors().get(i));
                        errors += validateTransformerSteps(channel.getDestinationConnectors().get(i));
                    }
                }
                if (tempConnector != null) {
                    String validationMessage = tempConnector.doValidate(tempProps, false);
                    if (validationMessage != null) {
                        errors += validationMessage;
                    }
                }

                tempConnector = null;
                tempProps = null;
            }
        }

        errors += validateScripts(channel);

        if (errors.equals("")) {
            errors = null;
            channelValidationFailed = false;
        } else {
            channelValidationFailed = true;
        }

        return errors;
    }

    private String validateTransformerSteps(Connector connector) {
        String errors = "";

        for (Step step : connector.getTransformer().getSteps()) {
            String validationMessage = this.transformerPane.validateStep(step);
            if (validationMessage != null) {
                errors += "Error in connector \"" + connector.getName() + "\" at step " + step.getSequenceNumber() + " (\"" + step.getName() + "\"):\n" + validationMessage + "\n\n";
            }
        }

        return errors;
    }

    private String validateFilterRules(Connector connector) {
        String errors = "";

        for (Rule rule : connector.getFilter().getRules()) {
            String validationMessage = this.filterPane.validateRule(rule);
            if (validationMessage != null) {
                String ruleName;
                if (rule.getName() == null) {
                    ruleName = "";
                } else {
                    ruleName = rule.getName();
                }

                errors += "Error in connector \"" + connector.getName() + "\" at rule " + rule.getSequenceNumber() + " (\"" + ruleName + "\"):\n" + validationMessage + "\n\n";
            }
        }

        return errors;
    }

    private String validateScripts(Channel channel) {
        String errors = "";

        String validationMessage = this.scripts.validateScript(channel.getDeployScript());
        if (validationMessage != null) {
            errors += "Error in channel script \"" + ScriptPanel.DEPLOY_SCRIPT + "\":\n" + validationMessage + "\n\n";
        }

        validationMessage = this.scripts.validateScript(channel.getPreprocessingScript());
        if (validationMessage != null) {
            errors += "Error in channel script \"" + ScriptPanel.PREPROCESSOR_SCRIPT + "\":\n" + validationMessage + "\n\n";
        }

        validationMessage = this.scripts.validateScript(channel.getPostprocessingScript());
        if (validationMessage != null) {
            errors += "Error in channel script \"" + ScriptPanel.POSTPROCESSOR_SCRIPT + "\":\n" + validationMessage + "\n\n";
        }

        validationMessage = this.scripts.validateScript(channel.getShutdownScript());
        if (validationMessage != null) {
            errors += "Error in channel script \"" + ScriptPanel.SHUTDOWN_SCRIPT + "\":\n" + validationMessage + "\n\n";
        }

        return errors;
    }

    public void doValidate() {
        if (source.isVisible()) {
            String validationMessage = sourceConnectorClass.doValidate(sourceConnectorClass.getProperties(), true);
            if (validationMessage != null) {
                parent.alertCustomError(this.parent, validationMessage, CustomErrorDialog.ERROR_VALIDATING_CONNECTOR);
            } else {
                parent.alertInformation(this.parent, "The connector was successfully validated.");
            }
        } else {
            String validationMessage = destinationConnectorClass.doValidate(destinationConnectorClass.getProperties(), true);
            if (validationMessage != null) {
                parent.alertWarning(this.parent, validationMessage);
            } else {
                parent.alertInformation(this.parent, "The connector was successfully validated.");
            }
        }
    }

    public void validateScripts() {
        scripts.validateCurrentScript();
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        channelView = new javax.swing.JTabbedPane();
        summary = new javax.swing.JPanel();
        summaryNameLabel = new javax.swing.JLabel();
        summaryDescriptionLabel = new javax.swing.JLabel();
        summaryNameField = new com.mirth.connect.client.ui.components.MirthTextField();
        summaryPatternLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        summaryDescriptionText = new com.mirth.connect.client.ui.components.MirthTextPane();
        jLabel1 = new javax.swing.JLabel();
        initialState = new com.mirth.connect.client.ui.components.MirthComboBox();
        encryptMessagesCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        storeMessages = new com.mirth.connect.client.ui.components.MirthCheckBox();
        storeMessagesAll = new com.mirth.connect.client.ui.components.MirthRadioButton();
        storeMessagesDays = new com.mirth.connect.client.ui.components.MirthRadioButton();
        days = new javax.swing.JLabel();
        numDays = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel3 = new javax.swing.JLabel();
        storeMessagesErrors = new com.mirth.connect.client.ui.components.MirthCheckBox();
        storeFiltered = new com.mirth.connect.client.ui.components.MirthCheckBox();
        jPanel1 = new javax.swing.JPanel();
        summaryEnabledCheckbox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        transactionalCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        synchronousCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        clearGlobalChannelMapCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        summaryRevision = new javax.swing.JLabel();
        lastModified = new javax.swing.JLabel();
        changeDataTypesButton = new javax.swing.JButton();
        source = new javax.swing.JPanel();
        sourceSourceDropdown = new com.mirth.connect.client.ui.components.MirthComboBox();
        sourceSourceLabel = new javax.swing.JLabel();
        sourceConnectorPane = new javax.swing.JScrollPane();
        sourceConnectorClass = new com.mirth.connect.connectors.ConnectorClass();
        destination = new javax.swing.JPanel();
        destinationSourceDropdown = new com.mirth.connect.client.ui.components.MirthComboBox();
        destinationSourceLabel = new javax.swing.JLabel();
        destinationVariableList = new com.mirth.connect.client.ui.VariableList();
        destinationConnectorPane = new javax.swing.JScrollPane();
        destinationConnectorClass = new com.mirth.connect.connectors.ConnectorClass();
        destinationTablePane = new javax.swing.JScrollPane();
        destinationTable = new com.mirth.connect.client.ui.components.MirthTable();
        scripts = new ScriptPanel(ContextType.CHANNEL_CONTEXT.getContext());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        channelView.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        channelView.setFocusable(false);

        summary.setBackground(new java.awt.Color(255, 255, 255));
        summary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        summary.setFocusable(false);
        summary.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                summaryComponentShown(evt);
            }
        });

        summaryNameLabel.setText("Channel Name:");

        summaryDescriptionLabel.setText("Description:");

        summaryNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                summaryNameFieldKeyReleased(evt);
            }
        });

        summaryPatternLabel1.setText("Data Types:");

        jScrollPane1.setViewportView(summaryDescriptionText);

        jLabel1.setText("Initial State:");

        initialState.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Started", "Stopped" }));

        encryptMessagesCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        encryptMessagesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        encryptMessagesCheckBox.setText("Encrypt messages in database");
        encryptMessagesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        storeMessages.setBackground(new java.awt.Color(255, 255, 255));
        storeMessages.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        storeMessages.setText("Store message data");
        storeMessages.setMargin(new java.awt.Insets(0, 0, 0, 0));
        storeMessages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeMessagesActionPerformed(evt);
            }
        });

        storeMessagesAll.setBackground(new java.awt.Color(255, 255, 255));
        storeMessagesAll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(storeMessagesAll);
        storeMessagesAll.setText("Store indefinitely");
        storeMessagesAll.setMargin(new java.awt.Insets(0, 0, 0, 0));
        storeMessagesAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeMessagesAllActionPerformed(evt);
            }
        });

        storeMessagesDays.setBackground(new java.awt.Color(255, 255, 255));
        storeMessagesDays.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(storeMessagesDays);
        storeMessagesDays.setText("Prune messages after");
        storeMessagesDays.setMargin(new java.awt.Insets(0, 0, 0, 0));
        storeMessagesDays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeMessagesDaysActionPerformed(evt);
            }
        });

        days.setText("day(s)");

        jLabel3.setText("Messages:");

        storeMessagesErrors.setBackground(new java.awt.Color(255, 255, 255));
        storeMessagesErrors.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        storeMessagesErrors.setText("With errors only");
        storeMessagesErrors.setMargin(new java.awt.Insets(0, 0, 0, 0));
        storeMessagesErrors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeMessagesErrorsActionPerformed(evt);
            }
        });

        storeFiltered.setBackground(new java.awt.Color(255, 255, 255));
        storeFiltered.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        storeFiltered.setText("Do not store filtered messages");
        storeFiltered.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        summaryEnabledCheckbox.setBackground(new java.awt.Color(255, 255, 255));
        summaryEnabledCheckbox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        summaryEnabledCheckbox.setSelected(true);
        summaryEnabledCheckbox.setText("Enabled");
        summaryEnabledCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        transactionalCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        transactionalCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        transactionalCheckBox.setText("Use transactional endpoints");
        transactionalCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        synchronousCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        synchronousCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        synchronousCheckBox.setText("Synchronize channel");
        synchronousCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        synchronousCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                synchronousCheckBoxActionPerformed(evt);
            }
        });

        clearGlobalChannelMapCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        clearGlobalChannelMapCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        clearGlobalChannelMapCheckBox.setText("Clear global channel map on deploy");
        clearGlobalChannelMapCheckBox.setToolTipText("This will clear the global channel map on both single channel deploy and a full redeploy.");
        clearGlobalChannelMapCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(summaryEnabledCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(transactionalCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(92, 92, 92))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(synchronousCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(128, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(clearGlobalChannelMapCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(summaryEnabledCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(transactionalCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(synchronousCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearGlobalChannelMapCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        summaryRevision.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        summaryRevision.setText("Revision: ");

        lastModified.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lastModified.setText("Last Modified: ");

        changeDataTypesButton.setText("Set Data Types");
        changeDataTypesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeDataTypesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout summaryLayout = new javax.swing.GroupLayout(summary);
        summary.setLayout(summaryLayout);
        summaryLayout.setHorizontalGroup(
            summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(summaryLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(summaryDescriptionLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(summaryPatternLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(summaryNameLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(summaryLayout.createSequentialGroup()
                        .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(initialState, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(summaryNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(encryptMessagesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(changeDataTypesButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(summaryRevision, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                            .addComponent(lastModified, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)))
                    .addComponent(storeMessages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(summaryLayout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(summaryLayout.createSequentialGroup()
                                .addComponent(storeMessagesAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(storeMessagesDays, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(4, 4, 4)
                                .addComponent(numDays, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(days))
                            .addComponent(storeMessagesErrors, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(storeFiltered, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE))
                .addContainerGap())
        );
        summaryLayout.setVerticalGroup(
            summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(summaryLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(summaryLayout.createSequentialGroup()
                        .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(summaryLayout.createSequentialGroup()
                                .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(summaryNameLabel)
                                    .addComponent(summaryNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(summaryPatternLabel1)
                                    .addComponent(changeDataTypesButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel1)
                                    .addComponent(initialState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3)
                                    .addComponent(encryptMessagesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(storeMessages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(storeFiltered, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(storeMessagesErrors, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(storeMessagesAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(numDays, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(storeMessagesDays, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(days)))
                    .addGroup(summaryLayout.createSequentialGroup()
                        .addComponent(summaryRevision)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lastModified)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(summaryDescriptionLabel)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE))
                .addContainerGap())
        );

        channelView.addTab("Summary", summary);

        source.setBackground(new java.awt.Color(255, 255, 255));
        source.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        source.setFocusable(false);
        source.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                sourceComponentShown(evt);
            }
        });

        sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NHIN CONNECT Gateway Listener", "Web Service Listener", "Email" }));
        sourceSourceDropdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceSourceDropdownActionPerformed(evt);
            }
        });

        sourceSourceLabel.setText("Connector Type:");

        sourceConnectorPane.setBackground(new java.awt.Color(255, 255, 255));
        sourceConnectorPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Connetor Class"));

        sourceConnectorClass.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout sourceConnectorClassLayout = new javax.swing.GroupLayout(sourceConnectorClass);
        sourceConnectorClass.setLayout(sourceConnectorClassLayout);
        sourceConnectorClassLayout.setHorizontalGroup(
            sourceConnectorClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1428, Short.MAX_VALUE)
        );
        sourceConnectorClassLayout.setVerticalGroup(
            sourceConnectorClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 646, Short.MAX_VALUE)
        );

        sourceConnectorPane.setViewportView(sourceConnectorClass);

        javax.swing.GroupLayout sourceLayout = new javax.swing.GroupLayout(source);
        source.setLayout(sourceLayout);
        sourceLayout.setHorizontalGroup(
            sourceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sourceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sourceConnectorPane, javax.swing.GroupLayout.DEFAULT_SIZE, 656, Short.MAX_VALUE)
                    .addGroup(sourceLayout.createSequentialGroup()
                        .addComponent(sourceSourceLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sourceSourceDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        sourceLayout.setVerticalGroup(
            sourceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sourceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceSourceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sourceSourceDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sourceConnectorPane, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                .addContainerGap())
        );

        channelView.addTab("Source", source);

        destination.setBackground(new java.awt.Color(255, 255, 255));
        destination.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        destination.setFocusable(false);
        destination.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                destinationComponentShown(evt);
            }
        });

        destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NHIN CONNECT Gateway Listener", "Web Service Listener", "Email" }));
        destinationSourceDropdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                destinationSourceDropdownActionPerformed(evt);
            }
        });

        destinationSourceLabel.setText("Connector Type:");

        destinationConnectorPane.setBackground(new java.awt.Color(255, 255, 255));
        destinationConnectorPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Connector Class"));

        destinationConnectorClass.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout destinationConnectorClassLayout = new javax.swing.GroupLayout(destinationConnectorClass);
        destinationConnectorClass.setLayout(destinationConnectorClassLayout);
        destinationConnectorClassLayout.setHorizontalGroup(
            destinationConnectorClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 624, Short.MAX_VALUE)
        );
        destinationConnectorClassLayout.setVerticalGroup(
            destinationConnectorClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 601, Short.MAX_VALUE)
        );

        destinationConnectorPane.setViewportView(destinationConnectorClass);

        destinationTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        destinationTablePane.setViewportView(destinationTable);

        javax.swing.GroupLayout destinationLayout = new javax.swing.GroupLayout(destination);
        destination.setLayout(destinationLayout);
        destinationLayout.setHorizontalGroup(
            destinationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(destinationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(destinationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(destinationTablePane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 656, Short.MAX_VALUE)
                    .addGroup(destinationLayout.createSequentialGroup()
                        .addComponent(destinationConnectorPane, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(destinationVariableList, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, destinationLayout.createSequentialGroup()
                        .addComponent(destinationSourceLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(destinationSourceDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        destinationLayout.setVerticalGroup(
            destinationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(destinationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(destinationTablePane, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(destinationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(destinationSourceLabel)
                    .addComponent(destinationSourceDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(destinationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(destinationVariableList, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                    .addComponent(destinationConnectorPane, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE))
                .addContainerGap())
        );

        channelView.addTab("Destinations", destination);

        scripts.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                scriptsComponentShown(evt);
            }
        });
        channelView.addTab("Scripts", scripts);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(channelView, javax.swing.GroupLayout.DEFAULT_SIZE, 681, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(channelView, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void synchronousCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_synchronousCheckBoxActionPerformed
    {//GEN-HEADEREND:event_synchronousCheckBoxActionPerformed
        if (!synchronousCheckBox.isSelected()) {
            boolean disableSynch = parent.alertOption(this.parent, "Disabling synchronization is not recommended for the following reasons:\n"
                    + "    1) Destinations are not guaranteed to run in order, so they cannot reference each other.\n"
                    + "    2) Your source cannot send a response from any of the destinations.\n"
                    + "    3) Your source connector may be modified to to ensure compatibility with requirement #2.\n"
                    + "    4) Turning off synchronization will disable the post-processor.\n"
                    + "Are you sure you want to disable synchronization of your channel?");

            if (!disableSynch) {
                synchronousCheckBox.setSelected(true);
            }
        }

        sourceConnectorClass.updateResponseDropDown();
    }//GEN-LAST:event_synchronousCheckBoxActionPerformed

    private void scriptsComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_scriptsComponentShown
    {//GEN-HEADEREND:event_scriptsComponentShown
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 12, false);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 14, 14, true);
    }//GEN-LAST:event_scriptsComponentShown

    private void changeDataTypesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeDataTypesButtonActionPerformed
        new DataTypesDialog();
    }//GEN-LAST:event_changeDataTypesButtonActionPerformed

    private void summaryNameFieldKeyReleased(java.awt.event.KeyEvent evt)// GEN-FIRST:event_summaryNameFieldKeyReleased
    {// GEN-HEADEREND:event_summaryNameFieldKeyReleased
        currentChannel.setName(summaryNameField.getText());
        parent.setPanelName("Edit Channel - " + currentChannel.getName());
    }// GEN-LAST:event_summaryNameFieldKeyReleased

    private void storeMessagesErrorsActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void storeMessagesDaysActionPerformed(java.awt.event.ActionEvent evt) {
        numDays.setEnabled(true);
    }

    private void storeMessagesAllActionPerformed(java.awt.event.ActionEvent evt) {
        numDays.setEnabled(false);
        numDays.setText("");
    }

    private void storeMessagesActionPerformed(java.awt.event.ActionEvent evt) {
        if (storeMessages.isSelected()) {
            storeMessagesAll.setEnabled(true);
            storeMessagesDays.setEnabled(true);
            storeMessagesErrors.setEnabled(true);
            storeFiltered.setEnabled(true);
            days.setEnabled(true);
            numDays.setEnabled(true);
        } else {
            storeMessagesAll.setEnabled(false);
            storeMessagesDays.setEnabled(false);
            storeMessagesErrors.setEnabled(false);
            storeFiltered.setEnabled(false);
            days.setEnabled(false);
            numDays.setText("");
            numDays.setEnabled(false);
        }
    }

    /** Action when the summary tab is shown. */
    private void summaryComponentShown(java.awt.event.ComponentEvent evt)// GEN-FIRST:event_summaryComponentShown
    {
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 12, false);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 14, 14, false);
    }

    /** Action when the source tab is shown. */
    private void sourceComponentShown(java.awt.event.ComponentEvent evt)// GEN-FIRST:event_sourceComponentShown
    {
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 1, true);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 2, 8, false);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 9, 13, true);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 14, 14, false);

        // Update number of rules and steps on the filter and transformer
        parent.updateFilterTaskName(currentChannel.getSourceConnector().getFilter().getRules().size());
        parent.updateTransformerTaskName(currentChannel.getSourceConnector().getTransformer().getSteps().size());


        int connectorIndex = destinationTable.getSelectedModelIndex();
        Connector destinationConnector = currentChannel.getDestinationConnectors().get(connectorIndex);
        destinationConnector.setProperties(destinationConnectorClass.getProperties());
        updateScripts();

        sourceConnectorClass.updateResponseDropDown();

        // If validation has failed, then highlight any errors on this form.
        if (channelValidationFailed) {
            sourceConnectorClass.checkProperties(sourceConnectorClass.getProperties(), true);
        }
    }

    /** Action when the destinations tab is shown. */
    private void destinationComponentShown(java.awt.event.ComponentEvent evt)// GEN-FIRST:event_destinationComponentShown
    {
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 1, true);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 2, 12, true);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 14, 14, false);

        checkVisibleDestinationTasks();

        // If validation has failed and this destination is enabled, then highlight any errors on this form.
        if (channelValidationFailed && currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex()).isEnabled()) {
            destinationConnectorClass.checkProperties(destinationConnectorClass.getProperties(), true);
        }
    }

    /** Action when an action is performed on the source connector type dropdown. */
    private void sourceSourceDropdownActionPerformed(java.awt.event.ActionEvent evt) {
        // If a channel is not being loaded then alert the user when necessary
        // that
        // changing the connector type will lose all current connector data.
        if (!loadingChannel) {
            if (sourceConnectorClass.getName() != null && sourceConnectorClass.getName().equals(sourceSourceDropdown.getSelectedItem())) {
                return;
            }

            if (!PropertyVerifier.compareProps(sourceConnectorClass.getProperties(), sourceConnectorClass.getDefaults())) {
                boolean changeType = parent.alertOption(this.parent, "Are you sure you would like to change this connector type and lose all of the current connector data?");
                if (!changeType) {
                    sourceSourceDropdown.setSelectedItem(sourceConnectorClass.getProperties().get(DATA_TYPE_KEY));
                    return;
                }
            }
        }

        if (currentChannel.getSourceConnector().getTransportName().equalsIgnoreCase(DATABASE_READER)) {
            currentChannel.getSourceConnector().getTransformer().setInboundTemplate("");

            if (parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundProtocol() == MessageObject.Protocol.XML
                    && parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundTemplate() != null
                    && parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundTemplate().length() == 0) {
                List<Connector> list = parent.channelEditPanel.currentChannel.getDestinationConnectors();
                for (Connector c : list) {
                    c.getTransformer().setInboundTemplate("");
                }
            }
        }

        // Get the selected source connector and set it.
        for (int i = 0; i < parent.sourceConnectors.size(); i++) {
            if (parent.sourceConnectors.get(i).getName().equalsIgnoreCase((String) sourceSourceDropdown.getSelectedItem())) {
                sourceConnectorClass = parent.sourceConnectors.get(i);
            }
        }

        // Sets all of the properties, transformer, filter, etc. on the new
        // source connector.
        Connector sourceConnector = currentChannel.getSourceConnector();
        if (sourceConnector != null) {
            String dataType = sourceConnector.getProperties().getProperty(DATA_TYPE_KEY);
            if (dataType == null) {
                dataType = "";
            }

            if (sourceConnector.getProperties().size() == 0 || !dataType.equals(sourceSourceDropdown.getSelectedItem())) {
                String name = sourceConnector.getName();
                changeConnectorType(sourceConnector, false);
                sourceConnector.setName(name);
                sourceConnectorClass.setProperties(sourceConnectorClass.getDefaults());
                sourceConnector.setProperties(sourceConnectorClass.getProperties());
            }

            sourceConnector.setTransportName((String) sourceSourceDropdown.getSelectedItem());
            currentChannel.setSourceConnector(sourceConnector);
            sourceConnectorClass.setProperties(sourceConnector.getProperties());
        }

        // Set the source data type to XML if necessary
        checkAndSetXmlDataType();

        sourceConnectorPane.setViewportView(sourceConnectorClass);
        ((TitledBorder) sourceConnectorPane.getBorder()).setTitle(sourceConnectorClass.getName());
        sourceConnectorPane.repaint();

        // If validation has failed, then highlight any errors on this form.
        if (channelValidationFailed) {
            sourceConnectorClass.checkProperties(sourceConnectorClass.getProperties(), true);
        }
    }

    /**
     * Action when an action is performed on the destination connector type
     * dropdown. Fires off either generateMultipleDestinationPage() or
     * generateSingleDestinationPage()
     */
    private void destinationSourceDropdownActionPerformed(java.awt.event.ActionEvent evt) {
        // If a channel is not being loaded then alert the user when necessary
        // that
        // changing the connector type will lose all current connector data.
        if (!loadingChannel) {
            if (destinationConnectorClass.getName() != null && destinationConnectorClass.getName().equals(destinationSourceDropdown.getSelectedItem()) && lastModelIndex == destinationTable.getSelectedModelIndex()) {
                return;
            }

            // if the selected destination is still the same AND the default
            // properties/transformer/filter have
            // not been changed from defaults then ask if the user would
            // like to really change connector type.
            if (lastModelIndex == destinationTable.getSelectedModelIndex() && (!PropertyVerifier.compareProps(destinationConnectorClass.getProperties(), destinationConnectorClass.getDefaults()) || currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex()).getFilter().getRules().size() > 0 || currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex()).getTransformer().getSteps().size() > 0)) {
                boolean changeType = parent.alertOption(this.parent, "Are you sure you would like to change this connector type and lose all of the current connector data?");
                if (!changeType) {
                    destinationSourceDropdown.setSelectedItem(destinationConnectorClass.getProperties().get(DATA_TYPE_KEY));
                    return;
                }
            }
        }
        generateMultipleDestinationPage();

        // If validation has failed and this destination is enabled, then highlight any errors on this form.
        if (channelValidationFailed && currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex()).isEnabled()) {
            destinationConnectorClass.checkProperties(destinationConnectorClass.getProperties(), true);
        }
    }

    public void generateMultipleDestinationPage() {
        // Get the selected destination connector and set it.
        for (int i = 0; i < parent.destinationConnectors.size(); i++) {
            if (parent.destinationConnectors.get(i).getName().equalsIgnoreCase((String) destinationSourceDropdown.getSelectedItem())) {
                destinationConnectorClass = parent.destinationConnectors.get(i);
            }
        }

        // Get the currently selected destination connector.
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        int connectorIndex = destinationTable.getSelectedModelIndex();
        Connector destinationConnector = destinationConnectors.get(connectorIndex);

        String dataType = destinationConnector.getProperties().getProperty(DATA_TYPE_KEY);
        if (dataType == null) {
            dataType = "";
        }

        // Debug with:
        // System.out.println(destinationConnector.getTransportName() + " " +
        // (String)destinationSourceDropdown.getSelectedItem());

        // Set to defaults on first load of connector or if it has changed
        // types.
        if (destinationConnector.getProperties().size() == 0 || !dataType.equals(destinationSourceDropdown.getSelectedItem())) {
            String name = destinationConnector.getName();
            changeConnectorType(destinationConnector, true);
            destinationConnector.setName(name);
            destinationConnectorClass.setProperties(destinationConnectorClass.getDefaults());
            destinationConnector.setProperties(destinationConnectorClass.getProperties());
        }

        destinationVariableList.setPrefixAndSuffix(destinationConnectorClass.getDragAndDropCharacters(destinationConnector.getProperties())[0], destinationConnectorClass.getDragAndDropCharacters(destinationConnector.getProperties())[1]);

        // Set the transport name of the destination connector and set it in the
        // list.
        destinationConnector.setTransportName((String) destinationSourceDropdown.getSelectedItem());
        destinationConnectors.set(connectorIndex, destinationConnector);

        // If the connector type has changed then set the new value in the
        // destination table.
        if (destinationConnector.getTransportName() != null && !((String) destinationTable.getModel().getValueAt(destinationTable.getSelectedModelIndex(), destinationTable.getColumnModelIndex(CONNECTOR_TYPE_COLUMN_NAME))).equals(destinationConnector.getTransportName()) && destinationTable.getSelectedModelIndex() != -1) {
            destinationTable.getModel().setValueAt(destinationSourceDropdown.getSelectedItem(), destinationTable.getSelectedModelIndex(), destinationTable.getColumnModelIndex(CONNECTOR_TYPE_COLUMN_NAME));
        }

        // Debug with:
        // System.out.println(destinationConnector.getProperties().toString());
        destinationConnectorClass.setProperties(destinationConnector.getProperties());
        setDestinationVariableList();

        destinationConnectorPane.setViewportView(destinationConnectorClass);
        ((TitledBorder) destinationConnectorPane.getBorder()).setTitle(destinationConnectorClass.getName());
        destinationConnectorPane.repaint();
    }

    private Set<String> getMultipleDestinationRules(Connector currentDestination) {
        Set<String> concatenatedRules = new LinkedHashSet<String>();
        VariableListUtil.getRuleVariables(concatenatedRules, currentChannel.getSourceConnector(), false);

        // add only the global variables
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        Iterator<Connector> it = destinationConnectors.iterator();
        boolean seenCurrent = false;
        while (it.hasNext()) {
            Connector destination = it.next();
            if (currentDestination == destination) {
                seenCurrent = true;
                // add all the variables
                VariableListUtil.getRuleVariables(concatenatedRules, destination, true);
            } else if (!seenCurrent) {
                // add only the global variables
                VariableListUtil.getRuleVariables(concatenatedRules, destination, false);
                concatenatedRules.add(destination.getName());
            }
        }
        return concatenatedRules;
    }

    private Set<String> getMultipleDestinationStepVariables(Connector currentDestination) {
        Set<String> concatenatedSteps = new LinkedHashSet<String>();
        VariableListUtil.getStepVariables(concatenatedSteps, currentChannel.getSourceConnector(), false);

        // add only the global variables
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        Iterator<Connector> it = destinationConnectors.iterator();
        boolean seenCurrent = false;
        while (it.hasNext()) {
            Connector destination = it.next();
            if (currentDestination == destination) {
                seenCurrent = true;
                // add all the variables
                VariableListUtil.getStepVariables(concatenatedSteps, destination, true);
            } else if (!seenCurrent) {
                // add only the global variables
                VariableListUtil.getStepVariables(concatenatedSteps, destination, false);
                concatenatedSteps.add(destination.getName());
            }
        }
        return concatenatedSteps;
    }

    /** Sets the destination variable list from the transformer steps */
    public void setDestinationVariableList() {
        int destination = destinationTable.getSelectedModelIndex();
        Set<String> concatenatedRuleVariables = getMultipleDestinationRules(currentChannel.getDestinationConnectors().get(destination));
        Set<String> concatenatedStepVariables = getMultipleDestinationStepVariables(currentChannel.getDestinationConnectors().get(destination));
        concatenatedRuleVariables.addAll(concatenatedStepVariables);
        destinationVariableList.setVariableListInbound(concatenatedRuleVariables);
        destinationVariableList.setDestinationMappingsLabel();
        destinationVariableList.repaint();
    }

    /** Returns a new connector, that has a new transformer and filter */
    public Connector makeNewConnector(boolean isDestination) {
        Connector c = new Connector();
        c.setEnabled(true);

        Transformer dt = new Transformer();
        Filter df = new Filter();

        if (isDestination) {
            c.setMode(Connector.Mode.DESTINATION);
        } else {
            c.setMode(Connector.Mode.SOURCE);
        }

        c.setTransformer(dt);
        c.setFilter(df);
        return c;
    }

    /** Changes the connector type without clearing filter and transformer */
    public void changeConnectorType(Connector c, boolean isDestination) {
        Transformer oldTransformer = c.getTransformer();
        Filter oldFilter = c.getFilter();

        if (isDestination) {
            c = makeNewConnector(true);
        } else {
            c = makeNewConnector(false);
        }

        c.setTransformer(oldTransformer);
        c.setFilter(oldFilter);
    }

    /** Returns the source connector class */
    public ConnectorClass getSourceConnector() {
        return sourceConnectorClass;
    }

    /** Returns the destination connector class */
    public ConnectorClass getDestinationConnector() {
        return destinationConnectorClass;
    }

    /**
     * Returns true if this channel requires XML as a source data type,
     * and false if it does not.
     */
    public boolean requiresXmlDataType() {
        if (((String) sourceSourceDropdown.getSelectedItem()).equals(DATABASE_READER)) {
            return true;
        } else if (((String) sourceSourceDropdown.getSelectedItem()).equals(HTTP_LISTENER)) {
            String bodyOnly = sourceConnectorClass.getProperties().getProperty(HTTP_BODY_ONLY);
            if (bodyOnly != null && bodyOnly.equals(UIConstants.NO_OPTION)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if the source data type is required to be XML, and set it
     * if necessary.
     */
    public void checkAndSetXmlDataType() {
        Protocol xml = Protocol.XML;
        if (requiresXmlDataType() && !currentChannel.getSourceConnector().getTransformer().getInboundProtocol().equals(xml)) {
            Properties defaultProperties = PropertiesUtil.convertMapToProperties(DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(xml));

            currentChannel.getSourceConnector().getTransformer().setInboundProtocol(xml);
            currentChannel.getSourceConnector().getTransformer().setInboundProperties(defaultProperties);
        }
    }

    public void updateComponentShown() {
        if (channelView.getSelectedIndex() == SOURCE_TAB_INDEX) {
            sourceComponentShown(null);
        } else if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX) {
            destinationComponentShown(null);
        }
    }

    public Connector exportSelectedConnector() {
        if (channelView.getSelectedIndex() == SOURCE_TAB_INDEX) {
            return currentChannel.getSourceConnector();
        } else if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX) {
            return currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex());
        } else {
            return null;
        }
    }

    public void importConnector(Connector connector) {
        loadingChannel = true;

        // If the connector is a source, then set it, change the dropdown, and set the incoming data protocol.
        if ((channelView.getSelectedIndex() == SOURCE_TAB_INDEX) && (connector.getMode().equals(Mode.SOURCE))) {
            currentChannel.setSourceConnector(connector);
            sourceSourceDropdown.setSelectedItem(currentChannel.getSourceConnector().getTransportName());
        } // If the connector is a destination, then check/generate its name, add it, and re-make the destination table.
        else if ((channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX) && (connector.getMode().equals(Mode.DESTINATION))) {
            List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
            for (Connector destinationConnector : destinationConnectors) {
                if (destinationConnector.getName().equalsIgnoreCase(connector.getName())) {
                    connector.setName(getNewDestinationName(destinationConnectors.size() + 1));
                }
            }
            destinationConnectors.add(connector);
            makeDestinationTable(false);
        } // If the mode and tab don't match, display an error message and return.
        else {
            String errorMessage = "You must be on the Source tab to import a Source connector.";
            if (connector.getMode().equals(Mode.DESTINATION)) {
                errorMessage = "You must be on the Destinations tab to import a Destination connector.";
            }
            parent.alertError(parent, errorMessage);

            loadingChannel = false;
            return;
        }

        loadingChannel = false;

        parent.setSaveEnabled(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton changeDataTypesButton;
    private javax.swing.JTabbedPane channelView;
    public com.mirth.connect.client.ui.components.MirthCheckBox clearGlobalChannelMapCheckBox;
    private javax.swing.JLabel days;
    private javax.swing.JPanel destination;
    private com.mirth.connect.connectors.ConnectorClass destinationConnectorClass;
    private javax.swing.JScrollPane destinationConnectorPane;
    private com.mirth.connect.client.ui.components.MirthComboBox destinationSourceDropdown;
    private javax.swing.JLabel destinationSourceLabel;
    private com.mirth.connect.client.ui.components.MirthTable destinationTable;
    private javax.swing.JScrollPane destinationTablePane;
    public com.mirth.connect.client.ui.VariableList destinationVariableList;
    private com.mirth.connect.client.ui.components.MirthCheckBox encryptMessagesCheckBox;
    private com.mirth.connect.client.ui.components.MirthComboBox initialState;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lastModified;
    private com.mirth.connect.client.ui.components.MirthTextField numDays;
    private com.mirth.connect.client.ui.ScriptPanel scripts;
    private javax.swing.JPanel source;
    private com.mirth.connect.connectors.ConnectorClass sourceConnectorClass;
    private javax.swing.JScrollPane sourceConnectorPane;
    private com.mirth.connect.client.ui.components.MirthComboBox sourceSourceDropdown;
    private javax.swing.JLabel sourceSourceLabel;
    private com.mirth.connect.client.ui.components.MirthCheckBox storeFiltered;
    private com.mirth.connect.client.ui.components.MirthCheckBox storeMessages;
    private com.mirth.connect.client.ui.components.MirthRadioButton storeMessagesAll;
    private com.mirth.connect.client.ui.components.MirthRadioButton storeMessagesDays;
    private com.mirth.connect.client.ui.components.MirthCheckBox storeMessagesErrors;
    private javax.swing.JPanel summary;
    private javax.swing.JLabel summaryDescriptionLabel;
    private com.mirth.connect.client.ui.components.MirthTextPane summaryDescriptionText;
    private com.mirth.connect.client.ui.components.MirthCheckBox summaryEnabledCheckbox;
    private com.mirth.connect.client.ui.components.MirthTextField summaryNameField;
    private javax.swing.JLabel summaryNameLabel;
    private javax.swing.JLabel summaryPatternLabel1;
    private javax.swing.JLabel summaryRevision;
    public com.mirth.connect.client.ui.components.MirthCheckBox synchronousCheckBox;
    private com.mirth.connect.client.ui.components.MirthCheckBox transactionalCheckBox;
    // End of variables declaration//GEN-END:variables
}

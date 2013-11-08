/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellEditor;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellRenderer;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.editors.filter.FilterPane;
import com.mirth.connect.client.ui.editors.transformer.TransformerPane;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.util.VariableListUtil;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.channel.DispatcherConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;
import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;
import com.mirth.connect.donkey.model.channel.ResponseConnectorProperties;
import com.mirth.connect.donkey.model.channel.ResponseConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelProperties;
import com.mirth.connect.model.CodeTemplate.ContextType;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.MessageStorageMode;
import com.mirth.connect.model.Rule;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.model.attachments.AttachmentHandlerType;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.util.JavaScriptConstants;
import com.mirth.connect.util.PropertyVerifier;

/** The channel editor panel. Majority of the client application */
public class ChannelSetup extends javax.swing.JPanel {
    private static final String METADATA_NAME_COLUMN_NAME = "Column Name";
    private static final String METADATA_TYPE_COLUMN_NAME = "Type";
    private static final String METADATA_MAPPING_COLUMN_NAME = "Variable Mapping";
    private static final String DESTINATION_DEFAULT = "Channel Writer";
    private static final String SOURCE_DEFAULT = "Channel Reader";
    private static final String DATABASE_READER = "Database Reader";
    private static final String STATUS_COLUMN_NAME = "Status";
    private static final String METADATA_COLUMN_NAME = "Id";
    private static final String DESTINATION_COLUMN_NAME = "Destination";
    private static final String CONNECTOR_TYPE_COLUMN_NAME = "Connector Type";
    private static final String DESTINATION_CHAIN_COLUMN_NAME = "Chain";
    private static final int SOURCE_TAB_INDEX = 1;
    private static final int DESTINATIONS_TAB_INDEX = 2;

    public Channel currentChannel;
    public int lastModelIndex = -1;
    public TransformerPane transformerPane = new TransformerPane();
    public FilterPane filterPane = new FilterPane();

    private Frame parent;
    private boolean isDeleting = false;
    private boolean loadingChannel = false;
    private boolean channelValidationFailed = false;

    /**
     * Creates the Channel Editor panel. Calls initComponents() and sets up the model, dropdowns,
     * and mouse listeners.
     */
    public ChannelSetup() {
        this.parent = PlatformUI.MIRTH_FRAME;

        initComponents();
        initChannelTagsUI();
        initMetaDataTable();

        sourceConnectorPanel.setChannelSetup(this);
        destinationConnectorPanel.setChannelSetup(this);

        attachmentComboBox.setModel(new DefaultComboBoxModel(AttachmentHandlerType.values()));
        metadataPruningDaysTextField.setDocument(new MirthFieldConstraints(3, false, false, true));
        contentPruningDaysTextField.setDocument(new MirthFieldConstraints(3, false, false, true));
        summaryNameField.setDocument(new MirthFieldConstraints(40, false, true, true));

        channelView.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                showChannelEditPopupMenu(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                showChannelEditPopupMenu(evt);
            }
        });

        channelView.setMaximumSize(new Dimension(450, 3000));
    }

    private void updateTagTable() {
        DefaultTableModel model = (DefaultTableModel) tagTable.getModel();
        model.setRowCount(0);

        for (String tag : currentChannel.getProperties().getTags()) {
            model.addRow(new Object[] { tag });
        }
    }

    private void saveChannelTags() {
        currentChannel.getProperties().getTags().clear();

        DefaultTableModel model = (DefaultTableModel) tagTable.getModel();
        int rowCount = model.getRowCount();

        for (int i = 0; i < rowCount; i++) {
            currentChannel.getProperties().getTags().add((String) model.getValueAt(i, 0));
        }
    }

    private void updateMetaDataTable() {
        DefaultTableModel model = (DefaultTableModel) metaDataTable.getModel();
        model.setNumRows(0);

        for (MetaDataColumn column : currentChannel.getProperties().getMetaDataColumns()) {
            model.addRow(new Object[] { column.getName(), column.getType(), column.getMappingName() });
        }

        revertMetaDataButton.setEnabled(false);
    }

    public void saveMetaDataColumns() {
        DefaultTableModel model = (DefaultTableModel) metaDataTable.getModel();

        List<MetaDataColumn> metaDataColumns = currentChannel.getProperties().getMetaDataColumns();
        metaDataColumns.clear();

        for (int i = 0; i < model.getRowCount(); i++) {
            MetaDataColumn column = new MetaDataColumn();
            column.setName((String) model.getValueAt(i, 0));
            column.setType((MetaDataColumnType) model.getValueAt(i, 1));
            column.setMappingName((String) model.getValueAt(i, 2));

            metaDataColumns.add(column);
        }
    }

    private int getSelectedRow(MirthTable table) {
        return table.isEditing() ? table.getEditingRow() : table.getSelectedRow();
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been pushed.
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
     * Is called to load the transformer pane on either the source or destination
     */
    public String editTransformer() {
        String name = "";
        boolean changed = parent.changesHaveBeenMade();
        boolean transformerPaneLoaded = false;

        if (channelView.getSelectedIndex() == SOURCE_TAB_INDEX) {
            name = "Source";
            transformerPaneLoaded = transformerPane.load(currentChannel.getSourceConnector(), currentChannel.getSourceConnector().getTransformer(), changed, false);
        } else if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX) {
            Connector destination = currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex());
            transformerPaneLoaded = transformerPane.load(destination, destination.getTransformer(), changed, false);
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

    /**
     * Is called to load the response transformer pane on the destination
     */
    public String editResponseTransformer() {
        String name = "";
        boolean changed = parent.changesHaveBeenMade();
        boolean responseTransformerPaneLoaded = false;

        if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX) {
            Connector destination = currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex());
            responseTransformerPaneLoaded = transformerPane.load(destination, destination.getResponseTransformer(), changed, true);
            name = destination.getName();
        }

        if (!responseTransformerPaneLoaded) {
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
     * Makes the destination table with a parameter that is true if a new destination should be
     * added as well.
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

        int chain = 1;

        tableData = new Object[tableSize][5];

        for (int i = 0; i < tableSize; i++) {
            if (tableSize - 1 == i && addNew) {
                Connector connector = makeNewConnector(true);

                // Set the default inbound and outbound dataType and properties
                String dataType = currentChannel.getSourceConnector().getTransformer().getOutboundDataType();
                // Use a different properties object for the inbound and outbound
                DataTypeProperties defaultInboundProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties();
                DataTypeProperties defaultOutboundProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties();
                DataTypeProperties defaultResponseInboundProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties();
                DataTypeProperties defaultResponseOutboundProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties();

                connector.getTransformer().setInboundDataType(dataType);
                connector.getTransformer().setInboundProperties(defaultInboundProperties);
                connector.getTransformer().setOutboundDataType(dataType);
                connector.getTransformer().setOutboundProperties(defaultOutboundProperties);
                connector.getResponseTransformer().setInboundDataType(dataType);
                connector.getResponseTransformer().setInboundProperties(defaultResponseInboundProperties);
                connector.getResponseTransformer().setOutboundDataType(dataType);
                connector.getResponseTransformer().setOutboundProperties(defaultResponseOutboundProperties);

                connector.setName(getNewDestinationName(tableSize));
                connector.setTransportName(DESTINATION_DEFAULT);

                // We need to add the destination first so that the metadata ID is initialized.
                currentChannel.addDestination(connector);

                if (connector.isEnabled()) {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_blue.png")), UIConstants.ENABLED_STATUS);
                } else {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), UIConstants.DISABLED_STATUS);
                }
                tableData[i][1] = connector.getName();
                tableData[i][2] = connector.getMetaDataId();
                tableData[i][3] = connector.getTransportName();

                if (i > 0 && !connector.isWaitForPrevious()) {
                    chain++;
                }

                tableData[i][4] = chain;
            } else {

                if (destinationConnectors.get(i).isEnabled()) {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_blue.png")), UIConstants.ENABLED_STATUS);
                } else {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), UIConstants.DISABLED_STATUS);
                }
                tableData[i][1] = destinationConnectors.get(i).getName();
                tableData[i][2] = destinationConnectors.get(i).getMetaDataId();
                tableData[i][3] = destinationConnectors.get(i).getTransportName();

                if (i > 0 && !destinationConnectors.get(i).isWaitForPrevious()) {
                    chain++;
                }

                tableData[i][4] = chain;
            }
        }

        destinationTable = new MirthTable();

        destinationTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[] {
                STATUS_COLUMN_NAME, DESTINATION_COLUMN_NAME, METADATA_COLUMN_NAME,
                CONNECTOR_TYPE_COLUMN_NAME, DESTINATION_CHAIN_COLUMN_NAME }) {

            boolean[] canEdit = new boolean[] { false, true, false, false, false };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        destinationTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // Set the custom cell editor for the Destination Name column.
        destinationTable.getColumnModel().getColumn(destinationTable.getColumnModel().getColumnIndex(DESTINATION_COLUMN_NAME)).setCellEditor(new DestinationTableCellEditor());
        destinationTable.setCustomEditorControls(true);

        // Must set the maximum width on columns that should be packed.
        destinationTable.getColumnExt(STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        destinationTable.getColumnExt(STATUS_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);

        // Set the cell renderer for the status column.
        destinationTable.getColumnExt(STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());

        // Set the maximum width and cell renderer for the metadata ID column
        destinationTable.getColumnExt(METADATA_COLUMN_NAME).setMaxWidth(UIConstants.METADATA_ID_COLUMN_WIDTH);
        destinationTable.getColumnExt(METADATA_COLUMN_NAME).setMinWidth(UIConstants.METADATA_ID_COLUMN_WIDTH);
        destinationTable.getColumnExt(METADATA_COLUMN_NAME).setCellRenderer(new NumberCellRenderer(SwingConstants.CENTER, false));

        // Set the cell renderer and the max width for the destination chain column
        destinationTable.getColumnExt(DESTINATION_CHAIN_COLUMN_NAME).setCellRenderer(new NumberCellRenderer(SwingConstants.CENTER, false));
        destinationTable.getColumnExt(DESTINATION_CHAIN_COLUMN_NAME).setMaxWidth(50);

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
                        destinationConnector.setProperties(destinationConnectorPanel.getProperties());
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

                    /*
                     * Loading the connector may have updated the current destination with incorrect
                     * properties, so after updating lastModelIndex we need to update the
                     * destination panel again.
                     */
                    saveDestinationPanel();
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

        // Key Listener trigger for DEL
        destinationTable.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    parent.doDeleteDestination();
                }
            }

            public void keyReleased(KeyEvent e) {}

            public void keyTyped(KeyEvent e) {}
        });

        destinationTable.addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
                destinationTablePane.getMouseWheelListeners()[0].mouseWheelMoved(e);
            }
        });
    }

    /**
     * Get the name that should be used for a new destination so that it is unique.
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

//        PropertyVerifier.checkConnectorProperties(currentChannel, parent.getConnectorMetaData());

        sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(LoadedExtensions.getInstance().getSourceConnectors().keySet().toArray()));
        destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(LoadedExtensions.getInstance().getDestinationConnectors().keySet().toArray()));

        loadChannelInfo();
        makeDestinationTable(false);
        saveDestinationPanel();
        updateMetaDataTable();
        updateTagTable();
        setDestinationVariableList();
        loadingChannel = false;
        channelView.setSelectedIndex(0);

        sourceConnectorPanel.updateQueueWarning(currentChannel.getProperties().getMessageStorageMode());
        destinationConnectorPanel.updateQueueWarning(currentChannel.getProperties().getMessageStorageMode());

        parent.retrieveAllChannelTags();
    }

    /**
     * Adds a new channel that is passed in and then sets the overall panel to edit that channel.
     */
    public void addChannel(Channel channel) {
        loadingChannel = true;
        lastModelIndex = -1;
        currentChannel = channel;

        sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(LoadedExtensions.getInstance().getSourceConnectors().keySet().toArray()));
        destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(LoadedExtensions.getInstance().getDestinationConnectors().keySet().toArray()));

        Connector sourceConnector = makeNewConnector(false);
        sourceConnector.setName("sourceConnector");
        sourceConnector.setTransportName(SOURCE_DEFAULT);
        Transformer sourceTransformer = new Transformer();

        // Set the default inbound and outbound dataType and properties
        String defaultDataType = UIConstants.DATATYPE_DEFAULT;
        // If the default data type is not loaded, use the first data type that is.
        if (!LoadedExtensions.getInstance().getDataTypePlugins().containsKey(defaultDataType) && LoadedExtensions.getInstance().getDataTypePlugins().size() > 0) {
            defaultDataType = LoadedExtensions.getInstance().getDataTypePlugins().keySet().iterator().next();
        }

        // Use a different properties object for the inbound and outbound
        DataTypeProperties defaultInboundProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(defaultDataType).getDefaultProperties();
        DataTypeProperties defaultOutboundProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(defaultDataType).getDefaultProperties();

        sourceTransformer.setInboundDataType(defaultDataType);
        sourceTransformer.setInboundProperties(defaultInboundProperties);
        sourceTransformer.setOutboundDataType(defaultDataType);
        sourceTransformer.setOutboundProperties(defaultOutboundProperties);

        sourceConnector.setTransformer(sourceTransformer);

        currentChannel.setSourceConnector(sourceConnector);
        setLastModified();
        loadChannelInfo();
        makeDestinationTable(true);

        try {
            currentChannel.getProperties().setMetaDataColumns(parent.mirthClient.getServerSettings().getDefaultMetaDataColumns());
        } catch (ClientException e) {
            parent.alertException(parent, e.getStackTrace(), "Error loading default metadata columns: " + e.getMessage());
        }
        updateMetaDataTable();

        setDestinationVariableList();
        loadingChannel = false;
        channelView.setSelectedIndex(0);
        summaryNameField.requestFocus();
        parent.setSaveEnabled(true);
        parent.retrieveAllChannelTags();

        DefaultTableModel model = (DefaultTableModel) tagTable.getModel();
        model.setRowCount(0);
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
        boolean enabled = parent.isSaveEnabled();
        ChannelProperties properties = currentChannel.getProperties();
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
            scriptMap.put(ScriptPanel.PREPROCESSOR_SCRIPT, JavaScriptConstants.DEFAULT_CHANNEL_PREPROCESSOR_SCRIPT);
        }

        if (currentChannel.getDeployScript() != null) {
            scriptMap.put(ScriptPanel.DEPLOY_SCRIPT, currentChannel.getDeployScript());
        } else {
            scriptMap.put(ScriptPanel.DEPLOY_SCRIPT, JavaScriptConstants.DEFAULT_CHANNEL_DEPLOY_SCRIPT);
        }

        if (currentChannel.getShutdownScript() != null) {
            scriptMap.put(ScriptPanel.SHUTDOWN_SCRIPT, currentChannel.getShutdownScript());
        } else {
            scriptMap.put(ScriptPanel.SHUTDOWN_SCRIPT, JavaScriptConstants.DEFAULT_CHANNEL_SHUTDOWN_SCRIPT);
        }

        if (currentChannel.getPostprocessingScript() != null) {
            scriptMap.put(ScriptPanel.POSTPROCESSOR_SCRIPT, currentChannel.getPostprocessingScript());
        } else {
            scriptMap.put(ScriptPanel.POSTPROCESSOR_SCRIPT, JavaScriptConstants.DEFAULT_CHANNEL_POSTPROCESSOR_SCRIPT);
        }

        scripts.setScripts(scriptMap);

//        PropertyVerifier.checkChannelProperties(currentChannel);

        attachmentComboBox.setSelectedItem(AttachmentHandlerType.fromString(properties.getAttachmentProperties().getType()));

        clearGlobalChannelMapCheckBox.setSelected(properties.isClearGlobalChannelMap());
        encryptMessagesCheckBox.setSelected(properties.isEncryptData());

        // Fix dataTypes and properties not set by previous versions of Mirth Connect
        fixNullDataTypesAndProperties();

        // load message storage settings
        messageStorageSlider.setValue(properties.getMessageStorageMode().getValue());
        encryptMessagesCheckBox.setSelected(properties.isEncryptData());
        removeContentCheckbox.setSelected(properties.isRemoveContentOnCompletion());
        removeAttachmentsCheckbox.setSelected(properties.isRemoveAttachmentsOnCompletion());
        updateStorageMode();

        // load pruning settings
        Integer pruneMetaDataDays = properties.getPruneMetaDataDays();
        Integer pruneContentDays = properties.getPruneContentDays();

        if (pruneMetaDataDays == null) {
            metadataPruningDaysTextField.setText("");
            metadataPruningOffRadio.setSelected(true);
            metadataPruningOffRadioActionPerformed(null);
        } else {
            metadataPruningDaysTextField.setText(pruneMetaDataDays.toString());
            metadataPruningOnRadio.setSelected(true);
            metadataPruningOnRadioActionPerformed(null);
        }

        if (pruneContentDays == null) {
            contentPruningMetadataRadio.setSelected(true);
            contentPruningDaysTextField.setText("");
            contentPruningMetadataRadioActionPerformed(null);
        } else {
            contentPruningDaysRadio.setSelected(true);
            contentPruningDaysTextField.setText(pruneContentDays.toString());
            contentPruningDaysRadioActionPerformed(null);
        }

        archiveCheckBox.setSelected(properties.isArchiveEnabled());

        sourceSourceDropdown.setSelectedItem(currentChannel.getSourceConnector().getTransportName());

        initialState.setSelectedItem(currentChannel.getProperties().getInitialState());

        attachmentStoreCheckBox.setSelected(currentChannel.getProperties().isStoreAttachments());

        parent.setSaveEnabled(enabled);
    }

    private void updateStorageMode() {
        MessageStorageMode messageStorageMode = MessageStorageMode.fromInt(messageStorageSlider.getValue());

        switch (messageStorageMode) {
            case DEVELOPMENT:
                storageModeLabel.setText("Development");
                contentLabel.setText("Content: All");
                metadataLabel.setText("Metadata: All");
                durableStatusLabel.setText("On");
                durableStatusLabel.setForeground(new Color(0, 130, 0));
                messageStorageProgressBar.setValue(20);
                encryptMessagesCheckBox.setEnabled(true);
                removeContentCheckbox.setEnabled(true);
                removeAttachmentsCheckbox.setEnabled(true);
                break;

            case PRODUCTION:
                storageModeLabel.setText("Production");
                contentLabel.setText("Content: Raw, Encoded, Sent, Response, Maps");
                metadataLabel.setText("Metadata: All");
                durableStatusLabel.setText("On");
                durableStatusLabel.setForeground(new Color(0, 130, 0));
                messageStorageProgressBar.setValue(25);
                encryptMessagesCheckBox.setEnabled(true);
                removeContentCheckbox.setEnabled(true);
                removeAttachmentsCheckbox.setEnabled(true);
                break;

            case RAW:
                storageModeLabel.setText("Raw");
                contentLabel.setText("Content: Raw");
                metadataLabel.setText("Metadata: All");
                durableStatusLabel.setText("Reprocess only");
                durableStatusLabel.setForeground(new Color(255, 102, 0));
                messageStorageProgressBar.setValue(60);
                encryptMessagesCheckBox.setEnabled(true);
                removeContentCheckbox.setEnabled(true);
                removeAttachmentsCheckbox.setEnabled(true);
                break;

            case METADATA:
                storageModeLabel.setText("Metadata");
                contentLabel.setText("Content: None");
                metadataLabel.setText("Metadata: All");
                durableStatusLabel.setText("Off");
                durableStatusLabel.setForeground(new Color(130, 0, 0));
                messageStorageProgressBar.setValue(65);
                encryptMessagesCheckBox.setEnabled(false);
                removeContentCheckbox.setEnabled(false);
                removeAttachmentsCheckbox.setEnabled(false);
                break;

            case DISABLED:
                storageModeLabel.setText("Disabled");
                contentLabel.setText("Content: None");
                metadataLabel.setText("Metadata: None");
                durableStatusLabel.setText("Off");
                durableStatusLabel.setForeground(new Color(130, 0, 0));
                messageStorageProgressBar.setValue(100);
                encryptMessagesCheckBox.setEnabled(false);
                removeContentCheckbox.setEnabled(false);
                removeAttachmentsCheckbox.setEnabled(false);
                break;
        }

        // if content encryption is enabled, subtract a percentage from the progress bar
        if (encryptMessagesCheckBox.isEnabled() && encryptMessagesCheckBox.isSelected()) {
            messageStorageProgressBar.setValue(messageStorageProgressBar.getValue() - 3);
        }

        // if the "remove content on completion" option is enabled, subtract a percentage from the progress bar
        if (removeContentCheckbox.isEnabled() && removeContentCheckbox.isSelected()) {
            messageStorageProgressBar.setValue(messageStorageProgressBar.getValue() - 3);
        }

        // if the "remove content on completion" option is enabled, subtract a percentage from the progress bar
        if (removeAttachmentsCheckbox.isEnabled() && removeAttachmentsCheckbox.isSelected()) {
            messageStorageProgressBar.setValue(messageStorageProgressBar.getValue() - 3);
        }

        updateQueueWarning(messageStorageMode);
        sourceConnectorPanel.updateQueueWarning(messageStorageMode);
        destinationConnectorPanel.updateQueueWarning(messageStorageMode);
    }

    public MessageStorageMode getMessageStorageMode() {
        return MessageStorageMode.fromInt(messageStorageSlider.getValue());
    }

    public void updateQueueWarning(MessageStorageMode messageStorageMode) {
        String errorString = getQueueErrorString(messageStorageMode);

        if (errorString != null) {
            queueWarningLabel.setText("<html>Disable " + errorString + " queueing before using this mode</html>");
        } else {
            queueWarningLabel.setText("");
        }
    }

    private String getQueueErrorString(MessageStorageMode messageStorageMode) {
        StringBuilder stringBuilder = new StringBuilder();

        boolean sourceQueueEnabled = false;
        ConnectorProperties sourceConnectorProperties = sourceConnectorPanel.getProperties();

        if (sourceConnectorProperties != null && sourceConnectorProperties instanceof ResponseConnectorPropertiesInterface) {
            ResponseConnectorProperties responseConnectorProperties = ((ResponseConnectorPropertiesInterface) sourceConnectorProperties).getResponseConnectorProperties();
            sourceQueueEnabled = !responseConnectorProperties.isRespondAfterProcessing();
        }

        boolean destinationQueueEnabled = false;

        for (Connector connector : currentChannel.getDestinationConnectors()) {
            ConnectorProperties destinationConnectorProperties = connector.getProperties();

            if (destinationConnectorProperties instanceof DispatcherConnectorPropertiesInterface) {
                QueueConnectorProperties queueConnectorProperties = ((DispatcherConnectorPropertiesInterface) destinationConnectorProperties).getQueueConnectorProperties();

                if (queueConnectorProperties.isQueueEnabled()) {
                    destinationQueueEnabled = true;
                    break;
                }
            }
        }

        switch (messageStorageMode) {
            case METADATA:
            case DISABLED:
                if (sourceQueueEnabled) {
                    stringBuilder.append("source");

                    if (destinationQueueEnabled) {
                        stringBuilder.append(" & ");
                    }
                }

            case RAW:
                if (destinationQueueEnabled) {
                    stringBuilder.append("destination");
                }

                break;
        }

        return (stringBuilder.length() > 0) ? stringBuilder.toString() : null;
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

    public void saveSourcePanel() {
        currentChannel.getSourceConnector().setProperties(sourceConnectorPanel.getProperties());
    }

    public void saveDestinationPanel() {
        Connector temp;

        temp = currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex());
        temp.setProperties(destinationConnectorPanel.getProperties());
    }

    /**
     * Save all of the current channel information in the editor to the actual channel
     */
    public boolean saveChanges() {
        if (!parent.checkChannelName(summaryNameField.getText(), currentChannel.getId())) {
            return false;
        }

        if (metadataPruningOnRadio.isSelected() && metadataPruningDaysTextField.getText().equals("")) {
            parent.alertWarning(parent, "If metadata pruning is enabled, the age of metadata to prune cannot be blank.");
            return false;
        }

        if (contentPruningDaysRadio.isSelected() && contentPruningDaysTextField.getText().equals("")) {
            parent.alertWarning(parent, "If content pruning is enabled, the age of content to prune cannot be blank.");
            return false;
        }

        if (metadataPruningOnRadio.isSelected() && contentPruningDaysRadio.isSelected()) {
            Integer metadataPruningDays = Integer.parseInt(metadataPruningDaysTextField.getText());
            Integer contentPruningDays = Integer.parseInt(contentPruningDaysTextField.getText());

            if (contentPruningDays > metadataPruningDays) {
                parent.alertWarning(parent, "The age of content to prune cannot be greater than the age of metadata to prune.");
                return false;
            }
        }

        // Store the current metadata column data in a map with the column name as the key and the type as the value.
        Map<String, MetaDataColumnType> currentColumns = new HashMap<String, MetaDataColumnType>();
        for (MetaDataColumn column : currentChannel.getProperties().getMetaDataColumns()) {
            currentColumns.put(column.getName(), column.getType());
        }

        Set<String> columnNames = new HashSet<String>();
        for (int i = 0; i < metaDataTable.getRowCount(); i++) {
            DefaultTableModel model = (DefaultTableModel) metaDataTable.getModel();

            // Do not allow metadata column names to be empty
            String columnName = (String) model.getValueAt(i, model.findColumn(METADATA_NAME_COLUMN_NAME));
            if (StringUtils.isEmpty(columnName)) {
                parent.alertWarning(parent, "Empty column name detected in custom metadata table. Column names cannot be empty.");
                return false;
            } else {
                // Do not allow duplicate column names
                if (columnNames.contains(columnName)) {
                    parent.alertWarning(parent, "Duplicate column name detected in custom metadata table. Column names must be unique.");
                    return false;
                }

                // Add the column name to a set so it can be checked for duplicates
                columnNames.add(columnName);
            }

            MetaDataColumnType columnType = (MetaDataColumnType) model.getValueAt(i, model.findColumn(METADATA_TYPE_COLUMN_NAME));

            // Remove columns from the map only if they have NOT been modified in a way such that their data will be deleted on deploy
            if (currentColumns.containsKey(columnName) && currentColumns.get(columnName).equals(columnType)) {
                currentColumns.remove(columnName);
            }
        }

        // Notify the user if an existing column was modified in a way such that it will be deleted on deploy
        if (!currentColumns.isEmpty()) {
            if (!parent.alertOption(parent, "Renaming, deleting, or changing the type of existing custom metadata columns\nwill delete all existing data " + "for that column. Are you sure you want to do this?")) {
                return false;
            }
        }

        boolean enabled = summaryEnabledCheckbox.isSelected();

        saveSourcePanel();

        if (parent.currentContentPage == transformerPane) {
            transformerPane.accept(false);
            transformerPane.modified = false; // TODO: Check this. Fix to prevent double save on confirmLeave
        }

        if (parent.currentContentPage == filterPane) {
            filterPane.accept(false);
            filterPane.modified = false; // TODO: Check this. Fix to prevent double save on confirmLeave
        }

        saveDestinationPanel();

        MessageStorageMode messageStorageMode = MessageStorageMode.fromInt(messageStorageSlider.getValue());
        String errorString = getQueueErrorString(messageStorageMode);

        if (errorString != null) {
            parent.alertWarning(parent, StringUtils.capitalize(errorString) + " queueing must be disabled first before using the selected message storage mode.");
            return false;
        }

        currentChannel.setName(summaryNameField.getText());
        currentChannel.setDescription(summaryDescriptionText.getText());

        updateScripts();
        setLastModified();

        currentChannel.getProperties().setClearGlobalChannelMap(clearGlobalChannelMapCheckBox.isSelected());
        currentChannel.getProperties().setEncryptData(encryptMessagesCheckBox.isSelected());
        currentChannel.getProperties().setInitialState((DeployedState) initialState.getSelectedItem());
        currentChannel.getProperties().setStoreAttachments(attachmentStoreCheckBox.isSelected());

        String validationMessage = checkAllForms(currentChannel);
        if (validationMessage != null) {
            enabled = false;

            // If there is an error on one of the forms, then run the
            // validation on the current form to display any errors.
            if (channelView.getSelectedComponent() == destination) {
                // If the destination is enabled...
                if (currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex()).isEnabled()) {
                    destinationConnectorPanel.checkProperties(destinationConnectorPanel.getProperties(), true);
                }
            } else if (channelView.getSelectedComponent() == source) {
                sourceConnectorPanel.checkProperties(sourceConnectorPanel.getProperties(), true);
            }

            summaryEnabledCheckbox.setSelected(false);

            parent.alertCustomError(this.parent, validationMessage, CustomErrorDialog.ERROR_SAVING_CHANNEL);
        }

        // Set the channel to enabled or disabled after it has been validated
        currentChannel.setEnabled(enabled);

        saveChannelTags();
        saveMetaDataColumns();
        saveMessageStorage(messageStorageMode);
        saveMessagePruning();

        boolean updated = false;

        try {
            if (!parent.channels.containsKey(currentChannel.getId())) {
                currentChannel.setId(parent.mirthClient.getGuid());
            }

            // Will throw exception if the connection died or there was an exception
            // saving the channel, skipping the rest of this code.
            updated = parent.updateChannel(currentChannel, false);

            try {
                currentChannel = (Channel) SerializationUtils.clone(parent.channels.get(currentChannel.getId()));

                if (parent.currentContentPage == transformerPane) {
                    if (channelView.getSelectedIndex() == SOURCE_TAB_INDEX) {
                        transformerPane.reload(currentChannel.getSourceConnector());
                    } else if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX) {
                        int destination = destinationTable.getSelectedModelIndex();
                        transformerPane.reload(currentChannel.getDestinationConnectors().get(destination));
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
            } catch (SerializationException e) {
                parent.alertException(this.parent, e.getStackTrace(), e.getMessage());
            }
        } catch (ClientException e) {
            parent.alertException(this.parent, e.getStackTrace(), e.getMessage());
        }

        sourceConnectorPanel.updateQueueWarning(currentChannel.getProperties().getMessageStorageMode());
        destinationConnectorPanel.updateQueueWarning(currentChannel.getProperties().getMessageStorageMode());

        return updated;
    }

    private void saveMessageStorage(MessageStorageMode messageStorageMode) {
        ChannelProperties properties = currentChannel.getProperties();
        properties.setMessageStorageMode(messageStorageMode);
        properties.setEncryptData(encryptMessagesCheckBox.isSelected());
        properties.setRemoveContentOnCompletion(removeContentCheckbox.isSelected());
        properties.setRemoveAttachmentsOnCompletion(removeAttachmentsCheckbox.isSelected());
    }

    private void saveMessagePruning() {
        ChannelProperties properties = currentChannel.getProperties();

        if (metadataPruningOffRadio.isSelected()) {
            properties.setPruneMetaDataDays(null);
        } else {
            properties.setPruneMetaDataDays(Integer.parseInt(metadataPruningDaysTextField.getText()));
        }

        if (contentPruningMetadataRadio.isSelected()) {
            properties.setPruneContentDays(null);
        } else {
            properties.setPruneContentDays(Integer.parseInt(contentPruningDaysTextField.getText()));
        }

        properties.setArchiveEnabled(archiveCheckBox.isSelected());
    }

    /**
     * Set all the dataTypes and properties to proper values if they are null. This is only
     * necessary for channels from before version 2.0
     */
    public void fixNullDataTypesAndProperties() {
        Transformer sourceTransformer = currentChannel.getSourceConnector().getTransformer();

        String defaultDataType = UIConstants.DATATYPE_DEFAULT;
        // If the default data type is not loaded, use the first data type that is.
        if (!LoadedExtensions.getInstance().getDataTypePlugins().containsKey(defaultDataType) && LoadedExtensions.getInstance().getDataTypePlugins().size() > 0) {
            defaultDataType = LoadedExtensions.getInstance().getDataTypePlugins().keySet().iterator().next();
        }

        if (sourceTransformer.getInboundDataType() == null) {
            sourceTransformer.setInboundDataType(defaultDataType);
        }

        if (sourceTransformer.getInboundProperties() == null) {
            DataTypeProperties defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(sourceTransformer.getInboundDataType()).getDefaultProperties();
            sourceTransformer.setInboundProperties(defaultProperties);
        }

        if (sourceTransformer.getOutboundDataType() == null) {
            sourceTransformer.setOutboundDataType(sourceTransformer.getInboundDataType());
        }

        if (sourceTransformer.getOutboundProperties() == null) {
            DataTypeProperties defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(sourceTransformer.getOutboundDataType()).getDefaultProperties();
            sourceTransformer.setOutboundProperties(defaultProperties);
        }

        for (Connector c : currentChannel.getDestinationConnectors()) {
            Transformer destinationTransformer = c.getTransformer();

            if (destinationTransformer.getInboundDataType() == null) {
                destinationTransformer.setInboundDataType(sourceTransformer.getOutboundDataType());
            }

            if (destinationTransformer.getInboundProperties() == null) {
                DataTypeProperties defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(destinationTransformer.getInboundDataType()).getDefaultProperties();
                destinationTransformer.setInboundProperties(defaultProperties);
            }

            if (destinationTransformer.getOutboundDataType() == null) {
                destinationTransformer.setOutboundDataType(destinationTransformer.getInboundDataType());
            }

            if (destinationTransformer.getOutboundProperties() == null) {
                DataTypeProperties defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(destinationTransformer.getOutboundDataType()).getDefaultProperties();
                destinationTransformer.setOutboundProperties(defaultProperties);
            }

            Transformer destinationResponseTransformer = c.getResponseTransformer();

            if (destinationResponseTransformer.getInboundDataType() == null) {
                destinationResponseTransformer.setInboundDataType(destinationTransformer.getOutboundDataType());
            }

            if (destinationResponseTransformer.getInboundProperties() == null) {
                DataTypeProperties defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(destinationResponseTransformer.getInboundDataType()).getDefaultProperties();
                destinationResponseTransformer.setInboundProperties(defaultProperties);
            }

            if (destinationResponseTransformer.getOutboundDataType() == null) {
                destinationResponseTransformer.setOutboundDataType(destinationResponseTransformer.getInboundDataType());
            }

            if (destinationResponseTransformer.getOutboundProperties() == null) {
                DataTypeProperties defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(destinationResponseTransformer.getOutboundDataType()).getDefaultProperties();
                destinationResponseTransformer.setOutboundProperties(defaultProperties);
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
            if (!parent.alertOption(this.parent, "You must save your channel before cloning.  Would you like to save your channel now?") || !saveChanges()) {
                return;
            }
        }
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();

        Connector destination = null;
        try {
            destination = (Connector) SerializationUtils.clone(destinationConnectors.get(destinationTable.getSelectedModelIndex()));
        } catch (SerializationException e) {
            parent.alertException(this.parent, e.getStackTrace(), e.getMessage());
            return;
        }

        destination.setName(getNewDestinationName(destinationConnectors.size() + 1));
        currentChannel.addDestination(destination);
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
            destinationConnectorPanel.checkProperties(destinationConnectorPanel.getProperties(), true);
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
            destinationConnectorPanel.checkProperties(destinationConnectorPanel.getProperties(), false);
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
     * destinations and enables or disables them. Also sets the number of filter/transformer steps
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
            parent.updateTransformerTaskName(destination.getTransformer().getSteps().size(), StringUtils.isNotBlank(destination.getTransformer().getOutboundTemplate()));
            parent.updateResponseTransformerTaskName(destination.getResponseTransformer().getSteps().size(), StringUtils.isNotBlank(destination.getResponseTransformer().getOutboundTemplate()));
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
        ConnectorSettingsPanel tempConnector = null;
        ConnectorProperties tempProps = null;

        // Check source connector
        tempConnector = LoadedExtensions.getInstance().getSourceConnectors().get(channel.getSourceConnector().getTransportName());
        tempProps = channel.getSourceConnector().getProperties();

        errors += validateFilterRules(channel.getSourceConnector());
        errors += validateTransformerSteps(channel.getSourceConnector());

        if (tempConnector != null) {
            String validationMessage = sourceConnectorPanel.doValidate(tempConnector, tempProps, false);
            if (validationMessage != null) {
                errors += validationMessage;
            }
        }

        // Check destination connector
        for (int i = 0; i < channel.getDestinationConnectors().size(); i++) {
            // Only check the destination connector if it is enabled.
            if (channel.getDestinationConnectors().get(i).isEnabled()) {
                tempConnector = LoadedExtensions.getInstance().getDestinationConnectors().get(channel.getDestinationConnectors().get(i).getTransportName());
                tempProps = channel.getDestinationConnectors().get(i).getProperties();

                errors += validateFilterRules(channel.getDestinationConnectors().get(i));
                errors += validateTransformerSteps(channel.getDestinationConnectors().get(i));

                if (tempConnector != null) {
                    String validationMessage = destinationConnectorPanel.doValidate(tempConnector, tempProps, false);
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
                errors += "Error in connector \"" + connector.getName() + "\" at transformer step " + step.getSequenceNumber() + " (\"" + step.getName() + "\"):\n" + validationMessage + "\n\n";
            }
        }

        if (connector.getMode() == Connector.Mode.DESTINATION) {
            for (Step step : connector.getResponseTransformer().getSteps()) {
                String validationMessage = this.transformerPane.validateStep(step);
                if (validationMessage != null) {
                    errors += "Error in connector \"" + connector.getName() + "\" at response transformer step " + step.getSequenceNumber() + " (\"" + step.getName() + "\"):\n" + validationMessage + "\n\n";
                }
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
            String validationMessage = sourceConnectorPanel.doValidate(sourceConnectorPanel.getProperties(), true);
            if (validationMessage != null) {
                parent.alertCustomError(this.parent, validationMessage, CustomErrorDialog.ERROR_VALIDATING_CONNECTOR);
            } else {
                parent.alertInformation(this.parent, "The connector was successfully validated.");
            }
        } else {
            String validationMessage = destinationConnectorPanel.doValidate(destinationConnectorPanel.getProperties(), true);
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

    public void showAttachmentPropertiesDialog(AttachmentHandlerType type) {
        AttachmentHandlerProperties attachmentHandlerProperties = currentChannel.getProperties().getAttachmentProperties();
        if (type.equals(AttachmentHandlerType.REGEX)) {
            new RegexAttachmentDialog(attachmentHandlerProperties);
        } else if (type.equals(AttachmentHandlerType.DICOM)) {

        } else if (type.equals(AttachmentHandlerType.JAVASCRIPT)) {
            new JavaScriptAttachmentDialog(attachmentHandlerProperties);
        } else if (type.equals(AttachmentHandlerType.CUSTOM)) {
            new CustomAttachmentDialog(attachmentHandlerProperties);
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        metadataPruningButtonGroup = new javax.swing.ButtonGroup();
        contentPruningButtonGroup = new javax.swing.ButtonGroup();
        buttonGroup1 = new javax.swing.ButtonGroup();
        channelView = new javax.swing.JTabbedPane();
        summary = new javax.swing.JPanel();
        channelPropertiesPanel = new javax.swing.JPanel();
        summaryNameLabel = new javax.swing.JLabel();
        summaryNameField = new com.mirth.connect.client.ui.components.MirthTextField();
        summaryPatternLabel1 = new javax.swing.JLabel();
        initialStateLabel = new javax.swing.JLabel();
        changeDataTypesButton = new javax.swing.JButton();
        initialState = new com.mirth.connect.client.ui.components.MirthComboBox();
        attachmentLabel = new javax.swing.JLabel();
        attachmentPropertiesButton = new javax.swing.JButton();
        attachmentComboBox = new com.mirth.connect.client.ui.components.MirthComboBox();
        summaryEnabledCheckbox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        clearGlobalChannelMapCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        summaryRevision = new javax.swing.JLabel();
        lastModified = new javax.swing.JLabel();
        attachmentWarningLabel = new javax.swing.JLabel();
        attachmentStoreCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        messageStoragePanel = new javax.swing.JPanel();
        storageModeLabel = new javax.swing.JLabel();
        contentLabel = new javax.swing.JLabel();
        metadataLabel = new javax.swing.JLabel();
        durableLabel = new javax.swing.JLabel();
        performanceLabel = new javax.swing.JLabel();
        messageStorageSlider = new javax.swing.JSlider();
        messageStorageProgressBar = new javax.swing.JProgressBar();
        encryptMessagesCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        durableStatusLabel = new javax.swing.JLabel();
        removeContentCheckbox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        removeAttachmentsCheckbox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        queueWarningLabel = new javax.swing.JLabel();
        messagePruningPanel = new javax.swing.JPanel();
        metadataPruningLabel = new javax.swing.JLabel();
        metadataPruningOffRadio = new javax.swing.JRadioButton();
        metadataPruningOnRadio = new javax.swing.JRadioButton();
        metadataPruningDaysTextField = new com.mirth.connect.client.ui.components.MirthTextField();
        metadataDaysLabel = new javax.swing.JLabel();
        contentPruningMetadataRadio = new javax.swing.JRadioButton();
        contentPruningLabel = new javax.swing.JLabel();
        contentPruningDaysRadio = new javax.swing.JRadioButton();
        contentPruningDaysTextField = new com.mirth.connect.client.ui.components.MirthTextField();
        contentDaysLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        archiveCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        channelTagsPanel = new javax.swing.JPanel();
        channelTagsScrollPane = new javax.swing.JScrollPane();
        tagTable = new com.mirth.connect.client.ui.components.MirthTable();
        addTagButton = new com.mirth.connect.client.ui.components.MirthButton();
        deleteTagButton = new com.mirth.connect.client.ui.components.MirthButton();
        customMetadataPanel = new javax.swing.JPanel();
        addMetaDataButton = new javax.swing.JButton();
        deleteMetaDataButton = new javax.swing.JButton();
        metaDataTablePane = new javax.swing.JScrollPane();
        metaDataTable = new com.mirth.connect.client.ui.components.MirthTable();
        revertMetaDataButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        summaryDescriptionScrollPane = new javax.swing.JScrollPane();
        summaryDescriptionText = new com.mirth.connect.client.ui.components.MirthTextPane();
        source = new javax.swing.JPanel();
        sourceSourceDropdown = new com.mirth.connect.client.ui.components.MirthComboBox();
        sourceSourceLabel = new javax.swing.JLabel();
        sourceConnectorPane = new javax.swing.JScrollPane();
        sourceConnectorPanel = new com.mirth.connect.client.ui.panels.connectors.ConnectorPanel();
        destination = new javax.swing.JPanel();
        destinationSourceDropdown = new com.mirth.connect.client.ui.components.MirthComboBox();
        destinationSourceLabel = new javax.swing.JLabel();
        destinationVariableList = new com.mirth.connect.client.ui.VariableList();
        destinationConnectorPane = new javax.swing.JScrollPane();
        destinationConnectorPanel = new com.mirth.connect.client.ui.panels.connectors.ConnectorPanel();
        destinationTablePane = new javax.swing.JScrollPane();
        destinationTable = new com.mirth.connect.client.ui.components.MirthTable();
        waitForPreviousCheckbox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        scripts = new ScriptPanel(ContextType.CHANNEL_CONTEXT.getContext());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        channelView.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        channelView.setFocusable(false);
        channelView.setPreferredSize(new java.awt.Dimension(0, 0));

        summary.setBackground(new java.awt.Color(255, 255, 255));
        summary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        summary.setFocusable(false);
        summary.setPreferredSize(new java.awt.Dimension(0, 0));
        summary.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                summaryComponentShown(evt);
            }
        });

        channelPropertiesPanel.setBackground(new java.awt.Color(255, 255, 255));
        channelPropertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Channel Properties"));

        summaryNameLabel.setText("Name:");

        summaryNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                summaryNameFieldKeyReleased(evt);
            }
        });

        summaryPatternLabel1.setText("Data Types:");

        initialStateLabel.setText("Initial State:");

        changeDataTypesButton.setText("Set Data Types");
        changeDataTypesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeDataTypesButtonActionPerformed(evt);
            }
        });

        initialState.setModel(new javax.swing.DefaultComboBoxModel(new Object[] { DeployedState.STARTED, DeployedState.PAUSED, DeployedState.STOPPED }));

        attachmentLabel.setText("Attachment:");

        attachmentPropertiesButton.setText("Properties");
        attachmentPropertiesButton.setEnabled(false);
        attachmentPropertiesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attachmentPropertiesButtonActionPerformed(evt);
            }
        });

        attachmentComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Regex", "DICOM", "Javascript" }));
        attachmentComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attachmentComboBoxActionPerformed(evt);
            }
        });

        summaryEnabledCheckbox.setBackground(new java.awt.Color(255, 255, 255));
        summaryEnabledCheckbox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        summaryEnabledCheckbox.setSelected(true);
        summaryEnabledCheckbox.setText("Enabled");
        summaryEnabledCheckbox.setToolTipText("Enable this channel so that it can be deployed.");
        summaryEnabledCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        clearGlobalChannelMapCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        clearGlobalChannelMapCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        clearGlobalChannelMapCheckBox.setText("Clear global channel map on deploy");
        clearGlobalChannelMapCheckBox.setToolTipText("Clear the global channel map on both single channel deploy and a full redeploy.");
        clearGlobalChannelMapCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        summaryRevision.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        summaryRevision.setText("Revision: ");

        lastModified.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lastModified.setText("Last Modified: ");

        attachmentWarningLabel.setForeground(new java.awt.Color(255, 0, 0));
        attachmentWarningLabel.setText("Attachments will be extracted but not stored or reattached.");

        attachmentStoreCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        attachmentStoreCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        attachmentStoreCheckBox.setText("Store Attachments");
        attachmentStoreCheckBox.setToolTipText("If checked, attachments will be stored in the database and available for reattachment.");
        attachmentStoreCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        attachmentStoreCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                attachmentStoreCheckBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout channelPropertiesPanelLayout = new javax.swing.GroupLayout(channelPropertiesPanel);
        channelPropertiesPanel.setLayout(channelPropertiesPanelLayout);
        channelPropertiesPanelLayout.setHorizontalGroup(
            channelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(channelPropertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(channelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(summaryNameLabel)
                    .addComponent(summaryPatternLabel1)
                    .addComponent(initialStateLabel)
                    .addComponent(attachmentLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(channelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(channelPropertiesPanelLayout.createSequentialGroup()
                        .addComponent(attachmentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(attachmentPropertiesButton))
                    .addGroup(channelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(changeDataTypesButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(initialState, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(summaryNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(channelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(channelPropertiesPanelLayout.createSequentialGroup()
                        .addComponent(summaryEnabledCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(195, 195, 195)
                        .addComponent(summaryRevision, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(channelPropertiesPanelLayout.createSequentialGroup()
                        .addComponent(attachmentStoreCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(attachmentWarningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(channelPropertiesPanelLayout.createSequentialGroup()
                        .addComponent(clearGlobalChannelMapCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34)
                        .addComponent(lastModified, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        channelPropertiesPanelLayout.setVerticalGroup(
            channelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(channelPropertiesPanelLayout.createSequentialGroup()
                .addGroup(channelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(summaryNameLabel)
                    .addComponent(summaryNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(summaryRevision)
                    .addComponent(summaryEnabledCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(channelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(channelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lastModified)
                        .addComponent(clearGlobalChannelMapCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(channelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(summaryPatternLabel1)
                        .addComponent(changeDataTypesButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(channelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(initialState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(initialStateLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(channelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(attachmentLabel)
                    .addComponent(attachmentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attachmentStoreCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attachmentPropertiesButton)
                    .addComponent(attachmentWarningLabel))
                .addContainerGap())
        );

        messageStoragePanel.setBackground(new java.awt.Color(255, 255, 255));
        messageStoragePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Message Storage"));
        messageStoragePanel.setForeground(new java.awt.Color(0, 102, 0));

        storageModeLabel.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        storageModeLabel.setText("Development");

        contentLabel.setText("Content: ");

        metadataLabel.setText("Metadata:");

        durableLabel.setText("Durable Message Delivery:");

        performanceLabel.setText("Performance:");

        messageStorageSlider.setBackground(new java.awt.Color(255, 255, 255));
        messageStorageSlider.setMajorTickSpacing(1);
        messageStorageSlider.setMaximum(5);
        messageStorageSlider.setMinimum(1);
        messageStorageSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        messageStorageSlider.setPaintTicks(true);
        messageStorageSlider.setSnapToTicks(true);
        messageStorageSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                messageStorageSliderStateChanged(evt);
            }
        });

        messageStorageProgressBar.setValue(10);

        encryptMessagesCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        encryptMessagesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        encryptMessagesCheckBox.setText("Encrypt message content");
        encryptMessagesCheckBox.setToolTipText("<html>Encrypt message content that is stored in the database. Messages that<br>are stored while this option is enabled will still be viewable in the<br>message browser, but the content will not be searchable.</html>");
        encryptMessagesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        encryptMessagesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                encryptMessagesCheckBoxActionPerformed(evt);
            }
        });

        durableStatusLabel.setForeground(new java.awt.Color(0, 102, 0));
        durableStatusLabel.setText("On");

        removeContentCheckbox.setBackground(new java.awt.Color(255, 255, 255));
        removeContentCheckbox.setText("Remove content on completion");
        removeContentCheckbox.setToolTipText("<html>Remove message content once the message has completed processing.<br/>Not applicable for messages that are errored or queued.</html>");
        removeContentCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeContentCheckboxActionPerformed(evt);
            }
        });

        removeAttachmentsCheckbox.setBackground(new java.awt.Color(255, 255, 255));
        removeAttachmentsCheckbox.setText("Remove attachments on completion");
        removeAttachmentsCheckbox.setToolTipText("<html>Remove message attachments once the message has completed processing.<br/>Not applicable for messages that are errored or queued.</html>");
        removeAttachmentsCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAttachmentsCheckboxActionPerformed(evt);
            }
        });

        queueWarningLabel.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        queueWarningLabel.setForeground(new java.awt.Color(255, 0, 0));
        queueWarningLabel.setText("<html>Disable source & destination queueing before using this mode</html>");
        queueWarningLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout messageStoragePanelLayout = new javax.swing.GroupLayout(messageStoragePanel);
        messageStoragePanel.setLayout(messageStoragePanelLayout);
        messageStoragePanelLayout.setHorizontalGroup(
            messageStoragePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(messageStoragePanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(messageStorageSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(messageStoragePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(messageStoragePanelLayout.createSequentialGroup()
                        .addComponent(performanceLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(messageStorageProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, messageStoragePanelLayout.createSequentialGroup()
                        .addComponent(durableLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(durableStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(removeContentCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(encryptMessagesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(storageModeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(contentLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(metadataLabel)
                    .addComponent(removeAttachmentsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(queueWarningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        messageStoragePanelLayout.setVerticalGroup(
            messageStoragePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(messageStoragePanelLayout.createSequentialGroup()
                .addComponent(storageModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contentLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(metadataLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(messageStoragePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(durableLabel)
                    .addComponent(durableStatusLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(messageStoragePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(messageStorageProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(performanceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(encryptMessagesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeContentCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeAttachmentsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(queueWarningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(messageStoragePanelLayout.createSequentialGroup()
                .addComponent(messageStorageSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );

        messagePruningPanel.setBackground(new java.awt.Color(255, 255, 255));
        messagePruningPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Message Pruning"));

        metadataPruningLabel.setText("Metadata:");

        metadataPruningOffRadio.setBackground(new java.awt.Color(255, 255, 255));
        metadataPruningButtonGroup.add(metadataPruningOffRadio);
        metadataPruningOffRadio.setSelected(true);
        metadataPruningOffRadio.setText("Store indefinitely");
        metadataPruningOffRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                metadataPruningOffRadioActionPerformed(evt);
            }
        });

        metadataPruningOnRadio.setBackground(new java.awt.Color(255, 255, 255));
        metadataPruningButtonGroup.add(metadataPruningOnRadio);
        metadataPruningOnRadio.setText("Prune metadata older than");
        metadataPruningOnRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                metadataPruningOnRadioActionPerformed(evt);
            }
        });

        metadataPruningDaysTextField.setEnabled(false);

        metadataDaysLabel.setText("days");

        contentPruningMetadataRadio.setBackground(new java.awt.Color(255, 255, 255));
        contentPruningButtonGroup.add(contentPruningMetadataRadio);
        contentPruningMetadataRadio.setSelected(true);
        contentPruningMetadataRadio.setText("Prune when message metadata is removed");
        contentPruningMetadataRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contentPruningMetadataRadioActionPerformed(evt);
            }
        });

        contentPruningLabel.setText("Content:");

        contentPruningDaysRadio.setBackground(new java.awt.Color(255, 255, 255));
        contentPruningButtonGroup.add(contentPruningDaysRadio);
        contentPruningDaysRadio.setText("Prune content older than");
        contentPruningDaysRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contentPruningDaysRadioActionPerformed(evt);
            }
        });

        contentPruningDaysTextField.setEnabled(false);

        contentDaysLabel.setText("days");

        jLabel1.setText("(incomplete, errored, and queued messages will not be pruned)");

        archiveCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        archiveCheckBox.setText("Allow message archiving");
        archiveCheckBox.setToolTipText("<html>If checked and the data pruner and archiver are enabled, messages<br />in this channel will be archived before being pruned.</html>");

        javax.swing.GroupLayout messagePruningPanelLayout = new javax.swing.GroupLayout(messagePruningPanel);
        messagePruningPanel.setLayout(messagePruningPanelLayout);
        messagePruningPanelLayout.setHorizontalGroup(
            messagePruningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(messagePruningPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(messagePruningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(metadataPruningLabel)
                    .addComponent(contentPruningLabel)
                    .addGroup(messagePruningPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(messagePruningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(messagePruningPanelLayout.createSequentialGroup()
                                .addComponent(metadataPruningOnRadio)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(metadataPruningDaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(metadataDaysLabel))
                            .addComponent(metadataPruningOffRadio)
                            .addComponent(contentPruningMetadataRadio)
                            .addGroup(messagePruningPanelLayout.createSequentialGroup()
                                .addComponent(contentPruningDaysRadio)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(contentPruningDaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(contentDaysLabel))))
                    .addComponent(jLabel1)
                    .addComponent(archiveCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(43, Short.MAX_VALUE))
        );
        messagePruningPanelLayout.setVerticalGroup(
            messagePruningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(messagePruningPanelLayout.createSequentialGroup()
                .addComponent(metadataPruningLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(metadataPruningOffRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(messagePruningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(metadataPruningOnRadio)
                    .addComponent(metadataPruningDaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(metadataDaysLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contentPruningLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contentPruningMetadataRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(messagePruningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contentPruningDaysRadio)
                    .addComponent(contentPruningDaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contentDaysLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(archiveCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1))
        );

        channelTagsPanel.setBackground(new java.awt.Color(255, 255, 255));
        channelTagsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Channel Tags"));

        tagTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tag"
            }
        ));
        tagTable.setEditable(false);
        channelTagsScrollPane.setViewportView(tagTable);

        addTagButton.setText("Add");
        addTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTagButtonActionPerformed(evt);
            }
        });

        deleteTagButton.setText("Delete");
        deleteTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteTagButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout channelTagsPanelLayout = new javax.swing.GroupLayout(channelTagsPanel);
        channelTagsPanel.setLayout(channelTagsPanelLayout);
        channelTagsPanelLayout.setHorizontalGroup(
            channelTagsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(channelTagsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(channelTagsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(channelTagsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        channelTagsPanelLayout.setVerticalGroup(
            channelTagsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(channelTagsPanelLayout.createSequentialGroup()
                .addComponent(addTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 65, Short.MAX_VALUE))
            .addGroup(channelTagsPanelLayout.createSequentialGroup()
                .addComponent(channelTagsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );

        customMetadataPanel.setBackground(new java.awt.Color(255, 255, 255));
        customMetadataPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Custom Metadata"));
        customMetadataPanel.setPreferredSize(new java.awt.Dimension(120, 146));

        addMetaDataButton.setText("Add");
        addMetaDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMetaDataButtonActionPerformed(evt);
            }
        });

        deleteMetaDataButton.setText("Delete");
        deleteMetaDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMetaDataButtonActionPerformed(evt);
            }
        });

        metaDataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        metaDataTablePane.setViewportView(metaDataTable);

        revertMetaDataButton.setText("Revert");
        revertMetaDataButton.setToolTipText("<html>Revert the custom metadata settings to the last save.<br>This option allows you to undo your metadata changes without affecting the rest of the channel.</html>");
        revertMetaDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                revertMetaDataButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout customMetadataPanelLayout = new javax.swing.GroupLayout(customMetadataPanel);
        customMetadataPanel.setLayout(customMetadataPanelLayout);
        customMetadataPanelLayout.setHorizontalGroup(
            customMetadataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customMetadataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(metaDataTablePane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(customMetadataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(addMetaDataButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(deleteMetaDataButton, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                    .addComponent(revertMetaDataButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        customMetadataPanelLayout.setVerticalGroup(
            customMetadataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, customMetadataPanelLayout.createSequentialGroup()
                .addGroup(customMetadataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(customMetadataPanelLayout.createSequentialGroup()
                        .addComponent(addMetaDataButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteMetaDataButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                        .addComponent(revertMetaDataButton))
                    .addComponent(metaDataTablePane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Channel Description"));

        summaryDescriptionScrollPane.setViewportView(summaryDescriptionText);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(summaryDescriptionScrollPane)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(summaryDescriptionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout summaryLayout = new javax.swing.GroupLayout(summary);
        summary.setLayout(summaryLayout);
        summaryLayout.setHorizontalGroup(
            summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(summaryLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(channelPropertiesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(summaryLayout.createSequentialGroup()
                        .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(messageStoragePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(channelTagsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(messagePruningPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(customMetadataPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE))))
                .addContainerGap())
        );
        summaryLayout.setVerticalGroup(
            summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(summaryLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(channelPropertiesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(messagePruningPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(messageStoragePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(channelTagsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(customMetadataPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

        sourceSourceDropdown.setMaximumRowCount(20);
        sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NHIN CONNECT Gateway Listener", "Web Service Listener", "Email" }));
        sourceSourceDropdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceSourceDropdownActionPerformed(evt);
            }
        });

        sourceSourceLabel.setText("Connector Type:");

        sourceConnectorPane.setBackground(new java.awt.Color(255, 255, 255));
        sourceConnectorPane.setBorder(null);
        sourceConnectorPane.setViewportView(sourceConnectorPanel);

        javax.swing.GroupLayout sourceLayout = new javax.swing.GroupLayout(source);
        source.setLayout(sourceLayout);
        sourceLayout.setHorizontalGroup(
            sourceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sourceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sourceConnectorPane, javax.swing.GroupLayout.DEFAULT_SIZE, 787, Short.MAX_VALUE)
                    .addGroup(sourceLayout.createSequentialGroup()
                        .addComponent(sourceSourceLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sourceSourceDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 483, Short.MAX_VALUE)))
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
                .addComponent(sourceConnectorPane, javax.swing.GroupLayout.DEFAULT_SIZE, 543, Short.MAX_VALUE)
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

        destinationSourceDropdown.setMaximumRowCount(20);
        destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NHIN CONNECT Gateway Listener", "Web Service Listener", "Email" }));
        destinationSourceDropdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                destinationSourceDropdownActionPerformed(evt);
            }
        });

        destinationSourceLabel.setText("Connector Type:");

        destinationConnectorPane.setBackground(new java.awt.Color(255, 255, 255));
        destinationConnectorPane.setBorder(null);
        destinationConnectorPane.setViewportView(destinationConnectorPanel);

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

        waitForPreviousCheckbox.setBackground(new java.awt.Color(255, 255, 255));
        waitForPreviousCheckbox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        waitForPreviousCheckbox.setSelected(true);
        waitForPreviousCheckbox.setText("Wait for previous destination");
        waitForPreviousCheckbox.setToolTipText("<html>Wait for the previous destination to finish before processing the current destination.<br/>Each destination connector for which this is not selected marks the beginning of a destination chain,<br/>such that all chains execute asynchronously, but each destination within a particular chain executes in order.<br/>This option has no effect on the first destination connector, which always marks the beginning of the first chain.</html>");
        waitForPreviousCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        waitForPreviousCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                waitForPreviousCheckboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout destinationLayout = new javax.swing.GroupLayout(destination);
        destination.setLayout(destinationLayout);
        destinationLayout.setHorizontalGroup(
            destinationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(destinationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(destinationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(destinationTablePane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 787, Short.MAX_VALUE)
                    .addGroup(destinationLayout.createSequentialGroup()
                        .addComponent(destinationConnectorPane, javax.swing.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(destinationVariableList, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, destinationLayout.createSequentialGroup()
                        .addComponent(destinationSourceLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(destinationSourceDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(waitForPreviousCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 308, Short.MAX_VALUE)))
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
                    .addComponent(destinationSourceDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(waitForPreviousCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(destinationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(destinationVariableList, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(destinationConnectorPane, javax.swing.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE))
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
            .addComponent(channelView, javax.swing.GroupLayout.DEFAULT_SIZE, 816, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(channelView, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 622, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void initChannelTagsUI() {
        tagTable.setSortable(false);
        tagTable.getTableHeader().setReorderingAllowed(false);

        tagTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                deleteTagButton.setEnabled(getSelectedRow(tagTable) != -1);
            }
        });

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            tagTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        deleteTagButton.setEnabled(false);

        DefaultTableModel model = new DefaultTableModel(new Object[][] {}, new String[] { "Tag" }) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }
        };

        tagTable.setModel(model);
    }

    private void initMetaDataTable() {
        metaDataTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        DefaultTableModel model = new DefaultTableModel(new Object[][] {}, new String[] {
                METADATA_NAME_COLUMN_NAME, METADATA_TYPE_COLUMN_NAME, METADATA_MAPPING_COLUMN_NAME }) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }

            @Override
            public void setValueAt(Object value, int row, int column) {
                // Enable the revert button if any data was changed.
                if (!value.equals(getValueAt(row, column))) {
                    revertMetaDataButton.setEnabled(true);
                }

                super.setValueAt(value, row, column);
            }
        };

        model.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    parent.setSaveEnabled(true);
                }
            }

        });

        class AlphaNumericCellEditor extends TextFieldCellEditor {

            public AlphaNumericCellEditor() {
                super();
                MirthFieldConstraints constraints = new MirthFieldConstraints("^[a-zA-Z_0-9]*$");
                constraints.setLimit(30);
                getTextField().setDocument(constraints);
            }

            @Override
            protected boolean valueChanged(String value) {
                return true;
            }

        }

        metaDataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        metaDataTable.setDragEnabled(false);
        metaDataTable.setSortable(false);
        metaDataTable.getTableHeader().setReorderingAllowed(false);
        metaDataTable.setModel(model);

        metaDataTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                deleteMetaDataButton.setEnabled(metaDataTable.getSelectedRow() != -1);
            }
        });

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            metaDataTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        metaDataTable.getColumnModel().getColumn(metaDataTable.getColumnModel().getColumnIndex(METADATA_NAME_COLUMN_NAME)).setCellEditor(new AlphaNumericCellEditor());
        metaDataTable.getColumnModel().getColumn(metaDataTable.getColumnModel().getColumnIndex(METADATA_MAPPING_COLUMN_NAME)).setCellEditor(new AlphaNumericCellEditor());

        TableColumn column = metaDataTable.getColumnModel().getColumn(metaDataTable.getColumnModel().getColumnIndex(METADATA_TYPE_COLUMN_NAME));
        column.setCellRenderer(new MirthComboBoxTableCellRenderer(MetaDataColumnType.values()));
        column.setCellEditor(new MirthComboBoxTableCellEditor(metaDataTable, MetaDataColumnType.values(), 1, false, null));
        column.setMinWidth(100);
        column.setMaxWidth(100);

        deleteMetaDataButton.setEnabled(false);
    }

    private void scriptsComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_scriptsComponentShown
    {//GEN-HEADEREND:event_scriptsComponentShown
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 13, false);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 15, 15, true);
    }//GEN-LAST:event_scriptsComponentShown

    /** Action when the source tab is shown. */
    private void sourceComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_sourceComponentShown
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 1, true);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 2, 8, false);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 9, 10, true);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 11, 11, false);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 12, 14, true);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 15, 15, false);

        // Update number of rules and steps on the filter and transformer
        parent.updateFilterTaskName(currentChannel.getSourceConnector().getFilter().getRules().size());
        parent.updateTransformerTaskName(currentChannel.getSourceConnector().getTransformer().getSteps().size(), StringUtils.isNotBlank(currentChannel.getSourceConnector().getTransformer().getOutboundTemplate()));

        int connectorIndex = destinationTable.getSelectedModelIndex();
        Connector destinationConnector = currentChannel.getDestinationConnectors().get(connectorIndex);
        destinationConnector.setProperties(destinationConnectorPanel.getProperties());
        updateScripts();

        sourceConnectorPanel.updateResponseDropDown();

        // If validation has failed, then highlight any errors on this form.
        if (channelValidationFailed) {
            sourceConnectorPanel.checkProperties(sourceConnectorPanel.getProperties(), true);
        }
    }//GEN-LAST:event_sourceComponentShown

    /** Action when the destinations tab is shown. */
    private void destinationComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_destinationComponentShown
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 1, true);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 2, 13, true);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 15, 15, false);

        checkVisibleDestinationTasks();

        // If validation has failed and this destination is enabled, then highlight any errors on this form.
        if (channelValidationFailed && currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex()).isEnabled()) {
            destinationConnectorPanel.checkProperties(destinationConnectorPanel.getProperties(), true);
        }
    }//GEN-LAST:event_destinationComponentShown

    /**
     * Action when an action is performed on the source connector type dropdown.
     */
    private void sourceSourceDropdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceSourceDropdownActionPerformed
        // If a channel is not being loaded then alert the user when necessary
        // that changing the connector type will lose all current connector data.
        if (!loadingChannel) {
            if (sourceConnectorPanel.getName() != null && sourceConnectorPanel.getName().equals(sourceSourceDropdown.getSelectedItem())) {
                return;
            }

            if (!PropertyVerifier.compareProps(sourceConnectorPanel.getProperties(), sourceConnectorPanel.getDefaults())) {
                boolean changeType = parent.alertOption(this.parent, "Are you sure you would like to change this connector type and lose all of the current connector data?");
                if (!changeType) {
                    sourceSourceDropdown.setSelectedItem(sourceConnectorPanel.getProperties().getName());
                    return;
                }
            }
        }

        if (currentChannel.getSourceConnector().getTransportName().equalsIgnoreCase(DATABASE_READER)) {
            currentChannel.getSourceConnector().getTransformer().setInboundTemplate("");

            if (parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundDataType() == UIConstants.DATATYPE_XML && parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundTemplate() != null && parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundTemplate().length() == 0) {
                List<Connector> list = parent.channelEditPanel.currentChannel.getDestinationConnectors();
                for (Connector c : list) {
                    c.getTransformer().setInboundTemplate("");
                }
            }
        }

        // Get the selected source connector and set it.
        sourceConnectorPanel.setConnectorSettingsPanel(LoadedExtensions.getInstance().getSourceConnectors().get((String) sourceSourceDropdown.getSelectedItem()));

        // Sets all of the properties, transformer, filter, etc. on the new
        // source connector.
        Connector sourceConnector = currentChannel.getSourceConnector();
        if (sourceConnector != null) {
            String connectorName = "";

            if (sourceConnector.getProperties() != null) {
                connectorName = sourceConnector.getProperties().getName();
            }

            if (sourceConnector.getProperties() == null || !connectorName.equals(sourceSourceDropdown.getSelectedItem())) {
                String name = sourceConnector.getName();
                changeConnectorType(sourceConnector, false);
                sourceConnector.setName(name);
                sourceConnectorPanel.setProperties(sourceConnectorPanel.getDefaults());
                sourceConnector.setProperties(sourceConnectorPanel.getProperties());
            }

            sourceConnector.setTransportName((String) sourceSourceDropdown.getSelectedItem());
            currentChannel.setSourceConnector(sourceConnector);
            sourceConnectorPanel.setProperties(sourceConnector.getProperties());
        }

        // Set the source data type to XML if necessary
        checkAndSetXmlDataType();

        sourceConnectorPane.repaint();

        // If validation has failed, then highlight any errors on this form.
        if (channelValidationFailed) {
            sourceConnectorPanel.checkProperties(sourceConnectorPanel.getProperties(), true);
        }
    }//GEN-LAST:event_sourceSourceDropdownActionPerformed

    /**
     * Action when an action is performed on the destination connector type dropdown. Fires off
     * either generateMultipleDestinationPage()
     */
    private void destinationSourceDropdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_destinationSourceDropdownActionPerformed
        // If a channel is not being loaded then alert the user when necessary
        // that changing the connector type will lose all current connector
        // data. Continue when deleting a destination because the selected
        // destination index will not be different than the last index.
        if (!loadingChannel && !isDeleting) {
            if (destinationConnectorPanel.getProperties().getName() != null && destinationConnectorPanel.getProperties().getName().equals(destinationSourceDropdown.getSelectedItem()) && lastModelIndex == destinationTable.getSelectedModelIndex()) {
                return;
            }

            // if the selected destination is still the same (same index and
            // not deleting) AND the connector properties have not been 
            // changed from defaults then ask if the user would really like 
            // to change the connector type.
            if (lastModelIndex == destinationTable.getSelectedModelIndex() && !PropertyVerifier.compareProps(destinationConnectorPanel.getProperties(), destinationConnectorPanel.getDefaults())) {
                boolean changeType = parent.alertOption(this.parent, "Are you sure you would like to change this connector type and lose all of the current connector data?");
                if (!changeType) {
                    destinationSourceDropdown.setSelectedItem(destinationConnectorPanel.getProperties().getName());
                    return;
                }
            }
        }
        generateMultipleDestinationPage();

        // If validation has failed and this destination is enabled, then highlight any errors on this form.
        if (channelValidationFailed && currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex()).isEnabled()) {
            destinationConnectorPanel.checkProperties(destinationConnectorPanel.getProperties(), true);
        }
    }//GEN-LAST:event_destinationSourceDropdownActionPerformed

    private void waitForPreviousCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_waitForPreviousCheckboxActionPerformed
        currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex()).setWaitForPrevious(waitForPreviousCheckbox.isSelected());

        TableModel model = destinationTable.getModel();
        int rowCount = model.getRowCount();
        int colNum = destinationTable.getColumnModelIndex(DESTINATION_CHAIN_COLUMN_NAME);
        boolean waitForPrevious = waitForPreviousCheckbox.isSelected();

        for (int i = destinationTable.getSelectedModelIndex(); i < rowCount; i++) {
            Integer chain = (Integer) model.getValueAt(i, colNum);
            chain += (waitForPrevious) ? -1 : 1;
            model.setValueAt(chain, i, colNum);
        }
    }//GEN-LAST:event_waitForPreviousCheckboxActionPerformed

    private void summaryComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_summaryComponentShown
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 13, false);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 15, 15, false);
    }//GEN-LAST:event_summaryComponentShown

    private void metadataPruningOffRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_metadataPruningOffRadioActionPerformed
        parent.setSaveEnabled(true);
        metadataPruningDaysTextField.setEnabled(false);

        if (contentPruningMetadataRadio.isSelected()) {
            archiveCheckBox.setEnabled(false);
        }
    }//GEN-LAST:event_metadataPruningOffRadioActionPerformed

    private void attachmentComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attachmentComboBoxActionPerformed
        AttachmentHandlerType type = (AttachmentHandlerType) attachmentComboBox.getSelectedItem();
        AttachmentHandlerType lastType = AttachmentHandlerType.fromString(currentChannel.getProperties().getAttachmentProperties().getType());

        if (lastType != AttachmentHandlerType.NONE && lastType != type && !lastType.getDefaultProperties().equals(currentChannel.getProperties().getAttachmentProperties())) {
            boolean changeType = parent.alertOption(this.parent, "Are you sure you would like to change this attachment handler type and lose all of the current handler data?");
            if (!changeType) {
                attachmentComboBox.setSelectedItem(lastType);
                attachmentPropertiesButton.setEnabled((lastType != AttachmentHandlerType.NONE && lastType != AttachmentHandlerType.DICOM));
                return;
            }
        }

        attachmentPropertiesButton.setEnabled((type != AttachmentHandlerType.NONE && type != AttachmentHandlerType.DICOM));

        if (lastType != type) {
            currentChannel.getProperties().setAttachmentProperties(type.getDefaultProperties());
        }

        MessageStorageMode messageStorageMode = MessageStorageMode.fromInt(messageStorageSlider.getValue());

        switch (messageStorageMode) {
            case METADATA:
            case DISABLED:
                attachmentStoreCheckBox.setSelected(false);
                attachmentStoreCheckBox.setEnabled(false);
                break;

            default:
                attachmentStoreCheckBox.setSelected(type != AttachmentHandlerType.NONE);
                attachmentStoreCheckBox.setEnabled(type != AttachmentHandlerType.NONE);
                break;
        }

        if (type == AttachmentHandlerType.NONE) {
            attachmentWarningLabel.setVisible(false);
        } else {
            attachmentWarningLabel.setVisible(!attachmentStoreCheckBox.isEnabled());
        }
    }//GEN-LAST:event_attachmentComboBoxActionPerformed

    private void attachmentPropertiesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attachmentPropertiesButtonActionPerformed
        showAttachmentPropertiesDialog((AttachmentHandlerType) attachmentComboBox.getSelectedItem());
    }//GEN-LAST:event_attachmentPropertiesButtonActionPerformed

    private void changeDataTypesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeDataTypesButtonActionPerformed
        String previousDataType = currentChannel.getSourceConnector().getTransformer().getInboundDataType();
        AttachmentHandlerType previousDefaultAttachmentHandlerType = LoadedExtensions.getInstance().getDataTypePlugins().get(previousDataType).getDefaultAttachmentHandlerType();
        AttachmentHandlerType previousAttachmentHandlerType = (AttachmentHandlerType) attachmentComboBox.getSelectedItem();

        new DataTypesDialog();

        String dataType = currentChannel.getSourceConnector().getTransformer().getInboundDataType();
        AttachmentHandlerType defaultAttachmentHandlerType = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultAttachmentHandlerType();

        if (defaultAttachmentHandlerType != null) {
            attachmentComboBox.setSelectedItem(defaultAttachmentHandlerType);
        } else {
            if (previousAttachmentHandlerType == previousDefaultAttachmentHandlerType) {
                attachmentComboBox.setSelectedItem(AttachmentHandlerType.NONE);
            }
        }
    }//GEN-LAST:event_changeDataTypesButtonActionPerformed

    private void summaryNameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_summaryNameFieldKeyReleased
        currentChannel.setName(summaryNameField.getText());
        parent.setPanelName("Edit Channel - " + currentChannel.getName());
    }//GEN-LAST:event_summaryNameFieldKeyReleased

    private void addTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTagButtonActionPerformed
        new ChannelTagDialog(tagTable);
    }//GEN-LAST:event_addTagButtonActionPerformed

    private void deleteTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteTagButtonActionPerformed
        int selectedRow = getSelectedRow(tagTable);

        if (selectedRow != -1 && !tagTable.isEditing()) {
            ((DefaultTableModel) tagTable.getModel()).removeRow(selectedRow);
        }

        int rowCount = tagTable.getRowCount();

        if (rowCount > 0) {
            if (selectedRow >= rowCount) {
                selectedRow--;
            }

            tagTable.setRowSelectionInterval(selectedRow, selectedRow);
        }
    }//GEN-LAST:event_deleteTagButtonActionPerformed

    private void addMetaDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMetaDataButtonActionPerformed
        DefaultTableModel model = ((DefaultTableModel) metaDataTable.getModel());
        int row = model.getRowCount();

        model.addRow(new Object[] { "", MetaDataColumnType.STRING, "" });

        metaDataTable.setRowSelectionInterval(row, row);

        revertMetaDataButton.setEnabled(true);

        parent.setSaveEnabled(true);
    }//GEN-LAST:event_addMetaDataButtonActionPerformed

    private void deleteMetaDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMetaDataButtonActionPerformed
        int selectedRow = metaDataTable.getSelectedRow();

        if (selectedRow != -1 && !metaDataTable.isEditing()) {
            ((DefaultTableModel) metaDataTable.getModel()).removeRow(selectedRow);
        }

        int rowCount = metaDataTable.getRowCount();

        if (rowCount > 0) {
            if (selectedRow >= rowCount) {
                selectedRow--;
            }

            metaDataTable.setRowSelectionInterval(selectedRow, selectedRow);
        }

        revertMetaDataButton.setEnabled(true);

        parent.setSaveEnabled(true);
    }//GEN-LAST:event_deleteMetaDataButtonActionPerformed

    private void encryptMessagesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encryptMessagesCheckBoxActionPerformed
        parent.setSaveEnabled(true);
        updateStorageMode();
    }//GEN-LAST:event_encryptMessagesCheckBoxActionPerformed

    private void messageStorageSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_messageStorageSliderStateChanged
        parent.setSaveEnabled(true);
        updateStorageMode();

        MessageStorageMode messageStorageMode = MessageStorageMode.fromInt(messageStorageSlider.getValue());

        switch (messageStorageMode) {
            case METADATA:
            case DISABLED:
                if (attachmentStoreCheckBox.isEnabled()) {
                    attachmentStoreCheckBox.setSelected(false);
                    attachmentStoreCheckBox.setEnabled(false);
                }
                break;

            default:
                if (!attachmentStoreCheckBox.isEnabled()) {
                    attachmentStoreCheckBox.setSelected(attachmentComboBox.getSelectedItem() != AttachmentHandlerType.NONE);
                    attachmentStoreCheckBox.setEnabled(attachmentComboBox.getSelectedItem() != AttachmentHandlerType.NONE);
                }
                break;
        }
    }//GEN-LAST:event_messageStorageSliderStateChanged

    private void removeContentCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeContentCheckboxActionPerformed
        parent.setSaveEnabled(true);
        updateStorageMode();
    }//GEN-LAST:event_removeContentCheckboxActionPerformed

    private void metadataPruningOnRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_metadataPruningOnRadioActionPerformed
        parent.setSaveEnabled(true);
        metadataPruningDaysTextField.setEnabled(true);
        archiveCheckBox.setEnabled(true);
    }//GEN-LAST:event_metadataPruningOnRadioActionPerformed

    private void contentPruningMetadataRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contentPruningMetadataRadioActionPerformed
        parent.setSaveEnabled(true);
        contentPruningDaysTextField.setEnabled(false);

        if (metadataPruningOffRadio.isSelected()) {
            archiveCheckBox.setEnabled(false);
        }
    }//GEN-LAST:event_contentPruningMetadataRadioActionPerformed

    private void contentPruningDaysRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contentPruningDaysRadioActionPerformed
        parent.setSaveEnabled(true);
        contentPruningDaysTextField.setEnabled(true);
        archiveCheckBox.setEnabled(true);
    }//GEN-LAST:event_contentPruningDaysRadioActionPerformed

    private void revertMetaDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_revertMetaDataButtonActionPerformed
        if (parent.alertOption(parent, "Are you sure you want to revert custom metadata settings to the last save?")) {
            updateMetaDataTable();
        }
    }//GEN-LAST:event_revertMetaDataButtonActionPerformed

    private void removeAttachmentsCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAttachmentsCheckboxActionPerformed
        parent.setSaveEnabled(true);
        updateStorageMode();
    }//GEN-LAST:event_removeAttachmentsCheckboxActionPerformed

    private void attachmentStoreCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_attachmentStoreCheckBoxItemStateChanged
        attachmentWarningLabel.setVisible(evt.getStateChange() != ItemEvent.SELECTED && attachmentComboBox.getSelectedItem() != AttachmentHandlerType.NONE);
    }//GEN-LAST:event_attachmentStoreCheckBoxItemStateChanged

    public void generateMultipleDestinationPage() {
        // Get the selected destination connector and set it.
        destinationConnectorPanel.setConnectorSettingsPanel(LoadedExtensions.getInstance().getDestinationConnectors().get((String) destinationSourceDropdown.getSelectedItem()));

        // Get the currently selected destination connector.
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        int connectorIndex = destinationTable.getSelectedModelIndex();
        Connector destinationConnector = destinationConnectors.get(connectorIndex);

        if (connectorIndex == 0) {
            waitForPreviousCheckbox.setSelected(false);
            waitForPreviousCheckbox.setEnabled(false);
        } else {
            waitForPreviousCheckbox.setSelected(destinationConnector.isWaitForPrevious());
            waitForPreviousCheckbox.setEnabled(true);
        }

        String connectorName = "";

        if (destinationConnector.getProperties() != null) {
            connectorName = destinationConnector.getProperties().getName();
        }

        // Debug with:
        // System.out.println(destinationConnector.getTransportName() + " " + (String)destinationSourceDropdown.getSelectedItem());

        // Set to defaults on first load of connector or if it has changed types.
        if (destinationConnector.getProperties() == null || !connectorName.equals(destinationSourceDropdown.getSelectedItem())) {
            String name = destinationConnector.getName();
            changeConnectorType(destinationConnector, true);
            destinationConnector.setName(name);
            destinationConnectorPanel.setProperties(destinationConnectorPanel.getDefaults());
            destinationConnector.setProperties(destinationConnectorPanel.getProperties());
        }

        destinationVariableList.setTransferMode(destinationConnectorPanel.getTransferMode());

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
        destinationConnectorPanel.setProperties(destinationConnector.getProperties());
        setDestinationVariableList();

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
        VariableListUtil.getStepVariables(concatenatedSteps, currentChannel.getSourceConnector().getTransformer(), false);

        // add only the global variables
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        Iterator<Connector> it = destinationConnectors.iterator();
        boolean seenCurrent = false;
        while (it.hasNext()) {
            Connector destination = it.next();
            if (currentDestination == destination) {
                seenCurrent = true;
                // add all the variables
                VariableListUtil.getStepVariables(concatenatedSteps, destination.getTransformer(), true);
            } else if (!seenCurrent) {
                // add only the global variables
                VariableListUtil.getStepVariables(concatenatedSteps, destination.getTransformer(), false);
                VariableListUtil.getStepVariables(concatenatedSteps, destination.getResponseTransformer(), false);
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
        destinationVariableList.populateConnectors(currentChannel.getDestinationConnectors());
        destinationVariableList.setBorder("Destination Mappings", new Color(0, 0, 0));
        destinationVariableList.repaint();
    }

    /** Returns a new connector, that has a new transformer and filter */
    public Connector makeNewConnector(boolean isDestination) {
        Connector c = new Connector();
        c.setEnabled(true);

        Transformer dt = new Transformer();
        Transformer drt = null;
        Filter df = new Filter();

        if (isDestination) {
            c.setMode(Connector.Mode.DESTINATION);
            drt = new Transformer();
        } else {
            c.setMode(Connector.Mode.SOURCE);
        }

        c.setTransformer(dt);
        c.setResponseTransformer(drt);
        c.setFilter(df);
        return c;
    }

    /** Changes the connector type without clearing filter and transformer */
    public void changeConnectorType(Connector c, boolean isDestination) {
        Transformer oldTransformer = c.getTransformer();
        Filter oldFilter = c.getFilter();
        Transformer oldResponseTransformer = c.getResponseTransformer();

        if (isDestination) {
            c = makeNewConnector(true);
        } else {
            c = makeNewConnector(false);
        }

        c.setTransformer(oldTransformer);
        c.setFilter(oldFilter);
        c.setResponseTransformer(oldResponseTransformer);
    }

    /**
     * Returns true if this channel requires XML as a source data type, and false if it does not.
     */
    public boolean requiresXmlDataType() {
        return sourceConnectorPanel.requiresXmlDataType();
    }

    /**
     * Check if the source data type is required to be XML, and set it if necessary.
     */
    public void checkAndSetXmlDataType() {
        if (requiresXmlDataType() && !currentChannel.getSourceConnector().getTransformer().getInboundDataType().equals(UIConstants.DATATYPE_XML)) {
            DataTypeProperties defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(UIConstants.DATATYPE_XML).getDefaultProperties();

            currentChannel.getSourceConnector().getTransformer().setInboundDataType(UIConstants.DATATYPE_XML);
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

        // If the connector is a source, then set it, change the dropdown, and set the incoming dataType.
        if ((channelView.getSelectedIndex() == SOURCE_TAB_INDEX) && (connector.getMode().equals(Mode.SOURCE))) {
            currentChannel.setSourceConnector(connector);
            sourceSourceDropdown.setSelectedItem(currentChannel.getSourceConnector().getTransportName());

            updateAttachmentHandler(connector.getTransformer().getInboundDataType());

            sourceComponentShown(null);
        } // If the connector is a destination, then check/generate its name, add it, and re-make the destination table.
        else if ((channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX) && (connector.getMode().equals(Mode.DESTINATION))) {
            List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
            for (Connector destinationConnector : destinationConnectors) {
                if (destinationConnector.getName().equalsIgnoreCase(connector.getName())) {
                    connector.setName(getNewDestinationName(destinationConnectors.size() + 1));
                }
            }
            currentChannel.addDestination(connector);
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

    public void updateAttachmentHandler(String dataType) {
        if (LoadedExtensions.getInstance().getDataTypePlugins().containsKey(dataType)) {
            AttachmentHandlerType oldType = (AttachmentHandlerType) attachmentComboBox.getSelectedItem();
            AttachmentHandlerType newType = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultAttachmentHandlerType();
            if (newType == null) {
                newType = AttachmentHandlerType.NONE;
            }

            if ((oldType == AttachmentHandlerType.NONE && newType != AttachmentHandlerType.NONE) || (oldType == AttachmentHandlerType.DICOM && newType == AttachmentHandlerType.NONE)) {
                attachmentComboBox.setSelectedItem(newType);
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMetaDataButton;
    private com.mirth.connect.client.ui.components.MirthButton addTagButton;
    private com.mirth.connect.client.ui.components.MirthCheckBox archiveCheckBox;
    private com.mirth.connect.client.ui.components.MirthComboBox attachmentComboBox;
    private javax.swing.JLabel attachmentLabel;
    private javax.swing.JButton attachmentPropertiesButton;
    public com.mirth.connect.client.ui.components.MirthCheckBox attachmentStoreCheckBox;
    private javax.swing.JLabel attachmentWarningLabel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton changeDataTypesButton;
    private javax.swing.JPanel channelPropertiesPanel;
    private javax.swing.JPanel channelTagsPanel;
    private javax.swing.JScrollPane channelTagsScrollPane;
    private javax.swing.JTabbedPane channelView;
    public com.mirth.connect.client.ui.components.MirthCheckBox clearGlobalChannelMapCheckBox;
    private javax.swing.JLabel contentDaysLabel;
    private javax.swing.JLabel contentLabel;
    private javax.swing.ButtonGroup contentPruningButtonGroup;
    private javax.swing.JRadioButton contentPruningDaysRadio;
    private com.mirth.connect.client.ui.components.MirthTextField contentPruningDaysTextField;
    private javax.swing.JLabel contentPruningLabel;
    private javax.swing.JRadioButton contentPruningMetadataRadio;
    private javax.swing.JPanel customMetadataPanel;
    private javax.swing.JButton deleteMetaDataButton;
    private com.mirth.connect.client.ui.components.MirthButton deleteTagButton;
    private javax.swing.JPanel destination;
    private javax.swing.JScrollPane destinationConnectorPane;
    private com.mirth.connect.client.ui.panels.connectors.ConnectorPanel destinationConnectorPanel;
    private com.mirth.connect.client.ui.components.MirthComboBox destinationSourceDropdown;
    private javax.swing.JLabel destinationSourceLabel;
    private com.mirth.connect.client.ui.components.MirthTable destinationTable;
    private javax.swing.JScrollPane destinationTablePane;
    public com.mirth.connect.client.ui.VariableList destinationVariableList;
    private javax.swing.JLabel durableLabel;
    private javax.swing.JLabel durableStatusLabel;
    private com.mirth.connect.client.ui.components.MirthCheckBox encryptMessagesCheckBox;
    private com.mirth.connect.client.ui.components.MirthComboBox initialState;
    private javax.swing.JLabel initialStateLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lastModified;
    private javax.swing.JPanel messagePruningPanel;
    private javax.swing.JPanel messageStoragePanel;
    private javax.swing.JProgressBar messageStorageProgressBar;
    private javax.swing.JSlider messageStorageSlider;
    private com.mirth.connect.client.ui.components.MirthTable metaDataTable;
    private javax.swing.JScrollPane metaDataTablePane;
    private javax.swing.JLabel metadataDaysLabel;
    private javax.swing.JLabel metadataLabel;
    private javax.swing.ButtonGroup metadataPruningButtonGroup;
    private com.mirth.connect.client.ui.components.MirthTextField metadataPruningDaysTextField;
    private javax.swing.JLabel metadataPruningLabel;
    private javax.swing.JRadioButton metadataPruningOffRadio;
    private javax.swing.JRadioButton metadataPruningOnRadio;
    private javax.swing.JLabel performanceLabel;
    private javax.swing.JLabel queueWarningLabel;
    private com.mirth.connect.client.ui.components.MirthCheckBox removeAttachmentsCheckbox;
    private com.mirth.connect.client.ui.components.MirthCheckBox removeContentCheckbox;
    private javax.swing.JButton revertMetaDataButton;
    private com.mirth.connect.client.ui.ScriptPanel scripts;
    private javax.swing.JPanel source;
    private javax.swing.JScrollPane sourceConnectorPane;
    private com.mirth.connect.client.ui.panels.connectors.ConnectorPanel sourceConnectorPanel;
    private com.mirth.connect.client.ui.components.MirthComboBox sourceSourceDropdown;
    private javax.swing.JLabel sourceSourceLabel;
    private javax.swing.JLabel storageModeLabel;
    private javax.swing.JPanel summary;
    private javax.swing.JScrollPane summaryDescriptionScrollPane;
    private com.mirth.connect.client.ui.components.MirthTextPane summaryDescriptionText;
    private com.mirth.connect.client.ui.components.MirthCheckBox summaryEnabledCheckbox;
    private com.mirth.connect.client.ui.components.MirthTextField summaryNameField;
    private javax.swing.JLabel summaryNameLabel;
    private javax.swing.JLabel summaryPatternLabel1;
    private javax.swing.JLabel summaryRevision;
    private com.mirth.connect.client.ui.components.MirthTable tagTable;
    private com.mirth.connect.client.ui.components.MirthCheckBox waitForPreviousCheckbox;
    // End of variables declaration//GEN-END:variables
}

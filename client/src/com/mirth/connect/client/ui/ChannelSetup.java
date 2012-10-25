/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.editors.JavaScriptEditorDialog;
import com.mirth.connect.client.ui.editors.filter.FilterPane;
import com.mirth.connect.client.ui.editors.transformer.TransformerPane;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.util.VariableListUtil;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;
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
import com.mirth.connect.model.converters.DataTypeFactory;
import com.mirth.connect.model.converters.DefaultSerializerPropertiesFactory;
import com.mirth.connect.model.handlers.AttachmentHandlerFactory;
import com.mirth.connect.model.handlers.AttachmentHandlerType;
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
    private static final String DESTINATION_COLUMN_NAME = "Destination";
    private static final String CONNECTOR_TYPE_COLUMN_NAME = "Connector Type";
    private static final String WAIT_FOR_PREVIOUS_COLUMN_NAME = "Wait for previous";
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
    private ChannelTagDialog channelTagDialog;

    /**
     * Creates the Channel Editor panel. Calls initComponents() and sets up the
     * model, dropdowns, and mouse listeners.
     */
    public ChannelSetup() {
        this.parent = PlatformUI.MIRTH_FRAME;

        initComponents();
        initChannelTagsUI();
        initMetaDataTable();
        
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
        
        for (String tag : currentChannel.getTags()) {
            model.addRow(new Object[]{tag});
        }
    }
    
    private void saveChannelTags() {
        currentChannel.getTags().clear();
        
        DefaultTableModel model = (DefaultTableModel) tagTable.getModel();
        int rowCount = model.getRowCount();
        
        for (int i = 0; i < rowCount; i++) {
            currentChannel.getTags().add((String) model.getValueAt(i, 0));
        }
    }
    
    private void updateMetaDataTable() {
        DefaultTableModel model = (DefaultTableModel)metaDataTable.getModel();
        model.setNumRows(0);

        for (MetaDataColumn column : currentChannel.getProperties().getMetaDataColumns()) {
            model.addRow(new Object[]{column.getName(), column.getType(), column.getMappingName()});
        }
    }
    
    public void saveMetaDataColumns() {
        DefaultTableModel model = (DefaultTableModel)metaDataTable.getModel();
        
        List<MetaDataColumn> metaDataColumns = currentChannel.getProperties().getMetaDataColumns();
        metaDataColumns.clear();
        
        for (int i = 0; i < model.getRowCount(); i++) {
            MetaDataColumn column = new MetaDataColumn();
            column.setName((String) model.getValueAt(i, 0));
            column.setType((MetaDataColumnType) model.getValueAt(i, 1));
            column.setMappingName((String)model.getValueAt(i, 2));
            
            metaDataColumns.add(column);
        }
    }
    
    private int getSelectedRow(MirthTable table) {
        return table.isEditing() ? table.getEditingRow() : table.getSelectedRow();
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
        
        tableData = new Object[tableSize][4];
        
        for (int i = 0; i < tableSize; i++) {
            if (tableSize - 1 == i && addNew) {
                Connector connector = makeNewConnector(true);

                // Set the default inbound and outbound dataType and properties
                String dataType = currentChannel.getSourceConnector().getTransformer().getOutboundDataType();
                // Use a different properties object for the inbound and outbound
                Properties defaultInboundProperties = MapUtils.toProperties(DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(dataType));
                Properties defaultOutboundProperties = MapUtils.toProperties(DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(dataType));

                connector.getTransformer().setInboundDataType(dataType);
                connector.getTransformer().setInboundProperties(defaultInboundProperties);
                connector.getTransformer().setOutboundDataType(dataType);
                connector.getTransformer().setOutboundProperties(defaultOutboundProperties);

                connector.setName(getNewDestinationName(tableSize));
                connector.setTransportName(DESTINATION_DEFAULT);

                if (connector.isEnabled()) {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_blue.png")), UIConstants.ENABLED_STATUS);
                } else {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), UIConstants.DISABLED_STATUS);
                }
                tableData[i][1] = connector.getName();
                tableData[i][2] = connector.getTransportName();
                tableData[i][3] = connector.isWaitForPrevious();

                currentChannel.addDestination(connector);
            } else {

                if (destinationConnectors.get(i).isEnabled()) {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_blue.png")), UIConstants.ENABLED_STATUS);
                } else {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), UIConstants.DISABLED_STATUS);
                }
                tableData[i][1] = destinationConnectors.get(i).getName();
                tableData[i][2] = destinationConnectors.get(i).getTransportName();
                tableData[i][3] = destinationConnectors.get(i).isWaitForPrevious();
            }
        }

        destinationTable = new MirthTable();

        destinationTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[] { STATUS_COLUMN_NAME, DESTINATION_COLUMN_NAME, CONNECTOR_TYPE_COLUMN_NAME, WAIT_FOR_PREVIOUS_COLUMN_NAME }) {

            boolean[] canEdit = new boolean[] { false, true, false, false };

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

//        PropertyVerifier.checkConnectorProperties(currentChannel, parent.getConnectorMetaData());

        sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(LoadedExtensions.getInstance().getSourceConnectors().keySet().toArray()));
        destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(LoadedExtensions.getInstance().getDestinationConnectors().keySet().toArray()));

        loadChannelInfo();
        makeDestinationTable(false);
        updateMetaDataTable();
        updateTagTable();
        setDestinationVariableList();
        loadingChannel = false;
        channelView.setSelectedIndex(0);
        parent.retrieveAllChannelTags();
    }

    /**
     * Adds a new channel that is passed in and then sets the overall panel to
     * edit that channel.
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
        String defaultDataType = DataTypeFactory.HL7V2;
        // Use a different properties object for the inbound and outbound
        Properties defaultInboundProperties = MapUtils.toProperties(DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(defaultDataType));
        Properties defaultOutboundProperties = MapUtils.toProperties(DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(defaultDataType));

        sourceTransformer.setInboundDataType(defaultDataType);
        sourceTransformer.setInboundProperties(defaultInboundProperties);
        sourceTransformer.setOutboundDataType(defaultDataType);
        sourceTransformer.setOutboundProperties(defaultOutboundProperties);

        sourceConnector.setTransformer(sourceTransformer);

        currentChannel.setSourceConnector(sourceConnector);
        setLastModified();
        loadChannelInfo();
        makeDestinationTable(true);
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
        updateStorageMode();
        
        // load pruning settings
        Integer pruneMetaDataDays = properties.getPruneMetaDataDays();
        Integer pruneContentDays = properties.getPruneContentDays();
        
        if (pruneMetaDataDays == null) {
            metadataPruningOffRadio.setSelected(true);
            metadataPruningDaysTextField.setText("");
            metadataPruningDaysTextField.setEnabled(false);
        } else {
            metadataPruningOnRadio.setSelected(true);
            metadataPruningDaysTextField.setText(pruneMetaDataDays.toString());
            metadataPruningDaysTextField.setEnabled(true);
        }
        
        if (pruneContentDays == null) {
            contentPruningMetadataRadio.setSelected(true);
            contentPruningDaysTextField.setText("");
            contentPruningDaysTextField.setEnabled(false);
        } else {
            contentPruningDaysRadio.setSelected(true);
            contentPruningDaysTextField.setText(pruneContentDays.toString());
            contentPruningDaysTextField.setEnabled(true);
        }

        sourceSourceDropdown.setSelectedItem(currentChannel.getSourceConnector().getTransportName());

        if (currentChannel.getProperties().isInitialStateStarted()) {
            initialState.setSelectedItem("Started");
        } else {
            initialState.setSelectedItem("Stopped");
        }
        
        parent.setSaveEnabled(enabled);
    }
    
    private void updateStorageMode() {
        switch (MessageStorageMode.fromInt(messageStorageSlider.getValue())) {
            case DEVELOPMENT:
                storageModeLabel.setText("Development");
                storageLabel.setText("Storage: All message information");
                durableStatusLabel.setText("On");
                durableStatusLabel.setForeground(new Color(0, 130, 0));
                messageStorageProgressBar.setValue(15);
                encryptMessagesCheckBox.setEnabled(true);
                removeContentCheckbox.setEnabled(true);
                break;
                
            case PRODUCTION:
                storageModeLabel.setText("Production");
                storageLabel.setText("Storage: Raw, Encoded, Sent, Response");
                durableStatusLabel.setText("On");
                durableStatusLabel.setForeground(new Color(0, 130, 0));
                messageStorageProgressBar.setValue(20);
                encryptMessagesCheckBox.setEnabled(true);
                removeContentCheckbox.setEnabled(true);
                break;
                
            case RAW:
                storageModeLabel.setText("Raw");
                storageLabel.setText("Storage: Raw");
                durableStatusLabel.setText("Off");
                durableStatusLabel.setForeground(new Color(130, 0, 0));
                messageStorageProgressBar.setValue(39);
                encryptMessagesCheckBox.setEnabled(true);
                removeContentCheckbox.setEnabled(true);
                break;
                
            case METADATA:
                storageModeLabel.setText("Metadata");
                storageLabel.setText("Storage: No content, message metadata only");
                durableStatusLabel.setText("Off");
                durableStatusLabel.setForeground(new Color(130, 0, 0));
                messageStorageProgressBar.setValue(78);
                encryptMessagesCheckBox.setEnabled(false);
                removeContentCheckbox.setEnabled(false);
                break;
                
            case DISABLED:
                storageModeLabel.setText("Disabled");
                storageLabel.setText("Storage: No message information");
                durableStatusLabel.setText("Off");
                durableStatusLabel.setForeground(new Color(130, 0, 0));
                messageStorageProgressBar.setValue(100);
                encryptMessagesCheckBox.setEnabled(false);
                removeContentCheckbox.setEnabled(false);
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

        boolean enabled = summaryEnabledCheckbox.isSelected();

        currentChannel.getSourceConnector().setProperties(sourceConnectorPanel.getProperties());

        if (parent.currentContentPage == transformerPane) {
            transformerPane.accept(false);
            transformerPane.modified = false; // TODO: Check this. Fix to prevent double save on confirmLeave
        }
        if (parent.currentContentPage == filterPane) {
            filterPane.accept(false);
            filterPane.modified = false; // TODO: Check this. Fix to prevent double save on confirmLeave
        }

        Connector temp;

        temp = currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex());
        temp.setProperties(destinationConnectorPanel.getProperties());

        currentChannel.setName(summaryNameField.getText());
        currentChannel.setDescription(summaryDescriptionText.getText());

        updateScripts();
        setLastModified();

        currentChannel.getProperties().setClearGlobalChannelMap(clearGlobalChannelMapCheckBox.isSelected());
        currentChannel.getProperties().setEncryptData(encryptMessagesCheckBox.isSelected());
        currentChannel.getProperties().setInitialStateStarted(((String) initialState.getSelectedItem()).equalsIgnoreCase("Started"));

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
        saveMessageStorage();
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
            } catch (SerializationException e) {
                parent.alertException(this.parent, e.getStackTrace(), e.getMessage());
            }
        } catch (ClientException e) {
            parent.alertException(this.parent, e.getStackTrace(), e.getMessage());
        }

        return updated;
    }
    
    private void saveMessageStorage() {
        ChannelProperties properties = currentChannel.getProperties();
        properties.setMessageStorageMode(MessageStorageMode.fromInt(messageStorageSlider.getValue()));
        properties.setEncryptData(encryptMessagesCheckBox.isSelected());
        properties.setRemoveContentOnCompletion(removeContentCheckbox.isSelected());
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
    }

    /**
     * Set all the dataTypes and properties to proper values if they are null.
     * This is only necessary for channels from before version 2.0
     */
    public void fixNullDataTypesAndProperties() {
        Transformer sourceTransformer = currentChannel.getSourceConnector().getTransformer();

        String defaultDataType = DataTypeFactory.HL7V2;

        if (sourceTransformer.getInboundDataType() == null) {
            sourceTransformer.setInboundDataType(defaultDataType);
        }

        if (sourceTransformer.getInboundProperties() == null) {
            Properties defaultProperties = MapUtils.toProperties(DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(sourceTransformer.getInboundDataType()));
            sourceTransformer.setInboundProperties(defaultProperties);
        }

        if (sourceTransformer.getOutboundDataType() == null) {
            sourceTransformer.setOutboundDataType(sourceTransformer.getInboundDataType());
        }

        if (sourceTransformer.getOutboundProperties() == null) {
            Properties defaultProperties = MapUtils.toProperties(DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(sourceTransformer.getOutboundDataType()));
            sourceTransformer.setOutboundProperties(defaultProperties);
        }

        for (Connector c : currentChannel.getDestinationConnectors()) {
            Transformer destinationTransformer = c.getTransformer();

            if (destinationTransformer.getInboundDataType() == null) {
                destinationTransformer.setInboundDataType(sourceTransformer.getOutboundDataType());
            }

            if (destinationTransformer.getInboundProperties() == null) {
                destinationTransformer.setInboundProperties(sourceTransformer.getOutboundProperties());
            }

            if (destinationTransformer.getOutboundDataType() == null) {
                destinationTransformer.setOutboundDataType(destinationTransformer.getInboundDataType());
            }

            if (destinationTransformer.getOutboundProperties() == null) {
                Properties defaultProperties = MapUtils.toProperties(DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(destinationTransformer.getOutboundDataType()));
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
     * Checks to see which tasks (move up, move down, enable, and disable)
     * should be available for
     * destinations and enables or disables them. Also sets the number of
     * filter/transformer steps
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
     * Checks all of the connectors in this channel and returns the errors
     * found.
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
        if (type.equals(AttachmentHandlerType.REGEX)) {
            String pattern = currentChannel.getProperties().getAttachmentProperties().getProperties().get("regex.pattern");
            String mimeType = currentChannel.getProperties().getAttachmentProperties().getProperties().get("regex.mimetype");
            RegexAttachmentDialog dialog = new RegexAttachmentDialog(pattern, mimeType);
            currentChannel.getProperties().getAttachmentProperties().getProperties().put("regex.pattern", dialog.getSavedPattern());
            currentChannel.getProperties().getAttachmentProperties().getProperties().put("regex.mimetype", dialog.getMimeType());
        } else if (type.equals(AttachmentHandlerType.DICOM)) {
            
        } else if (type.equals(AttachmentHandlerType.JAVASCRIPT)) {
            String script = currentChannel.getProperties().getAttachmentProperties().getProperties().get("javascript.script");
            JavaScriptEditorDialog dialog = new JavaScriptEditorDialog(script);
            currentChannel.getProperties().getAttachmentProperties().getProperties().put("javascript.script", dialog.getSavedScript());
        } else if (type.equals(AttachmentHandlerType.CUSTOM)) {
            String className = currentChannel.getProperties().getAttachmentProperties().getClassName();
            CustomAttachmentDialog dialog = new CustomAttachmentDialog(className, currentChannel.getProperties().getAttachmentProperties().getProperties());
            currentChannel.getProperties().getAttachmentProperties().setClassName(dialog.getSavedClassName());
            currentChannel.getProperties().getAttachmentProperties().setProperties(dialog.getSavedProperties());
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
        messageStoragePanel = new javax.swing.JPanel();
        storageModeLabel = new javax.swing.JLabel();
        storageLabel = new javax.swing.JLabel();
        durableLabel = new javax.swing.JLabel();
        performanceLabel = new javax.swing.JLabel();
        messageStorageSlider = new javax.swing.JSlider();
        messageStorageProgressBar = new javax.swing.JProgressBar();
        encryptMessagesCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        durableStatusLabel = new javax.swing.JLabel();
        removeContentCheckbox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        messagePruningPanel = new javax.swing.JPanel();
        metadataLabel = new javax.swing.JLabel();
        metadataPruningOffRadio = new javax.swing.JRadioButton();
        metadataPruningOnRadio = new javax.swing.JRadioButton();
        metadataPruningDaysTextField = new com.mirth.connect.client.ui.components.MirthTextField();
        metadataDaysLabel = new javax.swing.JLabel();
        contentPruningMetadataRadio = new javax.swing.JRadioButton();
        contentLabel = new javax.swing.JLabel();
        contentPruningDaysRadio = new javax.swing.JRadioButton();
        contentPruningDaysTextField = new com.mirth.connect.client.ui.components.MirthTextField();
        contentDaysLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
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

        initialState.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Started", "Stopped" }));

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
                .addGap(34, 34, 34)
                .addGroup(channelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(clearGlobalChannelMapCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(summaryEnabledCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(channelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(summaryRevision, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lastModified, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addGroup(channelPropertiesPanelLayout.createSequentialGroup()
                        .addGroup(channelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(summaryPatternLabel1)
                            .addComponent(changeDataTypesButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(channelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(initialState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(initialStateLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(channelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(attachmentLabel)
                            .addComponent(attachmentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attachmentPropertiesButton)))
                    .addComponent(lastModified)
                    .addComponent(clearGlobalChannelMapCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        messageStoragePanel.setBackground(new java.awt.Color(255, 255, 255));
        messageStoragePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Message Storage"));

        storageModeLabel.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        storageModeLabel.setText("Development");

        storageLabel.setText("Storage: ");

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

        javax.swing.GroupLayout messageStoragePanelLayout = new javax.swing.GroupLayout(messageStoragePanel);
        messageStoragePanel.setLayout(messageStoragePanelLayout);
        messageStoragePanelLayout.setHorizontalGroup(
            messageStoragePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(messageStoragePanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(messageStorageSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(messageStoragePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(messageStoragePanelLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(messageStoragePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(messageStoragePanelLayout.createSequentialGroup()
                                .addComponent(performanceLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(messageStorageProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE))
                            .addGroup(messageStoragePanelLayout.createSequentialGroup()
                                .addGroup(messageStoragePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(encryptMessagesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(removeContentCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(messageStoragePanelLayout.createSequentialGroup()
                                .addComponent(durableLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(durableStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(messageStoragePanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(messageStoragePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(messageStoragePanelLayout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(storageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(storageModeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        messageStoragePanelLayout.setVerticalGroup(
            messageStoragePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(messageStoragePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(messageStoragePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(messageStoragePanelLayout.createSequentialGroup()
                        .addComponent(storageModeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(storageLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(messageStoragePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(durableLabel)
                            .addComponent(durableStatusLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(messageStoragePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(performanceLabel)
                            .addComponent(messageStorageProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(encryptMessagesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeContentCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(messageStorageSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(9, 9, 9))
        );

        messagePruningPanel.setBackground(new java.awt.Color(255, 255, 255));
        messagePruningPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Message Pruning"));

        metadataLabel.setText("Metadata:");

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

        contentLabel.setText("Content:");

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

        javax.swing.GroupLayout messagePruningPanelLayout = new javax.swing.GroupLayout(messagePruningPanel);
        messagePruningPanel.setLayout(messagePruningPanelLayout);
        messagePruningPanelLayout.setHorizontalGroup(
            messagePruningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(messagePruningPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(messagePruningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(metadataLabel)
                    .addComponent(contentLabel)
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
                    .addComponent(jLabel1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        messagePruningPanelLayout.setVerticalGroup(
            messagePruningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(messagePruningPanelLayout.createSequentialGroup()
                .addComponent(metadataLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(metadataPruningOffRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(messagePruningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(metadataPruningOnRadio)
                    .addComponent(metadataPruningDaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(metadataDaysLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contentLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contentPruningMetadataRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(messagePruningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contentPruningDaysRadio)
                    .addComponent(contentPruningDaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contentDaysLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addComponent(channelTagsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(channelTagsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(deleteTagButton, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                    .addComponent(addTagButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        channelTagsPanelLayout.setVerticalGroup(
            channelTagsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(channelTagsPanelLayout.createSequentialGroup()
                .addGroup(channelTagsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(channelTagsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(channelTagsPanelLayout.createSequentialGroup()
                        .addComponent(addTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 70, Short.MAX_VALUE)))
                .addContainerGap())
        );

        customMetadataPanel.setBackground(new java.awt.Color(255, 255, 255));
        customMetadataPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Custom Metadata"));

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

        javax.swing.GroupLayout customMetadataPanelLayout = new javax.swing.GroupLayout(customMetadataPanel);
        customMetadataPanel.setLayout(customMetadataPanelLayout);
        customMetadataPanelLayout.setHorizontalGroup(
            customMetadataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customMetadataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(metaDataTablePane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(customMetadataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(addMetaDataButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(deleteMetaDataButton, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))
                .addContainerGap())
        );
        customMetadataPanelLayout.setVerticalGroup(
            customMetadataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customMetadataPanelLayout.createSequentialGroup()
                .addComponent(addMetaDataButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteMetaDataButton)
                .addGap(0, 82, Short.MAX_VALUE))
            .addGroup(customMetadataPanelLayout.createSequentialGroup()
                .addComponent(metaDataTablePane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
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
                .addComponent(summaryDescriptionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
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
                            .addComponent(channelTagsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(summaryLayout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(messageStoragePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(messagePruningPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(customMetadataPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(channelTagsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(customMetadataPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(sourceConnectorPane, javax.swing.GroupLayout.DEFAULT_SIZE, 773, Short.MAX_VALUE)
                    .addGroup(sourceLayout.createSequentialGroup()
                        .addComponent(sourceSourceLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sourceSourceDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 449, Short.MAX_VALUE)))
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
                .addComponent(sourceConnectorPane, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)
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
        waitForPreviousCheckbox.setToolTipText("Enable this channel so that it can be deployed.");
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
                    .addComponent(destinationTablePane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 773, Short.MAX_VALUE)
                    .addGroup(destinationLayout.createSequentialGroup()
                        .addComponent(destinationConnectorPane, javax.swing.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(destinationVariableList, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, destinationLayout.createSequentialGroup()
                        .addComponent(destinationSourceLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(destinationSourceDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(waitForPreviousCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 262, Short.MAX_VALUE)))
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
                    .addComponent(destinationVariableList, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                    .addComponent(destinationConnectorPane, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE))
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
            .addComponent(channelView, javax.swing.GroupLayout.DEFAULT_SIZE, 802, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(channelView, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 654, Short.MAX_VALUE)
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
        
        channelTagDialog = new ChannelTagDialog(tagTable);
        
        DefaultTableModel model = new DefaultTableModel(new Object [][] {}, new String[] { "Tag" }) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }
        };
        
        tagTable.setModel(model);
    }
    
    private void initMetaDataTable() {        
        metaDataTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        DefaultTableModel model = new DefaultTableModel(new Object [][] {}, new String[] { METADATA_NAME_COLUMN_NAME, METADATA_TYPE_COLUMN_NAME, METADATA_MAPPING_COLUMN_NAME }) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }
            
            public void setValue(Object value, int row, int column) {
                if (getColumnName(column).equals(METADATA_NAME_COLUMN_NAME)) {
                    
                } else if (getColumnName(column).equals(METADATA_MAPPING_COLUMN_NAME)) {
                    
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
        DefaultCellEditor cellEditor = new DefaultCellEditor(new JComboBox(MetaDataColumnType.values()));
        cellEditor.setClickCountToStart(2);
        column.setCellEditor(cellEditor);
        column.setMinWidth(100);
        column.setMaxWidth(100);
        
        deleteMetaDataButton.setEnabled(false);
    }
    
    private void scriptsComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_scriptsComponentShown
    {//GEN-HEADEREND:event_scriptsComponentShown
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 12, false);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 14, 14, true);
    }//GEN-LAST:event_scriptsComponentShown

    /** Action when the source tab is shown. */
    private void sourceComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_sourceComponentShown
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 1, true);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 2, 8, false);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 9, 13, true);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 14, 14, false);

        // Update number of rules and steps on the filter and transformer
        parent.updateFilterTaskName(currentChannel.getSourceConnector().getFilter().getRules().size());
        parent.updateTransformerTaskName(currentChannel.getSourceConnector().getTransformer().getSteps().size());

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
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 2, 12, true);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 14, 14, false);

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

            if (parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundDataType() == DataTypeFactory.XML && parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundTemplate() != null && parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundTemplate().length() == 0) {
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
     * Action when an action is performed on the destination connector type
     * dropdown. Fires off either generateMultipleDestinationPage()
     */
    private void destinationSourceDropdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_destinationSourceDropdownActionPerformed
        // If a channel is not being loaded then alert the user when necessary
        // that changing the connector type will lose all current connector
        // data. Continue when deleting a destination because the selected
        // destination index will not be different than the last index.
        if (!loadingChannel && !isDeleting) {
            if (destinationConnectorPanel.getName() != null && destinationConnectorPanel.getName().equals(destinationSourceDropdown.getSelectedItem()) && lastModelIndex == destinationTable.getSelectedModelIndex()) {
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
        destinationTable.getModel().setValueAt(waitForPreviousCheckbox.isSelected(), destinationTable.getSelectedModelIndex(), destinationTable.getColumnModelIndex(WAIT_FOR_PREVIOUS_COLUMN_NAME));
    }//GEN-LAST:event_waitForPreviousCheckboxActionPerformed

    private void summaryComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_summaryComponentShown
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 12, false);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 14, 14, false);
    }//GEN-LAST:event_summaryComponentShown

    private void metadataPruningOffRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_metadataPruningOffRadioActionPerformed
        parent.setSaveEnabled(true);
        metadataPruningDaysTextField.setEnabled(false);
    }//GEN-LAST:event_metadataPruningOffRadioActionPerformed

    private void attachmentComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attachmentComboBoxActionPerformed
        AttachmentHandlerType type = (AttachmentHandlerType) attachmentComboBox.getSelectedItem();
        AttachmentHandlerType lastType = AttachmentHandlerType.fromString(currentChannel.getProperties().getAttachmentProperties().getType());

        if (lastType != AttachmentHandlerType.NONE && lastType != type && !AttachmentHandlerFactory.getDefaults(lastType).equals(currentChannel.getProperties().getAttachmentProperties())) {
            boolean changeType = parent.alertOption(this.parent, "Are you sure you would like to change this attachment handler type and lose all of the current handler data?");
            if (!changeType) {
                attachmentComboBox.setSelectedItem(lastType);
                attachmentPropertiesButton.setEnabled((lastType != AttachmentHandlerType.NONE && lastType != AttachmentHandlerType.DICOM));
                return;
            }
        }
        
        attachmentPropertiesButton.setEnabled((type != AttachmentHandlerType.NONE && type != AttachmentHandlerType.DICOM));

        if (lastType != type) {
            currentChannel.getProperties().setAttachmentProperties(AttachmentHandlerFactory.getDefaults(type));
        }
    }//GEN-LAST:event_attachmentComboBoxActionPerformed

    private void attachmentPropertiesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attachmentPropertiesButtonActionPerformed
        showAttachmentPropertiesDialog((AttachmentHandlerType) attachmentComboBox.getSelectedItem());
    }//GEN-LAST:event_attachmentPropertiesButtonActionPerformed

    private void changeDataTypesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeDataTypesButtonActionPerformed
        new DataTypesDialog();
        if (currentChannel.getSourceConnector().getTransformer().getInboundDataType().equals(DataTypeFactory.DICOM)) {
            attachmentComboBox.setSelectedItem(AttachmentHandlerType.DICOM);
        } else {
            if (attachmentComboBox.getSelectedItem() == AttachmentHandlerType.DICOM) {
                attachmentComboBox.setSelectedItem(AttachmentHandlerType.NONE);
            }
        }
    }//GEN-LAST:event_changeDataTypesButtonActionPerformed

    private void summaryNameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_summaryNameFieldKeyReleased
        currentChannel.setName(summaryNameField.getText());
        parent.setPanelName("Edit Channel - " + currentChannel.getName());
    }//GEN-LAST:event_summaryNameFieldKeyReleased

    private void addTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTagButtonActionPerformed
        channelTagDialog.setVisible(true);
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

        model.addRow(new Object[]{"", MetaDataColumnType.STRING, ""});

        metaDataTable.setRowSelectionInterval(row, row);

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

        parent.setSaveEnabled(true);
    }//GEN-LAST:event_deleteMetaDataButtonActionPerformed

    private void encryptMessagesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encryptMessagesCheckBoxActionPerformed
        parent.setSaveEnabled(true);
        updateStorageMode();
    }//GEN-LAST:event_encryptMessagesCheckBoxActionPerformed

    private void messageStorageSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_messageStorageSliderStateChanged
        parent.setSaveEnabled(true);
        updateStorageMode();
    }//GEN-LAST:event_messageStorageSliderStateChanged

    private void removeContentCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeContentCheckboxActionPerformed
        parent.setSaveEnabled(true);
        updateStorageMode();
    }//GEN-LAST:event_removeContentCheckboxActionPerformed

    private void metadataPruningOnRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_metadataPruningOnRadioActionPerformed
        parent.setSaveEnabled(true);
        metadataPruningDaysTextField.setEnabled(true);
    }//GEN-LAST:event_metadataPruningOnRadioActionPerformed

    private void contentPruningMetadataRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contentPruningMetadataRadioActionPerformed
        parent.setSaveEnabled(true);
        contentPruningDaysTextField.setEnabled(false);
    }//GEN-LAST:event_contentPruningMetadataRadioActionPerformed

    private void contentPruningDaysRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contentPruningDaysRadioActionPerformed
        parent.setSaveEnabled(true);
        contentPruningDaysTextField.setEnabled(true);
    }//GEN-LAST:event_contentPruningDaysRadioActionPerformed

    public void generateMultipleDestinationPage() {
        // Get the selected destination connector and set it.
        destinationConnectorPanel.setConnectorSettingsPanel(LoadedExtensions.getInstance().getDestinationConnectors().get((String) destinationSourceDropdown.getSelectedItem()));

        // Get the currently selected destination connector.
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        int connectorIndex = destinationTable.getSelectedModelIndex();
        Connector destinationConnector = destinationConnectors.get(connectorIndex);

        waitForPreviousCheckbox.setSelected(destinationConnector.isWaitForPrevious());

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

        destinationVariableList.setPrefixAndSuffix(destinationConnectorPanel.getDragAndDropPrefix(), destinationConnectorPanel.getDragAndDropSuffix());

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

    /**
     * Returns true if this channel requires XML as a source data type,
     * and false if it does not.
     */
    public boolean requiresXmlDataType() {
        return sourceConnectorPanel.requiresXmlDataType();
    }

    /**
     * Check if the source data type is required to be XML, and set it
     * if necessary.
     */
    public void checkAndSetXmlDataType() {
        if (requiresXmlDataType() && !currentChannel.getSourceConnector().getTransformer().getInboundDataType().equals(DataTypeFactory.XML)) {
            Properties defaultProperties = MapUtils.toProperties(DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(DataTypeFactory.XML));

            currentChannel.getSourceConnector().getTransformer().setInboundDataType(DataTypeFactory.XML);
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMetaDataButton;
    private com.mirth.connect.client.ui.components.MirthButton addTagButton;
    private com.mirth.connect.client.ui.components.MirthComboBox attachmentComboBox;
    private javax.swing.JLabel attachmentLabel;
    private javax.swing.JButton attachmentPropertiesButton;
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
    private javax.swing.JRadioButton metadataPruningOffRadio;
    private javax.swing.JRadioButton metadataPruningOnRadio;
    private javax.swing.JLabel performanceLabel;
    private com.mirth.connect.client.ui.components.MirthCheckBox removeContentCheckbox;
    private com.mirth.connect.client.ui.ScriptPanel scripts;
    private javax.swing.JPanel source;
    private javax.swing.JScrollPane sourceConnectorPane;
    private com.mirth.connect.client.ui.panels.connectors.ConnectorPanel sourceConnectorPanel;
    private com.mirth.connect.client.ui.components.MirthComboBox sourceSourceDropdown;
    private javax.swing.JLabel sourceSourceLabel;
    private javax.swing.JLabel storageLabel;
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

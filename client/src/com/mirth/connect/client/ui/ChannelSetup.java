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
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.mozilla.javascript.Context;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.attachments.CustomAttachmentDialog;
import com.mirth.connect.client.ui.attachments.IdentityAttachmentDialog;
import com.mirth.connect.client.ui.attachments.JavaScriptAttachmentDialog;
import com.mirth.connect.client.ui.attachments.RegexAttachmentDialog;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellEditor;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellRenderer;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.components.MirthTextPane;
import com.mirth.connect.client.ui.components.tag.FilterCompletion;
import com.mirth.connect.client.ui.components.tag.MirthTagField;
import com.mirth.connect.client.ui.components.tag.SearchFilterListener;
import com.mirth.connect.client.ui.components.tag.TagFilterCompletion;
import com.mirth.connect.client.ui.editors.filter.FilterPane;
import com.mirth.connect.client.ui.editors.transformer.TransformerPane;
import com.mirth.connect.client.ui.panels.connectors.ConnectorPanel;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.util.VariableListUtil;
import com.mirth.connect.donkey.model.channel.ConnectorPluginProperties;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.channel.DestinationConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;
import com.mirth.connect.donkey.model.channel.SourceConnectorProperties;
import com.mirth.connect.donkey.model.channel.SourceConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelMetadata;
import com.mirth.connect.model.ChannelProperties;
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.InvalidConnectorPluginProperties;
import com.mirth.connect.model.MessageStorageMode;
import com.mirth.connect.model.ResourceProperties;
import com.mirth.connect.model.Rule;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.model.attachments.AttachmentHandlerType;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.filter.SearchFilter;
import com.mirth.connect.model.filter.SearchFilterParser;
import com.mirth.connect.model.util.JavaScriptConstants;
import com.mirth.connect.plugins.ChannelTabPlugin;
import com.mirth.connect.util.JavaScriptSharedUtil;
import com.mirth.connect.util.PropertyVerifier;

import net.miginfocom.swing.MigLayout;

/** The channel editor panel. Majority of the client application */
public class ChannelSetup extends JPanel {
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
    private static final int SCRIPTS_TAB_INDEX = 3;

    public Channel currentChannel;
    public Map<Integer, Map<String, String>> resourceIds = new HashMap<Integer, Map<String, String>>();
    public int defaultQueueBufferSize = 1000;
    public int lastModelIndex = -1;
    public TransformerPane transformerPane = new TransformerPane();
    public FilterPane filterPane = new FilterPane();

    private Frame parent;
    private String saveGroupId;
    private boolean isDeleting = false;
    private boolean loadingChannel = false;
    private boolean channelValidationFailed = false;

    private int previousTab = -1;

    public ChannelSetup() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        initToolTips();
        initLayout();
    }

    public void closePopupWindow() {
        tagsField.closePopupWindow();
    }

    private void saveChannelTags() {
        currentChannel.getExportData().getChannelTags().clear();

        List<SearchFilter> filters = SearchFilterParser.parse(tagsField.getTags(), parent.getCachedChannelTags());

        if (CollectionUtils.isEmpty(filters)) {
            for (ChannelTag channelTag : parent.getCachedChannelTags()) {
                channelTag.getChannelIds().remove(currentChannel.getId());
            }
        } else {
            List<String> newTagNames = new ArrayList<String>();

            for (SearchFilter filter : filters) {
                List<String> tagNames = filter.getValues();

                for (ChannelTag channelTag : parent.getCachedChannelTags()) {
                    boolean found = false;

                    for (Iterator<String> it = tagNames.iterator(); it.hasNext();) {
                        String tagName = it.next();

                        if (StringUtils.equalsIgnoreCase(channelTag.getName(), tagName)) {
                            // Matching tag found
                            found = true;
                            channelTag.getChannelIds().add(currentChannel.getId());
                            currentChannel.getExportData().getChannelTags().add(channelTag);
                            it.remove();
                            break;
                        }
                    }

                    if (!found) {
                        channelTag.getChannelIds().remove(currentChannel.getId());
                    }
                }

                newTagNames.addAll(tagNames);
            }

            // New tags
            Map<String, Color> tagColors = tagsField.getTagColors();
            for (String tagName : newTagNames) {
                Set<String> channelIds = new HashSet<String>();
                channelIds.add(currentChannel.getId());

                Color tagColor = tagColors.get(tagName);
                if (tagColor == null) {
                    tagColor = Color.LIGHT_GRAY;
                }

                ChannelTag newTag = new ChannelTag(UUID.randomUUID().toString(), tagName, channelIds, tagColor);
                currentChannel.getExportData().getChannelTags().add(newTag);
                parent.getCachedChannelTags().add(newTag);
            }
        }
    }

    private void updateMetaDataTable() {
        DefaultTableModel model = (DefaultTableModel) metaDataTable.getModel();
        model.setNumRows(0);

        for (MetaDataColumn column : currentChannel.getProperties().getMetaDataColumns()) {
            model.addRow(new Object[] { column.getName(), column.getType(),
                    column.getMappingName() });
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

    /**
     * Shows the popup menu when the trigger button (right-click) has been pushed.
     */
    private void checkSelectionAndPopupMenu(MouseEvent evt) {
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
    private void showChannelEditPopupMenu(MouseEvent evt) {
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
                    tableData[i][0] = new CellData(new ImageIcon(Frame.class.getResource("images/bullet_blue.png")), UIConstants.ENABLED_STATUS);
                } else {
                    tableData[i][0] = new CellData(new ImageIcon(Frame.class.getResource("images/bullet_black.png")), UIConstants.DISABLED_STATUS);
                }
                tableData[i][1] = connector.getName();
                tableData[i][2] = connector.getMetaDataId();
                tableData[i][3] = new ConnectorTypeData(destinationConnectors.get(i).getTransportName());

                if (i > 0 && !connector.isWaitForPrevious()) {
                    chain++;
                }

                tableData[i][4] = chain;
            } else {

                if (destinationConnectors.get(i).isEnabled()) {
                    tableData[i][0] = new CellData(new ImageIcon(Frame.class.getResource("images/bullet_blue.png")), UIConstants.ENABLED_STATUS);
                } else {
                    tableData[i][0] = new CellData(new ImageIcon(Frame.class.getResource("images/bullet_black.png")), UIConstants.DISABLED_STATUS);
                }
                tableData[i][1] = destinationConnectors.get(i).getName();
                tableData[i][2] = destinationConnectors.get(i).getMetaDataId();
                tableData[i][3] = new ConnectorTypeData(destinationConnectors.get(i).getTransportName());

                if (i > 0 && !destinationConnectors.get(i).isWaitForPrevious()) {
                    chain++;
                }

                tableData[i][4] = chain;
            }
        }

        ((RefreshTableModel) destinationTable.getModel()).refreshDataVector(tableData);

        destinationTable.requestFocus();

        // Checks to see what to set the new row selection to based on
        // last index and if a new destination was added.
        int last = lastModelIndex;

        // Select each destination in turn so that the connector types can be decorated
        for (int row = 0; row < destinationTable.getRowCount(); row++) {
            destinationTable.setRowSelectionInterval(row, row);
            destinationConnectorPanel.decorateConnectorType();
        }

        if (addNew) {
            destinationTable.setRowSelectionInterval(destinationTable.getRowCount() - 1, destinationTable.getRowCount() - 1);
        } else if (last == -1) {
            destinationTable.setRowSelectionInterval(0, 0); // Makes sure the event is called when the table is created.
        } else if (last == destinationTable.getRowCount()) {
            destinationTable.setRowSelectionInterval(last - 1, last - 1);
        } else {
            destinationTable.setRowSelectionInterval(last, last);
        }
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
                    destinationConnectorTypeComboBox.setSelectedItem(destinationConnectors.get(i).getTransportName());
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

        Set<FilterCompletion> channelTags = new HashSet<FilterCompletion>();
        for (ChannelTag channelTag : parent.channelPanel.getCachedChannelTags()) {
            channelTags.add(new TagFilterCompletion(channelTag));
        }
        tagsField.update(channelTags, true, true);
        tagsField.setFocus(false);

        channelValidationFailed = false;
        lastModelIndex = -1;
        currentChannel = channel;
        saveGroupId = null;
        setResourceIds();
        setChannelTags();

//        PropertyVerifier.checkConnectorProperties(currentChannel, parent.getConnectorMetaData());

        sourceConnectorTypeComboBox.setModel(new DefaultComboBoxModel(LoadedExtensions.getInstance().getSourceConnectors().keySet().toArray()));
        destinationConnectorTypeComboBox.setModel(new DefaultComboBoxModel(LoadedExtensions.getInstance().getDestinationConnectors().keySet().toArray()));

        try {
            ServerSettings serverSettings = parent.mirthClient.getServerSettings();
            if (serverSettings.getQueueBufferSize() != null && serverSettings.getQueueBufferSize() > 0) {
                defaultQueueBufferSize = serverSettings.getQueueBufferSize();
            }
        } catch (ClientException e) {
        }

        loadChannelInfo();
        makeDestinationTable(false);
        saveDestinationPanel();
        updateMetaDataTable();
        setDestinationVariableList();
        loadingChannel = false;
        channelView.setSelectedIndex(0);

        sourceConnectorPanel.updateQueueWarning(currentChannel.getProperties().getMessageStorageMode());
        destinationConnectorPanel.updateQueueWarning(currentChannel.getProperties().getMessageStorageMode());
        updateResourceIds();

        List<ChannelTag> tags = new ArrayList<ChannelTag>();
        for (ChannelTag channelTag : parent.channelPanel.getCachedChannelTags()) {
            if (channelTag.getChannelIds().contains(channel.getId())) {
                tags.add(channelTag);
            }
        }

        Collections.sort(tags, new Comparator<ChannelTag>() {
            @Override
            public int compare(ChannelTag tag1, ChannelTag tag2) {
                return tag1.getName().compareTo(tag2.getName());
            }

        });

        tagsField.setChannelTags(tags);
    }

    /**
     * Adds a new channel that is passed in and then sets the overall panel to edit that channel.
     */
    public void addChannel(Channel channel, String groupId) {
        loadingChannel = true;
        lastModelIndex = -1;
        currentChannel = channel;
        saveGroupId = groupId;

        sourceConnectorTypeComboBox.setModel(new DefaultComboBoxModel(LoadedExtensions.getInstance().getSourceConnectors().keySet().toArray()));
        destinationConnectorTypeComboBox.setModel(new DefaultComboBoxModel(LoadedExtensions.getInstance().getDestinationConnectors().keySet().toArray()));

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

        try {
            ServerSettings serverSettings = parent.mirthClient.getServerSettings();
            currentChannel.getProperties().setMetaDataColumns(serverSettings.getDefaultMetaDataColumns());
            if (serverSettings.getQueueBufferSize() != null && serverSettings.getQueueBufferSize() > 0) {
                defaultQueueBufferSize = serverSettings.getQueueBufferSize();
            }
        } catch (ClientException e) {
            parent.alertThrowable(parent, e, "Error loading default metadata columns: " + e.getMessage());
        }

        loadChannelInfo();
        makeDestinationTable(true);
        updateMetaDataTable();

        tagsField.clear();
        Set<FilterCompletion> channelTags = new HashSet<FilterCompletion>();
        for (ChannelTag channelTag : parent.channelPanel.getCachedChannelTags()) {
            channelTags.add(new TagFilterCompletion(channelTag));
        }
        tagsField.update(channelTags, true, false);

        setDestinationVariableList();
        loadingChannel = false;
        channelView.setSelectedIndex(0);
        nameField.requestFocus();
        parent.setSaveEnabled(true);

        Map<String, String> sourceResourceIds = new LinkedHashMap<String, String>();
        sourceResourceIds.put(ResourceProperties.DEFAULT_RESOURCE_ID, ResourceProperties.DEFAULT_RESOURCE_NAME);
        ((SourceConnectorPropertiesInterface) currentChannel.getSourceConnector().getProperties()).getSourceConnectorProperties().setResourceIds(sourceResourceIds);

        for (Connector destinationConnector : currentChannel.getDestinationConnectors()) {
            Map<String, String> destinationResourceIds = new LinkedHashMap<String, String>();
            destinationResourceIds.put(ResourceProperties.DEFAULT_RESOURCE_ID, ResourceProperties.DEFAULT_RESOURCE_NAME);
            ((DestinationConnectorPropertiesInterface) destinationConnector.getProperties()).getDestinationConnectorProperties().setResourceIds(destinationResourceIds);
        }
        setResourceIds();
    }

    private void setChannelTags() {
        currentChannel.getExportData().getChannelTags().clear();

        Set<ChannelTag> channelTags = parent.channelPanel.getCachedChannelTags();
        if (channelTags != null) {
            List<ChannelTag> currentTags = new ArrayList<ChannelTag>();

            for (ChannelTag channelTag : channelTags) {
                if (channelTag.getChannelIds().contains(currentChannel.getId())) {
                    currentTags.add(channelTag);
                }
            }

            currentChannel.getExportData().setChannelTags(currentTags);
        }
    }

    /**
     * Update the resource map with values from the current channel model.
     */
    private void setResourceIds() {
        resourceIds = new HashMap<Integer, Map<String, String>>();
        resourceIds.put(null, currentChannel.getProperties().getResourceIds());
        if (currentChannel.getSourceConnector() != null && currentChannel.getSourceConnector().getProperties() != null) {
            resourceIds.put(0, ((SourceConnectorPropertiesInterface) currentChannel.getSourceConnector().getProperties()).getSourceConnectorProperties().getResourceIds());
        }
        for (Connector destinationConnector : currentChannel.getDestinationConnectors()) {
            if (destinationConnector.getProperties() != null) {
                resourceIds.put(destinationConnector.getMetaDataId(), ((DestinationConnectorPropertiesInterface) destinationConnector.getProperties()).getDestinationConnectorProperties().getResourceIds());
            }
        }
    }

    /**
     * Update resources in the current channel model with values from the cached map.
     */
    private void updateResourceIds() {
        Map<String, String> sourceResourceIds = resourceIds.get(0);
        if (sourceResourceIds == null) {
            sourceResourceIds = new LinkedHashMap<String, String>();
        }
        ((SourceConnectorPropertiesInterface) currentChannel.getSourceConnector().getProperties()).getSourceConnectorProperties().setResourceIds(sourceResourceIds);

        for (Connector destinationConnector : currentChannel.getDestinationConnectors()) {
            Map<String, String> destinationResourceIds = resourceIds.get(destinationConnector.getMetaDataId());
            if (destinationResourceIds == null) {
                destinationResourceIds = new LinkedHashMap<String, String>();
            }
            ((DestinationConnectorPropertiesInterface) destinationConnector.getProperties()).getDestinationConnectorProperties().setResourceIds(destinationResourceIds);
        }
    }

    private void setLastModified() {
        currentChannel.getExportData().getMetadata().setLastModified(Calendar.getInstance());
    }

    private void updateChannelId() {
        channelIdField.setText("Id: " + currentChannel.getId());
    }

    private void updateRevision() {
        revisionLabel.setText("Revision: " + currentChannel.getRevision());
    }

    private void updateLastModified() {
        lastModifiedLabel.setText("Last Modified: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentChannel.getExportData().getMetadata().getLastModified().getTime()));
    }

    /** Load all of the saved channel information into the channel editor */
    private void loadChannelInfo() {
        boolean enabled = parent.isSaveEnabled();
        ChannelProperties properties = currentChannel.getProperties();
        ChannelMetadata metadata = currentChannel.getExportData().getMetadata();
        parent.setPanelName("Edit Channel - " + currentChannel.getName());
        nameField.setText(currentChannel.getName());
        summaryDescriptionText.setText(currentChannel.getDescription());
        updateChannelId();
        updateRevision();
        updateLastModified();

        if (currentChannel.getExportData().getMetadata().isEnabled()) {
            summaryEnabledCheckBox.setSelected(true);
        } else {
            summaryEnabledCheckBox.setSelected(false);
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

        if (currentChannel.getUndeployScript() != null) {
            scriptMap.put(ScriptPanel.UNDEPLOY_SCRIPT, currentChannel.getUndeployScript());
        } else {
            scriptMap.put(ScriptPanel.UNDEPLOY_SCRIPT, JavaScriptConstants.DEFAULT_CHANNEL_UNDEPLOY_SCRIPT);
        }

        if (currentChannel.getPostprocessingScript() != null) {
            scriptMap.put(ScriptPanel.POSTPROCESSOR_SCRIPT, currentChannel.getPostprocessingScript());
        } else {
            scriptMap.put(ScriptPanel.POSTPROCESSOR_SCRIPT, JavaScriptConstants.DEFAULT_CHANNEL_POSTPROCESSOR_SCRIPT);
        }

        scriptsPanel.setScripts(scriptMap);
        updateScriptsPanel(scriptMap);

//        PropertyVerifier.checkChannelProperties(currentChannel);

        attachmentComboBox.setSelectedItem(AttachmentHandlerType.fromString(properties.getAttachmentProperties().getType()));

        clearGlobalChannelMapCheckBox.setSelected(properties.isClearGlobalChannelMap());
        encryptMessagesCheckBox.setSelected(properties.isEncryptData());

        // Fix dataTypes and properties not set by previous versions of Mirth Connect
        fixNullDataTypesAndProperties();

        // load message storage settings
        messageStorageSlider.setValue(properties.getMessageStorageMode().getValue());
        encryptMessagesCheckBox.setSelected(properties.isEncryptData());
        removeContentCheckBox.setSelected(properties.isRemoveContentOnCompletion());
        removeOnlyFilteredCheckBox.setSelected(properties.isRemoveOnlyFilteredOnCompletion());
        removeAttachmentsCheckBox.setSelected(properties.isRemoveAttachmentsOnCompletion());
        updateStorageMode();

        // load pruning settings
        Integer pruneMetaDataDays = metadata.getPruningSettings().getPruneMetaDataDays();
        Integer pruneContentDays = metadata.getPruningSettings().getPruneContentDays();

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

        archiveCheckBox.setSelected(metadata.getPruningSettings().isArchiveEnabled());

        sourceConnectorTypeComboBox.setSelectedItem(currentChannel.getSourceConnector().getTransportName());

        initialStateComboBox.setSelectedItem(currentChannel.getProperties().getInitialState());

        attachmentStoreCheckBox.setSelected(currentChannel.getProperties().isStoreAttachments());

        for (ChannelTabPlugin channelTabPlugin : LoadedExtensions.getInstance().getChannelTabPlugins().values()) {
            AbstractChannelTabPanel channelTabPanel = channelTabPlugin.getChannelTabPanel();
            channelView.addTab(channelTabPlugin.getPluginPointName(), channelTabPanel);
            channelTabPanel.load(currentChannel);
        }

        parent.setSaveEnabled(enabled);
    }

    private void updateScriptsPanel(Map<String, String> scriptMap) {
        int nonDefaultCount = 0;
        if (!compareScripts(scriptMap.get(ScriptPanel.PREPROCESSOR_SCRIPT), JavaScriptConstants.DEFAULT_CHANNEL_PREPROCESSOR_SCRIPT)) {
            nonDefaultCount++;
        }

        if (!compareScripts(scriptMap.get(ScriptPanel.POSTPROCESSOR_SCRIPT), JavaScriptConstants.DEFAULT_CHANNEL_POSTPROCESSOR_SCRIPT)) {
            nonDefaultCount++;
        }

        if (!compareScripts(scriptMap.get(ScriptPanel.DEPLOY_SCRIPT), JavaScriptConstants.DEFAULT_CHANNEL_DEPLOY_SCRIPT)) {
            nonDefaultCount++;
        }

        if (!compareScripts(scriptMap.get(ScriptPanel.UNDEPLOY_SCRIPT), JavaScriptConstants.DEFAULT_CHANNEL_UNDEPLOY_SCRIPT)) {
            nonDefaultCount++;
        }

        channelView.setTitleAt(SCRIPTS_TAB_INDEX, nonDefaultCount > 0 ? "Scripts (" + nonDefaultCount + ")" : "Scripts");
    }

    private boolean compareScripts(String savedScript, String defualtScript) {
        Context context = JavaScriptSharedUtil.getGlobalContextForValidation();
        try {
            String decompiledSavedScript = "";
            String decompiledDefaultScript = "";

            try {
                decompiledSavedScript = context.decompileScript(context.compileString("function doScript() {" + savedScript + "}", PlatformUI.MIRTH_FRAME.mirthClient.getGuid(), 1, null), 1);
                decompiledDefaultScript = context.decompileScript(context.compileString("function doScript() {" + defualtScript + "}", PlatformUI.MIRTH_FRAME.mirthClient.getGuid(), 1, null), 1);
            } catch (Exception e) {
                //If any script fails to compile for any reason, we can just assume they aren't equal.
                return false;
            }

            return decompiledSavedScript.equals(decompiledDefaultScript);
        } finally {
            Context.exit();
        }
    }

    public void decorateConnectorType(ConnectorTypeDecoration connectorTypeDecoration) {
        if (connectorTypeDecoration != null && connectorTypeDecoration.getMode() == Mode.DESTINATION && destinationTable.getSelectedModelIndex() >= 0) {
            ConnectorTypeData connectorTypeData = (ConnectorTypeData) destinationTable.getModel().getValueAt(destinationTable.getSelectedModelIndex(), destinationTable.getColumnModelIndex(CONNECTOR_TYPE_COLUMN_NAME));
            connectorTypeData.setDecoration(connectorTypeDecoration);
            destinationTable.getModel().setValueAt(connectorTypeData, destinationTable.getSelectedModelIndex(), destinationTable.getColumnModelIndex(CONNECTOR_TYPE_COLUMN_NAME));
        }
    }

    public String checkInvalidPluginProperties(Connector connector) {
        return checkInvalidPluginProperties(null, connector);
    }

    public String checkInvalidPluginProperties(Channel channel) {
        return checkInvalidPluginProperties(channel, null);
    }

    private String checkInvalidPluginProperties(Channel channel, Connector connector) {
        Set<String> invalidPluginPropertiesNames = new HashSet<String>();

        if (channel != null) {
            checkInvalidPluginProperties(channel.getSourceConnector(), invalidPluginPropertiesNames);
            for (Connector destinationConnector : channel.getDestinationConnectors()) {
                checkInvalidPluginProperties(destinationConnector, invalidPluginPropertiesNames);
            }
        } else {
            checkInvalidPluginProperties(connector, invalidPluginPropertiesNames);
        }

        if (!invalidPluginPropertiesNames.isEmpty()) {
            StringBuilder alertMessage = new StringBuilder("The following invalid connector plugin properties were found:\n\n");
            for (String name : invalidPluginPropertiesNames) {
                alertMessage.append(name);
                alertMessage.append('\n');
            }
            return alertMessage.toString();
        }

        return null;
    }

    private void checkInvalidPluginProperties(Connector connector, Set<String> invalidPluginPropertiesNames) {
        if (connector.getProperties().getPluginProperties() != null) {
            for (ConnectorPluginProperties pluginProperties : connector.getProperties().getPluginProperties()) {
                if (pluginProperties instanceof InvalidConnectorPluginProperties) {
                    invalidPluginPropertiesNames.add(pluginProperties.getName());
                }
            }
        }
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
                removeContentCheckBox.setEnabled(true);
                removeOnlyFilteredCheckBox.setEnabled(removeContentCheckBox.isSelected());
                removeAttachmentsCheckBox.setEnabled(true);
                break;

            case PRODUCTION:
                storageModeLabel.setText("Production");
                contentLabel.setText("Content: Raw, Encoded, Sent, Response, Maps");
                metadataLabel.setText("Metadata: All");
                durableStatusLabel.setText("On");
                durableStatusLabel.setForeground(new Color(0, 130, 0));
                messageStorageProgressBar.setValue(25);
                encryptMessagesCheckBox.setEnabled(true);
                removeContentCheckBox.setEnabled(true);
                removeOnlyFilteredCheckBox.setEnabled(removeContentCheckBox.isSelected());
                removeAttachmentsCheckBox.setEnabled(true);
                break;

            case RAW:
                storageModeLabel.setText("Raw");
                contentLabel.setText("Content: Raw");
                metadataLabel.setText("Metadata: All");
                durableStatusLabel.setText("Reprocess only");
                durableStatusLabel.setForeground(new Color(255, 102, 0));
                messageStorageProgressBar.setValue(60);
                encryptMessagesCheckBox.setEnabled(true);
                removeContentCheckBox.setEnabled(true);
                removeOnlyFilteredCheckBox.setEnabled(removeContentCheckBox.isSelected());
                removeAttachmentsCheckBox.setEnabled(true);
                break;

            case METADATA:
                storageModeLabel.setText("Metadata");
                contentLabel.setText("Content: None");
                metadataLabel.setText("Metadata: All");
                durableStatusLabel.setText("Off");
                durableStatusLabel.setForeground(new Color(130, 0, 0));
                messageStorageProgressBar.setValue(65);
                encryptMessagesCheckBox.setEnabled(false);
                removeContentCheckBox.setEnabled(false);
                removeOnlyFilteredCheckBox.setEnabled(false);
                removeAttachmentsCheckBox.setEnabled(false);
                break;

            case DISABLED:
                storageModeLabel.setText("Disabled");
                contentLabel.setText("Content: None");
                metadataLabel.setText("Metadata: None");
                durableStatusLabel.setText("Off");
                durableStatusLabel.setForeground(new Color(130, 0, 0));
                messageStorageProgressBar.setValue(100);
                encryptMessagesCheckBox.setEnabled(false);
                removeContentCheckBox.setEnabled(false);
                removeOnlyFilteredCheckBox.setEnabled(false);
                removeAttachmentsCheckBox.setEnabled(false);
                break;
        }

        // if content encryption is enabled, subtract a percentage from the progress bar
        if (encryptMessagesCheckBox.isEnabled() && encryptMessagesCheckBox.isSelected()) {
            messageStorageProgressBar.setValue(messageStorageProgressBar.getValue() - 3);
        }

        // if the "remove content on completion" option is enabled, subtract a percentage from the progress bar
        if (removeContentCheckBox.isEnabled() && removeContentCheckBox.isSelected()) {
            messageStorageProgressBar.setValue(messageStorageProgressBar.getValue() - 3);
        }

        // if the "remove content on completion" option is enabled, subtract a percentage from the progress bar
        if (removeAttachmentsCheckBox.isEnabled() && removeAttachmentsCheckBox.isSelected()) {
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
        ConnectorProperties connectorProperties = sourceConnectorPanel.getProperties();

        if (connectorProperties != null && connectorProperties instanceof SourceConnectorPropertiesInterface) {
            SourceConnectorProperties sourceConnectorProperties = ((SourceConnectorPropertiesInterface) connectorProperties).getSourceConnectorProperties();
            sourceQueueEnabled = !sourceConnectorProperties.isRespondAfterProcessing();
        }

        boolean destinationQueueEnabled = false;

        for (Connector connector : currentChannel.getDestinationConnectors()) {
            connectorProperties = connector.getProperties();

            if (connectorProperties instanceof DestinationConnectorPropertiesInterface) {
                DestinationConnectorProperties destinationConnectorProperties = ((DestinationConnectorPropertiesInterface) connectorProperties).getDestinationConnectorProperties();

                if (destinationConnectorProperties.isQueueEnabled()) {
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
        currentChannel.setPreprocessingScript(scriptsPanel.getScripts().get(ScriptPanel.PREPROCESSOR_SCRIPT));
        currentChannel.setDeployScript(scriptsPanel.getScripts().get(ScriptPanel.DEPLOY_SCRIPT));
        currentChannel.setUndeployScript(scriptsPanel.getScripts().get(ScriptPanel.UNDEPLOY_SCRIPT));
        currentChannel.setPostprocessingScript(scriptsPanel.getScripts().get(ScriptPanel.POSTPROCESSOR_SCRIPT));

        updateScriptsPanel(getScriptsMap());
    }

    private Map<String, String> getScriptsMap() {
        LinkedHashMap<String, String> scriptMap = new LinkedHashMap<String, String>();

        scriptMap.put(ScriptPanel.PREPROCESSOR_SCRIPT, scriptsPanel.getScripts().get(ScriptPanel.PREPROCESSOR_SCRIPT));
        scriptMap.put(ScriptPanel.POSTPROCESSOR_SCRIPT, scriptsPanel.getScripts().get(ScriptPanel.POSTPROCESSOR_SCRIPT));
        scriptMap.put(ScriptPanel.DEPLOY_SCRIPT, scriptsPanel.getScripts().get(ScriptPanel.DEPLOY_SCRIPT));
        scriptMap.put(ScriptPanel.UNDEPLOY_SCRIPT, scriptsPanel.getScripts().get(ScriptPanel.UNDEPLOY_SCRIPT));

        return scriptMap;
    }

    public void saveSourcePanel() {
        currentChannel.getSourceConnector().setProperties(sourceConnectorPanel.getProperties());

        if (!loadingChannel && resourceIds.containsKey(currentChannel.getSourceConnector().getMetaDataId())) {
            ((SourceConnectorPropertiesInterface) currentChannel.getSourceConnector().getProperties()).getSourceConnectorProperties().setResourceIds(resourceIds.get(currentChannel.getSourceConnector().getMetaDataId()));
        }
    }

    public void saveDestinationPanel() {
        Connector temp;

        temp = currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex());
        temp.setProperties(destinationConnectorPanel.getProperties());

        if (!loadingChannel && temp.getProperties() != null && resourceIds.containsKey(temp.getMetaDataId())) {
            ((DestinationConnectorPropertiesInterface) temp.getProperties()).getDestinationConnectorProperties().setResourceIds(resourceIds.get(temp.getMetaDataId()));
        }
    }

    /**
     * Save all of the current channel information in the editor to the actual channel
     */
    public boolean saveChanges() {
        if (!parent.checkChannelName(nameField.getText(), currentChannel.getId())) {
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

        tagsField.createTagOnFocusLost();

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

                if (columnName.equalsIgnoreCase("MESSAGE_ID") || columnName.equalsIgnoreCase("METADATA_ID")) {
                    parent.alertWarning(parent, columnName + " is a reserved keyword and cannot be used as a column name in the custom metadata table.");
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

        boolean enabled = summaryEnabledCheckBox.isSelected();

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

        currentChannel.setName(nameField.getText());
        currentChannel.setDescription(summaryDescriptionText.getText());

        updateScripts();
        setLastModified();

        currentChannel.getProperties().setClearGlobalChannelMap(clearGlobalChannelMapCheckBox.isSelected());
        currentChannel.getProperties().setEncryptData(encryptMessagesCheckBox.isSelected());
        currentChannel.getProperties().setInitialState((DeployedState) initialStateComboBox.getSelectedItem());
        currentChannel.getProperties().setStoreAttachments(attachmentStoreCheckBox.isSelected());

        String validationMessage = checkAllForms(currentChannel);
        if (validationMessage != null) {
            enabled = false;

            // If there is an error on one of the forms, then run the
            // validation on the current form to display any errors.
            if (channelView.getSelectedComponent() == destinationsPanel) {
                // If the destination is enabled...
                if (currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex()).isEnabled()) {
                    destinationConnectorPanel.checkProperties(destinationConnectorPanel.getProperties(), true);
                }
            } else if (channelView.getSelectedComponent() == sourcePanel) {
                sourceConnectorPanel.checkProperties(sourceConnectorPanel.getProperties(), true);
            }

            summaryEnabledCheckBox.setSelected(false);

            parent.alertCustomError(this.parent, validationMessage, CustomErrorDialog.ERROR_SAVING_CHANNEL);
        }

        // Set the channel to enabled or disabled after it has been validated
        currentChannel.getExportData().getMetadata().setEnabled(enabled);

        saveChannelTags();
        saveMetaDataColumns();
        saveMessageStorage(messageStorageMode);
        saveMessagePruning();

        // Update resource names
        parent.updateResourceNames(currentChannel);

        for (ChannelTabPlugin channelTabPlugin : LoadedExtensions.getInstance().getChannelTabPlugins().values()) {
            channelTabPlugin.getChannelTabPanel().save(currentChannel);
        }

        boolean updated = false;

        try {
            // Will throw exception if the connection died or there was an exception
            // saving the channel, skipping the rest of this code.
            updated = parent.updateChannel(currentChannel, parent.channelPanel.getCachedChannelStatuses().containsKey(currentChannel.getId()));

            try {
                currentChannel = (Channel) SerializationUtils.clone(parent.channelPanel.getCachedChannelStatuses().get(currentChannel.getId()).getChannel());

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
                parent.alertThrowable(this.parent, e);
            }
        } catch (ClientException e) {
            parent.alertThrowable(this.parent, e);
        }

        sourceConnectorPanel.updateQueueWarning(currentChannel.getProperties().getMessageStorageMode());
        destinationConnectorPanel.updateQueueWarning(currentChannel.getProperties().getMessageStorageMode());

        if (updated && saveGroupId != null) {
            parent.channelPanel.addChannelToGroup(currentChannel.getId(), saveGroupId);
            saveGroupId = null;
        }

        return updated;
    }

    private void saveMessageStorage(MessageStorageMode messageStorageMode) {
        ChannelProperties properties = currentChannel.getProperties();
        properties.setMessageStorageMode(messageStorageMode);
        properties.setEncryptData(encryptMessagesCheckBox.isSelected());
        properties.setRemoveContentOnCompletion(removeContentCheckBox.isSelected());
        properties.setRemoveOnlyFilteredOnCompletion(removeOnlyFilteredCheckBox.isSelected());
        properties.setRemoveAttachmentsOnCompletion(removeAttachmentsCheckBox.isSelected());
    }

    private void saveMessagePruning() {
        ChannelMetadata metadata = currentChannel.getExportData().getMetadata();

        if (metadataPruningOffRadio.isSelected()) {
            metadata.getPruningSettings().setPruneMetaDataDays(null);
        } else {
            metadata.getPruningSettings().setPruneMetaDataDays(Integer.parseInt(metadataPruningDaysTextField.getText()));
        }

        if (contentPruningMetadataRadio.isSelected()) {
            metadata.getPruningSettings().setPruneContentDays(null);
        } else {
            metadata.getPruningSettings().setPruneContentDays(Integer.parseInt(contentPruningDaysTextField.getText()));
        }

        metadata.getPruningSettings().setArchiveEnabled(archiveCheckBox.isSelected());
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
        destinationTableScrollPane.getViewport().setViewPosition(new Point(0, destinationTable.getRowHeight() * destinationTable.getRowCount()));
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
            parent.alertThrowable(this.parent, e);
            return;
        }

        destination.setName(getNewDestinationName(destinationConnectors.size() + 1));
        currentChannel.addDestination(destination);
        // After adding the destination to the channel, make sure to update the cached resource ID map too
        resourceIds.put(destination.getMetaDataId(), ((DestinationConnectorPropertiesInterface) destination.getProperties()).getDestinationConnectorProperties().getResourceIds());
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
        if (channelView.getSelectedComponent() == destinationsPanel) {
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
            if (StringUtils.isNotBlank(validationMessage)) {
                errors += "Error in connector \"" + connector.getName() + "\" at transformer step " + step.getSequenceNumber() + " (\"" + step.getName() + "\"):\n" + validationMessage + "\n\n";
            }
        }

        if (connector.getMode() == Connector.Mode.DESTINATION) {
            for (Step step : connector.getResponseTransformer().getSteps()) {
                String validationMessage = this.transformerPane.validateStep(step);
                if (StringUtils.isNotBlank(validationMessage)) {
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

        String validationMessage = this.scriptsPanel.validateScript(channel.getDeployScript());
        if (validationMessage != null) {
            errors += "Error in channel script \"" + ScriptPanel.DEPLOY_SCRIPT + "\":\n" + validationMessage + "\n\n";
        }

        validationMessage = this.scriptsPanel.validateScript(channel.getPreprocessingScript());
        if (validationMessage != null) {
            errors += "Error in channel script \"" + ScriptPanel.PREPROCESSOR_SCRIPT + "\":\n" + validationMessage + "\n\n";
        }

        validationMessage = this.scriptsPanel.validateScript(channel.getPostprocessingScript());
        if (validationMessage != null) {
            errors += "Error in channel script \"" + ScriptPanel.POSTPROCESSOR_SCRIPT + "\":\n" + validationMessage + "\n\n";
        }

        validationMessage = this.scriptsPanel.validateScript(channel.getUndeployScript());
        if (validationMessage != null) {
            errors += "Error in channel script \"" + ScriptPanel.UNDEPLOY_SCRIPT + "\":\n" + validationMessage + "\n\n";
        }

        return errors;
    }

    public void doValidate() {
        if (sourcePanel.isVisible()) {
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
        scriptsPanel.validateCurrentScript();
    }

    public void showAttachmentPropertiesDialog(AttachmentHandlerType type) {
        AttachmentHandlerProperties attachmentHandlerProperties = currentChannel.getProperties().getAttachmentProperties();
        if (type.equals(AttachmentHandlerType.IDENTITY)) {
            new IdentityAttachmentDialog(attachmentHandlerProperties);
        } else if (type.equals(AttachmentHandlerType.REGEX)) {
            new RegexAttachmentDialog(attachmentHandlerProperties);
        } else if (type.equals(AttachmentHandlerType.DICOM)) {

        } else if (type.equals(AttachmentHandlerType.JAVASCRIPT)) {
            new JavaScriptAttachmentDialog(attachmentHandlerProperties);
        } else if (type.equals(AttachmentHandlerType.CUSTOM)) {
            new CustomAttachmentDialog(attachmentHandlerProperties);
        }
    }

    private void initComponents() {
        setBackground(UIConstants.COMBO_BOX_BACKGROUND);

        channelView = new JTabbedPane();
        channelView.setBackground(getBackground());

        channelView.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                showChannelEditPopupMenu(evt);
            }

            public void mouseReleased(MouseEvent evt) {
                showChannelEditPopupMenu(evt);
            }
        });

        channelView.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
                int selectedTab = channelView.getSelectedIndex();

                if (previousTab == SCRIPTS_TAB_INDEX && selectedTab != SCRIPTS_TAB_INDEX) {
                    updateScriptsPanel(getScriptsMap());
                }

                if (selectedTab == SCRIPTS_TAB_INDEX) {
                    scriptsPanel.updateDisplayOptions();
                }

                /*
                 * When connector-specific resources are changed and the connector panel has already
                 * been loaded, returning to the connector panel will not trigger a call to
                 * setProperties. In order to allow connector panels to take action when resources
                 * have changed, we call setVisible when the user switches tabs.
                 */
                if (selectedTab == SOURCE_TAB_INDEX && sourceConnectorPanel.getConnectorSettingsPanel() != null) {
                    sourceConnectorPanel.getConnectorSettingsPanel().setVisible(true);
                    sourceConnectorPanel.updateNextFireTime();
                } else if (selectedTab == DESTINATIONS_TAB_INDEX && destinationConnectorPanel.getConnectorSettingsPanel() != null) {
                    destinationConnectorPanel.getConnectorSettingsPanel().setVisible(true);
                }

                previousTab = selectedTab;
            }
        });

        // Summary Tab
        summaryPanel = new JPanel();
        summaryPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        summaryPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent evt) {
                summaryComponentShown(evt);
            }
        });

        // Channel Properties
        channelPropertiesPanel = new JPanel();
        channelPropertiesPanel.setBackground(summaryPanel.getBackground());
        channelPropertiesPanel.setBorder(BorderFactory.createTitledBorder("Channel Properties"));

        nameLabel = new JLabel("Name:");

        nameField = new MirthTextField();
        nameField.setDocument(new MirthFieldConstraints(40, false, true, true));
        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                summaryNameFieldKeyReleased(evt);
            }
        });

        summaryEnabledCheckBox = new MirthCheckBox("Enabled");
        summaryEnabledCheckBox.setBackground(channelPropertiesPanel.getBackground());
        summaryEnabledCheckBox.setText("Enabled");

        channelIdField = new JTextField("Id: ");
        channelIdField.setEditable(false);
        channelIdField.setBackground(channelPropertiesPanel.getBackground());
        channelIdField.setHorizontalAlignment(JTextField.RIGHT);
        channelIdField.setText("Id: ");
        channelIdField.setBorder(null);

        dataTypesLabel = new JLabel("Data Types:");

        dataTypesButton = new JButton("Set Data Types");
        dataTypesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                changeDataTypesButtonActionPerformed(evt);
            }
        });

        clearGlobalChannelMapCheckBox = new MirthCheckBox("Clear global channel map on deploy");
        clearGlobalChannelMapCheckBox.setBackground(channelPropertiesPanel.getBackground());

        revisionLabel = new JLabel("Revision: ");
        revisionLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        dependenciesLabel = new JLabel("Dependencies:");

        dependenciesButton = new JButton("Set Dependencies");
        dependenciesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setDependenciesButtonActionPerformed(evt);
            }
        });

        lastModifiedLabel = new JLabel("Last Modified: ");
        lastModifiedLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        initialStateLabel = new JLabel("Initial State:");
        initialStateComboBox = new MirthComboBox<DeployedState>();
        initialStateComboBox.setModel(new DefaultComboBoxModel<DeployedState>(new DeployedState[] {
                DeployedState.STARTED, DeployedState.PAUSED, DeployedState.STOPPED }));

        attachmentLabel = new JLabel("Attachment:");

        attachmentComboBox = new MirthComboBox<AttachmentHandlerType>();
        attachmentComboBox.setModel(new DefaultComboBoxModel<AttachmentHandlerType>(AttachmentHandlerType.values()));
        attachmentComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                attachmentComboBoxActionPerformed(evt);
            }
        });

        attachmentPropertiesButton = new JButton("Properties");
        attachmentPropertiesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                attachmentPropertiesButtonActionPerformed(evt);
            }
        });

        attachmentStoreCheckBox = new MirthCheckBox("Store Attachments");
        attachmentStoreCheckBox.setBackground(channelPropertiesPanel.getBackground());
        attachmentStoreCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent evt) {
                attachmentStoreCheckBoxItemStateChanged(evt);
            }
        });

        attachmentWarningLabel = new JLabel("Attachments will be extracted but not stored or reattached.");
        attachmentWarningLabel.setForeground(new Color(255, 0, 0));

        tagsLabel = new JLabel("Tags:");

        Set<FilterCompletion> tags = new HashSet<FilterCompletion>();
        for (ChannelTag tag : parent.getCachedChannelTags()) {
            tags.add(new TagFilterCompletion(tag));
        }

        tagsField = new MirthTagField(null, true, tags);
        tagsField.addUpdateSearchListener(new SearchFilterListener() {
            @Override
            public void doSearch(final String filterString) {}

            @Override
            public void doDelete(String filterString) {
                parent.setSaveEnabled(true);
            }
        });

        // Message Storage
        messageStoragePanel = new JPanel();
        messageStoragePanel.setBackground(summaryPanel.getBackground());
        messageStoragePanel.setBorder(BorderFactory.createTitledBorder("Message Storage"));
        messageStoragePanel.setForeground(new Color(0, 102, 0));

        messageStorageSlider = new JSlider();
        messageStorageSlider.setBackground(messageStoragePanel.getBackground());
        messageStorageSlider.setMajorTickSpacing(1);
        messageStorageSlider.setMinimum(1);
        messageStorageSlider.setMaximum(5);
        messageStorageSlider.setOrientation(JSlider.VERTICAL);
        messageStorageSlider.setPaintTicks(true);
        messageStorageSlider.setSnapToTicks(true);
        messageStorageSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                messageStorageSliderStateChanged(evt);
            }
        });

        storageModeLabel = new JLabel("Development");
        storageModeLabel.setFont(new Font("Dialog", 1, 14));

        contentLabel = new JLabel("Content: ");

        metadataLabel = new JLabel("Metadata:");

        durableLabel = new JLabel("Durable Message Delivery:");

        durableStatusLabel = new JLabel("On");
        durableStatusLabel.setForeground(new Color(0, 102, 0));

        performanceLabel = new JLabel("Performance:");

        messageStorageProgressBar = new JProgressBar();
        messageStorageProgressBar.setValue(10);

        encryptMessagesCheckBox = new MirthCheckBox("Encrypt message content");
        encryptMessagesCheckBox.setBackground(messageStoragePanel.getBackground());
        encryptMessagesCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                encryptMessagesCheckBoxActionPerformed(evt);
            }
        });

        removeContentCheckBox = new MirthCheckBox("Remove content on completion");
        removeContentCheckBox.setBackground(messageStoragePanel.getBackground());
        removeContentCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                removeContentCheckboxActionPerformed(evt);
            }
        });

        removeAttachmentsCheckBox = new MirthCheckBox("Remove attachments on completion");
        removeAttachmentsCheckBox.setBackground(messageStoragePanel.getBackground());
        removeAttachmentsCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                removeAttachmentsCheckboxActionPerformed(evt);
            }
        });

        removeOnlyFilteredCheckBox = new MirthCheckBox("Filtered only");
        removeOnlyFilteredCheckBox.setBackground(messageStoragePanel.getBackground());

        queueWarningLabel = new JLabel("<html>Disable source & destination queueing before using this mode</html>");
        queueWarningLabel.setFont(new Font("Dialog", 0, 11));
        queueWarningLabel.setForeground(new Color(255, 0, 0));
        queueWarningLabel.setVerticalAlignment(SwingConstants.TOP);

        // Message Pruning
        messagePruningPanel = new JPanel();
        messagePruningPanel.setBackground(summaryPanel.getBackground());
        messagePruningPanel.setBorder(BorderFactory.createTitledBorder("Message Pruning"));

        metadataPruningLabel = new JLabel("Metadata:");
        ButtonGroup metadataPruningButtonGroup = new ButtonGroup();

        metadataPruningOffRadio = new JRadioButton("Store indefinitely");
        metadataPruningOffRadio.setBackground(messagePruningPanel.getBackground());
        metadataPruningOffRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                metadataPruningOffRadioActionPerformed(evt);
            }
        });
        metadataPruningButtonGroup.add(metadataPruningOffRadio);

        metadataPruningOnRadio = new JRadioButton("Prune metadata older than");
        metadataPruningOnRadio.setBackground(messagePruningPanel.getBackground());
        metadataPruningOnRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                metadataPruningOnRadioActionPerformed(evt);
            }
        });
        metadataPruningButtonGroup.add(metadataPruningOnRadio);

        metadataPruningDaysTextField = new MirthTextField();
        metadataPruningDaysTextField.setDocument(new MirthFieldConstraints(3, false, false, true));

        metadataDaysLabel = new JLabel("days");

        contentPruningLabel = new JLabel("Content:");
        ButtonGroup contentPruningButtonGroup = new ButtonGroup();

        contentPruningMetadataRadio = new JRadioButton("Prune when message metadata is removed");
        contentPruningMetadataRadio.setBackground(messagePruningPanel.getBackground());
        contentPruningMetadataRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                contentPruningMetadataRadioActionPerformed(evt);
            }
        });
        contentPruningButtonGroup.add(contentPruningMetadataRadio);

        contentPruningDaysRadio = new JRadioButton("Prune content older than");
        contentPruningDaysRadio.setBackground(messagePruningPanel.getBackground());
        contentPruningDaysRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                contentPruningDaysRadioActionPerformed(evt);
            }
        });
        contentPruningButtonGroup.add(contentPruningDaysRadio);

        contentPruningDaysTextField = new MirthTextField();
        contentPruningDaysTextField.setDocument(new MirthFieldConstraints(3, false, false, true));

        contentDaysLabel = new JLabel("days");

        archiveCheckBox = new MirthCheckBox("Allow message archiving");
        archiveCheckBox.setBackground(messagePruningPanel.getBackground());

        pruningWarningLabel = new JLabel("(incomplete, errored, and queued messages will not be pruned)");

        // Custom Metadata
        customMetadataPanel = new JPanel();
        customMetadataPanel.setBackground(summaryPanel.getBackground());
        customMetadataPanel.setBorder(BorderFactory.createTitledBorder("Custom Metadata"));

        metaDataTable = new MirthTable();

        DefaultTableModel model = new DefaultTableModel(new Object[][] {}, new String[] {
                METADATA_NAME_COLUMN_NAME, METADATA_TYPE_COLUMN_NAME,
                METADATA_MAPPING_COLUMN_NAME }) {
            @Override
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
        metaDataTable.setModel(model);

        metaDataTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        metaDataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        metaDataTable.setDragEnabled(false);
        metaDataTable.setSortable(false);
        metaDataTable.getTableHeader().setReorderingAllowed(false);

        metaDataTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                deleteMetaDataButton.setEnabled(metaDataTable.getSelectedRow() != -1);
            }
        });

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            metaDataTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

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

        metaDataTable.getColumnModel().getColumn(metaDataTable.getColumnModel().getColumnIndex(METADATA_NAME_COLUMN_NAME)).setCellEditor(new AlphaNumericCellEditor());
        metaDataTable.getColumnModel().getColumn(metaDataTable.getColumnModel().getColumnIndex(METADATA_MAPPING_COLUMN_NAME)).setCellEditor(new AlphaNumericCellEditor());

        TableColumn column = metaDataTable.getColumnModel().getColumn(metaDataTable.getColumnModel().getColumnIndex(METADATA_TYPE_COLUMN_NAME));
        column.setCellRenderer(new MirthComboBoxTableCellRenderer(MetaDataColumnType.values()));
        column.setCellEditor(new MirthComboBoxTableCellEditor(metaDataTable, MetaDataColumnType.values(), 1, false, null));
        column.setMinWidth(100);
        column.setMaxWidth(100);

        metaDataScrollPane = new JScrollPane(metaDataTable);

        addMetaDataButton = new JButton("Add");
        addMetaDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                addMetaDataButtonActionPerformed(evt);
            }
        });

        deleteMetaDataButton = new JButton("Delete");
        deleteMetaDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                deleteMetaDataButtonActionPerformed(evt);
            }
        });

        revertMetaDataButton = new JButton("Revert");
        revertMetaDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                revertMetaDataButtonActionPerformed(evt);
            }
        });

        // Channel Description
        descriptionPanel = new JPanel();
        descriptionPanel.setBackground(summaryPanel.getBackground());
        descriptionPanel.setBorder(BorderFactory.createTitledBorder("Channel Description"));

        summaryDescriptionText = new MirthTextPane();

        summaryDescriptionScrollPane = new JScrollPane(summaryDescriptionText);

        // Source Panel
        sourcePanel = new JPanel();
        sourcePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        sourcePanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent evt) {
                sourceComponentShown(evt);
            }
        });

        sourceConnectorTypeLabel = new JLabel("Connector Type:");

        sourceConnectorTypeComboBox = new MirthComboBox<String>();
        sourceConnectorTypeComboBox.setMaximumRowCount(20);
        sourceConnectorTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                sourceSourceDropdownActionPerformed(evt);
            }
        });

        sourceConnectorPanel = new ConnectorPanel();
        sourceConnectorPanel.setChannelSetup(this);

        sourceConnectorScrollPane = new JScrollPane(sourceConnectorPanel);
        sourceConnectorScrollPane.setBorder(null);

        // Destinations Panel
        destinationsPanel = new JPanel();
        destinationsPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        destinationsPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent evt) {
                destinationComponentShown(evt);
            }
        });

        destinationTable = new MirthTable();

        destinationTable.setModel(new RefreshTableModel(new String[] { STATUS_COLUMN_NAME,
                DESTINATION_COLUMN_NAME, METADATA_COLUMN_NAME, CONNECTOR_TYPE_COLUMN_NAME,
                DESTINATION_CHAIN_COLUMN_NAME }, 0) {

            boolean[] canEdit = new boolean[] { false, true, false, false, false };

            @Override
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

        // Set the cell renderer for the destination connector type
        destinationTable.getColumnExt(CONNECTOR_TYPE_COLUMN_NAME).setCellRenderer(new ConnectorTypeCellRenderer());

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

        // This action is called when a new selection is made on the destination table.
        destinationTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (!evt.getValueIsAdjusting()) {
                    if (lastModelIndex != -1 && lastModelIndex != destinationTable.getRowCount() && !isDeleting) {
                        Connector destinationConnector = currentChannel.getDestinationConnectors().get(lastModelIndex);

                        ConnectorProperties props = destinationConnectorPanel.getProperties();
                        ((DestinationConnectorPropertiesInterface) props).getDestinationConnectorProperties().setResourceIds(resourceIds.get(destinationConnector.getMetaDataId()));
                        destinationConnector.setProperties(props);
                    }

                    if (!loadConnector()) {
                        if (lastModelIndex == destinationTable.getRowCount()) {
                            destinationTable.setRowSelectionInterval(lastModelIndex - 1, lastModelIndex - 1);
                        } else if (lastModelIndex >= 0 && lastModelIndex < destinationTable.getRowCount()) {
                            destinationTable.setRowSelectionInterval(lastModelIndex, lastModelIndex);
                        } else {
                            destinationTable.setRowSelectionInterval(0, 0);
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

        // Mouse listener for trigger-button popup on the table.
        destinationTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }
        });

        // Key Listener trigger for DEL
        destinationTable.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    parent.doDeleteDestination();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}

            @Override
            public void keyTyped(KeyEvent e) {}
        });

        destinationTable.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                destinationTableScrollPane.getMouseWheelListeners()[0].mouseWheelMoved(e);
            }
        });

        destinationTableScrollPane = new JScrollPane(destinationTable);
        destinationTableScrollPane.setWheelScrollingEnabled(true);

        destinationConnectorTypeLabel = new JLabel("Connector Type:");

        destinationConnectorTypeComboBox = new MirthComboBox<String>();
        destinationConnectorTypeComboBox.setMaximumRowCount(20);
        destinationConnectorTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                destinationSourceDropdownActionPerformed(evt);
            }
        });

        waitForPreviousCheckbox = new MirthCheckBox("Wait for previous destination");
        waitForPreviousCheckbox.setBackground(destinationsPanel.getBackground());
        waitForPreviousCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                waitForPreviousCheckboxActionPerformed(evt);
            }
        });

        destinationConnectorPanel = new ConnectorPanel();
        destinationConnectorPanel.setChannelSetup(this);

        destinationConnectorScrollPane = new JScrollPane(destinationConnectorPanel);
        destinationConnectorScrollPane.setBorder(null);

        destinationVariableList = new VariableList();

        // Scripts Panel
        scriptsPanel = new ScriptPanel(false);
        scriptsPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        scriptsPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent evt) {
                scriptsComponentShown(evt);
            }
        });
    }

    private void initToolTips() {
        summaryEnabledCheckBox.setToolTipText("Enable this channel so that it can be deployed.");
        clearGlobalChannelMapCheckBox.setToolTipText("Clear the global channel map on both single channel deploy and a full redeploy.");
        attachmentStoreCheckBox.setToolTipText("If checked, attachments will be stored in the database and available for reattachment.");
        encryptMessagesCheckBox.setToolTipText("<html>Encrypt message content that is stored in the database. Messages that<br>are stored while this option is enabled will still be viewable in the<br>message browser, but the content will not be searchable.</html>");
        removeContentCheckBox.setToolTipText("<html>Remove message content once the message has completed processing.<br/>Not applicable for messages that are errored or queued.</html>");
        removeAttachmentsCheckBox.setToolTipText("<html>Remove message attachments once the message has completed processing.<br/>Not applicable for messages that are errored or queued.</html>");
        removeOnlyFilteredCheckBox.setToolTipText("<html>If checked, only content for filtered connector messages will be removed.</html>");
        archiveCheckBox.setToolTipText("<html>If checked and the data pruner and archiver are enabled, messages<br />in this channel will be archived before being pruned.</html>");
        revertMetaDataButton.setToolTipText("<html>Revert the custom metadata settings to the last save.<br>This option allows you to undo your metadata changes without affecting the rest of the channel.</html>");
        waitForPreviousCheckbox.setToolTipText("<html>Wait for the previous destination to finish before processing the current destination.<br/>Each destination connector for which this is not selected marks the beginning of a destination chain,<br/>such that all chains execute asynchronously, but each destination within a particular chain executes in order.<br/>This option has no effect on the first destination connector, which always marks the beginning of the first chain.</html>");
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));

        channelPropertiesPanel.setLayout(new MigLayout("insets 0 10 10 10, novisualpadding, hidemode 3, fill, gap 6", "[]12[]12[][grow]"));
        channelPropertiesPanel.add(nameLabel, "right");
        channelPropertiesPanel.add(nameField, "w 185!");
        channelPropertiesPanel.add(summaryEnabledCheckBox);
        channelPropertiesPanel.add(channelIdField, "right");
        channelPropertiesPanel.add(dataTypesLabel, "newline, right");
        channelPropertiesPanel.add(dataTypesButton, "w 108!");
        channelPropertiesPanel.add(clearGlobalChannelMapCheckBox);
        channelPropertiesPanel.add(revisionLabel, "right");
        channelPropertiesPanel.add(dependenciesLabel, "newline, right");
        channelPropertiesPanel.add(dependenciesButton, "w 108!");
        channelPropertiesPanel.add(lastModifiedLabel, "skip 1, right");
        channelPropertiesPanel.add(initialStateLabel, "newline, right");
        channelPropertiesPanel.add(initialStateComboBox, "w 108!");
        channelPropertiesPanel.add(attachmentLabel, "newline, right");
        channelPropertiesPanel.add(attachmentComboBox, "w 108!, split 2");
        channelPropertiesPanel.add(attachmentPropertiesButton, "gapbefore 6");
        channelPropertiesPanel.add(attachmentStoreCheckBox);
        channelPropertiesPanel.add(tagsLabel, "newline, right");
        channelPropertiesPanel.add(tagsField, "sx, growx");

        messageStoragePanel.setLayout(new MigLayout("insets 0 10 5 22, novisualpadding, hidemode 3, gap 6", "", "[][][][][][]4[]4[]4[]"));
        messageStoragePanel.add(messageStorageSlider, "spany, h 167!, top");
        messageStoragePanel.add(storageModeLabel);
        messageStoragePanel.add(contentLabel, "newline");
        messageStoragePanel.add(metadataLabel, "newline");
        messageStoragePanel.add(durableLabel, "newline, split 2");
        messageStoragePanel.add(durableStatusLabel);
        messageStoragePanel.add(performanceLabel, "newline, sx, split 2");
        messageStoragePanel.add(messageStorageProgressBar, "growx, gapbefore 12");
        messageStoragePanel.add(encryptMessagesCheckBox, "newline");
        messageStoragePanel.add(removeContentCheckBox, "newline, split 2");
        messageStoragePanel.add(removeOnlyFilteredCheckBox);
        messageStoragePanel.add(removeAttachmentsCheckBox, "newline");
        messageStoragePanel.add(queueWarningLabel, "newline");

        messagePruningPanel.setLayout(new MigLayout("insets 0 10 10 10, novisualpadding, hidemode 3, gap 6"));
        messagePruningPanel.add(metadataPruningLabel);
        messagePruningPanel.add(metadataPruningOffRadio, "newline, gapleft 12");
        messagePruningPanel.add(metadataPruningOnRadio, "newline, gapleft 12, split 3");
        messagePruningPanel.add(metadataPruningDaysTextField, "w 30!");
        messagePruningPanel.add(metadataDaysLabel);
        messagePruningPanel.add(contentPruningLabel, "newline");
        messagePruningPanel.add(contentPruningMetadataRadio, "newline, gapleft 12");
        messagePruningPanel.add(contentPruningDaysRadio, "newline, gapleft 12, split 3");
        messagePruningPanel.add(contentPruningDaysTextField, "w 30!");
        messagePruningPanel.add(contentDaysLabel);
        messagePruningPanel.add(archiveCheckBox, "newline");
        messagePruningPanel.add(pruningWarningLabel, "newline");

        customMetadataPanel.setLayout(new MigLayout("insets 0 10 10 10, novisualpadding, hidemode 3, fill, gap 6", "[grow][]"));
        customMetadataPanel.add(metaDataScrollPane, "sy, grow");
        customMetadataPanel.add(addMetaDataButton, "top, sg button, flowy, split 2");
        customMetadataPanel.add(deleteMetaDataButton, "sg button");
        customMetadataPanel.add(revertMetaDataButton, "newline, bottom, sg button");

        descriptionPanel.setLayout(new MigLayout("insets 0 10 10 10, novisualpadding, hidemode 3, fill"));
        descriptionPanel.add(summaryDescriptionScrollPane, "grow");

        summaryPanel.setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill", "", "[][][][grow]"));
        summaryPanel.add(channelPropertiesPanel, "growx, sx");
        summaryPanel.add(messageStoragePanel, "newline, w 420!, h 210!, split 2");
        summaryPanel.add(messagePruningPanel, "growx, pushx, h 210!");
        summaryPanel.add(customMetadataPanel, "newline, growx, sx, h 150!");
        summaryPanel.add(descriptionPanel, "newline, sx, grow");

        sourcePanel.setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill, gap 6", "[]12[]", "[][grow]"));
        sourcePanel.add(sourceConnectorTypeLabel, "split 2");
        sourcePanel.add(sourceConnectorTypeComboBox, "gapbefore 12");
        sourcePanel.add(sourceConnectorScrollPane, "newline, sx, grow");

        destinationsPanel.setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill, gap 6", "[]12[]", "[][][grow]"));
        destinationsPanel.add(destinationTableScrollPane, "sx, growx, h 165!");
        destinationsPanel.add(destinationConnectorTypeLabel, "newline, split 3");
        destinationsPanel.add(destinationConnectorTypeComboBox, "gapbefore 12");
        destinationsPanel.add(waitForPreviousCheckbox, "gapbefore 12");
        destinationsPanel.add(destinationConnectorScrollPane, "newline, grow, pushx");
        destinationsPanel.add(destinationVariableList, "w 185!, growy");

        channelView.addTab("Summary", summaryPanel);
        channelView.addTab("Source", sourcePanel);
        channelView.addTab("Destinations", destinationsPanel);
        channelView.addTab("Scripts", scriptsPanel);
        add(channelView, "grow, h 600, w 600");
    }

    private void scriptsComponentShown(ComponentEvent evt) {
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 13, false);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 15, 15, true);
    }

    /** Action when the source tab is shown. */
    private void sourceComponentShown(ComponentEvent evt) {
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
        ConnectorProperties props = destinationConnectorPanel.getProperties();
        ((DestinationConnectorPropertiesInterface) props).getDestinationConnectorProperties().setResourceIds(resourceIds.get(destinationConnector.getMetaDataId()));
        destinationConnector.setProperties(props);
        updateScripts();

        sourceConnectorPanel.updateResponseDropDown();

        // If validation has failed, then highlight any errors on this form.
        if (channelValidationFailed) {
            sourceConnectorPanel.checkProperties(sourceConnectorPanel.getProperties(), true);
        }
    }

    /** Action when the destinations tab is shown. */
    private void destinationComponentShown(ComponentEvent evt) {
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 1, true);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 2, 13, true);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 15, 15, false);

        checkVisibleDestinationTasks();

        // If validation has failed and this destination is enabled, then highlight any errors on this form.
        if (channelValidationFailed && currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex()).isEnabled()) {
            destinationConnectorPanel.checkProperties(destinationConnectorPanel.getProperties(), true);
        }
    }

    /**
     * Action when an action is performed on the source connector type dropdown.
     */
    private void sourceSourceDropdownActionPerformed(ActionEvent evt) {
        // If a channel is not being loaded then alert the user when necessary
        // that changing the connector type will lose all current connector data.
        if (!loadingChannel) {
            if (sourceConnectorPanel.getName() != null && sourceConnectorPanel.getName().equals(sourceConnectorTypeComboBox.getSelectedItem())) {
                return;
            }

            if (!PropertyVerifier.compareProps(sourceConnectorPanel.getProperties(), sourceConnectorPanel.getDefaults())) {
                boolean changeType = parent.alertOption(this.parent, "Are you sure you would like to change this connector type and lose all of the current connector data?");
                if (!changeType) {
                    sourceConnectorTypeComboBox.setSelectedItem(sourceConnectorPanel.getProperties().getName());
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
        sourceConnectorPanel.setConnectorSettingsPanel(LoadedExtensions.getInstance().getSourceConnectors().get((String) sourceConnectorTypeComboBox.getSelectedItem()));

        // Sets all of the properties, transformer, filter, etc. on the new
        // source connector.
        Connector sourceConnector = currentChannel.getSourceConnector();
        if (sourceConnector != null) {
            String connectorName = "";

            if (sourceConnector.getProperties() != null) {
                connectorName = sourceConnector.getProperties().getName();
            }

            if (sourceConnector.getProperties() == null || !connectorName.equals(sourceConnectorTypeComboBox.getSelectedItem())) {
                String name = sourceConnector.getName();
                changeConnectorType(sourceConnector, false);
                sourceConnector.setName(name);

                ConnectorProperties props = sourceConnectorPanel.getDefaults();
                ((SourceConnectorPropertiesInterface) props).getSourceConnectorProperties().setResourceIds(resourceIds.get(sourceConnector.getMetaDataId()));
                sourceConnectorPanel.setProperties(props);

                props = sourceConnectorPanel.getProperties();
                ((SourceConnectorPropertiesInterface) props).getSourceConnectorProperties().setResourceIds(resourceIds.get(sourceConnector.getMetaDataId()));
                sourceConnector.setProperties(props);
            }

            sourceConnector.setTransportName((String) sourceConnectorTypeComboBox.getSelectedItem());
            currentChannel.setSourceConnector(sourceConnector);

            ConnectorProperties props = sourceConnector.getProperties();
            ((SourceConnectorPropertiesInterface) props).getSourceConnectorProperties().setResourceIds(resourceIds.get(sourceConnector.getMetaDataId()));
            sourceConnectorPanel.setProperties(props);
        }

        // Set the source data type to XML if necessary
        checkAndSetXmlDataType();

        sourceConnectorScrollPane.repaint();

        // If validation has failed, then highlight any errors on this form.
        if (channelValidationFailed) {
            sourceConnectorPanel.checkProperties(sourceConnectorPanel.getProperties(), true);
        }
    }

    /**
     * Action when an action is performed on the destination connector type dropdown. Fires off
     * either generateMultipleDestinationPage()
     */
    private void destinationSourceDropdownActionPerformed(ActionEvent evt) {
        // If a channel is not being loaded then alert the user when necessary
        // that changing the connector type will lose all current connector
        // data. Continue when deleting a destination because the selected
        // destination index will not be different than the last index.
        if (!loadingChannel && !isDeleting) {
            if (destinationConnectorPanel.getProperties().getName() != null && destinationConnectorPanel.getProperties().getName().equals(destinationConnectorTypeComboBox.getSelectedItem()) && lastModelIndex == destinationTable.getSelectedModelIndex()) {
                return;
            }

            // if the selected destination is still the same (same index and
            // not deleting) AND the connector properties have not been 
            // changed from defaults then ask if the user would really like 
            // to change the connector type.
            if (lastModelIndex == destinationTable.getSelectedModelIndex() && !PropertyVerifier.compareProps(destinationConnectorPanel.getProperties(), destinationConnectorPanel.getDefaults())) {
                boolean changeType = parent.alertOption(this.parent, "Are you sure you would like to change this connector type and lose all of the current connector data?");
                if (!changeType) {
                    destinationConnectorTypeComboBox.setSelectedItem(destinationConnectorPanel.getProperties().getName());
                    return;
                }
            }
        }
        generateMultipleDestinationPage();

        // If validation has failed and this destination is enabled, then highlight any errors on this form.
        if (channelValidationFailed && currentChannel.getDestinationConnectors().get(destinationTable.getSelectedModelIndex()).isEnabled()) {
            destinationConnectorPanel.checkProperties(destinationConnectorPanel.getProperties(), true);
        }
    }

    private void waitForPreviousCheckboxActionPerformed(ActionEvent evt) {
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
    }

    private void summaryComponentShown(ComponentEvent evt) {
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 13, false);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 15, 15, false);
    }

    private void metadataPruningOffRadioActionPerformed(ActionEvent evt) {
        parent.setSaveEnabled(true);
        metadataPruningDaysTextField.setEnabled(false);

        if (contentPruningMetadataRadio.isSelected()) {
            archiveCheckBox.setEnabled(false);
        }
    }

    private void attachmentComboBoxActionPerformed(ActionEvent evt) {
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
    }

    private void attachmentPropertiesButtonActionPerformed(ActionEvent evt) {
        showAttachmentPropertiesDialog((AttachmentHandlerType) attachmentComboBox.getSelectedItem());
    }

    private void changeDataTypesButtonActionPerformed(ActionEvent evt) {
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
    }

    private void summaryNameFieldKeyReleased(KeyEvent evt) {
        currentChannel.setName(nameField.getText());
        parent.setPanelName("Edit Channel - " + currentChannel.getName());
    }

    private void addMetaDataButtonActionPerformed(ActionEvent evt) {
        DefaultTableModel model = ((DefaultTableModel) metaDataTable.getModel());
        int row = model.getRowCount();

        model.addRow(new Object[] { "", MetaDataColumnType.STRING, "" });

        metaDataTable.setRowSelectionInterval(row, row);

        revertMetaDataButton.setEnabled(true);

        parent.setSaveEnabled(true);
    }

    private void deleteMetaDataButtonActionPerformed(ActionEvent evt) {
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
    }

    private void encryptMessagesCheckBoxActionPerformed(ActionEvent evt) {
        parent.setSaveEnabled(true);
        updateStorageMode();
    }

    private void messageStorageSliderStateChanged(ChangeEvent evt) {
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
    }

    private void removeContentCheckboxActionPerformed(ActionEvent evt) {
        parent.setSaveEnabled(true);
        updateStorageMode();
    }

    private void metadataPruningOnRadioActionPerformed(ActionEvent evt) {
        parent.setSaveEnabled(true);
        metadataPruningDaysTextField.setEnabled(true);
        archiveCheckBox.setEnabled(true);
    }

    private void contentPruningMetadataRadioActionPerformed(ActionEvent evt) {
        parent.setSaveEnabled(true);
        contentPruningDaysTextField.setEnabled(false);

        if (metadataPruningOffRadio.isSelected()) {
            archiveCheckBox.setEnabled(false);
        }
    }

    private void contentPruningDaysRadioActionPerformed(ActionEvent evt) {
        parent.setSaveEnabled(true);
        contentPruningDaysTextField.setEnabled(true);
        archiveCheckBox.setEnabled(true);
    }

    private void revertMetaDataButtonActionPerformed(ActionEvent evt) {
        if (parent.alertOption(parent, "Are you sure you want to revert custom metadata settings to the last save?")) {
            updateMetaDataTable();
        }
    }

    private void removeAttachmentsCheckboxActionPerformed(ActionEvent evt) {
        parent.setSaveEnabled(true);
        updateStorageMode();
    }

    private void attachmentStoreCheckBoxItemStateChanged(ItemEvent evt) {
        attachmentWarningLabel.setVisible(evt.getStateChange() != ItemEvent.SELECTED && attachmentComboBox.getSelectedItem() != AttachmentHandlerType.NONE);
    }

    private void setDependenciesButtonActionPerformed(ActionEvent evt) {
        ChannelDependenciesDialog dialog = new ChannelDependenciesDialog(currentChannel);
        if (dialog.wasSaved()) {
            resourceIds = dialog.getSelectedResourceIds();
            currentChannel.getProperties().setResourceIds(resourceIds.get(null));
            ((SourceConnectorPropertiesInterface) currentChannel.getSourceConnector().getProperties()).getSourceConnectorProperties().setResourceIds(resourceIds.get(currentChannel.getSourceConnector().getMetaDataId()));
            for (Connector destinationConnector : currentChannel.getDestinationConnectors()) {
                ((DestinationConnectorPropertiesInterface) destinationConnector.getProperties()).getDestinationConnectorProperties().setResourceIds(resourceIds.get(destinationConnector.getMetaDataId()));
            }
            parent.setSaveEnabled(true);
        }
    }

    public void generateMultipleDestinationPage() {
        // Get the selected destination connector and set it.
        destinationConnectorPanel.setConnectorSettingsPanel(LoadedExtensions.getInstance().getDestinationConnectors().get((String) destinationConnectorTypeComboBox.getSelectedItem()));

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

        // Set to defaults on first load of connector or if it has changed types.
        if (destinationConnector.getProperties() == null || !connectorName.equals(destinationConnectorTypeComboBox.getSelectedItem())) {
            String name = destinationConnector.getName();
            changeConnectorType(destinationConnector, true);
            destinationConnector.setName(name);

            Map<String, String> destinationResourceIds = resourceIds.get(destinationConnector.getMetaDataId());

            ConnectorProperties props = destinationConnectorPanel.getDefaults();
            if (destinationResourceIds != null) {
                ((DestinationConnectorPropertiesInterface) props).getDestinationConnectorProperties().setResourceIds(destinationResourceIds);
            }
            destinationConnectorPanel.setProperties(props);

            props = destinationConnectorPanel.getProperties();
            if (destinationResourceIds != null) {
                ((DestinationConnectorPropertiesInterface) props).getDestinationConnectorProperties().setResourceIds(destinationResourceIds);
            }
            destinationConnector.setProperties(props);

            setResourceIds();
        }

        destinationVariableList.setTransferMode(destinationConnectorPanel.getTransferMode());

        // Set the transport name of the destination connector and set it in the list.
        destinationConnector.setTransportName((String) destinationConnectorTypeComboBox.getSelectedItem());
        destinationConnectors.set(connectorIndex, destinationConnector);

        // If the connector type has changed then set the new value in the destination table.
        String transportName = ((ConnectorTypeData) destinationTable.getModel().getValueAt(destinationTable.getSelectedModelIndex(), destinationTable.getColumnModelIndex(CONNECTOR_TYPE_COLUMN_NAME))).getTransportName();
        if (destinationConnector.getTransportName() != null && !transportName.equals(destinationConnector.getTransportName()) && destinationTable.getSelectedModelIndex() != -1) {
            ConnectorTypeData connectorTypeData = new ConnectorTypeData((String) destinationConnectorTypeComboBox.getSelectedItem());
            destinationTable.getModel().setValueAt(connectorTypeData, destinationTable.getSelectedModelIndex(), destinationTable.getColumnModelIndex(CONNECTOR_TYPE_COLUMN_NAME));
        }

        destinationConnectorPanel.setProperties(destinationConnector.getProperties());
        setDestinationVariableList();

        destinationConnectorScrollPane.repaint();
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
        String alertMessage = checkInvalidPluginProperties(connector);
        if (StringUtils.isNotBlank(alertMessage)) {
            if (!parent.alertOption(parent, alertMessage + "\nWhen this channel is saved, those properties will be lost. You can choose to import this\nconnector at a later time after verifying that all necessary extensions are properly loaded.\nAre you sure you wish to continue?")) {
                return;
            }
        }

        // Update resource names
        parent.updateResourceNames(connector);

        loadingChannel = true;

        // If the connector is a source, then set it, change the dropdown, and set the incoming dataType.
        if ((channelView.getSelectedIndex() == SOURCE_TAB_INDEX) && (connector.getMode().equals(Mode.SOURCE))) {
            currentChannel.setSourceConnector(connector);
            // Update the cached resource ID map
            resourceIds.put(connector.getMetaDataId(), ((SourceConnectorPropertiesInterface) connector.getProperties()).getSourceConnectorProperties().getResourceIds());
            sourceConnectorTypeComboBox.setSelectedItem(currentChannel.getSourceConnector().getTransportName());

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
            // After adding the destination to the channel, make sure to update the cached resource ID map too
            resourceIds.put(connector.getMetaDataId(), ((DestinationConnectorPropertiesInterface) connector.getProperties()).getDestinationConnectorProperties().getResourceIds());
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

    // Tab Container
    private JTabbedPane channelView;

    // Summary Tab
    private JPanel summaryPanel;

    // Channel Properties
    private JPanel channelPropertiesPanel;
    private JLabel nameLabel;
    private MirthTextField nameField;
    private MirthCheckBox summaryEnabledCheckBox;
    private JTextField channelIdField;
    private JLabel dataTypesLabel;
    private JButton dataTypesButton;
    public MirthCheckBox clearGlobalChannelMapCheckBox;
    private JLabel revisionLabel;
    private JLabel dependenciesLabel;
    private JButton dependenciesButton;
    private JLabel lastModifiedLabel;
    private JLabel initialStateLabel;
    private MirthComboBox<DeployedState> initialStateComboBox;
    private JLabel attachmentLabel;
    private MirthComboBox<AttachmentHandlerType> attachmentComboBox;
    private JButton attachmentPropertiesButton;
    public MirthCheckBox attachmentStoreCheckBox;
    private JLabel attachmentWarningLabel;
    private JLabel tagsLabel;
    private MirthTagField tagsField;

    // Message Storage
    private JPanel messageStoragePanel;
    private JSlider messageStorageSlider;
    private JLabel storageModeLabel;
    private JLabel contentLabel;
    private JLabel metadataLabel;
    private JLabel durableLabel;
    private JLabel durableStatusLabel;
    private JLabel performanceLabel;
    private JProgressBar messageStorageProgressBar;
    private MirthCheckBox encryptMessagesCheckBox;
    private MirthCheckBox removeContentCheckBox;
    private MirthCheckBox removeOnlyFilteredCheckBox;
    private MirthCheckBox removeAttachmentsCheckBox;
    private JLabel queueWarningLabel;

    // Message Pruning
    private JPanel messagePruningPanel;
    private JLabel metadataPruningLabel;
    private JRadioButton metadataPruningOffRadio;
    private JRadioButton metadataPruningOnRadio;
    private MirthTextField metadataPruningDaysTextField;
    private JLabel metadataDaysLabel;
    private JLabel contentPruningLabel;
    private JRadioButton contentPruningMetadataRadio;
    private JRadioButton contentPruningDaysRadio;
    private MirthTextField contentPruningDaysTextField;
    private JLabel contentDaysLabel;
    private MirthCheckBox archiveCheckBox;
    private JLabel pruningWarningLabel;

    // Custom Metadata
    private JPanel customMetadataPanel;
    private MirthTable metaDataTable;
    private JScrollPane metaDataScrollPane;
    private JButton addMetaDataButton;
    private JButton deleteMetaDataButton;
    private JButton revertMetaDataButton;

    // Channel Description
    private JPanel descriptionPanel;
    private MirthTextPane summaryDescriptionText;
    private JScrollPane summaryDescriptionScrollPane;

    // Source
    private JPanel sourcePanel;
    private JLabel sourceConnectorTypeLabel;
    private MirthComboBox<String> sourceConnectorTypeComboBox;
    private ConnectorPanel sourceConnectorPanel;
    private JScrollPane sourceConnectorScrollPane;

    // Destinations
    private JPanel destinationsPanel;
    private MirthTable destinationTable;
    private JScrollPane destinationTableScrollPane;
    private JLabel destinationConnectorTypeLabel;
    private MirthComboBox<String> destinationConnectorTypeComboBox;
    private MirthCheckBox waitForPreviousCheckbox;
    private ConnectorPanel destinationConnectorPanel;
    private JScrollPane destinationConnectorScrollPane;
    public VariableList destinationVariableList;

    // Scripts
    private ScriptPanel scriptsPanel;
}
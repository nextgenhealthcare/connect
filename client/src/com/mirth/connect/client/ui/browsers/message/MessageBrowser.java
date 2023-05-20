/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.browsers.message;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.DateFormatter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.JSONTokenMarker;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.PaginatedMessageList;
import com.mirth.connect.client.core.RequestAbortedException;
import com.mirth.connect.client.core.api.servlets.MessageServletInterface;
import com.mirth.connect.client.core.api.util.OperationUtil;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.ViewContentDialog;
import com.mirth.connect.client.ui.attachments.AttachmentTypeDialog;
import com.mirth.connect.client.ui.components.MirthDatePicker;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthSyntaxTextArea;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTimePicker;
import com.mirth.connect.client.ui.util.DisplayUtil;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.model.filters.elements.ContentSearchElement;
import com.mirth.connect.model.filters.elements.MetaDataSearchElement;
import com.mirth.connect.model.filters.elements.MetaDataSearchOperator;
import com.mirth.connect.plugins.AttachmentViewer;
import com.mirth.connect.util.MirthJsonUtil;
import com.mirth.connect.util.MirthXmlUtil;
import com.mirth.connect.util.StringUtil;

/**
 * The message browser panel.
 */
public class MessageBrowser extends javax.swing.JPanel {
    protected static final int ID_COLUMN = 0;
    protected static final int CONNECTOR_COLUMN = 1;
    protected static final int STATUS_COLUMN = 2;
    protected static final int ORIGINAL_RECEIVED_DATE_COLUMN = 3;
    protected static final int RECEIVED_DATE_COLUMN = 4;
    protected static final int SEND_ATTEMPTS_COLUMN = 5;
    protected static final int SEND_DATE_COLUMN = 6;
    protected static final int RESPONSE_DATE_COLUMN = 7;
    protected static final int ERRORS_COLUMN = 8;
    protected static final int SERVER_ID_COLUMN = 9;
    protected static final int ORIGINAL_SERVER_ID_COLUMN = 10;
    protected static final int ORIGINAL_ID_COLUMN = 11;
    protected static final int IMPORT_ID_COLUMN = 12;
    protected static final int IMPORT_CHANNEL_ID_COLUMN = 13;
    protected static final int CHANNEL_NAME_COLUMN = 14;

    protected final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    private final String SCOPE_COLUMN_NAME = "Scope";
    private final String KEY_COLUMN_NAME = "Variable";
    private final String VALUE_COLUMN_NAME = "Value";
    private final String TYPE_COLUMN_NAME = "Type";
    private final String NUMBER_COLUMN_NAME = "#";
    private final String ATTACHMENTID_COLUMN_NAME = "Attachment Id";
    //TODO tune size or change to user option
    private final int MAX_CACHE_SIZE = 200;
    private String lastUserSelectedMessageType = "Raw";
    private String lastUserSelectedErrorType = "Processing Error";
    protected Frame parent;
    private String channelId;
    private String channelName;
    private boolean isChannelDeployed;
    private boolean isCURESPHILoggingOn;
    protected boolean isChannelMessagesPanelFirstLoadSearch;
    private Map<Integer, String> connectors;
    protected List<MetaDataColumn> metaDataColumns;
    private MessageBrowserTableModel tableModel;
    protected PaginatedMessageList messages;
    protected Map<Object, Message> messageCache;
    protected Map<Object, List<Attachment>> attachmentCache;
    protected MessageFilter messageFilter;
    protected MessageBrowserAdvancedFilter advancedSearchPopup;
    private JPopupMenu attachmentPopupMenu;
    protected TreeMap<Integer, String> columnMap;
    protected Set<String> defaultVisibleColumns;
    // Worker used for loading a page and counting the total number of messages
    private SwingWorker<Void, Void> worker;
    private Logger logger = LogManager.getLogger(this.getClass());
    private ExecutorService executor;
    private List<Future<Void>> prettyPrintWorkers = new ArrayList<Future<Void>>();

    private List<Integer> selectedMetaDataIds;

    /**
     * Constructs the new message browser and sets up its default information/layout
     */
    @SuppressWarnings("serial")
    public MessageBrowser() {
        this.parent = PlatformUI.MIRTH_FRAME;

        messageCache = Collections.synchronizedMap(new LinkedHashMap<Object, Message>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Object, Message> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        });

        attachmentCache = Collections.synchronizedMap(new LinkedHashMap<Object, List<Attachment>>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Object, List<Attachment>> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        });

        initColumnData();

        // Generated by Netbeans
        initComponents();
        // Add additional initializations here
        initComponentsManual();

        //Initialize the message table
        makeMessageTable();
        //Initialize the mappings table
        makeMappingsTable();

        executor = Executors.newFixedThreadPool(5);
    }

    public void initComponentsManual() {
        attachmentPopupMenu = new JPopupMenu();
        JMenuItem viewAttach = new JMenuItem("View Attachment");
        viewAttach.setIcon(new ImageIcon(Frame.class.getResource("images/attach.png")));
        viewAttach.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                viewAttachment();
            }
        });
        attachmentPopupMenu.add(viewAttach);

        JMenuItem exportAttach = new JMenuItem("Export Attachment");
        exportAttach.setIcon(new ImageIcon(Frame.class.getResource("images/report_disk.png")));
        exportAttach.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                exportAttachment();
            }
        });
        attachmentPopupMenu.add(exportAttach);

        pageSizeField.setDocument(new MirthFieldConstraints(3, false, false, true));
        pageNumberField.setDocument(new MirthFieldConstraints(7, false, false, true));

        advancedSearchPopup = createMessageBrowserAdvancedFilter();
        advancedSearchPopup.setVisible(false);

        LineBorder lineBorder = new LineBorder(new Color(0, 0, 0));
        TitledBorder titledBorder = new TitledBorder("Current Search");
        titledBorder.setBorder(lineBorder);

        lastSearchCriteriaPane.setBorder(titledBorder);
        lastSearchCriteriaPane.setBackground(Color.white);
        lastSearchCriteria.setBackground(Color.white);

        mirthDatePicker1.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {
                allDayCheckBox.setEnabled(mirthDatePicker1.getDate() != null || mirthDatePicker2.getDate() != null);
                mirthTimePicker1.setEnabled(mirthDatePicker1.getDate() != null && !allDayCheckBox.isSelected());
            }
        });

        mirthDatePicker2.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {
                allDayCheckBox.setEnabled(mirthDatePicker1.getDate() != null || mirthDatePicker2.getDate() != null);
                mirthTimePicker2.setEnabled(mirthDatePicker2.getDate() != null && !allDayCheckBox.isSelected());
            }
        });

        pageNumberField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER && pageGoButton.isEnabled()) {
                    jumpToPageNumber();
                }
            }
        });

        if (!Arrays.asList("postgres", "oracle", "mysql").contains(PlatformUI.SERVER_DATABASE)) {
            regexTextSearchCheckBox.setEnabled(false);
        }

        this.addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                // Stop waiting for message browser requests when the message browser 
                // is no longer being displayed
                parent.mirthClient.getServerConnection().abort(getAbortOperations());
                // Clear the message cache when leaving the message browser.
                parent.messageBrowser.clearCache();
                // Clear the table selection to prevent the selection listener from triggering multiple times while the model is being cleared
                deselectRows();
                // Clear the records in the table
                tableModel.clear();

                // Remove all columns
                for (TableColumn tableColumn : messageTreeTable.getColumns(true)) {
                    messageTreeTable.removeColumn(tableColumn);
                }
            }

        });
    }
    
    protected MessageBrowserAdvancedFilter createMessageBrowserAdvancedFilter() {
    	return new MessageBrowserAdvancedFilter(parent, this, "Advanced Search Filter", true, true);
    }
    
    public void loadChannels(List<MessageBrowserChannelModel> channelModels) {
    	if (channelModels != null && !channelModels.isEmpty()) {
    		loadChannel(channelModels.get(0));
    	}
    }
    
    public void loadChannel(MessageBrowserChannelModel channelModel) {
    	String channelId = channelModel.getChannelId();
    	String channelName = channelModel.getChannelName();
    	Map<Integer, String> connectors = channelModel.getConnectors();
    	List<MetaDataColumn> metaDataColumns = channelModel.getMetaDataColumns();
    	List<Integer> selectedMetaDataIds = channelModel.getSelectedMetaDataIds(); 
    	boolean isChannelDeployed = channelModel.isChannelDeployed();
    	
    	this.isChannelDeployed = isChannelDeployed;
        this.selectedMetaDataIds = selectedMetaDataIds;
        taskPaneWhenLoadingChannels();

        //Set the FormatCheckboxes to their default setting
        formatMessageCheckBox.setSelected(Preferences.userNodeForPackage(Mirth.class).getBoolean("messageBrowserFormat", true));

        this.channelId = channelId;
        this.channelName = channelName;
        this.connectors = connectors;
        this.connectors.put(null, "Deleted Connectors");
        initMetaDataColumns(channelModel);
        tableModel.clear();

        advancedSearchPopup.loadChannel();
        resetSearchCriteria();
        advancedSearchPopup.setSelectedMetaDataIds(selectedMetaDataIds);
        updateAdvancedSearchButtonFont();

        lastUserSelectedMessageType = "Raw";
        updateMessageRadioGroup();

        List<String> columnList = new ArrayList<String>();
        // Add standard columns
        columnList.addAll(columnMap.values());

        isCURESPHILoggingOn = false;
        for (MetaDataColumn column : this.metaDataColumns) {
            // if channel has patient_id metadata, turn on CURES PHI logging
        	if (column.getName().toLowerCase().equals("patient_id")) {
        		isCURESPHILoggingOn = true;
        		break;
        	}
        }
        
        // Add custom columns
        Set<String> metaDataColumnNames = createCustomMetaDataColumns();

        Map<String, Set<String>> customHiddenColumnMap = messageTreeTable.getCustomHiddenColumnMap();
        Set<String> hiddenCustomColumns = customHiddenColumnMap.get(channelId);
        if (hiddenCustomColumns == null) {
            hiddenCustomColumns = new HashSet<String>();
            customHiddenColumnMap.put(channelId, hiddenCustomColumns);
        } else {
            // If this channel was viewed before, remove any hidden custom columns that no longer exist
            Iterator<String> iterator = hiddenCustomColumns.iterator();
            while (iterator.hasNext()) {
                if (!metaDataColumnNames.contains(iterator.next())) {
                    iterator.remove();
                }
            }
        }

        columnList.addAll(metaDataColumnNames);
        tableModel.setColumnIdentifiers(columnList);

        // Create the column objects and add them to the message table
        MessageBrowserTableColumnFactory columnFactory = (MessageBrowserTableColumnFactory) messageTreeTable.getColumnFactory();
        for (int modelIndex = 0; modelIndex < columnList.size(); modelIndex++) {
            TableColumnExt column = columnFactory.createAndConfigureTableColumn(messageTreeTable.getModel(), modelIndex);
            messageTreeTable.addColumn(column);
        }

        messageTreeTable.setMetaDataColumns(metaDataColumnNames, channelId);
        messageTreeTable.restoreColumnPreferences();

        isChannelMessagesPanelFirstLoadSearch = true;
        runSearch();
        isChannelMessagesPanelFirstLoadSearch = false;
    }
    
    protected void initMetaDataColumns(MessageBrowserChannelModel channelModel) {
    	this.metaDataColumns = channelModel.getMetaDataColumns();
    }
    
    protected Set<String> createCustomMetaDataColumns() {
        // Add custom columns
        Set<String> metaDataColumnNames = new LinkedHashSet<String>();
        
        for (MetaDataColumn column : this.metaDataColumns) {
            metaDataColumnNames.add(column.getName());
        }
        
        return metaDataColumnNames;
    }
    
    protected void taskPaneWhenLoadingChannels() {
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 1, 1, isChannelDeployed);
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 7, 8, isChannelDeployed);
    }

    public Set<Operation> getAbortOperations() {
        return OperationUtil.getAbortableOperations(MessageServletInterface.class);
    }

    public void resetSearchCriteria() {
        mirthDatePicker1.setDate(null);
        mirthDatePicker2.setDate(null);
        textSearchField.setText("");
        regexTextSearchCheckBox.setSelected(false);
        allDayCheckBox.setSelected(false);
        statusBoxReceived.setSelected(false);
        statusBoxTransformed.setSelected(false);
        statusBoxFiltered.setSelected(false);
        statusBoxQueued.setSelected(false);
        statusBoxPending.setSelected(false);
        statusBoxSent.setSelected(false);
        statusBoxError.setSelected(false);
        pageSizeField.setText(String.valueOf(Preferences.userNodeForPackage(Mirth.class).getInt("messageBrowserPageSize", 20)));

        advancedSearchPopup.resetSelections();
        updateAdvancedSearchButtonFont();
    }

    public void updateFilterButtonFont(int font) {
        filterButton.setFont(filterButton.getFont().deriveFont(font));
        filterButton.requestFocus();
    }

    public void updateAdvancedSearchButtonFont() {
        if (advancedSearchPopup.hasAdvancedCriteria()) {
            advSearchButton.setFont(advSearchButton.getFont().deriveFont(Font.BOLD));
        } else {
            advSearchButton.setFont(advSearchButton.getFont().deriveFont(Font.PLAIN));
        }
    }

    public String getChannelId() {
        return channelId;
    }

    public Map<Integer, String> getConnectors() {
        return connectors;
    }

    public List<MetaDataColumn> getMetaDataColumns() {
        return metaDataColumns;
    }

    public MessageFilter getMessageFilter() {
        return messageFilter;
    }

    public String getPatientId(Long messageId, Integer metaDataId, List<Integer> selectedMetaDataIds) {
        String patientId = null;
        Message message = getMessageFromCache(channelId, messageId);
        try {
            if (message == null) {
                message = parent.mirthClient.getMessageContent(channelId, messageId, selectedMetaDataIds);
            }
            ConnectorMessage connectorMessage = message.getConnectorMessages().get(metaDataId);
            if (connectorMessage.getMetaDataMap().get("PATIENT_ID") != null) {
                patientId = (String) connectorMessage.getMetaDataMap().get("PATIENT_ID").toString();
            }
        } catch (ClientException e) {
            logger.error("Invalid patient ID.", e);
        }
        return patientId;
    }

    public int getPageSize() {
        return NumberUtils.toInt(pageSizeField.getText());
    }

    private Calendar getCalendar(MirthDatePicker datePicker, MirthTimePicker timePicker) throws ParseException {
        DateFormatter timeFormatter = new DateFormatter(new SimpleDateFormat("hh:mm aa"));
        Date date = datePicker.getDate();
        String time = timePicker.getDate();

        if (date != null && time != null) {
            Calendar dateCalendar = Calendar.getInstance();
            Calendar timeCalendar = Calendar.getInstance();
            Calendar dateTimeCalendar = Calendar.getInstance();

            dateCalendar.setTime(date);
            timeCalendar.setTime((Date) timeFormatter.stringToValue(time));
            dateTimeCalendar.setTime(date);

            // Only set the time if the time picker is enabled. Otherwise, it will default to 00:00:00
            if (timePicker.isEnabled()) {
                dateTimeCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
                dateTimeCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
                dateTimeCalendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND));
            }

            return dateTimeCalendar;
        }

        return null;
    }
    
    /***
     * All access to the message and attachment caches should go through these methods, 
     * so that we can just override them as necessary in subclasses.
     */
    protected Message getMessageFromCache(String channelId, Long messageId) {
    	return messageCache.get(messageId);
    }
    
    protected void putMessageInCache(String channelId, Long messageId, Message message) {
    	messageCache.put(messageId, message);
    }
    
    protected List<Attachment> getAttachmentsFromCache(String channelId, Long messageId) {
    	return attachmentCache.get(messageId);
    }
    
    protected void putAttachmentsInCache(String channelId, Long messageId, List<Attachment> attachments) {
    	attachmentCache.put(messageId, attachments);
    }
    /***/
    

    /**
     * Constructs the MessageFilter (this.filter) based on the current form selections
     */
    protected boolean generateMessageFilter() {
        messageFilter = new MessageFilter();

        // set start/end date
        try {
            messageFilter.setStartDate(getCalendar(mirthDatePicker1, mirthTimePicker1));
            Calendar endCalendar = getCalendar(mirthDatePicker2, mirthTimePicker2);

            if (endCalendar != null && !mirthTimePicker2.isEnabled()) {
                // If the end time picker is disabled, it will be set to 00:00:00 of the day provided.
                // Since our query is using <= instead of <, we add one day and then subtract a millisecond 
                // in order to set the time to the last millisecond of the day we want to search on
                endCalendar.add(Calendar.DATE, 1);
                endCalendar.add(Calendar.MILLISECOND, -1);
            }
            messageFilter.setEndDate(endCalendar);
        } catch (ParseException e) {
            parent.alertError(parent, "Invalid date.");
            return false;
        }

        Calendar startDate = messageFilter.getStartDate();
        Calendar endDate = messageFilter.getEndDate();

        if (startDate != null && endDate != null && startDate.getTimeInMillis() > endDate.getTimeInMillis()) {
            parent.alertError(parent, "Start date cannot be after the end date.");
            return false;
        }

        // Set text search
        String textSearch = StringUtils.trim(textSearchField.getText());

        if (textSearch.length() > 0) {
            messageFilter.setTextSearch(textSearch);
            List<String> textSearchMetaDataColumns = new ArrayList<String>();

            for (MetaDataColumn metaDataColumn : getMetaDataColumns()) {
                if (metaDataColumn.getType() == MetaDataColumnType.STRING) {
                    textSearchMetaDataColumns.add(metaDataColumn.getName());
                }
            }

            messageFilter.setTextSearchMetaDataColumns(textSearchMetaDataColumns);
        }

        if (regexTextSearchCheckBox.isSelected()) {
            messageFilter.setTextSearchRegex(true);
        }

        // set status
        Set<Status> statuses = new HashSet<Status>();

        if (statusBoxReceived.isSelected()) {
            statuses.add(Status.RECEIVED);
        }

        if (statusBoxTransformed.isSelected()) {
            statuses.add(Status.TRANSFORMED);
        }

        if (statusBoxFiltered.isSelected()) {
            statuses.add(Status.FILTERED);
        }

        if (statusBoxSent.isSelected()) {
            statuses.add(Status.SENT);
        }

        if (statusBoxError.isSelected()) {
            statuses.add(Status.ERROR);
        }

        if (statusBoxQueued.isSelected()) {
            statuses.add(Status.QUEUED);
        }
        if (statusBoxPending.isSelected()) {
            statuses.add(Status.PENDING);
        }

        if (!statuses.isEmpty()) {
            messageFilter.setStatuses(statuses);
        }

        if (StringUtils.isNotEmpty(textSearch) && Preferences.userNodeForPackage(Mirth.class).getBoolean("textSearchWarning", true)) {
            JCheckBox dontShowTextSearchWarningCheckBox = new JCheckBox("Don't show this message again in the future");

            String textSearchWarning = "<html>Text searching may take a long time, depending on the amount of messages being searched.<br/>Are you sure you want to proceed?</html>";
            String textRegexSearchWarning = "<html>Regular expression pattern matching may take a long time and be a costly operation, depending on the amount of messages being searched.<br/>Are you sure you want to proceed?</html>";
            String searchWarning = (regexTextSearchCheckBox.isSelected()) ? textRegexSearchWarning : textSearchWarning;
            Object[] params = new Object[] { searchWarning, dontShowTextSearchWarningCheckBox };
            int result = JOptionPane.showConfirmDialog(this, params, "Select an Option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            Preferences.userNodeForPackage(Mirth.class).putBoolean("textSearchWarning", !dontShowTextSearchWarningCheckBox.isSelected());

            if (result != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        advancedSearchPopup.applySelectionsToFilter(messageFilter);
        selectedMetaDataIds = messageFilter.getIncludedMetaDataIds();

        if (messageFilter.getMaxMessageId() == null) {
            try {
                Long maxMessageId = parent.mirthClient.getMaxMessageId(channelId);
                messageFilter.setMaxMessageId(maxMessageId);
            } catch (ClientException e) {
                parent.alertThrowable(parent, e);
                return false;
            }
        }

        return true;
    }

    protected void runSearch() {
        if (generateMessageFilter()) {
            updateFilterButtonFont(Font.PLAIN);

            try {
                configurePaginatedMessageList();
            } catch (NumberFormatException e) {
                parent.alertError(parent, "Invalid page size.");
                return;
            } catch (Exception e) {
            	parent.alertError(parent, "Error configuring paginated message list: " + e.getMessage());
            }

            countButton.setVisible(true);
            clearCache();
            loadPageNumber(1);

            updateSearchCriteriaPane();
            auditSearch();
        }
    }
    
    protected void configurePaginatedMessageList() throws Exception {
    	messages = new PaginatedMessageList();
        messages.setClient(parent.mirthClient);
        messages.setChannelId(channelId);
        messages.setMessageFilter(messageFilter);
        messages.setPageSize(Integer.parseInt(pageSizeField.getText()));
    }
    
    protected void auditSearch() {
    	auditSearch(channelId, channelName);
    }
    
    protected void auditSearch(String channelId, String channelName) {
    	// if CURES PHI logging is on and channel messages have been loaded, audit the event
        if (isCURESPHILoggingOn && !isChannelMessagesPanelFirstLoadSearch) {
            try {
                LinkedHashMap<String, String> auditMessageAttributesMap = new LinkedHashMap<String, String>();
                auditMessageAttributesMap.put("channel", "Channel[id=" + channelId + ",name=" + channelName + "]");
                auditMessageAttributesMap.put("filter", messageFilter.toString());
                if (messageFilter.getMetaDataSearch() != null) {
                    List<MetaDataSearchElement> elements = messageFilter.getMetaDataSearch();
                    for (MetaDataSearchElement element : elements) {
                        if (element.getColumnName().toString().equals("PATIENT_ID") && MetaDataSearchOperator.fromString(element.getOperator()).toString().equals("=")) {
                            auditMessageAttributesMap.put("patientId", element.getValue().toString());
                        }
                    }
                }
                parent.mirthClient.auditQueriedPHIMessage(auditMessageAttributesMap);
            } catch (ClientException e) {
                logger.error("Unable to audit the CURES queried PHI event.", e);
            }
        }
    }

    protected void updateSearchCriteriaPane() {
        StringBuilder text = new StringBuilder();
        Calendar startDate = messageFilter.getStartDate();
        Calendar endDate = messageFilter.getEndDate();
        String padding = "\n";

        text.append("Max Message Id: ");
        text.append(messageFilter.getMaxMessageId());

        if (messageFilter.getMinMessageId() != null) {
            text.append(padding + "Min Message Id: ");
            text.append(messageFilter.getMinMessageId());
        }

        String startDateFormatString = mirthTimePicker1.isEnabled() ? "yyyy-MM-dd HH:mm" : "yyyy-MM-dd";
        String endDateFormatString = mirthTimePicker2.isEnabled() ? "yyyy-MM-dd HH:mm" : "yyyy-MM-dd";

        DateFormat startDateFormat = new SimpleDateFormat(startDateFormatString);
        DateFormat endDateFormat = new SimpleDateFormat(endDateFormatString);

        text.append(padding + "Date Range: ");

        if (startDate == null) {
            text.append("(any)");
        } else {
            text.append(startDateFormat.format(startDate.getTime()));
            if (!mirthTimePicker1.isEnabled()) {
                text.append(" (all day)");
            }
        }

        text.append(" to ");

        if (endDate == null) {
            text.append("(any)");
        } else {
            text.append(endDateFormat.format(endDate.getTime()));
            if (!mirthTimePicker2.isEnabled()) {
                text.append(" (all day)");
            }
        }

        text.append(padding + "Statuses: ");

        if (messageFilter.getStatuses() == null) {
            text.append("(any)");
        } else {
            text.append(StringUtils.join(messageFilter.getStatuses(), ", "));
        }

        if (messageFilter.getTextSearch() != null) {
            text.append(padding + "Text Search: " + messageFilter.getTextSearch());
        }

        text.append(getConnectorSearchCriteriaText(padding));

        if (messageFilter.getOriginalIdLower() != null || messageFilter.getOriginalIdUpper() != null) {
            text.append(padding + "Original Id: ");
            if (messageFilter.getOriginalIdUpper() == null) {
                text.append("Greater than " + messageFilter.getOriginalIdLower());
            } else if (messageFilter.getOriginalIdLower() == null) {
                text.append("Less than " + messageFilter.getOriginalIdUpper());
            } else {
                text.append("Between " + messageFilter.getOriginalIdLower() + " and " + messageFilter.getOriginalIdUpper());
            }
        }

        if (messageFilter.getImportIdLower() != null || messageFilter.getImportIdUpper() != null) {
            text.append(padding + "Import Id: ");
            if (messageFilter.getImportIdUpper() == null) {
                text.append("Greater than " + messageFilter.getImportIdLower());
            } else if (messageFilter.getImportIdLower() == null) {
                text.append("Less than " + messageFilter.getImportIdUpper());
            } else {
                text.append("Between " + messageFilter.getImportIdLower() + " and " + messageFilter.getImportIdUpper());
            }
        }

        if (messageFilter.getServerId() != null) {
            text.append(padding + "Server Id: " + messageFilter.getServerId());
        }

        Integer sendAttemptsLower = messageFilter.getSendAttemptsLower();
        Integer sendAttemptsUpper = messageFilter.getSendAttemptsUpper();

        if (sendAttemptsLower != null || sendAttemptsUpper != null) {
            text.append(padding + "# of Send Attempts: ");

            if (sendAttemptsLower != null) {
                text.append(sendAttemptsLower);
            } else {
                text.append("(any)");
            }

            text.append(" - ");

            if (sendAttemptsUpper != null) {
                text.append(sendAttemptsUpper);
            } else {
                text.append("(any)");
            }
        }

        if (messageFilter.getContentSearch() != null) {
            List<ContentSearchElement> contentSearch = messageFilter.getContentSearch();

            for (ContentSearchElement element : contentSearch) {
                for (String value : element.getSearches()) {
                    text.append(padding + ContentType.fromCode(element.getContentCode()) + " contains \"" + value + "\"");
                }
            }
        }

        if (messageFilter.getMetaDataSearch() != null) {
            List<MetaDataSearchElement> elements = messageFilter.getMetaDataSearch();

            for (MetaDataSearchElement element : elements) {
                text.append(padding + element.getColumnName() + " " + MetaDataSearchOperator.fromString(element.getOperator()).toString() + " ");
                if (element.getValue() instanceof Calendar) {
                    Calendar date = (Calendar) element.getValue();
                    text.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.getTime()));
                } else {
                    text.append(element.getValue());
                }
                if (element.getIgnoreCase()) {
                    text.append(" (Ignore Case)");
                }
            }
        }

        if (messageFilter.getAttachment()) {
            text.append(padding + "Has Attachment");
        }

        if (messageFilter.getError()) {
            text.append(padding + "Has Error");
        }

        lastSearchCriteria.setText(text.toString());
    }
    
    protected String getConnectorSearchCriteriaText(String padding) {
    	StringBuilder text = new StringBuilder();
    	text.append(padding + "Connectors: ");

        if (messageFilter.getIncludedMetaDataIds() == null) {
            if (messageFilter.getExcludedMetaDataIds() == null) {
                text.append("(any)");
            } else {
                List<Integer> excludedMetaDataIds = messageFilter.getExcludedMetaDataIds();
                List<String> connectorNames = new ArrayList<String>();

                for (Entry<Integer, String> connectorEntry : connectors.entrySet()) {
                    if (!excludedMetaDataIds.contains(connectorEntry.getKey())) {
                        connectorNames.add(connectorEntry.getValue());
                    }
                }

                text.append(StringUtils.join(connectorNames, ", "));
            }
        } else if (messageFilter.getIncludedMetaDataIds().isEmpty()) {
            text.append("(none)");
        } else {
            List<Integer> includedMetaDataIds = messageFilter.getIncludedMetaDataIds();
            List<String> connectorNames = new ArrayList<String>();

            for (Entry<Integer, String> connectorEntry : connectors.entrySet()) {
                if (includedMetaDataIds.contains(connectorEntry.getKey())) {
                    connectorNames.add(connectorEntry.getValue());
                }
            }

            text.append(StringUtils.join(connectorNames, ", "));
        }
        return text.toString();
    }

    public void jumpToPageNumber() {
        if (messages.getPageCount() != null && messages.getPageCount() > 0 && StringUtils.isNotEmpty(pageNumberField.getText())) {
            loadPageNumber(Math.min(Math.max(Integer.parseInt(pageNumberField.getText()), 1), messages.getPageCount()));
        }
    }

    public void loadPageNumber(final int pageNumber) {
        final String workingId = parent.startWorking("Loading page...");

        if (worker != null && !worker.isDone()) {
            parent.mirthClient.getServerConnection().abort(getAbortOperations());
            worker.cancel(true);
        }

        filterButton.setEnabled(false);
        nextPageButton.setEnabled(false);
        previousPageButton.setEnabled(false);
        countButton.setEnabled(false);
        pageGoButton.setEnabled(false);

        // Give focus to the message tree table since these buttons will lose focus. That way the user can also immediately use the arrow keys after a search.
        messageTreeTable.requestFocus();

        worker = new SwingWorker<Void, Void>() {
            private boolean foundItems = false;
            private int retrievedPageNumber = 1;

            public Void doInBackground() {

                try {
                    foundItems = messages.loadPageNumber(pageNumber);
                } catch (Throwable t) { // catch Throwable in case the client runs out of memory

                    if (t.getMessage().contains("Java heap space")) {
                        parent.alertError(parent, "There was an out of memory error when trying to retrieve messages.\nIncrease your heap size or decrease your page size and search again.");
                    } else if (t instanceof RequestAbortedException) {
                        // The client is no longer waiting for the search request
                    } else {
                        parent.alertThrowable(parent, t);
                    }
                    cancel(true);
                }

                return null;
            }

            public void done() {
                if (!isCancelled()) {
                    boolean enableCountButton = (messages.getItemCount() == null);

                    deselectRows();
                    tableModel.clear();

                    if (foundItems) {

                        // if there are no results for pageNumber, loadPageNumber will recursively check previous pages
                        // so we must get the retrievedPageNumber from messages to use below.
                        retrievedPageNumber = messages.getPageNumber();
                        pageNumberField.setText(String.valueOf(retrievedPageNumber));

                        for (Message message : messages) {
                        	if (message.getChannelName() == null || message.getChannelName().isEmpty()) {
                        		message.setChannelName(channelName);
                        	}
                            tableModel.addMessage(message);
                        }

                        if (!messages.hasNextPage()) {
                            messages.setItemCount(new Long(((retrievedPageNumber - 1) * messages.getPageSize()) + messages.size()));
                            enableCountButton = false;
                        }
                    } else {
                        messages.setItemCount(new Long((retrievedPageNumber - 1) * messages.getPageSize()));
                        enableCountButton = false;
                        pageNumberField.setText("0");
                    }

                    messageTreeTable.expandAll();

                    updatePagination();

                    if (enableCountButton) {
                        countButton.setEnabled(true);
                    }
                }
                filterButton.setEnabled(true);
                parent.stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void updatePagination() {
        int pageNumber = messages.getPageNumber();
        Integer pageCount = messages.getPageCount();
        int startOffset, endOffset;

        if (messages.size() == 0) {
            startOffset = 0;
            endOffset = 0;
        } else {
            startOffset = messages.getOffset(pageNumber) + 1;
            endOffset = startOffset + messages.size() - 1;
        }

        String resultText = "Results " + DisplayUtil.formatNumber(startOffset) + " - " + DisplayUtil.formatNumber(endOffset) + " of ";

        // enable the previous page button if the page number is > 1
        // Now that we have hasNextPage, we no longer need any additional logic
        previousPageButton.setEnabled(pageNumber > 1);
        nextPageButton.setEnabled(messages.hasNextPage());

        if (pageCount != null) {
            resultsLabel.setText(resultText + DisplayUtil.formatNumber(messages.getItemCount()));
            pageTotalLabel.setText("of " + DisplayUtil.formatNumber(messages.getPageCount()));
            pageTotalLabel.setEnabled(true);
            pageGoButton.setEnabled(true);
            pageNumberLabel.setEnabled(true);
            pageNumberField.setEnabled(true);
        } else {
            resultsLabel.setText(resultText + "?");
            pageTotalLabel.setText("of " + "?");
            pageGoButton.setEnabled(false);
            pageTotalLabel.setEnabled(false);
            pageNumberLabel.setEnabled(false);
            pageNumberField.setEnabled(false);
        }
    }
    
    protected void resetResultsText() {
    	resultsLabel.setText("Results");
    }

    /**
     * Refreshes the panel with the current filter information and clears the message cache if
     * needed
     */
    public void refresh(Integer page, boolean clearCache) {
        if (clearCache) {
            clearCache();
        }

        if (page == null) {
            loadPageNumber(messages.getPageNumber());
        } else {
            loadPageNumber(page);
        }
    }

    public void clearCache() {
        messageCache.clear();
        attachmentCache.clear();
    }

    /**
     * Shows or hides message tabs depending on what part of the message is selected
     */
    public void updateDescriptionTabs(boolean hasErrors, boolean attachment) {
        //Save the current open tab
        String title = descriptionTabbedPane.getTitleAt(descriptionTabbedPane.getSelectedIndex());
        //Remove all tabs
        descriptionTabbedPane.removeAll();
        descriptionTabbedPane.addTab("Messages", MessagesPanel);
        descriptionTabbedPane.addTab("Mappings", mappingsPane);

        if (hasErrors) {
            descriptionTabbedPane.addTab("Errors", ErrorsPanel);
        }

        if (attachment) {
            descriptionTabbedPane.addTab("Attachments", attachmentsPane);
        }

        //Reopen the saved tab if it was re-added. Otherwise, open the Raw Message tab
        int tabIndex = Math.max(descriptionTabbedPane.indexOfTab(title), 0);
        descriptionTabbedPane.setSelectedIndex(tabIndex);
    }

    public void updateMessageRadioGroup() {
        JRadioButton button = getRadioButtonForMessagePane(lastUserSelectedMessageType);

        if (!button.isShowing()) {
            button = RawMessageRadioButton;
        }

        button.setSelected(true);
        showMessagePane(button.getText());
    }

    private JRadioButton getRadioButtonForMessagePane(String messagePaneName) {
        if (messagePaneName.equals("Raw")) {
            return RawMessageRadioButton;
        } else if (messagePaneName.equals("Processed Raw")) {
            return ProcessedRawMessageRadioButton;
        } else if (messagePaneName.equals("Transformed")) {
            return TransformedMessageRadioButton;
        } else if (messagePaneName.equals("Encoded")) {
            return EncodedMessageRadioButton;
        } else if (messagePaneName.equals("Sent")) {
            return SentMessageRadioButton;
        } else if (messagePaneName.equals("Response")) {
            return ResponseRadioButton;
        } else if (messagePaneName.equals("Response Transformed")) {
            return ResponseTransformedRadioButton;
        } else if (messagePaneName.equals("Processed Response")) {
            return ProcessedResponseRadioButton;
        } else {
            return null;
        }
    }

    private JRadioButton getRadioButtonForErrorPane(String errorPaneName) {
        if (errorPaneName.equals("Processing Error")) {
            return ProcessingErrorRadioButton;
        } else if (errorPaneName.equals("Postprocessor Error")) {
            return PostprocessorErrorRadioButton;
        } else if (errorPaneName.equals("Response Error")) {
            return ResponseErrorRadioButton;
        } else {

            return null;
        }
    }

    private void setCorrectDocument(final MirthSyntaxTextArea textPane, final String message, String dataType) {
        // First load the plain message (without pretty printing)
        SyntaxDocument newDoc = new SyntaxDocument();
        boolean isXml = false;
        boolean isJson = false;

        if (StringUtils.isNotEmpty(message)) {
            String trimmedMessage = message.trim();
            isXml = trimmedMessage.length() > 0 && trimmedMessage.charAt(0) == '<';
            isJson = trimmedMessage.length() > 0 && (trimmedMessage.charAt(0) == '{' || trimmedMessage.charAt(0) == '[');

            // Set token markers
            if (isXml) {
                newDoc.setTokenMarker(new XMLTokenMarker());
            } else if (isJson) {
                newDoc.setTokenMarker(new JSONTokenMarker());
            } else if (dataType != null) {
                newDoc.setTokenMarker(LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getTokenMarker());
            }

            textPane.setDocument(newDoc);
            textPane.setText(message);
        } else {
            textPane.setDocument(newDoc);
            textPane.setText("");
        }

        textPane.setCaretPosition(0);

        // Pretty print (if enabled) on a separate thread since it's prone to take a while for certain DTDs
        if (formatMessageCheckBox.isSelected() && (isXml || isJson)) {
            final boolean formatXml = isXml;
            final boolean formatJson = isJson;

            final String workingId = parent.startWorking("Pretty printing...");

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                String prettifiedMessage = null;

                @Override
                public Void doInBackground() {
                    if (StringUtils.isNotEmpty(message)) {
                        if (formatXml) {
                            prettifiedMessage = MirthXmlUtil.prettyPrint(message);
                        } else if (formatJson) {
                            try {
                                prettifiedMessage = MirthJsonUtil.prettyPrint(message);
                            } catch (Exception e) {
                                logger.error("Error pretty printing json.", e);
                            }
                        }
                    }

                    return null;
                }

                @Override
                public void done() {
                    if (!isCancelled()) {
                        if (prettifiedMessage != null && StringUtils.isNotEmpty(prettifiedMessage)) {
                            textPane.setText(prettifiedMessage);
                            textPane.setCaretPosition(0);
                        }
                    }

                    parent.stopWorking(workingId);
                }
            };

            prettyPrintWorkers.add(worker);
            executor.submit(worker);
        }
    }

    protected void initColumnData() {
        columnMap = new TreeMap<Integer, String>();
        columnMap.put(ID_COLUMN, "Id");
        columnMap.put(CONNECTOR_COLUMN, "Connector");
        columnMap.put(STATUS_COLUMN, "Status");
        columnMap.put(ORIGINAL_RECEIVED_DATE_COLUMN, "Orig. Received Date");
        columnMap.put(RECEIVED_DATE_COLUMN, "Received Date");
        columnMap.put(SEND_ATTEMPTS_COLUMN, "Send Attempts");
        columnMap.put(SEND_DATE_COLUMN, "Send Date");
        columnMap.put(RESPONSE_DATE_COLUMN, "Response Date");
        columnMap.put(ERRORS_COLUMN, "Errors");
        columnMap.put(SERVER_ID_COLUMN, "Server Id");
        columnMap.put(ORIGINAL_SERVER_ID_COLUMN, "Original Server Id");
        columnMap.put(ORIGINAL_ID_COLUMN, "Original Id");
        columnMap.put(IMPORT_ID_COLUMN, "Import Id");
        columnMap.put(IMPORT_CHANNEL_ID_COLUMN, "Import Channel Id");
        columnMap.put(CHANNEL_NAME_COLUMN, "Channel Name");

        defaultVisibleColumns = new LinkedHashSet<String>();
        defaultVisibleColumns.add(columnMap.get(ID_COLUMN));
        defaultVisibleColumns.add(columnMap.get(CONNECTOR_COLUMN));
        defaultVisibleColumns.add(columnMap.get(STATUS_COLUMN));
        defaultVisibleColumns.add(columnMap.get(RECEIVED_DATE_COLUMN));
        defaultVisibleColumns.add(columnMap.get(RESPONSE_DATE_COLUMN));
        defaultVisibleColumns.add(columnMap.get(ERRORS_COLUMN));
    }

    /**
     * Sets the properties and adds the listeners for the Message Table. No data is loaded at this
     * point.
     */
    private void makeMessageTable() {
        messageTreeTable.setDragEnabled(true);
        messageTreeTable.setSortable(false);
        messageTreeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messageTreeTable.setColumnFactory(new MessageBrowserTableColumnFactory());
        messageTreeTable.setLeafIcon(null);
        messageTreeTable.setOpenIcon(null);
        messageTreeTable.setClosedIcon(null);
        messageTreeTable.setAutoCreateColumnsFromModel(false);
        messageTreeTable.setMirthColumnControlEnabled(true);
        messageTreeTable.setShowGrid(true, true);
        messageTreeTable.setHorizontalScrollEnabled(true);
        messageTreeTable.setPreferredScrollableViewportSize(messageTreeTable.getPreferredSize());
        messageTreeTable.setMirthTransferHandlerEnabled(true);

        tableModel = new MessageBrowserTableModel(columnMap.size());
        // Add a blank column to the column initially, otherwise it return an exception on load
        // Columns will be re-generated when the message browser is viewed
        tableModel.setColumnIdentifiers(Arrays.asList(new String[] { "" }));
        messageTreeTable.setTreeTableModel(tableModel);

        // Sets the alternating highlighter for the table
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            messageTreeTable.setHighlighters(highlighter);
        }

        // Add the listener for when the table selection changes
        messageTreeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                MessageListSelected(evt);
            }

        });

        // Add the mouse listener
        messageTreeTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                checkMessageSelectionAndPopupMenu(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                checkMessageSelectionAndPopupMenu(evt);
            }

            // Opens the send message dialog when a message is double clicked.
            // If the root message or source connector is selected, select all destination connectors initially
            // If a destination connector is selected, select only that destination connector initially
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() >= 2) {
                    int row = getSelectedMessageIndex();
                    if (row >= 0) {
                        MessageBrowserTableNode messageNode = (MessageBrowserTableNode) messageTreeTable.getPathForRow(row).getLastPathComponent();
                        if (messageNode.isNodeActive()) {
                        	String channelId = messageNode.getChannelId();
                            Long messageId = messageNode.getMessageId();
                            Integer metaDataId = messageNode.getMetaDataId();

                            Message currentMessage = getMessageFromCache(channelId, messageId);
                            ConnectorMessage connectorMessage = currentMessage.getConnectorMessages().get(metaDataId);
                            List<Integer> selectedMetaDataIds = new ArrayList<Integer>();

                            Map<String, Object> sourceMap = new HashMap<String, Object>();
                            if (connectorMessage.getSourceMap() != null) {
                                sourceMap.putAll(connectorMessage.getSourceMap());
                                // Remove the destination set if it exists, because that will be determined by the selected metadata IDs
                                sourceMap.remove("destinationSet");
                            }

                            if (metaDataId == 0) {
                                selectedMetaDataIds = null;
                            } else {
                                selectedMetaDataIds.add(metaDataId);
                            }

                            if (connectorMessage.getRaw() != null) {
                                parent.editMessageDialog.setPropertiesAndShow(connectorMessage.getRaw().getContent(), connectorMessage.getRaw().getDataType(), channelId, parent.dashboardPanel.getDestinationConnectorNames(channelId), selectedMetaDataIds, sourceMap);
                            }
                        }
                    }
                }
            }
        });

        // Key Listener trigger for DEL
        messageTreeTable.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                int row = getSelectedMessageIndex();
                if (row >= 0) {
                    if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                        MessageBrowserTableNode messageNode = (MessageBrowserTableNode) messageTreeTable.getPathForRow(row).getLastPathComponent();

                        if (messageNode.isNodeActive()) {
                            parent.doRemoveMessage();
                        }

                    } else if (descriptionTabbedPane.getTitleAt(descriptionTabbedPane.getSelectedIndex()).equals("Messages")) {
                        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                            List<AbstractButton> buttons = Collections.list(messagesGroup.getElements());
                            boolean passedSelected = false;
                            for (int i = buttons.size() - 1; i >= 0; i--) {
                                AbstractButton button = buttons.get(i);
                                if (passedSelected && button.isShowing()) {
                                    lastUserSelectedMessageType = buttons.get(i).getText();
                                    updateMessageRadioGroup();
                                    break;
                                } else if (button.isSelected()) {
                                    passedSelected = true;
                                }
                            }
                        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                            List<AbstractButton> buttons = Collections.list(messagesGroup.getElements());
                            boolean passedSelected = false;
                            for (int i = 0; i < buttons.size(); i++) {
                                AbstractButton button = buttons.get(i);
                                if (passedSelected && button.isShowing()) {
                                    lastUserSelectedMessageType = buttons.get(i).getText();
                                    updateMessageRadioGroup();
                                    break;
                                } else if (button.isSelected()) {
                                    passedSelected = true;
                                }
                            }
                        }
                    }
                }

            }
        });
    }

    /**
     * Sets the properties and adds the listeners for the Mappings Table. No data is loaded at this
     * point.
     */
    private void makeMappingsTable() {
        updateMappingsTable(null, true);

        // listen for trigger button and double click to edit channel.
        mappingsTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (mappingsTable.rowAtPoint(new Point(evt.getX(), evt.getY())) == -1) {
                    return;
                }

                if (evt.getClickCount() >= 2) {
                    new ViewContentDialog((String) mappingsTable.getModel().getValueAt(mappingsTable.convertRowIndexToModel(mappingsTable.getSelectedRow()), 2));
                }
            }
        });

        mappingsTable.setSelectionMode(0);
        mappingsTable.getColumnExt(SCOPE_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        mappingsTable.getColumnExt(SCOPE_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);

        // Disable HTML in a column.
        DefaultTableCellRenderer noHTMLRenderer = new DefaultTableCellRenderer();
        noHTMLRenderer.putClientProperty("html.disable", Boolean.TRUE);
        mappingsTable.getColumnExt(VALUE_COLUMN_NAME).setCellRenderer(noHTMLRenderer);

        mappingsPane.setViewportView(mappingsTable);
    }

    public void updateMappingsTable(String[][] tableData, boolean cleared) {
        if (tableData == null || tableData.length == 0) {
            tableData = new String[1][3];
            if (cleared) {
                tableData[0][1] = "Please select a message to view mappings.";
            } else {
                tableData[0][1] = "There are no mappings present.";
            }
            tableData[0][0] = "";
            tableData[0][2] = "";
        }

        if (mappingsTable != null) {
            RefreshTableModel model = (RefreshTableModel) mappingsTable.getModel();
            model.refreshDataVector(tableData);
        } else {
            mappingsTable = new MirthTable();

            mappingsTable.setModel(new RefreshTableModel(tableData, new String[] {
                    SCOPE_COLUMN_NAME, KEY_COLUMN_NAME, VALUE_COLUMN_NAME }) {

                boolean[] canEdit = new boolean[] { false, false, false };

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }

        // Set highlighter.
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            mappingsTable.setHighlighters(highlighter);
        }
    }

    public void updateAttachmentsTable(String channelId, Long messageId) {

        Object[][] tableData = updateAttachmentList(channelId, messageId);

        // Create attachment Table if it has not been created yet. 
        if (attachmentTable != null) {
            RefreshTableModel model = (RefreshTableModel) attachmentTable.getModel();
            if (tableData != null) {
                model.refreshDataVector(tableData);
            }
        } else {
            attachmentTable = new MirthTable();
            attachmentTable.setModel(new RefreshTableModel(tableData, new String[] {
                    NUMBER_COLUMN_NAME, TYPE_COLUMN_NAME, ATTACHMENTID_COLUMN_NAME }) {

                boolean[] canEdit = new boolean[] { false, false, false };

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
            attachmentTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent evt) {
                    if (attachmentTable != null && attachmentTable.getSelectedRow() != -1) {
                        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 9, 10, true);
                    } else {
                        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 9, 10, false);
                    }
                }
            });
            // listen for trigger button and double click to edit channel.
            attachmentTable.addMouseListener(new java.awt.event.MouseAdapter() {

                public void mousePressed(java.awt.event.MouseEvent evt) {
                    checkAttachmentSelectionAndPopupMenu(evt);
                }

                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    checkAttachmentSelectionAndPopupMenu(evt);
                }

                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (attachmentTable.rowAtPoint(new Point(evt.getX(), evt.getY())) == -1) {
                        return;
                    }

                    if (evt.getClickCount() >= 2) {
                        viewAttachment();// do view

                    }
                }
            });
            // Set highlighter.
            if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
                Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
                attachmentTable.setHighlighters(highlighter);
            }

            attachmentTable.setSelectionMode(0);
            attachmentTable.getColumnExt(NUMBER_COLUMN_NAME).setMinWidth(UIConstants.WIDTH_SHORT_MIN);
            attachmentTable.getColumnExt(NUMBER_COLUMN_NAME).setMaxWidth(UIConstants.WIDTH_SHORT_MAX);
            attachmentTable.getColumnExt(TYPE_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
            attachmentTable.getColumnExt(TYPE_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
            attachmentsPane.setViewportView(attachmentTable);
        }
    }

    public Object[][] updateAttachmentList(String channelId, Long messageId) {
        if (messageId == null) {
            return null;
        }
        try {
            List<Attachment> attachments = getAttachmentsFromCache(channelId, messageId);
            ArrayList<Object[]> attachData = new ArrayList<Object[]>();
            int count = 1;
            ArrayList<String> types = new ArrayList<String>();
            // get arraylist of all types
            for (Attachment attachment : attachments) {
                String type = attachment.getType();
                if (!types.contains(type)) {
                    types.add(type);
                }
            }

            for (String type : types) {
                // If handle multiples
                if (getAttachmentViewer(type) != null && getAttachmentViewer(type).handleMultiple()) {
                    String number = Integer.toString(count);
                    String attachment_Ids = "";
                    for (Attachment attachment : attachments) {
                        if (type.equals(attachment.getType())) {
                            if (attachment_Ids.equals("")) {
                                attachment_Ids = attachment.getId();
                            } else {
                                count++;
                                attachment_Ids = attachment_Ids + ", " + attachment.getId();
                            }
                        }
                    }
                    if (!number.equals(Integer.toString(count))) {
                        number = number + " - " + Integer.toString(count);
                    }
                    Object[] rowData = new Object[3];
                    // add to attach Data
                    rowData[0] = number;
                    rowData[1] = type;
                    rowData[2] = attachment_Ids;
                    attachData.add(rowData);
                } // else do them seperate
                else {
                    for (Attachment attachment : attachments) {
                        if (attachment.getType().equals(type)) {
                            Object[] rowData = new Object[3];
                            rowData[0] = Integer.toString(count);
                            rowData[1] = attachment.getType();
                            rowData[2] = attachment.getId();
                            attachData.add(rowData);
                            count++;
                        }
                    }
                }
            }
            Object[][] temp = new Object[attachData.size()][3];
            int rowCount = 0;
            for (Object[] objects : attachData) {
                temp[rowCount] = objects;
                rowCount++;
            }
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public AttachmentViewer getAttachmentViewer(String type) {
        if (LoadedExtensions.getInstance().getAttachmentViewerPlugins().size() > 0) {
            for (AttachmentViewer plugin : LoadedExtensions.getInstance().getAttachmentViewerPlugins().values()) {
                if (plugin.isContentTypeViewable(type)) {
                    return plugin;
                }
            }
        }
        return null;
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been pushed. Deselects the
     * rows if no row was selected.
     */
    private void checkMessageSelectionAndPopupMenu(java.awt.event.MouseEvent evt) {
        int row = messageTreeTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
        if (row == -1) {
            deselectRows();
        }

        if (evt.isPopupTrigger()) {
            if (row != -1) {
                messageTreeTable.setRowSelectionInterval(row, row);
            }
            parent.messagePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been pushed. Deselects the
     * rows if no row was selected.
     */
    private void checkAttachmentSelectionAndPopupMenu(java.awt.event.MouseEvent evt) {
        int row = attachmentTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
        if (row == -1) {
            deselectAttachmentRows();
        }

        if (evt.isPopupTrigger()) {
            if (row != -1) {
                attachmentTable.setRowSelectionInterval(row, row);
            }
            attachmentPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    /**
     * Deselects all rows in the table and clears the description information.
     */
    public void deselectRows() {
        if (messageTreeTable != null) {
            messageTreeTable.clearSelection();
        }

        clearDescription(null);
    }

    /**
     * Deselects all rows in the table and clears the description information.
     */
    public void deselectAttachmentRows() {
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 9, 10, false);
//        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 7, 7, true);
        if (attachmentTable != null) {
            attachmentTable.clearSelection();
//            clearDescription();
        }
    }

    /**
     * Clears all description information.
     */
    private void clearDescription(String text) {
    	taskPaneWhenClearingDescription();
    	
        RawMessageTextPane.setDocument(new SyntaxDocument());
        RawMessageTextPane.setText(text != null ? text : "Select a message to view the raw message.");
        ProcessedRawMessageTextPane.setDocument(new SyntaxDocument());
        ProcessedRawMessageTextPane.setText(text != null ? text : "Select a message to view the processed raw message.");
        TransformedMessageTextPane.setDocument(new SyntaxDocument());
        TransformedMessageTextPane.setText(text != null ? text : "Select a message to view the transformed message.");
        EncodedMessageTextPane.setDocument(new SyntaxDocument());
        EncodedMessageTextPane.setText(text != null ? text : "Select a message to view the encoded message.");
        SentMessageTextPane.setDocument(new SyntaxDocument());
        SentMessageTextPane.setText(text != null ? text : "Select a message to view the sent message.");
        ResponseTextArea.setDocument(new SyntaxDocument());
        ResponseTextArea.setText(text != null ? text : "Select a message to view the response message.");
        responseStatusTextArea.setText("");
        responseStatusTextArea.setCaretPosition(0);
        ResponseTransformedTextPane.setDocument(new SyntaxDocument());
        ResponseTransformedTextPane.setText(text != null ? text : "Select a message to view the response transformed message.");
        ProcessedResponseTextArea.setDocument(new SyntaxDocument());
        ProcessedResponseTextArea.setText(text != null ? text : "Select a message to view the processed response message.");
        processedResponseStatusTextArea.setText("");
        processedResponseStatusTextArea.setCaretPosition(0);
        ProcessingErrorTextPane.setDocument(new SyntaxDocument());
        ProcessingErrorTextPane.setText(text != null ? text : "Select a message to view any errors.");
        ResponseErrorTextPane.setDocument(new SyntaxDocument());
        ResponseErrorTextPane.setText(text != null ? text : "Select a message to view any errors.");
        updateMappingsTable(new String[0][0], true);
        updateAttachmentsTable(null, null);
        descriptionTabbedPane.remove(attachmentsPane);
        formatMessageCheckBox.setEnabled(false);
    }
    
    protected void taskPaneWhenClearingDescription() {
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 6, -1, false);
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 7, 7, isChannelDeployed);
    }

    public Message getSelectedMessage() {
    	String channelId = getSelectedMessageChannelId();
        Long messageId = getSelectedMessageId();

        if (messageId != null) {
            return getMessageFromCache(channelId, messageId);
        }

        return null;
    }

    public ConnectorMessage getSelectedConnectorMessage() {
        Message message = getSelectedMessage();
        Integer metaDataId = getSelectedMetaDataId();

        if (message != null && metaDataId != null) {
            return message.getConnectorMessages().get(metaDataId);
        }

        return null;
    }
    
    public String getSelectedMessageChannelId() {
        int row = getSelectedMessageIndex();

        if (row >= 0) {
            MessageBrowserTableNode messageNode = (MessageBrowserTableNode) messageTreeTable.getPathForRow(row).getLastPathComponent();
            return messageNode.getChannelId();
        }

        return null;    	
    }

    public Long getSelectedMessageId() {
        int row = getSelectedMessageIndex();

        if (row >= 0) {
            MessageBrowserTableNode messageNode = (MessageBrowserTableNode) messageTreeTable.getPathForRow(row).getLastPathComponent();
            return messageNode.getMessageId();
        }

        return null;
    }

    public Integer getSelectedMetaDataId() {
        int row = getSelectedMessageIndex();

        if (row >= 0) {
            MessageBrowserTableNode messageNode = (MessageBrowserTableNode) messageTreeTable.getPathForRow(row).getLastPathComponent();
            return messageNode.getMetaDataId();
        }

        return null;
    }

    public String getSelectedAttachmentId() {
        return (String) attachmentTable.getModel().getValueAt(attachmentTable.convertRowIndexToModel(attachmentTable.getSelectedRow()), 2);
    }

    public String getSelectedMimeType() {
        return (String) attachmentTable.getModel().getValueAt(attachmentTable.convertRowIndexToModel(attachmentTable.getSelectedRow()), 1);
    }

    public boolean canReprocessMessage(Long messageId) {
        Message message = getMessageFromCache(channelId, messageId);

        if (message != null) {
            ConnectorMessage sourceMessage = message.getConnectorMessages().get(0);
            if (sourceMessage != null) {
                if (sourceMessage.getRaw() == null) {
                    return false;
                }
            }
        }

        return true;
    }

    // if we enable multiple row selection in the message browser at some point, then we may want to use this method
//    public List<Integer> getSelectedMetaDataIds() {
//        List<Integer> metaDataIds = new ArrayList<Integer>();
//        
//        for (int row : messageTreeTable.getSelectedRows()) {
//            row = messageTreeTable.convertRowIndexToModel(row);
//            MessageBrowserTableNode messageNode = (MessageBrowserTableNode) messageTreeTable.getPathForRow(row).getLastPathComponent();
//            metaDataIds.add(messageNode.getMetaDataId());
//        }
//        
//        return metaDataIds;
//    }

    public void viewAttachment() {
        try {
            final String attachmentId = getSelectedAttachmentId();
            final Long messageId = getSelectedMessageId();
            final String contentType = (String) attachmentTable.getModel().getValueAt(attachmentTable.convertRowIndexToModel(attachmentTable.getSelectedRow()), 1);

            if (LoadedExtensions.getInstance().getAttachmentViewerPlugins().size() > 0) {
                AttachmentViewer attachmentViewer;

                if (Preferences.userNodeForPackage(Mirth.class).getBoolean("messageBrowserShowAttachmentTypeDialog", true)) {
                    AttachmentTypeDialog dialog = new AttachmentTypeDialog(contentType);
                    attachmentViewer = dialog.getAttachmentViewer();
                } else {
                    attachmentViewer = getAttachmentViewer(contentType);
                    if (attachmentViewer == null) {
                        AttachmentTypeDialog dialog = new AttachmentTypeDialog(contentType);
                        attachmentViewer = dialog.getAttachmentViewer();
                    }
                }

                if (attachmentViewer != null) {
                    final String workingId = parent.startWorking("Loading " + attachmentViewer.getPluginName() + "...");
                    final AttachmentViewer finalAttachmentViewer = attachmentViewer;

                    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                        @Override
                        public Void doInBackground() {
                            finalAttachmentViewer.viewAttachments(channelId, messageId, attachmentId);
                            return null;
                        }

                        @Override
                        public void done() {
                            parent.stopWorking(workingId);
                        }
                    };
                    worker.execute();
                }
            } else {
                parent.alertWarning(this, "No Attachment Viewer plugins installed.");
            }
        } catch (Exception e) {
        }
    }

    public void exportAttachment() {
        parent.doExportAttachment();
    }

    private int getSelectedMessageIndex() {
        int row = -1;
        if (messageTreeTable.getSelectedRow() > -1) {
            row = messageTreeTable.convertRowIndexToModel(messageTreeTable.getSelectedRow());
        }
        return row;
    }

    /**
     * An action for when a row is selected in the table
     */
    private void MessageListSelected(ListSelectionEvent evt) {
        if (!evt.getValueIsAdjusting()) {
            int row = getSelectedMessageIndex();

            if (row >= 0) {
                // Cancel all pretty printing tasks
                for (Future<Void> worker : prettyPrintWorkers) {
                    worker.cancel(true);
                }
                prettyPrintWorkers.clear();
                taskPaneWhenSelectingMessages();

                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Get the table node
                MessageBrowserTableNode messageNode = (MessageBrowserTableNode) messageTreeTable.getPathForRow(row).getLastPathComponent();

                if (messageNode.isNodeActive()) {
                	// Get the channelId from the message node
                	String channelId = messageNode.getChannelId();
                    // Get the messageId from the message node
                    Long messageId = messageNode.getMessageId();
                    // Get the metaDataId from the message node
                    Integer metaDataId = messageNode.getMetaDataId();

                    // Attempt to get the message from the message cache
                    Message message = getMessageFromCache(channelId, messageId);
                    List<Attachment> attachments = getAttachmentsFromCache(channelId, messageId);

                    // If the message is not in the cache, retrieve it from the server
                    if (message == null) {
                        try {
                            message = getMessageContent(channelId, messageId);
                            // If the message was not found (ie. it may have been deleted during the request), do nothing
                            if (message == null || message.getConnectorMessages().size() == 0) {
                                clearDescription("Could not retrieve message content. The message may have been deleted.");
                                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                return;
                            }

                            attachments = parent.mirthClient.getAttachmentsByMessageId(channelId, messageId, false);
                        } catch (Throwable t) {
                            if (t.getMessage().contains("Java heap space")) {
                                parent.alertError(parent, "There was an out of memory error when trying to retrieve message content.\nIncrease your heap size and try again.");
                            } else if (t instanceof RequestAbortedException) {
                                // The client is no longer waiting for the message content request
                            } else {
                                parent.alertThrowable(parent, t);
                            }
                            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            return;
                        }
                        // Add the retrieved message to the message cache
                        putMessageInCache(channelId, messageId, message);
                        putAttachmentsInCache(channelId, messageId, attachments);
                    }

                    ConnectorMessage connectorMessage = message.getConnectorMessages().get(metaDataId);

                    if (connectorMessage != null) {
                        // Update the message tabs
                        updateDescriptionMessages(connectorMessage);
                        // Update the mappings tab
                        updateDescriptionMappings(connectorMessage);
                        // Update the attachments tab
                        updateAttachmentsTable(channelId, messageId);
                        // Update the errors tab
                        updateDescriptionErrors(connectorMessage);
                        // Show relevant tabs. Not using errorCode here just in case for some reason there are errors even though errorCode is 0
                        updateDescriptionTabs(connectorMessage.getProcessingError() != null || connectorMessage.getPostProcessorError() != null || connectorMessage.getResponseError() != null, attachments.size() > 0);
                        updateMessageRadioGroup();

                        if (attachmentTable == null || attachmentTable.getSelectedRow() == -1 || descriptionTabbedPane.indexOfTab("Attachments") == -1) {
                            parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 9, 10, false);
                        }

                        // if CURES PHI logging is on and a channel message has been accessed, audit the event
                        if (isCURESPHILoggingOn) {
                            try {
                                LinkedHashMap<String, String> auditMessageAttributesMap = new LinkedHashMap<String, String>();
                                auditMessageAttributesMap.put("patientId", connectorMessage.getMetaDataMap() != null && connectorMessage.getMetaDataMap().get("PATIENT_ID") != null ? connectorMessage.getMetaDataMap().get("PATIENT_ID").toString() : "");
                                auditMessageAttributesMap.put("channel", "Channel[id=" + channelId + ",name=" + channelName + "]");
                                auditMessageAttributesMap.put("messageId", String.valueOf(connectorMessage.getMessageId()));
                                parent.mirthClient.auditAccessedPHIMessage(auditMessageAttributesMap);
                            } catch (ClientException e) {
                                logger.error("Unable to audit the CURES accessed PHI event.", e);
                            }
                        }
                    }
                } else {
                    clearDescription(null);
                }

                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }
    
    protected Message getMessageContent(String channelId, Long messageId) throws ClientException {
    	return parent.mirthClient.getMessageContent(channelId, messageId, selectedMetaDataIds);
    }
    
    protected void taskPaneWhenSelectingMessages() {
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 6, 6, true);
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 7, -1, isChannelDeployed);
    }

    /**
     * Helper function to update the message tabs
     */
    private void updateDescriptionMessages(ConnectorMessage connectorMessage) {
        MessageContent rawMessage = connectorMessage.getRaw();
        MessageContent processedRawMessage = connectorMessage.getProcessedRaw();
        MessageContent transformedMessage = connectorMessage.getTransformed();
        MessageContent encodedMessage = connectorMessage.getEncoded();
        MessageContent sentMessage = connectorMessage.getSent();
        MessageContent responseMessage = connectorMessage.getResponse();
        MessageContent responseTransformedMessage = connectorMessage.getResponseTransformed();
        MessageContent processedResponseMessage = connectorMessage.getProcessedResponse();

        MessagesRadioPane.removeAll();

        String content = null;
        String dataType = null;

        content = (rawMessage == null) ? null : rawMessage.getContent();
        dataType = (rawMessage == null) ? null : rawMessage.getDataType();
        if (content != null) {
            MessagesRadioPane.add(RawMessageRadioButton);

        }
        setCorrectDocument(RawMessageTextPane, content, dataType);

        content = (processedRawMessage == null) ? null : processedRawMessage.getContent();
        dataType = (processedRawMessage == null) ? null : processedRawMessage.getDataType();
        if (content != null) {
            MessagesRadioPane.add(ProcessedRawMessageRadioButton);

        }
        setCorrectDocument(ProcessedRawMessageTextPane, content, dataType);

        content = (transformedMessage == null) ? null : transformedMessage.getContent();
        dataType = (transformedMessage == null) ? null : transformedMessage.getDataType();
        if (content != null) {
            MessagesRadioPane.add(TransformedMessageRadioButton);

        }
        setCorrectDocument(TransformedMessageTextPane, content, dataType);

        content = (encodedMessage == null) ? null : encodedMessage.getContent();
        dataType = (encodedMessage == null) ? null : encodedMessage.getDataType();
        if (content != null) {
            MessagesRadioPane.add(EncodedMessageRadioButton);

        }
        setCorrectDocument(EncodedMessageTextPane, content, dataType);

        content = null;
        if (sentMessage != null) {
            if (connectorMessage.getMetaDataId() > 0) {
                Serializer serializer = ObjectXMLSerializer.getInstance();
                ConnectorProperties sentObject = serializer.deserialize(sentMessage.getContent(), ConnectorProperties.class);
                content = (sentObject == null) ? null : sentObject.toFormattedString();
            } else {
                content = sentMessage.getContent();
            }
        }
        dataType = (sentMessage == null) ? null : sentMessage.getDataType();
        if (content != null) {
            MessagesRadioPane.add(SentMessageRadioButton);

        }
        setCorrectDocument(SentMessageTextPane, content, dataType);

        content = null;
        if (responseMessage != null) {
            Serializer serializer = ObjectXMLSerializer.getInstance();
            Response responseObject = serializer.deserialize(responseMessage.getContent(), Response.class);
            if (responseObject != null) {
                String responseStatusMessage = StringUtils.isEmpty(responseObject.getStatusMessage()) ? "" : ": " + responseObject.getStatusMessage();

                responseStatusTextArea.setText(responseObject.getStatus().toString() + responseStatusMessage);
                responseStatusTextArea.setCaretPosition(0);
                content = responseObject.getMessage();
            }
            dataType = (responseMessage == null) ? null : (connectorMessage.getMetaDataId() == 0 ? rawMessage.getDataType() : responseMessage.getDataType());
        }

        if (content != null) {
            MessagesRadioPane.add(ResponseRadioButton);

        }
        setCorrectDocument(ResponseTextArea, content, dataType);

        content = (responseTransformedMessage == null) ? null : responseTransformedMessage.getContent();
        dataType = (responseTransformedMessage == null) ? null : responseTransformedMessage.getDataType();
        if (content != null) {
            MessagesRadioPane.add(ResponseTransformedRadioButton);

        }
        setCorrectDocument(ResponseTransformedTextPane, content, dataType);

        content = null;
        if (processedResponseMessage != null) {
            if (connectorMessage.getMetaDataId() > 0) {
                Serializer serializer = ObjectXMLSerializer.getInstance();
                Response responseObject = serializer.deserialize(processedResponseMessage.getContent(), Response.class);
                if (responseObject != null) {
                    String processedResponseStatusMessage = StringUtils.isEmpty(responseObject.getStatusMessage()) ? "" : ": " + responseObject.getStatusMessage();

                    processedResponseStatusTextArea.setText(responseObject.getStatus().toString() + processedResponseStatusMessage);
                    processedResponseStatusTextArea.setCaretPosition(0);
                    content = responseObject.getMessage();
                }
            } else {
                processedResponseStatusTextArea.setText("");
                processedResponseStatusTextArea.setCaretPosition(0);
                content = processedResponseMessage.getContent();
            }
        }

        dataType = (processedResponseMessage == null) ? null : processedResponseMessage.getDataType();
        if (content != null) {
            MessagesRadioPane.add(ProcessedResponseRadioButton);

        }
        setCorrectDocument(ProcessedResponseTextArea, content, dataType);
    }

    /**
     * Helper function to update the mappings
     */
    private void updateDescriptionMappings(ConnectorMessage connectorMessage) {
        Map<String, Object> sourceMap = connectorMessage.getSourceMap();
        Map<String, Object> connectorMap = connectorMessage.getConnectorMap();
        Map<String, Object> channelMap = connectorMessage.getChannelMap();
        Map<String, Object> responseMap = connectorMessage.getResponseMap();

        int rowCount = 0;

        if (sourceMap != null) {
            rowCount += sourceMap.size();
        }

        if (connectorMap != null) {
            rowCount += connectorMap.size();
        }

        if (channelMap != null) {
            rowCount += channelMap.size();
        }

        if (responseMap != null) {
            rowCount += responseMap.size();
        }

        String[][] tableData = new String[rowCount][3];
        int row = 0;

        if (sourceMap != null) {
            for (Entry<String, Object> variableMapEntry : sourceMap.entrySet()) {
                tableData[row][0] = "Source";
                tableData[row][1] = StringUtil.valueOf(variableMapEntry.getKey());
                tableData[row][2] = StringUtil.valueOf(variableMapEntry.getValue());
                row++;
            }
        }

        if (connectorMap != null) {
            for (Entry<String, Object> variableMapEntry : connectorMap.entrySet()) {
                tableData[row][0] = "Connector";
                tableData[row][1] = StringUtil.valueOf(variableMapEntry.getKey());
                tableData[row][2] = StringUtil.valueOf(variableMapEntry.getValue());
                row++;
            }
        }

        if (channelMap != null) {
            for (Entry<String, Object> variableMapEntry : channelMap.entrySet()) {
                tableData[row][0] = "Channel";
                tableData[row][1] = StringUtil.valueOf(variableMapEntry.getKey());
                tableData[row][2] = StringUtil.valueOf(variableMapEntry.getValue());
                row++;
            }
        }

        if (responseMap != null) {
            for (Entry<String, Object> variableMapEntry : responseMap.entrySet()) {
                tableData[row][0] = "Response";
                tableData[row][1] = StringUtil.valueOf(variableMapEntry.getKey());
                tableData[row][2] = StringUtil.valueOf(variableMapEntry.getValue());
                row++;
            }
        }

        updateMappingsTable(tableData, false);
    }

    /**
     * Helper function to update the error tab
     */
    private void updateDescriptionErrors(ConnectorMessage connectorMessage) {
        String processingError = connectorMessage.getProcessingError();
        String postProcessorError = connectorMessage.getPostProcessorError();
        String responseError = connectorMessage.getResponseError();

        ErrorsRadioPane.removeAll();
        boolean paneSelected = false;
        String firstVisiblePane = null;

        if (processingError != null) {
            ErrorsRadioPane.add(ProcessingErrorRadioButton);
            paneSelected = lastUserSelectedErrorType.equals(ProcessingErrorRadioButton.getText());
            if (firstVisiblePane == null) {
                firstVisiblePane = ProcessingErrorRadioButton.getText();
            }
            setCorrectDocument(ProcessingErrorTextPane, processingError, null);
        }

        if (postProcessorError != null) {
            ErrorsRadioPane.add(PostprocessorErrorRadioButton);
            paneSelected = lastUserSelectedErrorType.equals(PostprocessorErrorRadioButton.getText());
            if (firstVisiblePane == null) {
                firstVisiblePane = PostprocessorErrorRadioButton.getText();
            }
            setCorrectDocument(PostprocessorErrorTextPane, postProcessorError, null);
        }

        if (responseError != null) {
            ErrorsRadioPane.add(ResponseErrorRadioButton);
            paneSelected = lastUserSelectedErrorType.equals(ResponseErrorRadioButton.getText());
            if (firstVisiblePane == null) {
                firstVisiblePane = ResponseErrorRadioButton.getText();
            }
            setCorrectDocument(ResponseErrorTextPane, responseError, null);
        }

        String paneToSelect;
        // Set the default pane if the last user selected one is not added.
        if (!paneSelected) {
            if (firstVisiblePane != null) {
                paneToSelect = firstVisiblePane;
            } else {
                paneToSelect = ProcessingErrorRadioButton.getText();
            }
        } else {
            paneToSelect = lastUserSelectedErrorType;
        }

        JRadioButton button = getRadioButtonForErrorPane(paneToSelect);

        button.setSelected(true);
        showErrorPane(button.getText());
    }

    private void messagesRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JRadioButton messagesRadioButton = (JRadioButton) evt.getSource();

        showMessagePane(messagesRadioButton.getText());

        lastUserSelectedMessageType = messagesRadioButton.getText();
    }

    private void showMessagePane(String messagePaneName) {
        CardLayout cardLayout = (CardLayout) MessagesCardPane.getLayout();
        updateXmlCheckBoxEnabled(messagePaneName);

        cardLayout.show(MessagesCardPane, messagePaneName);
    }

    private void errorsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JRadioButton errorsRadioButton = (JRadioButton) evt.getSource();

        showErrorPane(errorsRadioButton.getText());

        lastUserSelectedErrorType = errorsRadioButton.getText();
    }

    private void showErrorPane(String errorPaneName) {
        CardLayout cardLayout = (CardLayout) ErrorsCardPane.getLayout();

        cardLayout.show(ErrorsCardPane, errorPaneName);
    }

    private void updateXmlCheckBoxEnabled(String messagePaneName) {
        int row = getSelectedMessageIndex();

        if (row >= 0) {
            MessageBrowserTableNode messageNode = (MessageBrowserTableNode) messageTreeTable.getPathForRow(row).getLastPathComponent();

            if (messageNode.isNodeActive()) {
            	String channelId = messageNode.getChannelId();
                Long messageId = messageNode.getMessageId();
                Integer metaDataId = messageNode.getMetaDataId();

                Message message = getMessageFromCache(channelId, messageId);
                ConnectorMessage connectorMessage = message.getConnectorMessages().get(metaDataId);

                MessageContent content = null;

                if (messagePaneName.equals("Raw")) {
                    content = connectorMessage.getRaw();
                } else if (messagePaneName.equals("Processed Raw")) {
                    content = connectorMessage.getProcessedRaw();
                } else if (messagePaneName.equals("Transformed")) {
                    content = connectorMessage.getTransformed();
                } else if (messagePaneName.equals("Encoded")) {
                    content = connectorMessage.getEncoded();
                } else if (messagePaneName.equals("Sent")) {
                    content = connectorMessage.getSent();
                } else if (messagePaneName.equals("Response")) {
                    content = connectorMessage.getResponse();
                } else if (messagePaneName.equals("Response Transformed")) {
                    content = connectorMessage.getResponseTransformed();
                } else if (messagePaneName.equals("Processed Response")) {
                    content = connectorMessage.getProcessedResponse();
                }

                if (content != null && StringUtils.isNotEmpty(content.getContent())) {
                    String trimmedContent = "";

                    if (messagePaneName.equals("Response") || messagePaneName.equals("Processed Response")) {
                        Serializer serializer = ObjectXMLSerializer.getInstance();
                        Response responseObject = serializer.deserialize(content.getContent(), Response.class);
                        if (responseObject != null) {
                            trimmedContent = responseObject.getMessage().trim();
                        }
                    } else {
                        trimmedContent = content.getContent().trim();
                    }

                    if (trimmedContent.length() > 0) {
                        char firstChar = trimmedContent.charAt(0);
                        if (firstChar == '<' || firstChar == '{' || firstChar == '[') {
                            formatMessageCheckBox.setEnabled(true);
                        }
                    }
                }
            } else {
                formatMessageCheckBox.setEnabled(false);
            }
        }
    }

    private void formatCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        int row = getSelectedMessageIndex();

        if (row >= 0) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            MessageBrowserTableNode messageNode = (MessageBrowserTableNode) messageTreeTable.getPathForRow(row).getLastPathComponent();
            String channelId = messageNode.getChannelId();
            Long messageId = messageNode.getMessageId();
            Integer metaDataId = messageNode.getMetaDataId();

            Message message = getMessageFromCache(channelId, messageId);
            ConnectorMessage connectorMessage;

            connectorMessage = message.getConnectorMessages().get(metaDataId);

            updateDescriptionMessages(connectorMessage);

            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    protected void nextPageButtonActionPerformed(ActionEvent evt) {
        loadPageNumber(messages.getPageNumber() + 1);
    }

    protected void previousPageButtonActionPerformed(ActionEvent evt) {
        loadPageNumber(messages.getPageNumber() - 1);
    }

    // @formatter:off
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        messagesGroup = new javax.swing.ButtonGroup();
        errorsGroup = new javax.swing.ButtonGroup();
        jDialog1 = new javax.swing.JDialog();
        jSplitPane1 = new javax.swing.JSplitPane();
        descriptionTabbedPane = new javax.swing.JTabbedPane();
        MessagesPanel = new javax.swing.JPanel();
        MessagesRadioPane = new javax.swing.JPanel();
        RawMessageRadioButton = new javax.swing.JRadioButton();
        ProcessedRawMessageRadioButton = new javax.swing.JRadioButton();
        TransformedMessageRadioButton = new javax.swing.JRadioButton();
        EncodedMessageRadioButton = new javax.swing.JRadioButton();
        SentMessageRadioButton = new javax.swing.JRadioButton();
        ResponseRadioButton = new javax.swing.JRadioButton();
        ResponseTransformedRadioButton = new javax.swing.JRadioButton();
        ProcessedResponseRadioButton = new javax.swing.JRadioButton();
        MessagesCardPane = new javax.swing.JPanel();
        RawMessageTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        ProcessedRawMessageTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        TransformedMessageTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        EncodedMessageTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        SentMessageTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        ResponseTextPane = new javax.swing.JPanel();
        responseStatusLabel = new javax.swing.JLabel();
        responseLabel = new javax.swing.JLabel();
        ResponseTextArea = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        responseStatusTextArea = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        ResponseTransformedTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        ProcessedResponseTextPane = new javax.swing.JPanel();
        ProcessedResponseTextArea = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        processedResponseStatusLabel = new javax.swing.JLabel();
        processedResponseLabel = new javax.swing.JLabel();
        processedResponseStatusTextArea = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        formatMessageCheckBox = new javax.swing.JCheckBox();
        mappingsPane = new javax.swing.JScrollPane();
        mappingsTable = null;
        ErrorsPanel = new javax.swing.JPanel();
        ErrorsRadioPane = new javax.swing.JPanel();
        ProcessingErrorRadioButton = new javax.swing.JRadioButton();
        PostprocessorErrorRadioButton = new javax.swing.JRadioButton();
        ResponseErrorRadioButton = new javax.swing.JRadioButton();
        ErrorsCardPane = new javax.swing.JPanel();
        ProcessingErrorTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        PostprocessorErrorTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        ResponseErrorTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        attachmentsPane = new javax.swing.JScrollPane();
        attachmentTable = null;
        messageScrollPane = new javax.swing.JScrollPane();
        messageTreeTable = new com.mirth.connect.client.ui.components.MirthTreeTable(mirthTreeTablePrefix(), defaultVisibleColumns);
        jPanel1 = new javax.swing.JPanel();
        pageNumberLabel = new javax.swing.JLabel();
        mirthDatePicker1 = new com.mirth.connect.client.ui.components.MirthDatePicker();
        resultsLabel = new javax.swing.JLabel();
        lastSearchCriteriaPane = new javax.swing.JScrollPane();
        lastSearchCriteria = new javax.swing.JTextArea();
        previousPageButton = new javax.swing.JButton();
        statusBoxQueued = new com.mirth.connect.client.ui.components.MirthCheckBox();
        statusBoxPending = new com.mirth.connect.client.ui.components.MirthCheckBox();
        pageTotalLabel = new javax.swing.JLabel();
        textSearchField = new javax.swing.JTextField();
        pageNumberField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel2 = new javax.swing.JLabel();
        textSearchLabel = new javax.swing.JLabel();
        filterButton = new javax.swing.JButton();
        statusBoxFiltered = new com.mirth.connect.client.ui.components.MirthCheckBox();
        pageSizeLabel = new javax.swing.JLabel();
        statusBoxSent = new com.mirth.connect.client.ui.components.MirthCheckBox();
        resetButton = new javax.swing.JButton();
        countButton = new com.mirth.connect.client.ui.components.MirthButton();
        advSearchButton = new javax.swing.JButton();
        pageSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        statusBoxError = new com.mirth.connect.client.ui.components.MirthCheckBox();
        mirthTimePicker2 = new com.mirth.connect.client.ui.components.MirthTimePicker();
        statusBoxReceived = new com.mirth.connect.client.ui.components.MirthCheckBox();
        pageGoButton = new javax.swing.JButton();
        statusBoxTransformed = new com.mirth.connect.client.ui.components.MirthCheckBox();
        mirthTimePicker1 = new com.mirth.connect.client.ui.components.MirthTimePicker();
        jLabel3 = new javax.swing.JLabel();
        allDayCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        mirthDatePicker2 = new com.mirth.connect.client.ui.components.MirthDatePicker();
        nextPageButton = new javax.swing.JButton();
        regexTextSearchCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jList1);

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setBackground(new java.awt.Color(255, 255, 255));
        setFocusable(false);
        setRequestFocusEnabled(false);

        jSplitPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jSplitPane1.setDividerLocation(265);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        descriptionTabbedPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        descriptionTabbedPane.setFocusable(false);

        MessagesPanel.setBackground(new java.awt.Color(255, 255, 255));
        MessagesPanel.setFocusable(false);

        MessagesRadioPane.setBackground(new java.awt.Color(255, 255, 255));
        MessagesRadioPane.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        RawMessageRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        messagesGroup.add(RawMessageRadioButton);
        RawMessageRadioButton.setText("Raw");
        RawMessageRadioButton.setFocusable(false);
        RawMessageRadioButton.setRequestFocusEnabled(false);
        RawMessageRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RawMessageRadioButtonActionPerformed(evt);
            }
        });
        MessagesRadioPane.add(RawMessageRadioButton);

        ProcessedRawMessageRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        messagesGroup.add(ProcessedRawMessageRadioButton);
        ProcessedRawMessageRadioButton.setText("Processed Raw");
        ProcessedRawMessageRadioButton.setFocusable(false);
        ProcessedRawMessageRadioButton.setRequestFocusEnabled(false);
        ProcessedRawMessageRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessedRawMessageRadioButtonActionPerformed(evt);
            }
        });
        MessagesRadioPane.add(ProcessedRawMessageRadioButton);

        TransformedMessageRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        messagesGroup.add(TransformedMessageRadioButton);
        TransformedMessageRadioButton.setText("Transformed");
        TransformedMessageRadioButton.setFocusable(false);
        TransformedMessageRadioButton.setRequestFocusEnabled(false);
        TransformedMessageRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TransformedMessageRadioButtonActionPerformed(evt);
            }
        });
        MessagesRadioPane.add(TransformedMessageRadioButton);

        EncodedMessageRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        messagesGroup.add(EncodedMessageRadioButton);
        EncodedMessageRadioButton.setText("Encoded");
        EncodedMessageRadioButton.setFocusable(false);
        EncodedMessageRadioButton.setRequestFocusEnabled(false);
        EncodedMessageRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EncodedMessageRadioButtonActionPerformed(evt);
            }
        });
        MessagesRadioPane.add(EncodedMessageRadioButton);

        SentMessageRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        messagesGroup.add(SentMessageRadioButton);
        SentMessageRadioButton.setText("Sent");
        SentMessageRadioButton.setFocusable(false);
        SentMessageRadioButton.setRequestFocusEnabled(false);
        SentMessageRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SentMessageRadioButtonActionPerformed(evt);
            }
        });
        MessagesRadioPane.add(SentMessageRadioButton);

        ResponseRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        messagesGroup.add(ResponseRadioButton);
        ResponseRadioButton.setText("Response");
        ResponseRadioButton.setFocusable(false);
        ResponseRadioButton.setRequestFocusEnabled(false);
        ResponseRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ResponseRadioButtonActionPerformed(evt);
            }
        });
        MessagesRadioPane.add(ResponseRadioButton);

        ResponseTransformedRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        messagesGroup.add(ResponseTransformedRadioButton);
        ResponseTransformedRadioButton.setText("Response Transformed");
        ResponseTransformedRadioButton.setFocusable(false);
        ResponseTransformedRadioButton.setRequestFocusEnabled(false);
        ResponseTransformedRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ResponseTransformedRadioButtonActionPerformed(evt);
            }
        });
        MessagesRadioPane.add(ResponseTransformedRadioButton);

        ProcessedResponseRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        messagesGroup.add(ProcessedResponseRadioButton);
        ProcessedResponseRadioButton.setText("Processed Response");
        ProcessedResponseRadioButton.setFocusable(false);
        ProcessedResponseRadioButton.setRequestFocusEnabled(false);
        ProcessedResponseRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessedResponseRadioButtonActionPerformed(evt);
            }
        });
        MessagesRadioPane.add(ProcessedResponseRadioButton);

        MessagesCardPane.setBackground(new java.awt.Color(255, 255, 255));
        MessagesCardPane.setLayout(new java.awt.CardLayout());

        RawMessageTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        RawMessageTextPane.setEditable(false);
        MessagesCardPane.add(RawMessageTextPane, "Raw");

        ProcessedRawMessageTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ProcessedRawMessageTextPane.setEditable(false);
        MessagesCardPane.add(ProcessedRawMessageTextPane, "Processed Raw");

        TransformedMessageTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        TransformedMessageTextPane.setEditable(false);
        MessagesCardPane.add(TransformedMessageTextPane, "Transformed");

        EncodedMessageTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        EncodedMessageTextPane.setEditable(false);
        MessagesCardPane.add(EncodedMessageTextPane, "Encoded");

        SentMessageTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        SentMessageTextPane.setEditable(false);
        MessagesCardPane.add(SentMessageTextPane, "Sent");

        ResponseTextPane.setBackground(new java.awt.Color(255, 255, 255));

        responseStatusLabel.setText("Status:");

        responseLabel.setText("Response:");

        ResponseTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ResponseTextArea.setEditable(false);

        responseStatusTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        responseStatusTextArea.setEditable(false);

        javax.swing.GroupLayout ResponseTextPaneLayout = new javax.swing.GroupLayout(ResponseTextPane);
        ResponseTextPane.setLayout(ResponseTextPaneLayout);
        ResponseTextPaneLayout.setHorizontalGroup(
            ResponseTextPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ResponseTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(ResponseTextPaneLayout.createSequentialGroup()
                .addGroup(ResponseTextPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(responseStatusLabel)
                    .addComponent(responseLabel))
                .addGap(0, 720, Short.MAX_VALUE))
            .addComponent(responseStatusTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        ResponseTextPaneLayout.setVerticalGroup(
            ResponseTextPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ResponseTextPaneLayout.createSequentialGroup()
                .addComponent(responseStatusLabel)
                .addGap(3, 3, 3)
                .addComponent(responseStatusTextArea, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(responseLabel)
                .addGap(3, 3, 3)
                .addComponent(ResponseTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        MessagesCardPane.add(ResponseTextPane, "Response");

        ResponseTransformedTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ResponseTransformedTextPane.setEditable(false);
        MessagesCardPane.add(ResponseTransformedTextPane, "Response Transformed");

        ProcessedResponseTextPane.setBackground(new java.awt.Color(255, 255, 255));

        ProcessedResponseTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ProcessedResponseTextArea.setEditable(false);

        processedResponseStatusLabel.setText("Status:");

        processedResponseLabel.setText("Response:");

        processedResponseStatusTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        processedResponseStatusTextArea.setEditable(false);

        javax.swing.GroupLayout ProcessedResponseTextPaneLayout = new javax.swing.GroupLayout(ProcessedResponseTextPane);
        ProcessedResponseTextPane.setLayout(ProcessedResponseTextPaneLayout);
        ProcessedResponseTextPaneLayout.setHorizontalGroup(
            ProcessedResponseTextPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ProcessedResponseTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(ProcessedResponseTextPaneLayout.createSequentialGroup()
                .addGroup(ProcessedResponseTextPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(processedResponseStatusLabel)
                    .addComponent(processedResponseLabel))
                .addGap(0, 720, Short.MAX_VALUE))
            .addComponent(processedResponseStatusTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        ProcessedResponseTextPaneLayout.setVerticalGroup(
            ProcessedResponseTextPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ProcessedResponseTextPaneLayout.createSequentialGroup()
                .addComponent(processedResponseStatusLabel)
                .addGap(3, 3, 3)
                .addComponent(processedResponseStatusTextArea, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(processedResponseLabel)
                .addGap(3, 3, 3)
                .addComponent(ProcessedResponseTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        MessagesCardPane.add(ProcessedResponseTextPane, "Processed Response");

        formatMessageCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        formatMessageCheckBox.setText("Format XML Messages");
        formatMessageCheckBox.setToolTipText("Pretty print messages that are XML.");
        formatMessageCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatMessageCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout MessagesPanelLayout = new javax.swing.GroupLayout(MessagesPanel);
        MessagesPanel.setLayout(MessagesPanelLayout);
        MessagesPanelLayout.setHorizontalGroup(
            MessagesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MessagesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(MessagesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(MessagesCardPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(MessagesPanelLayout.createSequentialGroup()
                        .addComponent(formatMessageCheckBox)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(MessagesRadioPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        MessagesPanelLayout.setVerticalGroup(
            MessagesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MessagesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(MessagesRadioPane, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(MessagesCardPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(formatMessageCheckBox)
                .addContainerGap())
        );

        descriptionTabbedPane.addTab("Messages", MessagesPanel);

        mappingsPane.setViewportView(mappingsTable);

        descriptionTabbedPane.addTab("Mappings", mappingsPane);

        ErrorsPanel.setBackground(new java.awt.Color(255, 255, 255));
        ErrorsPanel.setFocusable(false);

        ErrorsRadioPane.setBackground(new java.awt.Color(255, 255, 255));
        ErrorsRadioPane.setMinimumSize(new java.awt.Dimension(601, 19));
        ErrorsRadioPane.setPreferredSize(new java.awt.Dimension(601, 19));
        ErrorsRadioPane.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        ProcessingErrorRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        errorsGroup.add(ProcessingErrorRadioButton);
        ProcessingErrorRadioButton.setText("Processing Error");
        ProcessingErrorRadioButton.setFocusable(false);
        ProcessingErrorRadioButton.setRequestFocusEnabled(false);
        ProcessingErrorRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessingErrorRadioButtonActionPerformed(evt);
            }
        });
        ErrorsRadioPane.add(ProcessingErrorRadioButton);

        PostprocessorErrorRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        errorsGroup.add(PostprocessorErrorRadioButton);
        PostprocessorErrorRadioButton.setText("Postprocessor Error");
        PostprocessorErrorRadioButton.setFocusable(false);
        PostprocessorErrorRadioButton.setRequestFocusEnabled(false);
        PostprocessorErrorRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PostprocessorErrorRadioButtonActionPerformed(evt);
            }
        });
        ErrorsRadioPane.add(PostprocessorErrorRadioButton);

        ResponseErrorRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        errorsGroup.add(ResponseErrorRadioButton);
        ResponseErrorRadioButton.setText("Response Error");
        ResponseErrorRadioButton.setFocusable(false);
        ResponseErrorRadioButton.setRequestFocusEnabled(false);
        ResponseErrorRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ResponseErrorRadioButtonActionPerformed(evt);
            }
        });
        ErrorsRadioPane.add(ResponseErrorRadioButton);

        ErrorsCardPane.setBackground(new java.awt.Color(255, 255, 255));
        ErrorsCardPane.setLayout(new java.awt.CardLayout());

        ProcessingErrorTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ProcessingErrorTextPane.setEditable(false);
        ErrorsCardPane.add(ProcessingErrorTextPane, "Processing Error");

        PostprocessorErrorTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        PostprocessorErrorTextPane.setEditable(false);
        ErrorsCardPane.add(PostprocessorErrorTextPane, "Postprocessor Error");

        ResponseErrorTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ResponseErrorTextPane.setEditable(false);
        ErrorsCardPane.add(ResponseErrorTextPane, "Response Error");

        javax.swing.GroupLayout ErrorsPanelLayout = new javax.swing.GroupLayout(ErrorsPanel);
        ErrorsPanel.setLayout(ErrorsPanelLayout);
        ErrorsPanelLayout.setHorizontalGroup(
            ErrorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ErrorsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ErrorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ErrorsCardPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ErrorsRadioPane, javax.swing.GroupLayout.DEFAULT_SIZE, 773, Short.MAX_VALUE))
                .addContainerGap())
        );
        ErrorsPanelLayout.setVerticalGroup(
            ErrorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ErrorsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ErrorsRadioPane, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ErrorsCardPane, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                .addContainerGap())
        );

        descriptionTabbedPane.addTab("Errors", ErrorsPanel);

        attachmentsPane.setViewportView(attachmentTable);

        descriptionTabbedPane.addTab("Attachments", attachmentsPane);

        jSplitPane1.setRightComponent(descriptionTabbedPane);

        messageScrollPane.setViewportView(messageTreeTable);

        jSplitPane1.setLeftComponent(messageScrollPane);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        pageNumberLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pageNumberLabel.setText("Page");

        resultsLabel.setForeground(new java.awt.Color(204, 0, 0));
        resultsLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        resultsLabel.setText("Results");

        lastSearchCriteriaPane.setBorder(null);
        lastSearchCriteriaPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        lastSearchCriteria.setEditable(false);
        lastSearchCriteria.setColumns(20);
        lastSearchCriteria.setForeground(new java.awt.Color(96, 96, 96));
        lastSearchCriteria.setLineWrap(true);
        lastSearchCriteria.setRows(5);
        lastSearchCriteria.setAlignmentX(0.0F);
        lastSearchCriteria.setDisabledTextColor(new java.awt.Color(255, 255, 255));
        lastSearchCriteriaPane.setViewportView(lastSearchCriteria);

        previousPageButton.setText("< Prev");
        previousPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousPageButtonActionPerformed(evt);
            }
        });

        statusBoxQueued.setBackground(new java.awt.Color(255, 255, 255));
        statusBoxQueued.setText("QUEUED");
        statusBoxQueued.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        statusBoxQueued.setToolTipText("The message either has not been attempted to be dispatched yet, or it has failed to dispatch and is waiting in the queue to be attempted again.");

        pageTotalLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pageTotalLabel.setText("of ?");
        pageTotalLabel.setAlignmentY(0.0F);
        pageTotalLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        textSearchField.setToolTipText("<html>\nSearch all message content for the given string. This process could take a long<br/>\ntime depending on the amount of message content currently stored. Any message<br/>\ncontent that was encrypted by this channel will not be searchable.\n</html>");
        textSearchField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textSearchFieldActionPerformed(evt);
            }
        });

        pageNumberField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        pageNumberField.setToolTipText("Enter a page number and press Enter to jump to that page.");
        pageNumberField.setPreferredSize(new java.awt.Dimension(40, 22));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("End Time:");
        jLabel2.setMaximumSize(new java.awt.Dimension(78, 15));

        textSearchLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        textSearchLabel.setText("Text Search:");

        filterButton.setText("Search");
        filterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterButtonActionPerformed(evt);
            }
        });

        statusBoxFiltered.setBackground(new java.awt.Color(255, 255, 255));
        statusBoxFiltered.setText("FILTERED");
        statusBoxFiltered.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        statusBoxFiltered.setMaximumSize(new java.awt.Dimension(83, 23));
        statusBoxFiltered.setMinimumSize(new java.awt.Dimension(83, 23));
        statusBoxFiltered.setToolTipText("The message has been rejected by the destination filter, and will not be dispatched by this destination. Other destinations may still dispatch this message.");

        pageSizeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pageSizeLabel.setText("Page Size:");
        pageSizeLabel.setMaximumSize(new java.awt.Dimension(78, 15));

        statusBoxSent.setBackground(new java.awt.Color(255, 255, 255));
        statusBoxSent.setText("SENT");
        statusBoxSent.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        statusBoxSent.setToolTipText("The message has been successfully dispatched / written out by the destination connector.");

        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        countButton.setText("Count");
        countButton.setToolTipText("Count the number of overall messages for the current search criteria.");
        countButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                countButtonActionPerformed(evt);
            }
        });

        advSearchButton.setText("Advanced...");
        advSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advSearchButtonActionPerformed(evt);
            }
        });

        pageSizeField.setToolTipText("<html>\nAfter changing the page size, a new search must be performed for the changes to<br/>\ntake effect.  The default page size can also be configured on the Settings panel.\n</html>");

        statusBoxError.setBackground(new java.awt.Color(255, 255, 255));
        statusBoxError.setText("ERROR");
        statusBoxError.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        statusBoxError.setToolTipText("An error occurred while processing the message through the destination connector.");

        statusBoxReceived.setBackground(new java.awt.Color(255, 255, 255));
        statusBoxReceived.setText("RECEIVED");
        statusBoxReceived.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        statusBoxReceived.setPreferredSize(new java.awt.Dimension(90, 22));
        statusBoxReceived.setToolTipText("The inbound data for the destination connector has been committed to the database, but the destination has not yet finished processing the message.");

        statusBoxPending.setBackground(new java.awt.Color(255, 255, 255));
        statusBoxPending.setText("PENDING");
        statusBoxPending.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        statusBoxPending.setToolTipText("The destination was able to dispatch / write the message outbound, but has not yet finished processing the message through the response transformer.");
        
        pageGoButton.setText("Go");
        pageGoButton.setNextFocusableComponent(messageTreeTable);
        pageGoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pageGoButtonActionPerformed(evt);
            }
        });

        statusBoxTransformed.setBackground(new java.awt.Color(255, 255, 255));
        statusBoxTransformed.setText("TRANSFORMED");
        statusBoxTransformed.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        statusBoxTransformed.setMaximumSize(new java.awt.Dimension(83, 23));
        statusBoxTransformed.setMinimumSize(new java.awt.Dimension(83, 23));
        statusBoxTransformed.setToolTipText("The message has passed the source filter/transformer, and the source encoded data has been dispatched to any destinations.");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Start Time:");
        jLabel3.setMaximumSize(new java.awt.Dimension(78, 15));

        allDayCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        allDayCheckBox.setText("All Day");
        allDayCheckBox.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        allDayCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allDayCheckBoxActionPerformed(evt);
            }
        });

        nextPageButton.setText("Next >");
        nextPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextPageButtonActionPerformed(evt);
            }
        });

        regexTextSearchCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        regexTextSearchCheckBox.setText("Regex");
        regexTextSearchCheckBox.setToolTipText("<html> Search all message content for a match to the regular expression pattern.<br/> Regex matching could be a very costly operation and should be used with<br/> caution, specially with large amount of messages. Any message content<br/> that was encrypted by this channel will not be searchable. Only supported<br/> on PostgreSQL, Oracle and MySQL databases.</html> ");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pageSizeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(textSearchLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(pageSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(advSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addComponent(mirthDatePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(mirthTimePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addComponent(mirthDatePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(mirthTimePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(textSearchField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(allDayCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(regexTextSearchCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statusBoxQueued, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusBoxSent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusBoxError, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusBoxPending, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(statusBoxReceived, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(statusBoxFiltered, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(statusBoxTransformed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(117, 117, 117)
                .addComponent(lastSearchCriteriaPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(previousPageButton, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nextPageButton, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(resultsLabel)
                        .addGap(5, 5, 5)
                        .addComponent(countButton, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(pageNumberLabel)
                        .addGap(4, 4, 4)
                        .addComponent(pageNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(pageTotalLabel)
                        .addGap(5, 5, 5)
                        .addComponent(pageGoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {mirthTimePicker1, mirthTimePicker2});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(mirthTimePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(mirthDatePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(7, 7, 7)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(mirthTimePicker2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(mirthDatePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(7, 7, 7)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(textSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(textSearchLabel)
                                    .addComponent(regexTextSearchCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(countButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(resultsLabel))
                                .addGap(7, 7, 7)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(pageGoButton)
                                    .addComponent(pageTotalLabel)
                                    .addComponent(pageNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(pageNumberLabel))))
                        .addGap(7, 7, 7)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(nextPageButton)
                            .addComponent(previousPageButton)
                            .addComponent(filterButton)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(resetButton)
                                .addComponent(advSearchButton)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(4, 4, 4)
                                    .addComponent(pageSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(1, 1, 1)
                                    .addComponent(pageSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lastSearchCriteriaPane, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(statusBoxReceived, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(allDayCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(statusBoxTransformed, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(statusBoxFiltered, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(statusBoxQueued, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(statusBoxSent, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(statusBoxError, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(statusBoxPending, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // @formatter:on
    
    public String mirthTreeTablePrefix() {
        return "messageBrowser";
    }

    private void advSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advSearchButtonActionPerformed
        advancedSearchPopup.setBackground(UIConstants.COMBO_BOX_BACKGROUND);
        advancedSearchPopup.setLocationRelativeTo(parent);
        advancedSearchPopup.setVisible(true);

        updateAdvancedSearchButtonFont();
    }//GEN-LAST:event_advSearchButtonActionPerformed

    private void filterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterButtonActionPerformed
        runSearch();
    }//GEN-LAST:event_filterButtonActionPerformed

    private void countButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_countButtonActionPerformed
        final String workingId = parent.startWorking("Counting search result size...");
        filterButton.setEnabled(false);
        nextPageButton.setEnabled(false);
        previousPageButton.setEnabled(false);
        countButton.setEnabled(false);
        pageGoButton.setEnabled(false);
        final MessageBrowser messageBrowser = this;

        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }

        worker = new SwingWorker<Void, Void>() {
            private Exception e;

            public Void doInBackground() {
                try {
                    messages.setItemCount(getMessageCount());
                } catch (ClientException e) {
                    if (e instanceof RequestAbortedException) {
                        // The client is no longer waiting for the count request
                    } else {
                        parent.alertThrowable(parent, e);
                    }
                    cancel(true);
                }

                return null;
            }

            public void done() {
                if (!isCancelled()) {
                    if (e != null) {
                        countButton.setEnabled(true);
                        parent.alertThrowable(messageBrowser, e);
                    } else {
                        updatePagination();
                        countButton.setEnabled(false);
                    }
                    filterButton.setEnabled(true);
                }

                parent.stopWorking(workingId);
            }
        };

        worker.execute();
    }//GEN-LAST:event_countButtonActionPerformed
    
    protected Long getMessageCount() throws ClientException {
    	return parent.mirthClient.getMessageCount(channelId, messageFilter);
    }

    private void formatMessageCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatMessageCheckBoxActionPerformed
        formatCheckBoxActionPerformed(evt);
    }//GEN-LAST:event_formatMessageCheckBoxActionPerformed

    private void RawMessageRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RawMessageRadioButtonActionPerformed
        messagesRadioButtonActionPerformed(evt);
    }//GEN-LAST:event_RawMessageRadioButtonActionPerformed

    private void ProcessedRawMessageRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ProcessedRawMessageRadioButtonActionPerformed
        messagesRadioButtonActionPerformed(evt);
    }//GEN-LAST:event_ProcessedRawMessageRadioButtonActionPerformed

    private void TransformedMessageRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TransformedMessageRadioButtonActionPerformed
        messagesRadioButtonActionPerformed(evt);
    }//GEN-LAST:event_TransformedMessageRadioButtonActionPerformed

    private void EncodedMessageRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EncodedMessageRadioButtonActionPerformed
        messagesRadioButtonActionPerformed(evt);
    }//GEN-LAST:event_EncodedMessageRadioButtonActionPerformed

    private void SentMessageRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SentMessageRadioButtonActionPerformed
        messagesRadioButtonActionPerformed(evt);
    }//GEN-LAST:event_SentMessageRadioButtonActionPerformed

    private void ResponseRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ResponseRadioButtonActionPerformed
        messagesRadioButtonActionPerformed(evt);
    }//GEN-LAST:event_ResponseRadioButtonActionPerformed

    private void ProcessedResponseRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ProcessedResponseRadioButtonActionPerformed
        messagesRadioButtonActionPerformed(evt);
    }//GEN-LAST:event_ProcessedResponseRadioButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        resetSearchCriteria();
    }//GEN-LAST:event_resetButtonActionPerformed

    private void allDayCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allDayCheckBoxActionPerformed
        mirthTimePicker1.setEnabled(mirthDatePicker1.getDate() != null && !allDayCheckBox.isSelected());
        mirthTimePicker2.setEnabled(mirthDatePicker2.getDate() != null && !allDayCheckBox.isSelected());
    }//GEN-LAST:event_allDayCheckBoxActionPerformed

    private void pageGoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pageGoButtonActionPerformed
        jumpToPageNumber();
    }//GEN-LAST:event_pageGoButtonActionPerformed

    private void ProcessingErrorRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ProcessingErrorRadioButtonActionPerformed
        errorsRadioButtonActionPerformed(evt);
    }//GEN-LAST:event_ProcessingErrorRadioButtonActionPerformed

    private void ResponseErrorRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ResponseErrorRadioButtonActionPerformed
        errorsRadioButtonActionPerformed(evt);
    }//GEN-LAST:event_ResponseErrorRadioButtonActionPerformed

    private void ResponseTransformedRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ResponseTransformedRadioButtonActionPerformed
        messagesRadioButtonActionPerformed(evt);
    }//GEN-LAST:event_ResponseTransformedRadioButtonActionPerformed

    private void PostprocessorErrorRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PostprocessorErrorRadioButtonActionPerformed
        errorsRadioButtonActionPerformed(evt);
    }//GEN-LAST:event_PostprocessorErrorRadioButtonActionPerformed

    private void textSearchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textSearchFieldActionPerformed
        runSearch();
    }//GEN-LAST:event_textSearchFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton EncodedMessageRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea EncodedMessageTextPane;
    private javax.swing.JPanel ErrorsCardPane;
    private javax.swing.JPanel ErrorsPanel;
    private javax.swing.JPanel ErrorsRadioPane;
    private javax.swing.JPanel MessagesCardPane;
    private javax.swing.JPanel MessagesPanel;
    private javax.swing.JPanel MessagesRadioPane;
    private javax.swing.JRadioButton PostprocessorErrorRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea PostprocessorErrorTextPane;
    private javax.swing.JRadioButton ProcessedRawMessageRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea ProcessedRawMessageTextPane;
    private javax.swing.JRadioButton ProcessedResponseRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea ProcessedResponseTextArea;
    private javax.swing.JPanel ProcessedResponseTextPane;
    private javax.swing.JRadioButton ProcessingErrorRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea ProcessingErrorTextPane;
    private javax.swing.JRadioButton RawMessageRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea RawMessageTextPane;
    private javax.swing.JRadioButton ResponseErrorRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea ResponseErrorTextPane;
    private javax.swing.JRadioButton ResponseRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea ResponseTextArea;
    private javax.swing.JPanel ResponseTextPane;
    private javax.swing.JRadioButton ResponseTransformedRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea ResponseTransformedTextPane;
    private javax.swing.JRadioButton SentMessageRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea SentMessageTextPane;
    private javax.swing.JRadioButton TransformedMessageRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea TransformedMessageTextPane;
    private javax.swing.JButton advSearchButton;
    private com.mirth.connect.client.ui.components.MirthCheckBox allDayCheckBox;
    private com.mirth.connect.client.ui.components.MirthTable attachmentTable;
    private javax.swing.JScrollPane attachmentsPane;
    private com.mirth.connect.client.ui.components.MirthButton countButton;
    private javax.swing.JTabbedPane descriptionTabbedPane;
    private javax.swing.ButtonGroup errorsGroup;
    private javax.swing.JButton filterButton;
    private javax.swing.JCheckBox formatMessageCheckBox;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea lastSearchCriteria;
    private javax.swing.JScrollPane lastSearchCriteriaPane;
    private javax.swing.JScrollPane mappingsPane;
    private com.mirth.connect.client.ui.components.MirthTable mappingsTable;
    private javax.swing.JScrollPane messageScrollPane;
    private com.mirth.connect.client.ui.components.MirthTreeTable messageTreeTable;
    private javax.swing.ButtonGroup messagesGroup;
    private com.mirth.connect.client.ui.components.MirthDatePicker mirthDatePicker1;
    private com.mirth.connect.client.ui.components.MirthDatePicker mirthDatePicker2;
    private com.mirth.connect.client.ui.components.MirthTimePicker mirthTimePicker1;
    private com.mirth.connect.client.ui.components.MirthTimePicker mirthTimePicker2;
    private javax.swing.JButton nextPageButton;
    private javax.swing.JButton pageGoButton;
    private com.mirth.connect.client.ui.components.MirthTextField pageNumberField;
    private javax.swing.JLabel pageNumberLabel;
    private com.mirth.connect.client.ui.components.MirthTextField pageSizeField;
    private javax.swing.JLabel pageSizeLabel;
    private javax.swing.JLabel pageTotalLabel;
    private javax.swing.JButton previousPageButton;
    private javax.swing.JLabel processedResponseLabel;
    private javax.swing.JLabel processedResponseStatusLabel;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea processedResponseStatusTextArea;
    private com.mirth.connect.client.ui.components.MirthCheckBox regexTextSearchCheckBox;
    private javax.swing.JButton resetButton;
    private javax.swing.JLabel responseLabel;
    private javax.swing.JLabel responseStatusLabel;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea responseStatusTextArea;
    private javax.swing.JLabel resultsLabel;
    private com.mirth.connect.client.ui.components.MirthCheckBox statusBoxError;
    private com.mirth.connect.client.ui.components.MirthCheckBox statusBoxFiltered;
    private com.mirth.connect.client.ui.components.MirthCheckBox statusBoxQueued;
    private com.mirth.connect.client.ui.components.MirthCheckBox statusBoxPending;
    private com.mirth.connect.client.ui.components.MirthCheckBox statusBoxReceived;
    private com.mirth.connect.client.ui.components.MirthCheckBox statusBoxSent;
    private com.mirth.connect.client.ui.components.MirthCheckBox statusBoxTransformed;
    private javax.swing.JTextField textSearchField;
    private javax.swing.JLabel textSearchLabel;
    // End of variables declaration//GEN-END:variables
}

/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.text.DateFormatter;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.EDITokenMarker;
import org.syntax.jedit.tokenmarker.HL7TokenMarker;
import org.syntax.jedit.tokenmarker.X12TokenMarker;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.MessageImportResult;
import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.Operations;
import com.mirth.connect.client.core.RequestAbortedException;
import com.mirth.connect.client.ui.EditMessageDialog;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PaginatedMessageList;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.SortableHeaderCellRenderer;
import com.mirth.connect.client.ui.SortableTreeTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.ViewContentDialog;
import com.mirth.connect.client.ui.components.MirthDatePicker;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthSyntaxTextArea;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTimePicker;
import com.mirth.connect.client.ui.util.DisplayUtil;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.converters.DataTypeFactory;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.model.filters.elements.MetaDataSearchElement;
import com.mirth.connect.model.filters.elements.MetaDataSearchOperator;
import com.mirth.connect.plugins.AttachmentViewer;
import com.mirth.connect.util.XmlUtil;

/**
 * The message browser panel.
 */
public class MessageBrowser extends javax.swing.JPanel {
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
    private Frame parent;
    private Channel channel;
    private MessageBrowserTableModel tableModel;
    private PaginatedMessageList messages;
    private Map<Long, Message> messageCache;
    private Map<Long, List<Attachment>> attachmentCache;
    private MessageFilter messageFilter;
    private MessageBrowserAdvancedFilter advancedSearchPopup;
    private MessageBrowserExportResults exportResultsPopup;
    private JPopupMenu attachmentPopupMenu;
    private final String[] columns = new String[] { "ID", "Connector", "Status", "Date & Time", "Server Id", "Send Attempts", "Import ID", "Reply Sent" };
    // Worker used for loading a page and counting the total number of messages
    private SwingWorker<Void, Void> worker;
    
    /**
     * Constructs the new message browser and sets up its default
     * information/layout
     */
    @SuppressWarnings("serial")
    public MessageBrowser() {
        this.parent = PlatformUI.MIRTH_FRAME;
        
        messageCache = Collections.synchronizedMap(new LinkedHashMap<Long, Message>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Message> eldest) {
                return size() > MAX_CACHE_SIZE;
             }
        });
        
        attachmentCache = Collections.synchronizedMap(new LinkedHashMap<Long, List<Attachment>>(){
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, List<Attachment>> eldest) {
                return size() > MAX_CACHE_SIZE;
             }
        });

        // Generated by Netbeans
        initComponents();
        // Add additional initializations here
        initComponentsManual();

        //Initialize the message table
        makeMessageTable();
        //Initialize the mappings table
        makeMappingsTable();
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

        pageSizeField.setDocument(new MirthFieldConstraints(3, false, false, true));
        pageNumberField.setDocument(new MirthFieldConstraints(7, false, false, true));

        advancedSearchPopup = new MessageBrowserAdvancedFilter(parent, this, "Advanced Search Filter", true, true);
        advancedSearchPopup.setVisible(false);

        exportResultsPopup = new MessageBrowserExportResults(parent, false);
        exportResultsPopup.setVisible(false);

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
            }

        });
    }

    public void loadChannel(Channel channel) {
        //Set the FormatXmlCheckboxes to their default setting
        formatXmlMessageCheckBox.setSelected(Preferences.userNodeForPackage(Mirth.class).getBoolean("messageBrowserFormatXml", true));
    	
        this.channel = channel;
        tableModel.clear();
        advancedSearchPopup.loadChannel(channel);
        exportResultsPopup.loadChannel(channel.getId());
        resetSearchCriteria();
        lastUserSelectedMessageType = "Raw";
        updateMessageRadioGroup();

        // Remove all columns
        for (TableColumn tableColumn : messageTreeTable.getColumns(true)) {
            messageTreeTable.removeColumn(tableColumn);
        }
        
        List<String> columnList = new ArrayList<String>();
        // Add standard columns
        columnList.addAll(Arrays.asList(columns));
        // Add custom columns
        columnList.addAll(getMetaDataColumns());
        tableModel.setColumnIdentifiers(columnList);
        
        // Create the column objects and add them to the message table
        ColumnFactory columnFactory = messageTreeTable.getColumnFactory();
        for (int modelIndex = 0; modelIndex < columnList.size(); modelIndex++) {
        	TableColumnExt column = columnFactory.createAndConfigureTableColumn(messageTreeTable.getModel(), modelIndex);
        	String columnName = column.getTitle();
        	
        	boolean defaultVisible = false;
        	if (columnName.equals("ID") || columnName.equals("Connector") || columnName.equals("Status") || columnName.equals("Date & Time")) {
        		defaultVisible = true;
        	}
        	
        	column.setVisible(Preferences.userNodeForPackage(Mirth.class).getBoolean("messageBrowserVisibleColumn" + columnName, defaultVisible));
        	
            messageTreeTable.addColumn(column);
        }
    }

    public List<Operation> getAbortOperations() {
        List<Operation> operations = new ArrayList<Operation>();

        operations.add(Operations.GET_MESSAGES);
        operations.add(Operations.GET_MESSAGE_CONTENT);
        operations.add(Operations.GET_SEARCH_COUNT);
        operations.add(Operations.MESSAGE_REMOVE);

        return operations;
    }

    public void resetSearchCriteria() {
        mirthDatePicker1.setDate(null);
        mirthDatePicker2.setDate(null);
        quickSearchField.setText("");
        allDayCheckBox.setSelected(false);
        statusBoxReceived.setSelected(false);
        statusBoxTransformed.setSelected(false);
        statusBoxFiltered.setSelected(false);
        statusBoxQueued.setSelected(false);
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

    public List<String> getMetaDataColumns() {
        List<String> metaDataColumnNames = new ArrayList<String>();
        for (MetaDataColumn column : channel.getProperties().getMetaDataColumns()) {
            metaDataColumnNames.add(column.getName());
        }

        return metaDataColumnNames;
    }

    public Channel getChannel() {
        return channel;
    }

    public MessageFilter getMessageFilter() {
        return messageFilter;
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

    /**
     * Constructs the MessageFilter (this.filter) based on the current form
     * selections
     */
    private void generateMessageFilter() {
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
            return;
        }

        Calendar startDate = messageFilter.getStartDate();
        Calendar endDate = messageFilter.getEndDate();

        if (startDate != null && endDate != null && startDate.getTimeInMillis() > endDate.getTimeInMillis()) {
            parent.alertError(parent, "Start date cannot be after the end date.");
            return;
        }

        // set quick search
        String quickSearch = StringUtils.trim(quickSearchField.getText());

        if (quickSearch.length() > 0) {
            messageFilter.setQuickSearch(quickSearch);
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

        if (!statuses.isEmpty()) {
            messageFilter.setStatuses(statuses);
        }

        advancedSearchPopup.applySelectionsToFilter(messageFilter);
        exportResultsPopup.setMessageFilter(messageFilter);

        try {
            Long maxMessageId = parent.mirthClient.getMaxMessageId(channel.getId());
            messageFilter.setMaxMessageId(maxMessageId);
        } catch (ClientException e) {
            parent.alertException(parent, e.getStackTrace(), e.getMessage());
            return;
        }
    }

    public void runSearch() {
        generateMessageFilter();
        updateFilterButtonFont(Font.PLAIN);
        messages = new PaginatedMessageList();
        messages.setClient(parent.mirthClient);
        messages.setChannelId(channel.getId());
        messages.setMessageFilter(messageFilter);

        try {
            messages.setPageSize(Integer.parseInt(pageSizeField.getText()));
        } catch (NumberFormatException e) {
            parent.alertError(parent, "Invalid page size.");
            return;
        }

        countButton.setVisible(true);
        clearCache();
        loadPageNumber(1);
        exportResultsPopup.setPageSize(messages.getPageSize());

        updateSearchCriteriaPane();
    }

    private void updateSearchCriteriaPane() {
        StringBuilder text = new StringBuilder();
        Calendar startDate = messageFilter.getStartDate();
        Calendar endDate = messageFilter.getEndDate();
        String padding = "\n";

        text.append("ID <= ");
        text.append(messageFilter.getMaxMessageId());

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

        if (messageFilter.getQuickSearch() != null) {
            text.append(padding + "Quick Search: " + messageFilter.getQuickSearch());
        }

        text.append(padding + "Connectors: ");

        if (messageFilter.getMetaDataIds() == null) {
            text.append("(any)");
        } else if (messageFilter.getMetaDataIds().isEmpty()) {
            text.append("(none)");
        } else {
            List<Integer> metaDataIds = messageFilter.getMetaDataIds();
            List<String> connectorNames = new ArrayList<String>();

            if (metaDataIds.contains(0)) {
                connectorNames.add("Source");
            }

            for (Connector connector : channel.getDestinationConnectors()) {
                if (metaDataIds.contains(connector.getMetaDataId())) {
                    connectorNames.add(connector.getName());
                }
            }

            text.append(StringUtils.join(connectorNames, ", "));
        }

        if (messageFilter.getMessageId() != null) {
            text.append(padding + "Message ID: " + messageFilter.getMessageId());
        }

        if (messageFilter.getServerId() != null) {
            text.append(padding + "Server ID: " + messageFilter.getServerId());
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
            Map<ContentType, String> contentSearch = messageFilter.getContentSearch();

            for (Entry<ContentType, String> entry : contentSearch.entrySet()) {
                text.append(padding + entry.getKey() + " contains \"" + entry.getValue() + "\"");
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

        lastSearchCriteria.setText(text.toString());
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

        worker = new SwingWorker<Void, Void>() {
            private boolean foundItems = false;
            private int retrievedPageNumber = 1;

            public Void doInBackground() {

                try {
                    foundItems = messages.loadPageNumber(pageNumber);
                } catch (Throwable t) { // catch Throwable in case the client runs out of memory

                    if (t.getMessage().contains("Java heap space")) {
                        parent.alertError(parent, "There was an out of memory error when trying to retrieve messages.\nIncrease your heap size or decrease your page size and search again.");
                    } else if (t.getCause() instanceof RequestAbortedException) {
                        // The client is no longer waiting for the search request
                    } else {
                        parent.alertException(parent, t.getStackTrace(), t.getMessage());
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
                    filterButton.setEnabled(true);
                }
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

    //TODO double check references to see whether cache should be cleared in those cases
    /**
     * Refreshes the panel with the current filter information and clears the
     * message cache if needed
     */
    public void refresh(Integer page) {
        clearCache();

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
     * Shows or hides message tabs depending on what part of the message is
     * selected
     */
    public void updateDescriptionTabs(Integer metaDataId, boolean errors, boolean attachment) {
        //Save the current open tab
        String title = descriptionTabbedPane.getTitleAt(descriptionTabbedPane.getSelectedIndex());
        //Remove all tabs
        descriptionTabbedPane.removeAll();
        descriptionTabbedPane.addTab("Messages", MessagesPanel);
        descriptionTabbedPane.addTab("Mappings", mappingsPane);
        
        if (errors) {
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
        } else if (messagePaneName.equals("Processed Response")) {
            return ProcessedResponseRadioButton;
        } else {

            return null;
        }
    }

    private JRadioButton getRadioButtonForErrorPane(String errorPaneName) {
        if (errorPaneName.equals("Processing Error")) {
            return ProcessingErrorRadioButton;
        } else if (errorPaneName.equals("Reply Error")) {
            return ReplyErrorRadioButton;
        } else {

            return null;
        }
    }

    private void setCorrectDocument(MirthSyntaxTextArea textPane, String message, String dataType) {
        SyntaxDocument newDoc = new SyntaxDocument();

        if (message != null) {
            if (dataType != null) {

                if (dataType.equals(DataTypeFactory.HL7V2) || dataType.equals(DataTypeFactory.NCPDP) || dataType.equals(DataTypeFactory.DICOM)) {
                    newDoc.setTokenMarker(new HL7TokenMarker());
                } else if (dataType.equals(DataTypeFactory.XML) || dataType.equals(DataTypeFactory.HL7V3)) {
                    newDoc.setTokenMarker(new XMLTokenMarker());

                    if (formatXmlMessageCheckBox.isSelected()) {
                        try {
                            message = XmlUtil.prettyPrint(message);
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                } else if (dataType.equals(DataTypeFactory.X12)) {
                    newDoc.setTokenMarker(new X12TokenMarker());
                } else if (dataType.equals(DataTypeFactory.EDI)) {
                    newDoc.setTokenMarker(new EDITokenMarker());
                }
            }

            textPane.setDocument(newDoc);
            textPane.setText(message);
        } else {
            textPane.setDocument(newDoc);
            textPane.setText("");
        }

        textPane.setCaretPosition(0);
    }

    /**
     * Sets the properties and adds the listeners for the Message Table. No data
     * is loaded at this point.
     */
    private void makeMessageTable() {
        messageTreeTable.setDragEnabled(false);
        //messageTreeTable.setFocusable(false);
        messageTreeTable.setSortable(false);
        messageTreeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messageTreeTable.setColumnFactory(new MessageBrowserTableColumnFactory());
        messageTreeTable.setLeafIcon(null);
        messageTreeTable.setOpenIcon(null);
        messageTreeTable.setClosedIcon(null);
        messageTreeTable.setAutoCreateColumnsFromModel(false);
        messageTreeTable.setColumnControlVisible(true);
        messageTreeTable.setShowGrid(true, true);

        tableModel = new MessageBrowserTableModel();
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
                            Long messageId = messageNode.getMessageId();
                            Integer metaDataId = messageNode.getMetaDataId();

                            Message currentMessage = messageCache.get(messageId);

                            ConnectorMessage connectorMessage;
                            List<Integer> selectedMetaDataIds = new ArrayList<Integer>();

                            connectorMessage = currentMessage.getConnectorMessages().get(metaDataId);
                            if (metaDataId == 0) {
                                selectedMetaDataIds = null;
                            } else {
                                selectedMetaDataIds.add(metaDataId);
                            }

                            if (connectorMessage.getRaw() != null) {
                                new EditMessageDialog(connectorMessage.getRaw().getContent(), channel.getSourceConnector().getTransformer().getInboundDataType(), channel.getId(), parent.dashboardPanel.getDestinationConnectorNames(channel.getId()), selectedMetaDataIds);
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

        // Add custom table header renderer to generate sorting arrows
        JTableHeader header = messageTreeTable.getTableHeader();
        header.setDefaultRenderer(new SortableHeaderCellRenderer(header.getDefaultRenderer()));

        // Add mouse listener to detect clicks on column header
        header.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {

                    //TODO Disabling sorting for now. Revisit the question once custom columns are added.
                    /*
                     * JTableHeader h = (JTableHeader) e.getSource();
                     * TableColumnModel columnModel = h.getColumnModel();
                     * 
                     * int viewColumn = h.columnAtPoint(e.getPoint());
                     * int column =
                     * columnModel.getColumn(viewColumn).getModelIndex();
                     * 
                     * if (column != -1 &&
                     * messageTreeTable.getColumnExt(messageTreeTable
                     * .getTreeTableModel().getColumnName(column)).isSortable())
                     * {
                     * // Toggle sort order (ascending <-> descending)
                     * SortableTreeTableModel model = (SortableTreeTableModel)
                     * messageTreeTable.getTreeTableModel();
                     * model.setColumnAndToggleSortOrder(column);
                     * 
                     * // Set sorting icon and current column index
                     * ((SortableHeaderCellRenderer)
                     * messageTreeTable.getTableHeader
                     * ().getDefaultRenderer()).setSortingIcon
                     * (model.getSortOrder());
                     * ((SortableHeaderCellRenderer)
                     * messageTreeTable.getTableHeader
                     * ().getDefaultRenderer()).setColumnIndex(column);
                     * }
                     */
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    // Use this model in order to get the total number of columns.
                    // If the column model is used, it getColumnCount only returns the number of visible columns
                    SortableTreeTableModel model = (SortableTreeTableModel) messageTreeTable.getTreeTableModel();
                    JPopupMenu columnMenu = new JPopupMenu();

                    for (int i = 0; i < model.getColumnCount(); i++) {
                        String columnName = model.getColumnName(i);
                        // Get the column object by name. Using an index may not return the column object if the column is hidden
                        TableColumnExt column = messageTreeTable.getColumnExt(columnName);
                        
                        // Create the menu item
                        final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(columnName);
                        // Show or hide the checkbox
                        menuItem.setSelected(column.isVisible());

                        menuItem.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent arg0) {
                                TableColumnExt column = messageTreeTable.getColumnExt(menuItem.getText());
                                // Determine whether to show or hide the selected column
                                boolean enable = !column.isVisible();
                                // Do not hide a column if it is the last remaining visible column              
                                if (enable) {
                                    column.setVisible(true);
                                } else {
                                    if (messageTreeTable.getColumnCount() > 1) {
                                        column.setVisible(false);
                                    }
                                }
                            }
                        });

                        columnMenu.add(menuItem);
                    }

                    columnMenu.addSeparator();

                    JMenuItem menuItem = new JMenuItem("Collapse All");
                    menuItem.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            messageTreeTable.collapseAll();
                        }

                    });
                    columnMenu.add(menuItem);

                    menuItem = new JMenuItem("Expand All");
                    menuItem.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            messageTreeTable.expandAll();
                        }

                    });
                    columnMenu.add(menuItem);

                    // Show the popup menu at the mouse clicked location
                    columnMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    /**
     * Sets the properties and adds the listeners for the Mappings Table. No
     * data
     * is loaded at this point.
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

            mappingsTable.setModel(new RefreshTableModel(tableData, new String[] { SCOPE_COLUMN_NAME, KEY_COLUMN_NAME, VALUE_COLUMN_NAME }) {

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

    public void updateAttachmentsTable(Long messageId) {

        Object[][] tableData = updateAttachmentList(messageId);

        // Create attachment Table if it has not been created yet. 
        if (attachmentTable != null) {
            RefreshTableModel model = (RefreshTableModel) attachmentTable.getModel();
            if (tableData != null) {
                model.refreshDataVector(tableData);
            }
        } else {
            attachmentTable = new MirthTable();
            attachmentTable.setModel(new RefreshTableModel(tableData, new String[] { NUMBER_COLUMN_NAME, TYPE_COLUMN_NAME, ATTACHMENTID_COLUMN_NAME }) {

                boolean[] canEdit = new boolean[] { false, false, false };

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
            attachmentTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent evt) {
                    if (attachmentTable != null && attachmentTable.getSelectedRow() != -1) {
                        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 9, 9, true);
                    } else {
                        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 9, 9, false);
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

    public Object[][] updateAttachmentList(Long messageId) {
        if (messageId == null) {
            return null;
        }
        try {
            List<Attachment> attachments = attachmentCache.get(messageId);
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
                if (type.toUpperCase().contains(plugin.getViewerType().toUpperCase())) {
                    return plugin;
                }
            }
        }
        return null;
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been
     * pushed. Deselects the rows if no row was selected.
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
     * Shows the popup menu when the trigger button (right-click) has been
     * pushed. Deselects the rows if no row was selected.
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
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 9, 9, false);
//        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 7, 7, true);
        if (attachmentTable != null) {
            attachmentTable.clearSelection();
//            clearDescription();
        }
    }

    /**
     * Clears all description information.
     */
    public void clearDescription(String text) {
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 6, -1, false);
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 7, 7, true);

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
        ResponseTextPane.setDocument(new SyntaxDocument());
        ResponseTextPane.setText(text != null ? text : "Select a message to view the response message.");
        ProcessedResponseTextPane.setDocument(new SyntaxDocument());
        ProcessedResponseTextPane.setText(text != null ? text : "Select a message to view the processed response message.");
        ProcessingErrorTextPane.setDocument(new SyntaxDocument());
        ProcessingErrorTextPane.setText(text != null ? text : "Select a message to view any errors.");
        ReplyErrorTextPane.setDocument(new SyntaxDocument());
        ReplyErrorTextPane.setText(text != null ? text : "Select a message to view any errors.");
        updateMappingsTable(new String[0][0], true);
        updateAttachmentsTable(null);
        descriptionTabbedPane.remove(attachmentsPane);
        formatXmlMessageCheckBox.setEnabled(false);
    }

    public Message getSelectedMessage() {
        Long messageId = getSelectedMessageId();

        if (messageId != null) {
            return messageCache.get(messageId);
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
        String attachId = (String) attachmentTable.getModel().getValueAt(attachmentTable.convertRowIndexToModel(attachmentTable.getSelectedRow()), 2);
        final String attachType = (String) attachmentTable.getModel().getValueAt(attachmentTable.convertRowIndexToModel(attachmentTable.getSelectedRow()), 1);
        String[] attachmentIdArray = attachId.split(", ");
        ArrayList<String> attachmentIds = new ArrayList<String>();
        for (int i = 0; i < attachmentIdArray.length; i++) {
            attachmentIds.add(attachmentIdArray[i]);
        }
        try {
            final AttachmentViewer attachmentViewer = getAttachmentViewer(attachType);
            final ArrayList<String> finalAttachmentIds = attachmentIds;
            if (attachmentViewer != null) {

                final String workingId = parent.startWorking("Loading " + attachType + " viewer...");

                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                    public Void doInBackground() {
                        attachmentViewer.viewAttachments(finalAttachmentIds, channel.getId());
                        return null;
                    }

                    public void done() {
                        parent.stopWorking(workingId);
                    }
                };
                worker.execute();
            } else {
                parent.alertInformation(this, "No Attachment Viewer plugin installed for type: " + attachType);
            }
        } catch (Exception e) {
        }
    }

    public MessageImportResult importMessages() {
        File file = parent.browseForFile(null);
        MessageImportResult result = null;
        
        if (file != null) {
            try {
                result = parent.mirthClient.importMessages(channel.getId(), file, UIConstants.CHARSET);
            } catch (ClientException e) {
                parent.alertException(this, e.getStackTrace(), "Error importing messages. " + e.getMessage());
            }
        }
        
        return result;
    }

    public void exportMessages() {
        exportResultsPopup.setVisible(true);
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
                parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 6, -1, true);
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Get the table node
                MessageBrowserTableNode messageNode = (MessageBrowserTableNode) messageTreeTable.getPathForRow(row).getLastPathComponent();

                if (messageNode.isNodeActive()) {
                    // Get the messageId from the message node
                    Long messageId = messageNode.getMessageId();
                    // Get the metaDataId from the message node
                    Integer metaDataId = messageNode.getMetaDataId();

                    // Attempt to get the message from the message cache
                    Message message = messageCache.get(messageId);
                    List<Attachment> attachments = attachmentCache.get(messageId);

                    // If the message is not in the cache, retrieve it from the server
                    if (message == null) {
                        try {
                            message = parent.mirthClient.getMessageContent(channel.getId(), messageId);
                            // If the message was not found (ie. it may have been deleted during the request), do nothing
                            if (message == null || message.getConnectorMessages().size() == 0) {
                                clearDescription("Could not retrieve message content. The message may have been deleted.");
                                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                return;
                            }

                            attachments = parent.mirthClient.getAttachmentIdsByMessageId(channel.getId(), messageId);
                        } catch (Throwable t) {
                            if (t.getMessage().contains("Java heap space")) {
                                parent.alertError(parent, "There was an out of memory error when trying to retrieve message content.\nIncrease your heap size and try again.");
                            } else if (t.getCause() instanceof RequestAbortedException) {
                                // The client is no longer waiting for the message content request
                            } else {
                                parent.alertException(parent, t.getStackTrace(), t.getMessage());
                            }
                            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            return;
                        }
                        // Add the retrieved message to the message cache
                        messageCache.put(messageId, message);
                        attachmentCache.put(messageId, attachments);
                    }

                    ConnectorMessage connectorMessage = message.getConnectorMessages().get(metaDataId);

                    // Update the message tabs
                    updateDescriptionMessages(connectorMessage, metaDataId);
                    // Update the mappings tab
                    updateDescriptionMappings(connectorMessage, metaDataId);
                    // Update the attachments tab
                    updateAttachmentsTable(messageId);
                    // Update the errors tab
                    updateDescriptionErrors(connectorMessage.getErrors(), message.getResponseError(), metaDataId);
                    // Show relevant tabs
                    updateDescriptionTabs(metaDataId, (connectorMessage.getErrors() != null || (message.getResponseError() != null && metaDataId == 0)), attachments.size() > 0);
                    updateMessageRadioGroup();

                    if (attachmentTable == null || attachmentTable.getSelectedRow() == -1 || descriptionTabbedPane.indexOfTab("Attachments") == -1) {
                        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 9, 9, false);
                    }
                } else {
                    clearDescription(null);
                }

                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            }
        }
    }

    /**
     * Helper function to update the message tabs
     */
    private void updateDescriptionMessages(ConnectorMessage connectorMessage, Integer metaDataId) {
        MessageContent rawMessage = connectorMessage.getRaw();
        MessageContent processedRawMessage = connectorMessage.getProcessedRaw();
        MessageContent transformedMessage = connectorMessage.getTransformed();
        MessageContent encodedMessage = connectorMessage.getEncoded();
        MessageContent sentMessage = connectorMessage.getSent();
        MessageContent responseMessage = connectorMessage.getResponse();
        MessageContent processedResponseMessage = connectorMessage.getProcessedResponse();

        MessagesRadioPane.removeAll();

        //TODO update data types for response and processed response

        String inboundDataType = null;
        String outboundDataType = null;

        if (metaDataId == 0) {
            inboundDataType = channel.getSourceConnector().getTransformer().getInboundDataType();
            outboundDataType = channel.getSourceConnector().getTransformer().getOutboundDataType();
        } else {

            inboundDataType = channel.getSourceConnector().getTransformer().getOutboundDataType();
            for (Connector destinationConnector : channel.getDestinationConnectors()) {
                if (metaDataId.equals(destinationConnector.getMetaDataId())) {
                    outboundDataType = destinationConnector.getTransformer().getOutboundDataType();
                    break;
                }
            }
        }

        String content;

        content = (rawMessage == null) ? null : rawMessage.getContent();
        if (content != null) {
            MessagesRadioPane.add(RawMessageRadioButton);
        }
        setCorrectDocument(RawMessageTextPane, content, inboundDataType);

        content = (processedRawMessage == null) ? null : processedRawMessage.getContent();
        if (content != null) {
            MessagesRadioPane.add(ProcessedRawMessageRadioButton);
        }
        setCorrectDocument(ProcessedRawMessageTextPane, content, inboundDataType);

        content = (transformedMessage == null) ? null : transformedMessage.getContent();
        if (content != null) {
            MessagesRadioPane.add(TransformedMessageRadioButton);
        }
        setCorrectDocument(TransformedMessageTextPane, content, DataTypeFactory.XML);

        content = (encodedMessage == null) ? null : encodedMessage.getContent();
        if (content != null) {
            MessagesRadioPane.add(EncodedMessageRadioButton);
        }
        setCorrectDocument(EncodedMessageTextPane, content, outboundDataType);

        content = (sentMessage == null) ? null : sentMessage.getContent();
        if (content != null) {
            MessagesRadioPane.add(SentMessageRadioButton);
        }
        setCorrectDocument(SentMessageTextPane, content, outboundDataType);

        content = (responseMessage == null) ? null : responseMessage.getContent();
        if (content != null) {
            MessagesRadioPane.add(ResponseRadioButton);
        }
        //TODO determine where data type comes from
        setCorrectDocument(ResponseTextPane, content, outboundDataType);

        content = (processedResponseMessage == null) ? null : processedResponseMessage.getContent();
        if (content != null) {
            MessagesRadioPane.add(ProcessedResponseRadioButton);
        }
        //TODO determine where data type comes from.
        setCorrectDocument(ProcessedResponseTextPane, content, outboundDataType);
    }

    /**
     * Helper function to update the mappings
     */
    private void updateDescriptionMappings(ConnectorMessage connectorMessage, Integer metaDataId) {
        Map<String, Object> connectorMap = connectorMessage.getConnectorMap();
        Map<String, Object> channelMap = connectorMessage.getChannelMap();
        Map<String, Response> responseMap = connectorMessage.getResponseMap();
        
        int rowCount = 0;
        
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

        if (connectorMap != null) {
            for (Entry<String, Object> variableMapEntry : connectorMap.entrySet()) {
                tableData[row][0] = "Connector";
                tableData[row][1] = variableMapEntry.getKey().toString();
                tableData[row][2] = variableMapEntry.getValue().toString();
                row++;
            }
        }

        if (channelMap != null) {
            for (Entry<String, Object> variableMapEntry : channelMap.entrySet()) {
                tableData[row][0] = "Channel";
                tableData[row][1] = variableMapEntry.getKey().toString();
                tableData[row][2] = variableMapEntry.getValue().toString();
                row++;
            }
        }

        if (responseMap != null) {
            for (Entry<String, Response> variableMapEntry : responseMap.entrySet()) {
                tableData[row][0] = "Response";
                tableData[row][1] = variableMapEntry.getKey().toString();
                tableData[row][2] = variableMapEntry.getValue().toString();
                row++;
            }
        }

        updateMappingsTable(tableData, false);
    }
    
    /**
     * Helper function to update the error tab
     */
    private void updateDescriptionErrors(String processingError, String replyError, Integer metaDataId) {
        if (metaDataId != 0) {
        	replyError = null;
        }

        ErrorsRadioPane.removeAll();
        boolean paneSelected = false;
        String firstVisiblePane = null;

        if (processingError != null) {
            ErrorsRadioPane.add(ProcessingErrorRadioButton);
            paneSelected = lastUserSelectedErrorType.equals(ProcessingErrorRadioButton.getText());
            if (firstVisiblePane == null) {
            	firstVisiblePane = ProcessingErrorRadioButton.getText();
            }
        }
        setCorrectDocument(ProcessingErrorTextPane, processingError, null);

        if (replyError != null) {
            ErrorsRadioPane.add(ReplyErrorRadioButton);
            paneSelected = lastUserSelectedErrorType.equals(ReplyErrorRadioButton.getText());
            if (firstVisiblePane == null) {
            	firstVisiblePane = ReplyErrorRadioButton.getText();
            }
        }
        setCorrectDocument(ReplyErrorTextPane, replyError, null);
        
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
                Integer metaDataId = messageNode.getMetaDataId();

                String dataType = null;

                if (messagePaneName.equals("Raw") || messagePaneName.equals("Processed Raw")) {
                    if (metaDataId == 0) {
                        dataType = channel.getSourceConnector().getTransformer().getInboundDataType();
                    } else {
                        dataType = channel.getSourceConnector().getTransformer().getOutboundDataType();
                    }
                } else if (messagePaneName.equals("Transformed")) {
                    dataType = DataTypeFactory.XML;
                } else if (messagePaneName.equals("Sent") || messagePaneName.equals("Encoded")) {
                    if (metaDataId == 0) {
                        dataType = channel.getSourceConnector().getTransformer().getOutboundDataType();
                    } else {
                        for (Connector destinationConnector : channel.getDestinationConnectors()) {
                            if (metaDataId.equals(destinationConnector.getMetaDataId())) {
                                dataType = destinationConnector.getTransformer().getOutboundDataType();
                                break;
                            }
                        }
                    }
                } else if (messagePaneName.equals("Response") || messagePaneName.equals("Processed Response")) {
                    //TODO update this logic once response and processed response are added
                    if (metaDataId == 0) {
                        dataType = channel.getSourceConnector().getTransformer().getOutboundDataType();
                    } else {
                        for (Connector destinationConnector : channel.getDestinationConnectors()) {
                            if (metaDataId.equals(destinationConnector.getMetaDataId())) {
                                dataType = destinationConnector.getTransformer().getOutboundDataType();
                                break;
                            }
                        }
                    }
                }

                if (dataType.equals(DataTypeFactory.XML) || dataType.equals(DataTypeFactory.HL7V3)) {
                    formatXmlMessageCheckBox.setEnabled(true);
                } else {
                    formatXmlMessageCheckBox.setEnabled(false);
                }
            } else {
                formatXmlMessageCheckBox.setEnabled(false);
            }
        }
    }

    private void formatXmlCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        int row = getSelectedMessageIndex();

        if (row >= 0) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            MessageBrowserTableNode messageNode = (MessageBrowserTableNode) messageTreeTable.getPathForRow(row).getLastPathComponent();
            Long messageId = messageNode.getMessageId();
            Integer metaDataId = messageNode.getMetaDataId();

            Message message = messageCache.get(messageId);
            ConnectorMessage connectorMessage;

            connectorMessage = message.getConnectorMessages().get(metaDataId);

            updateDescriptionMessages(connectorMessage, metaDataId);

            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    protected void nextPageButtonActionPerformed(ActionEvent evt) {
        loadPageNumber(messages.getPageNumber() + 1);
    }

    protected void previousPageButtonActionPerformed(ActionEvent evt) {
        loadPageNumber(messages.getPageNumber() - 1);
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        messagesGroup = new javax.swing.ButtonGroup();
        errorsGroup = new javax.swing.ButtonGroup();
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
        ProcessedResponseRadioButton = new javax.swing.JRadioButton();
        MessagesCardPane = new javax.swing.JPanel();
        RawMessageTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        ProcessedRawMessageTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        TransformedMessageTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        EncodedMessageTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        SentMessageTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        ResponseTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        ProcessedResponseTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        formatXmlMessageCheckBox = new javax.swing.JCheckBox();
        mappingsPane = new javax.swing.JScrollPane();
        mappingsTable = null;
        ErrorsPanel = new javax.swing.JPanel();
        ErrorsRadioPane = new javax.swing.JPanel();
        ProcessingErrorRadioButton = new javax.swing.JRadioButton();
        ReplyErrorRadioButton = new javax.swing.JRadioButton();
        ErrorsCardPane = new javax.swing.JPanel();
        ProcessingErrorTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        ReplyErrorTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        attachmentsPane = new javax.swing.JScrollPane();
        attachmentTable = null;
        messageScrollPane = new javax.swing.JScrollPane();
        messageTreeTable = new com.mirth.connect.client.ui.components.MirthTreeTable();
        statusBoxError = new com.mirth.connect.client.ui.components.MirthCheckBox();
        statusBoxQueued = new com.mirth.connect.client.ui.components.MirthCheckBox();
        statusBoxSent = new com.mirth.connect.client.ui.components.MirthCheckBox();
        statusBoxFiltered = new com.mirth.connect.client.ui.components.MirthCheckBox();
        statusBoxTransformed = new com.mirth.connect.client.ui.components.MirthCheckBox();
        statusBoxReceived = new com.mirth.connect.client.ui.components.MirthCheckBox();
        advSearchButton = new javax.swing.JButton();
        filterButton = new javax.swing.JButton();
        quickSearchField = new javax.swing.JTextField();
        mirthTimePicker2 = new com.mirth.connect.client.ui.components.MirthTimePicker();
        mirthTimePicker1 = new com.mirth.connect.client.ui.components.MirthTimePicker();
        mirthDatePicker1 = new com.mirth.connect.client.ui.components.MirthDatePicker();
        mirthDatePicker2 = new com.mirth.connect.client.ui.components.MirthDatePicker();
        quickSearchLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        resultsLabel = new javax.swing.JLabel();
        countButton = new com.mirth.connect.client.ui.components.MirthButton();
        pageSizeLabel = new javax.swing.JLabel();
        pageSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        previousPageButton = new javax.swing.JButton();
        nextPageButton = new javax.swing.JButton();
        pageGoButton = new javax.swing.JButton();
        lastSearchCriteriaPane = new javax.swing.JScrollPane();
        lastSearchCriteria = new javax.swing.JTextArea();
        resetButton = new javax.swing.JButton();
        allDayCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        pageNumberField = new com.mirth.connect.client.ui.components.MirthTextField();
        pageNumberLabel = new javax.swing.JLabel();
        pageTotalLabel = new javax.swing.JLabel();

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jList1);

        setBackground(new java.awt.Color(255, 255, 255));
        setFocusable(false);
        setRequestFocusEnabled(false);

        jSplitPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jSplitPane1.setDividerLocation(310);
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

        ResponseTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ResponseTextPane.setEditable(false);
        MessagesCardPane.add(ResponseTextPane, "Response");

        ProcessedResponseTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ProcessedResponseTextPane.setEditable(false);
        MessagesCardPane.add(ProcessedResponseTextPane, "Processed Response");

        formatXmlMessageCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        formatXmlMessageCheckBox.setText("Format XML Messages");
        formatXmlMessageCheckBox.setToolTipText("Pretty print messages that are XML.");
        formatXmlMessageCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatXmlMessageCheckBoxActionPerformed(evt);
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
                        .addComponent(formatXmlMessageCheckBox)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(MessagesRadioPane, javax.swing.GroupLayout.DEFAULT_SIZE, 646, Short.MAX_VALUE))
                .addContainerGap())
        );
        MessagesPanelLayout.setVerticalGroup(
            MessagesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MessagesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(MessagesRadioPane, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(MessagesCardPane, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(formatXmlMessageCheckBox)
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
        ProcessingErrorRadioButton.setSelected(true);
        ProcessingErrorRadioButton.setText("Processing Error");
        ProcessingErrorRadioButton.setFocusable(false);
        ProcessingErrorRadioButton.setRequestFocusEnabled(false);
        ProcessingErrorRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessingErrorRadioButtonActionPerformed(evt);
            }
        });
        ErrorsRadioPane.add(ProcessingErrorRadioButton);

        ReplyErrorRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        errorsGroup.add(ReplyErrorRadioButton);
        ReplyErrorRadioButton.setText("Reply Error");
        ReplyErrorRadioButton.setFocusable(false);
        ReplyErrorRadioButton.setRequestFocusEnabled(false);
        ReplyErrorRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ReplyErrorRadioButtonActionPerformed(evt);
            }
        });
        ErrorsRadioPane.add(ReplyErrorRadioButton);

        ErrorsCardPane.setBackground(new java.awt.Color(255, 255, 255));
        ErrorsCardPane.setLayout(new java.awt.CardLayout());

        ProcessingErrorTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ProcessingErrorTextPane.setEditable(false);
        ErrorsCardPane.add(ProcessingErrorTextPane, "Processing Error");

        ReplyErrorTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ReplyErrorTextPane.setEditable(false);
        ErrorsCardPane.add(ReplyErrorTextPane, "Reply Error");

        javax.swing.GroupLayout ErrorsPanelLayout = new javax.swing.GroupLayout(ErrorsPanel);
        ErrorsPanel.setLayout(ErrorsPanelLayout);
        ErrorsPanelLayout.setHorizontalGroup(
            ErrorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ErrorsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ErrorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ErrorsCardPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ErrorsRadioPane, javax.swing.GroupLayout.DEFAULT_SIZE, 646, Short.MAX_VALUE))
                .addContainerGap())
        );
        ErrorsPanelLayout.setVerticalGroup(
            ErrorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ErrorsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ErrorsRadioPane, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ErrorsCardPane, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                .addContainerGap())
        );

        descriptionTabbedPane.addTab("Errors", ErrorsPanel);

        attachmentsPane.setViewportView(attachmentTable);

        descriptionTabbedPane.addTab("Attachments", attachmentsPane);

        jSplitPane1.setRightComponent(descriptionTabbedPane);

        messageScrollPane.setViewportView(messageTreeTable);

        jSplitPane1.setLeftComponent(messageScrollPane);

        statusBoxError.setBackground(new java.awt.Color(255, 255, 255));
        statusBoxError.setText("ERROR");
        statusBoxError.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        statusBoxError.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusBoxErrorActionPerformed(evt);
            }
        });

        statusBoxQueued.setBackground(new java.awt.Color(255, 255, 255));
        statusBoxQueued.setText("QUEUED");
        statusBoxQueued.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        statusBoxQueued.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusBoxQueuedActionPerformed(evt);
            }
        });

        statusBoxSent.setBackground(new java.awt.Color(255, 255, 255));
        statusBoxSent.setText("SENT");
        statusBoxSent.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N

        statusBoxFiltered.setBackground(new java.awt.Color(255, 255, 255));
        statusBoxFiltered.setText("FILTERED");
        statusBoxFiltered.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        statusBoxFiltered.setMaximumSize(new java.awt.Dimension(83, 23));
        statusBoxFiltered.setMinimumSize(new java.awt.Dimension(83, 23));
        statusBoxFiltered.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusBoxFilteredActionPerformed(evt);
            }
        });

        statusBoxTransformed.setBackground(new java.awt.Color(255, 255, 255));
        statusBoxTransformed.setText("TRANSFORMED");
        statusBoxTransformed.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        statusBoxTransformed.setMaximumSize(new java.awt.Dimension(83, 23));
        statusBoxTransformed.setMinimumSize(new java.awt.Dimension(83, 23));

        statusBoxReceived.setBackground(new java.awt.Color(255, 255, 255));
        statusBoxReceived.setText("RECEIVED");
        statusBoxReceived.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        statusBoxReceived.setPreferredSize(new java.awt.Dimension(90, 22));
        statusBoxReceived.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusBoxReceivedActionPerformed(evt);
            }
        });

        advSearchButton.setText("Advanced...");
        advSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advSearchButtonActionPerformed(evt);
            }
        });

        filterButton.setText("Search");
        filterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterButtonActionPerformed(evt);
            }
        });

        quickSearchLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        quickSearchLabel.setText("Quick Search:");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("End Time:");
        jLabel2.setMaximumSize(new java.awt.Dimension(78, 15));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Start Time:");
        jLabel3.setMaximumSize(new java.awt.Dimension(78, 15));

        resultsLabel.setForeground(new java.awt.Color(204, 0, 0));
        resultsLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        resultsLabel.setText("Results");

        countButton.setText("Count");
        countButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                countButtonActionPerformed(evt);
            }
        });

        pageSizeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pageSizeLabel.setText("Page Size:");
        pageSizeLabel.setMaximumSize(new java.awt.Dimension(78, 15));

        pageSizeField.setToolTipText("After changing the page size, a new search must be performed for the changes to take effect.  The default page size can also be configured on the Settings panel.");
        pageSizeField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pageSizeFieldActionPerformed(evt);
            }
        });

        previousPageButton.setText("< Prev");
        previousPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousPageButtonActionPerformed(evt);
            }
        });

        nextPageButton.setText("Next >");
        nextPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextPageButtonActionPerformed(evt);
            }
        });

        pageGoButton.setText("Go");
        pageGoButton.setNextFocusableComponent(messageTreeTable);
        pageGoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pageGoButtonActionPerformed(evt);
            }
        });

        lastSearchCriteriaPane.setBorder(null);
        lastSearchCriteriaPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        lastSearchCriteria.setColumns(20);
        lastSearchCriteria.setEditable(false);
        lastSearchCriteria.setForeground(new java.awt.Color(96, 96, 96));
        lastSearchCriteria.setLineWrap(true);
        lastSearchCriteria.setRows(5);
        lastSearchCriteria.setAlignmentX(0.0F);
        lastSearchCriteria.setDisabledTextColor(new java.awt.Color(255, 255, 255));
        lastSearchCriteriaPane.setViewportView(lastSearchCriteria);

        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        allDayCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        allDayCheckBox.setText("All Day");
        allDayCheckBox.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        allDayCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allDayCheckBoxActionPerformed(evt);
            }
        });

        pageNumberField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        pageNumberField.setToolTipText("Enter a page number and press Enter to jump to that page.");
        pageNumberField.setPreferredSize(new java.awt.Dimension(40, 22));
        pageNumberField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pageNumberFieldActionPerformed(evt);
            }
        });

        pageNumberLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pageNumberLabel.setText("Page");

        pageTotalLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pageTotalLabel.setText("of ?");
        pageTotalLabel.setAlignmentY(0.0F);
        pageTotalLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pageSizeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(quickSearchLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pageSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(advSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(mirthDatePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(mirthTimePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(mirthDatePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(mirthTimePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(quickSearchField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(allDayCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statusBoxQueued, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusBoxSent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusBoxError, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(statusBoxReceived, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(statusBoxFiltered, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(statusBoxTransformed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lastSearchCriteriaPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(previousPageButton, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nextPageButton, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(resultsLabel)
                        .addGap(5, 5, 5)
                        .addComponent(countButton, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pageNumberLabel)
                        .addGap(4, 4, 4)
                        .addComponent(pageNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(pageTotalLabel)
                        .addGap(5, 5, 5)
                        .addComponent(pageGoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {mirthTimePicker1, mirthTimePicker2});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(countButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(resultsLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(pageGoButton)
                            .addComponent(pageTotalLabel)
                            .addComponent(pageNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pageNumberLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nextPageButton)
                            .addComponent(previousPageButton)))
                    .addComponent(lastSearchCriteriaPane, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mirthTimePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(mirthDatePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mirthTimePicker2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(mirthDatePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(quickSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(quickSearchLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(pageSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pageSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(advSearchButton)
                            .addComponent(resetButton)
                            .addComponent(filterButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
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
                        .addComponent(statusBoxError, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 567, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void pageSizeFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pageSizeFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pageSizeFieldActionPerformed

    private void advSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advSearchButtonActionPerformed
        advancedSearchPopup.setBackground(UIConstants.COMBO_BOX_BACKGROUND);
        advancedSearchPopup.setLocationRelativeTo(parent);
        advancedSearchPopup.setVisible(true);

        updateAdvancedSearchButtonFont();
    }//GEN-LAST:event_advSearchButtonActionPerformed

    private void filterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterButtonActionPerformed
        runSearch();
    }//GEN-LAST:event_filterButtonActionPerformed

    private void statusBoxReceivedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusBoxReceivedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_statusBoxReceivedActionPerformed

    private void statusBoxErrorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusBoxErrorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_statusBoxErrorActionPerformed

    private void statusBoxFilteredActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusBoxFilteredActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_statusBoxFilteredActionPerformed

    private void statusBoxQueuedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusBoxQueuedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_statusBoxQueuedActionPerformed

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
                    messages.setItemCount(parent.mirthClient.getMessageCount(channel.getId(), messageFilter));
                } catch (ClientException e) {
                    if (e.getCause() instanceof RequestAbortedException) {
                        // The client is no longer waiting for the count request
                    } else {
                        parent.alertException(parent, e.getStackTrace(), e.getMessage());
                    }
                    cancel(true);
                }

                return null;
            }

            public void done() {
                if (!isCancelled()) {
                    if (e != null) {
                        countButton.setEnabled(true);
                        parent.alertException(messageBrowser, e.getStackTrace(), e.getMessage());
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

    private void formatXmlMessageCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatXmlMessageCheckBoxActionPerformed
        formatXmlCheckBoxActionPerformed(evt);
    }//GEN-LAST:event_formatXmlMessageCheckBoxActionPerformed

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

    private void pageNumberFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pageNumberFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pageNumberFieldActionPerformed

    private void pageGoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pageGoButtonActionPerformed
        jumpToPageNumber();
    }//GEN-LAST:event_pageGoButtonActionPerformed

    private void ProcessingErrorRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ProcessingErrorRadioButtonActionPerformed
    	errorsRadioButtonActionPerformed(evt);
    }//GEN-LAST:event_ProcessingErrorRadioButtonActionPerformed

    private void ReplyErrorRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ReplyErrorRadioButtonActionPerformed
    	errorsRadioButtonActionPerformed(evt);
    }//GEN-LAST:event_ReplyErrorRadioButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton EncodedMessageRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea EncodedMessageTextPane;
    private javax.swing.JPanel ErrorsCardPane;
    private javax.swing.JPanel ErrorsPanel;
    private javax.swing.JPanel ErrorsRadioPane;
    private javax.swing.JPanel MessagesCardPane;
    private javax.swing.JPanel MessagesPanel;
    private javax.swing.JPanel MessagesRadioPane;
    private javax.swing.JRadioButton ProcessedRawMessageRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea ProcessedRawMessageTextPane;
    private javax.swing.JRadioButton ProcessedResponseRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea ProcessedResponseTextPane;
    private javax.swing.JRadioButton ProcessingErrorRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea ProcessingErrorTextPane;
    private javax.swing.JRadioButton RawMessageRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea RawMessageTextPane;
    private javax.swing.JRadioButton ReplyErrorRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea ReplyErrorTextPane;
    private javax.swing.JRadioButton ResponseRadioButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea ResponseTextPane;
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
    private javax.swing.JCheckBox formatXmlMessageCheckBox;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JList jList1;
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
    private javax.swing.JTextField quickSearchField;
    private javax.swing.JLabel quickSearchLabel;
    private javax.swing.JButton resetButton;
    private javax.swing.JLabel resultsLabel;
    private com.mirth.connect.client.ui.components.MirthCheckBox statusBoxError;
    private com.mirth.connect.client.ui.components.MirthCheckBox statusBoxFiltered;
    private com.mirth.connect.client.ui.components.MirthCheckBox statusBoxQueued;
    private com.mirth.connect.client.ui.components.MirthCheckBox statusBoxReceived;
    private com.mirth.connect.client.ui.components.MirthCheckBox statusBoxSent;
    private com.mirth.connect.client.ui.components.MirthCheckBox statusBoxTransformed;
    // End of variables declaration//GEN-END:variables
}

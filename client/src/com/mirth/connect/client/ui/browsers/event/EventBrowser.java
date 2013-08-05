/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.browsers.event;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.DateFormatter;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.Operations;
import com.mirth.connect.client.core.PaginatedEventList;
import com.mirth.connect.client.core.RequestAbortedException;
import com.mirth.connect.client.ui.CellData;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.ImageCellRenderer;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.ViewContentDialog;
import com.mirth.connect.client.ui.components.MirthDatePicker;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTimePicker;
import com.mirth.connect.client.ui.util.DisplayUtil;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.ServerEvent.Level;
import com.mirth.connect.model.ServerEvent.Outcome;
import com.mirth.connect.model.User;
import com.mirth.connect.model.filters.EventFilter;

public class EventBrowser extends javax.swing.JPanel {

    private final String EVENT_ID_COLUMN_NAME = "ID";
    private final String EVENT_DATE_COLUMN_NAME = "Date & Time";
    private final String EVENT_LEVEL_COLUMN_NAME = "Level";
    private final String EVENT_NAME_COLUMN_NAME = "Name";
    private final String EVENT_USER_COLUMN_NAME = "User";
    private final String EVENT_OUTCOME_COLUMN_NAME = "Outcome";
    private final String EVENT_IP_ADDRESS_COLUMN_NAME = "IP Address";
    private final String ATTRIBUTES_NAME_COLUMN_NAME = "Name";
    private final String ATTRIBUTES_VALUE_COLUMN_NAME = "Value";
    private final int ATTRIBUTES_VALUE_COLUMN_NUMBER = 1;
    private Frame parent;
    private PaginatedEventList events;
    private EventFilter eventFilter;
    private EventBrowserAdvancedFilter advancedSearchPopup;
    private Map<Integer, String> userMapById = new LinkedHashMap<Integer, String>();
    private SwingWorker<Void, Void> worker;

    /**
     * Constructs the new event browser and sets up its default
     * information/layout.
     */
    public EventBrowser() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        initComponentsManual();
        makeEventTable();
        makeAttributesTable();
    }

    public void initComponentsManual() {
        this.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    parent.eventPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    parent.eventPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });

        pageSizeField.setDocument(new MirthFieldConstraints(3, false, false, true));
        pageNumberField.setDocument(new MirthFieldConstraints(7, false, false, true));

        LineBorder lineBorder = new LineBorder(new Color(0, 0, 0));
        TitledBorder titledBorder = new TitledBorder("Current Search");
        titledBorder.setBorder(lineBorder);

        lastSearchCriteriaPane.setBorder(titledBorder);
        lastSearchCriteriaPane.setBackground(Color.white);
        lastSearchCriteria.setBackground(Color.white);

        startDatePicker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {
                allDayCheckBox.setEnabled(startDatePicker.getDate() != null || endDatePicker.getDate() != null);
                startTimePicker.setEnabled(startDatePicker.getDate() != null && !allDayCheckBox.isSelected());
            }
        });

        endDatePicker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {
                allDayCheckBox.setEnabled(startDatePicker.getDate() != null || endDatePicker.getDate() != null);
                endTimePicker.setEnabled(endDatePicker.getDate() != null && !allDayCheckBox.isSelected());
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

        updateCachedUserMap();

        advancedSearchPopup = new EventBrowserAdvancedFilter(parent, "Advanced Search Filter", true, userMapById);
        advancedSearchPopup.setVisible(false);

        eventSplitPane.setDividerLocation(0.8);

        this.addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                // Stop waiting for event browser requests when the event browser 
                // is no longer being displayed
                parent.mirthClient.getServerConnection().abort(getAbortOperations());
                // Clear the event cache when leaving the event browser.
                parent.eventBrowser.clearCache();
            }

        });
    }

    /**
     * Loads up a clean event browser as if a new one was constructed.
     */
    public void loadNew(String eventNameFilter) {
        // Set the default page size
        pageSizeField.setText(String.valueOf(Preferences.userNodeForPackage(Mirth.class).getInt("eventBrowserPageSize", 100)));

        if (eventNameFilter != null) {
            nameField.setText(eventNameFilter);
        } else {
            nameField.setText("");
        }

        updateCachedUserMap();

        advancedSearchPopup.reset();

        runSearch();
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

    public boolean generateEventFilter() {
        eventFilter = new EventFilter();

        // set start/end date
        try {
            eventFilter.setStartDate(getCalendar(startDatePicker, startTimePicker));
            Calendar endCalendar = getCalendar(endDatePicker, endTimePicker);

            if (endCalendar != null && !endTimePicker.isEnabled()) {
                // If the end time picker is disabled, it will be set to 00:00:00 of the day provided.
                // Since our query is using <= instead of <, we add one day and then subtract a millisecond 
                // in order to set the time to the last millisecond of the day we want to search on
                endCalendar.add(Calendar.DATE, 1);
                endCalendar.add(Calendar.MILLISECOND, -1);
            }
            eventFilter.setEndDate(endCalendar);
        } catch (ParseException e) {
            parent.alertError(parent, "Invalid date.");
            return false;
        }

        Calendar startDate = eventFilter.getStartDate();
        Calendar endDate = eventFilter.getEndDate();

        if (startDate != null && endDate != null && startDate.getTimeInMillis() > endDate.getTimeInMillis()) {
            parent.alertError(parent, "Start date cannot be after the end date.");
            return false;
        }

        if (!nameField.getText().equals("")) {
            eventFilter.setName(nameField.getText());
        }

        // set levels
        Set<Level> levels = new HashSet<Level>();

        if (levelBoxInformation.isSelected()) {
            levels.add(Level.INFORMATION);
        }

        if (levelBoxWarning.isSelected()) {
            levels.add(Level.WARNING);
        }

        if (levelBoxError.isSelected()) {
            levels.add(Level.ERROR);
        }

        if (!levels.isEmpty()) {
            eventFilter.setLevels(levels);
        }

        // Start advanced search properties

        if (advancedSearchPopup.getUser() != -1) {
            eventFilter.setUserId(advancedSearchPopup.getUser());
        }

        if (!advancedSearchPopup.getOutcome().equals(UIConstants.ALL_OPTION)) {
            for (int i = 0; i < Outcome.values().length; i++) {
                if (advancedSearchPopup.getOutcome().equalsIgnoreCase(Outcome.values()[i].toString())) {
                    eventFilter.setOutcome(Outcome.values()[i]);
                }
            }
        }

        if (!advancedSearchPopup.getIpAddress().equals("")) {
            eventFilter.setIpAddress(advancedSearchPopup.getIpAddress());
        }

        try {
            Integer maxEventId = parent.mirthClient.getMaxEventId();
            eventFilter.setMaxEventId(maxEventId);
        } catch (ClientException e) {
            parent.alertException(parent, e.getStackTrace(), e.getMessage());
            return false;
        }

        return true;
    }

    public void runSearch() {
        if (generateEventFilter()) {
            updateFilterButtonFont(Font.PLAIN);
            events = new PaginatedEventList();
            events.setClient(parent.mirthClient);
            events.setEventFilter(eventFilter);
        
            try {
                events.setPageSize(Integer.parseInt(pageSizeField.getText()));
            } catch (NumberFormatException e) {
                parent.alertError(parent, "Invalid page size.");
                return;
            }
        
            countButton.setVisible(true);
            clearCache();
            loadPageNumber(1);
        
            updateSearchCriteriaPane();
        }
    }

    private void updateSearchCriteriaPane() {
        StringBuilder text = new StringBuilder();
        Calendar startDate = eventFilter.getStartDate();
        Calendar endDate = eventFilter.getEndDate();
        String padding = "\n";

        text.append("Max Event Id: ");
        text.append(eventFilter.getMaxEventId());

        String startDateFormatString = startTimePicker.isEnabled() ? "yyyy-MM-dd HH:mm" : "yyyy-MM-dd";
        String endDateFormatString = endTimePicker.isEnabled() ? "yyyy-MM-dd HH:mm" : "yyyy-MM-dd";

        DateFormat startDateFormat = new SimpleDateFormat(startDateFormatString);
        DateFormat endDateFormat = new SimpleDateFormat(endDateFormatString);

        text.append(padding + "Date Range: ");

        if (startDate == null) {
            text.append("(any)");
        } else {
            text.append(startDateFormat.format(startDate.getTime()));
            if (!startTimePicker.isEnabled()) {
                text.append(" (all day)");
            }
        }

        text.append(" to ");

        if (endDate == null) {
            text.append("(any)");
        } else {
            text.append(endDateFormat.format(endDate.getTime()));
            if (!endTimePicker.isEnabled()) {
                text.append(" (all day)");
            }
        }

        text.append(padding + "Levels: ");

        if (eventFilter.getLevels() == null) {
            text.append("(any)");
        } else {
            text.append(StringUtils.join(eventFilter.getLevels(), ", "));
        }

        if (eventFilter.getName() != null) {
            text.append(padding + "Name: " + eventFilter.getName());
        }

        if (eventFilter.getUserId() != null) {
            text.append(padding + "User Id: " + eventFilter.getUserId());
        }

        if (eventFilter.getOutcome() != null) {
            text.append(padding + "Outcome: " + eventFilter.getOutcome());
        }

        if (eventFilter.getIpAddress() != null) {
            text.append(padding + "IP Address: " + eventFilter.getIpAddress());
        }

        lastSearchCriteria.setText(text.toString());
    }

    public void clearCache() {}

    public void jumpToPageNumber() {
        if (events.getPageCount() != null && events.getPageCount() > 0 && StringUtils.isNotEmpty(pageNumberField.getText())) {
            loadPageNumber(Math.min(Math.max(Integer.parseInt(pageNumberField.getText()), 1), events.getPageCount()));
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

        // Give focus to the event table since these buttons will lose focus. That way the user can also immediately use the arrow keys after a search.
        eventTable.requestFocus();

        worker = new SwingWorker<Void, Void>() {
            private boolean foundItems = false;
            private int retrievedPageNumber = 1;

            public Void doInBackground() {

                try {
                    foundItems = events.loadPageNumber(pageNumber);
                } catch (Throwable t) { // catch Throwable in case the client runs out of memory

                    if (t.getMessage().contains("Java heap space")) {
                        parent.alertError(parent, "There was an out of memory error when trying to retrieve events.\nIncrease your heap size or decrease your page size and search again.");
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
                    boolean enableCountButton = (events.getItemCount() == null);

                    deselectRows();

                    if (foundItems) {

                        // if there are no results for pageNumber, loadPageNumber will recursively check previous pages
                        // so we must get the retrievedPageNumber from events to use below.
                        retrievedPageNumber = events.getPageNumber();
                        pageNumberField.setText(String.valueOf(retrievedPageNumber));

                        updateEventTable(events);

                        if (!events.hasNextPage()) {
                            events.setItemCount(new Long(((retrievedPageNumber - 1) * events.getPageSize()) + events.size()));
                            enableCountButton = false;
                        }
                    } else {
                        if (eventTable != null) {
                            RefreshTableModel model = (RefreshTableModel) eventTable.getModel();
                            model.refreshDataVector(new Object[0][0]);
                        }

                        events.setItemCount(new Long((retrievedPageNumber - 1) * events.getPageSize()));
                        enableCountButton = false;
                        pageNumberField.setText("0");
                    }

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
        int pageNumber = events.getPageNumber();
        Integer pageCount = events.getPageCount();
        int startOffset, endOffset;

        if (events.size() == 0) {
            startOffset = 0;
            endOffset = 0;
        } else {
            startOffset = events.getOffset(pageNumber) + 1;
            endOffset = startOffset + events.size() - 1;
        }

        String resultText = "Results " + DisplayUtil.formatNumber(startOffset) + " - " + DisplayUtil.formatNumber(endOffset) + " of ";

        // enable the previous page button if the page number is > 1
        // Now that we have hasNextPage, we no longer need any additional logic
        previousPageButton.setEnabled(pageNumber > 1);
        nextPageButton.setEnabled(events.hasNextPage());

        if (pageCount != null) {
            resultsLabel.setText(resultText + DisplayUtil.formatNumber(events.getItemCount()));
            pageTotalLabel.setText("of " + DisplayUtil.formatNumber(events.getPageCount()));
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

    /**
     * Updates the cached username/id list so user ids can be displayed with
     * their names.
     */
    private void updateCachedUserMap() {
        // Retrieve users again to update the cache
        try {
            parent.retrieveUsers();
        } catch (ClientException e) {
            parent.alertException(this, e.getStackTrace(), e.getMessage());
        }

        userMapById.clear();
        userMapById.put(-1, UIConstants.ALL_OPTION);
        userMapById.put(0, "System");
        for (User user : parent.users) {
            userMapById.put(user.getId(), user.getUsername());
        }
    }

    /**
     * Refreshes the panel with the curent filter information.
     */
    public void refresh(Integer page) {
        clearCache();

        if (page == null) {
            loadPageNumber(events.getPageNumber());
        } else {
            loadPageNumber(page);
        }
    }

    public List<Operation> getAbortOperations() {
        List<Operation> operations = new ArrayList<Operation>();

        operations.add(Operations.EVENT_GET);
        operations.add(Operations.EVENT_GET_COUNT);
        operations.add(Operations.EVENT_REMOVE_ALL);

        return operations;
    }

    public void resetSearchCriteria() {
        startDatePicker.setDate(null);
        endDatePicker.setDate(null);
        nameField.setText("");
        allDayCheckBox.setSelected(false);
        levelBoxInformation.setSelected(false);
        levelBoxWarning.setSelected(false);
        levelBoxError.setSelected(false);
        pageSizeField.setText(String.valueOf(Preferences.userNodeForPackage(Mirth.class).getInt("eventBrowserPageSize", 100)));

        advancedSearchPopup.reset();
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

    public void updateEventTable(List<ServerEvent> systemEventList) {
        Object[][] tableData = null;

        if (systemEventList != null) {
            tableData = new Object[systemEventList.size()][7];

            for (int i = 0; i < systemEventList.size(); i++) {
                ServerEvent systemEvent = systemEventList.get(i);

                tableData[i][0] = systemEvent.getId();

                if (systemEvent.getLevel().equals(Level.INFORMATION)) {
                    tableData[i][1] = new CellData(UIConstants.ICON_INFORMATION, "");
                } else if (systemEvent.getLevel().equals(Level.WARNING)) {
                    tableData[i][1] = new CellData(UIConstants.ICON_WARNING, "");
                } else if (systemEvent.getLevel().equals(Level.ERROR)) {
                    tableData[i][1] = new CellData(UIConstants.ICON_ERROR, "");
                } else {
                    tableData[i][1] = new CellData(null, systemEvent.getLevel().toString());
                }

                tableData[i][2] = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS:%1$tL", systemEvent.getEventTime());

                tableData[i][3] = systemEvent.getName();

                // Write the username (if cached) next to the user id
                int userId = systemEvent.getUserId();
                String user = String.valueOf(userId);
                if (userMapById.containsKey(userId)) {
                    user += " (" + userMapById.get(userId) + ")";
                }
                tableData[i][4] = user;

                if (systemEvent.getOutcome().equals(Outcome.SUCCESS)) {
                    tableData[i][5] = new CellData(UIConstants.ICON_CHECK, "");
                } else if (systemEvent.getOutcome().equals(Outcome.FAILURE)) {
                    tableData[i][5] = new CellData(UIConstants.ICON_X, "");
                } else {
                    tableData[i][5] = new CellData(null, systemEvent.getOutcome().toString());
                }

                tableData[i][6] = systemEvent.getIpAddress();
            }
        } else {
            tableData = new Object[0][7];
        }

        if (eventTable != null) {
            RefreshTableModel model = (RefreshTableModel) eventTable.getModel();
            model.refreshDataVector(tableData);
        } else {
            eventTable = new MirthTable();
            eventTable.setModel(new RefreshTableModel(tableData, new String[] {
                    EVENT_ID_COLUMN_NAME, EVENT_LEVEL_COLUMN_NAME, EVENT_DATE_COLUMN_NAME,
                    EVENT_NAME_COLUMN_NAME, EVENT_USER_COLUMN_NAME, EVENT_OUTCOME_COLUMN_NAME,
                    EVENT_IP_ADDRESS_COLUMN_NAME }) {

                boolean[] canEdit = new boolean[] { false, false, false, false, false, false, false };

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }
    }

    /**
     * Creates the table with all of the information given after being filtered
     * by the specified 'filter'
     */
    private void makeEventTable() {
        updateEventTable(null);

        eventTable.setSelectionMode(0);

        eventTable.getColumnExt(EVENT_LEVEL_COLUMN_NAME).setCellRenderer(new ImageCellRenderer(SwingConstants.CENTER));
        eventTable.getColumnExt(EVENT_OUTCOME_COLUMN_NAME).setCellRenderer(new ImageCellRenderer(SwingConstants.CENTER));

        eventTable.getColumnExt(EVENT_ID_COLUMN_NAME).setVisible(false);

        eventTable.getColumnExt(EVENT_DATE_COLUMN_NAME).setMinWidth(140);
        eventTable.getColumnExt(EVENT_DATE_COLUMN_NAME).setMaxWidth(140);
        eventTable.getColumnExt(EVENT_LEVEL_COLUMN_NAME).setMinWidth(50);
        eventTable.getColumnExt(EVENT_LEVEL_COLUMN_NAME).setMaxWidth(50);
        eventTable.getColumnExt(EVENT_OUTCOME_COLUMN_NAME).setMinWidth(65);
        eventTable.getColumnExt(EVENT_OUTCOME_COLUMN_NAME).setMaxWidth(65);

        eventTable.setRowHeight(UIConstants.ROW_HEIGHT);
        eventTable.setOpaque(true);
        eventTable.setRowSelectionAllowed(true);
        deselectRows();

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            eventTable.setHighlighters(highlighter);
        }

        eventPane.setViewportView(eventTable);
        eventSplitPane.setLeftComponent(eventPane);

        eventTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                EventListSelected(evt);
            }
        });

        eventTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }
        });

        // Set highlighter.
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            eventTable.setHighlighters(highlighter);
        }
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been
     * pushed. Deselects the rows if no row was selected.
     */
    private void checkSelectionAndPopupMenu(java.awt.event.MouseEvent evt) {
        int row = eventTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
        if (row == -1) {
            deselectRows();
        }

        if (evt.isPopupTrigger()) {
            if (row != -1) {
                eventTable.setRowSelectionInterval(row, row);
            }
            parent.eventPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    /**
     * Deselects all rows in the table and clears the description information.
     */
    public void deselectRows() {
        if (eventTable != null) {
            eventTable.clearSelection();
            setEventAttributes(null, true);
        }
    }

    /**
     * An action for when a row is selected in the table.
     */
    private void EventListSelected(ListSelectionEvent evt) {
        if (!evt.getValueIsAdjusting()) {
            int row = eventTable.getSelectedModelIndex();

            if (row >= 0) {
                setEventAttributes(events.get(row).getAttributes(), false);
            }
        }
    }

    private void makeAttributesTable() {
        setEventAttributes(null, true);

        // listen for trigger button and double click to edit channel.
        eventAttributesTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (eventAttributesTable.rowAtPoint(new Point(evt.getX(), evt.getY())) == -1) {
                    return;
                }

                if (evt.getClickCount() >= 2) {
                    new ViewContentDialog((String) eventAttributesTable.getModel().getValueAt(eventAttributesTable.getSelectedModelIndex(), ATTRIBUTES_VALUE_COLUMN_NUMBER));
                }
            }
        });

        eventAttributesTable.setSelectionMode(0);

        // Disable HTML in a column.
        DefaultTableCellRenderer noHTMLRenderer = new DefaultTableCellRenderer();
        noHTMLRenderer.putClientProperty("html.disable", Boolean.TRUE);
        eventAttributesTable.getColumnExt(ATTRIBUTES_VALUE_COLUMN_NAME).setCellRenderer(noHTMLRenderer);

        eventAttributesPane.setViewportView(eventAttributesTable);
    }

    private void setEventAttributes(Map<String, String> attributes, boolean cleared) {

        Object[][] tableData;

        if (attributes == null || attributes.size() == 0) {
            tableData = new String[1][2];
            if (cleared) {
                tableData[0][0] = "Please select an event to view its attributes.";
            } else {
                tableData[0][0] = "There are no attributes for this event.";
            }
            tableData[0][1] = "";
        } else {
            tableData = new String[attributes.size()][2];

            int i = 0;
            for (Entry<String, String> entry : attributes.entrySet()) {
                tableData[i][0] = entry.getKey();
                tableData[i][1] = entry.getValue();
                i++;
            }
        }

        if (eventAttributesTable != null) {
            RefreshTableModel model = (RefreshTableModel) eventAttributesTable.getModel();
            model.refreshDataVector(tableData);
        } else {
            eventAttributesTable = new MirthTable();

            eventAttributesTable.setModel(new RefreshTableModel(tableData, new String[] {
                    ATTRIBUTES_NAME_COLUMN_NAME, ATTRIBUTES_VALUE_COLUMN_NAME }) {

                boolean[] canEdit = new boolean[] { false, false };

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }

        // Set highlighter.
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            eventAttributesTable.setHighlighters(highlighter);
        }

    }

    /**
     * Returns the current SystemEventFilter that is set.
     */
    public EventFilter getCurrentFilter() {
        return eventFilter;
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        eventSplitPane = new javax.swing.JSplitPane();
        eventPane = new javax.swing.JScrollPane();
        eventTable = null;
        eventDetailsPanel = new javax.swing.JPanel();
        eventAttributesPane = new javax.swing.JScrollPane();
        eventAttributesTable = null;
        resetButton = new javax.swing.JButton();
        allDayCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        lastSearchCriteriaPane = new javax.swing.JScrollPane();
        lastSearchCriteria = new javax.swing.JTextArea();
        nextPageButton = new javax.swing.JButton();
        pageGoButton = new javax.swing.JButton();
        pageSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        previousPageButton = new javax.swing.JButton();
        pageNumberField = new com.mirth.connect.client.ui.components.MirthTextField();
        pageNumberLabel = new javax.swing.JLabel();
        pageSizeLabel = new javax.swing.JLabel();
        countButton = new com.mirth.connect.client.ui.components.MirthButton();
        resultsLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        endDatePicker = new com.mirth.connect.client.ui.components.MirthDatePicker();
        startDatePicker = new com.mirth.connect.client.ui.components.MirthDatePicker();
        nameField = new javax.swing.JTextField();
        startTimePicker = new com.mirth.connect.client.ui.components.MirthTimePicker();
        endTimePicker = new com.mirth.connect.client.ui.components.MirthTimePicker();
        filterButton = new javax.swing.JButton();
        advSearchButton = new javax.swing.JButton();
        levelBoxInformation = new com.mirth.connect.client.ui.components.MirthCheckBox();
        levelBoxWarning = new com.mirth.connect.client.ui.components.MirthCheckBox();
        levelBoxError = new com.mirth.connect.client.ui.components.MirthCheckBox();
        pageTotalLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));

        eventSplitPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        eventSplitPane.setDividerLocation(250);
        eventSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        eventSplitPane.setResizeWeight(1.0);

        eventPane.setViewportView(eventTable);

        eventSplitPane.setLeftComponent(eventPane);

        eventDetailsPanel.setBackground(new java.awt.Color(255, 255, 255));
        eventDetailsPanel.setMinimumSize(new java.awt.Dimension(0, 150));

        eventAttributesPane.setViewportView(eventAttributesTable);

        javax.swing.GroupLayout eventDetailsPanelLayout = new javax.swing.GroupLayout(eventDetailsPanel);
        eventDetailsPanel.setLayout(eventDetailsPanelLayout);
        eventDetailsPanelLayout.setHorizontalGroup(
            eventDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(eventAttributesPane, javax.swing.GroupLayout.DEFAULT_SIZE, 832, Short.MAX_VALUE)
                .addContainerGap())
        );
        eventDetailsPanelLayout.setVerticalGroup(
            eventDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, eventDetailsPanelLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(eventAttributesPane, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                .addContainerGap())
        );

        eventSplitPane.setRightComponent(eventDetailsPanel);

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

        nextPageButton.setText("Next >");
        nextPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextPageButtonActionPerformed(evt);
            }
        });

        pageGoButton.setText("Go");
        pageGoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pageGoButtonActionPerformed(evt);
            }
        });

        pageSizeField.setToolTipText("<html>\nAfter changing the page size, a new search must be performed for the changes to<br/>\ntake effect.  The default page size can also be configured on the Settings panel.\n</html>");

        previousPageButton.setText("< Prev");
        previousPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousPageButtonActionPerformed(evt);
            }
        });

        pageNumberField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        pageNumberField.setToolTipText("Enter a page number and press Enter to jump to that page.");
        pageNumberField.setPreferredSize(new java.awt.Dimension(40, 22));

        pageNumberLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pageNumberLabel.setText("Page");

        pageSizeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pageSizeLabel.setText("Page Size:");
        pageSizeLabel.setMaximumSize(new java.awt.Dimension(78, 15));

        countButton.setText("Count");
        countButton.setToolTipText("Count the number of overall messages for the current search criteria.");
        countButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                countButtonActionPerformed(evt);
            }
        });

        resultsLabel.setForeground(new java.awt.Color(204, 0, 0));
        resultsLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        resultsLabel.setText("Results");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Start Time:");
        jLabel3.setMaximumSize(new java.awt.Dimension(78, 15));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("End Time:");
        jLabel2.setMaximumSize(new java.awt.Dimension(78, 15));

        nameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        nameLabel.setText("Name:");

        nameField.setToolTipText("<html>\nSearch all message content for the given string. This process could take a long<br/>\ntime depending on the amount of message content currently stored. Any message<br/>\ncontent that was encrypted by this channel will not be searchable.\n</html>");

        filterButton.setText("Search");
        filterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterButtonActionPerformed(evt);
            }
        });

        advSearchButton.setText("Advanced...");
        advSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advSearchButtonActionPerformed(evt);
            }
        });

        levelBoxInformation.setBackground(new java.awt.Color(255, 255, 255));
        levelBoxInformation.setText("INFORMATION");
        levelBoxInformation.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        levelBoxInformation.setPreferredSize(new java.awt.Dimension(90, 22));

        levelBoxWarning.setBackground(new java.awt.Color(255, 255, 255));
        levelBoxWarning.setText("WARNING");
        levelBoxWarning.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        levelBoxWarning.setMaximumSize(new java.awt.Dimension(83, 23));
        levelBoxWarning.setMinimumSize(new java.awt.Dimension(83, 23));

        levelBoxError.setBackground(new java.awt.Color(255, 255, 255));
        levelBoxError.setText("ERROR");
        levelBoxError.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        levelBoxError.setMaximumSize(new java.awt.Dimension(83, 23));
        levelBoxError.setMinimumSize(new java.awt.Dimension(83, 23));

        pageTotalLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pageTotalLabel.setText("of ?");
        pageTotalLabel.setAlignmentY(0.0F);
        pageTotalLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(eventSplitPane)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pageSizeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pageSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(advSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(resetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(startDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(startTimePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(endDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(endTimePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(nameField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(allDayCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(levelBoxInformation, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                            .addComponent(levelBoxError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(levelBoxWarning, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(filterButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lastSearchCriteriaPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addGap(18, 18, 18)
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
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(startTimePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(startDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(endTimePicker, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(endDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nameLabel)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(countButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(resultsLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(pageGoButton)
                            .addComponent(pageTotalLabel)
                            .addComponent(pageNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pageNumberLabel))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(nextPageButton)
                    .addComponent(previousPageButton))
                .addGap(10, 10, 10)
                .addComponent(eventSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(90, 90, 90)
                        .addComponent(pageSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(87, 87, 87)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(pageSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(advSearchButton)
                            .addComponent(resetButton)
                            .addComponent(filterButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(levelBoxInformation, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(allDayCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(levelBoxWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(levelBoxError, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(lastSearchCriteriaPane, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        resetSearchCriteria();
    }//GEN-LAST:event_resetButtonActionPerformed

    private void allDayCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allDayCheckBoxActionPerformed
        startTimePicker.setEnabled(startDatePicker.getDate() != null && !allDayCheckBox.isSelected());
        endTimePicker.setEnabled(endDatePicker.getDate() != null && !allDayCheckBox.isSelected());
    }//GEN-LAST:event_allDayCheckBoxActionPerformed

    private void nextPageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextPageButtonActionPerformed
        loadPageNumber(events.getPageNumber() + 1);
    }//GEN-LAST:event_nextPageButtonActionPerformed

    private void pageGoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pageGoButtonActionPerformed
        jumpToPageNumber();
    }//GEN-LAST:event_pageGoButtonActionPerformed

    private void previousPageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousPageButtonActionPerformed
        loadPageNumber(events.getPageNumber() - 1);
    }//GEN-LAST:event_previousPageButtonActionPerformed

    private void countButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_countButtonActionPerformed
        final String workingId = parent.startWorking("Counting search result size...");
        filterButton.setEnabled(false);
        nextPageButton.setEnabled(false);
        previousPageButton.setEnabled(false);
        countButton.setEnabled(false);
        pageGoButton.setEnabled(false);
        final EventBrowser eventBrowser = this;

        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }

        worker = new SwingWorker<Void, Void>() {
            private Exception e;

            public Void doInBackground() {
                try {
                    events.setItemCount(parent.mirthClient.getEventCount(eventFilter));
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
                        parent.alertException(eventBrowser, e.getStackTrace(), e.getMessage());
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

    private void filterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterButtonActionPerformed
        runSearch();
    }//GEN-LAST:event_filterButtonActionPerformed

    private void advSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advSearchButtonActionPerformed
        advancedSearchPopup.setBackground(UIConstants.COMBO_BOX_BACKGROUND);
        advancedSearchPopup.setLocationRelativeTo(parent);
        advancedSearchPopup.setVisible(true);

        updateAdvancedSearchButtonFont();
    }//GEN-LAST:event_advSearchButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton advSearchButton;
    private com.mirth.connect.client.ui.components.MirthCheckBox allDayCheckBox;
    private com.mirth.connect.client.ui.components.MirthButton countButton;
    private com.mirth.connect.client.ui.components.MirthDatePicker endDatePicker;
    private com.mirth.connect.client.ui.components.MirthTimePicker endTimePicker;
    private javax.swing.JScrollPane eventAttributesPane;
    private com.mirth.connect.client.ui.components.MirthTable eventAttributesTable;
    private javax.swing.JPanel eventDetailsPanel;
    private javax.swing.JScrollPane eventPane;
    private javax.swing.JSplitPane eventSplitPane;
    private com.mirth.connect.client.ui.components.MirthTable eventTable;
    private javax.swing.JButton filterButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextArea lastSearchCriteria;
    private javax.swing.JScrollPane lastSearchCriteriaPane;
    private com.mirth.connect.client.ui.components.MirthCheckBox levelBoxError;
    private com.mirth.connect.client.ui.components.MirthCheckBox levelBoxInformation;
    private com.mirth.connect.client.ui.components.MirthCheckBox levelBoxWarning;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JButton nextPageButton;
    private javax.swing.JButton pageGoButton;
    private com.mirth.connect.client.ui.components.MirthTextField pageNumberField;
    private javax.swing.JLabel pageNumberLabel;
    private com.mirth.connect.client.ui.components.MirthTextField pageSizeField;
    private javax.swing.JLabel pageSizeLabel;
    private javax.swing.JLabel pageTotalLabel;
    private javax.swing.JButton previousPageButton;
    private javax.swing.JButton resetButton;
    private javax.swing.JLabel resultsLabel;
    private com.mirth.connect.client.ui.components.MirthDatePicker startDatePicker;
    private com.mirth.connect.client.ui.components.MirthTimePicker startTimePicker;
    // End of variables declaration//GEN-END:variables
}

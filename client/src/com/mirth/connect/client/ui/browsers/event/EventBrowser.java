/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.browsers.event;

import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.DateFormatter;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.EventListHandler;
import com.mirth.connect.client.core.ListHandlerException;
import com.mirth.connect.client.ui.CellData;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.ImageCellRenderer;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.ViewContentDialog;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.model.Event;
import com.mirth.connect.model.Event.Level;
import com.mirth.connect.model.Event.Outcome;
import com.mirth.connect.model.User;
import com.mirth.connect.model.filters.EventFilter;

public class EventBrowser extends javax.swing.JPanel {

    private final int FIRST_PAGE = 0;
    private final int PREVIOUS_PAGE = -1;
    private final int NEXT_PAGE = 1;
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
    private EventListHandler eventListHandler;
    private List<Event> eventList;
    private EventFilter eventFilter;
    private int eventCount = -1;
    private int currentPage = 0;
    private int pageSize;
    private EventBrowserAdvancedFilter advancedSearchFilterPopup;
    private Map<Integer, String> userMapById = new LinkedHashMap<Integer, String>();

    /**
     * Constructs the new event browser and sets up its default
     * information/layout.
     */
    public EventBrowser() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        makeEventTable();
        makeAttributesTable();
        updateCachedUserMap();

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

        String[] values = new String[Level.values().length + 1];
        values[0] = UIConstants.ALL_OPTION;
        for (int i = 1; i < values.length; i++) {
            values[i] = Level.values()[i - 1].toString();
        }

        levelComboBox.setModel(new javax.swing.DefaultComboBoxModel(values));

        advancedSearchFilterPopup = new EventBrowserAdvancedFilter(parent, "Advanced Search Filter", true, userMapById);
        advancedSearchFilterPopup.setVisible(false);

        eventSplitPane.setDividerLocation(0.8);
    }

    /**
     * Loads up a clean message browser as if a new one was constructed.
     */
    public void loadNew() {
        // Set the default page size
        pageSize = Preferences.userNodeForPackage(Mirth.class).getInt("eventBrowserPageSize", 100);
        pageSizeField.setText(pageSize + "");

        // use the start filters and make the table.
        eventListHandler = null;

        nameField.setText("");
        levelComboBox.setSelectedIndex(0);
        
        Calendar calendar = Calendar.getInstance();
        startDatePicker.setDate(calendar.getTime());
        calendar.add(Calendar.DATE, 1);
        endDatePicker.setDate(calendar.getTime());
        
        startTimePicker.setDate("00:00 am");
        endTimePicker.setDate("00:00 am");

        updateCachedUserMap();

        advancedSearchFilterPopup.reset();

        searchButtonActionPerformed(null);
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
    public void refresh() {
        searchButtonActionPerformed(null);
    }

    public void updateEventTable(List<Event> systemEventList) {
        Object[][] tableData = null;

        if (systemEventList != null) {
            tableData = new Object[systemEventList.size()][7];

            for (int i = 0; i < systemEventList.size(); i++) {
                Event systemEvent = systemEventList.get(i);

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
                
                tableData[i][2] = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS:%1$tL", systemEvent.getDateTime());
                
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

        int systemEventListSize = 0;
        if (systemEventList != null) {
            systemEventListSize = systemEventList.size();
        }

        if (currentPage == 0) {
            previousPageButton.setEnabled(false);
        } else {
            previousPageButton.setEnabled(true);
        }

        int numberOfPages = getNumberOfPages(pageSize, eventCount);
        if (systemEventListSize < pageSize || pageSize == 0) {
            nextPageButton.setEnabled(false);
        } else if (currentPage == numberOfPages) {
            nextPageButton.setEnabled(false);
        } else {
            nextPageButton.setEnabled(true);
        }

        int startResult;
        if (systemEventListSize == 0) {
            startResult = 0;
        } else {
            startResult = (currentPage * pageSize) + 1;
        }

        int endResult;
        if (pageSize == 0) {
            endResult = systemEventListSize;
        } else {
            endResult = (currentPage + 1) * pageSize;
        }

        if (systemEventListSize < pageSize) {
            endResult = endResult - (pageSize - systemEventListSize);
        }

        if (eventCount == -1) {
            resultsLabel.setText("Results " + startResult + " - " + endResult);
        } else {
            resultsLabel.setText("Results " + startResult + " - " + endResult + " of " + eventCount);
        }

        if (eventTable != null) {
            RefreshTableModel model = (RefreshTableModel) eventTable.getModel();
            model.refreshDataVector(tableData);
        } else {
            eventTable = new MirthTable();
            eventTable.setModel(new RefreshTableModel(tableData, new String[]{EVENT_ID_COLUMN_NAME, EVENT_LEVEL_COLUMN_NAME, EVENT_DATE_COLUMN_NAME, EVENT_NAME_COLUMN_NAME, EVENT_USER_COLUMN_NAME, EVENT_OUTCOME_COLUMN_NAME, EVENT_IP_ADDRESS_COLUMN_NAME}) {

                boolean[] canEdit = new boolean[]{false, false, false, false, false, false, false};

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }

        // Set highlighter.
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            eventTable.setHighlighters(highlighter);
        }

        deselectRows();
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
    }

    private void getEventTableData(EventListHandler handler, int page) {
        if (handler != null) {
            // Do all paging information below.
            try {
                eventCount = handler.getSize();
                currentPage = handler.getCurrentPage();
                pageSize = handler.getPageSize();

                if (page == FIRST_PAGE) {
                    eventList = handler.getFirstPage();
                    currentPage = handler.getCurrentPage();
                } else if (page == PREVIOUS_PAGE) {
                    if (currentPage == 0) {
                        return;
                    }
                    eventList = handler.getPreviousPage();
                    currentPage = handler.getCurrentPage();
                } else if (page == NEXT_PAGE) {
                    int numberOfPages = getNumberOfPages(pageSize, eventCount);
                    if (currentPage == numberOfPages) {
                        return;
                    }

                    eventList = handler.getNextPage();
                    if (eventList.size() == 0) {
                        eventList = handler.getPreviousPage();
                    }
                    currentPage = handler.getCurrentPage();
                }

            } catch (ListHandlerException e) {
                eventList = null;
                parent.alertException(this, e.getStackTrace(), e.getMessage());
            }
        }
    }

    private int getNumberOfPages(int pageSize, int eventCount) {
        int numberOfPages;
        if (eventCount == -1) {
            return -1;
        }
        if (pageSize == 0) {
            numberOfPages = 0;
        } else {
            numberOfPages = eventCount / pageSize;
            if ((eventCount != 0) && ((eventCount % pageSize) == 0)) {
                numberOfPages--;
            }
        }

        return numberOfPages;
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been
     * pushed.  Deselects the rows if no row was selected.
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
                setEventAttributes(eventList.get(row).getAttributes(), false);
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

    private void setEventAttributes(Map<String, Object> attributes, boolean cleared) {

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
            for (Entry<String, Object> entry : attributes.entrySet()) {
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

            eventAttributesTable.setModel(new RefreshTableModel(tableData, new String[]{ATTRIBUTES_NAME_COLUMN_NAME, ATTRIBUTES_VALUE_COLUMN_NAME}) {

                boolean[] canEdit = new boolean[]{false, false};

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

        filterPanel = new javax.swing.JPanel();
        pagePanel = new javax.swing.JPanel();
        resultsLabel = new javax.swing.JLabel();
        pageSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        nextPageButton = new javax.swing.JButton();
        previousPageButton = new javax.swing.JButton();
        pageSizeLabel = new javax.swing.JLabel();
        searchPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameField = new com.mirth.connect.client.ui.components.MirthTextField();
        levelComboBox = new javax.swing.JComboBox();
        levelLabel = new javax.swing.JLabel();
        startTimeLabel = new javax.swing.JLabel();
        startDatePicker = new com.mirth.connect.client.ui.components.MirthDatePicker();
        startTimePicker = new com.mirth.connect.client.ui.components.MirthTimePicker();
        endTimePicker = new com.mirth.connect.client.ui.components.MirthTimePicker();
        endDatePicker = new com.mirth.connect.client.ui.components.MirthDatePicker();
        endTimeLabel = new javax.swing.JLabel();
        advancedSearchButton = new javax.swing.JButton();
        searchButton = new javax.swing.JButton();
        eventSplitPane = new javax.swing.JSplitPane();
        eventPane = new javax.swing.JScrollPane();
        eventTable = null;
        eventDetailsPanel = new javax.swing.JPanel();
        eventAttributesPane = new javax.swing.JScrollPane();
        eventAttributesTable = null;

        setBackground(new java.awt.Color(255, 255, 255));

        filterPanel.setBackground(new java.awt.Color(255, 255, 255));
        filterPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        pagePanel.setBackground(new java.awt.Color(255, 255, 255));
        pagePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1)));

        resultsLabel.setForeground(new java.awt.Color(204, 0, 0));
        resultsLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        resultsLabel.setText("Results");

        pageSizeField.setToolTipText("After changing the page size, a new search must be performed for the changes to take effect.  The default page size can also be configured on the Settings panel.");

        nextPageButton.setText(">");
        nextPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextPageButtonActionPerformed(evt);
            }
        });

        previousPageButton.setText("<");
        previousPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousPageButtonActionPerformed(evt);
            }
        });

        pageSizeLabel.setText("Page Size:");

        javax.swing.GroupLayout pagePanelLayout = new javax.swing.GroupLayout(pagePanel);
        pagePanel.setLayout(pagePanelLayout);
        pagePanelLayout.setHorizontalGroup(
            pagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pagePanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pagePanelLayout.createSequentialGroup()
                            .addComponent(pageSizeLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(pageSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(resultsLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pagePanelLayout.createSequentialGroup()
                        .addComponent(previousPageButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nextPageButton))))
        );

        pagePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {nextPageButton, previousPageButton});

        pagePanelLayout.setVerticalGroup(
            pagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pagePanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(resultsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pageSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pageSizeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nextPageButton)
                    .addComponent(previousPageButton)))
        );

        searchPanel.setBackground(new java.awt.Color(255, 255, 255));
        searchPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        nameLabel.setText("Name:");

        levelLabel.setText("Level:");

        startTimeLabel.setText("Start Time:");

        endTimeLabel.setText("End Time:");

        advancedSearchButton.setText("Advanced...");
        advancedSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advancedSearchButtonActionPerformed(evt);
            }
        });

        searchButton.setText("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(levelLabel)
                    .addComponent(endTimeLabel)
                    .addComponent(startTimeLabel)
                    .addComponent(nameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(startDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(endDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(endTimePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(startTimePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchPanelLayout.createSequentialGroup()
                        .addComponent(levelComboBox, 0, 125, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(advancedSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchButton)
                .addContainerGap())
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startTimeLabel)
                    .addComponent(startDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startTimePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(endDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(endTimeLabel)
                    .addComponent(endTimePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(levelComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(levelLabel)
                    .addComponent(advancedSearchButton)
                    .addComponent(searchButton)))
        );

        javax.swing.GroupLayout filterPanelLayout = new javax.swing.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addComponent(searchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                .addComponent(pagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(pagePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

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
                .addComponent(eventAttributesPane, javax.swing.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
                .addContainerGap())
        );
        eventDetailsPanelLayout.setVerticalGroup(
            eventDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, eventDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(eventAttributesPane, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                .addContainerGap())
        );

        eventSplitPane.setRightComponent(eventDetailsPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(filterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(eventSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 661, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(eventSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void advancedSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advancedSearchButtonActionPerformed
        // display the advanced search filter pop up window.
        Integer user = advancedSearchFilterPopup.getUser();
        String outcome = advancedSearchFilterPopup.getOutcome();
        String ipAddress = advancedSearchFilterPopup.getIpAddress();

        advancedSearchFilterPopup = new EventBrowserAdvancedFilter(parent, "Advanced Search Filter", true, userMapById);
        advancedSearchFilterPopup.setFieldValues(user, outcome, ipAddress);

        advancedSearchFilterPopup.setVisible(true);
    }//GEN-LAST:event_advancedSearchButtonActionPerformed

    /**
     * An action when the filter button is pressed. Creates the actual filter
     * and remakes the table with that filter.
     */
    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        eventFilter = new EventFilter();
        
        if (startDatePicker.getDate() != null && endDatePicker.getDate() != null
                && startTimePicker.getDate() != null && endTimePicker.getDate() != null) {
            SimpleDateFormat timeDateFormat = new SimpleDateFormat("hh:mm aa");
            DateFormatter timeFormatter = new DateFormatter(timeDateFormat);

            Date startDate = startDatePicker.getDate();
            Date endDate = endDatePicker.getDate();

            String startTime = startTimePicker.getDate();
            String endTime = endTimePicker.getDate();

            Date startTimeDate;
            Date endTimeDate;

            try {
                startTimeDate = (Date) timeFormatter.stringToValue(startTime);
                endTimeDate = (Date) timeFormatter.stringToValue(endTime);
            } catch (Exception e) {
                parent.alertError(this, "Invalid date.");
                return;
            }

            Calendar startDateCalendar = Calendar.getInstance();
            Calendar endDateCalendar = Calendar.getInstance();
            Calendar startTimeCalendar = Calendar.getInstance();
            Calendar endTimeCalendar = Calendar.getInstance();

            startDateCalendar.setTime(startDate);
            endDateCalendar.setTime(endDate);
            startTimeCalendar.setTime(startTimeDate);
            endTimeCalendar.setTime(endTimeDate);

            Calendar startCalendar = Calendar.getInstance();
            Calendar endCalendar = Calendar.getInstance();

            startCalendar.set(startDateCalendar.get(Calendar.YEAR), startDateCalendar.get(Calendar.MONTH), startDateCalendar.get(Calendar.DATE), startTimeCalendar.get(Calendar.HOUR_OF_DAY), startTimeCalendar.get(Calendar.MINUTE), startTimeCalendar.get(Calendar.SECOND));
            endCalendar.set(endDateCalendar.get(Calendar.YEAR), endDateCalendar.get(Calendar.MONTH), endDateCalendar.get(Calendar.DATE), endTimeCalendar.get(Calendar.HOUR_OF_DAY), endTimeCalendar.get(Calendar.MINUTE), endTimeCalendar.get(Calendar.SECOND));

            if (startCalendar.getTimeInMillis() > endCalendar.getTimeInMillis()) {
                parent.alertError(this, "Start date cannot be after the end date.");
                return;
            }
            
            eventFilter.setStartDate(startCalendar);
            eventFilter.setEndDate(endCalendar);
        }

        if (!nameField.getText().equals("")) {
            eventFilter.setName(nameField.getText());
        }

        if (!((String) levelComboBox.getSelectedItem()).equalsIgnoreCase(UIConstants.ALL_OPTION)) {
            for (int i = 0; i < Level.values().length; i++) {
                if (((String) levelComboBox.getSelectedItem()).equalsIgnoreCase(Level.values()[i].toString())) {
                    eventFilter.setLevel(Level.values()[i]);
                }
            }
        }

        // Start advanced search properties

        if (advancedSearchFilterPopup.getUser() != -1) {
            eventFilter.setUserId(advancedSearchFilterPopup.getUser());
        }

        if (!advancedSearchFilterPopup.getOutcome().equals(UIConstants.ALL_OPTION)) {
            for (int i = 0; i < Outcome.values().length; i++) {
                if (advancedSearchFilterPopup.getOutcome().equalsIgnoreCase(Outcome.values()[i].toString())) {
                    eventFilter.setOutcome(Outcome.values()[i]);
                }
            }
        }

        if (!advancedSearchFilterPopup.getIpAddress().equals("")) {
            eventFilter.setIpAddress(advancedSearchFilterPopup.getIpAddress());
        }

        // End advanced search properties

        if (!pageSizeField.getText().equals("")) {
            pageSize = Integer.parseInt(pageSizeField.getText());
        }

        parent.setWorking("Loading events...", true);

        if (eventListHandler == null) {
            updateEventTable(null);
        }

        class EventWorker extends SwingWorker<Void, Void> {

            public Void doInBackground() {
                try {
                    eventListHandler = parent.mirthClient.getEventListHandler(eventFilter, pageSize, false);
                } catch (ClientException e) {
                    parent.alertException(parent, e.getStackTrace(), e.getMessage());
                }
                getEventTableData(eventListHandler, FIRST_PAGE);
                return null;
            }

            public void done() {
                if (eventListHandler != null) {
                    updateEventTable(eventList);
                } else {
                    updateEventTable(null);
                }

                parent.setWorking("", false);
            }
        }
        ;
        EventWorker worker = new EventWorker();
        worker.execute();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void previousPageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousPageButtonActionPerformed
        parent.setWorking("Loading previous page...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                getEventTableData(eventListHandler, PREVIOUS_PAGE);
                return null;
            }

            public void done() {
                if (eventListHandler != null) {
                    updateEventTable(eventList);
                } else {
                    updateEventTable(null);
                }
                parent.setWorking("", false);
            }
        };
        worker.execute();
    }//GEN-LAST:event_previousPageButtonActionPerformed

    private void nextPageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextPageButtonActionPerformed
        parent.setWorking("Loading next page...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                getEventTableData(eventListHandler, NEXT_PAGE);
                return null;
            }

            public void done() {
                if (eventListHandler != null) {
                    updateEventTable(eventList);
                } else {
                    updateEventTable(null);
                }
                parent.setWorking("", false);
            }
        };
        worker.execute();
    }//GEN-LAST:event_nextPageButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton advancedSearchButton;
    private com.mirth.connect.client.ui.components.MirthDatePicker endDatePicker;
    private javax.swing.JLabel endTimeLabel;
    private com.mirth.connect.client.ui.components.MirthTimePicker endTimePicker;
    private javax.swing.JScrollPane eventAttributesPane;
    private com.mirth.connect.client.ui.components.MirthTable eventAttributesTable;
    private javax.swing.JPanel eventDetailsPanel;
    private javax.swing.JScrollPane eventPane;
    private javax.swing.JSplitPane eventSplitPane;
    private com.mirth.connect.client.ui.components.MirthTable eventTable;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JComboBox levelComboBox;
    private javax.swing.JLabel levelLabel;
    private com.mirth.connect.client.ui.components.MirthTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JButton nextPageButton;
    private javax.swing.JPanel pagePanel;
    private com.mirth.connect.client.ui.components.MirthTextField pageSizeField;
    private javax.swing.JLabel pageSizeLabel;
    private javax.swing.JButton previousPageButton;
    private javax.swing.JLabel resultsLabel;
    private javax.swing.JButton searchButton;
    private javax.swing.JPanel searchPanel;
    private com.mirth.connect.client.ui.components.MirthDatePicker startDatePicker;
    private javax.swing.JLabel startTimeLabel;
    private com.mirth.connect.client.ui.components.MirthTimePicker startTimePicker;
    // End of variables declaration//GEN-END:variables
}

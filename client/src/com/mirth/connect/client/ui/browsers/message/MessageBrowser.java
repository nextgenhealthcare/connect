/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.browsers.message;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.DateFormatter;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.EDITokenMarker;
import org.syntax.jedit.tokenmarker.HL7TokenMarker;
import org.syntax.jedit.tokenmarker.X12TokenMarker;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;
import org.w3c.dom.Document;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.MessageListHandler;
import com.mirth.connect.client.ui.EditMessageDialog;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.MirthFileFilter;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.ViewContentDialog;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthSyntaxTextArea;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.util.DisplayUtil;
import com.mirth.connect.model.Attachment;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.MessageObject.Protocol;
import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.model.filters.MessageObjectFilter;
import com.mirth.connect.plugins.AttachmentViewer;

/**
 * The message browser panel.
 */
public class MessageBrowser extends javax.swing.JPanel {

    private final int FIRST_PAGE = 0;
    private final int PREVIOUS_PAGE = -1;
    private final int NEXT_PAGE = 1;
    private final String MESSAGE_ID_COLUMN_NAME = "Message ID";
    private final String DATE_COLUMN_NAME = "Date & Time";
    private final String CONNECTOR_COLUMN_NAME = "Connector";
    private final String STATUS_COLUMN_NAME = "Status";
    private final String SCOPE_COLUMN_NAME = "Scope";
    private final String KEY_COLUMN_NAME = "Variable";
    private final String VALUE_COLUMN_NAME = "Value";
    private final String TYPE_COLUMN_NAME = "Type";
    private final String SOURCE_COLUMN_NAME = "Source";
    private final String PROTOCOL_COLUMN_NAME = "Protocol";
    private final String NUMBER_COLUMN_NAME = "#";
    private final String ATTACHMENTID_COLUMN_NAME = "Attachment Id";
    private Frame parent;
    private MessageListHandler messageListHandler;
    private List<MessageObject> messageObjectList;
    private MessageObjectFilter messageObjectFilter;
    private int messageCount = -1;
    private int currentPage = 0;
    private int pageSize;
    private MessageBrowserAdvancedFilter advSearchFilterPopup;
    private JPopupMenu attachmentPopupMenu;

    /**
     * Constructs the new message browser and sets up its default
     * information/layout.
     */
    public MessageBrowser() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        makeMessageTable();
        makeMappingsTable();
        updateAttachmentsTable(null, true);
        descriptionTabbedPane.remove(attachmentsPane);

        this.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    parent.messagePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    parent.messagePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });

        String[] statusValues = new String[MessageObject.Status.values().length + 1];
        statusValues[0] = UIConstants.ALL_OPTION;
        for (int i = 1; i < statusValues.length; i++) {
            statusValues[i] = MessageObject.Status.values()[i - 1].toString();
        }
        statusComboBox.setModel(new javax.swing.DefaultComboBoxModel(statusValues));

        pageSizeField.setDocument(new MirthFieldConstraints(3, false, false, true));

        attachmentPopupMenu = new JPopupMenu();
        JMenuItem viewAttach = new JMenuItem("View Attachment");
        viewAttach.setIcon(new ImageIcon(Frame.class.getResource("images/attach.png")));
        viewAttach.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                viewAttachment();
            }
        });
        attachmentPopupMenu.add(viewAttach);

        advSearchFilterPopup = new MessageBrowserAdvancedFilter(parent, "Advanced Search Filter", true, true);
        advSearchFilterPopup.setVisible(false);
    }

    public Object[][] updateAttachmentList(MessageObject message) {
        if (message == null) {
            return null;
        }
        try {
            String attachMessId;
            if (message.getCorrelationId() != null) {
                attachMessId = message.getCorrelationId();
            } else {
                attachMessId = message.getId();
            }
            List<Attachment> attachments = parent.mirthClient.getAttachmentIdsByMessageId(attachMessId);
            Iterator i = attachments.iterator();
            ArrayList attachData = new ArrayList();
            int count = 1;
            ArrayList<String> types = new ArrayList();
            // get arraylist of all types
            while (i.hasNext()) {
                Attachment a = (Attachment) i.next();
                String type = a.getType();
                if (!types.contains(type)) {
                    types.add(type);
                }
            }
            Iterator typesIterator = types.iterator();
            while (typesIterator.hasNext()) {
                String type = (String) typesIterator.next();
                Iterator attachmentIterator = attachments.iterator();
                // If handle multiples
                if (getAttachmentViewer(type) != null && getAttachmentViewer(type).handleMultiple()) {
                    String number = Integer.toString(count);
                    String attachment_Ids = "";
                    int j = 0;
                    while (attachmentIterator.hasNext()) {
                        Attachment a = (Attachment) attachmentIterator.next();
                        if (type.equals(a.getType())) {
                            if (attachment_Ids.equals("")) {
                                attachment_Ids = a.getAttachmentId();
                            } else {
                                count++;
                                attachment_Ids = attachment_Ids + ", " + a.getAttachmentId();
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
                    while (attachmentIterator.hasNext()) {
                        Attachment a = (Attachment) attachmentIterator.next();
                        if (a.getType().equals(type)) {
                            Object[] rowData = new Object[3];
                            rowData[0] = Integer.toString(count);
                            rowData[1] = a.getType();
                            rowData[2] = a.getAttachmentId();
                            attachData.add(rowData);
                            count++;
                        }
                    }
                }
            }
            Object[][] temp = new Object[attachData.size()][3];
            Iterator varIter = attachData.iterator();
            int rowCount = 0;
            while (varIter.hasNext()) {
                temp[rowCount] = (Object[]) varIter.next();
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
     * Loads up a clean message browser as if a new one was constructed.
     */
    public void loadNew() {
        // Set the default page size
        pageSize = Preferences.userNodeForPackage(Mirth.class).getInt("messageBrowserPageSize", 20);
        pageSizeField.setText(pageSize + "");
        
        boolean formatXmlSelected = Preferences.userNodeForPackage(Mirth.class).getBoolean("messageBrowserFormatXml", true);
        formatXmlRawCheckBox.setSelected(formatXmlSelected);
        formatXmlTransformedCheckBox.setSelected(formatXmlSelected);
        formatXmlEncodedCheckBox.setSelected(formatXmlSelected);
        
        disableFormatXmlCheckBox();

        // use the start filters and make the table.
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 6, -1, false);
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 7, 7, true);
        messageListHandler = null;

        statusComboBox.setSelectedIndex(0);
        quickSearchField.setText("");
        
        Channel selectedChannel = parent.getSelectedChannelFromDashboard();
        
        // Channel could be null if user doesn't have permission to get channels, so default to true.
        boolean allowSearch = true;
        if (selectedChannel != null) {
            allowSearch = !((String) selectedChannel.getProperties().get("encryptData")).equalsIgnoreCase("true");
        }
        
        quickSearchLabel.setEnabled(allowSearch);
        quickSearchField.setEnabled(allowSearch);
        advSearchFilterPopup.reset(allowSearch);

        Calendar calendar = Calendar.getInstance();
        mirthDatePicker1.setDate(calendar.getTime());
        calendar.add(Calendar.DATE, 1);
        mirthDatePicker2.setDate(calendar.getTime());

        mirthTimePicker1.setDate("00:00 am");
        mirthTimePicker2.setDate("00:00 am");

        filterButtonActionPerformed(null);
        descriptionTabbedPane.setSelectedIndex(0);
    }

    /**
     * Refreshes the panel with the curent filter information.
     */
    public void refresh() {
        filterButtonActionPerformed(null);
    }

    public MessageObject getMessageObjectById(String messageId) {
        if (messageObjectList != null) {
            for (MessageObject message : messageObjectList) {
                if (message.getId().equals(messageId)) {
                    return message;
                }
            }
        }
        
        return null;
    }

    public void importMessages() {
        File file = parent.importFile("XML");
        String channelId = parent.getSelectedChannelIdFromDashboard();
        
        try {
            int count = parent.mirthClient.importMessages(channelId, file, UIConstants.CHARSET);
            
            if (count > 0) {
                parent.alertInformation(this, count + " messages have been successfully imported.");    
            } else {
                parent.alertError(this, "No messages were found in the file.");
            }
        } catch (ClientException e) {
            parent.alertException(this, e.getStackTrace(), "Error importing messages. " + e.getMessage());
        }
    }

    /**
     * Export the current messages to be imported at a later time
     */
    public void exportMessages() {
        String[] formatOptions = new String[] {"Message Object XML", "Plain Text", "Cancel"};
        String[] plainTextOptions = new String[] {"Raw", "Transformed", "Encoded"};
        
        int plainTextResponse = 0;
        int formatResponse = 0;

        formatResponse = JOptionPane.showOptionDialog(parent, "Which of the following formats would you like to export the messages to?", "Select an Option", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, formatOptions, -1);

        if ((formatResponse == -1) || (formatResponse == 2)) {
            return;
        } else if (formatResponse == 1) {
            plainTextResponse = JOptionPane.showOptionDialog(parent, "Which message data would you like to export?", "Select an Option", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, plainTextOptions, -1);

            if (plainTextResponse == -1) {
                return;
            }
        }

        JFileChooser exportFileChooser = new JFileChooser();
        File currentDir = new File(Preferences.userNodeForPackage(Mirth.class).get("currentDirectory", ""));
        
        if (currentDir.exists()) {
            exportFileChooser.setCurrentDirectory(currentDir);
        }
        
        exportFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        String fileExtension = null;
        
        if (formatResponse == 1) {
            exportFileChooser.setFileFilter(new MirthFileFilter("TXT"));
            fileExtension = ".txt";
        } else {
            exportFileChooser.setFileFilter(new MirthFileFilter("XML"));
            fileExtension = ".xml";
        }

        // they clicked OK, so do the export
        if (exportFileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = exportFileChooser.getSelectedFile();

            if (file.exists()) {
                if (!parent.alertOption(this, "The file " + file.getName() + " already exists. Would you like to overwrite it?")) {
                    return;
                }
            }

            if (file.getName().length() < 4 || !file.getName().endsWith(fileExtension)) {
                file = new File(file.getAbsolutePath() + fileExtension);
            }

            try {
                int count = parent.mirthClient.exportMessages(formatResponse, plainTextResponse, messageListHandler.getFilter(), pageSize, file, UIConstants.CHARSET);
                
                if (count > 0) {
                    parent.alertInformation(this, count + " messages were sucessfully exported to " + file.getPath());    
                }
            } catch (Exception e) {
                parent.alertException(this, e.getStackTrace(), "Error exporting messages.");
            }
        }
    }

    public void updateMessageTable(List<MessageObject> messageObjectList) {
        Object[][] tableData = null;

        if (messageObjectList != null) {
            tableData = new Object[messageObjectList.size()][7];

            for (int i = 0; i < messageObjectList.size(); i++) {
                MessageObject messageObject = messageObjectList.get(i);

                tableData[i][0] = messageObject.getId();

                Calendar calendar = messageObject.getDateCreated();

                tableData[i][1] = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS:%1$tL", calendar);
                tableData[i][2] = messageObject.getConnectorName();

                tableData[i][3] = messageObject.getType();
                tableData[i][4] = messageObject.getSource();
                tableData[i][5] = messageObject.getStatus();
                tableData[i][6] = messageObject.getRawDataProtocol();
            }
        } else {
            tableData = new Object[0][7];
        }

        int messageObjectListSize = 0;
        if (messageObjectList != null) {
            messageObjectListSize = messageObjectList.size();
        }
        if (currentPage == 0) {
            previousPageButton.setEnabled(false);
        } else {
            previousPageButton.setEnabled(true);
        }
        int numberOfPages = getNumberOfPages(pageSize, messageCount);
        if (messageObjectListSize < pageSize || pageSize == 0) {
            nextPageButton.setEnabled(false);
        } else if (currentPage == numberOfPages) {
            nextPageButton.setEnabled(false);
        } else {
            nextPageButton.setEnabled(true);
        }
        int startResult;
        if (messageObjectListSize == 0) {
            startResult = 0;
        } else {
            startResult = (currentPage * pageSize) + 1;
        }
        int endResult;
        if (pageSize == 0) {
            endResult = messageObjectListSize;
        } else {
            endResult = (currentPage + 1) * pageSize;
        }
        if (messageObjectListSize < pageSize) {
            endResult = endResult - (pageSize - messageObjectListSize);
        }
        if (messageCount == -1) {
            resultsLabel.setText("Results " + DisplayUtil.formatNumber(startResult) + " - " + DisplayUtil.formatNumber(endResult));
        } else {
            resultsLabel.setText("Results " + DisplayUtil.formatNumber(startResult) + " - " + DisplayUtil.formatNumber(endResult) + " of " + DisplayUtil.formatNumber(messageCount));
        }
        if (messageTable != null) {
            //lastRow = messageTable.getSelectedRow();
            RefreshTableModel model = (RefreshTableModel) messageTable.getModel();
            model.refreshDataVector(tableData);
        } else {
            messageTable = new MirthTable();
            messageTable.setModel(new RefreshTableModel(tableData, new String[]{MESSAGE_ID_COLUMN_NAME, DATE_COLUMN_NAME, CONNECTOR_COLUMN_NAME, TYPE_COLUMN_NAME, SOURCE_COLUMN_NAME, STATUS_COLUMN_NAME, PROTOCOL_COLUMN_NAME}) {

                boolean[] canEdit = new boolean[]{false, false, false, false, false, false, false};

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }

        /*if (lastRow >= 0 && lastRow < messageTable.getRowCount())
        messageTable.setRowSelectionInterval(lastRow, lastRow);
        else
        lastRow = UIConstants.ERROR_CONSTANT;*/

        // Set highlighter.
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            messageTable.setHighlighters(highlighter);
        }

        deselectRows();
    }

    /**
     * Creates the table with all of the information given after being filtered
     * by the specified 'filter'
     */
    private void makeMessageTable() {
        updateMessageTable(null);

        messageTable.setSelectionMode(0);

        messageTable.getColumnExt(MESSAGE_ID_COLUMN_NAME).setVisible(false);
        messageTable.getColumnExt(DATE_COLUMN_NAME).setMinWidth(140);
        messageTable.getColumnExt(DATE_COLUMN_NAME).setMaxWidth(140);

        messageTable.setRowHeight(UIConstants.ROW_HEIGHT);
        messageTable.setOpaque(true);
        messageTable.setRowSelectionAllowed(true);
        deselectRows();

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            messageTable.setHighlighters(highlighter);
        }

        messagePane.setViewportView(messageTable);
        jSplitPane1.setLeftComponent(messagePane);

        messageTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                MessageListSelected(evt);
            }
        });

        messageTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                checkMessageSelectionAndPopupMenu(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                checkMessageSelectionAndPopupMenu(evt);
            }

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() >= 2) {
                    int row = getSelectedMessageIndex();
                    if (row >= 0) {
                        MessageObject currentMessage = messageObjectList.get(row);
                        new EditMessageDialog(currentMessage);
                    }
                }
            }
        });

        // Key Listener trigger for DEL
        messageTable.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    parent.doRemoveMessage();
                }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });
    }

    private void getMessageTableData(MessageListHandler handler, int page) {
        if (handler != null) {
            // Do all paging information below.
            try {
                messageCount = handler.getSize();
                currentPage = handler.getCurrentPage();
                pageSize = handler.getPageSize();

                if (page == FIRST_PAGE) {
                    messageObjectList = handler.getFirstPage();
                    currentPage = handler.getCurrentPage();
                } else if (page == PREVIOUS_PAGE) {
                    if (currentPage == 0) {
                        return;
                    }
                    messageObjectList = handler.getPreviousPage();
                    currentPage = handler.getCurrentPage();
                } else if (page == NEXT_PAGE) {
                    int numberOfPages = getNumberOfPages(pageSize, messageCount);
                    if (currentPage == numberOfPages) {
                        return;
                    }
                    messageObjectList = handler.getNextPage();
                    if (messageObjectList.size() == 0) {
                        messageObjectList = handler.getPreviousPage();
                    }
                    currentPage = handler.getCurrentPage();
                }

            } catch (Throwable t) {  // catch Throwable in case the client runs out of memory
                messageObjectList = null;
                
                if (t.getMessage().contains("Java heap space")) {
                    parent.alertError(parent, "There was an out of memory error when trying to retrieve messages.\nIncrease your heap size or decrease your page size and search again.");
                } else {
                    parent.alertException(this, t.getStackTrace(), t.getMessage());
                }
            }
        }
    }

    private int getNumberOfPages(int pageSize, int messageCount) {
        int numberOfPages;
        if (messageCount == -1) {
            return -1;
        }
        if (pageSize == 0) {
            numberOfPages = 0;
        } else {
            numberOfPages = messageCount / pageSize;
            if ((messageCount != 0) && ((messageCount % pageSize) == 0)) {
                numberOfPages--;
            }
        }

        return numberOfPages;
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

            mappingsTable.setModel(new RefreshTableModel(tableData, new String[]{SCOPE_COLUMN_NAME, KEY_COLUMN_NAME, VALUE_COLUMN_NAME}) {

                boolean[] canEdit = new boolean[]{false, false, false};

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

    public void updateAttachmentsTable(MessageObject currentMessage, boolean cleared) {

        Object[][] tableData = updateAttachmentList(currentMessage);

        // Create attachment Table if it has not been created yet. 
        if (attachmentTable != null) {
            RefreshTableModel model = (RefreshTableModel) attachmentTable.getModel();
            if (tableData != null) {
                model.refreshDataVector(tableData);
            }
        } else {
            attachmentTable = new MirthTable();
            attachmentTable.setModel(new RefreshTableModel(tableData, new String[]{NUMBER_COLUMN_NAME, TYPE_COLUMN_NAME, ATTACHMENTID_COLUMN_NAME}) {

                boolean[] canEdit = new boolean[]{false, false, false};

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

    /**
     * Shows the popup menu when the trigger button (right-click) has been
     * pushed.  Deselects the rows if no row was selected.
     */
    private void checkMessageSelectionAndPopupMenu(java.awt.event.MouseEvent evt) {
        int row = messageTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
        if (row == -1) {
            deselectRows();
        }

        if (evt.isPopupTrigger()) {
            if (row != -1) {
                messageTable.setRowSelectionInterval(row, row);
            }
            parent.messagePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been
     * pushed.  Deselects the rows if no row was selected.
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
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 6, -1, false);
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 7, 7, true);
        if (messageTable != null) {
            messageTable.clearSelection();
            clearDescription();
        }
        disableFormatXmlCheckBox();
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
    public void clearDescription() {
        RawMessageTextPane.setDocument(new SyntaxDocument());
        RawMessageTextPane.setText("Select a message to view the raw message.");
        TransformedMessageTextPane.setDocument(new SyntaxDocument());
        TransformedMessageTextPane.setText("Select a message to view the transformed message.");
        EncodedMessageTextPane.setDocument(new SyntaxDocument());
        EncodedMessageTextPane.setText("Select a message to view the encoded message.");
        ErrorsTextPane.setDocument(new SyntaxDocument());
        ErrorsTextPane.setText("Select a message to view any errors.");
        messageIdField.setText("");
        correlationIdField.setText("");
        updateMappingsTable(new String[0][0], true);
        updateAttachmentsTable(null, true);
        descriptionTabbedPane.remove(attachmentsPane);
    }

    private int getSelectedMessageIndex() {
        int row = -1;
        if (messageTable.getSelectedRow() > -1) {
            row = messageTable.convertRowIndexToModel(messageTable.getSelectedRow());
        }
        return row;
    }

    /**
     * An action for when a row is selected in the table.
     */
    private void MessageListSelected(ListSelectionEvent evt) {
        if (!evt.getValueIsAdjusting()) {
            int row = getSelectedMessageIndex();

            if (row >= 0) {
                parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 6, -1, true);
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                MessageObject currentMessage = messageObjectList.get(row);

                setCorrectDocument(RawMessageTextPane, currentMessage.getRawData(), currentMessage.getRawDataProtocol());
                setCorrectDocument(TransformedMessageTextPane, currentMessage.getTransformedData(), currentMessage.getTransformedDataProtocol());
                setCorrectDocument(EncodedMessageTextPane, currentMessage.getEncodedData(), currentMessage.getEncodedDataProtocol());
                setCorrectDocument(ErrorsTextPane, currentMessage.getErrors(), null);
                messageIdField.setText(currentMessage.getId());
                
                if (currentMessage.getCorrelationId() == null) {
                    correlationIdField.setText(currentMessage.getId());
                } else {
                    correlationIdField.setText(currentMessage.getCorrelationId());
                }
                
                if (currentMessage.isAttachment()) {
                    if (descriptionTabbedPane.indexOfTab("Attachments") == -1) {
                        descriptionTabbedPane.addTab("Attachments", attachmentsPane);
                    }
                    updateAttachmentsTable(currentMessage, true);
                } else {
                    descriptionTabbedPane.remove(attachmentsPane);
                }
                Map connectorMap = currentMessage.getConnectorMap();
                Map channelMap = currentMessage.getChannelMap();
                Map responseMap = currentMessage.getResponseMap();

                String[][] tableData = new String[connectorMap.size() + channelMap.size() + responseMap.size()][3];
                int i = 0;

                Iterator connectorMapSetIterator = connectorMap.entrySet().iterator();
                for (; connectorMapSetIterator.hasNext(); i++) {
                    Entry variableMapEntry = (Entry) connectorMapSetIterator.next();
                    tableData[i][0] = "Connector";
                    tableData[i][1] = variableMapEntry.getKey().toString();
                    tableData[i][2] = variableMapEntry.getValue().toString();
                }

                Iterator channelMapSetIterator = channelMap.entrySet().iterator();
                for (; channelMapSetIterator.hasNext(); i++) {
                    Entry variableMapEntry = (Entry) channelMapSetIterator.next();
                    tableData[i][0] = "Channel";
                    tableData[i][1] = variableMapEntry.getKey().toString();
                    tableData[i][2] = variableMapEntry.getValue().toString();
                }

                Iterator responseMapSetIterator = responseMap.entrySet().iterator();
                for (; responseMapSetIterator.hasNext(); i++) {
                    Entry variableMapEntry = (Entry) responseMapSetIterator.next();
                    tableData[i][0] = "Response";
                    tableData[i][1] = variableMapEntry.getKey().toString();
                    tableData[i][2] = variableMapEntry.getValue().toString();
                }


                updateMappingsTable(tableData, false);

                if (attachmentTable == null || attachmentTable.getSelectedRow() == -1 || descriptionTabbedPane.indexOfTab("Attachments") == -1) {
                    parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 9, 9, false);
                }
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    private void setCorrectDocument(MirthSyntaxTextArea textPane, String message, MessageObject.Protocol protocol) {
        SyntaxDocument newDoc = new SyntaxDocument();

        if (message != null) {
            if (protocol != null) {
                if (protocol.equals(MessageObject.Protocol.XML) || protocol.equals(Protocol.HL7V3)) {
                    getFormatXmlCheckBox(textPane).setEnabled(true);
                } else {
                    getFormatXmlCheckBox(textPane).setEnabled(false);
                }
                
                if (protocol.equals(MessageObject.Protocol.HL7V2) || protocol.equals(MessageObject.Protocol.NCPDP) || protocol.equals(MessageObject.Protocol.DICOM)) {
                    newDoc.setTokenMarker(new HL7TokenMarker());
                } else if (protocol.equals(MessageObject.Protocol.XML) || protocol.equals(Protocol.HL7V3)) {
                    newDoc.setTokenMarker(new XMLTokenMarker());

                    if (getFormatXmlCheckBox(textPane).isSelected()) {
                        DocumentSerializer serializer = new DocumentSerializer(true);

                        try {
                            Document doc = serializer.fromXML(message);
                            message = serializer.toXML(doc);
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                } else if (protocol.equals(MessageObject.Protocol.X12)) {
                    newDoc.setTokenMarker(new X12TokenMarker());
                } else if (protocol.equals(MessageObject.Protocol.EDI)) {
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
     * Returns the ID of the selected message in the table.
     */
    public String getSelectedMessageID() {
        int column = -1;
        for (int i = 0; i < messageTable.getModel().getColumnCount(); i++) {
            if (messageTable.getModel().getColumnName(i).equals(MESSAGE_ID_COLUMN_NAME)) {
                column = i;
            }
        }
        return ((String) messageTable.getModel().getValueAt(messageTable.convertRowIndexToModel(messageTable.getSelectedRow()), column));
    }

    /**
     * Returns the current MessageObjectFilter that is set.
     */
    public MessageObjectFilter getCurrentFilter() {
        return messageObjectFilter;
    }

    private void formatXmlCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        JCheckBox formatXmlCheckBox = (JCheckBox) evt.getSource();
        boolean selected = formatXmlCheckBox.isSelected();
        
        if (!formatXmlCheckBox.equals(formatXmlRawCheckBox)) {
            formatXmlRawCheckBox.setSelected(selected);
        }
        if (!formatXmlCheckBox.equals(formatXmlTransformedCheckBox)) {
            formatXmlTransformedCheckBox.setSelected(selected);
        }
        if (!formatXmlCheckBox.equals(formatXmlEncodedCheckBox)) {
            formatXmlEncodedCheckBox.setSelected(selected);
        }
        
        int row = getSelectedMessageIndex();

        if (row >= 0) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            MessageObject currentMessage = messageObjectList.get(row);

            setCorrectDocument(RawMessageTextPane, currentMessage.getRawData(), currentMessage.getRawDataProtocol());
            setCorrectDocument(TransformedMessageTextPane, currentMessage.getTransformedData(), currentMessage.getTransformedDataProtocol());
            setCorrectDocument(EncodedMessageTextPane, currentMessage.getEncodedData(), currentMessage.getEncodedDataProtocol());
            
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    private JCheckBox getFormatXmlCheckBox(MirthSyntaxTextArea textPane) {
        if (textPane.equals(RawMessageTextPane)) {
            return formatXmlRawCheckBox;
        } else if (textPane.equals(TransformedMessageTextPane)) {
            return formatXmlTransformedCheckBox;
        } else if (textPane.equals(EncodedMessageTextPane)) {
            return formatXmlEncodedCheckBox;
        } else {
            return null;
        }
    }
    
    private void disableFormatXmlCheckBox() {
        formatXmlRawCheckBox.setEnabled(false);
        formatXmlTransformedCheckBox.setEnabled(false);
        formatXmlEncodedCheckBox.setEnabled(false);
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filterPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        resultsLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        pageSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        previousPageButton = new javax.swing.JButton();
        nextPageButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        mirthDatePicker1 = new com.mirth.connect.client.ui.components.MirthDatePicker();
        jLabel3 = new javax.swing.JLabel();
        mirthDatePicker2 = new com.mirth.connect.client.ui.components.MirthDatePicker();
        jLabel2 = new javax.swing.JLabel();
        filterButton = new javax.swing.JButton();
        advSearchButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        statusComboBox = new javax.swing.JComboBox();
        mirthTimePicker1 = new com.mirth.connect.client.ui.components.MirthTimePicker();
        mirthTimePicker2 = new com.mirth.connect.client.ui.components.MirthTimePicker();
        quickSearchField = new javax.swing.JTextField();
        quickSearchLabel = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        descriptionTabbedPane = new javax.swing.JTabbedPane();
        RawMessagePanel = new javax.swing.JPanel();
        RawMessageTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        formatXmlRawCheckBox = new javax.swing.JCheckBox();
        TransformedMessagePanel = new javax.swing.JPanel();
        TransformedMessageTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        formatXmlTransformedCheckBox = new javax.swing.JCheckBox();
        EncodedMessagePanel = new javax.swing.JPanel();
        EncodedMessageTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        formatXmlEncodedCheckBox = new javax.swing.JCheckBox();
        mappingsPane = new javax.swing.JScrollPane();
        mappingsTable = null;
        ErrorsPanel = new javax.swing.JPanel();
        ErrorsTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        metaDataPane = new javax.swing.JScrollPane();
        metaDataPanel = new javax.swing.JPanel();
        messageIdField = new javax.swing.JTextField();
        messageIdLabel1 = new javax.swing.JLabel();
        correlationIdField = new javax.swing.JTextField();
        correlationIdLabel = new javax.swing.JLabel();
        attachmentsPane = new javax.swing.JScrollPane();
        attachmentTable = null;
        messagePane = new javax.swing.JScrollPane();
        messageTable = null;

        setBackground(new java.awt.Color(255, 255, 255));

        filterPanel.setBackground(new java.awt.Color(255, 255, 255));
        filterPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1)));

        resultsLabel.setForeground(new java.awt.Color(204, 0, 0));
        resultsLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        resultsLabel.setText("Results");

        jLabel6.setText("Page Size:");

        pageSizeField.setToolTipText("After changing the page size, a new search must be performed for the changes to take effect.  The default page size can also be configured on the Settings panel.");

        previousPageButton.setText("<");
        previousPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousPageButtonActionPerformed(evt);
            }
        });

        nextPageButton.setText(">");
        nextPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextPageButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(36, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(resultsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pageSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(previousPageButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nextPageButton))))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {nextPageButton, previousPageButton});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(resultsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pageSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(previousPageButton)
                    .addComponent(nextPageButton)))
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        jLabel3.setText("Start Time:");

        jLabel2.setText("End Time:");

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

        jLabel5.setText("Status:");

        quickSearchLabel.setText("Quick Search:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel5)
                    .addComponent(jLabel3)
                    .addComponent(quickSearchLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(statusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mirthDatePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mirthDatePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mirthTimePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mirthTimePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(advSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(quickSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterButton)
                .addContainerGap(27, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {advSearchButton, mirthTimePicker1, mirthTimePicker2});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(mirthDatePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(mirthTimePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(mirthDatePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(mirthTimePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(quickSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(quickSearchLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(statusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(advSearchButton)
                    .addComponent(filterButton)))
        );

        javax.swing.GroupLayout filterPanelLayout = new javax.swing.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 60, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jSplitPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.5);

        descriptionTabbedPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        descriptionTabbedPane.setFocusable(false);

        RawMessagePanel.setBackground(new java.awt.Color(255, 255, 255));
        RawMessagePanel.setFocusable(false);

        RawMessageTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        RawMessageTextPane.setEditable(false);

        formatXmlRawCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        formatXmlRawCheckBox.setText("Format XML Messages");
        formatXmlRawCheckBox.setToolTipText("Pretty print messages that are XML.");
        formatXmlRawCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatXmlRawCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout RawMessagePanelLayout = new javax.swing.GroupLayout(RawMessagePanel);
        RawMessagePanel.setLayout(RawMessagePanelLayout);
        RawMessagePanelLayout.setHorizontalGroup(
            RawMessagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RawMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RawMessagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(formatXmlRawCheckBox)
                    .addComponent(RawMessageTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE))
                .addContainerGap())
        );
        RawMessagePanelLayout.setVerticalGroup(
            RawMessagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RawMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(RawMessageTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(formatXmlRawCheckBox)
                .addContainerGap())
        );

        descriptionTabbedPane.addTab("Raw Message", RawMessagePanel);

        TransformedMessagePanel.setBackground(new java.awt.Color(255, 255, 255));
        TransformedMessagePanel.setFocusable(false);

        TransformedMessageTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        TransformedMessageTextPane.setEditable(false);

        formatXmlTransformedCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        formatXmlTransformedCheckBox.setText("Format XML Messages");
        formatXmlTransformedCheckBox.setToolTipText("Pretty print messages that are XML.");
        formatXmlTransformedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatXmlTransformedCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout TransformedMessagePanelLayout = new javax.swing.GroupLayout(TransformedMessagePanel);
        TransformedMessagePanel.setLayout(TransformedMessagePanelLayout);
        TransformedMessagePanelLayout.setHorizontalGroup(
            TransformedMessagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TransformedMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(TransformedMessagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(formatXmlTransformedCheckBox)
                    .addComponent(TransformedMessageTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE))
                .addContainerGap())
        );
        TransformedMessagePanelLayout.setVerticalGroup(
            TransformedMessagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TransformedMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(TransformedMessageTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(formatXmlTransformedCheckBox)
                .addContainerGap())
        );

        descriptionTabbedPane.addTab("Transformed Message", TransformedMessagePanel);

        EncodedMessagePanel.setBackground(new java.awt.Color(255, 255, 255));
        EncodedMessagePanel.setFocusable(false);

        EncodedMessageTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        EncodedMessageTextPane.setEditable(false);

        formatXmlEncodedCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        formatXmlEncodedCheckBox.setText("Format XML Messages");
        formatXmlEncodedCheckBox.setToolTipText("Pretty print messages that are XML.");
        formatXmlEncodedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatXmlEncodedCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout EncodedMessagePanelLayout = new javax.swing.GroupLayout(EncodedMessagePanel);
        EncodedMessagePanel.setLayout(EncodedMessagePanelLayout);
        EncodedMessagePanelLayout.setHorizontalGroup(
            EncodedMessagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EncodedMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(EncodedMessagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(formatXmlEncodedCheckBox)
                    .addComponent(EncodedMessageTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE))
                .addContainerGap())
        );
        EncodedMessagePanelLayout.setVerticalGroup(
            EncodedMessagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EncodedMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(EncodedMessageTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(formatXmlEncodedCheckBox)
                .addContainerGap())
        );

        descriptionTabbedPane.addTab("Encoded Message", EncodedMessagePanel);

        mappingsPane.setViewportView(mappingsTable);

        descriptionTabbedPane.addTab("Mappings", mappingsPane);

        ErrorsPanel.setBackground(new java.awt.Color(255, 255, 255));
        ErrorsPanel.setFocusable(false);

        ErrorsTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ErrorsTextPane.setEditable(false);

        javax.swing.GroupLayout ErrorsPanelLayout = new javax.swing.GroupLayout(ErrorsPanel);
        ErrorsPanel.setLayout(ErrorsPanelLayout);
        ErrorsPanelLayout.setHorizontalGroup(
            ErrorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ErrorsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ErrorsTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE)
                .addContainerGap())
        );
        ErrorsPanelLayout.setVerticalGroup(
            ErrorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ErrorsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ErrorsTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
                .addContainerGap())
        );

        descriptionTabbedPane.addTab("Errors", ErrorsPanel);

        metaDataPanel.setBackground(new java.awt.Color(255, 255, 255));

        messageIdField.setEditable(false);
        messageIdField.setToolTipText("The GUID of the message in the Mirth Connect database.");

        messageIdLabel1.setText("Message ID:");

        correlationIdField.setEditable(false);
        correlationIdField.setToolTipText("The correlation GUID of the group of messages in the Mirth Connect database");

        correlationIdLabel.setText("Correlation ID:");

        javax.swing.GroupLayout metaDataPanelLayout = new javax.swing.GroupLayout(metaDataPanel);
        metaDataPanel.setLayout(metaDataPanelLayout);
        metaDataPanelLayout.setHorizontalGroup(
            metaDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(metaDataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(metaDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(messageIdLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(correlationIdLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(metaDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(correlationIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messageIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(461, Short.MAX_VALUE))
        );
        metaDataPanelLayout.setVerticalGroup(
            metaDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(metaDataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(metaDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(messageIdLabel1)
                    .addComponent(messageIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(metaDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(correlationIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(correlationIdLabel))
                .addContainerGap(113, Short.MAX_VALUE))
        );

        metaDataPane.setViewportView(metaDataPanel);

        descriptionTabbedPane.addTab("Meta Data", metaDataPane);

        attachmentsPane.setViewportView(attachmentTable);

        descriptionTabbedPane.addTab("Attachments", attachmentsPane);

        jSplitPane1.setRightComponent(descriptionTabbedPane);

        messagePane.setViewportView(messageTable);

        jSplitPane1.setLeftComponent(messagePane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(filterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 742, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

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

                parent.setWorking("Loading " + attachType + " viewer...", true);

                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                    public Void doInBackground() {
                        attachmentViewer.viewAttachments(finalAttachmentIds);
                        return null;
                    }

                    public void done() {
                        parent.setWorking("", false);
                    }
                };
                worker.execute();
            } else {
                parent.alertInformation(this, "No Attachment Viewer plugin installed for type: " + attachType);
            }
        } catch (Exception e) {
        }

    }

    private void advSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advSearchButtonActionPerformed

        // display the advanced search filter pop up window.
        String connector = advSearchFilterPopup.getConnector();
        String messageSource = advSearchFilterPopup.getMessageSource();
        String messageType = advSearchFilterPopup.getMessageType();
        String containingKeyword = advSearchFilterPopup.getContainingKeyword();
        String messageId = advSearchFilterPopup.getMessageId();
        String correlationId = advSearchFilterPopup.getCorrelationId();
        boolean includeRawMessage = advSearchFilterPopup.isIncludeRawMessage();
        boolean includeTransformedMessage = advSearchFilterPopup.isIncludeTransformedMessage();
        boolean includeEncodedMessage = advSearchFilterPopup.isIncludeEncodedMessage();
        boolean includeErrors = advSearchFilterPopup.isIncludeErrors();
        String protocol = advSearchFilterPopup.getProtocol();

        Channel selectedChannel = parent.getSelectedChannelFromDashboard();
        
        // Channel could be null if user doesn't have permission to get channels, so default to true.
        boolean allowSearch = true;
        if (selectedChannel != null) {
            allowSearch = !((String) selectedChannel.getProperties().get("encryptData")).equalsIgnoreCase("true");
        }
        
        advSearchFilterPopup = new MessageBrowserAdvancedFilter(parent, "Advanced Search Filter", true, allowSearch);
        advSearchFilterPopup.setFieldValues(connector, messageSource, messageType, containingKeyword, messageId, correlationId, includeRawMessage, includeTransformedMessage, includeEncodedMessage, includeErrors, protocol);

        advSearchFilterPopup.setVisible(true);

    }//GEN-LAST:event_advSearchButtonActionPerformed

    private void formatXmlRawCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatXmlRawCheckBoxActionPerformed
        formatXmlCheckBoxActionPerformed(evt);
    }//GEN-LAST:event_formatXmlRawCheckBoxActionPerformed

    private void formatXmlTransformedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatXmlTransformedCheckBoxActionPerformed
        formatXmlCheckBoxActionPerformed(evt);
    }//GEN-LAST:event_formatXmlTransformedCheckBoxActionPerformed

    private void formatXmlEncodedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatXmlEncodedCheckBoxActionPerformed
        formatXmlCheckBoxActionPerformed(evt);
    }//GEN-LAST:event_formatXmlEncodedCheckBoxActionPerformed

    private void nextPageButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_nextPageButtonActionPerformed
        parent.setWorking("Loading next page...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                getMessageTableData(messageListHandler, NEXT_PAGE);
                return null;
            }

            public void done() {
                if (messageListHandler != null) {
                    updateMessageTable(messageObjectList);
                } else {
                    updateMessageTable(null);
                }
                parent.setWorking("", false);
            }
        };
        worker.execute();

    }// GEN-LAST:event_nextPageButtonActionPerformed

    private void previousPageButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_previousPageButtonActionPerformed
        parent.setWorking("Loading previous page...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                getMessageTableData(messageListHandler, PREVIOUS_PAGE);
                return null;
            }

            public void done() {
                if (messageListHandler != null) {
                    updateMessageTable(messageObjectList);
                } else {
                    updateMessageTable(null);
                }
                parent.setWorking("", false);
            }
        };
        worker.execute();
    }// GEN-LAST:event_previousPageButtonActionPerformed

    /**
     * An action when the filter button is pressed. Creates the actual filter
     * and remakes the table with that filter.
     */
    private void filterButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_filterButtonActionPerformed      
        messageObjectFilter = new MessageObjectFilter();

        if (mirthDatePicker1.getDate() != null && mirthDatePicker2.getDate() != null
                && mirthTimePicker1.getDate() != null && mirthTimePicker2.getDate() != null) {
            SimpleDateFormat timeDateFormat = new SimpleDateFormat("hh:mm aa");
            DateFormatter timeFormatter = new DateFormatter(timeDateFormat);

            Date startDate = mirthDatePicker1.getDate();
            Date endDate = mirthDatePicker2.getDate();

            String startTime = mirthTimePicker1.getDate();
            String endTime = mirthTimePicker2.getDate();

            Date startTimeDate;
            Date endTimeDate;

            try {
                startTimeDate = (Date) timeFormatter.stringToValue(startTime);
                endTimeDate = (Date) timeFormatter.stringToValue(endTime);
            } catch (Exception e) {
                parent.alertError(parent, "Invalid date.");
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
                parent.alertError(parent, "Start date cannot be after the end date.");
                return;
            }

            messageObjectFilter.setStartDate(startCalendar);
            messageObjectFilter.setEndDate(endCalendar);

        }

        messageObjectFilter.setChannelId(parent.getSelectedChannelIdFromDashboard());

        if (!((String) statusComboBox.getSelectedItem()).equalsIgnoreCase(UIConstants.ALL_OPTION)) {
            for (int i = 0; i < MessageObject.Status.values().length; i++) {
                if (((String) statusComboBox.getSelectedItem()).equalsIgnoreCase(MessageObject.Status.values()[i].toString())) {
                    messageObjectFilter.setStatus(MessageObject.Status.values()[i]);
                }
            }
        }

        // Get the advanced search criteria.        
        if (!advSearchFilterPopup.getConnector().equals("")) {
            messageObjectFilter.setConnectorName(advSearchFilterPopup.getConnector());
        }
        if (!advSearchFilterPopup.getMessageSource().equals("")) {
            messageObjectFilter.setSource(advSearchFilterPopup.getMessageSource());
        }
        if (!advSearchFilterPopup.getMessageType().equals("")) {
            messageObjectFilter.setType(advSearchFilterPopup.getMessageType());
        }
        if (!advSearchFilterPopup.getContainingKeyword().equals("")) {
            messageObjectFilter.setSearchCriteria(advSearchFilterPopup.getContainingKeyword());
        }
        if (!advSearchFilterPopup.getMessageId().equals("")) {
            messageObjectFilter.setId(advSearchFilterPopup.getMessageId());
        }
        if (!advSearchFilterPopup.getCorrelationId().equals("")) {
            messageObjectFilter.setCorrelationId(advSearchFilterPopup.getCorrelationId());
        }
        if (advSearchFilterPopup.isIncludeRawMessage()) {
            messageObjectFilter.setSearchRawData(true);
        }
        if (advSearchFilterPopup.isIncludeTransformedMessage()) {
            messageObjectFilter.setSearchTransformedData(true);
        }
        if (advSearchFilterPopup.isIncludeEncodedMessage()) {
            messageObjectFilter.setSearchEncodedData(true);
        }
        if (advSearchFilterPopup.isIncludeErrors()) {
            messageObjectFilter.setSearchErrors(true);
        }

        if (!quickSearchField.getText().equals("")) {
            messageObjectFilter.setQuickSearch(quickSearchField.getText());
        }

        if (advSearchFilterPopup.getProtocol().equalsIgnoreCase(UIConstants.ALL_OPTION)) {
            // clear the protocol search criteria.
            messageObjectFilter.setProtocol(null);
        } else {
            for (int i = 0; i < MessageObject.Protocol.values().length; i++) {
                if (advSearchFilterPopup.getProtocol().equalsIgnoreCase(MessageObject.Protocol.values()[i].toString())) {
                    messageObjectFilter.setProtocol(MessageObject.Protocol.values()[i]);
                }
            }
        }

        if (!pageSizeField.getText().equals("")) {
            pageSize = Integer.parseInt(pageSizeField.getText());
        }
        parent.setWorking("Loading messages...", true);

        if (messageListHandler == null) {
            updateMessageTable(null);
        }
        class MessageWorker extends SwingWorker<Void, Void> {

            public Void doInBackground() {
                try {
                    messageListHandler = parent.mirthClient.getMessageListHandler(messageObjectFilter, pageSize, false);
                } catch (ClientException e) {
                    parent.alertException(parent, e.getStackTrace(), e.getMessage());
                }
                getMessageTableData(messageListHandler, FIRST_PAGE);
                return null;
            }

            public void done() {
                if (messageListHandler != null) {
                    updateMessageTable(messageObjectList);
                } else {
                    updateMessageTable(null);
                }
                parent.setWorking("", false);
            }
        }
        ;
        MessageWorker worker = new MessageWorker();
        worker.execute();
    }// GEN-LAST:event_filterButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel EncodedMessagePanel;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea EncodedMessageTextPane;
    private javax.swing.JPanel ErrorsPanel;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea ErrorsTextPane;
    private javax.swing.JPanel RawMessagePanel;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea RawMessageTextPane;
    private javax.swing.JPanel TransformedMessagePanel;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea TransformedMessageTextPane;
    private javax.swing.JButton advSearchButton;
    private com.mirth.connect.client.ui.components.MirthTable attachmentTable;
    private javax.swing.JScrollPane attachmentsPane;
    private javax.swing.JTextField correlationIdField;
    private javax.swing.JLabel correlationIdLabel;
    private javax.swing.JTabbedPane descriptionTabbedPane;
    private javax.swing.JButton filterButton;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JCheckBox formatXmlEncodedCheckBox;
    private javax.swing.JCheckBox formatXmlRawCheckBox;
    private javax.swing.JCheckBox formatXmlTransformedCheckBox;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JScrollPane mappingsPane;
    private com.mirth.connect.client.ui.components.MirthTable mappingsTable;
    private javax.swing.JTextField messageIdField;
    private javax.swing.JLabel messageIdLabel1;
    private javax.swing.JScrollPane messagePane;
    private com.mirth.connect.client.ui.components.MirthTable messageTable;
    private javax.swing.JScrollPane metaDataPane;
    private javax.swing.JPanel metaDataPanel;
    private com.mirth.connect.client.ui.components.MirthDatePicker mirthDatePicker1;
    private com.mirth.connect.client.ui.components.MirthDatePicker mirthDatePicker2;
    private com.mirth.connect.client.ui.components.MirthTimePicker mirthTimePicker1;
    private com.mirth.connect.client.ui.components.MirthTimePicker mirthTimePicker2;
    private javax.swing.JButton nextPageButton;
    private com.mirth.connect.client.ui.components.MirthTextField pageSizeField;
    private javax.swing.JButton previousPageButton;
    private javax.swing.JTextField quickSearchField;
    private javax.swing.JLabel quickSearchLabel;
    private javax.swing.JLabel resultsLabel;
    private javax.swing.JComboBox statusComboBox;
    // End of variables declaration//GEN-END:variables
}

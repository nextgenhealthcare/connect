/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.client.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.client.ui.components.MirthVariableList;
import com.webreach.mirth.model.Alert;
import com.webreach.mirth.model.Channel;

/** The channel editor panel. Majority of the client application */
public class AlertPanel extends javax.swing.JPanel
{
    private Frame parent;
    private boolean isDeleting = false;
    private int lastAlertRow, lastEmailRow;
    private final String ALERT_NAME_COLUMN_NAME = "Name";
    private final String ALERT_STATUS_COLUMN_NAME = "Status";
    private final String APPLY_CHANNEL_NAME_COLUMN_NAME = "Channel Name";
    private final String APPLY_STATUS_COLUMN_NAME = "Applied";
    private final String APPLY_CHANNEL_ID_COLUMN_NAME = "Channel ID";
    private final String EMAIL_COLUMN_NAME = "Email Address";
    private final String ENABLED_TEXT = "Enabled";
    private final String DISABLED_TEXT = "Disabled";
    private final JPanel blankPanel;

    /**
     * Creates the Channel Editor panel. Calls initComponents() and sets up the
     * model, dropdowns, and mouse listeners.
     */
    public AlertPanel()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        lastAlertRow = -1;
        lastEmailRow = -1;
        blankPanel = new JPanel();
        initComponents();
        makeAlertTable();
        makeApplyToChannelsTable();
        makeEmailsTable();
        setAlertErrorList();
    }

    /**
     * Makes the alert table with a parameter that is true if a new alert should
     * be added as well.
     */
    public void makeAlertTable()
    {
        updateAlertTable(false);

        // Set the custom cell editor for the Alert Name column.
        alertTable.getColumnModel().getColumn(alertTable.getColumnNumber(ALERT_NAME_COLUMN_NAME)).setCellEditor(new AlertTableCellEditor());
        alertTable.getColumnExt(ALERT_STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());

        alertTable.setSelectionMode(0);
        alertTable.setRowSelectionAllowed(true);
        alertTable.setRowHeight(UIConstants.ROW_HEIGHT);
        alertTable.setFocusable(false);
        alertTable.setSortable(false);
        alertTable.setOpaque(true);
        alertTable.setDragEnabled(false);

        alertTable.getColumnExt(ALERT_STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        alertTable.getColumnExt(ALERT_STATUS_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);

        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            alertTable.setHighlighters(highlighter);
        }

        // This action is called when a new selection is made on the alert
        // table.
        alertTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                if (!evt.getValueIsAdjusting())
                {
                    if (lastAlertRow != -1 && lastAlertRow != alertTable.getSelectedRow() && lastAlertRow < alertTable.getRowCount() && !isDeleting)
                    {
                        saveAlert();
                    }

                    if (!loadAlert())
                    {
                        int rowCount = alertTable.getRowCount();
                        if (rowCount > 0 && lastAlertRow == rowCount)
                            alertTable.setRowSelectionInterval(lastAlertRow - 1, lastAlertRow - 1);
                        else if (lastAlertRow != -1 && lastAlertRow < rowCount)
                            alertTable.setRowSelectionInterval(lastAlertRow, lastAlertRow);
                        
                        lastAlertRow = alertTable.getSelectedRow();
                    }
                    else
                    {
                        lastAlertRow = alertTable.getSelectedRow();
                    }

                    checkVisibleAlertTasks();
                }
            }
        });

        // Mouse listener for trigger-button popup on the table.
        alertTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showAlertPopupMenu(evt, true);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showAlertPopupMenu(evt, true);
            }
        });

        alertPane.setViewportView(alertTable);

        // Mouse listener for trigger-button popup on the table pane (not actual
        // table).
        alertPane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showAlertPopupMenu(evt, false);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showAlertPopupMenu(evt, false);
            }
        });
        // Key Listener trigger for CTRL-S and DEL
        alertTable.addKeyListener(new KeyListener()
        {
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown())
                {
                    PlatformUI.MIRTH_FRAME.doSaveAlerts();
                }
                else if (e.getKeyCode() == KeyEvent.VK_DELETE)
                {
                	parent.doDeleteAlert();
                }
            }

            public void keyReleased(KeyEvent e)
            {
            }

            public void keyTyped(KeyEvent e)
            {
            }
        });
    }

    public void updateAlertTable(boolean addNew)
    {
        Object[][] tableData = null;
        int tableSize = 0;

        if (parent.alerts != null)
        {
            tableSize = parent.alerts.size();

            if (addNew)
                tableSize++;

            tableData = new Object[tableSize][2];
            for (int i = 0; i < tableSize; i++)
            {
                if (tableSize - 1 == i && addNew)
                {
                    Alert alert = new Alert();

                    try
                    {
                        alert.setId(parent.mirthClient.getGuid());
                    }
                    catch (ClientException e)
                    {
                    	parent.alertException(this, e.getStackTrace(), e.getMessage());
                    }

                    alert.setName(getNewAlertName(tableSize));
                    alert.setEnabled(false);
                    tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_black.png")), DISABLED_TEXT);
                    tableData[i][1] = alert.getName();
                    parent.alerts.add(alert);
                }
                else
                {
                    Alert alert = parent.alerts.get(i);
                    if (alert.isEnabled())
                        tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_blue.png")), ENABLED_TEXT);
                    else
                        tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_black.png")), DISABLED_TEXT);
                    tableData[i][1] = alert.getName();
                }
            }
        }

        int row = UIConstants.ERROR_CONSTANT;

        if (alertTable != null)
        {
            RefreshTableModel model = (RefreshTableModel) alertTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            alertTable = new MirthTable();
            alertTable.setModel(new RefreshTableModel(tableData, new String[] { ALERT_STATUS_COLUMN_NAME, ALERT_NAME_COLUMN_NAME })
            {
                boolean[] canEdit = new boolean[] { false, true };

                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
        }
        if (addNew)
        {
            alertTable.setRowSelectionInterval(alertTable.getRowCount() - 1, alertTable.getRowCount() - 1);
        }
        
        row = alertTable.getSelectedRow();
        lastAlertRow = row;
    }

    public void setDefaultAlert()
    {
        lastAlertRow = -1;
        lastEmailRow = -1;

        if (parent.alerts.size() > 0)
        {
            alertTable.setRowSelectionInterval(0, 0);
            lastAlertRow = 0;
        }
        else
        {
            deselectAlertRows();
        }
    }

    /**
     * Checks to see what tasks should be available in the alert pane
     */
    public void checkVisibleAlertTasks()
    {
        int selected = getAlertIndex();

        if (selected == UIConstants.ERROR_CONSTANT)
        {
            parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 5, 6, false);
        }
        else
        {
            parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 5, 5, true);

            if (parent.alerts.get(selected).isEnabled())
            {
                parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 6, 6, false);
                parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 7, 7, true);
            }
            else
            {
                parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 6, 6, true);
                parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 7, 7, false);
            }
        }
    }

    /**
     * Makes the alert table with a parameter that is true if a new alert should
     * be added as well.
     */
    public void makeApplyToChannelsTable()
    {
        updateApplyToChannelsTable(null);

        applyToChannelsTable.setDragEnabled(false);
        applyToChannelsTable.setRowSelectionAllowed(false);
        applyToChannelsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        applyToChannelsTable.setFocusable(false);
        applyToChannelsTable.setOpaque(true);
        applyToChannelsTable.getTableHeader().setReorderingAllowed(false);
        applyToChannelsTable.setSortable(false);

        applyToChannelsTable.getColumnExt(APPLY_STATUS_COLUMN_NAME).setMaxWidth(50);
        applyToChannelsTable.getColumnExt(APPLY_STATUS_COLUMN_NAME).setMinWidth(50);

        applyToChannelsTable.getColumnExt(APPLY_CHANNEL_ID_COLUMN_NAME).setVisible(false);

        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            applyToChannelsTable.setHighlighters(highlighter);
        }

        applyToChannelsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (applyToChannelsTable.getSelectedColumn() == 1)
                {
                    parent.enableSave();
                }
            }
        });

        // Mouse listener for trigger-button popup on the table.
        applyToChannelsTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showAlertPopupMenu(evt, true);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showAlertPopupMenu(evt, true);
            }
        });

        applyToChannelsScrollPane.setViewportView(applyToChannelsTable);

        // Key Listener trigger for CTRL-S
        applyToChannelsTable.addKeyListener(new KeyListener()
        {
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown())
                {
                    PlatformUI.MIRTH_FRAME.doSaveAlerts();
                }
            }

            public void keyReleased(KeyEvent e)
            {
            }

            public void keyTyped(KeyEvent e)
            {
            }
        });
    }

    public void updateApplyToChannelsTable(Alert alert)
    {
        Object[][] tableData = null;
        int tableSize = 0;

        if (alert != null && parent.alerts != null)
        {
            tableSize = parent.channels.size();
            tableData = new Object[tableSize][3];

            int i = 0;
            for (Channel channel : parent.channels.values())
            {
                tableData[i][0] = channel.getName();
                if (alert.getChannels() != null && alert.getChannels().contains(channel.getId()))
                {
                    tableData[i][1] = Boolean.TRUE;
                }
                else
                {
                    tableData[i][1] = Boolean.FALSE;
                }
                tableData[i][2] = channel.getId();
                i++;
            }
        }

        if (alert != null && applyToChannelsTable != null)
        {
            RefreshTableModel model = (RefreshTableModel) applyToChannelsTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            applyToChannelsTable = new MirthTable();
            applyToChannelsTable.setModel(new RefreshTableModel(tableData, new String[] { APPLY_CHANNEL_NAME_COLUMN_NAME, APPLY_STATUS_COLUMN_NAME, APPLY_CHANNEL_ID_COLUMN_NAME })
            {
                boolean[] canEdit = new boolean[] { false, true, false };

                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
        }
    }

    public void setAlertErrorList()
    {
        ArrayList<String> variables = new ArrayList<String>();
        variables.add("Any");
        variables.add("Server");
        variables.add("Client");
        variables.add("200: Filter");
        variables.add("300: Transformer");
        variables.add("301: Transformer conversion");
        variables.add("302: Custom transformer");
        variables.add("400: Connector");
        variables.add("401: Document connector");
        variables.add("402: SMTP connector");
        variables.add("403: File connector");
        variables.add("404: HTTP connector");
        variables.add("405: FTP connector");
        variables.add("406: JDBC Connector");
        variables.add("407: JMS Connector");
        variables.add("408: MLLP Connector");
        variables.add("409: SFTP Connector");
        variables.add("410: SOAP Connector");
        variables.add("411: TCP Connector");
        variables.add("412: VM Connector");
        variables.add("413: Email Connector");
            	
        errorList.removeAll();
        errorList.setListData(variables.toArray());
        errorScrollPane.setViewportView(errorList);
    }

    /** Loads a selected connector and returns true on success. */
    public boolean loadAlert()
    {
        int index = getAlertIndex();

        if (index == UIConstants.ERROR_CONSTANT)
            return false;

        boolean changed = parent.alertTasks.getContentPane().getComponent(1).isVisible();

        Alert current = parent.alerts.get(index);
        updateApplyToChannelsTable(current);
        updateEmailsTable(current);
        errorField.setText(current.getExpression());
        template.setText(current.getTemplate());

        int dividerLocation = split.getDividerLocation();
        split.setRightComponent(bottomPane);
        split.setDividerLocation(dividerLocation);

        parent.alertTasks.getContentPane().getComponent(1).setVisible(changed);

        return true;
    }

    public boolean saveAlert()
    {
        if (lastAlertRow == UIConstants.ERROR_CONSTANT)
            return false;
        
        int index = getAlertIndex(lastAlertRow);
        
        boolean changed = parent.alertTasks.getContentPane().getComponent(1).isVisible();

        Alert current = parent.alerts.get(index);
        current.setChannels(getChannels());
        current.setExpression(errorField.getText());

        stopAlertEditing();
        stopEmailEditing();

        current.setEmails(getEmails());
        current.setTemplate(template.getText());

        parent.alertTasks.getContentPane().getComponent(1).setVisible(changed);

        return true;
    }

    public List<String> getChannels()
    {
        ArrayList<String> channelList = new ArrayList<String>();

        for (int i = 0; i < applyToChannelsTable.getModel().getRowCount(); i++)
        {
            if (((Boolean) applyToChannelsTable.getModel().getValueAt(i, 1)).booleanValue())
            {
                channelList.add((String) applyToChannelsTable.getModel().getValueAt(i, 2));
            }
        }
        return channelList;
    }

    /** Get the currently selected alert table index */
    public int getSelectedAlertIndex()
    {
        if (alertTable.isEditing())
            return alertTable.getEditingRow();
        else
            return alertTable.getSelectedRow();
    }

    /** Get a alert connector index by passing in its name */
    private int getAlertIndex(int selectedRow)
    {
        String alertName = (String) alertTable.getValueAt(selectedRow, alertTable.getColumnNumber(ALERT_NAME_COLUMN_NAME));
        for (int i = 0; i < parent.alerts.size(); i++)
        {
            if (parent.alerts.get(i).getName().equalsIgnoreCase(alertName))
                return i;
        }
        return UIConstants.ERROR_CONSTANT;
    }

    /** Get the index of the currently selected alert. */
    public int getAlertIndex()
    {
        if (alertTable.getSelectedRow() >= 0 && alertTable.getSelectedRow() < alertTable.getRowCount())
            return getAlertIndex(alertTable.getSelectedRow());
        return UIConstants.ERROR_CONSTANT;
    }

    public void setSelectedAlertIndex(int index)
    {
        if (index == UIConstants.ERROR_CONSTANT)
            deselectAlertRows();
        else
            alertTable.setRowSelectionInterval(index, index);
    }

    /**
     * Get the name that should be used for a new alert so that it is unique.
     */
    private String getNewAlertName(int size)
    {
        String temp = "Alert ";

        for (int i = 1; i <= size; i++)
        {
            boolean exists = false;
            for (int j = 0; j < size - 1; j++)
            {
                if (((String) alertTable.getValueAt(j, alertTable.getColumnNumber(ALERT_NAME_COLUMN_NAME))).equalsIgnoreCase(temp + i))
                    exists = true;
            }
            if (!exists)
                return temp + i;
        }
        return "";
    }

    /**
     * Shows the trigger-button popup menu. If the trigger was pressed on a row
     * of the alert table, that row should be selected as well.
     */
    private void showAlertPopupMenu(java.awt.event.MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = alertTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                alertTable.setRowSelectionInterval(row, row);
            }
            parent.alertPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    /** Adds a new alert. */
    public void addAlert()
    {
        stopAlertEditing();
        updateAlertTable(true);
        alertPane.getViewport().setViewPosition(new Point(0, alertTable.getRowHeight() * alertTable.getRowCount()));
        parent.enableSave();
    }

    public void enableAlert()
    {
        stopAlertEditing();
        parent.alerts.get(getAlertIndex()).setEnabled(true);
        updateAlertTable(false);
        parent.enableSave();
    }

    public void disableAlert()
    {
        stopAlertEditing();
        parent.alerts.get(getAlertIndex()).setEnabled(false);
        updateAlertTable(false);
        parent.enableSave();
    }

    public void deleteAlert()
    {
        if (!parent.alertOption(this, "Are you sure you want to delete this alert?"))
            return;
        isDeleting = true;

        stopAlertEditing();
        parent.alerts.remove(getAlertIndex());
        updateAlertTable(false);
        parent.enableSave();
        isDeleting = false;

        if (parent.alerts.size() == 0)
            resetBlankPane();
    }

    public void makeEmailsTable()
    {
        updateEmailsTable(null);

        emailsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                if (emailsTable.getSelectedRow() != -1)
                {
                    lastEmailRow = emailsTable.getSelectedRow();
                    removeButton.setEnabled(true);
                }
                else
                    removeButton.setEnabled(false);
            }
        });

        class EmailsTableCellEditor extends AbstractCellEditor implements TableCellEditor
        {
            JComponent component = new JTextField();

            Object originalValue;

            public EmailsTableCellEditor()
            {
                super();
            }

            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
            {
                // 'value' is value contained in the cell located at (rowIndex,
                // vColIndex)
                originalValue = value;

                // Configure the component with the specified value
                ((JTextField) component).setText((String) value);

                // Return the configured component
                return component;
            }

            public Object getCellEditorValue()
            {
                return ((JTextField) component).getText();
            }

            public boolean stopCellEditing()
            {
                String s = (String) getCellEditorValue();
                parent.enableSave();
                removeButton.setEnabled(true);
                return super.stopCellEditing();
            }

            /**
             * Enables the editor only for double-clicks.
             */
            public boolean isCellEditable(EventObject evt)
            {
                if (evt instanceof MouseEvent && ((MouseEvent) evt).getClickCount() >= 2)
                {
                    return true;
                }
                return false;
            }
        };

        // Set the custom cell editor for the Destination Name column.
        emailsTable.getColumnModel().getColumn(emailsTable.getColumnModel().getColumnIndex(EMAIL_COLUMN_NAME)).setCellEditor(new EmailsTableCellEditor());

        emailsTable.setSelectionMode(0);
        emailsTable.setRowSelectionAllowed(true);
        emailsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        emailsTable.setDragEnabled(false);
        emailsTable.setOpaque(true);
        emailsTable.setSortable(false);
        emailsTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            emailsTable.setHighlighters(highlighter);
        }

        emailsScrollPane.setViewportView(emailsTable);
    }

    public void updateEmailsTable(Alert alert)
    {
        Object[][] tableData = null;

        if (alert != null)
        {
            tableData = new Object[alert.getEmails().size()][1];

            for (int i = 0; i < alert.getEmails().size(); i++)
            {
                tableData[i][0] = alert.getEmails().get(i);
            }
        }

        if (alert != null && emailsTable != null)
        {
            RefreshTableModel model = (RefreshTableModel) emailsTable.getModel();
            model.refreshDataVector(tableData);
            if (alert.getEmails().size() == 0)
            {
                deselectEmailRows();
            }
        }
        else
        {
            emailsTable = new MirthTable();
            emailsTable.setModel(new RefreshTableModel(tableData, new String[] { EMAIL_COLUMN_NAME })
            {
                boolean[] canEdit = new boolean[] { true };

                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
        }
    }

    public List<String> getEmails()
    {
        ArrayList<String> emails = new ArrayList<String>();

        for (int i = 0; i < emailsTable.getRowCount(); i++)
            if (((String) emailsTable.getModel().getValueAt(i, 0)).length() > 0)
                emails.add((String) emailsTable.getModel().getValueAt(i, 0));

        return emails;
    }

    /** Clears the selection in the table */
    public void deselectEmailRows()
    {
        emailsTable.clearSelection();
        removeButton.setEnabled(false);
    }

    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectAlertRows()
    {
        alertTable.clearSelection();
        parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 5, 7, false);
        resetBlankPane();
    }

    public void resetBlankPane()
    {
        int dividerLocation = split.getDividerLocation();
        split.setRightComponent(blankPanel);
        split.setDividerLocation(dividerLocation);
    }

    public void stopAlertEditing()
    {
        if (alertTable.isEditing())
            alertTable.getColumnModel().getColumn(alertTable.getColumnModel().getColumnIndex(ALERT_NAME_COLUMN_NAME)).getCellEditor().stopCellEditing();
    }

    public void stopEmailEditing()
    {
        if (emailsTable.isEditing())
            emailsTable.getColumnModel().getColumn(emailsTable.getColumnModel().getColumnIndex(EMAIL_COLUMN_NAME)).getCellEditor().stopCellEditing();
    }

    /** Get the currently selected destination index */
    public int getSelectedEmailRow()
    {
        if (emailsTable.isEditing())
            return emailsTable.getEditingRow();
        else
            return emailsTable.getSelectedRow();
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        split = new javax.swing.JSplitPane();
        alertPane = new javax.swing.JScrollPane();
        alertTable = null;
        bottomPane = new javax.swing.JPanel();
        applyToChannelsPanel = new javax.swing.JPanel();
        applyToChannelsScrollPane = new javax.swing.JScrollPane();
        applyToChannelsTable = null;
        errorPane = new javax.swing.JPanel();
        errorField = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();
        emailsPane = new javax.swing.JPanel();
        emailsScrollPane = new javax.swing.JScrollPane();
        emailsTable = null;
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        templatePane = new javax.swing.JPanel();
        template = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();
        errorPanel = new javax.swing.JPanel();
        errorScrollPane = new javax.swing.JScrollPane();
        errorList = new MirthVariableList("", "");

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        split.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        split.setDividerLocation(125);
        split.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        alertPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        alertPane.setViewportView(alertTable);

        split.setLeftComponent(alertPane);

        bottomPane.setBackground(new java.awt.Color(255, 255, 255));
        applyToChannelsPanel.setBackground(new java.awt.Color(255, 255, 255));
        applyToChannelsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Apply to Channels", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        applyToChannelsScrollPane.setViewportView(applyToChannelsTable);

        org.jdesktop.layout.GroupLayout applyToChannelsPanelLayout = new org.jdesktop.layout.GroupLayout(applyToChannelsPanel);
        applyToChannelsPanel.setLayout(applyToChannelsPanelLayout);
        applyToChannelsPanelLayout.setHorizontalGroup(applyToChannelsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, applyToChannelsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE));
        applyToChannelsPanelLayout.setVerticalGroup(applyToChannelsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, applyToChannelsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE));

        errorPane.setBackground(new java.awt.Color(255, 255, 255));
        errorPane.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Error Matching Field", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        errorField.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.jdesktop.layout.GroupLayout errorPaneLayout = new org.jdesktop.layout.GroupLayout(errorPane);
        errorPane.setLayout(errorPaneLayout);
        errorPaneLayout.setHorizontalGroup(errorPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(errorField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE));
        errorPaneLayout.setVerticalGroup(errorPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(errorField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE));

        emailsPane.setBackground(new java.awt.Color(255, 255, 255));
        emailsPane.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Emails to Receive Alerts", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        emailsScrollPane.setViewportView(emailsTable);

        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addButtonActionPerformed(evt);
            }
        });

        removeButton.setText("Remove");
        removeButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                removeButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout emailsPaneLayout = new org.jdesktop.layout.GroupLayout(emailsPane);
        emailsPane.setLayout(emailsPaneLayout);
        emailsPaneLayout.setHorizontalGroup(emailsPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, emailsPaneLayout.createSequentialGroup().add(emailsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(emailsPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false).add(addButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(removeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))));
        emailsPaneLayout.setVerticalGroup(emailsPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(emailsPaneLayout.createSequentialGroup().add(addButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(removeButton).addContainerGap(78, Short.MAX_VALUE)).add(emailsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE));

        templatePane.setBackground(new java.awt.Color(255, 255, 255));
        templatePane.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Error Template", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        template.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.jdesktop.layout.GroupLayout templatePaneLayout = new org.jdesktop.layout.GroupLayout(templatePane);
        templatePane.setLayout(templatePaneLayout);
        templatePaneLayout.setHorizontalGroup(templatePaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(template, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE));
        templatePaneLayout.setVerticalGroup(templatePaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(template, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE));

        errorPanel.setBackground(new java.awt.Color(255, 255, 255));
        errorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Error Codes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        errorList.setModel(new javax.swing.AbstractListModel()
        {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };

            public int getSize()
            {
                return strings.length;
            }

            public Object getElementAt(int i)
            {
                return strings[i];
            }
        });
        errorScrollPane.setViewportView(errorList);

        org.jdesktop.layout.GroupLayout errorPanelLayout = new org.jdesktop.layout.GroupLayout(errorPanel);
        errorPanel.setLayout(errorPanelLayout);
        errorPanelLayout.setHorizontalGroup(errorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(errorScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE));
        errorPanelLayout.setVerticalGroup(errorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(errorScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE));

        org.jdesktop.layout.GroupLayout bottomPaneLayout = new org.jdesktop.layout.GroupLayout(bottomPane);
        bottomPane.setLayout(bottomPaneLayout);
        bottomPaneLayout.setHorizontalGroup(bottomPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(bottomPaneLayout.createSequentialGroup().addContainerGap().add(bottomPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(org.jdesktop.layout.GroupLayout.LEADING, templatePane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(bottomPaneLayout.createSequentialGroup().add(errorPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(errorPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(bottomPaneLayout.createSequentialGroup().add(applyToChannelsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(emailsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))).addContainerGap()));
        bottomPaneLayout.setVerticalGroup(bottomPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(bottomPaneLayout.createSequentialGroup().addContainerGap().add(bottomPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(applyToChannelsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(emailsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(bottomPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(errorPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(errorPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(templatePane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addContainerGap()));
        split.setRightComponent(bottomPane);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(split, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(split, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE));
    }// </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_removeButtonActionPerformed
    {// GEN-HEADEREND:event_removeButtonActionPerformed
        stopEmailEditing();
        if (getSelectedEmailRow() != -1 && !emailsTable.isEditing())
        {
            ((DefaultTableModel) emailsTable.getModel()).removeRow(getSelectedEmailRow());

            if (emailsTable.getRowCount() != 0)
            {
                if (lastEmailRow == 0)
                    emailsTable.setRowSelectionInterval(0, 0);
                else if (lastEmailRow == emailsTable.getRowCount())
                    emailsTable.setRowSelectionInterval(lastEmailRow - 1, lastEmailRow - 1);
                else
                    emailsTable.setRowSelectionInterval(lastEmailRow, lastEmailRow);
            }

            parent.enableSave();
        }
    }// GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_addButtonActionPerformed
    {// GEN-HEADEREND:event_addButtonActionPerformed
        stopEmailEditing();
        ((DefaultTableModel) emailsTable.getModel()).addRow(new Object[] { "" });
        emailsTable.setRowSelectionInterval(emailsTable.getRowCount() - 1, emailsTable.getRowCount() - 1);
        parent.enableSave();
    }// GEN-LAST:event_addButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;

    private javax.swing.JScrollPane alertPane;

    private com.webreach.mirth.client.ui.components.MirthTable alertTable;

    private javax.swing.JPanel applyToChannelsPanel;

    private javax.swing.JScrollPane applyToChannelsScrollPane;

    private com.webreach.mirth.client.ui.components.MirthTable applyToChannelsTable;

    private javax.swing.JPanel bottomPane;

    private javax.swing.JPanel emailsPane;

    private javax.swing.JScrollPane emailsScrollPane;

    private com.webreach.mirth.client.ui.components.MirthTable emailsTable;

    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea errorField;

    private com.webreach.mirth.client.ui.components.MirthVariableList errorList;

    private javax.swing.JPanel errorPane;

    private javax.swing.JPanel errorPanel;

    private javax.swing.JScrollPane errorScrollPane;

    private javax.swing.JButton removeButton;

    private javax.swing.JSplitPane split;

    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea template;

    private javax.swing.JPanel templatePane;
    // End of variables declaration//GEN-END:variables

}

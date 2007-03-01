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

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.components.MirthCheckBoxCellEditor;
import com.webreach.mirth.client.ui.components.MirthCheckBoxCellRenderer;
import com.webreach.mirth.model.Channel;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.model.Alert;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Properties;
import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

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
    
    private final String EMAIL_COLUMN_NAME = "Email";    
    
    private final String ENABLED_TEXT = "Enabled";
    private final String DISABLED_TEXT = "Disabled";
    
    /**
     * Creates the Channel Editor panel. Calls initComponents() and sets up the
     * model, dropdowns, and mouse listeners.
     */
    public AlertPanel()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        lastAlertRow = -1;
        lastEmailRow = -1;
        initComponents();
        makeAlertTable();
        makeApplyToChannelsTable();
        makeEmailsTable();
    }
    
    /**
     * Makes the alert table with a parameter that is true if a new
     * alert should be added as well.
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
        alertTable.setSortable(true);
        alertTable.setOpaque(true);
        alertTable.setDragEnabled(false);
        
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
                    if (lastAlertRow != -1 && lastAlertRow != alertTable.getRowCount() && !isDeleting)
                    {
                        saveAlert();
                    }
                    
                    if (!loadAlert())
                    {
                        if (lastAlertRow == alertTable.getRowCount())
                            alertTable.setRowSelectionInterval(lastAlertRow - 1, lastAlertRow - 1);
                        else
                            alertTable.setRowSelectionInterval(lastAlertRow, lastAlertRow);
                    }
                    else
                    {
                        lastAlertRow = alertTable.getSelectedRow();
                    }
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
        //Key Listener trigger for CTRL-S
        alertTable.addKeyListener(new KeyListener()
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
    
    public void updateAlertTable(boolean addNew)
    {
        Object[][] tableData = null;
        int tableSize = 0;
        
        if(parent.alerts != null)
        {
            tableSize = parent.alerts.size();
            
            if(addNew)
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
                    catch (ClientException ex)
                    {
                        ex.printStackTrace();
                    }
                    
                    alert.setName(getNewAlertName(tableSize));
                    alert.setEnabled(false);
                    tableData[i][0] = alert.getName();
                    tableData[i][1] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_black.png")),DISABLED_TEXT);
                    parent.alerts.add(alert);
                }
                else
                {    
                    Alert alert = parent.alerts.get(i);
                    tableData[i][0] = alert.getName();
                    if(alert.isEnabled())
                        tableData[i][1] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_blue.png")),ENABLED_TEXT);
                    else
                        tableData[i][1] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_black.png")),DISABLED_TEXT);
                }
            }
        }
        
        int row = UIConstants.ERROR_CONSTANT;
        
        if(alertTable != null)
        {
            row = alertTable.getSelectedRow();
            lastAlertRow = row;
            RefreshTableModel model = (RefreshTableModel)alertTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            alertTable = new MirthTable();
            alertTable.setModel(new RefreshTableModel(tableData,new String []{ALERT_NAME_COLUMN_NAME, ALERT_STATUS_COLUMN_NAME})
            {
                boolean[] canEdit = new boolean []
                {
                    true, false
                };
                
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit [columnIndex];
                }
            });
        }
        if(addNew)
        {
            //alertTable.setRowSelectionInterval(alertTable.getRowCount() - 1, alertTable.getRowCount() - 1);
        }
        else if(lastAlertRow >= 0 && lastAlertRow < alertTable.getRowCount())
        {
            alertTable.setRowSelectionInterval(lastAlertRow,lastAlertRow);
        }
    }
    
    /**
     * Makes the alert table with a parameter that is true if a new
     * alert should be added as well.
     */
    public void makeApplyToChannelsTable()
    {
        updateApplyToChannelsTable(null);

        applyToChannelsTable.getColumnModel().getColumn(applyToChannelsTable.getColumnNumber(APPLY_STATUS_COLUMN_NAME)).setCellRenderer(new MirthCheckBoxCellRenderer());
        applyToChannelsTable.getColumnModel().getColumn(applyToChannelsTable.getColumnNumber(APPLY_STATUS_COLUMN_NAME)).setCellEditor(new MirthCheckBoxCellEditor());
        
        applyToChannelsTable.setDragEnabled(false);
        applyToChannelsTable.setRowSelectionAllowed(false);
        applyToChannelsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        applyToChannelsTable.setFocusable(false); 
        applyToChannelsTable.setOpaque(true);
        applyToChannelsTable.getTableHeader().setReorderingAllowed(false);
        applyToChannelsTable.setSortable(false);
        
        applyToChannelsTable.getColumnExt(APPLY_STATUS_COLUMN_NAME).setMaxWidth(50);
        applyToChannelsTable.getColumnExt(APPLY_STATUS_COLUMN_NAME).setMinWidth(50);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            applyToChannelsTable.setHighlighters(highlighter);
        }
        
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

        //Key Listener trigger for CTRL-S
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
        
        if(alert != null && parent.alerts != null)
        {               
            tableSize = parent.channels.size();
            tableData = new Object[tableSize][2];
            
            int i = 0;
            for(Channel channel : parent.channels.values())
            {
                tableData[i][0] = channel.getName();
                if(alert.getChannels() != null && alert.getChannels().contains(channel.getId()))
                {
                    tableData[i][1] = Boolean.TRUE;
                }    
                else
                {
                    tableData[i][1] = Boolean.FALSE;
                }
                i++;
            }
        }
        
        if(alert != null  && applyToChannelsTable != null)
        {
            RefreshTableModel model = (RefreshTableModel)applyToChannelsTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            applyToChannelsTable = new MirthTable();
            applyToChannelsTable.setModel(new RefreshTableModel(tableData,new String []{APPLY_CHANNEL_NAME_COLUMN_NAME, APPLY_STATUS_COLUMN_NAME})
            {
                boolean[] canEdit = new boolean []
                {
                    false, true
                };
                
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit [columnIndex];
                }
            });
        }
    }    
    
    /** Loads a selected connector and returns true on success. */
    public boolean loadAlert()
    {
        Alert current = parent.alerts.get(getSelectedAlertIndex());
        updateApplyToChannelsTable(current);
        updateEmailsTable(current);
        errorField.setText(current.getExpression());
        return true;
    }
    
    public boolean saveAlert()
    {
        int alertIndex = getAlertIndex(lastAlertRow);
        Alert current = parent.alerts.get(alertIndex);
        current.setChannels(getChannels());
        current.setExpression(errorField.getText());
        current.setEmails(getEmails());
        return true;
    }
    
    public List<String> getChannels()
    {
        ArrayList<String> channelList = new ArrayList<String>();
        
        for(int i = 0; i < applyToChannelsTable.getModel().getRowCount(); i++)
        {
            System.out.println(applyToChannelsTable.getModel().getValueAt(i,1));
            //if(Boolean.valueOf((String)applyToChannelsTable.getModel().getValueAt(i,1)).booleanValue())
            //{
                channelList.add((String)applyToChannelsTable.getModel().getValueAt(i,0));
            //}
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
        String alertName = (String) alertTable.getValueAt(selectedRow,alertTable.getColumnNumber(ALERT_NAME_COLUMN_NAME));
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
        int columnNumber = alertTable.getColumnNumber(ALERT_NAME_COLUMN_NAME);
         
        if (alertTable.getSelectedRow() != -1)
        {
            String alertName = ((CellData)alertTable.getValueAt(alertTable.getSelectedRow(), columnNumber)).getText();
            for (int i = 0; i < parent.alerts.size(); i++)
            {
                if (parent.alerts.get(i).getName().equalsIgnoreCase(alertName))
                    return i;
            }
        }
        return UIConstants.ERROR_CONSTANT;
    }
    
    public void setSelectedAlertIndex(int index)
    {
        if(index == UIConstants.ERROR_CONSTANT)
            alertTable.deselectRows();
        else
            alertTable.setRowSelectionInterval(index,index);
    }
        
    /**
     * Get the name that should be used for a new alert so that it is
     * unique.
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
        updateAlertTable(true);
        alertPane.getViewport().setViewPosition(new Point(0, alertTable.getRowHeight()*alertTable.getRowCount()));
        parent.enableSave();
    }
    
    public void enableAlert()
    {
        parent.alerts.get(getAlertIndex()).setEnabled(true);
        parent.enableSave();
    }
    
    public void disableAlert()
    {
        parent.alerts.get(getAlertIndex()).setEnabled(false);
        parent.enableSave();
    }
    
    public void makeEmailsTable()
    {
        updateEmailsTable(null);
        
        emailsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                if(emailsTable.getSelectedRow() != -1)
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
                // 'value' is value contained in the cell located at (rowIndex, vColIndex)
                originalValue = value;

                if (isSelected)
                {
                    // cell (and perhaps other cells) are selected
                }

                // Configure the component with the specified value
                ((JTextField)component).setText((String)value);

                // Return the configured component
                return component;
            }

            public Object getCellEditorValue()
            {
                return ((JTextField)component).getText();
            }
            
            public boolean stopCellEditing()
            {
                String s = (String)getCellEditorValue();
                parent.enableSave();
                removeButton.setEnabled(true);
                return super.stopCellEditing();
            }
            
            /**
             * Enables the editor only for double-clicks.
             */
            public boolean isCellEditable(EventObject evt) 
            {
                if (evt instanceof MouseEvent && ((MouseEvent)evt).getClickCount() >= 2) 
                {
                    removeButton.setEnabled(false);
                    return true;
                }
                return false;
            }
        };
        
        // Set the custom cell editor for the Destination Name column.
        emailsTable.getColumnModel().getColumn(
                emailsTable.getColumnModel().getColumnIndex(
                EMAIL_COLUMN_NAME)).setCellEditor(
                new EmailsTableCellEditor());
                
        emailsTable.setSelectionMode(0);
        emailsTable.setRowSelectionAllowed(true);
        emailsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        emailsTable.setDragEnabled(false);
        emailsTable.setOpaque(true);
        emailsTable.setSortable(false);
        emailsTable.getTableHeader().setReorderingAllowed(false);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean(
        "highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter
                    .addHighlighter(new AlternateRowHighlighter(
                    UIConstants.HIGHLIGHTER_COLOR,
                    UIConstants.BACKGROUND_COLOR,
                    UIConstants.TITLE_TEXT_COLOR));
            emailsTable.setHighlighters(highlighter);
        }
        
        emailsScrollPane.setViewportView(emailsTable);
    }
    
    public void updateEmailsTable(Alert alert)
    {
        Object[][] tableData = null;
        
        if(alert != null)
        {
            tableData = new Object[alert.getEmails().size()][1];       

            for(int i = 0; i < alert.getEmails().size(); i++)
            {
                tableData[i][0] = alert.getEmails().get(i);
            }
        }

        if(alert != null  && applyToChannelsTable != null)
        {
            RefreshTableModel model = (RefreshTableModel)emailsTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            emailsTable = new MirthTable();
            emailsTable.setModel(new javax.swing.table.DefaultTableModel(
            tableData, new String[] { EMAIL_COLUMN_NAME })
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
       
        for(int i = 0; i < emailsTable.getRowCount(); i++)
            if(((String)emailsTable.getModel().getValueAt(i,0)).length() > 0)
                emails.add((String)emailsTable.getModel().getValueAt(i,0));
        
        return emails;
    }
    
    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectRows()
    {
        emailsTable.clearSelection();
        removeButton.setEnabled(false);
    }
    
        /** Get the currently selected destination index */
    public int getSelectedRow()
    {
        if (emailsTable.isEditing())
            return emailsTable.getEditingRow();
        else
            return emailsTable.getSelectedRow();
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
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
        errorCodePane = new javax.swing.JPanel();
        errorCodeScrollPane = new javax.swing.JScrollPane();
        errorCodeList = new com.webreach.mirth.client.ui.components.MirthVariableList();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        split.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        split.setDividerLocation(125);
        split.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        alertPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        alertPane.setViewportView(alertTable);

        split.setLeftComponent(alertPane);

        bottomPane.setBackground(new java.awt.Color(255, 255, 255));
        applyToChannelsPanel.setBackground(new java.awt.Color(255, 255, 255));
        applyToChannelsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(""), "Apply to Channels", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        applyToChannelsScrollPane.setViewportView(applyToChannelsTable);

        org.jdesktop.layout.GroupLayout applyToChannelsPanelLayout = new org.jdesktop.layout.GroupLayout(applyToChannelsPanel);
        applyToChannelsPanel.setLayout(applyToChannelsPanelLayout);
        applyToChannelsPanelLayout.setHorizontalGroup(
            applyToChannelsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(applyToChannelsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(applyToChannelsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                .addContainerGap())
        );
        applyToChannelsPanelLayout.setVerticalGroup(
            applyToChannelsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(applyToChannelsPanelLayout.createSequentialGroup()
                .add(applyToChannelsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                .addContainerGap())
        );

        errorPane.setBackground(new java.awt.Color(255, 255, 255));
        errorPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Error Matching Field", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        errorField.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.jdesktop.layout.GroupLayout errorPaneLayout = new org.jdesktop.layout.GroupLayout(errorPane);
        errorPane.setLayout(errorPaneLayout);
        errorPaneLayout.setHorizontalGroup(
            errorPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(errorPaneLayout.createSequentialGroup()
                .addContainerGap()
                .add(errorField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                .addContainerGap())
        );
        errorPaneLayout.setVerticalGroup(
            errorPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(errorPaneLayout.createSequentialGroup()
                .add(errorField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                .addContainerGap())
        );

        emailsPane.setBackground(new java.awt.Color(255, 255, 255));
        emailsPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Emails to Receive Alerts", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
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
        emailsPaneLayout.setHorizontalGroup(
            emailsPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, emailsPaneLayout.createSequentialGroup()
                .addContainerGap()
                .add(emailsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(emailsPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(addButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(removeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        emailsPaneLayout.setVerticalGroup(
            emailsPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(emailsPaneLayout.createSequentialGroup()
                .add(emailsPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(emailsPaneLayout.createSequentialGroup()
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton))
                    .add(emailsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE))
                .addContainerGap())
        );

        errorCodePane.setBackground(new java.awt.Color(255, 255, 255));
        errorCodePane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Error Codes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        errorCodeScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        errorCodeList.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        errorCodeList.setModel(new javax.swing.AbstractListModel()
        {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        errorCodeScrollPane.setViewportView(errorCodeList);

        org.jdesktop.layout.GroupLayout errorCodePaneLayout = new org.jdesktop.layout.GroupLayout(errorCodePane);
        errorCodePane.setLayout(errorCodePaneLayout);
        errorCodePaneLayout.setHorizontalGroup(
            errorCodePaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(errorCodeScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
        );
        errorCodePaneLayout.setVerticalGroup(
            errorCodePaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(errorCodePaneLayout.createSequentialGroup()
                .add(errorCodeScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout bottomPaneLayout = new org.jdesktop.layout.GroupLayout(bottomPane);
        bottomPane.setLayout(bottomPaneLayout);
        bottomPaneLayout.setHorizontalGroup(
            bottomPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bottomPaneLayout.createSequentialGroup()
                .addContainerGap()
                .add(applyToChannelsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bottomPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(emailsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(errorPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(errorCodePane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        bottomPaneLayout.setVerticalGroup(
            bottomPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bottomPaneLayout.createSequentialGroup()
                .addContainerGap()
                .add(bottomPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, errorCodePane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, bottomPaneLayout.createSequentialGroup()
                        .add(errorPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(emailsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(applyToChannelsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        split.setRightComponent(bottomPane);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(split, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 735, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(split, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeButtonActionPerformed
    {//GEN-HEADEREND:event_removeButtonActionPerformed
        if(getSelectedRow() != -1 && !emailsTable.isEditing())
        {            
            ((DefaultTableModel)emailsTable.getModel()).removeRow(getSelectedRow());
            
            if(emailsTable.getRowCount() != 0)
            {
                if(lastEmailRow == 0)
                    emailsTable.setRowSelectionInterval(0,0);
                else if(lastEmailRow == emailsTable.getRowCount())
                    emailsTable.setRowSelectionInterval(lastEmailRow-1,lastEmailRow-1);
                else
                    emailsTable.setRowSelectionInterval(lastEmailRow,lastEmailRow);
            }
                        
            parent.enableSave();
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addButtonActionPerformed
    {//GEN-HEADEREND:event_addButtonActionPerformed
        ((DefaultTableModel)emailsTable.getModel()).addRow(new Object[]{""});
        emailsTable.setRowSelectionInterval(emailsTable.getRowCount()-1,emailsTable.getRowCount()-1);
        parent.enableSave();
    }//GEN-LAST:event_addButtonActionPerformed
    
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
    private com.webreach.mirth.client.ui.components.MirthVariableList errorCodeList;
    private javax.swing.JPanel errorCodePane;
    private javax.swing.JScrollPane errorCodeScrollPane;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea errorField;
    private javax.swing.JPanel errorPane;
    private javax.swing.JButton removeButton;
    private javax.swing.JSplitPane split;
    // End of variables declaration//GEN-END:variables
    
}

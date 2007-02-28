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
import com.webreach.mirth.model.Channel;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.model.Alert;
import java.awt.Component;
import javax.swing.JCheckBox;

/** The channel editor panel. Majority of the client application */
public class AlertPanel extends javax.swing.JPanel
{
    private Frame parent;
    private boolean isDeleting = false;
    private int lastRow, lastApplyRow;
    
    private final String ALERT_NAME_COLUMN_NAME = "Name";
    private final String ALERT_STATUS_COLUMN_NAME = "Status";
    
    private final String APPLY_CHANNEL_NAME_COLUMN_NAME = "Channel Name";
    private final String APPLY_STATUS_COLUMN_NAME = "Applied";
    
    private final String ENABLED_TEXT = "Enabled";
    private final String DISABLED_TEXT = "Disabled";
    
    /**
     * Creates the Channel Editor panel. Calls initComponents() and sets up the
     * model, dropdowns, and mouse listeners.
     */
    public AlertPanel()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        lastRow = -1;
        initComponents();
        makeAlertTable();
        makeApplyToChannelsTable();
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
                    if (lastRow != -1 && lastRow != alertTable.getRowCount() && !isDeleting)
                    {
                        saveAlert();
                    }
                    
                    if (!loadAlert())
                    {
                        if (lastRow == alertTable.getRowCount())
                            alertTable.setRowSelectionInterval(lastRow - 1, lastRow - 1);
                        else
                            alertTable.setRowSelectionInterval(lastRow, lastRow);
                    }
                    else
                    {
                        lastRow = alertTable.getSelectedRow();
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
            lastRow = row;
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
        else if(lastRow >= 0 && lastRow < alertTable.getRowCount())
        {
            alertTable.setRowSelectionInterval(lastRow,lastRow);
        }
    }
    
    /**
     * Makes the alert table with a parameter that is true if a new
     * alert should be added as well.
     */
    public void makeApplyToChannelsTable()
    {
        updateApplyToChannelsTable(null);

        applyToChannelsTable.getColumnModel().getColumn(applyToChannelsTable.getColumnNumber(APPLY_STATUS_COLUMN_NAME)).setCellRenderer(new DefaultTableCellRenderer()
        {
            // the method gives the component  like whome the cell must be rendered
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean isFocused, int row, int col)
            {
                this.setHorizontalAlignment(CENTER);
                if (isSelected)
                {
                    setForeground(applyToChannelsTable.getSelectionForeground());
                    setBackground(applyToChannelsTable.getSelectionBackground());
                }
                else
                {
                    setForeground(applyToChannelsTable.getForeground());
                    setBackground(applyToChannelsTable.getBackground());
                }
        
                boolean marked = Boolean.valueOf( value.toString() ).booleanValue();
                JCheckBox rendererComponent = new JCheckBox();
                if (marked)
                {
                    rendererComponent.setSelected(true);
                }
                else
                {
                    rendererComponent.setSelected(false);
                }
                return rendererComponent;
            }
        });
        
        applyToChannelsTable.setDragEnabled(false);
        applyToChannelsTable.setSelectionMode(0);
        applyToChannelsTable.setRowSelectionAllowed(true);
        applyToChannelsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        applyToChannelsTable.setFocusable(false); 
        applyToChannelsTable.setSortable(true);
        applyToChannelsTable.setOpaque(true);
        
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
        
        applyToChannelsPane.setViewportView(applyToChannelsTable);

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
        
        int row = UIConstants.ERROR_CONSTANT;
        
        if(alert != null  && applyToChannelsTable != null)
        {
            row = applyToChannelsTable.getSelectedRow();
            lastApplyRow = row;
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
        
        if(lastApplyRow >= 0 && lastApplyRow < applyToChannelsTable.getRowCount())
        {
            applyToChannelsTable.setRowSelectionInterval(lastRow,lastRow);
        }
    }    
    
    /** Loads a selected connector and returns true on success. */
    public boolean loadAlert()
    {
        updateApplyToChannelsTable(parent.alerts.get(getSelectedAlertIndex()));        
        return true;
    }
    
    public boolean saveAlert()
    {
        int alertIndex = getAlertIndex(lastRow);
        Alert current = parent.alerts.get(alertIndex);
        return false;
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
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        split = new javax.swing.JSplitPane();
        alertPane = new javax.swing.JScrollPane();
        alertTable = null;
        bottomPane = new javax.swing.JPanel();
        applyToChannelsPanel = new javax.swing.JPanel();
        applyToChannelsPane = new javax.swing.JScrollPane();
        applyToChannelsTable = null;
        jPanel1 = new javax.swing.JPanel();
        mirthSyntaxTextArea1 = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        mirthTable1 = null;
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        mirthVariableList1 = new com.webreach.mirth.client.ui.components.MirthVariableList();

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
        applyToChannelsPane.setViewportView(applyToChannelsTable);

        org.jdesktop.layout.GroupLayout applyToChannelsPanelLayout = new org.jdesktop.layout.GroupLayout(applyToChannelsPanel);
        applyToChannelsPanel.setLayout(applyToChannelsPanelLayout);
        applyToChannelsPanelLayout.setHorizontalGroup(
            applyToChannelsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(applyToChannelsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(applyToChannelsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                .addContainerGap())
        );
        applyToChannelsPanelLayout.setVerticalGroup(
            applyToChannelsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(applyToChannelsPanelLayout.createSequentialGroup()
                .add(applyToChannelsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Error Matching Field", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        mirthSyntaxTextArea1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(mirthSyntaxTextArea1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(mirthSyntaxTextArea1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Emails to Receive Alerts", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        jScrollPane1.setViewportView(mirthTable1);

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

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(addButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(removeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Error Codes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        jScrollPane2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mirthVariableList1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mirthVariableList1.setModel(new javax.swing.AbstractListModel()
        {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(mirthVariableList1);

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
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
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        bottomPaneLayout.setVerticalGroup(
            bottomPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bottomPaneLayout.createSequentialGroup()
                .addContainerGap()
                .add(bottomPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, bottomPaneLayout.createSequentialGroup()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
// TODO add your handling code here:
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addButtonActionPerformed
    {//GEN-HEADEREND:event_addButtonActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_addButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JScrollPane alertPane;
    private com.webreach.mirth.client.ui.components.MirthTable alertTable;
    private javax.swing.JScrollPane applyToChannelsPane;
    private javax.swing.JPanel applyToChannelsPanel;
    private com.webreach.mirth.client.ui.components.MirthTable applyToChannelsTable;
    private javax.swing.JPanel bottomPane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea mirthSyntaxTextArea1;
    private com.webreach.mirth.client.ui.components.MirthTable mirthTable1;
    private com.webreach.mirth.client.ui.components.MirthVariableList mirthVariableList1;
    private javax.swing.JButton removeButton;
    private javax.swing.JSplitPane split;
    // End of variables declaration//GEN-END:variables
    
}

/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dashboardstatus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.CellData;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.ImageCellRenderer;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;

public class DashboardConnectorStatusPanel extends javax.swing.JPanel {

    private static final String ID_COLUMN_HEADER = "Id";
    private static final String CHANNEL_COLUMN_HEADER = "Channel";
    private static final String TIME_COLUMN_HEADER = "Timestamp";
    private static final String CONNECTOR_INFO_COLUMN_HEADER = "Connector Info";
    private static final String EVENT_COLUMN_HEADER = "Event";
    private static final String INFORMATION_COLUMN_HEADER = "Info";
    private static final int PAUSED = 0;
    private static final int RESUMED = 1;
    private HashMap<String, Integer> channelStates = new HashMap<String, Integer>();
    private JPopupMenu rightclickPopup;
    private ImageIcon greenBullet;      //  CONNECTED
    private ImageIcon yellowBullet;     //  BUSY
    private ImageIcon redBullet;        //  DISCONNECTED
    private ImageIcon blueBullet;       //  INITIALIZED
    private ImageIcon blackBullet;	    //  DONE
    private static final String NO_CHANNEL_SELECTED = "No Channel Selected";
    private String selectedChannelId;
    private Map<String, List<Integer>> selectedConnectors;
    private DashboardConnectorStatusClient dcsc;
    private Preferences userPreferences;
    private Frame parent;
    private int currentDashboardLogSize;

    /** Creates new form DashboardConnectorStatusPanel */
    public DashboardConnectorStatusPanel(DashboardConnectorStatusClient dcsc) {
        this.parent = PlatformUI.MIRTH_FRAME;
        this.dcsc = dcsc;
        greenBullet = new ImageIcon(Frame.class.getResource("images/bullet_green.png"));
        yellowBullet = new ImageIcon(Frame.class.getResource("images/bullet_yellow.png"));
        redBullet = new ImageIcon(Frame.class.getResource("images/bullet_red.png"));
        blueBullet = new ImageIcon(Frame.class.getResource("images/bullet_blue.png"));
        blackBullet = new ImageIcon(Frame.class.getResource("images/bullet_black.png"));
        channelStates.put(NO_CHANNEL_SELECTED, RESUMED);

        initComponents();
        initLayouts();

        clearLog.setIcon(UIConstants.ICON_X);
        clearLog.setToolTipText("Clear Displayed Log");

        logSizeChange.setIcon(UIConstants.ICON_CHECK);
        logSizeChange.setToolTipText("Change Log Display Size");

        makeLogTable();

        logSizeTextField.setDocument(new MirthFieldConstraints(3, false, false, true));     // max 999. all numbers. default to 250.
        userPreferences = Preferences.userNodeForPackage(Mirth.class);
        currentDashboardLogSize = userPreferences.getInt("dashboardLogSize", 250);
        logSizeTextField.setText(currentDashboardLogSize + "");

    }

    /*
    This method overwrites the setting layout part in initComponent generated code by NetBeans, because NetBeans wouldn't support the vertical alignment well enough.
     */
    public void initLayouts() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(2, 2, 2).addComponent(pauseResume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(2, 2, 2).addComponent(clearLog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 336, Short.MAX_VALUE).addComponent(logSizeText).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(logSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(2, 2, 2).addComponent(logSizeChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(2, 2, 2)).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE));

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{clearLog, logSizeChange, pauseResume});

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE).addGap(0, 0, 0).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(pauseResume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(clearLog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(logSizeChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(logSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(logSizeText))));

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[]{clearLog, logSizeChange, pauseResume});

    }

    /**
     * Makes the status table with all current server information.
     */
    public void makeLogTable() {
        updateTable(null);
        logTable.setDoubleBuffered(true);
        logTable.setSelectionMode(0);
        logTable.getColumnExt(ID_COLUMN_HEADER).setVisible(false);
        logTable.getColumnExt(EVENT_COLUMN_HEADER).setCellRenderer(new ImageCellRenderer());
        logTable.packTable(UIConstants.COL_MARGIN);
        logTable.setRowHeight(UIConstants.ROW_HEIGHT);
        logTable.setOpaque(true);
        logTable.setRowSelectionAllowed(false);
        logTable.setSortable(true);
        logTable.setFocusable(false);
        logTable.setHorizontalScrollEnabled(true);
        logTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        createPopupMenu();
        jScrollPane1.setViewportView(logTable);
    }

    public void createPopupMenu() {
        JMenuItem menuItem;

        //Create the popup menu.
        rightclickPopup = new JPopupMenu();
        menuItem = new JMenuItem("Pause Log");
        menuItem.setIcon(UIConstants.ICON_PAUSE);
        menuItem.addActionListener(new PauseResumeActionListener());
        rightclickPopup.add(menuItem);
        menuItem = new JMenuItem("Resume Log");
        menuItem.setIcon(UIConstants.ICON_RESUME);
        menuItem.addActionListener(new PauseResumeActionListener());
        rightclickPopup.add(menuItem);
        rightclickPopup.addSeparator();
        menuItem = new JMenuItem("Clear Log");
        menuItem.setIcon(UIConstants.ICON_X);
        menuItem.addActionListener(new ClearLogActionListener());
        rightclickPopup.add(menuItem);

        // initially show 'Pause', hide 'Resume'
        rightclickPopup.getComponent(0).setVisible(true);
        rightclickPopup.getComponent(1).setVisible(false);

        //Add listener to the text area so the popup menu can come up.
        MouseListener popupListener = new PopupListener(rightclickPopup);
        jScrollPane1.addMouseListener(popupListener);
        logTable.addMouseListener(popupListener);
    }

    class PauseResumeActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (!isPaused(selectedChannelId)) {
                channelStates.put(selectedChannelId, PAUSED);
            } else {
                channelStates.put(selectedChannelId, RESUMED);
            }
            adjustPauseResumeButton(selectedChannelId);
        }
    }

    class ClearLogActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            // "clear log" only affects on the client side.
            // because clearing log on one client should NOT affect other clients' logs.
            // clear logs on client side only.
            dcsc.clearLog(selectedChannelId);
        }
    }

    class PopupListener extends MouseAdapter {

        JPopupMenu popup;

        PopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }

        public void mousePressed(MouseEvent e) {
            checkPopup(e);
        }

        public void mouseClicked(MouseEvent e) {
            checkPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            checkPopup(e);
        }

        private void checkPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public int getCurrentDashboardLogSize() {
        return currentDashboardLogSize;
    }

    public void setSelectedChannelId(String channelId) {
        selectedChannelId = channelId;
    }
    
    public void setSelectedConnectors(Map<String, List<Integer>> selectedConnectors) {
        this.selectedConnectors = selectedConnectors;
    }

    public boolean isPaused(String channelId) {
        if (channelStates.containsKey(channelId)) {
            return channelStates.get(channelId) == PAUSED;
        } else {
            // first time viewing the channel log. default to RESUME.
            channelStates.put(channelId, RESUMED);
            return false;
        }
    }

    public void resetAllChannelStates() {
        channelStates.clear();
    }

    public void adjustPauseResumeButton(String channelId) {
        if (isPaused(channelId)) {
            pauseResume.setIcon(UIConstants.ICON_RESUME);
            pauseResume.setToolTipText("Resume Log");
            rightclickPopup.getComponent(0).setVisible(false);
            rightclickPopup.getComponent(1).setVisible(true);
        } else {
            pauseResume.setIcon(UIConstants.ICON_PAUSE);
            pauseResume.setToolTipText("Pause Log");
            rightclickPopup.getComponent(0).setVisible(true);
            rightclickPopup.getComponent(1).setVisible(false);
        }
    }

    /**
     * This method won't be called when it's in the PAUSED state.
     * @param channelLogs
     */
    public synchronized void updateTable(LinkedList<ConnectionLogItem> channelLogs) {
        Object[][] tableData;
        if (channelLogs != null) {
            tableData = new Object[channelLogs.size()][6];
            int tableSize = 0;
            for (int i = 0; i < channelLogs.size(); i++) {
                ConnectionLogItem logItem = channelLogs.get(i);
                
                String channelId = logItem.getChannelId();
                Integer metaDataId = logItem.getMetadataId().intValue();
                
                // If there are multiple selected channels defined (not null), then 
                // check to make sure this channel log row is one of those channels.
                //
                // With multi-select, the log list/state for ALL channels is used.
                // This means pausing the list for multi-select would also pause the 
                // list for no selection.  The log size will also not be correct for
                // multi-select because it is enforcing the size on the larger list.
                
                boolean addRow = false;
                if (selectedConnectors == null) {
                    addRow = true;
                } else {
                    List<Integer> selectedMetaDataIds = selectedConnectors.get(channelId);
                    addRow = selectedMetaDataIds != null && (selectedMetaDataIds.contains(null) || selectedMetaDataIds.contains(metaDataId));
                }
                
                if (addRow) {
                    tableSize++;

                    tableData[i][0] = logItem.getLogId();       	// Id (hidden)
                    tableData[i][1] = logItem.getDateAdded();       // Timestamp
                    tableData[i][2] = logItem.getChannelName();   	// Channel Name (hidden when viewing a specific channel)
                    tableData[i][3] = logItem.getConnectorType();   // Connector Info

                    if (logItem.getEventState().equalsIgnoreCase("IDLE")) {
                        tableData[i][4] = new CellData(yellowBullet, "Idle");
                    } else if (logItem.getEventState().equalsIgnoreCase("READING")) {
                        tableData[i][4] = new CellData(greenBullet, "Reading");
                    } else if (logItem.getEventState().equalsIgnoreCase("WRITING")) {
                        tableData[i][4] = new CellData(greenBullet, "Writing");
                    } else if (logItem.getEventState().equalsIgnoreCase("POLLING")) {
                        tableData[i][4] = new CellData(greenBullet, "Polling");
                    } else if (logItem.getEventState().equalsIgnoreCase("RECEIVING")) {
                        tableData[i][4] = new CellData(greenBullet, "Receiving");
                    } else if (logItem.getEventState().equalsIgnoreCase("SENDING")) {
                        tableData[i][4] = new CellData(greenBullet, "Sending");
                    } else if (logItem.getEventState().equalsIgnoreCase("WAITING FOR RESPONSE")) {
                        tableData[i][4] = new CellData(yellowBullet, "Waiting for Response");
                    } else if (logItem.getEventState().equalsIgnoreCase("CONNECTED")) {
                        tableData[i][4] = new CellData(greenBullet, "Connected");
                    } else if (logItem.getEventState().equalsIgnoreCase("CONNECTING")) {
                        tableData[i][4] = new CellData(yellowBullet, "Connecting");
                    } else if (logItem.getEventState().equalsIgnoreCase("DISCONNECTED")) {
                        tableData[i][4] = new CellData(redBullet, "Disconnected");
                    } else if (logItem.getEventState().equalsIgnoreCase("INFO")) {
                        tableData[i][4] = new CellData(blueBullet, "Info");
                    } else if (logItem.getEventState().equalsIgnoreCase("FAILURE")) {
                        tableData[i][4] = new CellData(blackBullet, "Failure");
                    }

                    tableData[i][5] = logItem.getInformation();       // Infomation
                }
            }

            // If there were multiple rows selected, the number of rows
            // added might be smaller than the array.  Rebuild the array
            // with only the rows added.
            if (tableData.length > tableSize) {
                Object[][] newTableData = new Object[tableSize][6];
                int j = 0;
                for (int i = 0; i < tableData.length; i++) {
                    if (tableData[i][0] != null) {
                        newTableData[j] = tableData[i];
                        j++;
                    }
                }
                tableData = newTableData;
            }
        } else {
            tableData = new Object[0][6];
        }

        if (logTable != null) {
            RefreshTableModel model = (RefreshTableModel) logTable.getModel();
            model.refreshDataVector(tableData);
        } else {
            logTable = new MirthTable();
            logTable.setModel(new RefreshTableModel(tableData,
                    new String[]{ID_COLUMN_HEADER, TIME_COLUMN_HEADER,
                        CHANNEL_COLUMN_HEADER, CONNECTOR_INFO_COLUMN_HEADER,
                        EVENT_COLUMN_HEADER, INFORMATION_COLUMN_HEADER}) {

                boolean[] canEdit = new boolean[]{false, false, false, false, false, false};

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            logTable.setHighlighters(highlighter);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        logSizeChange = new javax.swing.JButton();
        clearLog = new javax.swing.JButton();
        pauseResume = new javax.swing.JButton();
        logSizeTextField = new javax.swing.JTextField();
        logSizeText = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        logTable = null;

        logSizeChange.setBorderPainted(false);
        logSizeChange.setContentAreaFilled(false);
        logSizeChange.setMargin(new java.awt.Insets(4, 4, 4, 4));
        logSizeChange.setMaximumSize(new java.awt.Dimension(24, 24));
        logSizeChange.setMinimumSize(new java.awt.Dimension(24, 24));
        logSizeChange.setPreferredSize(new java.awt.Dimension(24, 24));
        logSizeChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logSizeChangeActionPerformed(evt);
            }
        });
        logSizeChange.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logSizeChangeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logSizeChangeMouseExited(evt);
            }
        });

        clearLog.setBorderPainted(false);
        clearLog.setContentAreaFilled(false);
        clearLog.setMargin(new java.awt.Insets(4, 4, 4, 4));
        clearLog.setMaximumSize(new java.awt.Dimension(24, 24));
        clearLog.setMinimumSize(new java.awt.Dimension(24, 24));
        clearLog.setPreferredSize(new java.awt.Dimension(24, 24));
        clearLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearLogActionPerformed(evt);
            }
        });
        clearLog.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                clearLogMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                clearLogMouseExited(evt);
            }
        });

        pauseResume.setBorderPainted(false);
        pauseResume.setContentAreaFilled(false);
        pauseResume.setMargin(new java.awt.Insets(4, 4, 4, 4));
        pauseResume.setMaximumSize(new java.awt.Dimension(24, 24));
        pauseResume.setMinimumSize(new java.awt.Dimension(24, 24));
        pauseResume.setPreferredSize(new java.awt.Dimension(24, 24));
        pauseResume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseResumeActionPerformed(evt);
            }
        });
        pauseResume.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pauseResumeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pauseResumeMouseExited(evt);
            }
        });

        logSizeTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        logSizeTextField.setMaximumSize(new java.awt.Dimension(45, 19));
        logSizeTextField.setMinimumSize(new java.awt.Dimension(45, 19));
        logSizeTextField.setPreferredSize(new java.awt.Dimension(45, 19));

        logSizeText.setText("Log Size:");

        jScrollPane1.setViewportView(logTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(pauseResume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(clearLog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 336, Short.MAX_VALUE)
                .addComponent(logSizeText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(logSizeChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {clearLog, logSizeChange, pauseResume});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pauseResume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearLog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(logSizeChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(logSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(logSizeText)))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {clearLog, logSizeChange, pauseResume});

    }// </editor-fold>//GEN-END:initComponents

    private void pauseResumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseResumeActionPerformed
        if (!isPaused(selectedChannelId)) {
            channelStates.put(selectedChannelId, PAUSED);
        } else {
            channelStates.put(selectedChannelId, RESUMED);
        }
        adjustPauseResumeButton(selectedChannelId);
    }//GEN-LAST:event_pauseResumeActionPerformed

    private void clearLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearLogActionPerformed
        // "clear log" only affects on the client side.
        // because clearing log on one client should NOT affect other clients' logs.
        // clear logs on client side only.
        dcsc.clearLog(selectedChannelId);
    }//GEN-LAST:event_clearLogActionPerformed

    private void logSizeChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logSizeChangeActionPerformed
        // NOTE: the log size on the server is always 1000, which is max. because if there are multiple clients connected to the same server,
        //  it has to be able to support the maximum allowed in case some client has it set at 999.
        // i.e. this log size change only affects on the client side.
        if (logSizeTextField.getText().length() == 0) {
            parent.alertWarning(this, "Please enter a valid number.");
            return;
        }
        int newDashboardLogSize = Integer.parseInt(logSizeTextField.getText());
        if (newDashboardLogSize != currentDashboardLogSize) {
            if (newDashboardLogSize <= 0) {
                parent.alertWarning(this, "Please enter a log size that is larger than 0.");
            } else {
                userPreferences.putInt("dashboardLogSize", newDashboardLogSize);
                currentDashboardLogSize = newDashboardLogSize;
                dcsc.resetLogSize(newDashboardLogSize, selectedChannelId);
            }
        }
    }//GEN-LAST:event_logSizeChangeActionPerformed

    private void logSizeChangeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logSizeChangeMouseExited
        logSizeChange.setBorderPainted(false);
        logSizeChange.setContentAreaFilled(false);
    }//GEN-LAST:event_logSizeChangeMouseExited

    private void logSizeChangeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logSizeChangeMouseEntered
        logSizeChange.setBorderPainted(true);
        logSizeChange.setContentAreaFilled(true);
    }//GEN-LAST:event_logSizeChangeMouseEntered

    private void clearLogMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clearLogMouseExited
        clearLog.setBorderPainted(false);
        clearLog.setContentAreaFilled(false);
    }//GEN-LAST:event_clearLogMouseExited

    private void clearLogMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clearLogMouseEntered
        clearLog.setBorderPainted(true);
        clearLog.setContentAreaFilled(true);
    }//GEN-LAST:event_clearLogMouseEntered

    private void pauseResumeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pauseResumeMouseExited
        pauseResume.setBorderPainted(false);
        pauseResume.setContentAreaFilled(false);
    }//GEN-LAST:event_pauseResumeMouseExited

    private void pauseResumeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pauseResumeMouseEntered
        pauseResume.setBorderPainted(true);
        pauseResume.setContentAreaFilled(true);
    }//GEN-LAST:event_pauseResumeMouseEntered
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearLog;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton logSizeChange;
    private javax.swing.JLabel logSizeText;
    private javax.swing.JTextField logSizeTextField;
    private com.mirth.connect.client.ui.components.MirthTable logTable;
    private javax.swing.JButton pauseResume;
    // End of variables declaration//GEN-END:variables
}

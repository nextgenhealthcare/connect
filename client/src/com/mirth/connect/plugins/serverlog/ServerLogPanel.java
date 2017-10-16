/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.serverlog;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.plugins.DashboardTablePlugin;

public class ServerLogPanel extends javax.swing.JPanel {

    private static final String ID_COLUMN_HEADER = "Id";
    private static final String LOG_INFO_COLUMN_HEADER = "Log Information";
    private JPopupMenu rightclickPopup;
    private static final int PAUSED = 0;
    private static final int RESUMED = 1;
    private int state = PAUSED;     // initialized the state as PAUSED so that when the administrator first opens up and calls the adjustPauseResumeButton method, it'll be in the RESUMED state.
    private int currentServerLogSize;
    private Preferences userPreferences;
    private Frame parent;
    private ServerLogClient serverLogClient;

    /** Creates new form ServerLogPanel */
    public ServerLogPanel(ServerLogClient serverLogClient) {
        this.parent = PlatformUI.MIRTH_FRAME;
        this.serverLogClient = serverLogClient;

        initComponents();
        initLayouts();

        clearLog.setIcon(UIConstants.ICON_X);
        clearLog.setToolTipText("Clear Displayed Log");

        logSizeChange.setIcon(UIConstants.ICON_CHECK);
        logSizeChange.setToolTipText("Change Log Display Size");

        makeLogTextArea();

        logSizeTextField.setDocument(new MirthFieldConstraints(2, false, false, true));     // max 100. all numbers. default to 50.
        userPreferences = Preferences.userNodeForPackage(Mirth.class);
        currentServerLogSize = userPreferences.getInt("serverLogSize", 50);
        logSizeTextField.setText(currentServerLogSize + "");
    }


    /*
    This method overwrites the setting layout part in initComponent generated code by NetBeans, because NetBeans wouldn't support the vertical alignment well enough.
     */
    public void initLayouts() {
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(2, 2, 2).addComponent(pauseResume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(2, 2, 2).addComponent(clearLog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 336, Short.MAX_VALUE).addComponent(logSizeText).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(logSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(2, 2, 2).addComponent(logSizeChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(2, 2, 2)).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE).addGap(0, 0, 0).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER).addComponent(pauseResume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(clearLog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(logSizeChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(logSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(logSizeText))));
    }

    /**
     * Makes the status table with all current server information.
     */
    public void makeLogTextArea() {
        updateTable(null);
        logTable.setDoubleBuffered(true);
        logTable.setSelectionMode(0);
        logTable.getColumnExt(ID_COLUMN_HEADER).setVisible(false);
        logTable.packTable(UIConstants.COL_MARGIN);
        logTable.setRowHeight(UIConstants.ROW_HEIGHT);
        logTable.setOpaque(true);
        logTable.setRowSelectionAllowed(true);
        logTable.setSortable(false);
        logTable.setFocusable(false);
        logTable.setHorizontalScrollEnabled(true);
        logTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        createPopupMenu();
        jScrollPane1.setViewportView(logTable);

        // listen for trigger button and double click to edit channel.
        logTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (logTable.rowAtPoint(new Point(evt.getX(), evt.getY())) == -1) {
                    return;
                }

                if (evt.getClickCount() >= 2) {
                    // synchronizing this to prevent ArrayIndexOutOfBoundsException since the server log table is constantly being redrawn.
                    synchronized (this) {
                        new ViewServerLogContentDialog(parent, String.valueOf(logTable.getModel().getValueAt(logTable.convertRowIndexToModel(logTable.getSelectedRow()), 1)));
                    }
                }
            }
        });
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
        adjustPauseResumeButton();

        //Add listener to the text area so the popup menu can come up.
        MouseListener popupListener = new PopupListener(rightclickPopup);
        jScrollPane1.addMouseListener(popupListener);
        logTable.addMouseListener(popupListener);
    }

    class PauseResumeActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            adjustPauseResumeButton();
        }
    }

    class ClearLogActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            // "clear log" only affects on the client side.
            // because clearing log on one client should NOT affect other clients' logs.
            // clear logs on client side only.
            serverLogClient.clearLog();
        }
    }

    /**
     * This method won't be called when it's in the PAUSED state.
     * @param serverLogs
     */
    public synchronized void updateTable(LinkedList<ServerLogItem> serverLogs) {
        Object[][] tableData;

        if (serverLogs != null) {
            List<Object[]> dataList = new ArrayList<Object[]>();

            String serverId = null;
            for (DashboardTablePlugin plugin : LoadedExtensions.getInstance().getDashboardTablePlugins().values()) {
                serverId = plugin.getServerId();
                if (serverId != null) {
                    break;
                }
            }

            for (ServerLogItem item : serverLogs) {
                if (serverId == null || serverId.equals(item.getServerId())) {
                    dataList.add(new Object[] { item.getId(), item });
                }
            }

            tableData = dataList.toArray(new Object[dataList.size()][]);
        } else {
            tableData = new Object[0][2];
        }

        if (logTable != null) {
            RefreshTableModel model = (RefreshTableModel) logTable.getModel();
            model.refreshDataVector(tableData);
        } else {
            logTable = new MirthTable();
            logTable.setModel(new RefreshTableModel(tableData, new String[]{ID_COLUMN_HEADER, LOG_INFO_COLUMN_HEADER}) {

                boolean[] canEdit = new boolean[]{false, false};

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

    public boolean isPaused() {
        return state == PAUSED;
    }

    public void adjustPauseResumeButton() {
        if (state == RESUMED) {
            state = PAUSED;
            pauseResume.setIcon(UIConstants.ICON_RESUME);
            pauseResume.setToolTipText("Resume Log");
            rightclickPopup.getComponent(0).setVisible(false);
            rightclickPopup.getComponent(1).setVisible(true);
        } else {
            state = RESUMED;
            pauseResume.setIcon(UIConstants.ICON_PAUSE);
            pauseResume.setToolTipText("Pause Log");
            rightclickPopup.getComponent(0).setVisible(true);
            rightclickPopup.getComponent(1).setVisible(false);
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

    public int getCurrentServerLogSize() {
        return currentServerLogSize;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        logSizeTextField = new javax.swing.JTextField();
        logSizeText = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        logTable = null;
        clearLog = new com.mirth.connect.client.ui.components.IconButton();
        logSizeChange = new com.mirth.connect.client.ui.components.IconButton();
        pauseResume = new com.mirth.connect.client.ui.components.IconButton();

        logSizeTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        logSizeTextField.setMaximumSize(new java.awt.Dimension(45, 19));
        logSizeTextField.setMinimumSize(new java.awt.Dimension(45, 19));
        logSizeTextField.setPreferredSize(new java.awt.Dimension(45, 19));

        logSizeText.setText("Log Size:");

        jScrollPane1.setViewportView(logTable);

        clearLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearLogActionPerformed(evt);
            }
        });

        logSizeChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logSizeChangeActionPerformed(evt);
            }
        });

        pauseResume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseResumeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pauseResume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearLog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 316, Short.MAX_VALUE)
                .addComponent(logSizeText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logSizeChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(clearLog, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(logSizeChange, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(logSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(logSizeText)))
                    .addComponent(pauseResume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void clearLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearLogActionPerformed
        // "clear log" only affects on the client side.
        // because clearing log on one client should NOT affect other clients' logs.
        // clear logs on client side only.
        serverLogClient.clearLog();
    }//GEN-LAST:event_clearLogActionPerformed

    private void logSizeChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logSizeChangeActionPerformed
        // NOTE: the log size on the server is always 100, which is max. because if there are multiple clients connected to the same server,
        //  it has to be able to support the maximum allowed in case some client has it set at 99.
        // i.e. this log size change only affects on the client side.
        if (logSizeTextField.getText().length() == 0) {
            parent.alertWarning(this, "Please enter a valid number.");
            return;
        }
        int newServerLogSize = Integer.parseInt(logSizeTextField.getText());
        if (newServerLogSize != currentServerLogSize) {
            if (newServerLogSize <= 0) {
                parent.alertWarning(this, "Please enter a log size that is larger than 0.");
            } else {
                userPreferences.putInt("serverLogSize", newServerLogSize);
                currentServerLogSize = newServerLogSize;
                serverLogClient.resetServerLogSize(newServerLogSize);
            }
        }
    }//GEN-LAST:event_logSizeChangeActionPerformed

    private void pauseResumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseResumeActionPerformed
        adjustPauseResumeButton();
    }//GEN-LAST:event_pauseResumeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.IconButton clearLog;
    private javax.swing.JScrollPane jScrollPane1;
    private com.mirth.connect.client.ui.components.IconButton logSizeChange;
    private javax.swing.JLabel logSizeText;
    private javax.swing.JTextField logSizeTextField;
    private com.mirth.connect.client.ui.components.MirthTable logTable;
    private com.mirth.connect.client.ui.components.IconButton pauseResume;
    // End of variables declaration//GEN-END:variables
}

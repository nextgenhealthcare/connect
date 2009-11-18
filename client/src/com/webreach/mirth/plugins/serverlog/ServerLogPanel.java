/*
 * ServerLogPanel.java
 *
 * Created on October 15, 2007, 11:30 PM
 */

package com.webreach.mirth.plugins.serverlog;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.prefs.Preferences;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.RefreshTableModel;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import com.webreach.mirth.client.ui.components.MirthTable;

/**
 *
 * @author  chrisr
 */

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
    public ServerLogPanel(ServerLogClient serverLogClient)
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        this.serverLogClient = serverLogClient;
        
        initComponents();
        initLayouts();

        clearLog.setIcon(UIConstants.CLEAR_LOG_ICON);
        clearLog.setToolTipText("Clear Displayed Log");

        logSizeChange.setIcon(UIConstants.CHANGE_LOGSIZE_ICON);
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
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(2, 2, 2)
                .add(pauseResume, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(2, 2, 2)
                .add(clearLog, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 336, Short.MAX_VALUE)
                .add(logSizeText)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(logSizeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(2, 2, 2)
                .add(logSizeChange, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(2, 2, 2))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                .add(2, 2, 2)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(pauseResume, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(clearLog, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(logSizeChange, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(logSizeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(logSizeText))
                .add(2, 2, 2))
        );
    }


    /**
     * Makes the status table with all current server information.
     */
    public void makeLogTextArea()
    {
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
        logTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                if (logTable.rowAtPoint(new Point(evt.getX(), evt.getY())) == -1) {
                    return;
                }
                
                if (evt.getClickCount() >= 2)
                {
                    // synchronizing this to prevent ArrayIndexOutOfBoundsException since the server log table is constantly being redrawn.
                    synchronized (this) {
                        new ViewServerLogContentDialog(parent, (String) logTable.getModel().getValueAt(logTable.convertRowIndexToModel(logTable.getSelectedRow()), 1));
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
        menuItem.setIcon(UIConstants.PAUSE_LOG_ICON);
        menuItem.addActionListener(new PauseResumeActionListener());
        rightclickPopup.add(menuItem);
        menuItem = new JMenuItem("Resume Log");
        menuItem.setIcon(UIConstants.RESUME_LOG_ICON);
        menuItem.addActionListener(new PauseResumeActionListener());
        rightclickPopup.add(menuItem);
        rightclickPopup.addSeparator();
        menuItem = new JMenuItem("Clear Log");
        menuItem.setIcon(UIConstants.CLEAR_LOG_ICON);
        menuItem.addActionListener(new ClearLogActionListener());
        rightclickPopup.add(menuItem);

        // initially show 'Pause', hide 'Resume'
        adjustPauseResumeButton();

        //Add listener to the text area so the popup menu can come up.
        MouseListener popupListener = new PopupListener(rightclickPopup);
        jScrollPane1.addMouseListener(popupListener);
        logTable.addMouseListener(popupListener); 
    }


    class PauseResumeActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e) {
            adjustPauseResumeButton();
        }
    }

    class ClearLogActionListener implements ActionListener
    {
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
    public synchronized void updateTable(LinkedList<String[]> serverLogs)
    {
        Object[][] tableData;
        if (serverLogs != null)
        {
            tableData = new Object[serverLogs.size()][2];
            for (int i=0; i < serverLogs.size(); i++) {
                tableData[i][0] = serverLogs.get(i)[0];       // Id (hidden) - used to keep track of which log entries are sent new.
                tableData[i][1] = serverLogs.get(i)[1];       // Log Information
            }
        } else {
            tableData = new Object[0][2];
        }

        if (logTable != null)
        {
            RefreshTableModel model = (RefreshTableModel) logTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            logTable = new MirthTable();
            logTable.setModel(new RefreshTableModel(tableData, new String[] { ID_COLUMN_HEADER, LOG_INFO_COLUMN_HEADER })
            {
                boolean[] canEdit = new boolean[] { false, false };

                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
        }

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
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
            pauseResume.setIcon(UIConstants.RESUME_LOG_ICON);
            pauseResume.setToolTipText("Resume Log");
            rightclickPopup.getComponent(0).setVisible(false);
            rightclickPopup.getComponent(1).setVisible(true);
        } else {
            state = RESUMED;
            pauseResume.setIcon(UIConstants.PAUSE_LOG_ICON);
            pauseResume.setToolTipText("Pause Log");
            rightclickPopup.getComponent(0).setVisible(true);
            rightclickPopup.getComponent(1).setVisible(false);
        }
    }

    class PopupListener extends MouseAdapter
    {
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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(2, 2, 2)
                .add(pauseResume, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(2, 2, 2)
                .add(clearLog, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 336, Short.MAX_VALUE)
                .add(logSizeText)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(logSizeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(2, 2, 2)
                .add(logSizeChange, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(2, 2, 2))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                .add(0, 0, 0)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pauseResume, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(clearLog, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(logSizeChange, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(logSizeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(logSizeText)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void pauseResumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseResumeActionPerformed
        adjustPauseResumeButton();
    }//GEN-LAST:event_pauseResumeActionPerformed

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
        if (logSizeTextField.getText().length() == 0)
        {
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
    private com.webreach.mirth.client.ui.components.MirthTable logTable;
    private javax.swing.JButton pauseResume;
    // End of variables declaration//GEN-END:variables

}

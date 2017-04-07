/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.globalmapviewer;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.ViewContentDialog;
import com.mirth.connect.client.ui.components.MirthTable;

public class GlobalMapPanel extends JPanel {

    public GlobalMapPanel() {
        initComponents();
    }

    private String selectedMap;
    private String selectedVar;

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);
        setLayout(new MigLayout("fill, insets 0"));

        mapTable = new MirthTable();
        mapTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        mapTable.getTableHeader().setReorderingAllowed(false);
        mapTable.setSortable(false);
        mapTable.setEditable(false);
        mapTable.setFocusable(false);
        mapTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mapTable.setModel(new RefreshTableModel(new String[][] {}, new String[] { "Channel", "Key",
                "Value" }));
        mapTable.addMouseListener(new MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = mapTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                if (row == -1) {
                    return;
                }

                if (evt.getClickCount() >= 2) {
                    new ViewContentDialog((String) mapTable.getModel().getValueAt(mapTable.convertRowIndexToModel(row), 2));
                }
            }
        });

        mapTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (mapTable.getSelectedRow() != -1) {
                    selectedMap = (String) mapTable.getValueAt(mapTable.getSelectedRow(), 0);
                    selectedVar = (String) mapTable.getValueAt(mapTable.getSelectedRow(), 1);
                } else {
                    selectedMap = null;
                    selectedVar = null;
                }
            }
        });

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            mapTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        mapScrollPane = new JScrollPane();
        mapScrollPane.setViewportView(mapTable);

        add(mapScrollPane, "grow");
    }

    public synchronized void updateTable(Vector<Object> data, int selectedRow) {
        RefreshTableModel model = (RefreshTableModel) mapTable.getModel();
        model.refreshDataVector(data);

        //Re-select the row that was selected before the new vector was created
        if (selectedRow > 0 && selectedRow <= mapTable.getRowCount()) {
            mapTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
        }
    }

    public String getSelectedMap() {
        return this.selectedMap;
    }

    public String getSelectedVar() {
        return this.selectedVar;
    }

    private JXTable mapTable;
    private JScrollPane mapScrollPane;
}

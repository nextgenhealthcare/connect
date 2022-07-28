/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.util;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.CellData;
import com.mirth.connect.client.ui.ChannelPanel;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.connectors.jdbc.DatabaseReceiverProperties;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.User;

import net.miginfocom.swing.MigLayout;

public class PortUsageDialog extends MirthDialog {
	
    private List<DriverInfo> portsInUse;
    private boolean saved;
    
    public PortUsageDialog(Window owner) {
        super(owner, "Ports in Use", true);
        if (portsInUse == null) {
        	portsInUse = new ArrayList<DriverInfo>();
        } else {
        	portsInUse = new ArrayList<DriverInfo>(portsInUse);
        }
        if (portsInUse.size() > 0) {
            if (StringUtils.equals(portsInUse.get(0).getName(), DatabaseReceiverProperties.DRIVER_DEFAULT)) {
            	portsInUse.remove(0);
            }
            if (StringUtils.equals(portsInUse.get(portsInUse.size() - 1).getName(), DatabaseReceiverProperties.DRIVER_CUSTOM)) {
            	portsInUse.remove(portsInUse.size() - 1);
            }
        }
        this.portsInUse = portsInUse;
        
        initComponents();
        initToolTips();
        initLayout();
        setPorts(portsInUse);
        
        setPreferredSize(new Dimension(250, 216));
        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

	private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);
        getContentPane().setBackground(getBackground());

        portsTable = new MirthTable();
        portsTable.setModel(new RefreshTableModel(new Object[] { "Port", "Channel Name", "Status" }, 0));
        portsTable.setDragEnabled(false);
        portsTable.setRowSelectionAllowed(true);
        portsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        portsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        portsTable.setFocusable(true);
        portsTable.setOpaque(true);
        portsTable.getTableHeader().setReorderingAllowed(true);
        portsTable.setEditable(false);
        portsTable.setSortable(true);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            portsTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        portsTable.getColumnExt(0).setPreferredWidth(25);
        portsTable.getColumnExt(1).setPreferredWidth(200);
        portsTable.getColumnExt(2).setPreferredWidth(25);

        portsScrollPane = new JScrollPane(portsTable);

        separator = new JSeparator();

        closeButton = new JButton("Close");
        closeButton.addActionListener(evt -> close());
        getRootPane().registerKeyboardAction(evt -> close(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // fill the table with data
//        Map portsInUse = getChannelPortsUsed();
	}	


    private void initToolTips() {
        portsTable.getColumnExt(0).setToolTipText("<html>The port number in use.</html>");
        portsTable.getColumnExt(1).setToolTipText("<html>The channel name to which the port is assigned.</html>");
        portsTable.getColumnExt(2).setToolTipText("<html>The status of the port, such as deployed, undeployed, or disabled.</html>");
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill"));
        add(portsScrollPane, "grow, push");
        add(separator, "newline, sx, growx");
        add(closeButton, "right");
    }
    
    private void setPorts(List<DriverInfo> ports) {
//        if (ports == null) {
//        	ports = new ArrayList<DriverInfo>();
//        }
//
//        Object[][] data = new Object[ports.size()][4];
//
//        for (int i = 0; i < ports.size(); i++) {
//            DriverInfo info = ports.get(i);
//            data[i][0] = StringUtils.trim(StringUtils.defaultString(info.getPort()));
//            data[i][1] = StringUtils.trim(StringUtils.defaultString(info.getName()));
//            data[i][2] = StringUtils.trim(StringUtils.defaultString(info.getStatus()));
//
//            String alternativeClassNamesStr = "";
//            List<String> alternativeClassNames = info.getAlternativeClassNames();
//            if (CollectionUtils.isNotEmpty(alternativeClassNames)) {
//                alternativeClassNamesStr = StringUtils.join(alternativeClassNames, ',');
//            }
//            data[i][4] = alternativeClassNamesStr;
//        }
//
//        ((RefreshTableModel) portsTable.getModel()).refreshDataVector(data);
    }

    private void close() {
            dispose();
    }

    private MirthTable portsTable;
    private JScrollPane portsScrollPane;
    private JSeparator separator;
    private JButton closeButton;
}

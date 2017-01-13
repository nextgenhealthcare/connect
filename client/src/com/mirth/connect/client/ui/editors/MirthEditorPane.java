/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.model.Connector;

public abstract class MirthEditorPane extends JPanel {
    // transformer constants

    public static final int STEP_NUMBER_COL = 0;
    public static final int STEP_NAME_COL = 1;
    public static final int STEP_TYPE_COL = 2;
    public static final int STEP_DATA_COL = 3;
    // filter constants
    public static final int RULE_NUMBER_COL = 0;
    public static final int RULE_OP_COL = 1;
    public static final int RULE_NAME_COL = 2;
    public static final int RULE_TYPE_COL = 3;
    public static final int RULE_DATA_COL = 4;
    // a list of panels to load
    public static final String BLANK_TYPE = "";
    public static final String MESSAGE_TYPE = "Message Builder";
    public static final String MAPPER_TYPE = "Mapper";
    public static final String JAVASCRIPT_TYPE = "JavaScript";
    public int prevSelRow = -1;
    public boolean updating = false;
    public boolean modified = false;
    public Frame parent = PlatformUI.MIRTH_FRAME;
    public JScrollPane referenceScrollPane;
    public JPanel refPanel;
    public TabbedTemplatePanel tabTemplatePanel;

    public void updateTaskPane(String type) {}

    public MirthEditorPane() {
        super();
        this.setBorder(BorderFactory.createEmptyBorder());
        tabTemplatePanel = new TabbedTemplatePanel();
        tabTemplatePanel.setBorder(BorderFactory.createEmptyBorder());
        refPanel = new JPanel();
        refPanel.setBorder(BorderFactory.createEmptyBorder());
        refPanel.setLayout(new BorderLayout());
        refPanel.add(tabTemplatePanel, BorderLayout.CENTER);
        // let the parent decide how big this should be
        this.setPreferredSize(new Dimension(0, 0));
    }

    public abstract Connector getConnector();

    public int getSelectedRow() {
        return 0;
    }

    public DefaultTableModel getTableModel() {
        return null;
    }
}

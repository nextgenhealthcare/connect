/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import java.util.Set;

import com.mirth.connect.client.ui.FunctionList;
import com.mirth.connect.client.ui.VariableListHandler;
import com.mirth.connect.client.ui.panels.reference.VariableReferenceTable;
import com.mirth.connect.model.CodeTemplate.ContextType;
import com.mirth.connect.model.datatype.DataTypeProperties;

public class TabbedTemplatePanel extends javax.swing.JPanel {

    private MirthEditorPane parent;

    /** Creates new form TabbedTemplatePanel */
    public TabbedTemplatePanel(MirthEditorPane p) {
        parent = p;
        initComponents();
        messageTemplatePanel.setInboundTreePanel(messageTreePanel.getInboundTreePanel());
        messageTemplatePanel.setOutboundTreePanel(messageTreePanel.getOutboundTreePanel());

        // ArrayList<ReferenceListItem> functionListItems = new
        // ReferenceListBuilder().getVariableListItems();
        variableTable = new VariableReferenceTable("Available Variables", new String[]{});
        variableTable.setDragEnabled(true);
        variableTable.setTransferHandler(new VariableListHandler("$('", "')"));
        variableListScrollPane.setViewportView(variableTable);
    }

    public void setFilterView() {
        messageTreePanel.hideOutbound();
        messageTemplatePanel.hideOutbound();

        messageTreePanel.getInboundTreePanel().setFilterView();
        messageTreePanel.getOutboundTreePanel().setFilterView();
    }

    public void setTransformerView() {
        messageTreePanel.showOutbound();
        messageTemplatePanel.showOutbound();

        messageTreePanel.getInboundTreePanel().setTransformerView();
        messageTreePanel.getOutboundTreePanel().setTransformerView();
    }
    
    /**
     * Sets the the inbound and outbound data types and properties to be 
     * enabled.  The inbound data type may be disabled if XML is required.
     */
    public void setSourceView() {
        boolean inboundEnabled = true;
        if (parent.parent.channelEditPanel.requiresXmlDataType()) {
            inboundEnabled = false;
        }
        messageTemplatePanel.setDataTypeEnabled(inboundEnabled, true, true, true);
    }
    
    /**
     * Sets the inbound data type and properties to be disabled and
     * the outbound data type and proeprties to be enabled.
     */
    public void setDestinationView() {
        messageTemplatePanel.setDataTypeEnabled(false, false, true, true);
    }

    public void resizePanes() {
        variableSplitPane.setDividerLocation(.5);
        variableSplitPane.setResizeWeight(.5);
        messageTreePanel.resizePanes();
        messageTemplatePanel.resizePanes();
    }

    public void updateVariables(Set<String> rules, Set<String> steps) {
        if (rules != null && steps != null) {
            rules.addAll(steps);
        }
        variableTable.updateVariables(rules);
    }

    public String getIncomingMessage() {
        return messageTemplatePanel.getInboundMessage();
    }

    public void setIncomingMessage(String msg) {
        messageTemplatePanel.setInboundMessage(msg);
    }

    public String getOutgoingMessage() {
        return messageTemplatePanel.getOutboundMessage();
    }

    public void setOutgoingMessage(String msg) {
        messageTemplatePanel.setOutboundMessage(msg);
    }

    public void setIncomingDataType(String dataType) {
        messageTemplatePanel.setInboundDataType(dataType);
    }

    public void setOutgoingDataType(String dataType) {
        messageTemplatePanel.setOutboundDataType(dataType);
    }

    public String getIncomingDataType() {
        return messageTemplatePanel.getInboundDataType();
    }

    public String getOutgoingDataType() {
        return messageTemplatePanel.getOutboundDataType();
    }

    public void setIncomingDataProperties(DataTypeProperties properties) {
        messageTemplatePanel.setInboundDataProperties(properties);
    }

    public void setOutgoingDataProperties(DataTypeProperties properties) {
        messageTemplatePanel.setOutboundDataProperties(properties);
    }

    public DataTypeProperties getIncomingDataProperties() {
        return messageTemplatePanel.getInboundDataProperties();
    }

    public DataTypeProperties getOutgoingDataProperties() {
        return messageTemplatePanel.getOutboundDataProperties();
    }

    public void setDefaultComponent() {
        tabPanel.setSelectedIndex(0);
        functionList.setDefaultDropDownValue();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabPanel = new javax.swing.JTabbedPane();
        variableTab = new javax.swing.JPanel();
        variableSplitPane = new javax.swing.JSplitPane();
        functionList = new FunctionList(ContextType.MESSAGE_CONTEXT.getContext());
        variableListScrollPane = new javax.swing.JScrollPane();
        variableTable = new com.mirth.connect.client.ui.panels.reference.VariableReferenceTable();
        treeTab = new javax.swing.JPanel();
        messageTreePanel = new com.mirth.connect.client.ui.editors.MessageTreePanel();
        messageTab = new javax.swing.JPanel();
        messageTemplatePanel = new MessageTemplatePanel(parent);

        variableTab.setBackground(new java.awt.Color(255, 255, 255));

        variableSplitPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        variableSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        functionList.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        variableSplitPane.setLeftComponent(functionList);

        variableListScrollPane.setViewportView(variableTable);

        variableSplitPane.setRightComponent(variableListScrollPane);

        javax.swing.GroupLayout variableTabLayout = new javax.swing.GroupLayout(variableTab);
        variableTab.setLayout(variableTabLayout);
        variableTabLayout.setHorizontalGroup(
            variableTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(variableSplitPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
        );
        variableTabLayout.setVerticalGroup(
            variableTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, variableTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(variableSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE))
        );

        tabPanel.addTab("Reference", variableTab);

        treeTab.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout treeTabLayout = new javax.swing.GroupLayout(treeTab);
        treeTab.setLayout(treeTabLayout);
        treeTabLayout.setHorizontalGroup(
            treeTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(messageTreePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
        );
        treeTabLayout.setVerticalGroup(
            treeTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(messageTreePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
        );

        tabPanel.addTab("Message Trees", treeTab);

        messageTab.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout messageTabLayout = new javax.swing.GroupLayout(messageTab);
        messageTab.setLayout(messageTabLayout);
        messageTabLayout.setHorizontalGroup(
            messageTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(messageTemplatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 257, Short.MAX_VALUE)
        );
        messageTabLayout.setVerticalGroup(
            messageTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(messageTemplatePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
        );

        tabPanel.addTab("Message Templates", messageTab);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.FunctionList functionList;
    public javax.swing.JPanel messageTab;
    private com.mirth.connect.client.ui.editors.MessageTemplatePanel messageTemplatePanel;
    private com.mirth.connect.client.ui.editors.MessageTreePanel messageTreePanel;
    public javax.swing.JTabbedPane tabPanel;
    public javax.swing.JPanel treeTab;
    private javax.swing.JScrollPane variableListScrollPane;
    private javax.swing.JSplitPane variableSplitPane;
    private javax.swing.JPanel variableTab;
    private com.mirth.connect.client.ui.panels.reference.VariableReferenceTable variableTable;
    // End of variables declaration//GEN-END:variables
}

/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

import com.mirth.connect.client.ui.FunctionList;
import com.mirth.connect.client.ui.TransformerType;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.VariableListHandler;
import com.mirth.connect.client.ui.VariableListHandler.TransferMode;
import com.mirth.connect.client.ui.panels.reference.VariableReferenceTable;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.model.datatype.DataTypeProperties;

public class TabbedTemplatePanel extends JPanel {

    private MirthEditorPane editorPane;
    private ContextType contextType;

    public TabbedTemplatePanel(MirthEditorPane editorPane) {
        this.editorPane = editorPane;
        initComponents();
        initLayout();
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
     * Sets the the inbound and outbound data types and properties to be enabled. The inbound data
     * type may be disabled if XML is required.
     */
    public void setSourceView() {
        boolean inboundEnabled = true;
        if (editorPane.parent.channelEditPanel.requiresXmlDataType()) {
            inboundEnabled = false;
        }
        messageTemplatePanel.setDataTypeEnabled(inboundEnabled, true, true, true, TransformerType.SOURCE);
        setContextType(ContextType.SOURCE_FILTER_TRANSFORMER);
    }

    /**
     * Sets the inbound data type and properties to be disabled and the outbound data type and
     * proeprties to be enabled.
     */
    public void setDestinationView(boolean isResponse) {
        messageTemplatePanel.setDataTypeEnabled(isResponse, true, true, true, isResponse ? TransformerType.RESPONSE : TransformerType.DESTINATION);
        setContextType(isResponse ? ContextType.DESTINATION_RESPONSE_TRANSFORMER : ContextType.DESTINATION_FILTER_TRANSFORMER);
    }

    public void resizePanes() {
        referenceSplitPane.setDividerLocation(.5);
        referenceSplitPane.setResizeWeight(.5);
        messageTreePanel.resizePanes();
        messageTemplatePanel.resizePanes();
    }

    public void updateVariables(Set<String> rules, Set<String> steps) {
        if (rules != null && steps != null) {
            rules.addAll(steps);
        }
        variableTable.updateVariables(rules);
    }

    public void populateConnectors(List<Connector> connectors) {
        ((VariableListHandler) variableTable.getTransferHandler()).populateConnectors(connectors);
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
        tabbedPane.setSelectedIndex(0);
        functionListMap.get(contextType).setDefaultDropDownValue();
    }

    public void setContextType(ContextType contextType) {
        this.contextType = contextType;

        FunctionList functionList = functionListMap.get(contextType);
        if (functionList == null) {
            functionList = new FunctionList(contextType);
            functionList.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            functionListMap.put(contextType, functionList);
        }

        referenceSplitPane.setTopComponent(functionList);
    }

    public MessageTemplatePanel getMessageTemplatePanel() {
        return messageTemplatePanel;
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();

        referencePanel = new JPanel();
        referencePanel.setBackground(UIConstants.BACKGROUND_COLOR);

        referenceSplitPane = new JSplitPane();
        referenceSplitPane.setBackground(referencePanel.getBackground());
        referenceSplitPane.setBorder(null);
        referenceSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

        functionListMap = new HashMap<ContextType, FunctionList>();

        variableTable = new VariableReferenceTable("Available Variables", new String[] {});
        variableTable.setDragEnabled(true);
        variableTable.setTransferHandler(new VariableListHandler(TransferMode.JAVASCRIPT));

        variableListScrollPane = new JScrollPane(variableTable);

        messageTreePanel = new MessageTreePanel();

        messageTemplatePanel = new MessageTemplatePanel(editorPane);
        messageTemplatePanel.setInboundTreePanel(messageTreePanel.getInboundTreePanel());
        messageTemplatePanel.setOutboundTreePanel(messageTreePanel.getOutboundTreePanel());
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));

        referencePanel.setLayout(new MigLayout("insets 12 0 0 0, novisualpadding, hidemode 3, fill"));
        referenceSplitPane.setBottomComponent(variableListScrollPane);
        referencePanel.add(referenceSplitPane, "grow");

        tabbedPane.addTab("Reference", referencePanel);
        tabbedPane.addTab("Message Trees", messageTreePanel);
        tabbedPane.addTab("Message Templates", messageTemplatePanel);
        add(tabbedPane, "grow");
    }

    public JTabbedPane tabbedPane;
    private JPanel referencePanel;
    private JSplitPane referenceSplitPane;
    private Map<ContextType, FunctionList> functionListMap;
    private JScrollPane variableListScrollPane;
    private VariableReferenceTable variableTable;
    private MessageTreePanel messageTreePanel;
    private MessageTemplatePanel messageTemplatePanel;
}

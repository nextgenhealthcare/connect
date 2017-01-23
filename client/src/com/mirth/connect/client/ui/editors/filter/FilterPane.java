/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors.filter;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.RuleDropData;
import com.mirth.connect.client.ui.TreeTransferable;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellEditor;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellRenderer;
import com.mirth.connect.client.ui.components.MirthTree;
import com.mirth.connect.client.ui.editors.BaseEditorPane;
import com.mirth.connect.client.ui.util.VariableListUtil;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.Rule;
import com.mirth.connect.model.Rule.Operator;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.plugins.FilterTransformerTypePlugin;

public class FilterPane extends BaseEditorPane<Filter, Rule> {

    public static final String RULE_BUILDER = "Rule Builder";

    private String originalInboundDataType;
    private DataTypeProperties originalInboundDataTypeProperties;
    private String originalInboundTemplate;

    @Override
    protected Class<?> getContainerClass() {
        return Filter.class;
    }

    @Override
    protected String getContainerName() {
        return "Filter";
    }

    @Override
    protected String getElementName() {
        return "Rule";
    }

    @Override
    protected boolean allowCellEdit(int rowIndex, int columnIndex) {
        if (columnIndex == operatorColumn) {
            return rowIndex > 0;
        }
        return super.allowCellEdit(rowIndex, columnIndex);
    }

    @Override
    protected void onTableLoad() {
        table.getColumnExt(operatorColumn).setMaxWidth(UIConstants.MAX_WIDTH);
        table.getColumnExt(operatorColumn).setPreferredWidth(60);

        Operator[] operatorValues = new Operator[] { Operator.AND, Operator.OR };
        table.getColumnExt(operatorColumn).setCellRenderer(new MirthComboBoxTableCellRenderer(operatorValues));
        table.getColumnExt(operatorColumn).setCellEditor(new MirthComboBoxTableCellEditor(table, operatorValues, 2, true, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                updateOperations();
            }
        }));
    }

    @Override
    protected void updateTable() {
        updateOperations();
    }

    private void updateOperations() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (i == 0) {
                tableModel.setValueAt(null, i, operatorColumn);
            } else if (tableModel.getValueAt(i, operatorColumn) == null) {
                tableModel.setValueAt(Operator.AND, i, operatorColumn);
            }
        }
    }

    @Override
    protected boolean useOperatorColumn() {
        return true;
    }

    @Override
    protected Object getOperator(Rule element) {
        return element.getOperator();
    }

    @Override
    protected void setOperator(Rule element, Object value) {
        element.setOperator((Operator) value);
    }

    @Override
    protected Map<String, FilterTransformerTypePlugin<Rule>> getPlugins() {
        return new TreeMap<String, FilterTransformerTypePlugin<Rule>>(LoadedExtensions.getInstance().getFilterRulePlugins());
    }

    @Override
    protected void doAccept(Connector connector, Filter properties, boolean response) {
        connector.setFilter(properties);
        connector.getTransformer().setInboundDataType(getInboundDataType());
        connector.getTransformer().setInboundProperties(getInboundDataTypeProperties());
        connector.getTransformer().setInboundTemplate(getInboundTemplate());
    }

    @Override
    public Filter getProperties() {
        Filter props = new Filter();
        props.setElements(getElements());
        return props;
    }

    @Override
    public void doSetProperties(Connector connector, Filter properties, boolean response, boolean overwriteOriginal) {
        if (overwriteOriginal) {
            originalInboundDataType = connector.getTransformer().getInboundDataType();
            originalInboundDataTypeProperties = connector.getTransformer().getInboundProperties().clone();
            originalInboundTemplate = connector.getTransformer().getInboundTemplate();
        }
        setElements(properties.getElements());
        setInboundDataType(connector.getTransformer().getInboundDataType());
        setInboundDataTypeProperties(connector.getTransformer().getInboundProperties());
        setInboundTemplate(connector.getTransformer().getInboundTemplate());
        templatePanel.setFilterView();
    }

    @Override
    protected boolean isModified(Filter properties) {
        return super.isModified(properties) || !StringUtils.equals(originalInboundDataType, getInboundDataType()) || !Objects.equals(originalInboundDataTypeProperties, getInboundDataTypeProperties()) || !StringUtils.equals(originalInboundTemplate, getInboundTemplate());
    }

    @Override
    public void addNewElement() {
        addNewElement("", "", "", RULE_BUILDER);
    }

    @Override
    protected void getRuleVariables(Connector connector, Set<String> concatenatedRules, boolean includeLocalVars) {
        VariableListUtil.getRuleVariables(concatenatedRules, getProperties(), includeLocalVars);
    }

    @Override
    protected void getStepVariables(Connector connector, Set<String> concatenatedSteps, boolean includeLocalVars, int viewRow) {
        VariableListUtil.getStepVariables(concatenatedSteps, connector.getTransformer(), includeLocalVars, viewRow);
    }

    @Override
    protected boolean handleDragEnter(DropTargetDragEvent dtde, Transferable tr) throws UnsupportedFlavorException, IOException {
        if (tr.isDataFlavorSupported(TreeTransferable.RULE_DATA_FLAVOR)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            return true;
        }
        return false;
    }

    @Override
    protected void handleDrop(DropTargetDropEvent dtde, Transferable tr) throws UnsupportedFlavorException, IOException {
        if (tr.isDataFlavorSupported(TreeTransferable.RULE_DATA_FLAVOR)) {
            Object transferData = tr.getTransferData(TreeTransferable.RULE_DATA_FLAVOR);

            if (transferData instanceof RuleDropData) {
                RuleDropData data = (RuleDropData) transferData;
                addNewElement(MirthTree.constructNodeDescription(data.getNode()), "", data.getMapping(), RULE_BUILDER);
            }
        }
    }
}
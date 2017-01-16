/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors.transformer;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.MapperDropData;
import com.mirth.connect.client.ui.MessageBuilderDropData;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.TreeTransferable;
import com.mirth.connect.client.ui.components.MirthTree;
import com.mirth.connect.client.ui.editors.BaseEditorPane;
import com.mirth.connect.client.ui.util.VariableListUtil;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.plugins.FilterTransformerTypePlugin;

public class TransformerPane extends BaseEditorPane<Transformer, Step> {

    public static final String MAPPER = "Mapper";
    public static final String MESSAGE_BUILDER = "Message Builder";

    @Override
    protected Class<?> getContainerClass() {
        return Transformer.class;
    }

    @Override
    protected String getContainerName() {
        return "Transformer";
    }

    @Override
    protected String getElementName() {
        return "Step";
    }

    @Override
    protected boolean useOperatorColumn() {
        return false;
    }

    @Override
    protected Object getOperator(Step element) {
        return null;
    }

    @Override
    protected void setOperator(Step element, Object value) {}

    @Override
    protected Map<String, FilterTransformerTypePlugin<Step>> getPlugins() {
        return new TreeMap<String, FilterTransformerTypePlugin<Step>>(LoadedExtensions.getInstance().getTransformerStepPlugins());
    }

    @Override
    protected void doAccept(Connector connector, Transformer properties, boolean response) {
        if (response) {
            connector.setResponseTransformer(properties);
        } else {
            connector.setTransformer(properties);
        }
    }

    @Override
    public Transformer getProperties() {
        Transformer props = new Transformer();
        props.setElements(getElements());
        props.setInboundTemplate(getInboundTemplate());
        props.setOutboundTemplate(getOutboundTemplate());
        props.setInboundDataType(getInboundDataType());
        props.setOutboundDataType(getOutboundDataType());
        props.setInboundProperties(getInboundDataTypeProperties());
        props.setOutboundProperties(getOutboundDataTypeProperties());
        return props;
    }

    @Override
    public void doSetProperties(Connector connector, Transformer properties, boolean response) {
        setElements(properties.getElements());
        setInboundDataType(properties.getInboundDataType());
        setOutboundDataType(properties.getOutboundDataType());
        setInboundDataTypeProperties(properties.getInboundProperties());
        setOutboundDataTypeProperties(properties.getOutboundProperties());
        setInboundTemplate(properties.getInboundTemplate());
        setOutboundTemplate(properties.getOutboundTemplate());
        templatePanel.setTransformerView();

        if (connector.getMetaDataId() == 0) {
            PlatformUI.MIRTH_FRAME.channelEditPanel.updateAttachmentHandler(properties.getInboundDataType());
        }
    }

    @Override
    public void addNewElement() {
        addNewElement("", "", "", MAPPER);
    }

    @Override
    protected void getRuleVariables(Connector connector, Set<String> concatenatedRules, boolean includeLocalVars) {
        VariableListUtil.getRuleVariables(concatenatedRules, connector.getFilter(), includeLocalVars);
    }

    @Override
    protected void getStepVariables(Connector connector, Set<String> concatenatedSteps, boolean includeLocalVars, int viewRow) {
        VariableListUtil.getStepVariables(concatenatedSteps, getProperties(), includeLocalVars, viewRow);
    }

    @Override
    protected void handleDrop(DropTargetDropEvent dtde, Transferable tr) throws UnsupportedFlavorException, IOException {
        if (tr.isDataFlavorSupported(TreeTransferable.MAPPER_DATA_FLAVOR) || tr.isDataFlavorSupported(TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR)) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

            Object mapperTransferData = tr.getTransferData(TreeTransferable.MAPPER_DATA_FLAVOR);
            Object messageBuilderTransferData = tr.getTransferData(TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR);

            if (mapperTransferData != null && !PlatformUI.MIRTH_FRAME.isAcceleratorKeyPressed()) {
                Object transferData = tr.getTransferData(TreeTransferable.MAPPER_DATA_FLAVOR);
                MapperDropData data = (MapperDropData) transferData;
                addNewElement(data.getVariable(), data.getVariable(), data.getMapping(), MAPPER);
            } else if (mapperTransferData != null && PlatformUI.MIRTH_FRAME.isAcceleratorKeyPressed()) {
                Object transferData = tr.getTransferData(TreeTransferable.MAPPER_DATA_FLAVOR);
                MapperDropData data2 = (MapperDropData) transferData;
                MessageBuilderDropData data = new MessageBuilderDropData(data2.getNode(), MirthTree.constructPath(data2.getNode().getParent(), "msg", "").toString(), "");
                addNewElement(MirthTree.constructMessageBuilderStepName(null, data.getNode()), data.getMessageSegment(), data.getMapping(), MESSAGE_BUILDER);
            } else if (messageBuilderTransferData != null) {
                Object transferData = tr.getTransferData(TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR);
                MessageBuilderDropData data = (MessageBuilderDropData) transferData;
                addNewElement(MirthTree.constructMessageBuilderStepName(null, data.getNode()), data.getMessageSegment(), data.getMapping(), MESSAGE_BUILDER);
            }
        }
    }
}
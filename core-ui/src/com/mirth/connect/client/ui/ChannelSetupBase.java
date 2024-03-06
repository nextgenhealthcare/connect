package com.mirth.connect.client.ui;

import java.util.Map;

import javax.swing.JPanel;

import com.mirth.connect.client.ui.editors.BaseEditorPaneBase;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.MessageStorageMode;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;

public abstract class ChannelSetupBase extends JPanel {

    public abstract int getDefaultQueueBufferSize();
    
    public abstract Channel getCurrentChannel();
    
    public abstract Map<Integer, Map<String, String>> getResourceIds();
    
    public abstract int getLastModelIndex();
    
    public abstract BaseEditorPaneBase<Transformer, Step> getTransformerPane();
    
    public abstract VariableList getDestinationVariableList();
    
    public abstract void decorateConnectorType(ConnectorTypeDecoration connectorTypeDecoration, boolean isDestination);
    
    public abstract void saveSourcePanel();
    
    public abstract void saveDestinationPanel();
    
    public abstract MessageStorageMode getMessageStorageMode();
    
    public abstract void updateQueueWarning(MessageStorageMode messageStorageMode);
    
    /** Sets the destination variable list from the transformer steps */
    public abstract void setDestinationVariableList();
    
    /**
     * Returns the required source data type of this channel.
     */
    public abstract String getRequiredInboundDataType();

    /**
     * Returns the required source data type of this channel.
     */
    public abstract String getRequiredOutboundDataType();

    /**
     * Returns the initial, or default, source inbound data type of this channel.
     */
    public abstract String getInitialInboundDataType();
    
    /**
     * Returns the required data type for the selected destination of this channel.
     */
    public abstract String getRequiredOutboundDestinationDataType();

    /**
     * Returns the initial, or default, inbound data type for the selected destination response of
     * this channel.
     */
    public abstract String getInitialInboundResponseDataType();

    /**
     * Returns the initial, or default, outbound data type for the selected destination response of
     * this channel.
     */
    public abstract String getInitialOutboundResponseDataType();
    
    /*
     * Set Data Types for source inbound and outbound which also means destination inbound
     */
    public abstract void checkAndSetSourceDataType();
    
    /**
     * Set Data types specified by selected destination for destination and response
     */
    public abstract void checkAndSetDestinationAndResponseDataType();
}

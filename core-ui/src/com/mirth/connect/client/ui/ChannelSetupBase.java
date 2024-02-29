package com.mirth.connect.client.ui;

import javax.swing.JPanel;

import com.mirth.connect.client.ui.editors.BaseEditorPaneBase;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.MessageStorageMode;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;

public abstract class ChannelSetupBase extends JPanel {

    public abstract int getDefaultQueueBufferSize();
    
    public abstract Channel getCurrentChannel();
    
    public abstract BaseEditorPaneBase<Transformer, Step> getTransformerPane();
    
    public abstract void decorateConnectorType(ConnectorTypeDecoration connectorTypeDecoration, boolean isDestination);
    
    public abstract void saveSourcePanel();
    
    public abstract void saveDestinationPanel();
    
    public abstract MessageStorageMode getMessageStorageMode();
    
    public abstract void updateQueueWarning(MessageStorageMode messageStorageMode);
}

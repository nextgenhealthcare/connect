package com.mirth.connect.client.ui.browsers.message;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import com.mirth.connect.client.core.PaginatedMessageList;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.model.filters.MessageFilter;

public abstract class MessageBrowserBase extends JPanel {

    protected abstract void initMetaDataColumns(MessageBrowserChannelModel channelModel);
    
    protected abstract Set<String> createCustomMetaDataColumns();
    
    protected abstract void taskPaneWhenLoadingChannels();
    
    protected abstract void taskPaneWhenClearingDescription();
    
    public abstract String getChannelId();
    
    public abstract List<String> getChannelIds();
    
    public abstract boolean getIsChannelMessagesPanelFirstLoadSearch();
    
    public abstract Map<Integer, String> getConnectors();
    
    public abstract List<MetaDataColumn> getMetaDataColumns();
    
    public abstract PaginatedMessageList getMessages();
    
    public abstract MessageFilter getMessageFilter();
    
    public abstract String getPatientId(Long messageId, Integer metaDataId, List<Integer> selectedMetaDataIds);
    
    public abstract int getPageSize();
    
    public abstract Message getSelectedMessage();
    
    public abstract ConnectorMessage getSelectedConnectorMessage();
    
    public abstract String getSelectedMessageChannelId();
    
    public abstract Long getSelectedMessageId();
    
    public abstract Integer getSelectedMetaDataId();
    
    public abstract String getSelectedAttachmentId();
    
    public abstract String getSelectedMimeType();
    
    public abstract boolean canReprocessMessage(Long messageId);
}

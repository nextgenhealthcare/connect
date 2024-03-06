package com.mirth.connect.client.ui;

import java.util.Map;
import java.util.Set;

import com.mirth.connect.model.ChannelDependency;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.ChannelTag;

public abstract class ChannelPanelBase extends AbstractFramePanel {

    public abstract Map<String, String> getCachedChannelIdsAndNames();

    public abstract Map<String, ChannelStatus> getCachedChannelStatuses();

    public abstract Map<String, ChannelGroupStatus> getCachedGroupStatuses();

    public abstract Set<ChannelDependency> getCachedChannelDependencies();

    public abstract Set<ChannelTag> getCachedChannelTags();

    public abstract String getUserTags();

    public abstract void doRefreshChannels();

    public abstract void doRefreshChannels(boolean queue);
    
    public abstract void retrieveGroups();
        
    public abstract void retrieveChannelIdsAndNames();

    public abstract void retrieveChannels();

    public abstract void retrieveChannels(boolean refreshTags);
    
    public abstract void retrieveDependencies();
    
    public abstract Map<String, ChannelHeader> getChannelHeaders();
}

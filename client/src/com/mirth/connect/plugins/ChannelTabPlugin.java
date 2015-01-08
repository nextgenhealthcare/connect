package com.mirth.connect.plugins;

import com.mirth.connect.client.ui.AbstractChannelTabPanel;

public abstract class ChannelTabPlugin extends ClientPlugin {
    public ChannelTabPlugin(String pluginName) {
        super(pluginName);
    }

    public abstract AbstractChannelTabPanel getChannelTabPanel();
    
    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}
}

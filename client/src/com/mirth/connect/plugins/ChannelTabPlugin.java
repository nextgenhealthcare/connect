/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

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

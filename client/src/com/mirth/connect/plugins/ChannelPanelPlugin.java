/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.util.List;

import javax.swing.JComponent;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.model.Channel;

public abstract class ChannelPanelPlugin extends ClientPlugin {

    public ChannelPanelPlugin(String name) {
        super(name);
    }

    public abstract JComponent getComponent();

    public void prepareData() throws ClientException {};

    public void prepareData(List<Channel> channels) throws ClientException {};

    // used for setting actions to be called for updating when there is no
    // channel selected
    public abstract void update();

    // used for setting actions to be called for updating when there is a channel
    // selected
    public abstract void update(List<Channel> channels);

}

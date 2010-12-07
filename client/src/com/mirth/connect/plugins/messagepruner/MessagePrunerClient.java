/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagepruner;

import com.mirth.connect.client.ui.AbstractSettingsPanel;
import com.mirth.connect.plugins.SettingsPanelPlugin;

public class MessagePrunerClient extends SettingsPanelPlugin {

    private AbstractSettingsPanel settingsPanel = null;

    public MessagePrunerClient(String name) {
        super(name);

        settingsPanel = new MessagePrunerPanel("Message Pruner", this);
    }

    @Override
    public AbstractSettingsPanel getSettingsPanel() {
        return settingsPanel;
    }
}

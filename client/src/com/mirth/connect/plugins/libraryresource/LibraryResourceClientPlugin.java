/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.libraryresource;

import com.mirth.connect.client.ui.ResourcePropertiesPanel;
import com.mirth.connect.plugins.ResourceClientPlugin;

public class LibraryResourceClientPlugin extends ResourceClientPlugin {

    private ResourcePropertiesPanel settingsPanel;

    public LibraryResourceClientPlugin(String pluginName) {
        super(pluginName);
        settingsPanel = new LibraryResourcePropertiesPanel();
    }

    @Override
    public String getType() {
        return LibraryResourceProperties.TYPE;
    }

    @Override
    public ResourcePropertiesPanel getPropertiesPanel() {
        return settingsPanel;
    }

    @Override
    public String getPluginPointName() {
        return LibraryResourceProperties.PLUGIN_POINT;
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}
}
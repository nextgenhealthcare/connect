/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.globalmapviewer;

import static com.mirth.connect.plugins.globalmapviewer.GlobalMapServletInterface.PERMISSION_VIEW;
import static com.mirth.connect.plugins.globalmapviewer.GlobalMapServletInterface.PLUGIN_POINT;

import java.util.Properties;

import com.mirth.connect.client.core.api.util.OperationUtil;
import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.plugins.ServicePlugin;

public class GlobalMapProvider implements ServicePlugin {

    @Override
    public String getPluginPointName() {
        return PLUGIN_POINT;
    }

    public synchronized Object invoke(String method, Object object, String sessionId) {
        return null;
    }

    public Properties getDefaultProperties() {
        return new Properties();
    }

    @Override
    public ExtensionPermission[] getExtensionPermissions() {
        ExtensionPermission viewPermission = new ExtensionPermission(PLUGIN_POINT, PERMISSION_VIEW, "Displays the contents of the global map and global channel maps on the Dashboard.", OperationUtil.getOperationNamesForPermission(PERMISSION_VIEW, GlobalMapServletInterface.class), new String[] {});
        return new ExtensionPermission[] { viewPermission };
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void update(Properties properties) {}

    @Override
    public void init(Properties properties) {}
}

/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.util.Properties;

import com.mirth.connect.model.ExtensionPermission;

public interface ServicePlugin extends ServerPlugin {
    public void update(Properties properties);

    public void onDeploy();

    public Object invoke(String method, Object object, String sessionId) throws Exception;

    /**
     * Returns the default properties for this plugin, or an empty Properties if
     * there are none.
     * 
     * @return
     */
    public Properties getDefaultProperties();

    /**
     * Returns permissions for this plugin so they can be initialized on
     * startup.
     * 
     * @return
     */
    public ExtensionPermission[] getExtensionPermissions();
}

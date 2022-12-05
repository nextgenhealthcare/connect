/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 *
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.util.Map;
import java.util.Properties;

import com.mirth.connect.model.ExtensionPermission;

public interface ServicePlugin extends ServerPlugin {
    /**
     * Called when a plugin is being loaded.
     *
     * The properties for the plugin are first loaded from the database
     * and merged with those obtained from {@link #getDefaultProperties},
     * with the properties from the database taking precedence.
     *
     * Once the stored-properties are merged with the defaults, the resulting
     * properties are persisted to the database.
     *
     * @param properties The properties of the plugin.
     */
    public void init(Properties properties);

    /**
     * Called when a plugin's properties are being updated by some component
     * in the system via a call to {@link com.mirth.connect.server.controllers.ExtensionController.updatePluginProperties(String, Properties)}.
     *
     * @param properties The new properties for the plugin.
     */
    public void update(Properties properties);

    /**
     * Returns the default properties for this plugin, or an empty Properties
     * if there are no defaults.
     *
     * These properties only provide default values for each property. Any
     * plugin properties persisted to the database will take precedence
     * over those returned here.
     *
     * @return The default properties for this plugin.
     */
    public Properties getDefaultProperties();

    /**
     * Returns permissions for this plugin so they can be initialized on startup.
     *
     * @return
     */
    public ExtensionPermission[] getExtensionPermissions();
    
    /**
     * Returns a map of strings to example objects for use in populating swagger's examples.
     * 
     * @return
     */
    public Map<String, Object> getObjectsForSwaggerExamples();
}

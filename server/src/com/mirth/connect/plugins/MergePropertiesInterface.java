/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.util.Properties;

import com.mirth.connect.client.core.ControllerException;

public interface MergePropertiesInterface {

    /**
     * On server configuration restore, allows the plugin to modify the properties before they are
     * saved to the database.
     */
    public void modifyPropertiesOnRestore(Properties properties) throws ControllerException;
}
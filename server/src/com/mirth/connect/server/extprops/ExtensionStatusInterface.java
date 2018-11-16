/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.extprops;

import java.util.Set;

public interface ExtensionStatusInterface {

    public void reload();

    public Set<String> keySet();

    public boolean containsKey(String pluginName);

    public boolean isEnabled(String pluginName);

    public void setEnabled(String pluginName, boolean enabled);

    public void remove(String pluginName);

    public void save();
}

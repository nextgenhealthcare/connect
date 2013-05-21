/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v3;

import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.plugins.DataTypeServerPlugin;

public class HL7V3DataTypeServerPlugin extends DataTypeServerPlugin {
    private DataTypeDelegate dataTypeDelegate = new HL7V3DataTypeDelegate();

    @Override
    public String getPluginPointName() {
        return dataTypeDelegate.getName();
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    protected DataTypeDelegate getDataTypeDelegate() {
        return dataTypeDelegate;
    }
}

/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.delimited;

import com.mirth.connect.donkey.server.message.BatchAdaptor;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.plugins.DataTypeServerPlugin;

public class DelimitedDataTypeServerPlugin extends DataTypeServerPlugin {
    private DataTypeDelegate dataTypeDelegate = new DelimitedDataTypeDelegate();

    @Override
    public String getPluginPointName() {
        return dataTypeDelegate.getName();
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public BatchAdaptor getBatchAdaptor(SerializerProperties properties) {
        return new DelimitedBatchAdaptor(properties);
    }

    @Override
    protected DataTypeDelegate getDataTypeDelegate() {
        return dataTypeDelegate;
    }

    @Override
    public String getBatchScript(BatchAdaptor batchAdaptor) {
        DelimitedBatchAdaptor delimitedBatchAdaptor = (DelimitedBatchAdaptor) batchAdaptor;
        return delimitedBatchAdaptor.getBatchScript();
    }
}

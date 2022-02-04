/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.delimited;

import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.plugins.datatypes.DebuggableBatchAdaptorFactory;

public class DelimitedBatchAdaptorFactory extends DebuggableBatchAdaptorFactory {
    private DelimitedSerializationProperties serializationProperties;

    public DelimitedBatchAdaptorFactory(SourceConnector sourceConnector, SerializerProperties serializerProperties) {
        super(sourceConnector, serializerProperties);
        serializationProperties = (DelimitedSerializationProperties) serializerProperties.getSerializationProperties();
    }

    @Override
    public BatchAdaptor createBatchAdaptor(BatchRawMessage batchRawMessage) {
        DelimitedBatchAdaptor batchAdaptor = new DelimitedBatchAdaptor(this, sourceConnector, batchRawMessage);

        batchAdaptor.setSerializationProperties(serializationProperties);
        batchAdaptor.setBatchProperties(batchProperties);
        batchAdaptor.setDelimitedReader(new DelimitedReader((DelimitedSerializationProperties) serializationProperties));

        return batchAdaptor;
    }
}

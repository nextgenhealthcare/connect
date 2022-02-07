/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.raw;

import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.server.message.DebuggableBatchAdaptorFactory;

public class RawBatchAdaptorFactory extends DebuggableBatchAdaptorFactory {
    public RawBatchAdaptorFactory(SourceConnector sourceConnector, SerializerProperties serializerProperties) {
        super(sourceConnector, serializerProperties);
    }

    @Override
    public BatchAdaptor createBatchAdaptor(BatchRawMessage batchRawMessage) {
        RawBatchAdaptor batchAdaptor = new RawBatchAdaptor(this, sourceConnector, batchRawMessage);

        batchAdaptor.setBatchProperties(batchProperties);

        return batchAdaptor;
    }
}

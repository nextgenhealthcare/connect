/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v2;

import java.io.InputStream;

import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.AutoResponder;
import com.mirth.connect.donkey.server.message.ResponseValidator;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptorFactory;
import com.mirth.connect.donkey.server.message.batch.BatchStreamReader;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;
import com.mirth.connect.model.datatype.ResponseValidationProperties;
import com.mirth.connect.model.datatype.SerializationProperties;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.model.transmission.framemode.FrameModeProperties;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import com.mirth.connect.util.TcpUtil;

public class HL7v2DataTypeServerPlugin extends DataTypeServerPlugin {
    private DataTypeDelegate dataTypeDelegate = new HL7v2DataTypeDelegate();

    @Override
    public String getPluginPointName() {
        return dataTypeDelegate.getName();
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public AutoResponder getAutoResponder(SerializationProperties serializationProperties, ResponseGenerationProperties generationProperties) {
        return new HL7v2AutoResponder(serializationProperties, generationProperties);
    }

    @Override
    public ResponseValidator getResponseValidator(SerializationProperties serializationProperties, ResponseValidationProperties responseValidationProperties) {
        return new HL7v2ResponseValidator(serializationProperties, responseValidationProperties);
    }

    @Override
    public BatchAdaptorFactory getBatchAdaptorFactory(SourceConnector sourceConnector, SerializerProperties properties) {
        return new ER7BatchAdaptorFactory(sourceConnector, properties);
    }

    @Override
    public BatchStreamReader getBatchStreamReader(InputStream inputStream, TransmissionModeProperties properties) {
        BatchStreamReader batchStreamReader = null;
        if (properties instanceof FrameModeProperties) {
            batchStreamReader = new ER7BatchStreamReader(inputStream, TcpUtil.stringToByteArray(((FrameModeProperties) properties).getEndOfMessageBytes()));
        } else {
            batchStreamReader = new ER7BatchStreamReader(inputStream);
        }
        return batchStreamReader;
    }

    @Override
    protected DataTypeDelegate getDataTypeDelegate() {
        return dataTypeDelegate;
    }

}

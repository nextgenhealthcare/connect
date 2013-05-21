/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.donkey.server.message.AutoResponder;
import com.mirth.connect.donkey.server.message.BatchAdaptor;
import com.mirth.connect.donkey.server.message.ResponseValidator;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;
import com.mirth.connect.model.datatype.ResponseValidationProperties;
import com.mirth.connect.model.datatype.SerializationProperties;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.server.message.DefaultAutoResponder;
import com.mirth.connect.server.message.DefaultResponseValidator;

public abstract class DataTypeServerPlugin implements ServerPlugin {

    /**
     * Get an instance of the data type's serializer with the given properties
     */
    final public IXMLSerializer getSerializer(SerializerProperties properties) {
        return getDataTypeDelegate().getSerializer(properties);
    }

    /**
     * Indicates if the data type is in binary format
     */
    final public boolean isBinary() {
        return getDataTypeDelegate().isBinary();
    }

    /**
     * Get the serialization type
     */
    final public SerializationType getSerializationType() {
        return getDataTypeDelegate().getSerializationType();
    }

    /**
     * Get the data type delegate that is used for client/server shared methods
     */
    protected abstract DataTypeDelegate getDataTypeDelegate();

    /**
     * Get the batch adaptor for the data type
     */
    public BatchAdaptor getBatchAdaptor(SerializerProperties properties) {
        return null;
    }

    /**
     * Get the auto responder for the data type
     */
    public AutoResponder getAutoResponder(SerializationProperties serializationProperties, ResponseGenerationProperties generationProperties) {
        return new DefaultAutoResponder();
    }

    /**
     * Get the response validator for the data type
     */
    public ResponseValidator getResponseValidator(SerializationProperties serializationProperties, ResponseValidationProperties responseValidationProperties) {
        return new DefaultResponseValidator();
    }

    /**
     * Get the batch script from a serializer.
     * Returns null if no script exists.
     */
    public String getBatchScript(BatchAdaptor batchAdaptor) {
        return null;
    }
}

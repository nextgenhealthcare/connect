/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.json;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.donkey.model.message.MessageSerializer;
import com.mirth.connect.donkey.model.message.MessageSerializerException;
import com.mirth.connect.model.converters.IMessageSerializer;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.model.util.DefaultMetaData;

public class JSONSerializer implements IMessageSerializer {

    public JSONSerializer(SerializerProperties properties) {}

    @Override
    public boolean isSerializationRequired(boolean toJson) {
        boolean serializationRequired = false;

        return serializationRequired;
    }

    @Override
    public String toXML(String source) throws MessageSerializerException {
        return null;
    }

    @Override
    public String fromXML(String source) throws MessageSerializerException {
        return null;
    }

    @Override
    public String toJSON(String source) throws MessageSerializerException {
        return source;
    }

    @Override
    public String fromJSON(String source) throws MessageSerializerException {
        return source;
    }

    @Override
    public String transformWithoutSerializing(String message, MessageSerializer outboundSerializer) throws MessageSerializerException {
        return null;
    }

    @Override
    public void populateMetaData(String message, Map<String, Object> map) {}

    @Override
    public Map<String, Object> getMetaDataFromMessage(String message) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(DefaultMetaData.TYPE_VARIABLE_MAPPING, "JSON");
        return map;
    }
}

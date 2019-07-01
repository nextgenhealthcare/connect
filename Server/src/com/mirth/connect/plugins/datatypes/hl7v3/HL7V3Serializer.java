/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v3;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.donkey.model.message.MessageSerializer;
import com.mirth.connect.donkey.model.message.MessageSerializerException;
import com.mirth.connect.model.converters.IMessageSerializer;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.model.util.DefaultMetaData;
import com.mirth.connect.util.ErrorMessageBuilder;
import com.mirth.connect.util.StringUtil;

public class HL7V3Serializer implements IMessageSerializer {

    private HL7V3SerializationProperties serializationProperties;

    public HL7V3Serializer(SerializerProperties properties) {
        serializationProperties = (HL7V3SerializationProperties) properties.getSerializationProperties();
    }

    @Override
    public boolean isSerializationRequired(boolean toXml) {
        boolean serializationRequired = false;

        return serializationRequired;
    }

    @Override
    public String transformWithoutSerializing(String message, MessageSerializer outboundSerializer) throws MessageSerializerException {
        try {
            if (serializationProperties.isStripNamespaces()) {
                return StringUtil.stripNamespaces(message);
            }
        } catch (Exception e) {
            throw new MessageSerializerException("Error transforming HL7 v3.x", e, ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), "Error transforming HL7 v3.x", e));
        }

        return null;
    }

    @Override
    public String toXML(String source) throws MessageSerializerException {
        try {
            if (serializationProperties.isStripNamespaces()) {
                source = StringUtil.stripNamespaces(source);
            }
            source = source.trim();
        } catch (Exception e) {
            throw new MessageSerializerException("Error transforming HL7 v3.x", e, ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), "Error transforming HL7 v3.x", e));
        }

        return source;
    }

    @Override
    public String fromXML(String source) throws MessageSerializerException {
        return source;
    }

    @Override
    public Map<String, Object> getMetaDataFromMessage(String message) {
        Map<String, Object> map = new HashMap<String, Object>();

        // TODO: Update this to real version codes
        map.put(DefaultMetaData.VERSION_VARIABLE_MAPPING, "3.0");

        StringBuilder builder = new StringBuilder();
        int index = 0;
        boolean found = false;

        // Find the QName of the root node of the XML
        while (index < message.length() - 1) {
            char c = message.charAt(index);
            char next = message.charAt(index + 1);
            if (!found && c == '<' && ((next >= 'A' && next <= 'Z') || (next >= 'a' && next <= 'z') || next == '_')) {
                found = true;
            } else if (found) {
                if (c <= ' ' || c == '/' || c == '>') {
                    break;
                }
                builder.append(c);
            }
            index++;
        }

        if (builder.length() > 0) {
            map.put(DefaultMetaData.TYPE_VARIABLE_MAPPING, builder.toString());
        }

        return map;
    }

    @Override
    public void populateMetaData(String message, Map<String, Object> map) {}

    @Override
    public String toJSON(String message) throws MessageSerializerException {
        return null;
    }

    @Override
    public String fromJSON(String message) throws MessageSerializerException {
        return null;
    }
}
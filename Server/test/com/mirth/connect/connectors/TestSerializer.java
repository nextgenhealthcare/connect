/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors;

import java.util.Map;

import com.mirth.connect.donkey.model.message.MessageSerializer;
import com.mirth.connect.donkey.model.message.MessageSerializerException;

public class TestSerializer implements MessageSerializer {

    @Override
    public boolean isSerializationRequired(boolean isXml) {
        return false;
    }

    @Override
    public String transformWithoutSerializing(String message, MessageSerializer outboundSerializer) {
        return message;
    }

    @Override
    public String toXML(String message) throws MessageSerializerException {
        return message;
    }

    @Override
    public String fromXML(String message) throws MessageSerializerException {
        return message;
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

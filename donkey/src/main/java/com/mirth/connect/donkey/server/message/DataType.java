/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.message;

import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.donkey.model.message.XmlSerializer;

public class DataType {

    private String type;
    private XmlSerializer serializer;
    private AutoResponder autoResponder;
    private SerializationType serializationType;

    public DataType(String type, XmlSerializer serializer, AutoResponder autoResponder) {
        this(type, serializer, autoResponder, SerializationType.XML);
    }

    public DataType(String type, XmlSerializer serializer, AutoResponder autoResponder, SerializationType serializationType) {
        this.type = type;
        this.serializer = serializer;
        this.autoResponder = autoResponder;
        this.serializationType = serializationType;
    }

    public String getType() {
        return type;
    }

    public XmlSerializer getSerializer() {
        return serializer;
    }

    public AutoResponder getAutoResponder() {
        return autoResponder;
    }

    public SerializationType getSerializationType() {
        return serializationType;
    }
}

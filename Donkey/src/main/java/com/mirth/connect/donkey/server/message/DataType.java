/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.message;

import com.mirth.connect.donkey.model.message.MessageSerializer;
import com.mirth.connect.donkey.model.message.SerializationType;

public class DataType {

    private String type;
    private MessageSerializer serializer;
    private AutoResponder autoResponder;
    private SerializationType serializationType;
    private SerializationType templateSerializationType;

    public DataType(String type, MessageSerializer serializer, AutoResponder autoResponder) {
        this(type, serializer, autoResponder, SerializationType.XML, SerializationType.XML);
    }

    public DataType(String type, MessageSerializer serializer, AutoResponder autoResponder, SerializationType serializationType, SerializationType templateSerializationType) {
        this.type = type;
        this.serializer = serializer;
        this.autoResponder = autoResponder;
        this.serializationType = serializationType;
        this.templateSerializationType = templateSerializationType;
    }

    public String getType() {
        return type;
    }

    public MessageSerializer getSerializer() {
        return serializer;
    }

    public AutoResponder getAutoResponder() {
        return autoResponder;
    }

    public SerializationType getSerializationType() {
        return serializationType;
    }

    public SerializationType getTemplateSerializationType() {
        return templateSerializationType;
    }
}

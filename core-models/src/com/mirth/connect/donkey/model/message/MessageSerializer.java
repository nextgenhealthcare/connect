/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

import java.util.Map;

public interface MessageSerializer {
    public String toXML(String message) throws MessageSerializerException;

    public String fromXML(String message) throws MessageSerializerException;

    public String toJSON(String message) throws MessageSerializerException;

    public String fromJSON(String message) throws MessageSerializerException;

    public boolean isSerializationRequired(boolean toXml);

    public String transformWithoutSerializing(String message, MessageSerializer outboundSerializer) throws MessageSerializerException;

    public void populateMetaData(String message, Map<String, Object> map);
}
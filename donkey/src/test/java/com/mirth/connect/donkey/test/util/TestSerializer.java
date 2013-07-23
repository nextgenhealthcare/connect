/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test.util;

import java.util.Map;

import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.donkey.model.message.XmlSerializerException;

public class TestSerializer implements XmlSerializer {

    @Override
    public boolean isSerializationRequired(boolean isXml) {
        return false;
    }

    @Override
    public String transformWithoutSerializing(String message, XmlSerializer outboundSerializer) {
        return message;
    }

    @Override
    public String toXML(String message) throws XmlSerializerException {
        return message;
    }

    @Override
    public String fromXML(String message) throws XmlSerializerException {
        return message;
    }

    @Override
    public void populateMetaData(String message, Map<String, Object> map) {}
}

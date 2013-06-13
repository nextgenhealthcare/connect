/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.raw;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.mirth.connect.donkey.model.message.XmlSerializerException;
import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.SerializerProperties;

public class RawSerializer implements IXMLSerializer {
    private Logger logger = Logger.getLogger(this.getClass());

    public RawSerializer(SerializerProperties properties) {}

    @Override
    public boolean isSerializationRequired(boolean toXml) {
        boolean serializationRequired = false;

        return serializationRequired;
    }

    @Override
    public String transformWithoutSerializing(String message, XmlSerializer outboundSerializer) {
        return null;
    }

    @Override
    public String toXML(String source) throws XmlSerializerException {
        return null;
    }

    @Override
    public String fromXML(String source) throws XmlSerializerException {
        return null;
    }

    @Override
    public Map<String, String> getMetadataFromDocument(Document doc) throws XmlSerializerException {
        Map<String, String> map = new HashMap<String, String>();
        return map;
    }

}

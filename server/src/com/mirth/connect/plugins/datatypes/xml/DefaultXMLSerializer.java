/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.xml;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.donkey.model.message.XmlSerializerException;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.model.util.DefaultMetaData;
import com.mirth.connect.util.ErrorMessageBuilder;
import com.mirth.connect.util.StringUtil;

public class DefaultXMLSerializer implements IXMLSerializer {

    private XMLSerializationProperties serializationProperties;

    public DefaultXMLSerializer(SerializerProperties properties) {
        serializationProperties = (XMLSerializationProperties) properties.getSerializationProperties();
    }

    @Override
    public boolean isSerializationRequired(boolean toXml) {
        boolean serializationRequired = false;

        return serializationRequired;
    }

    @Override
    public String transformWithoutSerializing(String message, XmlSerializer outboundSerializer) throws XmlSerializerException {
        try {
            if (serializationProperties.isStripNamespaces()) {
                return StringUtil.stripNamespaces(message);
            }
        } catch (Exception e) {
            throw new XmlSerializerException("Error transforming XML", e, ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), "Error transforming XML", e));
        }

        return null;
    }

    @Override
    public String toXML(String source) throws XmlSerializerException {
        try {
            if (serializationProperties.isStripNamespaces()) {
                source = StringUtil.stripNamespaces(source);
            }
        } catch (Exception e) {
            throw new XmlSerializerException("Error transforming XML", e, ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), "Error transforming XML", e));
        }

        return source;
    }

    @Override
    public String fromXML(String source) throws XmlSerializerException {
        return source;
    }

    @Override
    public Map<String, Object> getMetaDataFromMessage(String message) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(DefaultMetaData.VERSION_VARIABLE_MAPPING, "1.0");
        map.put(DefaultMetaData.TYPE_VARIABLE_MAPPING, "XML-Message");
        return map;
    }

    @Override
    public void populateMetaData(String message, Map<String, Object> map) {}
}

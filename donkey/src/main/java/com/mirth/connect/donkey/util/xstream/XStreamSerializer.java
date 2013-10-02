/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util.xstream;

import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import org.xmlpull.mxp1.MXParser;

import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.DeployedStateEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.event.MessageEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.util.Serializer;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import com.thoughtworks.xstream.core.util.HierarchicalStreams;
import com.thoughtworks.xstream.io.xml.Xpp3Driver;
import com.thoughtworks.xstream.io.xml.XppReader;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class XStreamSerializer implements Serializer {
    // http://jira.codehaus.org/browse/XSTR-395
    private static final Map<String, WeakReference<String>> stringCache = new WeakHashMap<String, WeakReference<String>>();

    private static final Class<?>[] annotatedClasses = new Class<?>[] {// @formatter:off
        Attachment.class,
        DeployedStateEventType.class,
        ConnectionStatusEventType.class,
        ConnectorMessage.class,
        ErrorEventType.class,
        Message.class,
        MessageEventType.class,
        Response.class
    }; // @formatter:on

    private XStream xstream;

    public XStreamSerializer() {
        this(null);
    }

    public XStreamSerializer(final DonkeyMapperWrapper mapperWrapper) {
        if (mapperWrapper != null) {
            xstream = new XStream(new Xpp3Driver()) {
                @Override
                protected MapperWrapper wrapMapper(MapperWrapper next) {
                    return mapperWrapper.wrapMapper(next);
                }
            };
        } else {
            xstream = new XStream(new Xpp3Driver());
        }

        xstream.registerConverter(new StringConverter(stringCache));
        xstream.registerConverter(new ConcurrentHashMapConverter(xstream.getMapper()));
        xstream.registerConverter(new PropertiesConverter());
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.processAnnotations(annotatedClasses);
    }

    @Override
    public String serialize(Object object) {
        try {
            return xstream.toXML(object);
        } catch (Exception e) {
            throw new SerializerException(e);
        }
    }

    @Override
    public <T> T deserialize(String serializedObject, Class<T> expectedClass) {
        try {
            return (T) xstream.fromXML(serializedObject);
        } catch (Exception e) {
            throw new SerializerException(e);
        }
    }

    @Override
    public Class<?> getClass(String serializedObject) {
        try {
            return HierarchicalStreams.readClassType(new XppReader(new StringReader(serializedObject), new MXParser()), getXStream().getMapper());
        } catch (Exception e) {
            throw new SerializerException(e);
        }
    }

    public XStream getXStream() {
        return xstream;
    }
}
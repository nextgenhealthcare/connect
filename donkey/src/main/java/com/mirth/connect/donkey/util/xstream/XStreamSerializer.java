/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util.xstream;

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import org.xmlpull.mxp1.MXParser;

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

public class XStreamSerializer implements Serializer {
    // http://jira.codehaus.org/browse/XSTR-395
    private static final Map<String, WeakReference<String>> stringCache = new WeakHashMap<String, WeakReference<String>>();

    private static final Class<?>[] annotatedClasses = new Class<?>[] {// @formatter:off
        Attachment.class,
        ConnectorMessage.class,
        Message.class,
        Response.class
    }; // @formatter:on

    private XStream xstream;

    public XStreamSerializer() {
        xstream = new XStream(new Xpp3Driver());
        xstream.registerConverter(new StringConverter(stringCache));
        xstream.registerConverter(new ConcurrentHashMapConverter(xstream.getMapper()));
        xstream.registerConverter(new PropertiesConverter());
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.processAnnotations(annotatedClasses);
    }

    @Override
    public String serialize(Object serializableObject) {
        return xstream.toXML(serializableObject);
    }
    
    @Override
    public void serialize(Object serializableObject, Writer writer) {
        xstream.toXML(serializableObject, writer);
    }

    @Override
    public Object deserialize(String serializedObject) {
        return xstream.fromXML(serializedObject);
    }
    
    @Override
    public Object deserialize(Reader reader) {
        return xstream.fromXML(reader);
    }
    
    @Override
    public Class<?> getClass(String serializedObject) {
        return getClass(new StringReader(serializedObject));
    }

    @Override
    public Class<?> getClass(Reader reader) {
        return HierarchicalStreams.readClassType(new XppReader(reader, new MXParser()), getXStream().getMapper());
    }
    
    protected XStream getXStream() {
        return xstream;
    }
}

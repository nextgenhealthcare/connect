/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.w3c.dom.Document;

import com.mirth.connect.model.Alert;
import com.mirth.connect.model.ArchiveMetaData;
import com.mirth.connect.model.Attachment;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.Event;
import com.mirth.connect.model.ExtensionLibrary;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.Rule;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.User;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class ObjectXMLSerializer {
    private XStream xstream;
    private static final Map<String, String> stringCache = new WeakHashMap<String, String>();
    
    private static final Class<?>[] annotatedClasses = new Class<?>[] {
        Alert.class,
        ArchiveMetaData.class,
        Attachment.class,
        Channel.class,
        CodeTemplate.class,
        Connector.class,
        ConnectorMetaData.class,
        Event.class,
        ExtensionLibrary.class,
        Filter.class,
        MessageObject.class,
        MetaData.class,
        PluginMetaData.class,
        Rule.class,
        ServerConfiguration.class,
        ServerSettings.class,
        Step.class,
        Transformer.class,
        UpdateSettings.class,
        User.class
    };

    public ObjectXMLSerializer() {
        xstream = new XStream(new XppDriver());
        xstream.registerConverter(new StringConverter(stringCache));
        xstream.setMode(XStream.NO_REFERENCES);
        processAnnotations();
    }

    public ObjectXMLSerializer(Class<?>[] aliases) {
        xstream = new XStream(new XppDriver());
        xstream.registerConverter(new StringConverter(stringCache));
        processAnnotations();
        xstream.processAnnotations(aliases);
        xstream.setMode(XStream.NO_REFERENCES);
    }

    public ObjectXMLSerializer(Class<?>[] aliases, Converter[] converters) {
        xstream = new XStream(new XppDriver());
        processAnnotations();
        xstream.processAnnotations(aliases);
        xstream.setMode(XStream.NO_REFERENCES);

        for (int i = 0; i < converters.length; i++) {
            xstream.registerConverter(converters[i]);
        }
    }

    public String toXML(Object source) {
        return xstream.toXML(source);
    }

    public Object fromXML(String source) {
        return xstream.fromXML(source);
    }

    private void processAnnotations() {
        xstream.processAnnotations(annotatedClasses);
    }
}

/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.io.Reader;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import com.mirth.connect.model.Alert;
import com.mirth.connect.model.ArchiveMetaData;
import com.mirth.connect.model.Attachment;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelStatistics;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.DeployedChannelInfo;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.Event;
import com.mirth.connect.model.ExtensionLibrary;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.Rule;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerInfo;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.model.UpdateInfo;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.UsageData;
import com.mirth.connect.model.User;
import com.mirth.connect.model.filters.EventFilter;
import com.mirth.connect.model.filters.MessageObjectFilter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import com.thoughtworks.xstream.converters.collections.ConcurrentHashMapConverter;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class ObjectXMLSerializer {
    private XStream xstream;
    
    // http://jira.codehaus.org/browse/XSTR-395
    private static final Map<String, WeakReference<String>> stringCache = new WeakHashMap<String, WeakReference<String>>();
    
    private static final Class<?>[] annotatedClasses = new Class<?>[] {
        Alert.class,
        ArchiveMetaData.class,
        Attachment.class,
        Channel.class,
        ChannelStatistics.class,
        ChannelStatus.class,
        ChannelSummary.class,
        CodeTemplate.class,
        Connector.class,
        ConnectorMetaData.class,
        DeployedChannelInfo.class,
        DriverInfo.class,
        Event.class,
        EventFilter.class,
        ExtensionLibrary.class,
        Filter.class,
        MessageObject.class,
        MessageObjectFilter.class,
        MetaData.class,
        PasswordRequirements.class,
        PluginMetaData.class,
        Rule.class,
        ServerConfiguration.class,
        ServerInfo.class,
        ServerSettings.class,
        Step.class,
        Transformer.class,
        UpdateInfo.class,
        UpdateSettings.class,
        UsageData.class,
        User.class
    };

    public ObjectXMLSerializer() {
        // http://jira.codehaus.org/browse/XSTR-651
        xstream = new XStream(new Sun14ReflectionProvider(), new XppDriver());
        xstream.registerConverter(new StringConverter(stringCache));
        xstream.registerConverter(new ConcurrentHashMapConverter(xstream.getMapper()));
        xstream.setMode(XStream.NO_REFERENCES);
        processAnnotations();
    }

    public ObjectXMLSerializer(Class<?>[] aliases) {
        // http://jira.codehaus.org/browse/XSTR-651
        xstream = new XStream(new Sun14ReflectionProvider(), new XppDriver());
        xstream.registerConverter(new StringConverter(stringCache));
        xstream.registerConverter(new ConcurrentHashMapConverter(xstream.getMapper()));
        processAnnotations();
        xstream.processAnnotations(aliases);
        xstream.setMode(XStream.NO_REFERENCES);
    }

    public ObjectXMLSerializer(Class<?>[] aliases, Converter[] converters) {
        // http://jira.codehaus.org/browse/XSTR-651
        xstream = new XStream(new Sun14ReflectionProvider(), new XppDriver());
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

    public void toXML(Object source, Writer writer) {
        xstream.toXML(source, writer);
    }

    public Object fromXML(String source) {
        return xstream.fromXML(source);
    }

    public Object fromXML(Reader reader) {
        return xstream.fromXML(reader);
    }

    private void processAnnotations() {
        xstream.processAnnotations(annotatedClasses);
    }
}

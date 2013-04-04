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

import com.mirth.connect.donkey.util.xstream.XStreamSerializer;
import com.mirth.connect.model.Alert;
import com.mirth.connect.model.ArchiveMetaData;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelStatistics;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.DeployedChannelInfo;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.Event;
import com.mirth.connect.model.ExtensionLibrary;
import com.mirth.connect.model.Filter;
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
import com.mirth.connect.model.filters.MessageFilter;

public class ObjectXMLSerializer extends XStreamSerializer {
    private static final Class<?>[] annotatedClasses = new Class<?>[] {//@formatter:off
        Alert.class,
        ArchiveMetaData.class,
        Channel.class,
        ChannelStatistics.class,
        ChannelSummary.class,
        CodeTemplate.class,
        Connector.class,
        ConnectorMetaData.class,
        DashboardStatus.class,
        DeployedChannelInfo.class,
        DriverInfo.class,
        Event.class,
        EventFilter.class,
        ExtensionLibrary.class,
        Filter.class,
        MessageFilter.class,
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
    }; // @formatter:on

    private static final ObjectXMLSerializer instance = new ObjectXMLSerializer();

    public static ObjectXMLSerializer getInstance() {
        return instance;
    }

    private ObjectXMLSerializer() {
        processAnnotations(annotatedClasses);
    }

    public String toXML(Object source) {
        return serialize(source);
    }

    public void toXML(Object source, Writer writer) {
        serialize(source, writer);
    }

    public Object fromXML(String source) {
        return deserialize(source);
    }

    public Object fromXML(Reader reader) {
        return deserialize(reader);
    }

    public void processAnnotations(Class<?>[] classes) {
        getXStream().processAnnotations(classes);
    }
}

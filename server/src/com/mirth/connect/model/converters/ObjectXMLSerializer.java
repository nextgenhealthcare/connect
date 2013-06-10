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
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.mirth.connect.donkey.util.xstream.XStreamSerializer;
import com.mirth.connect.model.ArchiveMetaData;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelProperties;
import com.mirth.connect.model.ChannelStatistics;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.DeployedChannelInfo;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.ExtensionLibrary;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.Rule;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.ServerInfo;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.model.UpdateInfo;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.UsageData;
import com.mirth.connect.model.User;
import com.mirth.connect.model.alert.AlertAction;
import com.mirth.connect.model.alert.AlertActionGroup;
import com.mirth.connect.model.alert.AlertActionProtocol;
import com.mirth.connect.model.alert.AlertChannels;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.alert.AlertStatus;
import com.mirth.connect.model.alert.DefaultTrigger;
import com.mirth.connect.model.filters.EventFilter;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.model.util.ImportConverter3_0_0;
import com.mirth.connect.util.MigrationUtil;
import com.thoughtworks.xstream.io.xml.DomReader;

public class ObjectXMLSerializer extends XStreamSerializer {
    public final static String VERSION_ATTRIBUTE_NAME = "version";
    
    private static final Class<?>[] annotatedClasses = new Class<?>[] {//@formatter:off
        AlertAction.class,
        AlertActionGroup.class,
        AlertActionProtocol.class,
        AlertChannels.class,
        AlertModel.class,
        AlertStatus.class,
        ArchiveMetaData.class,
        Channel.class,
        ChannelProperties.class,
        ChannelStatistics.class,
        ChannelSummary.class,
        CodeTemplate.class,
        Connector.class,
        ConnectorMetaData.class,
        DashboardStatus.class,
        DefaultTrigger.class,
        DeployedChannelInfo.class,
        DriverInfo.class,
        EventFilter.class,
        ExtensionLibrary.class,
        Filter.class,
        MessageFilter.class,
        MetaData.class,
        PasswordRequirements.class,
        PluginMetaData.class,
        Rule.class,
        ServerConfiguration.class,
        ServerEvent.class,
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

    public static Class<?>[] getAnnotatedClasses() {
        return annotatedClasses;
    }
    
    private ObjectXMLSerializer() {
        processAnnotations(annotatedClasses);
    }
    
    public void init(String currentVersion) {
        getXStream().registerConverter(new MigratableConverter(MigrationUtil.normalizeVersion(currentVersion, 3), getXStream().getMapper()));
    }

    public String toXML(Object source) {
        return serialize(source);
    }

    public void toXML(Object source, Writer writer) {
        serialize(source, writer);
    }

    public Object fromXML(String source) {
        return deserialize(new StringReader(source));
    }
    
    /**
     * Deserializes a source XML string and returns an object of the expectedClass type.
     */
    public <T> T fromXML(String source, Class<T> expectedClass) {
        return (T) doFromXML(source, expectedClass);
    }

    /**
     * Deserializes a source XML string and returns a List of objects of the expectedListItemClass
     * type. If the source xml string represents a single object, then a list with that single
     * object will be returned.
     */
    public <T> List<T> listFromXML(String source, Class<T> expectedListItemClass) {
        Object object = doFromXML(source, expectedListItemClass);
        
        if (!(object instanceof List<?>)) {
            List<T> objectList = new ArrayList<T>();
            objectList.add((T) object);
            return objectList;
        }
        
        return (List<T>) object;
    }

    private Object doFromXML(String source, Class<?> expectedClass) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(source)));

            /*
             * Have the ImportConverter migrate the serialized object to the version 3.0.0
             * structure, which is when the "version" attribute and the Migratable interface were
             * first introduced. After converting it to the 3.0.0 structure, the migration methods
             * in the Migratable interface will migrate the object to the current Mirth version (see
             * MigratableConverter).
             */
            document = ImportConverter3_0_0.convert(document, source, expectedClass);

            return getXStream().unmarshal(new DomReader(document));
        } catch (Exception e) {
            // TODO handle exception
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Object deserialize(String source) {
        return deserialize(new StringReader(source));
    }

    @Override
    public Object deserialize(Reader reader) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(reader));
            return getXStream().unmarshal(new DomReader(document));
        } catch (Exception e) {
            // TODO handle exception
            e.printStackTrace();
            return null;
        }
    }
    
    public void processAnnotations(Class<?>[] classes) {
        getXStream().processAnnotations(classes);
    }
}

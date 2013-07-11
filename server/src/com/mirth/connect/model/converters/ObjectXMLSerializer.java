/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.xstream.SerializerException;
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
    
    private Logger logger = Logger.getLogger(getClass());
    
    private ObjectXMLSerializer() {
        processAnnotations(annotatedClasses);
    }
    
    public void processAnnotations(Class<?>[] classes) {
        getXStream().processAnnotations(classes);
    }
    
    public void init(String currentVersion) {
        getXStream().registerConverter(new MigratableConverter(MigrationUtil.normalizeVersion(currentVersion, 3), getXStream().getMapper()));
    }
    
    /**
     * Serializes an object.
     */
    public void serialize(Object object, Writer writer) {
        try {
            getXStream().toXML(object, writer);
        } catch (Exception e) {
            logger.error(e);
            throw new SerializerException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(String serializedObject, Class<T> expectedClass) {
        try {
            if (skipMigration(expectedClass)) {
                return (T) getXStream().fromXML(serializedObject);
            } else {
                DonkeyElement element = new DonkeyElement(serializedObject);
                
                if (ImportConverter3_0_0.isMigratable(expectedClass)) {
                    element = ImportConverter3_0_0.migrate(element, expectedClass);
                }
                
                return (T) getXStream().unmarshal(new DomReader(element.getElement()));
            }
        } catch (Exception e) {
            logger.error(e);
            throw new SerializerException(e);
        }
    }

    /**
     * Deserializes a source XML string and returns a List of objects of the expectedListItemClass
     * type. If the source xml string represents a single object, then a list with that single
     * object will be returned.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> deserializeList(String serializedObject, Class<T> expectedListItemClass) {
        try {
            /*
             * If the expectedListItemClass is migratable to version 3.0.0, then we need to invoke
             * ImportConverter3_0_0.migrate() on each item in the list.
             */
            if (ImportConverter3_0_0.isMigratable(expectedListItemClass)) {
                DonkeyElement listElement = new DonkeyElement(serializedObject);

                if (listElement.getNodeName().equalsIgnoreCase("null")) {
                    return null;
                } else {
                    // If the element is not a list, then re-create the element as a list with one item
                    if (!listElement.getNodeName().equals("list")) {
                        listElement = new DonkeyElement("<list/>");
                        listElement.addChildElementFromXml(serializedObject);
                    }
        
                    List<T> list = new ArrayList<T>();
        
                    for (DonkeyElement child : listElement.getChildElements()) {
                        child = ImportConverter3_0_0.migrate(child, expectedListItemClass);
                        list.add((T) getXStream().unmarshal(new DomReader(child.getElement())));
                    }
        
                    return list;
                }
            } else {
                Object object;
                
                if (skipMigration(expectedListItemClass)) {
                    object = (T) getXStream().fromXML(serializedObject);
                } else {
                    object = getXStream().unmarshal(new DomReader(new DonkeyElement(serializedObject).getElement()));
                }

                if (object == null) {
                    return null;
                } else if (object instanceof List) {
                    return (List<T>) object;
                } else {
                    List<T> list = new ArrayList<T>();
                    list.add((T) object);
                    return list;
                }
            }
        } catch (Exception e) {
            logger.error(e);
            throw new SerializerException(e);
        }
    }

    /**
     * This should return true only for classes that are not Migratable AND whose instances do not
     * contain references to other Migratable objects. The purpose of this method is to avoid
     * parsing serialized data for these types into a DOM document, since that is only required if
     * MigratableConverter is triggered.
     */
    private boolean skipMigration(Class<?> expectedClass) {
        return (expectedClass.equals(String.class) || expectedClass.equals(Integer.class) || expectedClass.equals(Long.class) || expectedClass.equals(Float.class) || expectedClass.equals(Double.class));
    }
}

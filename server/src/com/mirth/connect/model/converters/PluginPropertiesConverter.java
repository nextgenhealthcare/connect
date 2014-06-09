/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;

import com.mirth.connect.donkey.model.channel.ConnectorPluginProperties;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.mirth.connect.model.InvalidConnectorPluginProperties;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.copy.HierarchicalStreamCopier;
import com.thoughtworks.xstream.io.xml.DocumentReader;
import com.thoughtworks.xstream.io.xml.XppReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class PluginPropertiesConverter extends MigratableConverter {

    private HierarchicalStreamCopier copier = new HierarchicalStreamCopier();
    private XmlPullParser parser = new MXParser();

    public PluginPropertiesConverter(String currentVersion, Mapper mapper) {
        super(currentVersion, mapper);
    }

    @Override
    public boolean canConvert(Class type) {
        // This is a local converter, so we're guaranteeing its usage
        return true;
    }

    @Override
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        for (ConnectorPluginProperties properties : (Collection<ConnectorPluginProperties>) value) {
            String propertiesXml;

            if (properties instanceof InvalidConnectorPluginProperties) {
                try {
                    propertiesXml = ((InvalidConnectorPluginProperties) properties).getPropertiesXml();
                } catch (Exception e) {
                    throw new SerializerException(e);
                }
            } else {
                propertiesXml = ObjectXMLSerializer.getInstance().serialize(properties);
            }

            copier.copy(new XppReader(new StringReader(propertiesXml), parser), writer);
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        if (reader instanceof DocumentReader) {
            DocumentReader documentReader = (DocumentReader) reader;
            DonkeyElement element = new DonkeyElement((Element) documentReader.getCurrent());
            Set<ConnectorPluginProperties> propertiesSet = new HashSet<ConnectorPluginProperties>();

            for (DonkeyElement child : element.getChildElements()) {
                ConnectorPluginProperties properties;

                try {
                    properties = ObjectXMLSerializer.getInstance().deserialize(child.toXml(), ConnectorPluginProperties.class);
                } catch (LinkageError e) {
                    properties = new InvalidConnectorPluginProperties(child, e);
                } catch (Exception e) {
                    properties = new InvalidConnectorPluginProperties(child, e);
                }

                propertiesSet.add(properties);
            }

            return propertiesSet;
        } else {
            return super.unmarshal(reader, context);
        }
    }
}

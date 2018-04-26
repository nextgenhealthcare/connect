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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;
import org.xmlpull.mxp1.MXParser;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;
import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.mirth.connect.model.InvalidResourceProperties;
import com.mirth.connect.model.ResourceProperties;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.copy.HierarchicalStreamCopier;
import com.thoughtworks.xstream.io.xml.DocumentReader;
import com.thoughtworks.xstream.io.xml.XppReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class ResourcePropertiesConverter extends MigratableConverter {

    private HierarchicalStreamCopier copier = new HierarchicalStreamCopier();

    public ResourcePropertiesConverter(String currentVersion, Mapper mapper) {
        super(currentVersion, mapper);
    }

    @Override
    public boolean canConvert(Class type) {
        // This is a local converter, so we're guaranteeing its usage
        return true;
    }

    @Override
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        for (ResourceProperties properties : (Collection<ResourceProperties>) value) {
            String propertiesXml;

            if (properties instanceof InvalidResourceProperties) {
                try {
                    propertiesXml = ((InvalidResourceProperties) properties).getPropertiesXml();
                } catch (Exception e) {
                    throw new SerializerException(e);
                }
            } else {
                propertiesXml = ObjectXMLSerializer.getInstance().serialize(properties);
            }

            copier.copy(new XppReader(new StringReader(propertiesXml), new MXParser()), writer);
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        if (reader.underlyingReader() instanceof DocumentReader) {
            DonkeyElement element = new DonkeyElement((Element) ((DocumentReader) reader.underlyingReader()).getCurrent());
            List<ResourceProperties> propertiesList = new ArrayList<ResourceProperties>();

            for (DonkeyElement child : element.getChildElements()) {
                ResourceProperties properties;
                String preUnmarshalXml = null;

                try {
                    try {
                        preUnmarshalXml = child.toXml();
                    } catch (DonkeyElementException e) {
                    }

                    properties = ObjectXMLSerializer.getInstance().deserialize(preUnmarshalXml, ResourceProperties.class);
                } catch (LinkageError e) {
                    properties = new InvalidResourceProperties(preUnmarshalXml, child, e);
                } catch (Exception e) {
                    properties = new InvalidResourceProperties(preUnmarshalXml, child, e);
                }

                propertiesList.add(properties);
            }

            return propertiesList;
        } else {
            return super.unmarshal(reader, context);
        }
    }
}

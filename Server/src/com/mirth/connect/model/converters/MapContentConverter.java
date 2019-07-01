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

import org.w3c.dom.Element;
import org.xmlpull.mxp1.MXParser;

import com.mirth.connect.donkey.model.message.MapContent;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;
import com.mirth.connect.donkey.util.MapUtil;
import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.copy.HierarchicalStreamCopier;
import com.thoughtworks.xstream.io.xml.DocumentReader;
import com.thoughtworks.xstream.io.xml.XppReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class MapContentConverter extends ReflectionConverter {

    private HierarchicalStreamCopier copier = new HierarchicalStreamCopier();

    public MapContentConverter(Mapper mapper) {
        super(mapper, JVM.newReflectionProvider());
    }

    @Override
    public boolean canConvert(Class type) {
        return type == MapContent.class;
    }

    @Override
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        MapContent mapContent = (MapContent) value;

        if (!mapContent.isEncrypted() && MapUtil.hasInvalidValues(mapContent.getMap())) {
            String serializedMap = MapUtil.serializeMap(ObjectXMLSerializer.getInstance(), mapContent.getMap());

            try {
                DonkeyElement mapElement = new DonkeyElement(serializedMap);
                mapElement.setNodeName("content");
                mapElement.setAttribute("class", "map");
                copier.copy(new XppReader(new StringReader(mapElement.toXml()), new MXParser()), writer);
            } catch (DonkeyElementException e) {
                throw new SerializerException(e);
            }

            writer.startNode("encrypted");
            context.convertAnother(mapContent.isEncrypted());
            writer.endNode();
        } else {
            super.marshal(value, writer, context);
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        if (reader.underlyingReader() instanceof DocumentReader) {
            DonkeyElement mapContentElement = new DonkeyElement((Element) ((DocumentReader) reader.underlyingReader()).getCurrent());

            try {
                return super.unmarshal(reader, context);
            } catch (Exception e) {
                // Reset the stream reader to the map content element
                while (((DocumentReader) reader.underlyingReader()).getCurrent() instanceof Element && !((DocumentReader) reader.underlyingReader()).getCurrent().equals(mapContentElement.getElement())) {
                    reader.moveUp();
                }

                MapContent mapContent = new MapContent();
                mapContent.setContent(MapUtil.deserializeMapWithInvalidValues(ObjectXMLSerializer.getInstance(), mapContentElement.getChildElement("content")));
                mapContent.setEncrypted(Boolean.parseBoolean(mapContentElement.getChildElement("encrypted").getTextContent()));
                return mapContent;
            }
        } else {
            return super.unmarshal(reader, context);
        }
    }
}

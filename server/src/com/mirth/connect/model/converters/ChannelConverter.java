/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.io.StringReader;

import org.w3c.dom.Element;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.InvalidChannel;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.copy.HierarchicalStreamCopier;
import com.thoughtworks.xstream.io.xml.DocumentReader;
import com.thoughtworks.xstream.io.xml.XppReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class ChannelConverter extends MigratableConverter {

    private HierarchicalStreamCopier copier = new HierarchicalStreamCopier();
    private XmlPullParser parser = new MXParser();

    public ChannelConverter(String currentVersion, Mapper mapper) {
        super(currentVersion, mapper);
    }

    @Override
    public boolean canConvert(Class type) {
        return type != null && Channel.class.isAssignableFrom(type);
    }

    @Override
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        if (value instanceof InvalidChannel) {
            try {
                DonkeyElement element = new DonkeyElement(((InvalidChannel) value).getChannelXml());

                for (DonkeyElement child : element.getChildElements()) {
                    copier.copy(new XppReader(new StringReader(child.toXml()), parser), writer);
                }
            } catch (Exception e) {
                throw new SerializerException(e);
            }
        } else {
            super.marshal(value, writer, context);
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        if (reader instanceof DocumentReader) {
            DocumentReader documentReader = (DocumentReader) reader;
            DonkeyElement channel = new DonkeyElement((Element) documentReader.getCurrent());

            try {
                return super.unmarshal(documentReader, context);
            } catch (LinkageError e) {
                return new InvalidChannel(channel, e, documentReader);
            } catch (Exception e) {
                return new InvalidChannel(channel, e, documentReader);
            }
        } else {
            return super.unmarshal(reader, context);
        }
    }
}
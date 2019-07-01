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

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.xmlpull.mxp1.MXParser;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;
import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.mirth.connect.model.InvalidThrowable;
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

public class ThrowableConverter extends ReflectionConverter {

    private HierarchicalStreamCopier copier = new HierarchicalStreamCopier();

    public ThrowableConverter(Mapper mapper) {
        super(mapper, JVM.newReflectionProvider());
    }

    @Override
    public boolean canConvert(Class type) {
        return type != null && Throwable.class.isAssignableFrom(type);
    }

    @Override
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        if (value instanceof InvalidThrowable) {
            try {
                DonkeyElement element = new DonkeyElement(((InvalidThrowable) value).getThrowableXml());

                String version = element.getAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME);
                if (StringUtils.isNotEmpty(version)) {
                    writer.addAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, version);
                }

                for (DonkeyElement child : element.getChildElements()) {
                    copier.copy(new XppReader(new StringReader(child.toXml()), new MXParser()), writer);
                }
            } catch (Exception e) {
                throw new SerializerException(e);
            }
        } else {
            Throwable t = (Throwable) value;

            // For better serialization, initialize the cause and stacktrace
            if (t.getCause() == null) {
                try {
                    t.initCause(null);
                } catch (IllegalStateException e) {
                    // Ignore
                }
            }
            t.getStackTrace();

            super.marshal(value, writer, context);
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        if (reader.underlyingReader() instanceof DocumentReader) {
            DonkeyElement element = new DonkeyElement((Element) ((DocumentReader) reader.underlyingReader()).getCurrent());
            String preUnmarshalXml = null;

            try {
                try {
                    preUnmarshalXml = element.toXml();
                } catch (DonkeyElementException e) {
                }

                return super.unmarshal(reader, context);
            } catch (Throwable t) {
                return new InvalidThrowable(preUnmarshalXml, element, reader);
            }
        } else {
            return super.unmarshal(reader, context);
        }
    }
}

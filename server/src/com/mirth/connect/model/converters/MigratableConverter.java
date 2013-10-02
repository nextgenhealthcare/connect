/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import org.w3c.dom.Element;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.mirth.connect.util.MigrationUtil;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DocumentReader;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * MigratableConverter will be triggered during serialization/deserialization of
 * any classes that implement the Migratable interface. When serializing, it
 * will add a 'version' attribute to the XML node containing the Mith version at
 * the time of serialization. When deserializing, it will check the 'version'
 * attribute to see if the XML data needs to be migrated to the current version.
 * If migration is needed, it will invoke the appropriate migration methods to
 * transform the XML data before deserializing.
 * 
 * @author brentm
 */
public class MigratableConverter extends ReflectionConverter {
    protected String currentVersion;

    public MigratableConverter(String currentVersion, Mapper mapper) {
        super(mapper, new JVM().bestReflectionProvider());
        this.currentVersion = currentVersion;
    }

    @Override
    public boolean canConvert(Class type) {
        return type != null && super.canConvert(type) && Migratable.class.isAssignableFrom(type);
    }

    @Override
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        writer.addAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, currentVersion);
        super.marshal(value, writer, context);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        String version = reader.getAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME);

        /*
         * If the current DOM element contains a version attribute, then check
         * if the element needs to be migrated to the current version. The
         * reader should always be a DocumentReader at this point.
         */
        if (version != null && MigrationUtil.compareVersions(version, currentVersion) < 0 && context.getRequiredType() != null) {
            migrateElement(new DonkeyElement((Element) ((DocumentReader) reader).getCurrent()), version, context.getRequiredType());
        }

        return super.unmarshal(reader, context);
    }

    private void migrateElement(DonkeyElement element, String elementVersion, Class<?> clazz) {
        try {
            Migratable instance = (Migratable) clazz.newInstance();

//            if (MigrationUtil.compareVersions(elementVersion, "3.0.1") < 0) {
//                instance.migrate3_0_1(element);
//            }
//
//            if (MigrationUtil.compareVersions(elementVersion, "3.0.2") < 0) {
//                instance.migrate3_0_2(element);
//            }
        } catch (Exception e) {
            throw new SerializerException("An error occurred while attempting to migrate serialized object element: " + element.getNodeName(), e);
        }
    }
}

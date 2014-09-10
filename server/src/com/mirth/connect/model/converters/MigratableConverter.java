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
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DocumentReader;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * MigratableConverter will be triggered during serialization/deserialization of any classes that
 * implement the Migratable interface. When serializing, it will add a 'version' attribute to the
 * XML node containing the Mith version at the time of serialization. When deserializing, it will
 * check the 'version' attribute to see if the XML data needs to be migrated to the current version.
 * If migration is needed, it will invoke the appropriate migration methods to transform the XML
 * data before deserializing.
 * 
 * @author brentm
 */
public class MigratableConverter extends ReflectionConverter {
    // see comment below in unmarshal()
    private final static String DEFAULT_VERSION = "3.0.0";

    protected String currentVersion;

    public MigratableConverter(String currentVersion, Mapper mapper) {
        super(mapper, JVM.newReflectionProvider());
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
         * If the version attribute is missing, we set it to the default version (3.0.0). The
         * version attribute could be missing for one of two reasons:
         * 
         * 1) The class was recently modified to implement the Migratable interface, so previously
         * serialized instances of the class were not given the 'version' attribute. Setting the
         * version to 3.0.0 will cause it to run the migration methods from 3.0.0 onward. We assume
         * that the migration methods up until the version in which the Migratable implementation
         * was added are empty. (also see comment in the Migratable interface)
         * 
         * 2) The object is being migrated from a pre-3.0.0 version. In this case the object does
         * not have the 'version' attribute since it was not present prior to 3.0.0. At this point
         * we can assume that any pre-3.0.0 objects have been fully migrated to the 3.0.0 structure
         * by the migration code in ImportConverter3_0_0.
         */
        if (version == null) {
            version = DEFAULT_VERSION;
        }

        /*
         * Check if the element needs to be migrated to the current version. The reader should
         * always be a DocumentReader at this point.
         */
        if (MigrationUtil.compareVersions(version, currentVersion) < 0 && context.getRequiredType() != null) {
            migrateElement(new DonkeyElement((Element) ((DocumentReader) reader).getCurrent()), version, context.getRequiredType());

            /*
             * MIRTH-3446: If any migration was performed, we need to tell the DomReader to reload
             * its internal list of child elements (since children may have been added or removed).
             */
            if (reader instanceof MirthDomReader) {
                ((MirthDomReader) reader).reloadCurrentElement();
            }
        }

        return super.unmarshal(reader, context);
    }

    private void migrateElement(DonkeyElement element, String elementVersion, Class<?> clazz) {
        try {
            /*
             * The newInstance() method of a Class object only works if the class has a no argument
             * constructor. XStream is able to get around this limitation by in enhanced mode. Here
             * we piggyback off of XStream and use its reflection provider to instantiate our class.
             * If our class does not have a no argument constructor, we will still be able to create
             * an instance of it if XStream is running in enhanced mode. If the JVM does not support
             * enhanced mode, then XStream would break anyway if it encountered a class without a no
             * argument constructor.
             */
            ReflectionProvider provider = ObjectXMLSerializer.getInstance().getXStream().getReflectionProvider();
            Migratable instance = (Migratable) provider.newInstance(clazz);

            if (MigrationUtil.compareVersions(elementVersion, "3.0.1") < 0) {
                instance.migrate3_0_1(element);
            }

            if (MigrationUtil.compareVersions(elementVersion, "3.0.2") < 0) {
                instance.migrate3_0_2(element);
            }

            if (MigrationUtil.compareVersions(elementVersion, "3.1.0") < 0) {
                instance.migrate3_1_0(element);
            }
        } catch (Exception e) {
            throw new SerializerException("An error occurred while attempting to migrate serialized object element: " + element.getNodeName(), e);
        }
    }
}

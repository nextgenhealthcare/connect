package com.mirth.connect.model.converters;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.util.MigrationUtil;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DocumentReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class MigratableConverter extends ReflectionConverter {
    private String currentVersion;
    private Logger logger = Logger.getLogger(getClass());

    public MigratableConverter(String currentVersion, Mapper mapper) {
        super(mapper, new JVM().bestReflectionProvider());
        this.currentVersion = currentVersion;
    }

    @Override
    public boolean canConvert(Class type) {
        return (super.canConvert(type) && ArrayUtils.contains(type.getInterfaces(), Migratable.class));
    }

    @Override
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        if (context.get("wroteDocumentElement") == null) {
            context.put("wroteDocumentElement", true);
            writer.addAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, currentVersion);
        }
        
        super.marshal(value, writer, context);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        DonkeyElement element = new DonkeyElement((org.w3c.dom.Element) ((DocumentReader) reader).getCurrent());
        logger.debug("Unmarshalling element: " + element.getNodeName());
        
        /*
         * The first element to be unmarshalled should have the "version" attribute. Once the
         * version is read from the attribute, it will be stored in the context since Migratable
         * child nodes will not have the "version" attribute.
         */
        if (context.get("readDocumentElement") == null) {
            String version = element.hasAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME) ? element.getAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME) : null;
            context.put(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME, version);
            context.put("readDocumentElement", true);
        }

        String version = (String) context.get(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME);

        if (version != null && MigrationUtil.compareVersions(version, currentVersion) < 0 && context.getRequiredType() != null) {
            migrateElement(element, version, context.getRequiredType());
        }

        return super.unmarshal(reader, context);
    }

    private void migrateElement(DonkeyElement element, String version, Class<?> clazz) {
        try {
            Migratable instance = (Migratable) clazz.newInstance();

//            if (compareVersions(version, "3.0.1") < 0) {
//                instance.migrate3_0_1(element);
//            }
//
//            if (compareVersions(version, "3.0.2") < 0) {
//                instance.migrate3_0_2(element);
//            }
        } catch (Exception e) {
            logger.error("An error occurred while attempting to migrate serialized object element: " + element.getNodeName(), e);
            // TODO should we throw a runtime exception here, or allow execution to continue?
        }
    }
}

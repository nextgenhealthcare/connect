package com.mirth.connect.model.converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.util.migration.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.model.util.MirthElement;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.core.util.HierarchicalStreams;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DocumentReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class MigratableConverter extends ReflectionConverter {
    // TODO get this from somewhere else, can't use ConfigurationController.getServerVersion() since it may not have initialized yet
    private String currentVersion = "3.0.0";
    private Mapper mapper;
    private String versionAttributeName;
    private Logger logger = Logger.getLogger(getClass());

    public MigratableConverter(Mapper mapper, String versionAttributeName) {
        super(mapper, new JVM().bestReflectionProvider());
        this.mapper = mapper;
        this.versionAttributeName = versionAttributeName;
    }

    @Override
    public boolean canConvert(Class type) {
        return (super.canConvert(type) && ArrayUtils.contains(type.getInterfaces(), Migratable.class));
    }

    @Override
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        writer.addAttribute(versionAttributeName, currentVersion);
        super.marshal(value, writer, context);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        DonkeyElement element = new MirthElement((org.w3c.dom.Element) ((DocumentReader) reader).getCurrent());
        logger.debug("Unmarshalling element: " + element.getNodeName());

        if (element.hasAttribute(versionAttributeName) && compareVersions(element.getAttribute(versionAttributeName), currentVersion) < 0) {
            String classAttribute = HierarchicalStreams.readClassAttribute(reader, mapper);
            Class<?> type = (classAttribute == null) ? null : mapper.realClass(classAttribute);

            if (type != null) {
                migrateElement(element, type);
            }
        }

        return super.unmarshal(reader, context);
    }

    private void migrateElement(DonkeyElement element, Class<?> clazz) {
        String elementVersion = element.getAttribute(versionAttributeName);

        try {
            Migratable instance = (Migratable) clazz.newInstance();

//            if (compareVersions(elementVersion, "3.0.1") < 0) {
//                instance.migrate3_0_1(element);
//                element.setAttribute(versionAttributeName, "3.0.1");
//            }
//
//            if (compareVersions(elementVersion, "3.0.2") < 0) {
//                instance.migrate3_0_2(element);
//                element.setAttribute(versionAttributeName, "3.0.2");
//            }
        } catch (Exception e) {
            logger.error("An error occurred while attempting to migrate serialized object element: " + element.getNodeName(), e);
            // TODO should we throw a runtime exception here, or allow execution to continue?
        }
    }

    /**
     * Compares two versions strings (ex. 1.6.1.2335).
     * 
     * @param version1
     * @param version2
     * @return -1 if version1 < version2, 1 if version1 > version2, 0 if
     *         version1 = version2
     */
    public static int compareVersions(String version1, String version2) {
        if ((version1 == null) && (version2 == null)) {
            return 0;
        } else if ((version1 != null) && (version2 == null)) {
            return 1;
        } else if ((version1 == null) && (version2 != null)) {
            return -1;
        } else {
            String[] numbers1 = normalizeVersion(version1, 4).split("\\.");
            String[] numbers2 = normalizeVersion(version2, 4).split("\\.");

            for (int i = 0; i < numbers1.length; i++) {
                if (Integer.valueOf(numbers1[i]) < Integer.valueOf(numbers2[i])) {
                    return -1;
                } else if (Integer.valueOf(numbers1[i]) > Integer.valueOf(numbers2[i])) {
                    return 1;
                }
            }
        }

        return 0;
    }

    public static String normalizeVersion(String version, int length) {
        List<String> numbers = new ArrayList<String>(Arrays.asList(version.split("\\.")));

        while (numbers.size() < length) {
            numbers.add("0");
        }

        StringBuilder builder = new StringBuilder();

        for (ListIterator<String> iterator = numbers.listIterator(); iterator.hasNext() && iterator.nextIndex() < length;) {
            String number = iterator.next();

            if (iterator.hasNext()) {
                builder.append(number + ".");
            } else {
                builder.append(number);
            }
        }

        return builder.toString();
    }
}

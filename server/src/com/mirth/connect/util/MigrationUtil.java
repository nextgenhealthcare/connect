/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class MigrationUtil {
    /**
     * Serializes a DOM element into an XML string
     */
    public static String elementToXml(Element element) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        document.appendChild(document.importNode(element, true));
        return new DocumentSerializer().toXML(document);
    }
    
    /**
     * Deserializes a DOM element from an XML string
     */
    public static Element elementFromXml(String xml) {
        return new DocumentSerializer().fromXML(xml).getDocumentElement();
    }
    
    /**
     * Extract the version string from an XML serialized object
     */
    public static String getSerializedObjectVersion(String serializedObject) {
        DonkeyElement element = new DonkeyElement(elementFromXml(serializedObject));

        // Objects serialized by version >= 3.0.0 should have a version attribute on the root element
        if (element.hasAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME)) {
            return element.getAttribute(ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME);
        }

        // Pre-3.0.0 objects might have a 'version' child node, check for it
        DonkeyElement versionElement = element.getChildElement("version");

        if (versionElement != null) {
            return versionElement.getTextContent();
        }

        return null;
    }
    
    /**
     * Compares two versions strings (ex. 1.6.1)
     * 
     * @return -1 if version1 < version2, 1 if version1 > version2, 0 if
     *         version1 = version2
     */
    public static int compareVersions(String version1, String version2) {
        return compareVersions(version1, version2, 3);
    }
    
    /**
     * Compares two versions strings (ex. 1.6.1)
     * 
     * @return -1 if version1 < version2, 1 if version1 > version2, 0 if
     *         version1 = version2
     */
    public static int compareVersions(String version1, String version2, int length) {
        if ((version1 == null) && (version2 == null)) {
            return 0;
        } else if ((version1 != null) && (version2 == null)) {
            return 1;
        } else if ((version1 == null) && (version2 != null)) {
            return -1;
        } else {
            String[] numbers1 = normalizeVersion(version1, length).split("\\.");
            String[] numbers2 = normalizeVersion(version2, length).split("\\.");

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

    /**
     * Normalizes a version string so that it has 'length' number of version numbers separated by '.'
     */
    public static String normalizeVersion(String version, int length) {
        List<String> numbers = new ArrayList<String>(Arrays.asList(version.split("\\.")));

        while (numbers.size() < length) {
            numbers.add("0");
        }

        StringBuilder builder = new StringBuilder();

        for (ListIterator<String> iterator = numbers.listIterator(); iterator.hasNext() && iterator.nextIndex() < length;) {
            String number = iterator.next();

            if (iterator.hasNext() && iterator.nextIndex() < length) {
                builder.append(number + ".");
            } else {
                builder.append(number);
            }
        }

        return builder.toString();
    }
}

/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class MigrationUtil {
    /**
     * Extract the version string from an XML serialized object
     */
    public static String getSerializedObjectVersion(String serializedObject) throws Exception {
        String version = getRootNodeAttribute(serializedObject, ObjectXMLSerializer.VERSION_ATTRIBUTE_NAME);

        if (version != null) {
            return version;
        }
        
        /*
         * Pre-3.0.0 objects might have a 'version' child node, check for it. The reason we
         * don't create a DonkeyElement above is to avoid parsing the entire XML string.
         */
        DonkeyElement element = new DonkeyElement(serializedObject);

        for (DonkeyElement child : element.getChildElements()) {
            if (child.getNodeName().equals("version")) {
                return child.getTextContent();
            }
        }

        return null;
    }
    
    private static String getRootNodeAttribute(String serializedObject, String attributeName) throws Exception {
        XmlPullParser parser = new MXParser();
        parser.setInput(new StringReader(serializedObject));
        
        while (parser.getEventType() != XmlPullParser.START_TAG) {
            parser.next();
        }
        
        int attrCount = parser.getAttributeCount();
        
        for (int i = 0; i < attrCount; i++) {
            if (parser.getAttributeName(i).equals(attributeName)) {
                return parser.getAttributeValue(i);
            }
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
        if (version == null) {
            return null;
        }
        
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

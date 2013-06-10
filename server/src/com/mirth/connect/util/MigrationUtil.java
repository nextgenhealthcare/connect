package com.mirth.connect.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mirth.connect.model.converters.DocumentSerializer;

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
     * Compares two versions strings (ex. 1.6.1)
     * 
     * @return -1 if version1 < version2, 1 if version1 > version2, 0 if
     *         version1 = version2
     */
    public static int compareVersions(String version1, String version2) {
        return compareVersions(version1, version2);
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

            if (iterator.hasNext()) {
                builder.append(number + ".");
            } else {
                builder.append(number);
            }
        }

        return builder.toString();
    }
}

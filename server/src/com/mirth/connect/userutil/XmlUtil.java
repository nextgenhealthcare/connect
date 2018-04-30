/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.userutil;

import com.mirth.connect.util.JsonXmlUtil;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;

/**
 * Provides XML utility methods.
 */
public class XmlUtil {
    private XmlUtil() {}

    /**
     * Formats an XML string with indented markup.
     * 
     * @param input
     *            The XML string to format.
     * @return The formatted XML string.
     */
    public static String prettyPrint(String input) {
        return com.mirth.connect.util.MirthXmlUtil.prettyPrint(input);
    }

    /**
     * Converts an XML/HTML entity reference into a string with the literal character.
     * 
     * @param entity
     *            The XML/HTML entity to decode.
     * @return A string containing the decoded character.
     */
    public static String decode(String entity) {
        return com.mirth.connect.util.MirthXmlUtil.decode(entity);
    }

    /**
     * Encodes a character into the corresponding XML/HTML entity.
     * 
     * @param s
     *            The character to encode.
     * @return The encoded XML/HTML entity.
     */
    public static String encode(char s) {
        return com.mirth.connect.util.MirthXmlUtil.encode(s);
    }

    /**
     * Converts a string, encoding characters into the corresponding XML/HTML entities as needed.
     * 
     * @param s
     *            The string to encode.
     * @return The encoded string with replaced XML/HTML entities.
     */
    public static String encode(String s) {
        return com.mirth.connect.util.MirthXmlUtil.encode(s);
    }

    /**
     * Converts a character array, encoding characters into the corresponding XML/HTML entities as
     * needed.
     * 
     * @param text
     *            The character array to encode.
     * @param start
     *            The index to start at in the character array.
     * @param length
     *            The maximum amount of characters to read from the array.
     * @return The encoded string with replaced XML/HTML entities.
     */
    public static String encode(char[] text, int start, int length) {
        return com.mirth.connect.util.MirthXmlUtil.encode(text, start, length);
    }

    /**
     * Converts an XML string to JSON, while stripping bound namespace prefixes.
     * 
     * @param xmlString
     *            The XML string to convert.
     * @return The converted JSON string.
     * @throws Exception
     *             If conversion failed.
     */
    public static String toJson(String xmlString) throws Exception {
        return JsonXmlUtil.xmlToJson(xmlString, true);
    }

    /**
     * Converts an XML string to JSON.
     * 
     * @param xmlString
     *            The XML string to convert.
     * @param normalizeNamespaces
     *            Whether or not to normalize namespaces by stripping prefixes.
     * @return The converted JSON string.
     * @throws Exception
     *             If conversion failed.
     */
    public static String toJson(String xmlString, boolean normalizeNamespaces) throws Exception {
        return JsonXmlUtil.xmlToJson(xmlString, normalizeNamespaces);
    }

    /**
     * Converts an XML string to JSON.
     * 
     * @param xmlString
     *            The XML string to convert.
     * @param autoArray
     *            If true, sibling nodes with the same tag name will be consolidated into a JSON
     *            array. If false, multiple properties with the same name will be present.
     * @param autoPrimitive
     *            If true, element text will be converted to JSON primitive values where applicable.
     *            If false, element text will always be converted to string values.
     * @param prettyPrint
     *            Whether or not to fully indent the JSON output.
     * @param normalizeNamespaces
     *            Whether or not to normalize namespaces by stripping prefixes.
     * @return The converted JSON string.
     * @throws Exception
     *             If conversion failed.
     */
    public static String toJson(String xmlString, boolean autoArray, boolean autoPrimitive, boolean prettyPrint, boolean normalizeNamespaces) throws Exception {
        JsonXMLConfig config = new JsonXMLConfigBuilder().autoArray(autoArray).autoPrimitive(autoPrimitive).prettyPrint(prettyPrint).build();
        return JsonXmlUtil.xmlToJson(config, xmlString, normalizeNamespaces);
    }
}
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
import com.mirth.connect.util.MirthJsonUtil;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;

/**
 * Provides JSON utility methods.
 */
public class JsonUtil {
    private JsonUtil() {}

    /**
     * Formats an JSON string with indented markup.
     * 
     * @param input
     *            The JSON string to format.
     * @return The formatted JSON string.
     */
    public static String prettyPrint(String input) {
        return MirthJsonUtil.prettyPrint(input);
    }

    /**
     * Escapes any special JSON characters in the input.
     * 
     * @param input
     *            The string to escape.
     * @return The escaped string.
     */
    public static String escape(String input) {
        return MirthJsonUtil.escape(input);
    }

    /**
     * Converts a JSON string to XML. This is the same as calling toXml(String jsonString, boolean
     * multiplePI, boolean prettyPrint) with multiplePI = false and prettyPrint = false
     * 
     * @param jsonString
     *            The JSON string to convert.
     * @return The converted XML string.
     * @throws Exception
     *             If the conversion failed.
     */
    public static String toXml(String jsonString) throws Exception {
        return toXml(jsonString, false, false);
    }

    /**
     * Converts a JSON string to XML.
     * 
     * @param jsonString
     *            The JSON string to convert.
     * @param multiplePI
     *            If true, the <code>&lt;? xml-multiple ... ?&gt;</code> processing instruction will
     *            be included for arrays.
     * @param prettyPrint
     *            Whether or not to fully indent the XML output.
     * @return The converted XML string.
     * @throws Exception
     *             If the conversion failed.
     */
    public static String toXml(String jsonString, boolean multiplePI, boolean prettyPrint) throws Exception {
        JsonXMLConfig config = new JsonXMLConfigBuilder().multiplePI(multiplePI).prettyPrint(prettyPrint).build();
        return JsonXmlUtil.jsonToXml(config, jsonString);
    }
}

/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class MirthJsonUtil {

    private static Logger logger = Logger.getLogger(MirthJsonUtil.class);

    public static String prettyPrint(String input) {
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            // Modified Jackson's default pretty printer to separate each array element onto its own line
            DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
            prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
            JsonNode json = mapper.readTree(input);

            return mapper.writer(prettyPrinter).writeValueAsString(json);
        } catch (Exception e) {
            logger.warn("Error pretty printing json.", e);
        }

        return input;
    }

    public static String escape(String input) {
        return StringEscapeUtils.escapeJson(input);
    }
}

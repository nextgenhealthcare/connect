/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.util.Properties;

import com.mirth.connect.plugins.datatypes.edi.EDISerializer;
import com.mirth.connect.plugins.datatypes.hl7v2.ER7Serializer;
import com.mirth.connect.plugins.datatypes.ncpdp.NCPDPSerializer;
import com.mirth.connect.plugins.datatypes.x12.X12Serializer;


public class SerializerFactory {

    // TODO: Remove in 3.0
    @Deprecated
    public static ER7Serializer getHL7Serializer(boolean useStrictParser, boolean useStrictValidation, boolean handleRepetitions, boolean convertLFtoCR, boolean handleSubcomponents) {
        Properties properties = new Properties();
        properties.put("useStrictParser", Boolean.toString(useStrictParser));
        properties.put("useStrictValidation", Boolean.toString(useStrictValidation));
        properties.put("handleRepetitions", Boolean.toString(handleRepetitions));
        if (convertLFtoCR) {
            properties.put("inputSegmentDelimiter", "\\r\\n|\\r|\\n");
        } else {
            properties.put("inputSegmentDelimiter", "\\r");
        }
        properties.put("outputSegmentDelimiter", "\\r");
        properties.put("handleSubcomponents", Boolean.toString(handleSubcomponents));
        return new ER7Serializer(properties);
    }

    @Deprecated
    public static ER7Serializer getHL7Serializer(boolean useStrictParser, boolean useStrictValidation, boolean handleRepetitions, boolean convertLFtoCR) {
        Properties properties = new Properties();
        properties.put("useStrictParser", Boolean.toString(useStrictParser));
        properties.put("useStrictValidation", Boolean.toString(useStrictValidation));
        properties.put("handleRepetitions", Boolean.toString(handleRepetitions));
        if (convertLFtoCR) {
            properties.put("inputSegmentDelimiter", "\\r\\n|\\r|\\n");
        } else {
            properties.put("inputSegmentDelimiter", "\\r");
        }
        properties.put("outputSegmentDelimiter", "\\r");
        return new ER7Serializer(properties);
    }

    @Deprecated
    public static ER7Serializer getHL7Serializer(boolean useStrictParser, boolean useStrictValidation, boolean handleRepetitions) {
        Properties properties = new Properties();
        properties.put("useStrictParser", Boolean.toString(useStrictParser));
        properties.put("useStrictValidation", Boolean.toString(useStrictValidation));
        properties.put("handleRepetitions", Boolean.toString(handleRepetitions));
        return new ER7Serializer(properties);
    }

    @Deprecated
    public static ER7Serializer getHL7Serializer(boolean useStrictParser, boolean useStrictValidation) {
        Properties properties = new Properties();
        properties.put("useStrictParser", Boolean.toString(useStrictParser));
        properties.put("useStrictValidation", Boolean.toString(useStrictValidation));
        return new ER7Serializer(properties);
    }

    @Deprecated
    public static ER7Serializer getHL7Serializer() {
        return new ER7Serializer(new Properties());
    }

    @Deprecated
    public static X12Serializer getX12Serializer(boolean inferDelimiters) {
        Properties properties = new Properties();
        properties.put("inferDelimiters", Boolean.toString(inferDelimiters));
        return new X12Serializer(properties);
    }

    @Deprecated
    public static EDISerializer getEDISerializer(String segmentDelim, String elementDelim, String subelementDelim) {
        Properties properties = new Properties();
        properties.put("segmentDelimiter", segmentDelim);
        properties.put("elementDelimiter", elementDelim);
        properties.put("subelementDelimiter", subelementDelim);
        return new EDISerializer(properties);
    }

    @Deprecated
    public static NCPDPSerializer getNCPDPSerializer(String segmentDelim, String groupDelim, String fieldDelim) {
        Properties properties = new Properties();
        properties.put("segmentDelimiter", segmentDelim);
        properties.put("groupDelimiter", groupDelim);
        properties.put("fieldDelimiter", fieldDelim);
        return new NCPDPSerializer(properties);
    }

    @Deprecated
    public static NCPDPSerializer getNCPDPSerializer(String segmentDelim, String groupDelim, String fieldDelim, boolean useStrictValidation) {
        Properties properties = new Properties();
        properties.put("segmentDelimiter", segmentDelim);
        properties.put("groupDelimiter", groupDelim);
        properties.put("fieldDelimiter", fieldDelim);
        properties.put("useStrictValidation", Boolean.toString(useStrictValidation));
        return new NCPDPSerializer(properties);
    }
}
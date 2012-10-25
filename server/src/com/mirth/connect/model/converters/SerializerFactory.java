/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.util.Map;
import java.util.Properties;

import com.mirth.connect.model.converters.delimited.DelimitedSerializer;
import com.mirth.connect.model.converters.dicom.DICOMSerializer;
import com.mirth.connect.model.converters.edi.EDISerializer;
import com.mirth.connect.model.converters.hl7v2.ER7Serializer;
import com.mirth.connect.model.converters.hl7v3.HL7V3Serializer;
import com.mirth.connect.model.converters.ncpdp.NCPDPSerializer;
import com.mirth.connect.model.converters.x12.X12Serializer;
import com.mirth.connect.model.converters.xml.DefaultXMLSerializer;


public class SerializerFactory {
    public static IXMLSerializer getSerializer(String dataType, Map<?, ?> properties) {
        if (dataType.equals(DataTypeFactory.HL7V2)) {
            return new ER7Serializer(properties);
        } else if (dataType.equals(DataTypeFactory.HL7V3)) {
            return new HL7V3Serializer(properties);
        } else if (dataType.equals(DataTypeFactory.X12)) {
            return new X12Serializer(properties);
        } else if (dataType.equals(DataTypeFactory.EDI)) {
            return new EDISerializer(properties);
        } else if (dataType.equals(DataTypeFactory.NCPDP)) {
            return new NCPDPSerializer(properties);
        } else if (dataType.equals(DataTypeFactory.DICOM)) {
            return new DICOMSerializer(properties);
        } else if (dataType.equals(DataTypeFactory.DELIMITED)) {
            return new DelimitedSerializer(properties);
        } else {
            return new DefaultXMLSerializer(properties);
        }
    }

    // TODO: Remove in 3.0
    @Deprecated
    public static ER7Serializer getHL7Serializer(boolean useStrictParser, boolean useStrictValidation, boolean handleRepetitions, boolean convertLFtoCR, boolean handleSubcomponents) {
        Properties properties = new Properties();
        properties.put("useStrictParser", Boolean.toString(useStrictParser));
        properties.put("useStrictValidation", Boolean.toString(useStrictValidation));
        properties.put("handleRepetitions", Boolean.toString(handleRepetitions));
        properties.put("convertLFtoCR", Boolean.toString(convertLFtoCR));
        properties.put("handleSubcomponents", Boolean.toString(handleSubcomponents));
        return new ER7Serializer(properties);
    }

    @Deprecated
    public static ER7Serializer getHL7Serializer(boolean useStrictParser, boolean useStrictValidation, boolean handleRepetitions, boolean convertLFtoCR) {
        Properties properties = new Properties();
        properties.put("useStrictParser", Boolean.toString(useStrictParser));
        properties.put("useStrictValidation", Boolean.toString(useStrictValidation));
        properties.put("handleRepetitions", Boolean.toString(handleRepetitions));
        properties.put("convertLFtoCR", Boolean.toString(convertLFtoCR));
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
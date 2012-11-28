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

import com.mirth.connect.donkey.model.message.DataType;

public class DataTypeFactory {
    public static final String HL7V2 = "HL7V2";
    public static final String X12 = "X12";
    public static final String XML = "XML";
    public static final String HL7V3 = "HL7V3";
    public static final String EDI = "EDI";
    public static final String NCPDP = "NCPDP";
    public static final String DICOM = "DICOM";
    public static final String DELIMITED = "DELIMITED";

    public static DataType getDataType(String dataType, Properties properties) {
        return new DataType(dataType, SerializerFactory.getSerializer(dataType, properties), AutoResponderFactory.getAutoResponder(dataType, properties));
    }

    public static String[] getDataTypeNames() {
        return new String[] { HL7V2, X12, XML, HL7V3, EDI, NCPDP, DICOM, DELIMITED };
    }
}

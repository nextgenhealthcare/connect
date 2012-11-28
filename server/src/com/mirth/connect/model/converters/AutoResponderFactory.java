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

import com.mirth.connect.donkey.model.message.AutoResponder;

public class AutoResponderFactory {

    public static AutoResponder getAutoResponder(String dataType, Map<?, ?> properties) {
//        if (dataType.equals(DataTypeFactory.HL7V2)) {
//            return new HL7v2AutoResponder(properties);
//        } else if (dataType.equals(DataTypeFactory.HL7V3)) {
//            return new HL7V3AutoResponder(properties);
//        } else if (dataType.equals(DataTypeFactory.X12)) {
//            return new X12AutoResponder(properties);
//        } else if (dataType.equals(DataTypeFactory.EDI)) {
//            return new EDIAutoResponder(properties);
//        } else if (dataType.equals(DataTypeFactory.NCPDP)) {
//            return new NCPDPAutoResponder(properties);
//        } else if (dataType.equals(DataTypeFactory.DICOM)) {
//            return new DICOMAutoResponder(properties);
//        } else if (dataType.equals(DataTypeFactory.DELIMITED)) {
//            return new DelimitedAutoResponder(properties);
//        } else {
//            return new DefaultAutoResponder();
//        }
        return new DefaultAutoResponder();
    }
}

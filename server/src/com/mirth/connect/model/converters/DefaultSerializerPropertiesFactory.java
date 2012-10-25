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

import com.mirth.connect.model.converters.delimited.DelimitedSerializer;
import com.mirth.connect.model.converters.dicom.DICOMSerializer;
import com.mirth.connect.model.converters.edi.EDISerializer;
import com.mirth.connect.model.converters.hl7v2.ER7Serializer;
import com.mirth.connect.model.converters.hl7v3.HL7V3Serializer;
import com.mirth.connect.model.converters.ncpdp.NCPDPSerializer;
import com.mirth.connect.model.converters.x12.X12Serializer;
import com.mirth.connect.model.converters.xml.DefaultXMLSerializer;

public class DefaultSerializerPropertiesFactory {
	public static Map<String, String> getDefaultSerializerProperties(String dataType) {
		if (dataType.equals(DataTypeFactory.HL7V2)) {
			return ER7Serializer.getDefaultProperties();
		} else if (dataType.equals(DataTypeFactory.HL7V3)) {
			return HL7V3Serializer.getDefaultProperties();
		} else if (dataType.equals(DataTypeFactory.X12)) {
			return X12Serializer.getDefaultProperties();
		} else if (dataType.equals(DataTypeFactory.EDI)) {
			return EDISerializer.getDefaultProperties();
		} else if (dataType.equals(DataTypeFactory.NCPDP)) {
			return NCPDPSerializer.getDefaultProperties();
		} else if (dataType.equals(DataTypeFactory.DICOM)) {
			return DICOMSerializer.getDefaultProperties();
		} else if (dataType.equals(DataTypeFactory.DELIMITED)) {
			return DelimitedSerializer.getDefaultProperties();
		} else {
			return DefaultXMLSerializer.getDefaultProperties();
		}
	}
}
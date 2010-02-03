/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.model.converters;

import java.util.Map;

import com.webreach.mirth.model.MessageObject.Protocol;

public class DefaultSerializerPropertiesFactory {
	public static Map<String, String> getDefaultSerializerProperties(Protocol protocol) {
		if (protocol.equals(Protocol.HL7V2)) {
			return ER7Serializer.getDefaultProperties();
		} else if (protocol.equals(Protocol.HL7V3)) {
			return HL7V3Serializer.getDefaultProperties();
		} else if (protocol.equals(Protocol.X12)) {
			return X12Serializer.getDefaultProperties();
		} else if (protocol.equals(Protocol.EDI)) {
			return EDISerializer.getDefaultProperties();
		} else if (protocol.equals(Protocol.NCPDP)) {
			return NCPDPSerializer.getDefaultProperties();
		} else if (protocol.equals(Protocol.DICOM)) {
			return DICOMSerializer.getDefaultProperties();
		} else if (protocol.equals(Protocol.DELIMITED)) {
			return DelimitedSerializer.getDefaultProperties();
		} else {
			return DefaultXMLSerializer.getDefaultProperties();
		}
	}
}
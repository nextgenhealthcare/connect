/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.mule.adaptors;

import com.mirth.connect.model.MessageObject.Protocol;

public class AdaptorFactory {
	public static Adaptor getAdaptor(Protocol protocol) {
		if (protocol.equals(Protocol.HL7V2)) {
			return new HL7v2Adaptor();
		} else if (protocol.equals(Protocol.HL7V3)) {
			return new HL7v3Adaptor();
		} else if (protocol.equals(Protocol.X12)){
			return new X12Adaptor();
		} else if (protocol.equals(Protocol.EDI)){
			return new EDIAdaptor();
		} else if (protocol.equals(Protocol.NCPDP)){
			return new NCPDPAdaptor();
		} else if (protocol.equals(Protocol.DICOM)){
			return new DICOMAdaptor();
		} else if (protocol.equals(Protocol.DELIMITED)){
			return new DelimitedAdaptor();
		} else {
			return new XMLAdaptor();
		}
	}
}

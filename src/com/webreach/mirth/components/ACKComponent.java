/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.components;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.Callable;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.primitive.CommonTS;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.sourcegen.SourceGenerator;
import ca.uhn.hl7v2.util.MessageIDGenerator;
import ca.uhn.hl7v2.util.Terser;

public class ACKComponent implements Callable {
	protected static transient Log logger = LogFactory.getLog(ACKComponent.class);

	public Object onCall(UMOEventContext context) throws Exception {
		return generateAckResponse(context.getTransformedMessageAsString());
	}
	
	private String generateAckResponse(String source) throws Exception {
		PipeParser pipeParser = new PipeParser();
		Message message = pipeParser.parse(source);

		Segment inboundHeader = (Segment) message.get("MSH");

		if (!inboundHeader.getName().equals("MSH"))
			throw new HL7Exception("Need an MSH segment to create a response ACK (got " + inboundHeader.getName() + ")");

		// make ACK of correct version
		String version = null;

		try {
			version = Terser.get(inboundHeader, 12, 0, 1, 1);
		} catch (HL7Exception e) {
			/* proceed with null */
		}

		// default version is 2.4
		if (version == null)
			version = "2.4";

		String ackClassName = SourceGenerator.getVersionPackageName(version) + "message.ACK";

		Message out = null;

		try {
			Class ackClass = Class.forName(ackClassName);
			out = (Message) ackClass.newInstance();
		} catch (Exception e) {
			throw new HL7Exception("Can't instantiate ACK of class " + ackClassName + ": " + e.getClass().getName());
		}

		Terser terser = new Terser(out);

		// populate outbound MSH using data from inbound message ...
		Segment outHeader = (Segment) out.get("MSH");
		fillResponseHeader(inboundHeader, outHeader);
		terser.set("/MSH-9", "ACK");
		terser.set("/MSH-12", version);
		terser.set("/MSA-1", "AA");
		terser.set("/MSA-2", Terser.get(inboundHeader, 10, 0, 1, 1));

		// encode the message to a String
		return pipeParser.encode(out);
	}
	
    private void fillResponseHeader(Segment inbound, Segment outbound) throws HL7Exception, IOException {
        if (!inbound.getName().equals("MSH") || !outbound.getName().equals("MSH"))
            throw new HL7Exception("Need MSH segments.  Got " + inbound.getName() + " and " + outbound.getName());

        //get MSH data from incoming message ...        
        String encChars = Terser.get(inbound, 2, 0, 1, 1);
        String fieldSep = Terser.get(inbound, 1, 0, 1, 1);
        String procID = Terser.get(inbound, 11, 0, 1, 1);

        //populate outbound MSH using data from inbound message ...                     
        Terser.set(outbound, 2, 0, 1, 1, encChars);
        Terser.set(outbound, 1, 0, 1, 1, fieldSep);
        GregorianCalendar now = new GregorianCalendar();
        now.setTime(new Date());
        Terser.set(outbound, 7, 0, 1, 1, CommonTS.toHl7TSFormat(now));
        Terser.set(outbound, 10, 0, 1, 1, MessageIDGenerator.getInstance().getNewID());
        Terser.set(outbound, 11, 0, 1, 1, procID);
    }
}

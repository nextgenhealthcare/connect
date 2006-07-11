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


package com.webreach.mirth.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.Callable;

import ca.uhn.hl7v2.app.DefaultApplication;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.parser.PipeParser;

public class ACKGenerator implements Callable {
	protected static transient Log logger = LogFactory.getLog(ACKGenerator.class);

	public Object onCall(UMOEventContext context) throws Exception {
		return generateAckResponse(context.getTransformedMessageAsString());
	}
	
	public String generateAckResponse(String message) throws Exception {
		PipeParser parser = new PipeParser();
		Segment header = parser.getCriticalResponseData(message);
	
        Message response = DefaultApplication.makeACK(header);
        String originalEncoding = parser.getEncoding(message);
        String ackMessage = parser.encode(response, originalEncoding);
        logger.debug(ackMessage);

        return ackMessage;
	}
}

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


package com.webreach.mirth.transformers;

import java.io.OutputStream;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mule.umo.UMOEventContext;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;

import com.webreach.mirth.managers.LogManager;
import com.webreach.mirth.managers.MessageManager;
import com.webreach.mirth.managers.types.MirthMessage;

/**
 * Performs JavaScript transformation on incoming HL7 message.
 *  
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 * @since 1.0
 */
public class NullTransformer {
	protected transient Log logger = LogFactory.getLog(InboundECMAScriptTransformer.class);
	private String script;
	
	private MessageManager messageManager = MessageManager.getInstance();
	private LogManager logManager = LogManager.getInstance();

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public Object onCall(UMOEventContext eventContext) throws Exception {
		
		return eventContext.getMessageAsBytes();
	}

}

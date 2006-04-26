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


package com.webreach.mirth.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mule.umo.UMOFilter;
import org.mule.umo.UMOMessage;

/**
 * Filters incoming messages based on user-defined JavaScript
 *  
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 * @since 1.0
 */
public class ECMAScriptFilter implements UMOFilter {
	protected static transient Log logger = LogFactory.getLog(ECMAScriptFilter.class);
	private String script;

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}
	
	public boolean accept(UMOMessage umoMessage) {
		try {
			String message = (String) umoMessage.getPayloadAsString();

			Context context = Context.enter();
			Scriptable scope = context.initStandardObjects();
			
			scope.put("message", scope, message);
			scope.put("logger", scope, logger);
			
			String jsSource = "function debug(debug_message) { logger.debug(debug_message) } function doFilter() { default xml namespace = new Namespace(\"urn:hl7-org:v2xml\"); var msg = new XML(message); " + script + " } doFilter()";
			Object result = context.evaluateString(scope, jsSource, "<cmd>", 1, null);
			
			return ((Boolean) Context.jsToJava(result, java.lang.Boolean.class)).booleanValue();
		} catch (Exception e) {
			logger.error(e.getMessage());
			
			return false;
		} finally {
			Context.exit();
		}
	}
}

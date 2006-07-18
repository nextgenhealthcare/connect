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


package com.webreach.wrhs.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;

public class RhinoTest {
	protected static transient Log logger = LogFactory.getLog(RhinoTest.class);

	public static void main(String[] args) {

		Context cx = Context.enter();
		
		try {
			Scriptable scope = cx.initStandardObjects();
			scope.put("logger", scope, logger);
			String code = "function myFunction() { test=3; test=4; return test } myFunction()";
			Object resultObj = cx.evaluateString(scope, code, "<cmd>", 1, null);

			System.out.println("TRANSFORMED MESSAGE: " + Context.jsToJava(resultObj, java.lang.String.class));
		} catch(RhinoException err) {
			System.out.println("[line " + err.lineNumber() + "]: " + err.details());  
		} finally {
			Context.exit();
		}
	}
}

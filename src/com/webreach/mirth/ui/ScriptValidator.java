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


package com.webreach.mirth.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Scriptable;

public class ScriptValidator extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Log logger = LogFactory.getLog(ScriptValidator.class);

		try {
			// response.setContentType("application/x-java-serialized-object");
			response.setContentType("text/plain");

			// read a String-object from applet
			// instead of a String-object, you can transmit any object, which
			// is known to the servlet and to the applet
			InputStream in = request.getInputStream();
			ObjectInputStream inputFromApplet = new ObjectInputStream(in);
			String script = (String) inputFromApplet.readObject();
			Context cx = Context.enter();

			try {
				Scriptable scope = cx.initStandardObjects();
				scope.put("logger", scope, logger);
				cx.evaluateString(scope, script, "<cmd>", 1, null);

			} catch (EcmaError e) {
				writeErrorToConsole("[line " + e.lineNumber() + "]: " + e.details(), response);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Context.exit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void writeErrorToConsole(String error, HttpServletResponse response) throws Exception {
		OutputStream outstr = response.getOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(outstr);
		oos.writeObject(error);
		oos.flush();
		oos.close();
	}

	public String getServletInfo() {
		return "A simple servlet";
	}
}

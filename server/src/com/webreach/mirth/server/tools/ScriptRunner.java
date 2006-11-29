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


package com.webreach.mirth.server.tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.derby.tools.ij;
import org.apache.log4j.Logger;

public class ScriptRunner {
	private static Logger logger = Logger.getLogger("ScriptRunner");

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: java ScriptRunner scriptFile");
		} else {
			runScript(args[0]);
		}
	}

	public static void runScript(String scriptFile) {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:derby:mirthdb;create=true"); 
			DataInputStream in = new DataInputStream(new FileInputStream(scriptFile));
			DataOutputStream out = new DataOutputStream(new FileOutputStream("scriptrunner.log"));
			ij.runScript(connection, in, "UTF-8", out, "UTF-8");
		} catch (Exception e) {
			logger.error("error executing script", e);
		}
	}
}

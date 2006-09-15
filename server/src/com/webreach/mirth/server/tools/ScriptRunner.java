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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.log4j.Logger;

import com.webreach.mirth.server.util.DatabaseConnection;
import com.webreach.mirth.server.util.DatabaseConnectionFactory;
import com.webreach.mirth.server.util.DatabaseUtil;

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
		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			StringBuilder scriptContent = new StringBuilder();

			BufferedReader reader = new BufferedReader(new FileReader(new File(scriptFile)));
			String line = null;

			while ((line = reader.readLine()) != null) {
				scriptContent.append(line + "\n");
			}

			reader.close();

			logger.info("executing script '" + scriptFile + "' on database '" + dbConnection.getAddress() + "'");

			dbConnection.executeUpdate(scriptContent.toString());
			dbConnection.executeUpdate("shutdown");
		} catch (Exception e) {
			logger.error("error executing script", e);
		} finally {
			DatabaseUtil.close(dbConnection);
		}
	}
}

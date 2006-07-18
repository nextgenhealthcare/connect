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

package com.webreach.mirth.server.util;

import java.sql.SQLException;
import java.util.Properties;

import com.webreach.mirth.util.PropertyLoader;


public class DatabaseConnectionFactory {
	private static boolean isDriverRegistered = false;
	private static Properties properties = PropertyLoader.loadProperties("mirth");
	
	public static DatabaseConnection createDatabaseConnection() throws SQLException {
		if (!isDriverRegistered) {
			try {
				Class.forName(properties.getProperty("database.driver"));
				isDriverRegistered = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Properties info = new Properties();
		info.setProperty("user", "sa");
		info.setProperty("password", "");
		info.setProperty("shutdown", "true");

		return new DatabaseConnection(properties.getProperty("database.url"), info);
	}
}

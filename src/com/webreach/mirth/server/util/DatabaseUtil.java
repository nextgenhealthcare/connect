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

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.GregorianCalendar;

public class DatabaseUtil {
	
	/**
	 * Closes the specified ResultSet.
	 * 
	 * @param result the ResultSet to be closed.
	 * @throws RuntimeException
	 */
	public static void close(ResultSet result) throws RuntimeException {
		try {
			result.close();
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}
	
	/**
	 * Closes the specified Statement.
	 * 
	 * @param statement the Statement to be closed.
	 * @throws RuntimeException
	 */
	public static void close(Statement statement) throws RuntimeException {
		try {
			statement.close();
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}
	
	/**
	 * Returns a String representation of a SQL Timestamp with the current time.
	 * 
	 * @return a String representation of a SQL Timestamp with the current time.
	 */
	public static String getNowTimestamp() {
		return (new Timestamp(new GregorianCalendar().getTimeInMillis())).toString();
	}
}

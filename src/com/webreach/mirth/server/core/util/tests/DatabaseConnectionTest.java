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


package com.webreach.mirth.server.core.util.tests;

import java.sql.ResultSet;

import com.webreach.mirth.server.core.util.DatabaseConnection;
import com.webreach.mirth.server.core.util.DatabaseUtil;

import junit.framework.TestCase;

public class DatabaseConnectionTest extends TestCase {

	private DatabaseConnection dbConnection;
	private final int TEST_VALUE = 5;
	private final String TEST_TABLE = "dbtest";
	
	protected void setUp() throws Exception {
		super.setUp();
		dbConnection = new DatabaseConnection();
		StringBuffer statement = new StringBuffer();
		statement.append("DROP TABLE " + TEST_TABLE + " IF EXISTS;");
		statement.append("CREATE TABLE " + TEST_TABLE + " (testvalue INTEGER);");
		dbConnection.update(statement.toString());
		dbConnection.close();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		dbConnection = new DatabaseConnection();
		dbConnection.update("DROP TABLE " + TEST_TABLE + " IF EXISTS;");
		dbConnection.close();
	}
	
	public void testQuery() {
		ResultSet result = null;
		int resultValue = -1;
		int resultCount = 0;
		
		try {
			dbConnection.update("INSERT INTO dbtest VALUES(" + TEST_VALUE + ")");
			result = dbConnection.query("SELECT testvalue FROM dbtest");
			
			while (result.next()) {
				resultValue = result.getInt("testvalue");
				resultCount++;
			}
			
			assertEquals(TEST_VALUE, resultValue);
			assertEquals(1, resultCount);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}
	
	public void testUpdate() {
		int rowCount = -1;
		
		try {
			rowCount = dbConnection.update("INSERT INTO dbtest VALUES(" + TEST_VALUE + ")");
			assertEquals(1, rowCount);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbConnection.close();
		}
	}
}

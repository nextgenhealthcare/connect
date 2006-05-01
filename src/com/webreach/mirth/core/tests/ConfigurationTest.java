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


package com.webreach.mirth.core.tests;

import junit.framework.TestCase;

import com.webreach.mirth.core.Configuration;
import com.webreach.mirth.core.util.DatabaseConnection;

public class ConfigurationTest extends TestCase {
	private Configuration configuration;
	private DatabaseConnection dbConnection;
	
	protected void setUp() throws Exception {
		super.setUp();
		configuration = Configuration.getInstance();
		dbConnection = new DatabaseConnection();
		StringBuffer statement = new StringBuffer();
		statement.append("DROP SEQUENCE SEQ_CONFIGURATION IF EXISTS;");
		statement.append("CREATE SEQUENCE SEQ_CONFIGURATION START WITH 1 INCREMENT BY 1;");
		dbConnection.update(statement.toString());
		dbConnection.close();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		dbConnection = new DatabaseConnection();
		dbConnection.update("DROP SEQUENCE SEQ_CONFIGURATION IF EXISTS;");
		dbConnection.close();
	}
	
	public void testGetProperties() {
		
	}
	
	public void testGetChannels() {

	}
	
	public void testGetTransports() {

	}
	
	public void testStore() {
		
	}
	
	public void testGetNextId() {
		int id1 = configuration.getNextId();
		int id2 = configuration.getNextId();
		int id3 = configuration.getNextId();
		assertEquals(id2 - 1, id1);
		assertEquals(id3 - 1, id2);
	}
}

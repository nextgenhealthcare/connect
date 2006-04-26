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

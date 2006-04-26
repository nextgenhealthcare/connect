package com.webreach.mirth.core.tests;

import java.sql.ResultSet;

import com.webreach.mirth.core.util.DatabaseConnection;
import com.webreach.mirth.core.util.DatabaseUtil;

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

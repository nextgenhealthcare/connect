package com.webreach.mirth.core.util;

import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseUtil {
	public static void close(ResultSet result) throws RuntimeException {
		try {
			result.close();
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}
	
	public static void close(Statement statement) throws RuntimeException {
		try {
			statement.close();
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}
}

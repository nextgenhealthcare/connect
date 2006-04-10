package com.webreach.mirth.testbench;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseOutput
{
	private Connection connection = null;
	private TestData properties = new TestData();

	public boolean receive(String query, String queryResult)
	{
		boolean result = false;
		
		loadDriver();
		connectToDatabase();
		result = runQuery(query, queryResult);
		closeConnection();
		
		return result;
	}

	private final void loadDriver() 
	{
		try 
		{
			Class.forName(properties.getProperty("JDBC_DRIVER_NAME")).newInstance();
		}
		catch (ClassNotFoundException cnf_excp) 
		{
			System.out.println("Failed to load JDBC driver " + properties.getProperty("JDBC_DRIVER_NAME") + "."); 
		}
		catch (InstantiationException i_excp) 
		{
			System.out.println("Failed to load JDBC driver " + properties.getProperty("JDBC_DRIVER_NAME") + ".");
		}
		catch (IllegalAccessException ia_excp) 
		{
			System.out.println("Failed to load JDBC driver " + properties.getProperty("JDBC_DRIVER_NAME") + ".");
		}
	}
	
	private void connectToDatabase()
	{
		String url = "jdbc:mysql://" + properties.getProperty("dataBaseServer") + ":" + properties.getProperty("dataBasePort") + "/" + 
		properties.getProperty("dataBaseSchema") + "?user=" + properties.getProperty("dataBaseUser") + "&password=" + properties.getProperty("dataBasePassword");
		
		// Attempt to get a connection.
		try 
		{
			this.connection = DriverManager.getConnection(url);
		}
		catch (SQLException sql_excp) 
		{
			System.out.println("Failed to get database connection with url, " + url);
		}
	}
    
	public boolean runQuery(String query, String queryResult)
	{
		boolean result = false;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try 
		{
			statement = this.connection.createStatement();
		    resultSet = statement.executeQuery(query);
		    resultSet.next();
		    
		    if (queryResult.equals(resultSet.getString(1)))
		    	result = true;
		    
		}
		catch (SQLException sql_excp) 
		{
			System.out.println("Encountered error while trying to execute query.");
		}
		
		/* Close the result set and dispose of the statement. */
		try 
		{
			resultSet.close();
		    statement.close();
		}
		catch (SQLException sql_excp) 
		{
			System.out.println("Encountered error while trying to close result set or statement.");
		}
		return result;
	}
	
	private void closeConnection() 
	{
		try 
		{
			this.connection.close();
		}
		catch (SQLException sql_excp) 
		{
			System.out.println("Failed to close database connection.");
		}
	}
}

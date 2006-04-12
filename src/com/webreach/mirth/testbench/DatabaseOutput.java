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
	private int rowCount = 0;
	
	public DatabaseOutput(String query)
	{
		loadDriver();
		connectToDatabase();
		ResultSet result = runQuery(query);
		try
		{
			result.next();
			rowCount = Integer.parseInt(result.getString(1));
			closeConnection();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
	}

	public boolean receive(String query)
	{
		int newRowCount = 0;
		
		loadDriver();
		connectToDatabase();
		ResultSet result = runQuery(query);
		
		try
		{
			result.next();
			newRowCount = Integer.parseInt(result.getString(1));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		if (newRowCount == rowCount+1)
		{
			rowCount = newRowCount;
			closeConnection();
			return true;
		}
		
		closeConnection();
		return false;
		

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
    
	public ResultSet runQuery(String query)
	{
		Statement statement = null;
		ResultSet resultSet = null;
		
		try 
		{
			statement = this.connection.createStatement();
		    resultSet = statement.executeQuery(query);

		    return resultSet;
		    
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
		return resultSet;
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

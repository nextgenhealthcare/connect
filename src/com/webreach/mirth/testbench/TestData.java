package com.webreach.mirth.testbench;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class TestData
{
	Properties p;
	public TestData()
	{
		p = new Properties();
		try
		{
			p.load(new FileInputStream("properties.ini"));
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public String getProperty(String key)
	{
		return p.getProperty(key);
	}
}

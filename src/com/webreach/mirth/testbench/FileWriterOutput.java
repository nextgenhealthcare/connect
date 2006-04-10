package com.webreach.mirth.testbench;

import java.io.FileReader;
import java.io.IOException;

public class FileWriterOutput
{
	TestData properties = new TestData();

	public boolean receive(String path)
	{
		try
		{
			FileReader fr = new FileReader(path);
			fr.close();
			return true;
		}
		catch(IOException e)
		{
			return false;
		}
	}
}

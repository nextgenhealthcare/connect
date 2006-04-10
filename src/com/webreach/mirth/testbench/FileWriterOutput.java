package com.webreach.mirth.testbench;

import java.io.File;
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
			File file = new File(path);
			file.delete();
			return true;
		}
		catch(IOException e)
		{
			return false;
		}
	}
}

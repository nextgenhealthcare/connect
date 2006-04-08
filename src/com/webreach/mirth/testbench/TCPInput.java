package com.webreach.mirth.testbench;

import org.mule.extras.client.MuleClient;
import org.mule.umo.UMOException;

public class TCPInput
{
	TestData properties = new TestData();
	
	public boolean send(MuleClient client, String hl7)
	{
		try
		{
			client.send(properties.getProperty("TCPip"), hl7, null);
		}
		catch (UMOException e)
		{
			System.out.println("UMOException:");
			e.printStackTrace();
		}
		return true;
	}
}

package com.webreach.mirth.testbench;

import org.mule.extras.client.MuleClient;
import org.mule.umo.UMOException;

public class TCPInput
{
	TestData properties = new TestData();
	
	public boolean send(MuleClient client, String hl7, String outputPort)
	{
		try
		{
			client.send("tcp://" + properties.getProperty("IP") + ":" + outputPort, hl7, null);
		}
		catch (UMOException e)
		{
			System.out.println("UMOException:");
			e.printStackTrace();
		}
		return true;
	}
}

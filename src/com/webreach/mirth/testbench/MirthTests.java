package com.webreach.mirth.testbench;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.mule.extras.client.MuleClient;
import org.mule.umo.UMOException;

import junit.framework.TestCase;

public class MirthTests extends TestCase
{	
	private ArrayList<String> hl7messages;
	private MuleClient client;
	private TestData properties = new TestData();
	private String query = "SELECT COUNT(*) FROM patients";
	
	protected void setUp()
	{
		Scanner s;
		try
		{
			client = new MuleClient();
		}
		catch (UMOException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			s = new Scanner(new File(properties.getProperty("hl7file")));
			String message = "";
			hl7messages = new ArrayList<String>();

			if(s.hasNext())
				message = s.nextLine();
			while(s.hasNext())
			{
				String temp = s.nextLine();
				if(temp.length() == 0)
				{
					hl7messages.add(message);
					message = "";
					temp = s.nextLine();
					if(s.hasNext())
						message = s.nextLine();
				}
				else
				{
					message += "\r" + temp;	
				}
			}
			if (message.length() > 0)
				hl7messages.add(message);
		}
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
		}
	}
	
	public void testTCPtoDatabase()
	{
		TCPInput in = new TCPInput();
		DatabaseOutput out = new DatabaseOutput(query);
		
		for(int i = 0; i < hl7messages.size(); i++)
		{
			assertTrue(in.send(client, hl7messages.get(i), properties.getProperty("TCPtoDatabasePort")));
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{}
			assertTrue(out.receive(query));
		}	
	}
	
	public void testHTTPtoDatabase()
	{
		HTTPInput in = new HTTPInput();
		DatabaseOutput out = new DatabaseOutput(query);
		
		for(int i = 0; i < hl7messages.size(); i++)
		{
			assertTrue(in.send(client, hl7messages.get(i), properties.getProperty("HTTPtoDatabasePort")));
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{}
			assertTrue(out.receive(query));	
		}			
	}
	
	public void testTCPtoFileWriter()
	{
		TCPInput in = new TCPInput();
		FileWriterOutput out = new FileWriterOutput();
		
		for(int i = 0; i < hl7messages.size(); i++)
		{
			assertTrue(in.send(client, hl7messages.get(i), properties.getProperty("TCPtoFileWriterPort")));
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{}
			assertTrue(out.receive("\\\\34.34.34.108\\shared\\inbox\\test.txt"));
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{}
		}
	}
	
	public void testHTTPtoFileWriter()
	{
		HTTPInput in = new HTTPInput();
		FileWriterOutput out = new FileWriterOutput();
		
		for(int i = 0; i < hl7messages.size(); i++)
		{
			assertTrue(in.send(client, hl7messages.get(i), properties.getProperty("HTTPtoFileWriterPort")));
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{}
			assertTrue(out.receive("\\\\34.34.34.108\\shared\\inbox\\test.txt"));
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{}
		}				
	}
	
/*	public void testTCPtoTCP()
	{
		TCPInput in = new TCPInput();
		TCPOutput out = new TCPOutput(properties.getProperty("TCPfromTCPPort"));
		
		for(int i = 0; i < hl7messages.size(); i++)
		{
			assertTrue(in.send(client, hl7messages.get(i), properties.getProperty("TCPtoTCPPort")));
			assertTrue(out.receive());
		}				
	}
	
	public void testHTTPtoTCP()
	{
		HTTPInput in = new HTTPInput();
		TCPOutput out = new TCPOutput(properties.getProperty("TCPfromHTTPPort"));
		
		for(int i = 0; i < hl7messages.size(); i++)
		{
			assertTrue(in.send(client, hl7messages.get(i), properties.getProperty("HTTPtoTCPPort")));
			assertTrue(out.receive());
		}				
	}

	public void testTCPtoEmail()
	{
		TCPInput in = new TCPInput();
		EmailOutput out = new EmailOutput();
		
		for(int i = 0; i < hl7messages.size(); i++)
		{
			assertTrue(in.send(client, hl7messages.get(i), properties.getProperty("TCPtoEmailPort")));
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{}
			assertTrue(out.receive());
		}		
	}
	
	public void testHTTPtoEmail()
	{
		TCPInput in = new TCPInput();
		EmailOutput out = new EmailOutput();
		
		for(int i = 0; i < hl7messages.size(); i++)
		{
			assertTrue(in.send(client, hl7messages.get(i), properties.getProperty("HTTPtoEmailPort ")));
			try
			{
				Thread.sleep(10000);
			}
			catch (InterruptedException e)
			{}
			assertTrue(out.receive());
		}		
	}
*/
}

package com.webreach.mirth.testbench;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPOutput
{
	private TestData properties = new TestData();
	private Socket mirthSocket;
	
	public TCPOutput(String outputPort)
	{
		try
		{
			mirthSocket = new Socket(properties.getProperty("ip"), Integer.parseInt(outputPort));
		} 
		catch (NumberFormatException e)
		{
			System.out.println("Port needs to be a valid integer.");
		} 
		catch (UnknownHostException e)
		{
            System.out.println("Unknown host: " + properties.getProperty("ip"));
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean receive()
	{
		BufferedReader in = null;
		
		try 
		{
			in = new BufferedReader(new InputStreamReader(mirthSocket.getInputStream()));
			in.readLine();
			mirthSocket.close();
			return true;
		}
		catch(IOException e) 
		{
			System.out.println("Error reading or closing socket: " + e.getMessage());
			try
			{
				mirthSocket.close();
			} 
			catch (IOException ioe)
			{
			}
			return false;
		}
	}
}

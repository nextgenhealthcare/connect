package com.webreach.mirth.testbench;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPOutput
{
	private TestData properties = new TestData();
	
	public boolean receive()
	{
		Socket mirthSocket;
		BufferedReader in = null;

		try 
		{
			mirthSocket = new Socket(properties.getProperty("ip"), Integer.parseInt(properties.getProperty("TCPPort")));
			in = new BufferedReader(new InputStreamReader(mirthSocket.getInputStream()));
			in.readLine();
			return true;
		}
		catch (UnknownHostException e) 
		{
            System.err.println("Don't know about host: " + properties.getProperty("ip"));
            System.exit(1);
            return false;
        }
		catch(IOException e) 
		{
			System.out.println("Error opening socket: " + e.getMessage());
			return false;
		}
	}
}

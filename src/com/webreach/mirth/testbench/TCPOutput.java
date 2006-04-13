package com.webreach.mirth.testbench;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPOutput
{
	private TestData properties = new TestData();
	private ServerSocket mirthServerSocket;
	private Socket mirthSocket;
	private BufferedReader in = null;
	
	public TCPOutput(String outputPort)
	{
		try
		{
			mirthServerSocket = new ServerSocket(Integer.parseInt(outputPort));
		} 
		catch (NumberFormatException e)
		{
			System.out.println("Port must be a valid integer.");
		} 
		catch (UnknownHostException e)
		{
            System.out.println("Unknown host: " + properties.getProperty("IP"));
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean receive()
	{
		try 
		{
			// System.out.println("before accept");
			mirthSocket = mirthServerSocket.accept();
			in = new BufferedReader(new InputStreamReader(mirthSocket.getInputStream()));
			// System.out.println("before readLine");
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

package com.webreach.mirth.testbench;

import java.io.IOException;
import java.util.Properties;

import javax.mail.*;

public class EmailOutput
{
	private TestData properties = new TestData();
	private String username = "";
	private String password = "";
	private Store store;
	private Folder folder;

	public boolean receive()
	{
		getConnection(properties.getProperty("POPserver"));
		boolean worked = readMail();
		closeConnection();
		return worked;
	}

	private void getConnection(String host)
	{
		System.out.println("\tConnecting to server... "); 
		//	get system properties
		Properties props = System.getProperties();
	
		//	Get Session
		Session session = Session.getDefaultInstance(props,null);
		
		username = properties.getProperty("emailUserName"); 
		password = properties.getProperty("emailUserPassword"); 
		
		//	connect to store
		try
		{
			store = session.getStore("pop3");
		} 
		catch (NoSuchProviderException e)
		{
			e.printStackTrace();
		}
	
		try
		{
			store.connect(host,username,password);
		}
		catch (MessagingException e)
		{
			e.printStackTrace();
		}
	}

	private boolean readMail()
	{
		//	open folder
		try
		{
			Folder folder = store.getFolder("INBOX");
			folder.open(Folder.READ_ONLY);
		
			//	Get Directory
			Message message[] = folder.getMessages();
			for(int i = 0,n = message.length;i < n;i++)
			{
				System.out.println(i + ":" + message[i].getFrom() + "\t" + message[i].getSubject());
				message[i].writeTo(System.out);
			}
			return true;
		}
		catch(MessagingException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
		}
		return false;
	}

	private void closeConnection()
	{
		//	Close Connection	
		try
		{
			folder.close(false);
			store.close();
		} 
		catch (MessagingException e)
		{
			e.printStackTrace();
		}
	} 
}
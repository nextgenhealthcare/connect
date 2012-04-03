package com.mirth.connect.simplesender;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class SimpleSenderServer {
	
	public static void main(String[] args) {
		try {
			ArrayList<String> hl7messages = new ArrayList<String>();
			Scanner s = null;
			try
			{
				s = new Scanner(new File(args[0]));
				String message = "";

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
			
			System.out.println("Starting server...");
			ServerSocket serverSocket = new ServerSocket(6661);
			System.out.println("Waiting for connection...");
			Socket socket = serverSocket.accept();
			while (true) {
				System.out.println("Connection received, sending message...");
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				DataInputStream in = new DataInputStream(socket.getInputStream());
				
				System.out.println("\n" + LLPUtil.HL7Encode(hl7messages.get(0)));
				out.write(LLPUtil.HL7Encode(hl7messages.get(0)).getBytes());
				System.out.println("Message sent...");
				
				char[] chars = new char[9999];
				int i = 0;
				char c = (char) in.read();
				while (c != 13) {
					System.out.println(c);
					chars[i] = c;
					i++;
					c = (char) in.read();
				}
				
				System.out.println("ACK Received:");
				System.out.println(new String(chars));
				
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

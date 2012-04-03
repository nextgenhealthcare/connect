package com.mirth.connect.simplesender;

import java.io.DataInputStream;
import java.net.Socket;

public class SimpleSenderServerTest {
	
	public static void main(String[] args) {
		try {
			Socket socket = new Socket("localhost", 6661);
			DataInputStream in = new DataInputStream(socket.getInputStream());
			byte[] bytes = new byte[9999];
			in.read(bytes);
			System.out.println(new String(bytes));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

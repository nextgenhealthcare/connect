package com.cloudsolutions.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;

import com.cloudsolutions.ct.NtpMessage;

public class NtpTest4CT {

	SimpleDateFormat sdf=new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
	@Test
	public void DOES_CT_WORKS() {
		String serverName = "ovh1.ihe-europe.net";

		// Send request
		DatagramSocket socket = null;
		DatagramPacket packet = null;
		byte[] buf = null;
		try {
			socket = new DatagramSocket();

			InetAddress address = InetAddress.getByName(serverName);
			buf = new NtpMessage().toByteArray();
			packet = new DatagramPacket(buf, buf.length, address, 123);

			// Set the transmit timestamp *just* before sending the packet
			// ToDo: Does this actually improve performance or not?
			NtpMessage.encodeTimestamp(packet.getData(), 40, (System.currentTimeMillis() / 1000.0) + 2208988800.0);

			socket.send(packet);
			// Get response
			System.out.println("NTP request sent, waiting for response...\n");
			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Immediately record the incoming timestamp
		double destinationTimestamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;

		// Process response
		NtpMessage msg = new NtpMessage(packet.getData());

		// Corrected, according to RFC2030 errata
		double roundTripDelay = (destinationTimestamp - msg.originateTimestamp)
				- (msg.transmitTimestamp - msg.receiveTimestamp);

		double localClockOffset = ((msg.receiveTimestamp - msg.originateTimestamp)
				+ (msg.transmitTimestamp - destinationTimestamp)) / 2;
		// System.out.println(localClockOffset);

		// Display response
		System.out.println("NTP server: " + serverName);
		System.out.println(msg.toString());
		String destinationTSString= NtpMessage.timestampToString(destinationTimestamp);
		destinationTSString=destinationTSString.substring(0, destinationTSString.length()-3);
		try {
			
			Object o=sdf.parseObject(destinationTSString);
			System.out.println(o);
			assertTrue(o instanceof java.util.Date);
		} catch (ParseException e) {
			fail("Conversion failed for:"+e.getMessage());
			e.printStackTrace();
		}
		
		System.out.println("Dest. timestamp:     " + destinationTSString);

		System.out.println("Round-trip delay: " + new DecimalFormat("0.00").format(roundTripDelay * 1000) + " ms");

		System.out.println("Local clock offset: " + new DecimalFormat("0.00").format(localClockOffset * 1000) + " ms");

		socket.close();
		

	}

}

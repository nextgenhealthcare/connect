/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.deploy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.mule.extras.client.MuleClient;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;

public class Client {
	private static ArrayList<String> hl7messages;
	private static byte startOfMessage = (byte)0x0B;
	private static byte endOfMessage = (byte)0x1C;
	private static byte endOfRecord = (byte)0x0D;
	private static void LoadHL7Messages(String filename) throws FileNotFoundException{
		
		hl7messages = new ArrayList<String>();
		StringBuilder message = new StringBuilder();
		Scanner s = new Scanner(new File(filename));
		
		while(s.hasNextLine())
		{
			String temp = s.nextLine();
			if(temp.length() == 0)
			{
				hl7messages.add(message.toString());
				message = new StringBuilder();
				while (temp.length() == 0 && s.hasNextLine()){
					temp = s.nextLine();
				}
				if (temp.length()> 0){
					message.append(temp);
					message.append((char)endOfRecord);
				}
				
			}
			else
			{
				message.append(temp);
				message.append((char)endOfRecord);
			}
		}
	}
	private static UMOMessage SendHL7Message(String server, String message) throws UMOException{
		MuleClient client = new MuleClient();
		//message = (char)startOfMessage + message.trim() + (char)endOfMessage + (char)13;
		//UMOMessage response = client.send("tcp://localhost:5500?tcpProtocolClassName=org.mule.providers.tcp.protocols.LlpProtocol", message, null);
		UMOMessage response = client.send("llprouter://localhost:5000?tcpProtocolClassName=org.mule.providers.llprouter.protocols.LlpProtocol", message, null);
		//UMOMessage response = client.send("vmrouter://ADTA01", message, null);
		client.dispose();
		return response;
		
	}
	public static void main(String[] args) {
		System.out.println("Mirth Client Demo");
		
		try {
			MuleClient client = new MuleClient();
			StringBuffer message = new StringBuffer();

			if (args.length == 2) {
				BufferedReader reader = new BufferedReader(new FileReader(new File(args[1])));
				String line = null;
				while ((line = reader.readLine()) != null) {
					message.append(line + "\r");
				}
				reader.close();
			} else {
				System.out.println("Invalid parameters.");
			}
				LoadHL7Messages("c:\\adt.txt");
				Iterator<String> it = hl7messages.iterator();
				UMOMessage response;
				String tempMessage;
				String port = "";
				String lookup = "ADT^A";
				while (it.hasNext()){
					tempMessage = it.next();
					
					int pos = tempMessage.indexOf(lookup);
					port = "4500";//"66" + tempMessage.substring(pos + lookup.length(), pos + lookup.length() + 2);
					
					System.out.println("Sending message from " + args[1] + " to " + args[0]);
					
					response = SendHL7Message(args[0] + ":"  + port , tempMessage);
					
					System.out.println("Message successfully sent.");
					if (response != null) {
						System.out.println("Response was: " + new String(response.getPayloadAsBytes()));
					} else {
						System.out.println("There was no response.");
					}
				}
			/*
			System.out.println("Sending message from " + args[1] + " to " + args[0]);
			byte startOfMessage = (byte)0x0B;
			byte endOfMessage = (byte)0x1C;
			byte endOfRecord = (byte)0x0D;
			String messageSimple = "MSH|^~\\&|EPIC|EPICADT|SMS|SMSADT|199912271408|CHARRIS|ADT^A04|1817457|D|2.3|" + 
			(char)endOfRecord +
			"EVN|A04|199912271408|||CHARRIS" +
			(char)endOfRecord +
			"PID||0493575^^^2^ID1|454721||DOE^JOHN^^^^|DOE^JOHN^^^^|19480203|M||B|254E238ST^^EUCLID^OH^44123^USA||(216)731-4359|||M|NON|400003403~1129086|999-|" + 
			(char)endOfRecord 
			"NK1||CONROY^MARI^^^^|SPO||(216)731-4359||EC|||||||||||||||||||||||||||" + 
			(char)endOfRecord +
			"PV1||O|168~219~C~PMA^^^^^^^^^||||277^ALLENFADZL^BONNIE^^^^||||||||||||2688684|||||||||||||||||||||||||199912271408||||||002376853";
			UMOMessage response = client.send("tcp://" + args[0] + "?tcpProtocolClassName=org.mule.providers.tcp.protocols.LlpProtocol", messageSimple, null);
			client.dispose();
			System.out.println("Message successfully sent.");
			*/
				

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

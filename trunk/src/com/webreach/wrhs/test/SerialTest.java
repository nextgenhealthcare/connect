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


package com.webreach.wrhs.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;

public class SerialTest {

	public static void main(String[] args) {
		
		PipeParser pipeParser = new PipeParser();
		String messageStr = 
			"MSH|^~\\&|system1|W|system2|UHN|200105231927||ADT^A01|22139243|P|2.4\r" +
			"EVN|A01|200105231927|\r" +
			"PID||123^^|2216506^||Freeman^Gordon^^^MR.^MR.||19720227|M|||123 Foo ST.^^TORONTO^ON^M6G 3E6^CA^H^~123 Foo ST.^^TORONTO^ON^M6G 3E6^CA^M^|1811|(416)111-1111||E^ ENGLISH|S| PATIENT DID NOT INDICATE|211004554^||||||||||||\r" +
			"DG1|1||451.2^Phlebitis And Thrombophlebitis Of Lower Extremities, Unspecified^I9|||F\r" +
			"DG1|1||550.00^Unilateral Or Unspecified Inguinal Hernia, With Gangrene^I9|||F\r" +
			"DG1|1||680.2^Carbuncle And Furuncle Of Trunk^I9|||F\r";

		try {
			System.out.println("Starting serialization...");
			Message messageObj = pipeParser.parse(messageStr);
			
			ObjectOutputStream obj_out = new ObjectOutputStream (new FileOutputStream ("message.data"));
			
			obj_out.writeObject(messageObj);
			obj_out.close();
			System.out.println("Finished.");
			
			System.out.println("Reading file...");
			
			ObjectInputStream obj_in = new ObjectInputStream (new FileInputStream ("message.data"));
			Object obj = obj_in.readObject ();
			if (obj instanceof Message)
			{
			  // Cast object to a Vector
			  Message msg = (Message) obj;
			  System.out.println(msg.getVersion());

			  // Do something with vector ...
			}
			obj_in.close();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}
}

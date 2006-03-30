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
import java.io.FileReader;

import org.mule.extras.client.MuleClient;
import org.mule.umo.UMOMessage;

public class Client {
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

			System.out.println("Sending message from " + args[1] + " to " + args[0]);
			UMOMessage response = client.send("mllp://" + args[0], message.toString(), null);
			client.dispose();
			System.out.println("Message successfully sent.");

			if (response != null) {
				System.out.println("Response was: " + response);
			} else {
				System.out.println("There was no response.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

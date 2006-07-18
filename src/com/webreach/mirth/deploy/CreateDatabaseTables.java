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

import com.webreach.mirth.managers.Database;

public class CreateDatabaseTables {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java CreateDatabaseTables database script");
		}

		try {
			Database database = new Database(args[0]);
			StringBuffer script = new StringBuffer();

			if (args.length == 2) {
				BufferedReader reader = new BufferedReader(new FileReader(new File(args[1])));
				String line = null;

				while ((line = reader.readLine()) != null) {
					script.append(line + "\n");
				}

				reader.close();
			} else {
				System.out.println("Invalid parameters.");
			}

			System.out.println("Executing \"" + args[1] + "\" on database \"" + args[0] + "\".");

			database.update(script.toString());
			database.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

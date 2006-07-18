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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.webreach.mirth.managers.PropertyFormUtil;
import com.webreach.mirth.managers.PropertyManager;
import com.webreach.mirth.managers.types.MirthPropertyType;
import com.webreach.mirth.ui.MirthController;

public class GenerateEndpointForm {

	public static void main(String[] args) {
		PropertyManager propertyManager = PropertyManager.getInstance();
		propertyManager.initialize();
		
		String forms = "";
		ArrayList<MirthPropertyType> types = propertyManager.getTypes("endpoint");
		File outputFile;
		File basedir = new File(".");
		FileWriter fileOut;
		boolean defaultType = true;

		for (int i = 0; i < types.size(); i++) {
			forms += PropertyFormUtil.getForm("endpoint", types.get(i).getName(), defaultType);
			defaultType = false;
		}

		try {
			outputFile = new File(basedir.getCanonicalPath() + MirthController.WEBAPPS_FOLDER + MirthController.ENDPOINT_FOLDER + File.separator + "form.jsp");
			fileOut = new FileWriter(outputFile);
			fileOut.write(forms);
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

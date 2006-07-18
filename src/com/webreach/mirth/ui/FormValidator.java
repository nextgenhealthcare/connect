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


package com.webreach.mirth.ui;

import java.util.ArrayList;

import com.webreach.mirth.managers.types.MirthProperty;

public class FormValidator {

	public boolean validate(ArrayList<MirthProperty> propertyList, ArrayList<String> properties, ArrayList<String> errorMessages) {
		boolean pass = true;
		MirthProperty curProperty;
		String propertyValue;
		for (int i = 0; i < propertyList.size(); i++) {
			curProperty = propertyList.get(i);
			propertyValue = properties.get(i);
			if (curProperty.getFormValidator().equals("intValidator")) {
				if (!intValidator(curProperty, propertyValue)) {
					errorMessages.add("The value of " + curProperty.getDisplayName() + " must fall between " + curProperty.getFormValidatorOptions()[0] + " and " + curProperty.getFormValidatorOptions()[1] + ".");
					pass = false;
				}
			} else if (curProperty.getFormValidator().equals("stringValidator")) {
				if (!stringValidator(curProperty, propertyValue)) {
					errorMessages.add("The value of " + curProperty.getDisplayName() + " is not a valid string.");
					pass = false;
				}
			} else if (curProperty.getFormValidator().equals("ipAddressValidator")) {
				if (!ipAddressValidator(curProperty, propertyValue)) {
					errorMessages.add("The value of " + curProperty.getDisplayName() + " is not a valid IP Address, IP Addresses follow the format xxx.xxx.xxx.xxx where xxx is a number between 0 and 255.");
					pass = false;
				}
			} else if (curProperty.getFormValidator().equals("charValidator")) {
				if (!charValidator(curProperty, propertyValue)) {
					errorMessages.add("The value of " + curProperty.getDisplayName() + " is not a valid character.");
					pass = false;
				}
			} else if (curProperty.getFormValidator().equals("yesNoValidator")) {
				if (!yesNoValidator(curProperty, propertyValue)) {
					errorMessages.add("The value of " + curProperty.getDisplayName() + " is not valid.");
					pass = false;
				}
			}

		}
		return pass;
	}

	public boolean intValidator(MirthProperty property, String value) {
		String intValues = "0123456789";
		if (value == null)
			return false;
		if (property.isRequired() && value.equals("")) {
			return false;
		}
		for (int i = 0; i < value.length(); i++) {
			if (!intValues.contains(value.substring(i, i + 1)))
				return false;
		}
		if (Integer.parseInt(value) < Integer.parseInt(property.getFormValidatorOptions()[0]) || Integer.parseInt(value) > Integer.parseInt(property.getFormValidatorOptions()[1])) {
			return false;
		}
		return true;
	}

	public boolean stringValidator(MirthProperty property, String value) {
		if (value == null)
			return false;
		if (property.isRequired() && value.equals(""))
			return false;
		return true;
	}

	public boolean charValidator(MirthProperty property, String value) {
		if (value == null)
			return false;
		if (property.isRequired() && value.equals(""))
			return false;
		return true;
	}

	public boolean ipAddressValidator(MirthProperty property, String value) {
		String ipValid = "0123456789.";
		if (value == null)
			return false;
		if (property.isRequired() && value.equals(""))
			return false;
		if (value.equals("localhost"))
			return true;
		for (int i = 0; i < value.length(); i++) {
			if (!ipValid.contains(value.substring(i, i)))
				return false;
		}
		String[] ipValues = value.split("\\.");
		if (ipValues.length != 4)
			return false;
		else {
			for (int i = 0; i < 4; i++) {
				if (Integer.parseInt(ipValues[i]) < 0 || Integer.parseInt(ipValues[0]) > 255) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean yesNoValidator(MirthProperty property, String value) {
		if (value == null)
			return false;
		if (property.isRequired() && value.equals(""))
			return false;
		if (Integer.parseInt(value) != 0 && Integer.parseInt(value) != 1)
			return false;
		return true;
	}
}

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


package com.webreach.wrhs.components;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.ADT_A01;

public class PredictiveModelingUMO {

	protected static transient Log logger = LogFactory.getLog(PredictiveModelingUMO.class);

	public String addPatient(Message message) {
		try {
			//handle ADT_A01 messages
			if (message instanceof ADT_A01) {
				//cast message to ADT_A01 message
				ADT_A01 adtMsg = (ADT_A01) message;
				
				//get first and last name and sex
				String lastName = adtMsg.getPID().getPatientName(0).getFamilyName().getSurname().getValue();
				String firstName = adtMsg.getPID().getPatientName(0).getGivenName().getValue();
				String patientId = adtMsg.getPID().getPatientID().getID().getValue();
				String sex = adtMsg.getPID().getAdministrativeSex().getValue();

				//determine age
//				int age = determineAge(new Integer(adtMsg.getPID().getDateTimeOfBirth().getTimeOfAnEvent().getValue().substring(0, 4)).intValue());
					
				//get list of ICD-9 codes
				String codes = new String();
				for (int dgIndex = 0; dgIndex < adtMsg.getDG1Reps(); ++dgIndex) {
					codes += adtMsg.getDG1(dgIndex).getDiagnosisCodeDG1().getIdentifier().getValue() + " ";
				}
							
//				System.out.println(patientId + " " + lastName + ", " + firstName + " " + sex + " " + age + " - " + codes);
//				String query = "INTO Patient (patientId, lastName, firstName, sex, age) VALUES (" + patientId + ", '" + lastName + "', '" + firstName + "', '" + sex + "', " + age + ")";
				
				String query = patientId + ", '" + lastName + "', '" + firstName + "', '" + sex;				
				System.out.println(query);
				return query;
			}
		} catch (Exception err) {
			 
		}

		return null;
	}
}

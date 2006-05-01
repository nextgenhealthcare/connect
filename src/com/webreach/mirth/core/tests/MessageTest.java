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


package com.webreach.mirth.core.tests;

import java.sql.Timestamp;
import java.util.Calendar;

import com.webreach.mirth.core.Message;

import junit.framework.TestCase;

public class MessageTest extends TestCase {

	private Message message;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		Calendar calendar = Calendar.getInstance();
		message = new Message();
		message.setId(0);
		message.setDate(new Timestamp(calendar.getTimeInMillis()));
		message.setSendingFacility("Hospital A");
		message.setEvent("ADT_A01");
		message.setControlId("123456");
		message.setSize(10);
		message.setMessage("MSH");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}

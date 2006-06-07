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


package com.webreach.mirth.model.tests;

import java.sql.Timestamp;
import java.util.Calendar;

import com.webreach.mirth.model.SystemEvent;

import junit.framework.TestCase;

public class LogTest extends TestCase {
	
	private SystemEvent log;
	
	protected void setUp() throws Exception {
		super.setUp();
		Calendar calendar = Calendar.getInstance();
		
		log = new SystemEvent();
		log.setId(0);
		log.setDate(new Timestamp(calendar.getTimeInMillis()));
		log.setDescription("Message sucessfully transformed.");
		log.setLevel(0);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}

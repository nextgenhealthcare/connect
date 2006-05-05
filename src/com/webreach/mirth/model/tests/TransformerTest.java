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

import com.webreach.mirth.model.Transformer;

import junit.framework.TestCase;

public class TransformerTest extends TestCase {

	private Transformer transformer;
	
	protected void setUp() throws Exception {
		super.setUp();
		transformer = new Transformer();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testAddScript() {
		transformer.getVariables().put("dob", "msg.PID['PID.5']['XPN.6']");
		assertEquals(1, transformer.getVariables().size());
	}
	
	public void testGetMappingScript() {
		transformer.setType(Transformer.Type.MAP);
		transformer.getVariables().put("firstName", "msg.PID['PID.5']['XPN.1']");
		transformer.getVariables().put("lastName", "msg.PID['PID.5']['XPN.2']");
		String script = "map.put('firstName', msg.PID['PID.5']['XPN.1']);" + "\n" + "map.put('lastName', msg.PID['PID.5']['XPN.2']);";

		assertEquals(script, transformer.getScript());
	}

	public void testGetXSLTScript() {
		transformer.setType(Transformer.Type.XSLT);
		transformer.getVariables().put("xslt", "");
		String script = "";
		
		assertEquals(script, transformer.getScript());
	}
	
	public void testGetScript() {
		transformer.setType(Transformer.Type.SCRIPT);
		transformer.getVariables().put("script", "msg.PID['PID.5']['XPN.1'] == 'test';");
		String script = "msg.PID['PID.5']['XPN.1'] == 'test';";
		
		assertEquals(script, transformer.getScript());
	}

}

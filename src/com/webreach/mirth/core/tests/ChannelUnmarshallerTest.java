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

import com.webreach.mirth.core.Channel;
import com.webreach.mirth.core.util.ChannelUnmarshaller;

import junit.framework.TestCase;

public class ChannelUnmarshallerTest extends TestCase {
	private String source;
	private ChannelUnmarshaller cu = null;
	
	protected void setUp() throws Exception {
		super.setUp();
		cu = new ChannelUnmarshaller();
		
		StringBuffer sourceBuffer = new StringBuffer();
		sourceBuffer.append("<channel description=\"This is a test interface.\" direction=\"INBOUND\" enabled=\"true\" initial=\"STOPPED\" name=\"Test Interface\"> <source name=\"sourceConnector\" transport=\"\"> <property name=\"key\" value=\"value\"/> <transformer language=\"JAVASCRIPT\" type=\"MAP\"> <variable name=\"firstName\"><![CDATA[TestFirstName]]></variable> </transformer> </source> <filter><![CDATA[return true;]]></filter> <validator> <profile name=\"profile1\"><![CDATA[<XLST>]]></profile> </validator> <destinations> <destination name=\"destinationConnector1\" transport=\"\"> <property name=\"key1\" value=\"value1\"/> <property name=\"key2\" value=\"value2\"/> <transformer language=\"JAVASCRIPT\" type=\"SCRIPT\"> <variable name=\"lastName\"><![CDATA[TestLastName]]></variable> </transformer> </destination> <destination name=\"destinationConnector2\" transport=\"\"> <property name=\"key1\" value=\"value1\"/> <property name=\"key2\" value=\"value2\"/> <transformer language=\"TCL\" type=\"SCRIPT\"> <variable name=\"lastName\"><![CDATA[TestLastName]]></variable> </transformer> </destination> </destinations></channel>");
		source = sourceBuffer.toString();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testUnmarshal() {
		Channel channel = null;
		
		try {
			channel = cu.unmarshal(source);	
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals("Test Interface", channel.getName());
		assertEquals("This is a test interface.", channel.getDescription());
		assertEquals(Channel.Direction.INBOUND, channel.getDirection());
		assertEquals(Channel.Status.STOPPED, channel.getInitialStatus());
		assertEquals(true, channel.isEnabled());
		assertEquals("sourceConnector", channel.getSourceConnector().getName());
		assertEquals(1, channel.getSourceConnector().getProperties().size());
	}
}

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


package com.webreach.mirth.server.core.util.tests;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.bind.ChannelUnmarshaller;

import junit.framework.TestCase;

public class ChannelUnmarshallerTest extends TestCase {
	private String source;
	private ChannelUnmarshaller cu = null;
	
	protected void setUp() throws Exception {
		super.setUp();
		cu = new ChannelUnmarshaller();
		
		StringBuffer sourceBuffer = new StringBuffer();
		sourceBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <channel description=\"This is a test interface.\" direction=\"INBOUND\"     enabled=\"true\" id=\"0\" initial=\"STOPPED\" mode=\"ROUTER\" name=\"Test Interface\">     <source name=\"sourceConnector\" transport=\"\">         <properties>             <property name=\"sourceKey\" value=\"sourcevalue\"/>         </properties>         <transformer language=\"JAVASCRIPT\" type=\"MAP\">             <variable name=\"firstName\">TestFirstName</variable>         </transformer>     </source>     <filter>return true;</filter>     <validator>         <profile name=\"profile1\">&lt;XLST&gt;</profile>     </validator>     <destinations>         <destination name=\"destinationConnector1\" transport=\"\">             <properties>                 <property name=\"destkey2\" value=\"destvalue2\"/>                 <property name=\"destkey1\" value=\"destvalue1\"/>             </properties>             <transformer language=\"JAVASCRIPT\" type=\"SCRIPT\">                 <variable name=\"lastName\">TestLastName</variable>             </transformer>         </destination>         <destination name=\"destinationConnector2\" transport=\"\">             <properties>                 <property name=\"key2\" value=\"value2\"/>                 <property name=\"key1\" value=\"value1\"/>             </properties>             <transformer language=\"JAVASCRIPT\" type=\"SCRIPT\">                 <variable name=\"lastName\">TestLastName</variable>             </transformer>         </destination>     </destinations>     <properties>         <property name=\"test\" value=\"test\"/>    </properties></channel>");
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
		System.out.println(channel.getValidator().getProfiles().toString());
	}
}

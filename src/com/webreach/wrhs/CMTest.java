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


package com.webreach.wrhs;

import com.webreach.mirth.managers.ConfigUtil;
import com.webreach.mirth.managers.ConfigurationManager;
import com.webreach.mirth.managers.PropertyManager;
import com.webreach.mirth.managers.types.mirth.Channel;
import com.webreach.mirth.managers.types.mirth.Endpoint;
import com.webreach.mirth.managers.types.mirth.Filter;
import com.webreach.mirth.managers.types.mirth.Script;
import com.webreach.mirth.managers.types.mirth.Transformer;
import com.webreach.mirth.managers.types.mirth.User;

public class CMTest {
	private static ConfigurationManager configurationManager = ConfigurationManager.getInstance();
	private static PropertyManager propertyManager = PropertyManager.getInstance();
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try {
			configurationManager.initialize();
			propertyManager.initialize();
			
//			System.out.println("BUILD: " + configurationManager.getBuildNumber());
//
//			// create a new user
//			User u1 = configurationManager.createUser();
//			u1.setLogin("admin");
//			u1.setPassword(ConfigUtil.encryptPassword("password"));
//			configurationManager.addUser(u1);
//			
//			// create a new script
//			Script s1 = configurationManager.createScript();
//			s1.setType("JavaScript");
//			s1.setValue("return msg.MSH['MSH.3']['HD.1'];");
//			s1.setReturnClass("java.lang.String");
//
//			// create a new script
//			Script s2 = configurationManager.createScript();
//			s2.setType("JavaScript");
//			s2.setValue("return true;");
//
//			// create a new inbound endpoint
//			Endpoint e1 = configurationManager.createEndpoint();
//			e1.setName("Test Inbound Endpoint");
//			e1.setType(ConfigurationManager.ENDPOINT_TCP);
//			e1.setDescription("This is a test description of the endpoint.");
//			configurationManager.setProperty(e1.getProperties().getProperty(), "address", "tcp://localhost:123");
//
//			// create a new outbound endpoint
//			Endpoint e2 = configurationManager.createEndpoint();
//			e2.setName("Test Outbound Endpoint");
//			e2.setType(ConfigurationManager.ENDPOINT_TCP);
//			configurationManager.setProperty(e2.getProperties().getProperty(), "address", "tcp://localhost:321");
//			
//			// create a new transformer
//			Transformer t1 = configurationManager.createTransformer();
//			t1.setName("Test Transformer");
//			t1.setScript(s1);
//			
//			// create a new filter
//			Filter f1 = configurationManager.createFilter();
//			f1.setName("Test Filter");
//			f1.setScript(s2);
//			
//			// create a new channel
//			Channel chan1 = configurationManager.createChannel();
//			chan1.setName("Test Channel");
//			chan1.setEnabled(true);
//			chan1.setOutbound(false);
//			chan1.setSourceEndpointId(configurationManager.addEndpoint(e1));
//			chan1.setDestinationEndpointId(configurationManager.addEndpoint(e2));
//			chan1.setFilterId(String.valueOf(configurationManager.addFilter(f1)));
//			chan1.setTransformerId(String.valueOf(configurationManager.addTransformer(t1)));
//			configurationManager.addChannel(chan1);
			
			configurationManager.marshallMule();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@SuppressWarnings("unchecked")
	public static void generateTestConfig() {
		try {
			configurationManager.initialize();

			// create a new user
			User u1 = configurationManager.createUser();
			u1.setLogin("admin");
			u1.setPassword(ConfigUtil.encryptPassword("password"));
			configurationManager.addUser(u1);
			
			// create a new script
			Script s1 = configurationManager.createScript();
			s1.setType("JavaScript");
			s1.setValue("return msg.MSH['MSH.3']['HD.1'];");
			s1.setReturnClass("java.lang.String");

			// create a new script
			Script s2 = configurationManager.createScript();
			s2.setType("JavaScript");
			s2.setValue("return true;");

			// create a new inbound endpoint
			Endpoint e1 = configurationManager.createEndpoint();
			e1.setName("Test Inbound Endpoint");
			e1.setType(ConfigurationManager.ENDPOINT_TCP);
			configurationManager.addMirthProperty(e1.getProperties().getProperty(), "address", "tcp://localhost:123");

			// create a new outbound endpoint
			Endpoint e2 = configurationManager.createEndpoint();
			e2.setName("Test Outbound Endpoint");
			e2.setType(ConfigurationManager.ENDPOINT_TCP);
			configurationManager.addMirthProperty(e2.getProperties().getProperty(), "address", "tcp://localhost:321");
			
			// create a new transformer
			Transformer t1 = configurationManager.createTransformer();
			t1.setName("Test Transformer");
			t1.setScript(s1);
			
			// create a new filter
			Filter f1 = configurationManager.createFilter();
			f1.setName("Test Filter");
			f1.setScript(s2);
			
			// create a new channel
			Channel chan1 = configurationManager.createChannel();
			chan1.setName("Test Channel");
			chan1.setEnabled(true);
			chan1.setSourceEndpointId(configurationManager.addEndpoint(e1));
//			chan1.setDestinationEndpointId(configurationManager.addEndpoint(e2));
			chan1.setFilterId(String.valueOf(configurationManager.addFilter(f1)));
			chan1.setTransformerId(String.valueOf(configurationManager.addTransformer(t1)));
			configurationManager.addChannel(chan1);

			configurationManager.marshallMule();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

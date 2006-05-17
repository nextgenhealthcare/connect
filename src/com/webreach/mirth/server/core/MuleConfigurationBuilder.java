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

package com.webreach.mirth.server.core;

import java.io.File;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Transport;
import com.webreach.mirth.model.bind.PropertiesMarshaller;
import com.webreach.mirth.server.core.util.PropertyLoader;

/**
 * A MuleConfigurationBuilder is used to generate Mule configuration files based
 * on the current internal Channel configuration.
 * 
 * @author geraldb
 * 
 */
public class MuleConfigurationBuilder {
	public static final String[] cDataElements = null;
	private Logger logger = Logger.getLogger(PropertiesMarshaller.class);

	private List<Channel> channels = null;
	private List<Transport> transports = null;

	public MuleConfigurationBuilder(List<Channel> channels, List<Transport> transports) {
		this.channels = channels;
		this.transports = transports;
	}

	public Document getConfiguration() throws ConfigurationBuilderException {
		logger.debug("generating mule configuration");

		if ((channels == null) || (channels.size() == 0) || (transports == null) || (transports.size() == 0)) {
			throw new ConfigurationBuilderException();
		}

		try {
			Properties properties = PropertyLoader.loadProperties("mirth");
			File muleBootstrapFile = new File(properties.getProperty("mule.boot"));
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(muleBootstrapFile);
			Element muleConfigurationElement = document.getDocumentElement();
			
			System.out.println("CHILD NODE LENGHT: " + muleConfigurationElement.getChildNodes().getLength());

			
			
			document.appendChild(muleConfigurationElement);
			return document;
		} catch (Exception e) {
			throw new ConfigurationBuilderException(e);
		}
	}
}

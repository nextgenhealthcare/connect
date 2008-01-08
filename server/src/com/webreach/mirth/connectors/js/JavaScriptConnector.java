/*
 * $Header: /home/projects/mule/scm/mule/providers/jdbc/src/java/org/mule/providers/jdbc/JdbcConnector.java,v 1.7 2005/06/23 08:01:29 gnt Exp $
 * $Revision: 1.7 $
 * $Date: 2005/06/23 08:01:29 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.connectors.js;

import java.util.Map;

import org.mozilla.javascript.Script;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.UMOComponent;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOMessageReceiver;

import com.webreach.mirth.server.controllers.ScriptController;
import com.webreach.mirth.server.util.CompiledScriptCache;

/**
 * @author Guillaume Nodet
 * @version $Revision: 1.7 $
 */
public class JavaScriptConnector extends AbstractServiceEnabledConnector {

	private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
	private ScriptController scriptController = ScriptController.getInstance();

	public static final String PROPERTY_POLLING_FREQUENCY = "pollingFrequency";

	public static final String PROPERTY_POLLING_TYPE = "pollingType";
	public static final String PROPERTY_POLLING_TIME = "pollingTime";

	public static final String POLLING_TYPE_INTERVAL = "interval";
	public static final String POLLING_TYPE_TIME = "time";

	private String pollingType = POLLING_TYPE_INTERVAL;
	private String pollingTime = "12:00 AM";
	private long pollingFrequency = 5000;

	private String scriptId;
	private String channelId;

	// This method gets called when the JDBC connector is initialized. It
	// compiles the JavaScript and adds it to the cache.
	@Override
	protected synchronized void initFromServiceDescriptor() throws InitialisationException {
		super.initFromServiceDescriptor();
		org.mozilla.javascript.Context context = org.mozilla.javascript.Context.enter();

		try {
			if (scriptId != null) {
				String databaseScript = scriptController.getScript(scriptId);

				if (databaseScript != null) {
					String generatedScript = generateScript(databaseScript);
					logger.debug("compiling script");
					Script compiledScript = context.compileString(generatedScript, scriptId, 1, null);
					compiledScriptCache.putCompiledScript(scriptId, compiledScript);
				}
			}

		} catch (Exception e) {
			throw new InitialisationException(e, this);
		} finally {
			org.mozilla.javascript.Context.exit();
		}
	}

	// Generates the JavaScript based on the script which the user enters
	private String generateScript(String scriptString) {
		logger.debug("generating database script");
		StringBuilder script = new StringBuilder();
		script.append("importPackage(Packages.com.webreach.mirth.server.util);\n");

		script.append("function $(string) { ");
		script.append("if (connectorMap.containsKey(string)) { return connectorMap.get(string);} else ");
		script.append("if (channelMap.containsKey(string)) { return channelMap.get(string);} else ");
		script.append("if (globalMap.containsKey(string)) { return globalMap.get(string);} else ");
		script.append("{ return ''; }}");
		script.append("function $g(key, value){");
		script.append("if (arguments.length == 1){return globalMap.get(key); }");
		script.append("else if (arguments.length == 2){globalMap.put(key, value); }}");
		script.append("function $c(key, value){");
		script.append("if (arguments.length == 1){return channelMap.get(key); }");
		script.append("else if (arguments.length == 2){channelMap.put(key, value); }}");
		script.append("function $co(key, value){");
		script.append("if (arguments.length == 1){return connectorMap.get(key); }");
		script.append("else if (arguments.length == 2){connectorMap.put(key, value); }}");
		script.append("function $r(key, value){");
		script.append("if (arguments.length == 1){return responseMap.get(key); }");
		script.append("else if (arguments.length == 2){responseMap.put(key, value); }}");
		script.append("function doScript() {");
		script.append(scriptString + "}\n");
		script.append("doScript()\n");
		return script.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOConnector#getProtocol()
	 */
	public String getProtocol() {
		return "js";
	}

	public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception {
		String[] params = {};

		long polling = pollingFrequency;
		Map props = endpoint.getProperties();
		if (props != null) {
			// Override properties on the endpoint for the specific endpoint
			String tempPolling = (String) props.get(PROPERTY_POLLING_FREQUENCY);
			if (tempPolling != null) {
				polling = Long.parseLong(tempPolling);
			}

			String pollingType = (String) props.get(PROPERTY_POLLING_TYPE);
			if (pollingType != null) {
				setPollingType(pollingType);
			}
			String pollingTime = (String) props.get(PROPERTY_POLLING_TIME);
			if (pollingTime != null) {
				setPollingTime(pollingTime);
			}
		}
		if (polling <= 0) {
			polling = 1000;
		}
		logger.debug("set polling frequency to: " + polling);

		return getServiceDescriptor().createMessageReceiver(this, component, endpoint, params);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.providers.UMOConnector#create(java.util.HashMap)
	 */
	public void doInitialise() throws InitialisationException {
		super.doInitialise();
	}

	/**
	 * @return Returns the pollingFrequency.
	 */
	public long getPollingFrequency() {
		return pollingFrequency;
	}

	/**
	 * @param pollingFrequency
	 *            The pollingFrequency to set.
	 */
	public void setPollingFrequency(long pollingFrequency) {
		this.pollingFrequency = pollingFrequency;
	}

	public String getScriptId() {
		return this.scriptId;
	}

	public void setScriptId(String scriptId) {
		this.scriptId = scriptId;
	}

	public String getChannelId() {
		return this.channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.providers.TransactionEnabledConnector#getSessionFactory(org.mule.umo.endpoint.UMOEndpoint)
	 */

	public String getPollingTime() {
		return pollingTime;
	}

	public void setPollingTime(String pollingTime) {
		this.pollingTime = pollingTime;
	}

	public String getPollingType() {
		return pollingType;
	}

	public void setPollingType(String pollingType) {
		this.pollingType = pollingType;
	}
}

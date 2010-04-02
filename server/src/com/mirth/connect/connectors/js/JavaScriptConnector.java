/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.js;

import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Script;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.umo.UMOComponent;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOMessageReceiver;

import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.CompiledScriptCache;

public class JavaScriptConnector extends AbstractServiceEnabledConnector {

    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();

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
                String databaseScript = scriptController.getScript(channelId, scriptId);

                if (databaseScript != null) {
                    String generatedScript = generateScript(databaseScript);
                    logger.debug("compiling script");
                    Script compiledScript = context.compileString(generatedScript, scriptId, 1, null);
                    compiledScriptCache.putCompiledScript(scriptId, compiledScript, generatedScript);
                }
            }

        } catch (Exception e) {
            throw new InitialisationException(e, this);
        } finally {
            org.mozilla.javascript.Context.exit();
        }
    }

    // Generates the JavaScript based on the script which the user enters
    // TODO: This code is exactly the same as the code in the DB Reader. They should be made into a method.
    private String generateScript(String scriptString) {
        logger.debug("generating database script");
        StringBuilder script = new StringBuilder();
        script.append("importPackage(Packages.com.mirth.connect.server.util);\n");
        script.append("importPackage(Packages.com.mirth.connect.util);\n");

        script.append("function $(string) { ");
        script.append("if (connectorMap.containsKey(string)) { return connectorMap.get(string);} else ");
        script.append("if (channelMap.containsKey(string)) { return channelMap.get(string);} else ");
        script.append("if (globalChannelMap.containsKey(string)) { return globalChannelMap.get(string);} else ");
        script.append("if (globalMap.containsKey(string)) { return globalMap.get(string);} else ");
        script.append("{ return ''; }}");
        script.append("function $g(key, value){");
        script.append("if (arguments.length == 1){return globalMap.get(key); }");
        script.append("else if (arguments.length == 2){globalMap.put(key, value); }}");
        script.append("function $gc(key, value){");
        script.append("if (arguments.length == 1){return globalChannelMap.get(key); }");
        script.append("else if (arguments.length == 2){globalChannelMap.put(key, value); }}");
        script.append("function $c(key, value){");
        script.append("if (arguments.length == 1){return channelMap.get(key); }");
        script.append("else if (arguments.length == 2){channelMap.put(key, value); }}");
        script.append("function $co(key, value){");
        script.append("if (arguments.length == 1){return connectorMap.get(key); }");
        script.append("else if (arguments.length == 2){connectorMap.put(key, value); }}");
        script.append("function $r(key, value){");
        script.append("if (arguments.length == 1){return responseMap.get(key); }");
        script.append("else if (arguments.length == 2){responseMap.put(key, value); }}");

        try {
            List<CodeTemplate> templates = ControllerFactory.getFactory().createCodeTemplateController().getCodeTemplate(null);
            for (CodeTemplate template : templates) {
                if (template.getType() == CodeSnippetType.FUNCTION) {
                    script.append(template.getCode());
                }
            }
        } catch (ControllerException e) {
            logger.error("Could not get user functions.", e);
        }

        script.append("function doScript() {\n");
        script.append(scriptString + "\n");
        script.append("}\n");
        script.append("doScript();\n");
        return script.toString();
    }

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

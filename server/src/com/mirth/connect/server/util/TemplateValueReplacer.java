/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.velocity.VelocityContext;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.util.ValueReplacer;

public class TemplateValueReplacer extends ValueReplacer {
    /**
     * Replaces variables in the template.
     * Uses the default context along with the global channel variable map.
     * 
     * @return The replaced template
     */
    public String replaceValues(String template, String channelId) {
        if (hasReplaceableValues(template)) {
            VelocityContext context = getDefaultContext();
            loadContextFromMap(context, GlobalChannelVariableStoreFactory.getInstance().get(channelId).getVariables());
            return evaluate(context, template);
        } else {
            return template;
        }
    }
    
    /**
     * Replaces variables in a map
     */
    public Map<String, String> replaceValues(Map<String, String> template, String channelId) {
        Map<String, String> replacedTemplate = new HashMap<String, String>();

        for (Entry<String, String> entry : template.entrySet()) {
            replacedTemplate.put(entry.getKey(), replaceValues(entry.getValue(), channelId));
        }

        return replacedTemplate;
    }

    /**
     * Replaces variables in the template.
     * Uses the default context along with the global channel variable map and
     * the passed in map.
     * 
     * @return The replaced template
     */
    public String replaceValues(String template, String channelId, Map<String, Object> map) {
        if (hasReplaceableValues(template)) {
            VelocityContext context = getDefaultContext();
            loadContextFromMap(context, GlobalChannelVariableStoreFactory.getInstance().get(channelId).getVariables());
            loadContextFromMap(context, map);
            return evaluate(context, template);
        } else {
            return template;
        }
    }

    @Override
    protected VelocityContext getDefaultContext() {
        VelocityContext context = super.getDefaultContext();
        loadContextFromMap(context, GlobalVariableStore.getInstance().getVariables());
        return context;
    }

    /**
     * Loads the connector message, global channel map, channel map, connector map and response map
     * into the passed context.
     * 
     * @return void
     */
    @Override
    protected void loadContextFromConnectorMessage(VelocityContext context, ConnectorMessage connectorMessage) {
        loadContextFromMap(context, GlobalChannelVariableStoreFactory.getInstance().get(connectorMessage.getChannelId()).getVariables());
        super.loadContextFromConnectorMessage(context, connectorMessage);
    }

    /**
     * Loads the message, global channel map, merged channel map, merged connector map and merged
     * response map into the passed context.
     * 
     * @return void
     */
    @Override
    protected void loadContextFromMessage(VelocityContext context, Message message) {
        loadContextFromMap(context, GlobalChannelVariableStoreFactory.getInstance().get(message.getChannelId()).getVariables());
        super.loadContextFromMessage(context, message);
    }
}

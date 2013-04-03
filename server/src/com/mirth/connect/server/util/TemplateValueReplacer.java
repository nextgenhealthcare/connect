/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.generic.DateTool;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ImmutableConnectorMessage;
import com.mirth.connect.donkey.model.message.ImmutableMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.util.Entities;
import com.mirth.connect.util.XmlUtil;

public class TemplateValueReplacer {
    private Logger logger = Logger.getLogger(this.getClass());
    private long count = 1;

    protected synchronized long getCount() {
        return count++;
    }

    public static boolean hasReplaceableValues(String str) {
        return ((str != null) && (str.indexOf("$") > -1));
    }

    /**
     * Replaces all values in a map.
     * Uses the default context, which includes the global variable map.
     * The original map is not modified.
     * 
     * @return A cloned HashMap with all the replaced values.
     */
    public Map<String, String> replaceValuesInMap(Map<String, String> map) {
        Map<String, String> localMap = new HashMap<String, String>(map);

        for (Entry<String, String> entry : localMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            localMap.put(key, replaceValues(value));
        }

        return localMap;
    }

    /**
     * Replaces all values in a map.
     * Uses the default context along with the connector message and all
     * available variable maps.
     * The original map is not modified.
     * 
     * @return A cloned HashMap with all the replaced values.
     */
    public Map<String, String> replaceValuesInMap(Map<String, String> map, ConnectorMessage connectorMessage) {
        Map<String, String> localMap = new HashMap<String, String>(map);

        for (Entry<String, String> entry : localMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            localMap.put(key, replaceValues(value, connectorMessage));
        }

        return localMap;
    }

    /**
     * Replaces all values in a list.
     * Uses the default context, which includes the global variable map.
     * 
     * @return void
     */
    public void replaceValuesInList(List<String> list) {
        for (int i = 0; i <= list.size() - 1; i++) {
            list.set(i, replaceValues(list.get(i)));
        }
    }

    /**
     * Replaces all values in a list.
     * Uses the default context along with the connector message and all
     * available variable maps.
     * 
     * @return void
     */
    public void replaceValuesInList(List<String> list, ConnectorMessage connectorMessage) {
        for (int i = 0; i <= list.size() - 1; i++) {
            list.set(i, replaceValues(list.get(i), connectorMessage));
        }
    }

    /**
     * Replaces variables in the template with values from the passed in map.
     * Uses the default context, which includes the global variable map.
     * 
     * @return The replaced template
     */
    public String replaceValues(String template, Map<String, Object> map) {
        if (hasReplaceableValues(template)) {
            VelocityContext context = getDefaultContext();
            loadContextFromMap(context, map);
            return evaluate(context, template);
        } else {
            return template;
        }
    }

    /**
     * Replaces variables in the template.
     * Uses the default context along with the connector message and all
     * available variable maps.
     * 
     * @return The replaced template
     */
    public String replaceValues(String template, ConnectorMessage connectorMessage) {
        if (hasReplaceableValues(template)) {
            VelocityContext context = getDefaultContext();
            loadContextFromConnectorMessage(context, connectorMessage);
            return evaluate(context, template);
        } else {
            return template;
        }
    }
    
    public String replaceValues(String template, Message message) {
        if (hasReplaceableValues(template)) {
            VelocityContext context = getDefaultContext();
            loadContextFromMessage(context, message);
            return evaluate(context, template);
        } else {
            return template;
        }
    }

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

    /**
     * Replaces variables in the template.
     * Uses the default context, which includes the global variable map.
     * 
     * @return The replaced template
     */
    public String replaceValues(String template) {
        if (hasReplaceableValues(template)) {
            VelocityContext context = getDefaultContext();
            return evaluate(context, template);
        } else {
            return template;
        }
    }

    /**
     * Decodes a MIME application/x-www-form-urlencoded string and then replaces
     * any variables.
     * Uses the default context along with the connector message and all
     * available variable maps.
     * 
     * @return The decoded and replaced string
     */
    public String replaceURLValues(String url, ConnectorMessage connectorMessage) {
        String host = new String();

        if (StringUtils.isNotEmpty(url)) {
            try {
                host = URLDecoder.decode(url, "utf-8");
            } catch (UnsupportedEncodingException e) {
                try {
                    host = URLDecoder.decode(url, "default");
                } catch (UnsupportedEncodingException e1) {
                    // should not get here
                }
            }

            host = replaceValues(host, connectorMessage);
        }

        return host;
    }

    /**
     * Performs the actual template replacement using the passed context.
     * 
     * @return The replaced template
     */
    private String evaluate(VelocityContext context, String template) {
        StringWriter writer = new StringWriter();

        try {
            Velocity.init();
            Velocity.evaluate(context, writer, "LOG", template);
        } catch (Exception e) {
            logger.warn("Could not replace template values", e);
            return template;
        }

        return writer.toString();
    }

    /**
     * Returns the default VelocityContext used to replace template values.
     * Includes the global variable map, along with some utility
     * classes/variables.
     * 
     * @return The default context
     */
    private VelocityContext getDefaultContext() {
        VelocityContext context = new VelocityContext();

        context.put("date", new DateTool());
        context.put("DATE", new SimpleDateFormat("dd-MM-yy_HH-mm-ss.SS").format(new Date()));
        context.put("COUNT", String.valueOf(getCount()));
        context.put("UUID", UUID.randomUUID().toString());
        context.put("SYSTIME", String.valueOf(System.currentTimeMillis()));
        context.put("encoder", Entities.getInstance(false)); // TODO: Remove in 3.0
        context.put("XmlUtil", XmlUtil.class);
        context.put("maps", new MapTool());

        // Load global map
        loadContextFromMap(context, GlobalVariableStore.getInstance().getVariables());

        return context;
    }

    /**
     * Loads all key/value pairs from a Map into the passed context.
     * 
     * @return void
     */
    private void loadContextFromMap(VelocityContext context, Map<String, ?> map) {
        ((MapTool) context.get("maps")).addMap(map);
        for (Entry<String, ?> entry : map.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Loads the connector message, global channel map, channel map, connector map and response map
     * into the passed context.
     * 
     * @return void
     */
    private void loadContextFromConnectorMessage(VelocityContext context, ConnectorMessage connectorMessage) {
        context.put("message", new ImmutableConnectorMessage(connectorMessage));

        // Load maps
        loadContextFromMap(context, GlobalChannelVariableStoreFactory.getInstance().get(connectorMessage.getChannelId()).getVariables());
        loadContextFromMap(context, connectorMessage.getChannelMap());
        loadContextFromMap(context, connectorMessage.getConnectorMap());
        loadContextFromMap(context, connectorMessage.getResponseMap());

        // Use the current time as the original file name if there is no original file name.
        if (!context.containsKey("originalFilename")) {
            context.put("originalFilename", System.currentTimeMillis() + ".dat");
        }
    }

    /**
     * Loads the message, global channel map, merged channel map, merged connector map and merged
     * response map into the passed context.
     * 
     * @return void
     */
    private void loadContextFromMessage(VelocityContext context, Message message) {
        context.put("message", new ImmutableMessage(message));
        ConnectorMessage mergedConnectorMessage = message.getMergedConnectorMessage();

        // Load maps
        loadContextFromMap(context, GlobalChannelVariableStoreFactory.getInstance().get(message.getChannelId()).getVariables());
        loadContextFromMap(context, mergedConnectorMessage.getChannelMap());
        loadContextFromMap(context, mergedConnectorMessage.getConnectorMap());
        loadContextFromMap(context, mergedConnectorMessage.getResponseMap());

        // Use the current time as the original file name if there is no original file name.
        if (!context.containsKey("originalFilename")) {
            context.put("originalFilename", System.currentTimeMillis() + ".dat");
        }
    }

    public class MapTool {
        private List<Map<String, ?>> maps = new ArrayList<Map<String, ?>>();

        void addMap(Map<String, ?> map) {
            maps.add(map);
        }

        public Object get(String key) {
            for (Map<String, ?> map : maps) {
                if (map.containsKey(key)) {
                    return map.get(key);
                }
            }
            return null;
        }
    }
}

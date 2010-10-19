/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package org.mule.providers;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.generic.DateTool;
import org.mule.util.UUID;
import org.mule.util.Utility;

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.server.util.DICOMUtil;
import com.mirth.connect.server.util.GlobalChannelVariableStoreFactory;
import com.mirth.connect.server.util.GlobalVariableStore;
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

    public Map<String, String> replaceValuesInMap(Map<String, String> map, MessageObject mo) {
        Map<String, String> localMap = new HashMap<String, String>(map);

        for (Entry<String, String> entry : localMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            localMap.put(key, replaceValues(value, mo));
        }

        return localMap;
    }

    /**
     * Replaces variables in the template with values from the passed in map and
     * the global map.
     * 
     * @param template
     * @param map
     * @return
     */
    public String replaceValues(String template, Map<String, Object> map) {
        if (hasReplaceableValues(template)) {
            VelocityContext context = new VelocityContext();
            loadContextFromMap(context, map);
            loadContextFromMap(context, GlobalVariableStore.getInstance().getVariables());
            return evaluate(context, template);
        } else {
            return template;
        }
    }

    public String replaceValues(String template, MessageObject messageObject) {
        return replaceValues(template, messageObject, null, null);
    }

    public String replaceValues(String template, MessageObject messageObject, String channelId, String originalFilename) {
        if (hasReplaceableValues(template)) {
            VelocityContext context = new VelocityContext();
            loadContextFromMessageObject(context, messageObject, channelId, originalFilename);
            return evaluate(context, template);
        } else {
            return template;
        }
    }

    public String replaceValues(String template, String channelId) {
        if (hasReplaceableValues(template)) {
            VelocityContext context = new VelocityContext();
            loadContextFromMap(context, GlobalChannelVariableStoreFactory.getInstance().get(channelId).getVariables());
            loadContextFromMap(context, GlobalVariableStore.getInstance().getVariables());
            return evaluate(context, template);
        } else {
            return template;
        }
    }

    public String replaceValues(String template) {
        if (hasReplaceableValues(template)) {
            VelocityContext context = new VelocityContext();
            loadContextFromMap(context, GlobalVariableStore.getInstance().getVariables());
            return evaluate(context, template);
        } else {
            return template;
        }
    }

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

    public String replaceURLValues(String url, MessageObject messageObject) {
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

            if (hasReplaceableValues(host)) {
                host = replaceValues(host, messageObject);
            }
        }

        return host;
    }

    private void loadContextFromMap(VelocityContext context, Map<String, Object> map) {
        for (Iterator<Entry<String, Object>> iter = map.entrySet().iterator(); iter.hasNext();) {
            Entry<String, Object> entry = iter.next();
            context.put(entry.getKey().toString(), entry.getValue());
        }
    }

    private void loadContextFromMessageObject(VelocityContext context, MessageObject messageObject, String channelId, String originalFilename) {
        // message variables
        if (messageObject != null) {
            context.put("message", messageObject);
            context.put("DICOMMESSAGE", DICOMUtil.getDICOMRawData(messageObject));
            context.put("MESSAGEATTACH", DICOMUtil.reAttachMessage(messageObject));
            loadContextFromMap(context, messageObject.getConnectorMap());
            loadContextFromMap(context, messageObject.getChannelMap());

            // if the messageObject was passed in, use its channelId
            channelId = messageObject.getChannelId();
        }

        if (channelId != null) {
            // load global channel map variables
            loadContextFromMap(context, GlobalChannelVariableStoreFactory.getInstance().get(channelId).getVariables());
        }

        // load global map variables
        loadContextFromMap(context, GlobalVariableStore.getInstance().getVariables());

        // we might have the originalfilename in the context
        if (context.get("originalFilename") != null) {
            originalFilename = (String) context.get("originalFilename");
        } else if (StringUtils.isEmpty(originalFilename)) {
            originalFilename = System.currentTimeMillis() + ".dat";
        }

        // system variables
        context.put("date", new DateTool());
        context.put("DATE", Utility.getTimeStamp("dd-MM-yy_HH-mm-ss.SS"));
        context.put("COUNT", String.valueOf(getCount()));
        context.put("UUID", new UUID().getUUID());
        context.put("SYSTIME", String.valueOf(System.currentTimeMillis()));
        context.put("ORIGINALNAME", originalFilename);
        context.put("XmlUtil", XmlUtil.class);
    }
}

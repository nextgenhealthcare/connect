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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.tools.VelocityFormatter;
import org.apache.velocity.tools.generic.DateTool;
import org.mule.util.UUID;
import org.mule.util.Utility;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.util.DICOMUtil;
import com.webreach.mirth.server.util.GlobalVariableStore;
import com.webreach.mirth.util.Entities;

public class TemplateValueReplacer {
    private Logger logger = Logger.getLogger(this.getClass());
    private long count = 1;

    public TemplateValueReplacer() {
        
    }

    protected synchronized long getCount() {
        return count++;
    }

    public static boolean hasReplaceableValues(String str) {
        return ((str != null) && (str.indexOf("$") > -1));
    }

    public String replaceValues(String template, String originalFilename) {
        return replaceValues(template, null, originalFilename);
    }

    public String replaceValues(String template, Map map) {
        if (hasReplaceableValues(template)) {
            VelocityContext context = new VelocityContext();
            loadContextFromMap(context, map);
            return evaluate(context, template);
        } else {
            return template;
        }
    }

    public String replaceValues(String template, MessageObject messageObject) {
        return replaceValues(template, messageObject, new String());
    }

    public String replaceValues(String template, MessageObject messageObject, String originalFilename) {
        if (hasReplaceableValues(template)) {
            VelocityContext context = new VelocityContext();
            loadContextFromMessageObject(context, messageObject, originalFilename);
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
        URLDecoder decoder = new URLDecoder();
        String host = new String();

        if ((url != null) && (url.length() > 0)) {
            try {
                host = decoder.decode(url, "utf-8");
            } catch (UnsupportedEncodingException e) {
                try {
                    host = decoder.decode(url, "default");
                } catch (UnsupportedEncodingException e1) {
                    host = decoder.decode(url);
                }
            }

            if (hasReplaceableValues(host)) {
                host = replaceValues(host, messageObject);
            }
        }

        return host;
    }

    private void loadContextFromMap(VelocityContext context, Map map) {
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            Entry entry = (Entry) iter.next();
            context.put(entry.getKey().toString(), entry.getValue());
        }
    }

    private void loadContextFromMessageObject(VelocityContext context, MessageObject messageObject, String originalFilename) {
        // message variables
        if (messageObject != null) {
            context.put("message", messageObject);
            context.put("DICOMMESSAGE", DICOMUtil.getDICOMRawData(messageObject));
            context.put("MESSAGEATTACH", DICOMUtil.reAttachMessage(messageObject));
            loadContextFromMap(context, messageObject.getConnectorMap());
            loadContextFromMap(context, messageObject.getChannelMap());
        }

        // load global map variables
        loadContextFromMap(context, GlobalVariableStore.getInstance().getVariables());

        // we might have the originalfilename in the context
        if (context.get("originalFilename") != null) {
            originalFilename = (String) context.get("originalFilename");
        } else if (originalFilename == null || originalFilename.length() == 0) {
            originalFilename = System.currentTimeMillis() + ".dat";
        }

        // system variables
        context.put("date", new DateTool());
        context.put("DATE", Utility.getTimeStamp("dd-MM-yy_HH-mm-ss.SS"));
        context.put("FORMATTER", new VelocityFormatter(context));
        context.put("COUNT", String.valueOf(getCount()));
        context.put("UUID", (new UUID()).getUUID());
        context.put("SYSTIME", String.valueOf(System.currentTimeMillis()));
        context.put("ORIGINALNAME", originalFilename);
        context.put("encoder", Entities.getInstance());
    }
}

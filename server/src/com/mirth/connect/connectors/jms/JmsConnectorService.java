/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jms;

import java.util.LinkedHashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ExtensionController;

public class JmsConnectorService implements ConnectorService {
    private final static String PLUGIN_NAME = "JMS";
    private final static String TEMPLATES_PROPERTY = "templates";

    private Properties properties;
    private Serializer serializer = ObjectXMLSerializer.getInstance();
    private LinkedHashMap<String, JmsConnectorProperties> templates;
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public synchronized Object invoke(String channelId, String method, Object object, String sessionId) throws Exception {
        try {
            if (method.equals("getTemplates")) {
                load();
                return templates;
            } else if (method.equals("saveTemplate")) {
                if (object instanceof Object[]) {
                    Object[] params = (Object[]) object;
                    load();
                    templates.put(params[0].toString(), (JmsConnectorProperties) params[1]);
                    save();
                    return templates.keySet();
                } else {
                    logger.error("Invalid argument for the " + method + "() method");
                    return null;
                }
            } else if (method.equals("deleteTemplate")) {
                load();
                templates.remove(object.toString());
                save();
                return templates.keySet();
            }
        } catch (Exception e) {
            logger.error(e);
            throw e;
        }

        throw new Exception("Unknown method: " + method);
    }

    @SuppressWarnings("unchecked")
    private void load() throws JmsConnectorException {
        Object object;

        try {
            properties = ExtensionController.getInstance().getPluginProperties(PLUGIN_NAME);
            object = properties.get(TEMPLATES_PROPERTY);
        } catch (ControllerException e) {
            throw new JmsConnectorException("Failed to load the list of JMS provider classes from the extension properties for the JMS connector", e);
        }

        templates = null;

        if (object != null && !object.toString().isEmpty()) {
            Object deserialized = serializer.deserialize(object.toString(), Object.class);

            if (deserialized instanceof LinkedHashMap) {
                templates = (LinkedHashMap<String, JmsConnectorProperties>) deserialized;
            }
        }

        if (templates == null) {
            templates = new LinkedHashMap<String, JmsConnectorProperties>();
            save();
        }
    }

    private void save() {
        properties.put(TEMPLATES_PROPERTY, serializer.serialize(templates));

        try {
            ExtensionController.getInstance().setPluginProperties(PLUGIN_NAME, properties);
        } catch (ControllerException e) {
            logger.error("Failed to save list of JMS provider classes in the extension properties for the JMS connector");
        }
    }
}

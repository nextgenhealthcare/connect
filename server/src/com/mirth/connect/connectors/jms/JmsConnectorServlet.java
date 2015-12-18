/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jms;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ExtensionController;

public class JmsConnectorServlet extends MirthServlet implements JmsConnectorServletInterface {

    private static final String PLUGIN_NAME = "JMS";
    private static final String TEMPLATES_PROPERTY = "templates";
    private static final Serializer serializer = ObjectXMLSerializer.getInstance();
    private static final Logger logger = Logger.getLogger(JmsConnectorServlet.class);
    private static final Lock lock = new ReentrantLock(true);

    private static Properties properties = new Properties();
    private static LinkedHashMap<String, JmsConnectorProperties> templates = new LinkedHashMap<String, JmsConnectorProperties>();

    public JmsConnectorServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, PLUGIN_POINT);
    }

    @Override
    public LinkedHashMap<String, JmsConnectorProperties> getTemplates() {
        lock.lock();
        try {
            load();
            return new LinkedHashMap<String, JmsConnectorProperties>(templates);
        } catch (JmsConnectorException e) {
            throw new MirthApiException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public JmsConnectorProperties getTemplate(String templateName) {
        lock.lock();
        try {
            load();
            return templates.get(templateName);
        } catch (JmsConnectorException e) {
            throw new MirthApiException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<String> saveTemplate(String templateName, JmsConnectorProperties properties) {
        lock.lock();
        try {
            load();
            templates.put(templateName, properties);
            save();
            return new HashSet<String>(templates.keySet());
        } catch (JmsConnectorException e) {
            throw new MirthApiException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<String> deleteTemplate(String templateName) {
        lock.lock();
        try {
            load();
            templates.remove(templateName);
            save();
            return new HashSet<String>(templates.keySet());
        } catch (JmsConnectorException e) {
            throw new MirthApiException(e);
        } finally {
            lock.unlock();
        }
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
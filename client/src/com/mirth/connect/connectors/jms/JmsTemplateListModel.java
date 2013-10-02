/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractListModel;

public class JmsTemplateListModel extends AbstractListModel {
    private static JmsTemplateListModel instance;

    public static JmsTemplateListModel getInstance() {
        synchronized (JmsTemplateListModel.class) {
            if (instance == null) {
                instance = new JmsTemplateListModel();
            }

            return instance;
        }
    }

    private List<String> templateNames = new ArrayList<String>();
    private Map<String, JmsConnectorProperties> templates;
    private Set<String> readOnlyTemplateNames = new HashSet<String>();

    private JmsTemplateListModel() {
        templates = new LinkedHashMap<String, JmsConnectorProperties>();

        JmsConnectorProperties properties = new JmsConnectorProperties();
        properties.setUseJndi(false);
        properties.setConnectionFactoryClass("org.apache.activemq.ActiveMQConnectionFactory");
        properties.getConnectionProperties().put("brokerURL", "failover:(tcp://localhost:61616)?maxReconnectAttempts=0");
        properties.getConnectionProperties().put("closeTimeout", "15000");
        properties.getConnectionProperties().put("useCompression", "no");
        templates.put("ActiveMQ", properties);

        properties = new JmsConnectorProperties();
        properties.setUseJndi(true);
        properties.setJndiProviderUrl("jnp://localhost:1099");
        properties.setJndiInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
        properties.setJndiConnectionFactoryName("java:/ConnectionFactory");
        templates.put("JBoss Messaging / MQ", properties);

        templateNames.addAll(templates.keySet());
        readOnlyTemplateNames.addAll(templates.keySet());
    }

    public synchronized JmsConnectorProperties getTemplate(String templateName) {
        return templates.get(templateName);
    }

    public synchronized void putTemplate(String templateName, JmsConnectorProperties template) {
        if (isPredefinedTemplate(templateName)) {
            return;
        }
        
        int index = templateNames.size();

        if (!templateNames.contains(templateName)) {
            templateNames.add(templateName);
        }

        templates.put(templateName, template);
        fireIntervalAdded(this, index, index);
    }

    public synchronized void deleteTemplate(String templateName) {
        if (isPredefinedTemplate(templateName)) {
            return;
        }
        
        int index = templateNames.lastIndexOf(templateName);
        templateNames.remove(templateName);
        templates.remove(templateName);
        fireIntervalRemoved(this, index, index);
    }

    public synchronized boolean containsTemplate(String templateName) {
        return templateNames.contains(templateName);
    }

    public synchronized boolean isPredefinedTemplate(String templateName) {
        return readOnlyTemplateNames.contains(templateName);
    }

    @Override
    public synchronized int getSize() {
        return templateNames.size();
    }

    @Override
    public synchronized Object getElementAt(int index) {
        return templateNames.get(index);
    }
}

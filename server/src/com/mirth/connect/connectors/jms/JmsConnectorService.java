package com.mirth.connect.connectors.jms;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ExtensionController;

public class JmsConnectorService implements ConnectorService {
    private final static String PLUGIN_NAME = "JMS";
    private final static String PRESETS_PROPERTY = "presets";

    private Properties properties;
    private Serializer serializer = new ObjectXMLSerializer();
    private LinkedHashMap<String, JmsConnectorProperties> presets;
    private Logger logger = Logger.getLogger(getClass());

    /**
     * This method defines the default preset options for the JMS connectors.
     */
    private LinkedHashMap<String, JmsConnectorProperties> getDefaultPresets() {
        LinkedHashMap<String, JmsConnectorProperties> presets = new LinkedHashMap<String, JmsConnectorProperties>();

        JmsConnectorProperties connectorProperties = new JmsConnectorProperties();
        connectorProperties.setUseJndi(false);
        connectorProperties.setConnectionFactoryClass("org.apache.activemq.ActiveMQConnectionFactory");
        connectorProperties.getConnectionProperties().put("brokerURL", "failover:(tcp://localhost:61616)?maxReconnectAttempts=0");
        connectorProperties.getConnectionProperties().put("closeTimeout", "15000");
        connectorProperties.getConnectionProperties().put("useCompression", "no");
        presets.put("ActiveMQ", connectorProperties);

        connectorProperties = new JmsConnectorProperties();
        connectorProperties.setUseJndi(true);
        connectorProperties.setJndiProviderUrl("jnp://localhost:1099");
        connectorProperties.setJndiInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
        connectorProperties.setJndiConnectionFactoryName("java:/ConnectionFactory");
        connectorProperties.setUsername("guest");
        presets.put("JBoss Messaging / JBoss MQ", connectorProperties);

        return presets;
    }

    @Override
    public Object invoke(String method, Object object, String sessionId) throws Exception {
        if (method.equals("getPresets")) {
            load();
            return presets.keySet();
        } else if (method.equals("getPreset")) {
            load();
            return presets.get(object.toString());
        } else if (method.equals("savePreset")) {
            Object[] params = (Object[]) object;
            load();
            presets.put(params[0].toString(), (JmsConnectorProperties) params[1]);
            save();
        } else if (method.equals("deletePresets")) {
            load();
            @SuppressWarnings("unchecked")
            Set<String> deletePresets = (Set<String>) object;

            for (String preset : deletePresets) {
                presets.remove(preset);
            }

            save();
        } else if (method.equals("getPresetName")) {
            if (!(object instanceof JmsConnectorProperties)) {
                return null;
            }

            load();
            return lookupPresetName((JmsConnectorProperties) object);
        }

        return null;
    }

    public void load() throws JmsConnectorException {
        Object object;

        try {
            properties = ExtensionController.getInstance().getPluginProperties(PLUGIN_NAME);
            object = properties.get(PRESETS_PROPERTY);
        } catch (ControllerException e) {
            throw new JmsConnectorException("Failed to load the list of JMS provider classes from the extension properties for the JMS connector", e);
        }

        if (object == null || object.toString().isEmpty()) {
            presets = null;
        } else {
            presets = (LinkedHashMap<String, JmsConnectorProperties>) serializer.deserialize(object.toString());
        }

        if (presets == null || presets.isEmpty()) {
            presets = getDefaultPresets();
            save();
        }
    }

    public void save() {
        properties.put(PRESETS_PROPERTY, serializer.serialize(presets));

        try {
            ExtensionController.getInstance().setPluginProperties(PLUGIN_NAME, properties);
        } catch (ControllerException e) {
            logger.error("Failed to save list of JMS provider classes in the extension properties for the JMS connector");
        }
    }

    private String lookupPresetName(JmsConnectorProperties connectorProperties) {
        for (Entry<String, JmsConnectorProperties> entry : presets.entrySet()) {
            if (entry.getValue().equals(connectorProperties)) {
                return entry.getKey();
            }
        }

        return null;
    }
}

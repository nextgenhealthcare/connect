package com.mirth.connect.connectors.jms;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;

public class JmsConnectorProperties extends ConnectorProperties {
    private boolean useJndi;
    private String jndiProviderUrl;
    private String jndiInitialContextFactory;
    private String jndiConnectionFactoryName;
    private String connectionFactoryClass;
    private String username;
    private String password;
    private String destinationName;
    private boolean topic;
    private String reconnectIntervalMillis;
    private String clientId;
    private Map<String, String> connectionProperties;

    protected JmsConnectorProperties() {
        useJndi = false;
        jndiProviderUrl = "";
        jndiInitialContextFactory = "";
        jndiConnectionFactoryName = "";
        connectionFactoryClass = "";
        username = "";
        password = "";
        destinationName = "";
        topic = false;
        reconnectIntervalMillis = "10000";
        clientId = "";
        connectionProperties = new HashMap<String, String>();
    }

    public void setProperties(JmsConnectorProperties connectorProperties) {
        useJndi = connectorProperties.isUseJndi();
        jndiProviderUrl = connectorProperties.getJndiProviderUrl();
        jndiInitialContextFactory = connectorProperties.getJndiInitialContextFactory();
        jndiConnectionFactoryName = connectorProperties.getJndiConnectionFactoryName();
        connectionFactoryClass = connectorProperties.getConnectionFactoryClass();
        username = connectorProperties.getUsername();
        password = connectorProperties.getPassword();
        destinationName = connectorProperties.getDestinationName();
        topic = connectorProperties.isTopic();
        reconnectIntervalMillis = connectorProperties.getReconnectIntervalMillis();
        clientId = connectorProperties.getClientId();
        connectionProperties = connectorProperties.getConnectionProperties();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getProtocol() {
        return "JMS";
    }

    @Override
    public String toFormattedString() {
        String newLine = "\n";
        StringBuilder builder = new StringBuilder();

        if (useJndi) {
            builder.append("PROVIDER URL: " + jndiProviderUrl + newLine);
            builder.append("INITIAL CONTEXT FACTORY: " + jndiInitialContextFactory + newLine);
            builder.append("CONNECTION FACTORY NAME: " + jndiConnectionFactoryName + newLine);
            builder.append("DESTINATION: " + destinationName + newLine);
        } else {
            builder.append("CONNECTION FACTORY CLASS: " + connectionFactoryClass + newLine);

            if (topic) {
                builder.append("TOPIC: " + destinationName + newLine);
            } else {
                builder.append("QUEUE: " + destinationName + newLine);
            }
        }

        if (!clientId.isEmpty()) {
            builder.append("CLIENT ID: " + clientId + newLine);
        }

        if (!username.isEmpty()) {
            builder.append("USERNAME: " + username + newLine);
        }

        if (!connectionProperties.isEmpty()) {
            builder.append(newLine + "[CONNECTION PROPERTIES]" + newLine);

            for (Entry<String, String> property : connectionProperties.entrySet()) {
                builder.append(property.getKey() + ": " + property.getValue() + newLine);
            }

            builder.append(newLine);
        }

        return builder.toString();
    }

    public boolean isUseJndi() {
        return useJndi;
    }

    public void setUseJndi(boolean useJndi) {
        this.useJndi = useJndi;
    }

    public String getJndiProviderUrl() {
        return jndiProviderUrl;
    }

    public void setJndiProviderUrl(String jndiProviderUrl) {
        this.jndiProviderUrl = jndiProviderUrl;
    }

    public String getJndiInitialContextFactory() {
        return jndiInitialContextFactory;
    }

    public void setJndiInitialContextFactory(String jndiInitialContextFactory) {
        this.jndiInitialContextFactory = jndiInitialContextFactory;
    }

    public String getJndiConnectionFactoryName() {
        return jndiConnectionFactoryName;
    }

    public void setJndiConnectionFactoryName(String jndiConnectionFactoryName) {
        this.jndiConnectionFactoryName = jndiConnectionFactoryName;
    }

    public String getConnectionFactoryClass() {
        return connectionFactoryClass;
    }

    public void setConnectionFactoryClass(String connectionFactoryClass) {
        this.connectionFactoryClass = connectionFactoryClass;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public boolean isTopic() {
        return topic;
    }

    public void setTopic(boolean topic) {
        this.topic = topic;
    }

    public String getReconnectIntervalMillis() {
        return reconnectIntervalMillis;
    }

    public void setReconnectIntervalMillis(String reconnectIntervalMillis) {
        this.reconnectIntervalMillis = reconnectIntervalMillis;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Map<String, String> getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(Map<String, String> connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof JmsConnectorProperties)) {
            return false;
        }

        JmsConnectorProperties connectorProperties = (JmsConnectorProperties) object;

        // @formatter:off
        return (useJndi == connectorProperties.isUseJndi()
                && jndiProviderUrl.equals(connectorProperties.getJndiProviderUrl())
                && jndiInitialContextFactory.equals(connectorProperties.getJndiInitialContextFactory())
                && jndiConnectionFactoryName.equals(connectorProperties.getJndiConnectionFactoryName())
                && connectionFactoryClass.equals(connectorProperties.getConnectionFactoryClass())
                && username.equals(connectorProperties.getUsername())
                && password.equals(connectorProperties.getPassword())
                && destinationName.equals(connectorProperties.getDestinationName())
                && topic == connectorProperties.isTopic()
                && reconnectIntervalMillis.equals(connectorProperties.getReconnectIntervalMillis())
                && clientId.equals(connectorProperties.getClientId())
                && connectionProperties.equals(connectorProperties.getConnectionProperties()));
        // @formatter:on
    }
}

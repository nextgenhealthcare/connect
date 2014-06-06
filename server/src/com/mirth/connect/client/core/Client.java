/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import java.io.File;
import java.security.Provider;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.commons.encryption.KeyEncryptor;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.ChannelStatistics;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.DashboardChannelInfo;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.EncryptionSettings;
import com.mirth.connect.model.LoginStatus;
import com.mirth.connect.model.MessageImportResult;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.User;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.alert.AlertStatus;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.EventFilter;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.util.ConfigurationProperty;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class Client {
    private Logger logger = Logger.getLogger(this.getClass());
    private ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
    private ServerConnection serverConnection;
    private String address;
    private int timeout;

    public final static String USER_SERVLET = "/users";
    public final static String CHANNEL_SERVLET = "/channels";
    public final static String CONFIGURATION_SERVLET = "/configuration";
    public final static String CHANNEL_STATUS_SERVLET = "/channelstatus";
    public final static String CHANNEL_STATISTICS_SERVLET = "/channelstatistics";
    public final static String MESSAGE_SERVLET = "/messages";
    public final static String EVENT_SERVLET = "/events";
    public final static String ALERT_SERVLET = "/alerts";
    public final static String TEMPLATE_SERVLET = "/codetemplates";
    public final static String EXTENSION_SERVLET = "/extensions";
    public final static String ENGINE_SERVLET = "/engine";

    /**
     * Instantiates a new Mirth client with a connection to the specified server.
     * 
     * @param address
     */
    public Client(String address) {
        this.address = address;
        serverConnection = ServerConnectionFactory.createServerConnection(address);
    }

    public Client(String address, int timeout) {
        this.address = address;
        this.timeout = timeout;
        serverConnection = ServerConnectionFactory.createServerConnection(address, this.timeout);
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
        serverConnection = ServerConnectionFactory.createServerConnection(address, this.timeout);
    }

    public int getTimeout() {
        return timeout;
    }

    public void cleanup() {
        if (serverConnection != null)
            serverConnection.shutdown();
    }

    public ServerConnection getServerConnection() {
        return serverConnection;
    }

    /**
     * Logs a user in to the Mirth server using the specified name and password.
     * 
     * @param username
     * @param password
     * @return
     * @throws ClientException
     */
    public synchronized LoginStatus login(String username, String password, String version) throws ClientException {
        logger.debug("attempting to login user: username=" + username);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.USER_LOGIN.getName()), new BasicNameValuePair("username", username), new BasicNameValuePair("password", password), new BasicNameValuePair("version", version) };
        return serializer.deserialize(serverConnection.executePostMethod(USER_SERVLET, params), LoginStatus.class);
    }

    /**
     * Logs the user out of the server.
     * 
     * @throws ClientException
     */
    public synchronized void logout() throws ClientException {
        logger.debug("logging out");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.USER_LOGOUT.getName()) };
        serverConnection.executePostMethod(USER_SERVLET, params);
    }

    /**
     * Returns the server id.
     * 
     * @return
     * @throws ClientException
     */
    public String getServerId() throws ClientException {
        logger.debug("retrieving server's id");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CONFIGURATION_SERVER_ID_GET.getName()) };
        return serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
    }

    /**
     * Returns the time zone of the server.
     * 
     * @return
     * @throws ClientException
     */
    public String getServerTimezone() throws ClientException {
        logger.debug("retrieving server's timezone");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CONFIGURATION_SERVER_TIMEZONE_GET.getName()) };
        return serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
    }

    /**
     * Returns a ServerConfiguration object which contains all of the channels, users, alerts and properties stored on the Mirth server.
     * 
     * @return
     * @throws ClientException
     */
    public ServerConfiguration getServerConfiguration() throws ClientException {
        logger.debug("getting server configuration");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.SERVER_CONFIGURATION_GET.getName()) };
        return serializer.deserialize(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params), ServerConfiguration.class);
    }

    /**
     * Sets a ServerConfiguration object which sets all of the channels, alerts and properties stored on the Mirth server.
     * 
     * @return
     * @throws ClientException
     */
    public synchronized void setServerConfiguration(ServerConfiguration serverConfiguration) throws ClientException {
        logger.debug("setting server configuration");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.SERVER_CONFIGURATION_SET.getName()), new BasicNameValuePair("data", serializer.serialize(serverConfiguration)) };
        serverConnection.executePostMethodAsync(CONFIGURATION_SERVLET, params);
    }

    /**
     * Returns a List of all channels.
     * 
     * @return
     * @throws ClientException
     */
    public List<Channel> getChannels(Set<String> channelIds) throws ClientException {
        logger.debug("getting channel");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_GET.getName()), new BasicNameValuePair("channelIds", serializer.serialize(channelIds)) };
        return serializer.deserializeList(serverConnection.executePostMethod(CHANNEL_SERVLET, params), Channel.class);
    }

    public List<ChannelSummary> getChannelSummary(Map<String, ChannelHeader> cachedChannels) throws ClientException {
        logger.debug("getting channel summary");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_GET_SUMMARY.getName()), new BasicNameValuePair("cachedChannels", serializer.serialize(cachedChannels)) };
        return serializer.deserializeList(serverConnection.executePostMethodAsync(CHANNEL_SERVLET, params), ChannelSummary.class);
    }

    /**
     * Enables/disables the specified channels.
     * 
     * @param channel
     * @throws ClientException
     */
    public synchronized void setChannelEnabled(Set<String> channelIds, boolean enabled) throws ClientException {
        logger.debug("updating channel: channelIds=" + channelIds + ", enabled=" + enabled);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_SET_ENABLED.getName()), new BasicNameValuePair("channelIds", serializer.serialize(channelIds)), new BasicNameValuePair("enabled", new Boolean(enabled).toString()) };
        serverConnection.executePostMethod(CHANNEL_SERVLET, params);
    }
    
    /**
     * Updates the specified channel.
     * 
     * @param channel
     * @throws ClientException
     */
    public synchronized boolean updateChannel(Channel channel, boolean override) throws ClientException {
        logger.debug("updating channel: channelId=" + channel.getId() + ", override=" + override);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_UPDATE.getName()), new BasicNameValuePair("channel", serializer.serialize(channel)), new BasicNameValuePair("override", new Boolean(override).toString()) };
        return Boolean.valueOf(serverConnection.executePostMethod(CHANNEL_SERVLET, params)).booleanValue();
    }

    /**
     * Removes the channel with the specified id.
     * 
     * @param channelId
     * @throws ClientException
     */
    public synchronized void removeChannel(Channel channel) throws ClientException {
        removeChannels(Collections.singleton(channel.getId()), false);
    }

    /**
     * Removes the channel with the specified IDs. If undeployFirst is true, any currently deployed
     * channels will also first attempt to be undeployed.
     * 
     * @param channelIds
     * @param undeployFirst
     * @throws ClientException
     */
    public synchronized void removeChannels(Set<String> channelIds, boolean undeployFirst) throws ClientException {
        logger.debug("removing channel: channelIds=" + String.valueOf(channelIds) + ", undeployFirst=" + undeployFirst);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_REMOVE.getName()), new BasicNameValuePair("channelIds", serializer.serialize(channelIds)), new BasicNameValuePair("undeployFirst", new Boolean(undeployFirst).toString()) };
        serverConnection.executePostMethod(CHANNEL_SERVLET, params);
    }

    /**
     * Install new extension
     * 
     * @return
     * @throws ClientException
     */
    public void installExtension(File file) throws ClientException {
        logger.debug("installing extension");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.EXTENSION_INSTALL.getName()) };
        serverConnection.executeFileUpload(EXTENSION_SERVLET, params, file);
    }

    /**
     * Uninstall an extension
     * 
     * @return
     * @throws ClientException
     */
    public void uninstallExtension(String packageName) throws ClientException {
        logger.debug("installing extension");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.EXTENSION_UNINSTALL.getName()), new BasicNameValuePair("packageName", packageName) };
        serverConnection.executePostMethod(EXTENSION_SERVLET, params);
    }

    /**
     * Returns a List of all connectors.
     * 
     * @return
     * @throws ClientException
     */
    public Map<String, ConnectorMetaData> getConnectorMetaData() throws ClientException {
        logger.debug("retrieving connector list");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CONNECTOR_METADATA_GET.getName()) };
        return serializer.deserialize(serverConnection.executePostMethod(EXTENSION_SERVLET, params), Map.class);
    }

    /**
     * Returns a List of all plugins.
     * 
     * @return
     * @throws ClientException
     */
    public Map<String, PluginMetaData> getPluginMetaData() throws ClientException {
        logger.debug("retrieving plugin list");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.PLUGIN_METADATA_GET.getName()) };
        return serializer.deserialize(serverConnection.executePostMethod(EXTENSION_SERVLET, params), Map.class);
    }

    /**
     * Sets an exension as enabled or disabled.
     * 
     * @return
     * @throws ClientException
     */
    public void setExtensionEnabled(String extensionName, boolean enabled) throws ClientException {
        logger.debug("setting extension enabled");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.EXTENSION_SET_ENABLED.getName()), new BasicNameValuePair("name", extensionName), new BasicNameValuePair("enabled", BooleanUtils.toStringTrueFalse(enabled)) };
        serverConnection.executePostMethod(EXTENSION_SERVLET, params);
    }

    /**
     * Invoke a method on a plugin and pass back the Object returned
     * 
     * @return
     * @throws ClientException
     */
    public Object invokePluginMethod(String pluginName, String method, Object object) throws ClientException {
        logger.debug("invoking method " + method + " on " + pluginName);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.PLUGIN_SERVICE_INVOKE.getName()), new BasicNameValuePair("name", pluginName), new BasicNameValuePair("method", method), new BasicNameValuePair("object", serializer.serialize(object)) };
        return serializer.deserialize(serverConnection.executePostMethod(EXTENSION_SERVLET, params), Object.class);
    }

    /**
     * Invoke a method on a plugin and pass back the Object returned
     * 
     * @return
     * @throws ClientException
     */
    public Object invokePluginMethodAsync(String pluginName, String method, Object object) throws ClientException {
        logger.debug("invoking method " + method + " on " + pluginName);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.PLUGIN_SERVICE_INVOKE.getName()), new BasicNameValuePair("name", pluginName), new BasicNameValuePair("method", method), new BasicNameValuePair("object", serializer.serialize(object)) };
        return serializer.deserialize(serverConnection.executePostMethodAsync(EXTENSION_SERVLET, params), Object.class);
    }

    /**
     * Invoke a method on a connector and pass back the Object returned
     * 
     * @return
     * @throws ClientException
     */
    public Object invokeConnectorService(String channelId, String connectorName, String method, Object object) throws ClientException {
        logger.debug("invoking connector service " + method + " on " + connectorName);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CONNECTOR_SERVICE_INVOKE.getName()), new BasicNameValuePair("channelId", channelId), new BasicNameValuePair("name", connectorName), new BasicNameValuePair("method", method), new BasicNameValuePair("object", serializer.serialize(object)) };
        return serializer.deserialize(serverConnection.executePostMethod(EXTENSION_SERVLET, params), Object.class);
    }

    /**
     * Returns a List of all of the encodings supported by the server
     * 
     * @return
     * @throws ClientException
     */
    // ast: The available charset encodings depends on the JVM in which the
    // server is running
    public List<String> getAvailableCharsetEncodings() throws ClientException {
        logger.debug("retrieving the server supported charset encoging list");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CONFIGURATION_CHARSET_ENCODINGS_GET.getName()) };
        return serializer.deserializeList(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params), String.class);
    }

    /**
     * Returns a List of all users.
     * 
     * @return
     * @throws ClientException
     */
    public List<User> getUser(User user) throws ClientException {
        logger.debug("getting user: " + user);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.USER_GET.getName()), new BasicNameValuePair("user", serializer.serialize(user)) };
        return serializer.deserializeList(serverConnection.executePostMethodAsync(USER_SERVLET, params), User.class);
    }

    /**
     * Updates a specified user.
     * 
     * @param user
     * @throws ClientException
     */
    public synchronized void updateUser(User user) throws ClientException {
        logger.debug("updating user: " + user);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.USER_UPDATE.getName()), new BasicNameValuePair("user", serializer.serialize(user)) };
        serverConnection.executePostMethod(USER_SERVLET, params);
    }

    /**
     * Checks the password against the configured password policies if a null user id is passed in. If a user with an id is passed in their password is also updated.
     * 
     * @param userId
     * @param plainPassword
     * @return A list of errors that occurred with the password
     * @throws ClientException
     */
    public synchronized List<String> checkOrUpdateUserPassword(User user, String plainPassword) throws ClientException {
        logger.debug("updating password for user: " + user);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.USER_CHECK_OR_UPDATE_PASSWORD.getName()), new BasicNameValuePair("user", serializer.serialize(user)), new BasicNameValuePair("plainPassword", plainPassword) };
        return serializer.deserializeList(serverConnection.executePostMethod(USER_SERVLET, params), String.class);
    }

    /**
     * Removes the user with the specified id.
     * 
     * @param userId
     * @throws ClientException
     */
    public synchronized void removeUser(User user) throws ClientException {
        logger.debug("removing user: " + user);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.USER_REMOVE.getName()), new BasicNameValuePair("user", serializer.serialize(user)) };
        serverConnection.executePostMethod(USER_SERVLET, params);
    }

    /**
     * Returns a true if the specified user is logged in to the server.
     * 
     * @return
     * @throws ClientException
     */
    public boolean isUserLoggedIn(User user) throws ClientException {
        logger.debug("checking if user logged in: " + user);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.USER_IS_USER_LOGGED_IN.getName()), new BasicNameValuePair("user", serializer.serialize(user)) };
        return Boolean.valueOf(serverConnection.executePostMethod(USER_SERVLET, params));
    }
    
    /**
     * Returns a List of all alert statuses.
     * 
     * @return
     * @throws ClientException
     */
    public List<AlertStatus> getAlertStatusList() throws ClientException {
        logger.debug("retrieving alert statuses");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.ALERT_GET_STATUS.getName()) };
        return serializer.deserializeList(serverConnection.executePostMethod(ALERT_SERVLET, params), AlertStatus.class);
    }

    /**
     * Returns a List of all alerts.
     * 
     * @return
     * @throws ClientException
     */
    public List<AlertModel> getAlert(String alertId) throws ClientException {
        logger.debug("getting alert: " + alertId);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.ALERT_GET.getName()), new BasicNameValuePair("alertId", alertId) };
        return serializer.deserializeList(serverConnection.executePostMethod(ALERT_SERVLET, params), AlertModel.class);
    }

    /**
     * Updates a list of alerts.
     * 
     * @param alert
     * @throws ClientException
     */
    public synchronized void updateAlert(AlertModel alertModel) throws ClientException {
        logger.debug("updating alert: " + alertModel.getId());
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.ALERT_UPDATE.getName()), new BasicNameValuePair("alertModel", serializer.serialize(alertModel)) };
        serverConnection.executePostMethod(ALERT_SERVLET, params);
    }

    /**
     * Removes the alert with the specified id.
     * 
     * @param alertId
     * @throws ClientException
     */
    public synchronized void removeAlert(String alertId) throws ClientException {
        logger.debug("removing alert: " + alertId);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.ALERT_REMOVE.getName()), new BasicNameValuePair("alertId", alertId) };
        serverConnection.executePostMethod(ALERT_SERVLET, params);
    }
    
    /**
     * Enables the alert with the specified id.
     * 
     * @param alertId
     * @throws ClientException
     */
    public synchronized void enableAlert(String alertId) throws ClientException {
        logger.debug("removing alert: " + alertId);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.ALERT_ENABLE.getName()), new BasicNameValuePair("alertId", alertId) };
        serverConnection.executePostMethod(ALERT_SERVLET, params);
    }
    
    /**
     * Disables the alert with the specified id.
     * 
     * @param alertId
     * @throws ClientException
     */
    public synchronized void disableAlert(String alertId) throws ClientException {
        logger.debug("removing alert: " + alertId);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.ALERT_DISABLE.getName()), new BasicNameValuePair("alertId", alertId) };
        serverConnection.executePostMethod(ALERT_SERVLET, params);
    }

    /**
     * Returns a List of all code templates.
     * 
     * @return
     * @throws ClientException
     */
    public List<CodeTemplate> getCodeTemplate(CodeTemplate codeTemplate) throws ClientException {
        logger.debug("getting code template: " + codeTemplate);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CODE_TEMPLATE_GET.getName()), new BasicNameValuePair("codeTemplate", serializer.serialize(codeTemplate)) };
        return serializer.deserializeList(serverConnection.executePostMethod(TEMPLATE_SERVLET, params), CodeTemplate.class);
    }

    /**
     * Updates a list of code templates.
     * 
     * @param code
     *            template
     * @throws ClientException
     */
    public synchronized void updateCodeTemplates(List<CodeTemplate> codeTemplates) throws ClientException {
        logger.debug("updating code templates: " + codeTemplates);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CODE_TEMPLATE_UPDATE.getName()), new BasicNameValuePair("codeTemplates", serializer.serialize(codeTemplates)) };
        serverConnection.executePostMethod(TEMPLATE_SERVLET, params);
    }

    /**
     * Removes the specified code template.
     * 
     * @param codeTemplate
     * @throws ClientException
     */
    public synchronized void removeCodeTemplate(CodeTemplate codeTemplate) throws ClientException {
        logger.debug("removing code template: " + codeTemplate);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CODE_TEMPLATE_REMOVE.getName()), new BasicNameValuePair("codeTemplate", serializer.serialize(codeTemplate)) };
        serverConnection.executePostMethod(TEMPLATE_SERVLET, params);
    }

    /**
     * Returns a ServerSettings object with all server settings.
     * 
     * @return
     * @throws ClientException
     */
    public ServerSettings getServerSettings() throws ClientException {
        logger.debug("retrieving server settings");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CONFIGURATION_SERVER_SETTINGS_GET.getName()) };
        return serializer.deserialize(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params), ServerSettings.class);
    }

    /**
     * Returns an EncryptionSettings object with all encrpytion settings.
     * 
     * @return
     * @throws ClientException
     */
    public EncryptionSettings getEncryptionSettings() throws ClientException {
        logger.debug("retrieving encryption settings");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CONFIGURATION_ENCRYPTION_SETTINGS_GET.getName()) };
        return serializer.deserialize(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params), EncryptionSettings.class);
    }

    /**
     * Updates the server configuration settings.
     * 
     * @param settings
     * @throws ClientException
     */
    public synchronized void setServerSettings(ServerSettings settings) throws ClientException {
        logger.debug("updating server settings");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CONFIGURATION_SERVER_SETTINGS_SET.getName()), new BasicNameValuePair("data", serializer.serialize(settings)) };
        serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
    }

    /**
     * Returns an UpdateSettings object with all update settings.
     * 
     * @return
     * @throws ClientException
     */
    public UpdateSettings getUpdateSettings() throws ClientException {
        logger.debug("retrieving update settings");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CONFIGURATION_UPDATE_SETTINGS_GET.getName()) };
        return serializer.deserialize(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params), UpdateSettings.class);
    }

    /**
     * Updates the update settings.
     * 
     * @param settings
     * @throws ClientException
     */
    public synchronized void setUpdateSettings(UpdateSettings settings) throws ClientException {
        logger.debug("updating update settings");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CONFIGURATION_UPDATE_SETTINGS_SET.getName()), new BasicNameValuePair("data", serializer.serialize(settings)) };
        serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
    }

    /**
     * Returns a globaly unique id.
     * 
     * @return
     * @throws ClientException
     */
    public String getGuid() throws ClientException {
        return UUID.randomUUID().toString();
    }

    /**
     * Re-deploys all channels.
     * 
     * @throws ClientException
     */
    public void redeployAllChannels() throws ClientException {
        logger.debug("deploying channels");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_REDEPLOY.getName()) };
        serverConnection.executePostMethodAbortPending(ENGINE_SERVLET, params);
    }

    /**
     * Hot deploys specific channels.
     * 
     * @throws ClientException
     */
    public void deployChannels(Set<String> channelIds) throws ClientException {
        logger.debug("deploying channels");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_DEPLOY.getName()), new BasicNameValuePair("channelIds", serializer.serialize(channelIds)) };
        serverConnection.executePostMethodAbortPending(ENGINE_SERVLET, params);
    }

    /**
     * Undeploys specific channels.
     * 
     * @throws ClientException
     */
    public void undeployChannels(Set<String> channelIds) throws ClientException {
        logger.debug("undeploying channels");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_UNDEPLOY.getName()), new BasicNameValuePair("channelIds", serializer.serialize(channelIds)) };
        serverConnection.executePostMethodAbortPending(ENGINE_SERVLET, params);
    }

    /**
     * Starts the channel with the specified id.
     * 
     * @param channelId
     * @throws ClientException
     */
    public void startChannel(String channelId) throws ClientException {
        startChannels(Collections.singleton(channelId));
    }
    
    /**
     * Starts the channels with the specified IDs.
     * 
     * @param channelIds
     * @throws ClientException
     */
    public void startChannels(Set<String> channelIds) throws ClientException {
        logger.debug("starting channels: channelIds=" + String.valueOf(channelIds));
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_START.getName()), new BasicNameValuePair("channelIds", serializer.serialize(channelIds)) };
        serverConnection.executePostMethodAbortPending(CHANNEL_STATUS_SERVLET, params);
    }

    /**
     * Stops the channel with the specified id.
     * 
     * @param channelId
     * @throws ClientException
     */
    public void stopChannel(String channelId) throws ClientException {
        stopChannels(Collections.singleton(channelId));
    }
    
    /**
     * Stops the channels with the specified IDs.
     * 
     * @param channelIds
     * @throws ClientException
     */
    public void stopChannels(Set<String> channelIds) throws ClientException {
        logger.debug("stopping channels: channelIds=" + String.valueOf(channelIds));
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_STOP.getName()), new BasicNameValuePair("channelIds", serializer.serialize(channelIds)) };
        serverConnection.executePostMethodAbortPending(CHANNEL_STATUS_SERVLET, params);
    }
    
    /**
     * Halts the channel with the specified id.
     * 
     * @param channelId
     * @throws ClientException
     */
    public void haltChannel(String channelId) throws ClientException {
        haltChannels(Collections.singleton(channelId));
    }
    
    /**
     * Halts the channels with the specified IDs.
     * 
     * @param channelIds
     * @throws ClientException
     */
    public void haltChannels(Set<String> channelIds) throws ClientException {
        logger.debug("halting channels: channelIds=" + String.valueOf(channelIds));
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_HALT.getName()), new BasicNameValuePair("channelIds", serializer.serialize(channelIds)) };
        serverConnection.executePostMethodAsync(CHANNEL_STATUS_SERVLET, params);
    }

    /**
     * Pauses the channel with the specified id.
     * 
     * @param channelId
     * @throws ClientException
     */
    public void pauseChannel(String channelId) throws ClientException {
        pauseChannels(Collections.singleton(channelId));
    }
    
    /**
     * Pauses the channels with the specified IDs.
     * 
     * @param channelIds
     * @throws ClientException
     */
    public void pauseChannels(Set<String> channelIds) throws ClientException {
        logger.debug("pausing channels: channelIds=" + String.valueOf(channelIds));
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_PAUSE.getName()), new BasicNameValuePair("channelIds", serializer.serialize(channelIds)) };
        serverConnection.executePostMethodAbortPending(CHANNEL_STATUS_SERVLET, params);
    }

    /**
     * Resumes the channel with the specified id.
     * 
     * @param channelId
     * @throws ClientException
     */
    public void resumeChannel(String channelId) throws ClientException {
        resumeChannels(Collections.singleton(channelId));
    }
    
    /**
     * Resumes the channels with the specified IDs.
     * 
     * @param channelIds
     * @throws ClientException
     */
    public void resumeChannels(Set<String> channelIds) throws ClientException {
        logger.debug("resuming channels: channelIds=" + String.valueOf(channelIds));
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_RESUME.getName()), new BasicNameValuePair("channelIds", serializer.serialize(channelIds)) };
        serverConnection.executePostMethodAbortPending(CHANNEL_STATUS_SERVLET, params);
    }
    
    /**
     * Starts the connector with the specified channel and metaData ids.
     * 
     * @param channelId
     * @param metaDataId
     * @throws ClientException
     */
    public void startConnector(String channelId, Integer metaDataId) throws ClientException {
        startConnectors(Collections.singletonMap(channelId, Collections.singletonList(metaDataId)));
    }
    
    /**
     * Starts the connectors with the specified channels and metadata IDs.
     * 
     * @param connectorInfo
     * @throws ClientException
     */
    public void startConnectors(Map<String, List<Integer>> connectorInfo) throws ClientException {
        logger.debug("starting connectors: connectorInfo=" + String.valueOf(connectorInfo));
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_START_CONNECTOR.getName()), new BasicNameValuePair("connectorInfo", serializer.serialize(connectorInfo)) };
        serverConnection.executePostMethodAbortPending(CHANNEL_STATUS_SERVLET, params);
    }
    
    /**
     * Stops the connector with the specified channel and metaData ids.
     * 
     * @param channelId
     * @param metaDataId
     * @throws ClientException
     */
    public void stopConnector(String channelId, Integer metaDataId) throws ClientException {
        stopConnectors(Collections.singletonMap(channelId, Collections.singletonList(metaDataId)));
    }
    
    /**
     * Stops the connectors with the specified channel and metadata IDs.
     * 
     * @param connectorInfo
     * @throws ClientException
     */
    public void stopConnectors(Map<String, List<Integer>> connectorInfo) throws ClientException {
        logger.debug("stopping connectors: connectorInfo=" + String.valueOf(connectorInfo));
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_STOP_CONNECTOR.getName()), new BasicNameValuePair("connectorInfo", serializer.serialize(connectorInfo)) };
        serverConnection.executePostMethodAbortPending(CHANNEL_STATUS_SERVLET, params);
    }

    /**
     * Returns the Statistics for the channel with the specified id.
     * 
     * @return
     * @throws ClientException
     */
    public ChannelStatistics getStatistics(String channelId) throws ClientException {
        logger.debug("retrieving channel statistics: channelId=" + channelId);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_STATS_GET.getName()), new BasicNameValuePair("id", channelId) };
        return serializer.deserialize(serverConnection.executePostMethod(CHANNEL_STATISTICS_SERVLET, params), ChannelStatistics.class);
    }

    /**
     * Clears the statistics for the given connectors and/or channels
     * 
     * @param channelConnectorMap
     *            Channel IDs mapped to lists of metaDataIds (connectors). If the metaDataId list is null, then all statistics for the channel will be cleared.
     * @return
     * @throws ClientException
     */
    public void clearStatistics(Map<String, List<Integer>> channelConnectorMap, boolean received, boolean filtered, boolean sent, boolean error) throws ClientException {
        logger.debug("clearing statistics");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_STATS_CLEAR.getName()), new BasicNameValuePair("channelConnectorMap", serializer.serialize(channelConnectorMap)), new BasicNameValuePair("deleteReceived", new Boolean(received).toString()), new BasicNameValuePair("deleteFiltered", new Boolean(filtered).toString()), new BasicNameValuePair("deleteSent", new Boolean(sent).toString()), new BasicNameValuePair("deleteErrored", new Boolean(error).toString()) };
        serverConnection.executePostMethod(CHANNEL_STATISTICS_SERVLET, params);
    }

    public void clearAllStatistics() throws ClientException {
        logger.debug("clearing all statistics (including lifetime)");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_STATS_CLEAR_ALL.getName()) };
        serverConnection.executePostMethod(CHANNEL_STATISTICS_SERVLET, params);
    }

    public Integer getMaxEventId() throws ClientException {
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.EVENT_GET_MAX_ID.getName()) };
        Integer maxEventId = null;

        try {
            maxEventId = Integer.parseInt(serverConnection.executePostMethod(Client.EVENT_SERVLET, params));
        } catch (NumberFormatException e) {
            return null;
        }

        return maxEventId;
    }

    public List<ServerEvent> getEvents(EventFilter filter, Integer offset, Integer limit) throws ClientException {
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.EVENT_GET.getName()),
                new BasicNameValuePair("filter", serializer.serialize(filter)),
                new BasicNameValuePair("offset", (offset == null) ? "" : offset.toString()),
                new BasicNameValuePair("limit", (limit == null) ? "" : limit.toString()) };

        return serializer.deserializeList(serverConnection.executePostMethod(Client.EVENT_SERVLET, params), ServerEvent.class);
    }

    public Long getEventCount(EventFilter filter) throws ClientException {
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.EVENT_GET_COUNT.getName()),
                new BasicNameValuePair("filter", serializer.serialize(filter)) };
        Long count = null;

        try {
            count = Long.parseLong(serverConnection.executePostMethod(Client.EVENT_SERVLET, params));
        } catch (NumberFormatException e) {
            return null;
        }

        return count;
    }

    public void removeAllEvents() throws ClientException {
        logger.debug("removing all events");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.EVENT_REMOVE_ALL.getName()) };
        serverConnection.executePostMethod(EVENT_SERVLET, params);
    }

    public String exportAllEvents() throws ClientException {
        logger.debug("exporting all events");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.EVENT_EXPORT_ALL.getName()) };
        return (String) serverConnection.executePostMethod(EVENT_SERVLET, params);
    }

    public String exportAndRemoveAllEvents() throws ClientException {
        logger.debug("exporting and removing all events");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.EVENT_EXPORT_AND_REMOVE_ALL.getName()) };
        return (String) serverConnection.executePostMethod(EVENT_SERVLET, params);
    }

    public void removeMessages(String channelId, MessageFilter filter) throws ClientException {
        logger.debug("removing messages");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.MESSAGE_REMOVE.getName()), new BasicNameValuePair("channelId", channelId), new BasicNameValuePair("filter", serializer.serialize(filter)) };
        serverConnection.executePostMethod(MESSAGE_SERVLET, params);
    }

    public void reprocessMessages(String channelId, MessageFilter filter, boolean replace, List<Integer> reprocessMetaDataIds) throws ClientException {
        logger.debug("reprocessing messages");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.MESSAGE_REPROCESS.getName()), new BasicNameValuePair("channelId", channelId), new BasicNameValuePair("filter", serializer.serialize(filter)), new BasicNameValuePair("replace", String.valueOf(replace)), new BasicNameValuePair("reprocessMetaDataIds", serializer.serialize(reprocessMetaDataIds)) };
        serverConnection.executePostMethodAsync(MESSAGE_SERVLET, params);
    }

    public void processMessage(String channelId, String rawMessage) throws ClientException {
        processMessage(channelId, rawMessage, null);
    }

    public void processMessage(String channelId, String rawMessage, List<Integer> metaDataIds) throws ClientException {
        logger.debug("processing message");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.MESSAGE_PROCESS.getName()), new BasicNameValuePair("channelId", channelId), new BasicNameValuePair("message", rawMessage), new BasicNameValuePair("metaDataIds", serializer.serialize(metaDataIds)) };
        serverConnection.executePostMethodAsync(MESSAGE_SERVLET, params);
    }

    public Encryptor getEncryptor() {
        KeyEncryptor encryptor = null;

        try {
            EncryptionSettings encryptionSettings = getEncryptionSettings();
            encryptor = new KeyEncryptor();
            encryptor.setProvider((Provider) Class.forName(encryptionSettings.getSecurityProvider()).newInstance());
            SecretKey secretKey = new SecretKeySpec(encryptionSettings.getSecretKey(), encryptionSettings.getEncryptionAlgorithm());
            encryptor.setKey(secretKey);
        } catch (Exception e) {
            logger.error("Unable to load encryption settings.", e);
        }

        return encryptor;
    }

    public boolean isEncryptExport() {
        try {
            return getEncryptionSettings().getEncryptExport();
        } catch (Exception e) {
            logger.error("Unable to load encryption settings.");
        }

        return false;
    }

    public void importMessage(String channelId, Message message) throws ClientException {
        logger.debug("importing message");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.MESSAGE_IMPORT.getName()), new BasicNameValuePair("channelId", channelId), new BasicNameValuePair("message", serializer.serialize(message)) };
        serverConnection.executePostMethodAsync(MESSAGE_SERVLET, params);
    }
    
    public MessageImportResult importMessagesServer(String channelId, String path, boolean includeSubfolders) throws ClientException {
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.MESSAGE_IMPORT_SERVER.getName()), new BasicNameValuePair("channelId", channelId), new BasicNameValuePair("path", path), new BasicNameValuePair("includeSubfolders", serializer.serialize(includeSubfolders)) };
        return serializer.deserialize(serverConnection.executePostMethodAsync(Client.MESSAGE_SERVLET, params), MessageImportResult.class);
    }

    public int exportMessagesServer(final String channelId, final MessageFilter filter, final int pageSize, final boolean includeAttachments, final MessageWriterOptions writerOptions) throws ClientException {
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.MESSAGE_EXPORT.getName()), new BasicNameValuePair("channelId", channelId), new BasicNameValuePair("filter", serializer.serialize(filter)), new BasicNameValuePair("pageSize", serializer.serialize(pageSize)), new BasicNameValuePair("includeAttachments", serializer.serialize(includeAttachments)), new BasicNameValuePair("writerOptions", serializer.serialize(writerOptions)) };
        
        try {
            return Integer.parseInt(serverConnection.executePostMethodAsync(Client.MESSAGE_SERVLET, params));
        } catch (NumberFormatException e) {
            logger.error(e);
            return 0;
        }
    }
    

    public Map<String, String> getGlobalScripts() throws ClientException {
        logger.debug("getting global scripts");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.GLOBAL_SCRIPT_GET.getName()) };
        return serializer.deserialize(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params), Map.class);
    }

    public void setGlobalScripts(Map<String, String> scripts) throws ClientException {
        logger.debug("setting global scripts");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.GLOBAL_SCRIPT_SET.getName()), new BasicNameValuePair("scripts", serializer.serialize(scripts)) };
        serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
    }

    public Map<String, ConfigurationProperty> getConfigurationMap() throws ClientException {
        logger.debug("getting configuration map");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CONFIGURATION_MAP_GET.getName()) };
        return serializer.deserialize(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params), Map.class);
    }

    public void setConfigurationMap(Map<String, ConfigurationProperty> map) throws ClientException {
        logger.debug("setting configuration map");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CONFIGURATION_MAP_SET.getName()), new BasicNameValuePair("map", serializer.serialize(map)) };
        serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
    }
    
    /**
     * Sets properties for a given plugin
     * 
     * @return
     * @throws ClientException
     */
    public Properties getPluginProperties(String pluginName) throws ClientException {
        logger.debug("getting " + pluginName + " properties");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.PLUGIN_PROPERTIES_GET.getName()), new BasicNameValuePair("name", pluginName) };
        return serializer.deserialize(serverConnection.executePostMethod(EXTENSION_SERVLET, params), Properties.class);
    }

    /**
     * Gets properties for a given plugin
     * 
     * @return
     * @throws ClientException
     */
    public void setPluginProperties(String pluginName, Properties properties) throws ClientException {
        logger.debug("setting " + pluginName + " properties");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.PLUGIN_PROPERTIES_SET.getName()), new BasicNameValuePair("name", pluginName), new BasicNameValuePair("properties", serializer.serialize(properties)) };
        serverConnection.executePostMethod(EXTENSION_SERVLET, params);
    }

    /**
     * True if an extension is installed and enabled, else return false
     * 
     * @return
     * @throws ClientException
     */
    public boolean isExtensionEnabled(String extensionName) throws ClientException {
        logger.debug("checking if " + extensionName + " is installed/enabled");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.EXTENSION_IS_ENABLED.getName()), new BasicNameValuePair("name", extensionName) };
        return Boolean.valueOf(serverConnection.executePostMethod(EXTENSION_SERVLET, params)).booleanValue();
    }
    
    public Map<Integer, String> getConnectorNames(String channelId) throws ClientException {
        logger.debug("retrieving channel connector names");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_GET_CONNECTOR_NAMES.getName()), new BasicNameValuePair("channelId", channelId) };
        return serializer.deserialize(serverConnection.executePostMethod(CHANNEL_SERVLET, params), Map.class);
    }
    
    public List<MetaDataColumn> getMetaDataColumns(String channelId) throws ClientException {
        logger.debug("retrieving channel metadata columns");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_GET_METADATA_COLUMNS.getName()), new BasicNameValuePair("channelId", channelId) };
        return serializer.deserializeList(serverConnection.executePostMethod(CHANNEL_SERVLET, params), MetaDataColumn.class);
    }

    public void clearMessages(Set<String> channelIds, Boolean restartRunningChannels, Boolean clearStatistics) throws ClientException {
        logger.debug("clearing messages");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.MESSAGE_CLEAR.getName()), new BasicNameValuePair("channelIds", serializer.serialize(channelIds)), new BasicNameValuePair("restartRunningChannels", serializer.serialize(restartRunningChannels)), new BasicNameValuePair("clearStatistics", serializer.serialize(clearStatistics)) };
        serverConnection.executePostMethod(MESSAGE_SERVLET, params);
    }

    public Long getMaxMessageId(String channelId) throws ClientException {
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.MESSAGE_GET_MAX_ID.getName()), new BasicNameValuePair("channelId", channelId)};
        Long maxMessageId = null;
        
        try {
            maxMessageId = Long.parseLong(serverConnection.executePostMethodAsync(Client.MESSAGE_SERVLET, params));
        } catch (NumberFormatException e) {
            return null;
        }
        
        return maxMessageId;
    }
    
    public List<Message> getMessages(String channelId, MessageFilter filter, Boolean includeContent, Integer offset, Integer limit) throws ClientException {
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.MESSAGE_GET.getName()),
                new BasicNameValuePair("channelId", channelId),
                new BasicNameValuePair("filter", serializer.serialize(filter)),
                new BasicNameValuePair("includeContent", (includeContent) ? "y" : "n"),
                new BasicNameValuePair("offset", (offset == null) ? "" : offset.toString()),
                new BasicNameValuePair("limit", (limit == null) ? "" : limit.toString()) };

        return serializer.deserializeList(serverConnection.executePostMethod(Client.MESSAGE_SERVLET, params), Message.class);
    }
    
    public Long getMessageCount(String channelId, MessageFilter filter) throws ClientException {
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.MESSAGE_GET_COUNT.getName()), new BasicNameValuePair("channelId", channelId), new BasicNameValuePair("filter", serializer.serialize(filter)) };
        Long count = null;
        
        try {
            count = Long.parseLong(serverConnection.executePostMethod(Client.MESSAGE_SERVLET, params));
        } catch (NumberFormatException e) {
            return null;
        }
        
        return count;
    }
    
    public Message getMessageContent(String channelId, Long messageId) throws ClientException {
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.MESSAGE_GET_CONTENT.getName()), new BasicNameValuePair("channelId", channelId), new BasicNameValuePair("messageId", serializer.serialize(messageId)) };
        return serializer.deserialize(serverConnection.executePostMethodAsync(Client.MESSAGE_SERVLET, params), Message.class);
    }

    /**
     * Returns the channel status list.
     * 
     * @return
     * @throws ClientException
     */
    public List<DashboardStatus> getChannelStatusList() throws ClientException {
        logger.debug("retrieving channel status list");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_GET_STATUS_ALL.getName()) };
        return serializer.deserializeList(serverConnection.executePostMethodAsync(CHANNEL_STATUS_SERVLET, params), DashboardStatus.class);
    }

    /**
     * Returns a DashboardChannelInfo object containing a partial channel status list and a set of
     * remaining channel IDs. The block size specifies the maximum number of statuses to return.
     * 
     * @return
     * @throws ClientException
     */
    public DashboardChannelInfo getDashboardChannelInfo(int fetchSize) throws ClientException {
        logger.debug("retrieving channel status list: fetchSize=" + String.valueOf(fetchSize));
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_GET_STATUS_INITIAL.getName()), new BasicNameValuePair("fetchSize", String.valueOf(fetchSize)) };
        return serializer.deserialize(serverConnection.executePostMethodAsync(CHANNEL_STATUS_SERVLET, params), DashboardChannelInfo.class);
    }

    /**
     * Returns the channel status list for specific channel IDs.
     * 
     * @return
     * @throws ClientException
     */
    public List<DashboardStatus> getChannelStatusList(Set<String> channelIds) throws ClientException {
        logger.debug("retrieving channel status list: channelIds=" + String.valueOf(channelIds));
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CHANNEL_GET_STATUS.getName()), new BasicNameValuePair("channelIds", serializer.serialize(channelIds)) };
        return serializer.deserializeList(serverConnection.executePostMethodAsync(CHANNEL_STATUS_SERVLET, params), DashboardStatus.class);
    }

    /**
     * Returns the database driver list.
     * 
     * @return
     * @throws ClientException
     */
    public List<DriverInfo> getDatabaseDrivers() throws ClientException {
        logger.debug("retrieving database driver list");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CONFIGURATION_DATABASE_DRIVERS_GET.getName()) };
        return serializer.deserializeList(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params), DriverInfo.class);
    }

    /**
     * Returns the version of the Mirth server.
     * 
     * @return
     * @throws ClientException
     */
    public String getVersion() throws ClientException {
        logger.debug("retrieving version");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CONFIGURATION_VERSION_GET.getName()) };
        return serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
    }

    /**
     * Returns the status of the Mirth server.
     * 
     * @return
     * @throws ClientException
     */
    public synchronized int getStatus() throws ClientException {
        logger.debug("retrieving status");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CONFIGURATION_STATUS_GET.getName()) };
        return Integer.valueOf(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
    }

    /**
     * Returns the build date of the Mirth server.
     * 
     * @return
     * @throws ClientException
     */
    public String getBuildDate() throws ClientException {
        logger.debug("retrieving build date");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CONFIGURATION_BUILD_DATE_GET.getName()) };
        return serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
    }

    /**
     * Returns a Map of user prefereneces.
     * 
     * @return
     * @throws ClientException
     */
    public Properties getUserPreferences(User user) throws ClientException {
        logger.debug("retrieving user preferences");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.USER_PREFERENCES_GET.getName()), new BasicNameValuePair("user", serializer.serialize(user)) };
        return serializer.deserialize(serverConnection.executePostMethod(USER_SERVLET, params), Properties.class);
    }

    /**
     * Sets a user preference.
     * 
     * @return
     * @throws ClientException
     */
    public void setUserPreference(User user, String name, String value) throws ClientException {
        logger.debug("setting user preference");
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.USER_PREFERENCES_SET.getName()), new BasicNameValuePair("user", serializer.serialize(user)), new BasicNameValuePair("name", name), new BasicNameValuePair("value", value) };
        serverConnection.executePostMethod(USER_SERVLET, params);
    }

    public Attachment getAttachment(String channelId, String attachmentId) throws ClientException {
        logger.debug("getting Attachment: " + attachmentId);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.MESSAGE_ATTACHMENT_GET.getName()), new BasicNameValuePair("channelId", channelId), new BasicNameValuePair("attachmentId", attachmentId) };
        return serializer.deserialize(serverConnection.executePostMethodAsync(MESSAGE_SERVLET, params), Attachment.class);
    }

    public List<Attachment> getAttachmentsByMessageId(String channelId, Long messageId) throws ClientException {
        logger.debug("getting Attachments for message: " + messageId);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.MESSAGE_ATTACHMENT_GET_BY_MESSAGE_ID.getName()), new BasicNameValuePair("channelId", channelId), new BasicNameValuePair("messageId", serializer.serialize(messageId)) };
        return serializer.deserializeList(serverConnection.executePostMethodAsync(MESSAGE_SERVLET, params), Attachment.class);
    }

    public List<Attachment> getAttachmentIdsByMessageId(String channelId, Long messageId) throws ClientException {
        logger.debug("getting Attachments for message: " + messageId);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.MESSAGE_ATTACHMENT_GET_ID_BY_MESSAGE_ID.getName()), new BasicNameValuePair("channelId", channelId), new BasicNameValuePair("messageId", serializer.serialize(messageId)) };
        return serializer.deserializeList(serverConnection.executePostMethodAsync(MESSAGE_SERVLET, params), Attachment.class);
    }

    public String getDICOMMessage(ConnectorMessage message) throws ClientException {
        logger.debug("Getting DICOM message for message: " + message);
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.MESSAGE_DICOM_MESSAGE_GET.getName()), new BasicNameValuePair("message", serializer.serialize(message)) };
        return serverConnection.executePostMethodAsync(MESSAGE_SERVLET, params);
    }

    public UpdateClient getUpdateClient(User requestUser) {
        return new UpdateClient(this, requestUser);
    }

    public PasswordRequirements getPasswordRequirements() throws ClientException {
        NameValuePair[] params = { new BasicNameValuePair("op", Operations.CONFIGURATION_PASSWORD_REQUIREMENTS_GET.getName()) };
        return serializer.deserialize(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params), PasswordRequirements.class);
    }
}

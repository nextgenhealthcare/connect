/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.Provider;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipInputStream;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.log4j.Logger;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.commons.encryption.KeyEncryptor;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.model.Alert;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelStatistics;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.EncryptionSettings;
import com.mirth.connect.model.LoginStatus;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.User;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.EventFilter;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.util.export.MessageExportOptions;
import com.mirth.connect.util.export.MessageExporter;
import com.mirth.connect.util.export.MessageRetriever;

public class Client {
    private Logger logger = Logger.getLogger(this.getClass());
    private ObjectXMLSerializer serializer = new ObjectXMLSerializer();
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
            serverConnection.shutdownTimeoutThread();
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
        NameValuePair[] params = { new NameValuePair("op", Operations.USER_LOGIN.getName()), new NameValuePair("username", username), new NameValuePair("password", password), new NameValuePair("version", version) };
        return (LoginStatus) serializer.fromXML(serverConnection.executePostMethod(USER_SERVLET, params));
    }

    /**
     * Logs the user out of the server.
     * 
     * @throws ClientException
     */
    public synchronized void logout() throws ClientException {
        logger.debug("logging out");
        NameValuePair[] params = { new NameValuePair("op", Operations.USER_LOGOUT.getName()) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.CONFIGURATION_SERVER_ID_GET.getName()) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.CONFIGURATION_SERVER_TIMEZONE_GET.getName()) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.SERVER_CONFIGURATION_GET.getName()) };
        return (ServerConfiguration) serializer.fromXML(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
    }

    /**
     * Sets a ServerConfiguration object which sets all of the channels, alerts and properties stored on the Mirth server.
     * 
     * @return
     * @throws ClientException
     */
    public synchronized void setServerConfiguration(ServerConfiguration serverConfiguration) throws ClientException {
        logger.debug("setting server configuration");
        NameValuePair[] params = { new NameValuePair("op", Operations.SERVER_CONFIGURATION_SET.getName()), new NameValuePair("data", serializer.toXML(serverConfiguration)) };
        serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
    }

    /**
     * Returns a List of all channels.
     * 
     * @return
     * @throws ClientException
     */
    public List<Channel> getChannel(Channel channel) throws ClientException {
        logger.debug("getting channel");
        NameValuePair[] params = { new NameValuePair("op", Operations.CHANNEL_GET.getName()), new NameValuePair("channel", serializer.toXML(channel)) };
        return (List<Channel>) serializer.fromXML(serverConnection.executePostMethod(CHANNEL_SERVLET, params));
    }

    public List<ChannelSummary> getChannelSummary(Map<String, Integer> cachedChannels) throws ClientException {
        logger.debug("getting channel summary");
        NameValuePair[] params = { new NameValuePair("op", Operations.CHANNEL_GET_SUMMARY.getName()), new NameValuePair("cachedChannels", serializer.toXML(cachedChannels)) };
        return (List<ChannelSummary>) serializer.fromXML(serverConnection.executePostMethod(CHANNEL_SERVLET, params));
    }
    
    public Set<String> getChannelTags() throws ClientException {
        logger.debug("getting channel tags");
        NameValuePair[] params = { new NameValuePair("op", Operations.CHANNEL_GET_TAGS.getName()) };
        return (Set<String>) serializer.fromXML(serverConnection.executePostMethod(CHANNEL_SERVLET, params));
    }
    
    /**
     * Updates the specified channel.
     * 
     * @param channel
     * @throws ClientException
     */
    public synchronized boolean updateChannel(Channel channel, boolean override) throws ClientException {
        logger.debug("updating channel: channelId=" + channel.getId() + ", override=" + override);
        NameValuePair[] params = { new NameValuePair("op", Operations.CHANNEL_UPDATE.getName()), new NameValuePair("channel", serializer.toXML(channel)), new NameValuePair("override", new Boolean(override).toString()) };
        return Boolean.valueOf(serverConnection.executePostMethod(CHANNEL_SERVLET, params)).booleanValue();
    }

    /**
     * Removes the channel with the specified id.
     * 
     * @param channelId
     * @throws ClientException
     */
    public synchronized void removeChannel(Channel channel) throws ClientException {
        logger.debug("removing channel: channelId=" + channel.getId());
        NameValuePair[] params = { new NameValuePair("op", Operations.CHANNEL_REMOVE.getName()), new NameValuePair("channel", serializer.toXML(channel)) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.EXTENSION_INSTALL.getName()) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.EXTENSION_UNINSTALL.getName()), new NameValuePair("packageName", packageName) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.CONNECTOR_METADATA_GET.getName()) };
        return (Map<String, ConnectorMetaData>) serializer.fromXML(serverConnection.executePostMethod(EXTENSION_SERVLET, params));
    }

    /**
     * Returns a List of all plugins.
     * 
     * @return
     * @throws ClientException
     */
    public Map<String, PluginMetaData> getPluginMetaData() throws ClientException {
        logger.debug("retrieving plugin list");
        NameValuePair[] params = { new NameValuePair("op", Operations.PLUGIN_METADATA_GET.getName()) };
        return (Map<String, PluginMetaData>) serializer.fromXML(serverConnection.executePostMethod(EXTENSION_SERVLET, params));
    }

    /**
     * Sets an exension as enabled or disabled.
     * 
     * @return
     * @throws ClientException
     */
    public void setExtensionEnabled(String extensionName, boolean enabled) throws ClientException {
        logger.debug("setting extension enabled");
        NameValuePair[] params = { new NameValuePair("op", Operations.EXTENSION_SET_ENABLED.getName()), new NameValuePair("name", extensionName), new NameValuePair("enabled", BooleanUtils.toStringTrueFalse(enabled)) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.PLUGIN_SERVICE_INVOKE.getName()), new NameValuePair("name", pluginName), new NameValuePair("method", method), new NameValuePair("object", serializer.toXML(object)) };
        return serializer.fromXML(serverConnection.executePostMethod(EXTENSION_SERVLET, params));
    }

    /**
     * Invoke a method on a connector and pass back the Object returned
     * 
     * @return
     * @throws ClientException
     */
    public Object invokeConnectorService(String connectorName, String method, Object object) throws ClientException {
        logger.debug("invoking connector service " + method + " on " + connectorName);
        NameValuePair[] params = { new NameValuePair("op", Operations.CONNECTOR_SERVICE_INVOKE.getName()), new NameValuePair("name", connectorName), new NameValuePair("method", method), new NameValuePair("object", serializer.toXML(object)) };
        return serializer.fromXML(serverConnection.executePostMethod(EXTENSION_SERVLET, params));
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
        NameValuePair[] params = { new NameValuePair("op", Operations.CONFIGURATION_CHARSET_ENCODINGS_GET.getName()) };
        return (List<String>) serializer.fromXML(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
    }

    /**
     * Returns a List of all users.
     * 
     * @return
     * @throws ClientException
     */
    public List<User> getUser(User user) throws ClientException {
        logger.debug("getting user: " + user);
        NameValuePair[] params = { new NameValuePair("op", Operations.USER_GET.getName()), new NameValuePair("user", serializer.toXML(user)) };
        return (List<User>) serializer.fromXML(serverConnection.executePostMethod(USER_SERVLET, params));
    }

    /**
     * Updates a specified user.
     * 
     * @param user
     * @throws ClientException
     */
    public synchronized void updateUser(User user) throws ClientException {
        logger.debug("updating user: " + user);
        NameValuePair[] params = { new NameValuePair("op", Operations.USER_UPDATE.getName()), new NameValuePair("user", serializer.toXML(user)) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.USER_CHECK_OR_UPDATE_PASSWORD.getName()), new NameValuePair("user", serializer.toXML(user)), new NameValuePair("plainPassword", plainPassword) };
        return (List<String>) serializer.fromXML(serverConnection.executePostMethod(USER_SERVLET, params));
    }

    /**
     * Removes the user with the specified id.
     * 
     * @param userId
     * @throws ClientException
     */
    public synchronized void removeUser(User user) throws ClientException {
        logger.debug("removing user: " + user);
        NameValuePair[] params = { new NameValuePair("op", Operations.USER_REMOVE.getName()), new NameValuePair("user", serializer.toXML(user)) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.USER_IS_USER_LOGGED_IN.getName()), new NameValuePair("user", serializer.toXML(user)) };
        return Boolean.valueOf(serverConnection.executePostMethod(USER_SERVLET, params));
    }

    /**
     * Returns a List of all alerts.
     * 
     * @return
     * @throws ClientException
     */
    public List<Alert> getAlert(Alert alert) throws ClientException {
        logger.debug("getting alert: " + alert);
        NameValuePair[] params = { new NameValuePair("op", Operations.ALERT_GET.getName()), new NameValuePair("alert", serializer.toXML(alert)) };
        return (List<Alert>) serializer.fromXML(serverConnection.executePostMethod(ALERT_SERVLET, params));
    }

    /**
     * Updates a list of alerts.
     * 
     * @param alert
     * @throws ClientException
     */
    public synchronized void updateAlerts(List<Alert> alerts) throws ClientException {
        logger.debug("updating alerts: " + alerts);
        NameValuePair[] params = { new NameValuePair("op", Operations.ALERT_UPDATE.getName()), new NameValuePair("alerts", serializer.toXML(alerts)) };
        serverConnection.executePostMethod(ALERT_SERVLET, params);
    }

    /**
     * Removes the alert with the specified id.
     * 
     * @param alertId
     * @throws ClientException
     */
    public synchronized void removeAlert(Alert alert) throws ClientException {
        logger.debug("removing alert: " + alert);
        NameValuePair[] params = { new NameValuePair("op", Operations.ALERT_REMOVE.getName()), new NameValuePair("alert", serializer.toXML(alert)) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.CODE_TEMPLATE_GET.getName()), new NameValuePair("codeTemplate", serializer.toXML(codeTemplate)) };
        return (List<CodeTemplate>) serializer.fromXML(serverConnection.executePostMethod(TEMPLATE_SERVLET, params));
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
        NameValuePair[] params = { new NameValuePair("op", Operations.CODE_TEMPLATE_UPDATE.getName()), new NameValuePair("codeTemplates", serializer.toXML(codeTemplates)) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.CODE_TEMPLATE_REMOVE.getName()), new NameValuePair("codeTemplate", serializer.toXML(codeTemplate)) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.CONFIGURATION_SERVER_SETTINGS_GET.getName()) };
        return (ServerSettings) serializer.fromXML(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
    }

    /**
     * Returns an EncryptionSettings object with all encrpytion settings.
     * 
     * @return
     * @throws ClientException
     */
    public EncryptionSettings getEncryptionSettings() throws ClientException {
        logger.debug("retrieving encryption settings");
        NameValuePair[] params = { new NameValuePair("op", Operations.CONFIGURATION_ENCRYPTION_SETTINGS_GET.getName()) };
        return (EncryptionSettings) serializer.fromXML(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
    }

    /**
     * Updates the server configuration settings.
     * 
     * @param settings
     * @throws ClientException
     */
    public synchronized void setServerSettings(ServerSettings settings) throws ClientException {
        logger.debug("updating server settings");
        NameValuePair[] params = { new NameValuePair("op", Operations.CONFIGURATION_SERVER_SETTINGS_SET.getName()), new NameValuePair("data", serializer.toXML(settings)) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.CONFIGURATION_UPDATE_SETTINGS_GET.getName()) };
        return (UpdateSettings) serializer.fromXML(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
    }

    /**
     * Updates the update settings.
     * 
     * @param settings
     * @throws ClientException
     */
    public synchronized void setUpdateSettings(UpdateSettings settings) throws ClientException {
        logger.debug("updating update settings");
        NameValuePair[] params = { new NameValuePair("op", Operations.CONFIGURATION_UPDATE_SETTINGS_SET.getName()), new NameValuePair("data", serializer.toXML(settings)) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.CHANNEL_REDEPLOY.getName()) };
        serverConnection.executePostMethodChannel(ENGINE_SERVLET, params);
    }

    /**
     * Hot deploys specific channels.
     * 
     * @throws ClientException
     */
    public void deployChannels(List<String> channelIds) throws ClientException {
        logger.debug("deploying channels");
        NameValuePair[] params = { new NameValuePair("op", Operations.CHANNEL_DEPLOY.getName()), new NameValuePair("channelIds", serializer.toXML(channelIds)) };
        serverConnection.executePostMethodChannel(ENGINE_SERVLET, params);
    }

    /**
     * Undeploys specific channels.
     * 
     * @throws ClientException
     */
    public void undeployChannels(List<String> channelIds) throws ClientException {
        logger.debug("undeploying channels");
        NameValuePair[] params = { new NameValuePair("op", Operations.CHANNEL_UNDEPLOY.getName()), new NameValuePair("channelIds", serializer.toXML(channelIds)) };
        serverConnection.executePostMethodChannel(ENGINE_SERVLET, params);
    }

    /**
     * Starts the channel with the specified id.
     * 
     * @param channelId
     * @throws ClientException
     */
    public void startChannel(String channelId) throws ClientException {
        logger.debug("starting channel: channelId=" + channelId);
        NameValuePair[] params = { new NameValuePair("op", Operations.CHANNEL_START.getName()), new NameValuePair("id", channelId) };
        serverConnection.executePostMethodChannel(CHANNEL_STATUS_SERVLET, params);
    }

    /**
     * Stops the channel with the specified id.
     * 
     * @param channelId
     * @throws ClientException
     */
    public void stopChannel(String channelId) throws ClientException {
        logger.debug("stopping channel: channelId=" + channelId);
        NameValuePair[] params = { new NameValuePair("op", Operations.CHANNEL_STOP.getName()), new NameValuePair("id", channelId) };
        serverConnection.executePostMethodChannel(CHANNEL_STATUS_SERVLET, params);
    }
    
    /**
     * Halts the channel with the specified id.
     * 
     * @param channelId
     * @throws ClientException
     */
    public void haltChannel(String channelId) throws ClientException {
        logger.debug("halting channel: channelId=" + channelId);
        NameValuePair[] params = { new NameValuePair("op", Operations.CHANNEL_HALT.getName()), new NameValuePair("id", channelId) };
        serverConnection.executePostMethodAsync(CHANNEL_STATUS_SERVLET, params);
    }

    /**
     * Pauses the channel with the specified id.
     * 
     * @param channelId
     * @throws ClientException
     */
    public void pauseChannel(String channelId) throws ClientException {
        logger.debug("pausing channel: channelId=" + channelId);
        NameValuePair[] params = { new NameValuePair("op", Operations.CHANNEL_PAUSE.getName()), new NameValuePair("id", channelId) };
        serverConnection.executePostMethodChannel(CHANNEL_STATUS_SERVLET, params);
    }

    /**
     * Resumes the channel with the specified id.
     * 
     * @param channelId
     * @throws ClientException
     */
    public void resumeChannel(String channelId) throws ClientException {
        logger.debug("resuming channel: channelId=" + channelId);
        NameValuePair[] params = { new NameValuePair("op", Operations.CHANNEL_RESUME.getName()), new NameValuePair("id", channelId) };
        serverConnection.executePostMethodChannel(CHANNEL_STATUS_SERVLET, params);
    }

    /**
     * Returns the Statistics for the channel with the specified id.
     * 
     * @return
     * @throws ClientException
     */
    public ChannelStatistics getStatistics(String channelId) throws ClientException {
        logger.debug("retrieving channel statistics: channelId=" + channelId);
        NameValuePair[] params = { new NameValuePair("op", Operations.CHANNEL_STATS_GET.getName()), new NameValuePair("id", channelId) };
        return (ChannelStatistics) serializer.fromXML(serverConnection.executePostMethod(CHANNEL_STATISTICS_SERVLET, params));
    }

    /**
     * Clears the statistics for the given connectors and/or channels
     * 
     * @param channelConnectorMap
     *            Channel IDs mapped to lists of metaDataIds (connectors). If the metaDataId list is null, then all statistics for the channel will be cleared.
     * @return
     * @throws ClientException
     */
    public void clearStatistics(Map<String, List<Integer>> channelConnectorMap, boolean received, boolean filtered, boolean sent, boolean error, boolean alerted) throws ClientException {
        logger.debug("clearing statistics");
        NameValuePair[] params = { new NameValuePair("op", Operations.CHANNEL_STATS_CLEAR.getName()), new NameValuePair("channelConnectorMap", serializer.toXML(channelConnectorMap)), new NameValuePair("deleteReceived", new Boolean(received).toString()), new NameValuePair("deleteFiltered", new Boolean(filtered).toString()), new NameValuePair("deleteSent", new Boolean(sent).toString()), new NameValuePair("deleteErrored", new Boolean(error).toString()), new NameValuePair("deleteAlerted", new Boolean(alerted).toString()) };
        serverConnection.executePostMethod(CHANNEL_STATISTICS_SERVLET, params);
    }

    public void removeAllEvents() throws ClientException {
        logger.debug("removing all events");
        NameValuePair[] params = { new NameValuePair("op", Operations.EVENT_REMOVE_ALL.getName()) };
        serverConnection.executePostMethod(EVENT_SERVLET, params);
    }

    public String exportAllEvents() throws ClientException {
        logger.debug("exporting all events");
        NameValuePair[] params = { new NameValuePair("op", Operations.EVENT_EXPORT_ALL.getName()) };
        return (String) serverConnection.executePostMethod(EVENT_SERVLET, params);
    }

    public String exportAndRemoveAllEvents() throws ClientException {
        logger.debug("exporting and removing all events");
        NameValuePair[] params = { new NameValuePair("op", Operations.EVENT_EXPORT_AND_REMOVE_ALL.getName()) };
        return (String) serverConnection.executePostMethod(EVENT_SERVLET, params);
    }

    public void removeMessages(String channelId, MessageFilter filter) throws ClientException {
        logger.debug("removing messages");
        NameValuePair[] params = { new NameValuePair("op", Operations.MESSAGE_REMOVE.getName()), new NameValuePair("channelId", channelId), new NameValuePair("filter", serializer.toXML(filter)) };
        serverConnection.executePostMethod(MESSAGE_SERVLET, params);
    }

    public void reprocessMessages(String channelId, MessageFilter filter, boolean replace, List<Integer> reprocessMetaDataIds) throws ClientException {
        logger.debug("reprocessing messages");
        NameValuePair[] params = { new NameValuePair("op", Operations.MESSAGE_REPROCESS.getName()), new NameValuePair("channelId", channelId), new NameValuePair("filter", serializer.toXML(filter)), new NameValuePair("replace", String.valueOf(replace)), new NameValuePair("reprocessMetaDataIds", serializer.toXML(reprocessMetaDataIds)) };
        serverConnection.executePostMethodAsync(MESSAGE_SERVLET, params);
    }

    public void processMessage(String channelId, String rawMessage) throws ClientException {
        processMessage(channelId, rawMessage, null);
    }

    public void processMessage(String channelId, String rawMessage, List<Integer> metaDataIds) throws ClientException {
        logger.debug("processing message");
        NameValuePair[] params = { new NameValuePair("op", Operations.MESSAGE_PROCESS.getName()), new NameValuePair("channelId", channelId), new NameValuePair("message", rawMessage), new NameValuePair("metaDataIds", serializer.toXML(metaDataIds)) };
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
    
    public void encryptMessage(Message message) {
        Encryptor encryptor = getEncryptor();
        
        for (ConnectorMessage destinationMessage : message.getConnectorMessages().values()) {
            encryptConnectorMessage(destinationMessage, encryptor);
        }
    }

    private void encryptConnectorMessage(ConnectorMessage connectorMessage, Encryptor encryptor) {
        encryptMessageContent(connectorMessage.getRaw(), encryptor);
        encryptMessageContent(connectorMessage.getProcessedRaw(), encryptor);
        encryptMessageContent(connectorMessage.getTransformed(), encryptor);
        encryptMessageContent(connectorMessage.getEncoded(), encryptor);
        encryptMessageContent(connectorMessage.getSent(), encryptor);
        encryptMessageContent(connectorMessage.getResponse(), encryptor);
        encryptMessageContent(connectorMessage.getProcessedResponse(), encryptor);
    }

    private void encryptMessageContent(MessageContent content, Encryptor encryptor) {
        if (content != null) {
            String unencryptedContent = content.getContent();
            
            if (unencryptedContent != null) {
                content.setEncryptedContent(encryptor.encrypt(unencryptedContent));
                content.setContent(null);
            }
        }
    }
    
    public void decryptMessage(Message message) {
        Encryptor encryptor = getEncryptor();
        
        for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
            decryptConnectorMessage(connectorMessage, encryptor);
        }
    }

    private void decryptConnectorMessage(ConnectorMessage connectorMessage, Encryptor encryptor) {
        if (connectorMessage != null) {
            decryptMessageContent(connectorMessage.getRaw(), encryptor);
            decryptMessageContent(connectorMessage.getProcessedRaw(), encryptor);
            decryptMessageContent(connectorMessage.getTransformed(), encryptor);
            decryptMessageContent(connectorMessage.getEncoded(), encryptor);
            decryptMessageContent(connectorMessage.getSent(), encryptor);
            decryptMessageContent(connectorMessage.getResponse(), encryptor);
            decryptMessageContent(connectorMessage.getProcessedResponse(), encryptor);
        }
    }

    private void decryptMessageContent(MessageContent content, Encryptor encryptor) {
        if (content != null) {
            String encryptedContent = content.getEncryptedContent();
            
            if (encryptedContent != null) {
                content.setContent(encryptor.decrypt(encryptedContent));
            }
        }
    }
    
    public MessageImportResult importMessages(String channelId, File file, String charset) throws ClientException {
        final String openElement = "<message>";
        final String closeElement = "</message>";
        BufferedReader reader = null;
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        MessageImportResult result = new MessageImportResult();
        
        try {
            String fileNameLower = file.getName().toLowerCase();
            ZipInputStream zipInputStream = null;
            
            if (fileNameLower.substring(fileNameLower.length() - 4).equals(".zip")) {
                zipInputStream = new ZipInputStream(new FileInputStream(file));
                reader = new BufferedReader(new InputStreamReader(zipInputStream, charset));
            } else {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
            }
            
            String line;
            StringBuilder serializedMessage = new StringBuilder();
            boolean enteredMessage = false;

            do {
                while ((line = reader.readLine()) != null) {
                    if (line.equals(openElement)) {
                        enteredMessage = true;
                    }
                    
                    if (enteredMessage) {
                        serializedMessage.append(line);
                        
                        if (line.equals(closeElement)) {
                            Message message = (Message) serializer.fromXML(serializedMessage.toString());
                            
                            try {
                                decryptMessage(message);
                                importMessage(channelId, message);
                                result.addImported(1);
                            } catch (Exception e) {
                                result.addErrored(1);
                            }
                            
                            serializedMessage.delete(0, serializedMessage.length());
                            enteredMessage = false;
                        }
                    }
                }
            } while (zipInputStream != null && zipInputStream.getNextEntry() != null);
        } catch (Exception e) {
            throw new ClientException("Invalid message file. Stopping message import.", e);
        } finally {
            IOUtils.closeQuietly(reader);
        }

        return result;
     }

    public void importMessage(String channelId, Message message) throws ClientException {
        logger.debug("importing message");
        NameValuePair[] params = { new NameValuePair("op", Operations.MESSAGE_IMPORT.getName()), new NameValuePair("channelId", channelId), new NameValuePair("message", serializer.toXML(message)) };
        serverConnection.executePostMethodAsync(MESSAGE_SERVLET, params);
    }
    
    public int exportMessagesLocal(MessageExportOptions options) throws ClientException {
        final Client client = this;
        
        MessageExporter messageExporter = new MessageExporter();
        messageExporter.setOptions(options);
        messageExporter.setSerializer(serializer);
        messageExporter.setEncryptor(getEncryptor());
        messageExporter.setMessageRetriever(new MessageRetriever() {
            @Override
            public List<Message> getMessages(String channelId, MessageFilter filter, boolean includeContent, Integer offset, Integer limit) throws Exception {
                return client.getMessages(channelId, filter, includeContent, offset, limit);
            }
        });
        
        try {
            return messageExporter.export();
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }
    
    public int exportMessagesServer(MessageExportOptions options) throws ClientException {
        NameValuePair[] params = { new NameValuePair("op", Operations.MESSAGE_EXPORT.getName()), new NameValuePair("options", serializer.toXML(options)) };
        
        try {
            return Integer.parseInt(serverConnection.executePostMethod(Client.MESSAGE_SERVLET, params));
        } catch (NumberFormatException e) {
            logger.error(e);
            return 0;
        }
    }
    

    public Map<String, String> getGlobalScripts() throws ClientException {
        logger.debug("getting global scripts");
        NameValuePair[] params = { new NameValuePair("op", Operations.GLOBAL_SCRIPT_GET.getName()) };
        return (Map<String, String>) serializer.fromXML(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
    }

    public void setGlobalScripts(Map<String, String> scripts) throws ClientException {
        logger.debug("setting global scripts");
        NameValuePair[] params = { new NameValuePair("op", Operations.GLOBAL_SCRIPT_SET.getName()), new NameValuePair("scripts", serializer.toXML(scripts)) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.PLUGIN_PROPERTIES_GET.getName()), new NameValuePair("name", pluginName) };
        return (Properties) serializer.fromXML(serverConnection.executePostMethod(EXTENSION_SERVLET, params));
    }

    /**
     * Gets properties for a given plugin
     * 
     * @return
     * @throws ClientException
     */
    public void setPluginProperties(String pluginName, Properties properties) throws ClientException {
        logger.debug("setting " + pluginName + " properties");
        NameValuePair[] params = { new NameValuePair("op", Operations.PLUGIN_PROPERTIES_SET.getName()), new NameValuePair("name", pluginName), new NameValuePair("properties", serializer.toXML(properties)) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.EXTENSION_IS_ENABLED.getName()), new NameValuePair("name", extensionName) };
        return Boolean.valueOf(serverConnection.executePostMethod(EXTENSION_SERVLET, params)).booleanValue();
    }

    /**
     * Clears the message list for the channel with the specified id.
     * 
     * @param channelId
     * @throws ClientException
     */
    public boolean clearMessages(String channelId) throws ClientException {
        logger.debug("clearing messages: " + channelId);
        NameValuePair[] params = { new NameValuePair("op", Operations.MESSAGE_CLEAR.getName()), new NameValuePair("data", channelId) };
        return Boolean.valueOf(serverConnection.executePostMethod(MESSAGE_SERVLET, params));
    }

    // TODO: replace with calls to getMessages()
    @Deprecated
    public MessageListHandler getMessageListHandler(String channelId, MessageFilter filter, int pageSize, boolean newInstance) throws ClientException {
        return new MessageListHandler(channelId, filter, pageSize, (newInstance ? (System.currentTimeMillis() + "") : null), serverConnection);
    }

    public EventListHandler getEventListHandler(EventFilter filter, int pageSize, boolean newInstance) throws ClientException {
        return new EventListHandler(filter, pageSize, (newInstance ? (System.currentTimeMillis() + "") : null), serverConnection);
    }
    
    public Long getMaxMessageId(String channelId) throws ClientException {
        NameValuePair[] params = { new NameValuePair("op", Operations.GET_MAX_MESSAGE_ID.getName()), new NameValuePair("channelId", channelId)};
        Long maxMessageId = null;
        
        try {
            maxMessageId = Long.parseLong(serverConnection.executePostMethod(Client.MESSAGE_SERVLET, params));
        } catch (NumberFormatException e) {
            return null;
        }
        
        return maxMessageId;
    }
    
    public List<Message> getMessages(String channelId, MessageFilter filter, Boolean includeContent, Integer offset, Integer limit) throws ClientException {
        NameValuePair[] params = { new NameValuePair("op", Operations.GET_MESSAGES.getName()),
                new NameValuePair("channelId", channelId),
                new NameValuePair("filter", serializer.toXML(filter)),
                new NameValuePair("includeContent", (includeContent) ? "y" : "n"),
                new NameValuePair("offset", (offset == null) ? "" : offset.toString()),
                new NameValuePair("limit", (limit == null) ? "" : limit.toString()) };

        return (List<Message>) serializer.fromXML(serverConnection.executePostMethod(Client.MESSAGE_SERVLET, params));
    }
    
    public Long getMessageCount(String channelId, MessageFilter filter) throws ClientException {
        NameValuePair[] params = { new NameValuePair("op", Operations.GET_SEARCH_COUNT.getName()), new NameValuePair("channelId", channelId), new NameValuePair("filter", serializer.toXML(filter)) };
        Long count = null;
        
        try {
            count = Long.parseLong(serverConnection.executePostMethod(Client.MESSAGE_SERVLET, params));
        } catch (NumberFormatException e) {
            return null;
        }
        
        return count;
    }
    
    public Message getMessageContent(String channelId, Long messageId) throws ClientException {
        NameValuePair[] params = { new NameValuePair("op", Operations.GET_MESSAGE_CONTENT.getName()), new NameValuePair("channelId", channelId), new NameValuePair("messageId", serializer.toXML(messageId)) };
        return (Message) serializer.fromXML(serverConnection.executePostMethod(Client.MESSAGE_SERVLET, params));
    }

    /**
     * Returns the channel status list.
     * 
     * @return
     * @throws ClientException
     */
    public List<DashboardStatus> getChannelStatusList() throws ClientException {
        logger.debug("retrieving channel status list");
        NameValuePair[] params = { new NameValuePair("op", Operations.CHANNEL_GET_STATUS.getName()) };
        return (List<DashboardStatus>) serializer.fromXML(serverConnection.executePostMethod(CHANNEL_STATUS_SERVLET, params));
    }

    /**
     * Returns the database driver list.
     * 
     * @return
     * @throws ClientException
     */
    public List<DriverInfo> getDatabaseDrivers() throws ClientException {
        logger.debug("retrieving database driver list");
        NameValuePair[] params = { new NameValuePair("op", Operations.CONFIGURATION_DATABASE_DRIVERS_GET.getName()) };
        return (List<DriverInfo>) serializer.fromXML(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
    }

    /**
     * Returns the version of the Mirth server.
     * 
     * @return
     * @throws ClientException
     */
    public String getVersion() throws ClientException {
        logger.debug("retrieving version");
        NameValuePair[] params = { new NameValuePair("op", Operations.CONFIGURATION_VERSION_GET.getName()) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.CONFIGURATION_STATUS_GET.getName()) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.CONFIGURATION_BUILD_DATE_GET.getName()) };
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
        NameValuePair[] params = { new NameValuePair("op", Operations.USER_PREFERENCES_GET.getName()), new NameValuePair("user", serializer.toXML(user)) };
        return (Properties) serializer.fromXML(serverConnection.executePostMethod(USER_SERVLET, params));
    }

    /**
     * Sets a user preference.
     * 
     * @return
     * @throws ClientException
     */
    public void setUserPreference(User user, String name, String value) throws ClientException {
        logger.debug("setting user preference");
        NameValuePair[] params = { new NameValuePair("op", Operations.USER_PREFERENCES_SET.getName()), new NameValuePair("user", serializer.toXML(user)), new NameValuePair("name", name), new NameValuePair("value", value) };
        serverConnection.executePostMethod(USER_SERVLET, params);
    }

    public Attachment getAttachment(String channelId, String attachmentId) throws ClientException {
        logger.debug("getting Attachment: " + attachmentId);
        NameValuePair[] params = { new NameValuePair("op", Operations.MESSAGE_ATTACHMENT_GET.getName()), new NameValuePair("channelId", channelId), new NameValuePair("attachmentId", attachmentId) };
        return (Attachment) serializer.fromXML(serverConnection.executePostMethod(MESSAGE_SERVLET, params));
    }

    public List<Attachment> getAttachmentsByMessageId(String channelId, Long messageId) throws ClientException {
        logger.debug("getting Attachments for message: " + messageId);
        NameValuePair[] params = { new NameValuePair("op", Operations.MESSAGE_ATTACHMENT_GET_BY_MESSAGE_ID.getName()), new NameValuePair("channelId", channelId), new NameValuePair("messageId", serializer.toXML(messageId)) };
        return (List<Attachment>) serializer.fromXML(serverConnection.executePostMethod(MESSAGE_SERVLET, params));
    }

    public List<Attachment> getAttachmentIdsByMessageId(String channelId, Long messageId) throws ClientException {
        logger.debug("getting Attachments for message: " + messageId);
        NameValuePair[] params = { new NameValuePair("op", Operations.MESSAGE_ATTACHMENT_GET_ID_BY_MESSAGE_ID.getName()), new NameValuePair("channelId", channelId), new NameValuePair("messageId", serializer.toXML(messageId)) };
        return (List<Attachment>) serializer.fromXML(serverConnection.executePostMethod(MESSAGE_SERVLET, params));
    }

    public String getDICOMMessage(ConnectorMessage message) throws ClientException {
        logger.debug("Getting DICOM message for message: " + message);
        NameValuePair[] params = { new NameValuePair("op", Operations.MESSAGE_DICOM_MESSAGE_GET.getName()), new NameValuePair("message", serializer.toXML(message)) };
        return serverConnection.executePostMethod(MESSAGE_SERVLET, params);
    }

    public UpdateClient getUpdateClient(User requestUser) {
        return new UpdateClient(this, requestUser);
    }

    public PasswordRequirements getPasswordRequirements() throws ClientException {
        NameValuePair[] params = { new NameValuePair("op", Operations.CONFIGURATION_PASSWORD_REQUIREMENTS_GET.getName()) };
        return (PasswordRequirements) serializer.fromXML(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
    }
}

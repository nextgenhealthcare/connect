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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.Provider;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.reflections.Reflections;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.commons.encryption.KeyEncryptor;
import com.mirth.connect.client.core.Operation.ExecuteType;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.providers.MetaDataSearchParamConverterProvider.MetaDataSearch;
import com.mirth.connect.client.core.api.servlets.AlertServletInterface;
import com.mirth.connect.client.core.api.servlets.ChannelGroupServletInterface;
import com.mirth.connect.client.core.api.servlets.ChannelServletInterface;
import com.mirth.connect.client.core.api.servlets.ChannelStatisticsServletInterface;
import com.mirth.connect.client.core.api.servlets.ChannelStatusServletInterface;
import com.mirth.connect.client.core.api.servlets.CodeTemplateServletInterface;
import com.mirth.connect.client.core.api.servlets.ConfigurationServletInterface;
import com.mirth.connect.client.core.api.servlets.DatabaseTaskServletInterface;
import com.mirth.connect.client.core.api.servlets.EngineServletInterface;
import com.mirth.connect.client.core.api.servlets.EventServletInterface;
import com.mirth.connect.client.core.api.servlets.ExtensionServletInterface;
import com.mirth.connect.client.core.api.servlets.MessageServletInterface;
import com.mirth.connect.client.core.api.servlets.UsageServletInterface;
import com.mirth.connect.client.core.api.servlets.UserServletInterface;
import com.mirth.connect.client.core.api.util.OperationUtil;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelDependency;
import com.mirth.connect.model.ChannelGroup;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.ChannelMetadata;
import com.mirth.connect.model.ChannelStatistics;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.DashboardChannelInfo;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.DatabaseTask;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.EncryptionSettings;
import com.mirth.connect.model.LoginStatus;
import com.mirth.connect.model.MessageImportResult;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.ResourceProperties;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.ServerEvent.Level;
import com.mirth.connect.model.ServerEvent.Outcome;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.User;
import com.mirth.connect.model.alert.AlertInfo;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.alert.AlertStatus;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrarySaveResult;
import com.mirth.connect.model.codetemplates.CodeTemplateSummary;
import com.mirth.connect.model.filters.EventFilter;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.util.ConfigurationProperty;
import com.mirth.connect.util.ConnectionTestResponse;
import com.mirth.connect.util.MirthSSLUtil;
import com.mirth.connect.util.messagewriter.EncryptionType;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class Client implements UserServletInterface, ConfigurationServletInterface, ChannelServletInterface, ChannelGroupServletInterface, ChannelStatusServletInterface, ChannelStatisticsServletInterface, EngineServletInterface, MessageServletInterface, EventServletInterface, AlertServletInterface, CodeTemplateServletInterface, DatabaseTaskServletInterface, UsageServletInterface, ExtensionServletInterface {

    public static final int MAX_QUERY_PARAM_COLLECTION_SIZE = 100;

    private Logger logger = Logger.getLogger(this.getClass());
    private ServerConnection serverConnection;
    private javax.ws.rs.client.Client client;
    private URI api;
    private AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Instantiates a new Mirth client with a connection to the specified server.
     */
    public Client(String address) throws URISyntaxException {
        // Default timeout is infinite.
        this(address, 0, MirthSSLUtil.DEFAULT_HTTPS_CLIENT_PROTOCOLS, MirthSSLUtil.DEFAULT_HTTPS_CIPHER_SUITES, null);
    }

    public Client(String address, String[] httpsProtocols, String[] httpsCipherSuites) throws URISyntaxException {
        // Default timeout is infinite.
        this(address, 0, httpsProtocols, httpsCipherSuites, null);
    }

    public Client(String address, String[] httpsProtocols, String[] httpsCipherSuites, String[] apiProviderClasses) throws URISyntaxException {
        // Default timeout is infinite.
        this(address, 0, httpsProtocols, httpsCipherSuites, apiProviderClasses);
    }

    public Client(String address, int timeout, String[] httpsProtocols, String[] httpsCipherSuites) throws URISyntaxException {
        this(address, timeout, httpsProtocols, httpsCipherSuites, null);
    }

    public Client(String address, int timeout, String[] httpsProtocols, String[] httpsCipherSuites, String[] apiProviderClasses) throws URISyntaxException {
        if (!address.endsWith("/")) {
            address += "/";
        }
        URI addressURI = new URI(address);

        serverConnection = new ServerConnection(timeout, httpsProtocols, httpsCipherSuites, StringUtils.equalsIgnoreCase(addressURI.getScheme(), "http"));

        ClientConfig config = new ClientConfig().connectorProvider(new ConnectorProvider() {
            @Override
            public Connector getConnector(javax.ws.rs.client.Client client, Configuration runtimeConfig) {
                return serverConnection;
            }
        });

        // Register providers
        for (Class<?> providerClass : new Reflections("com.mirth.connect.client.core.api.providers").getTypesAnnotatedWith(javax.ws.rs.ext.Provider.class)) {
            config.register(providerClass);
        }
        config.register(MultiPartFeature.class);

        // Register servlet interfaces
        Set<Class<? extends BaseServletInterface>> servletClasses = new Reflections("com.mirth.connect.client.core.api.servlets").getSubTypesOf(BaseServletInterface.class);
        for (Class<?> servletClass : servletClasses) {
            config.register(servletClass);
        }

        if (ArrayUtils.isNotEmpty(apiProviderClasses)) {
            for (String apiProviderClass : apiProviderClasses) {
                try {
                    config.register(Class.forName(apiProviderClass));
                } catch (Throwable t) {
                    logger.error("Error registering API provider class: " + apiProviderClass);
                }
            }
        }

        client = ClientBuilder.newClient(config);
        api = addressURI.resolve("api/" + Version.getLatest().toString());
    }

    /**
     * Allows registration of extension providers after the client is initialized.
     */
    public void registerApiProviders(Set<String> packageNames, Set<String> classes) {
        if (CollectionUtils.isNotEmpty(packageNames)) {
            for (String packageName : packageNames) {
                try {
                    for (Class<?> clazz : new Reflections(packageName).getTypesAnnotatedWith(javax.ws.rs.ext.Provider.class)) {
                        client.register(clazz);
                    }
                    for (Class<?> clazz : new Reflections(packageName).getTypesAnnotatedWith(Path.class)) {
                        client.register(clazz);
                    }
                } catch (Throwable t) {
                    logger.error("Error registering API provider package: " + packageName);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(classes)) {
            for (String clazz : classes) {
                try {
                    client.register(Class.forName(clazz));
                } catch (Throwable t) {
                    logger.error("Error registering API provider class: " + clazz);
                }
            }
        }
    }

    public <T> T getServlet(Class<T> servletInterface) {
        return getServlet(servletInterface, null);
    }

    public <T> T getServlet(Class<T> servletInterface, ExecuteType executeType) {
        return getServlet(servletInterface, executeType, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T getServlet(final Class<T> servletInterface, final ExecuteType executeType, final Map<String, List<String>> customHeaders) {
        return (T) Proxy.newProxyInstance(AccessController.doPrivileged(ReflectionHelper.getClassLoaderPA(servletInterface)), new Class[] {
                servletInterface }, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws ClientException {
                        try {
                            WebTarget target = client.target(api);

                            Operation operation = OperationUtil.getOperation(servletInterface, method);
                            if (operation != null) {
                                target.property(ServerConnection.OPERATION_PROPERTY, operation);
                            }

                            if (executeType != null) {
                                target.property(ServerConnection.EXECUTE_TYPE_PROPERTY, executeType);
                            }

                            if (customHeaders != null) {
                                target.property(ServerConnection.CUSTOM_HEADERS_PROPERTY, customHeaders);
                            }

                            if (args == null && method.getName().equals("toString")) {
                                return target.toString();
                            }

                            T resource = WebResourceFactory.newResource(servletInterface, target);
                            Object result = method.invoke(resource, args);

                            // Make sure to return the right type
                            if (result == null && method.getReturnType().isPrimitive()) {
                                return method.getReturnType() == boolean.class ? false : (byte) 0x00;
                            }
                            return result;
                        } catch (Throwable t) {
                            Throwable cause = t;
                            if (cause instanceof InvocationTargetException && cause.getCause() != null) {
                                cause = cause.getCause();
                            }
                            if (cause instanceof ProcessingException && cause.getCause() != null) {
                                cause = cause.getCause();
                            }
                            if (cause instanceof ClientException) {
                                throw (ClientException) cause;
                            } else {
                                throw new ClientException(cause);
                            }
                        }
                    }
                });
    }

    public ServerConnection getServerConnection() {
        return serverConnection;
    }

    public void close() {
        closed.set(true);
        if (serverConnection != null) {
            serverConnection.shutdown();
            client.close();
        }
    }

    public boolean isClosed() {
        return closed.get();
    }

    /****************
     * User Servlet *
     ****************/

    /**
     * Logs in to the Mirth Connect server using the specified name and password.
     * 
     * @see UserServletInterface#login
     */
    @Override
    public synchronized LoginStatus login(String username, String password) throws ClientException {
        return getServlet(UserServletInterface.class).login(username, password);
    }

    /**
     * Logs out of the server.
     * 
     * @see UserServletInterface#logout
     */
    @Override
    public synchronized void logout() throws ClientException {
        getServlet(UserServletInterface.class).logout();
    }

    /**
     * Creates a new user.
     * 
     * @see UserServletInterface#createUser
     */
    @Override
    public synchronized void createUser(User user) throws ClientException {
        getServlet(UserServletInterface.class).createUser(user);
    }

    /**
     * Returns a List of all users.
     * 
     * @see UserServletInterface#getAllUsers
     */
    @Override
    public List<User> getAllUsers() throws ClientException {
        return getServlet(UserServletInterface.class).getAllUsers();
    }

    /**
     * Returns a specific user by ID.
     * 
     * @see UserServletInterface#getUser
     */
    public User getUser(Integer userId) throws ClientException {
        return getUser(String.valueOf(userId));
    }

    /**
     * Returns a specific user by ID or username.
     * 
     * @see UserServletInterface#getUser
     */
    @Override
    public User getUser(String userIdOrName) throws ClientException {
        return getServlet(UserServletInterface.class).getUser(userIdOrName);
    }

    /**
     * Returns the current logged in user.
     * 
     * @see UserServletInterface#getCurrentUser
     */
    @Override
    public User getCurrentUser() throws ClientException {
        return getServlet(UserServletInterface.class).getCurrentUser();
    }

    /**
     * Updates a specified user.
     * 
     * @see UserServletInterface#updateUser
     */
    public synchronized void updateUser(User user) throws ClientException {
        updateUser(user.getId(), user);
    }

    /**
     * Updates a specified user.
     * 
     * @see UserServletInterface#updateUser
     */
    @Override
    public synchronized void updateUser(Integer userId, User user) throws ClientException {
        getServlet(UserServletInterface.class).updateUser(user.getId(), user);
    }

    /**
     * Checks the password against the configured password policies.
     * 
     * @see UserServletInterface#checkUserPassword
     * 
     * @return A list of errors that occurred with the password
     */
    @Override
    public synchronized List<String> checkUserPassword(String plainPassword) throws ClientException {
        return getServlet(UserServletInterface.class).checkUserPassword(plainPassword);
    }

    /**
     * Updates a user's password.
     * 
     * @see UserServletInterface#updateUserPassword
     * 
     * @return A list of errors that occurred with the password
     */
    @Override
    public synchronized List<String> updateUserPassword(Integer userId, String plainPassword) throws ClientException {
        return getServlet(UserServletInterface.class).updateUserPassword(userId, plainPassword);
    }

    /**
     * Removes a specific user.
     * 
     * @see UserServletInterface#removeUser
     */
    @Override
    public synchronized void removeUser(Integer userId) throws ClientException {
        getServlet(UserServletInterface.class).removeUser(userId);
    }

    /**
     * Returns a true if the specified user is logged in to the server.
     * 
     * @see UserServletInterface#isUserLoggedIn
     */
    @Override
    public boolean isUserLoggedIn(Integer userId) throws ClientException {
        return getServlet(UserServletInterface.class).isUserLoggedIn(userId);
    }

    /**
     * Returns a Map of user preferences, optionally filtered by a set of property names.
     * 
     * @see UserServletInterface#getUserPreferences
     */
    @Override
    public Properties getUserPreferences(Integer userId, Set<String> names) throws ClientException {
        return getServlet(UserServletInterface.class).getUserPreferences(userId, names);
    }

    /**
     * Returns a specific user preference.
     * 
     * @see UserServletInterface#getUserPreference
     */
    @Override
    public String getUserPreference(Integer userId, String name) throws ClientException {
        return getServlet(UserServletInterface.class).getUserPreference(userId, name);
    }

    /**
     * Updates multiple user preferences.
     * 
     * @see UserServletInterface#setUserPreferences
     */
    @Override
    public void setUserPreferences(Integer userId, Properties properties) throws ClientException {
        getServlet(UserServletInterface.class).setUserPreferences(userId, properties);
    }

    /**
     * Updates a user preference.
     * 
     * @see UserServletInterface#setUserPreference
     */
    @Override
    public void setUserPreference(Integer userId, String name, String value) throws ClientException {
        getServlet(UserServletInterface.class).setUserPreference(userId, name, value);
    }

    /*************************
     * Configuration Servlet *
     *************************/

    /**
     * Returns the server id.
     * 
     * @see ConfigurationServletInterface#getServerId
     */
    @Override
    public String getServerId() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getServerId();
    }

    /**
     * Returns the version of the Mirth Connect server.
     * 
     * @see ConfigurationServletInterface#getVersion
     */
    @Override
    public String getVersion() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getVersion();
    }

    /**
     * Returns the build date of the Mirth Connect server.
     * 
     * @see ConfigurationServletInterface#getBuildDate
     */
    @Override
    public String getBuildDate() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getBuildDate();
    }

    /**
     * Returns the status of the Mirth Connect server.
     * 
     * @see ConfigurationServletInterface#getStatus
     */
    @Override
    public synchronized int getStatus() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getStatus();
    }

    /**
     * Returns the time zone of the server.
     * 
     * @see ConfigurationServletInterface#getServerTimezone
     */
    @Override
    public String getServerTimezone() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getServerTimezone();
    }

    /**
     * Returns the time of the server.
     * 
     * @see ConfigurationServletInterface#getServerTime
     */
    @Override
    public Calendar getServerTime() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getServerTime();
    }

    /**
     * Returns the name of the JVM running Mirth Connect.
     * 
     * @see ConfigurationServletInterface#getJVMName
     */
    @Override
    public String getJVMName() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getJVMName();
    }

    /**
     * Returns a map of common information about the Mirth Connect server.
     * 
     * @see ConfigurationServletInterface#getAbout
     */
    @Override
    public Map<String, Object> getAbout() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getAbout();
    }

    /**
     * Returns a ServerConfiguration object which contains all of the channels, users, alerts and
     * properties stored on the Mirth Connect server.
     * 
     * @see ConfigurationServletInterface#getServerConfiguration
     */
    public ServerConfiguration getServerConfiguration() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getServerConfiguration(null, false);
    }

    /**
     * Returns a ServerConfiguration object which contains all of the channels, users, alerts and
     * properties stored on the Mirth Connect server.
     * 
     * @see ConfigurationServletInterface#getServerConfiguration
     */
    @Override
    public ServerConfiguration getServerConfiguration(DeployedState initialState, boolean pollingOnly) throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getServerConfiguration(initialState, pollingOnly);
    }

    /**
     * Updates all of the channels, alerts and properties stored on the Mirth Connect server.
     * 
     * @see ConfigurationServletInterface#setServerConfiguration
     */
    @Override
    public synchronized void setServerConfiguration(ServerConfiguration serverConfiguration, boolean deploy) throws ClientException {
        getServlet(ConfigurationServletInterface.class).setServerConfiguration(serverConfiguration, deploy);
    }

    /**
     * Returns a List of all of the charset encodings supported by the server.
     * 
     * @see ConfigurationServletInterface#getAvailableCharsetEncodings
     */
    @Override
    public List<String> getAvailableCharsetEncodings() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getAvailableCharsetEncodings();
    }

    /**
     * Returns a ServerSettings object with all server settings.
     * 
     * @see ConfigurationServletInterface#getServerSettings
     */
    @Override
    public ServerSettings getServerSettings() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getServerSettings();
    }

    /**
     * Updates the server configuration settings.
     * 
     * @see ConfigurationServletInterface#setServerSettings
     */
    @Override
    public synchronized void setServerSettings(ServerSettings settings) throws ClientException {
        getServlet(ConfigurationServletInterface.class).setServerSettings(settings);
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
            logger.error("Unable to load encryption settings.", e);
        }

        return false;
    }

    /**
     * Returns an EncryptionSettings object with all encryption settings.
     * 
     * @see ConfigurationServletInterface#getEncryptionSettings
     */
    @Override
    public EncryptionSettings getEncryptionSettings() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getEncryptionSettings();
    }

    /**
     * Sends a test email.
     * 
     * @see ConfigurationServletInterface#sendTestEmail
     */
    @Override
    public synchronized ConnectionTestResponse sendTestEmail(Properties properties) throws ClientException {
        return getServlet(ConfigurationServletInterface.class).sendTestEmail(properties);
    }

    /**
     * Returns an UpdateSettings object with all update settings.
     * 
     * @see ConfigurationServletInterface#getUpdateSettings
     */
    @Override
    public UpdateSettings getUpdateSettings() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getUpdateSettings();
    }

    /**
     * Updates the update settings.
     * 
     * @see ConfigurationServletInterface#setUpdateSettings
     */
    @Override
    public synchronized void setUpdateSettings(UpdateSettings settings) throws ClientException {
        getServlet(ConfigurationServletInterface.class).setUpdateSettings(settings);
    }

    /**
     * Returns a globally unique id.
     * 
     * @see ConfigurationServletInterface#getGuid
     */
    @Override
    public String getGuid() throws ClientException {
        return UUID.randomUUID().toString();
    }

    /**
     * Returns a map containing all of the global scripts.
     * 
     * @see ConfigurationServletInterface#getGlobalScripts
     */
    @Override
    public Map<String, String> getGlobalScripts() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getGlobalScripts();
    }

    /**
     * Updates all of the global scripts.
     * 
     * @see ConfigurationServletInterface#setGlobalScripts
     */
    @Override
    public void setGlobalScripts(Map<String, String> scripts) throws ClientException {
        getServlet(ConfigurationServletInterface.class).setGlobalScripts(scripts);
    }

    /**
     * Returns all entries in the configuration map.
     * 
     * @see ConfigurationServletInterface#getConfigurationMap
     */
    @Override
    public Map<String, ConfigurationProperty> getConfigurationMap() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getConfigurationMap();
    }

    /**
     * Updates all entries in the configuration map.
     * 
     * @see ConfigurationServletInterface#setConfigurationMap
     */
    @Override
    public void setConfigurationMap(Map<String, ConfigurationProperty> map) throws ClientException {
        getServlet(ConfigurationServletInterface.class).setConfigurationMap(map);
    }

    /**
     * Returns the database driver list.
     * 
     * @see ConfigurationServletInterface#getDatabaseDrivers
     */
    @Override
    public List<DriverInfo> getDatabaseDrivers() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getDatabaseDrivers();
    }

    /**
     * Returns all password requirements for the server.
     * 
     * @see ConfigurationServletInterface#getPasswordRequirements
     */
    @Override
    public PasswordRequirements getPasswordRequirements() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getPasswordRequirements();
    }

    /**
     * Returns all resources for the server.
     * 
     * @see ConfigurationServletInterface#getResources
     */
    @Override
    public List<ResourceProperties> getResources() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getResources();
    }

    /**
     * Updates all resources for the server.
     * 
     * @see ConfigurationServletInterface#setResources
     */
    @Override
    public void setResources(List<ResourceProperties> resources) throws ClientException {
        getServlet(ConfigurationServletInterface.class).setResources(resources);
    }

    /**
     * Reloads a resource and all libraries associated with it.
     * 
     * @see ConfigurationServletInterface#reloadResource
     */
    @Override
    public void reloadResource(String resourceId) throws ClientException {
        getServlet(ConfigurationServletInterface.class).reloadResource(resourceId);
    }

    /**
     * Returns all channel dependencies for the server.
     * 
     * @see ConfigurationServletInterface#getChannelDependencies
     */
    @Override
    public Set<ChannelDependency> getChannelDependencies() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getChannelDependencies();
    }

    /**
     * Updates all channel dependencies for the server.
     * 
     * @see ConfigurationServletInterface#setChannelDependencies
     */
    @Override
    public void setChannelDependencies(Set<ChannelDependency> dependencies) throws ClientException {
        getServlet(ConfigurationServletInterface.class).setChannelDependencies(dependencies);
    }

    /**
     * Returns all channel metadata for the server.
     * 
     * @see ConfigurationServletInterface#getChannelMetadata
     */
    @Override
    public Map<String, ChannelMetadata> getChannelMetadata() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getChannelMetadata();
    }

    /**
     * Updates all channel metadata for the server.
     * 
     * @see ConfigurationServletInterface#setChannelMetadata
     */
    @Override
    public void setChannelMetadata(Map<String, ChannelMetadata> metadata) throws ClientException {
        getServlet(ConfigurationServletInterface.class).setChannelMetadata(metadata);
    }

    /**
     * Returns a map containing all supported and enabled TLS protocols and cipher suites.
     * 
     * @see ConfigurationServletInterface#getProtocolsAndCipherSuites
     */
    @Override
    public Map<String, String[]> getProtocolsAndCipherSuites() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getProtocolsAndCipherSuites();
    }

    /**
     * Returns a set containing all channel tags for the server.
     * 
     * @see ConfigurationServletInterface#getChannelTags
     */
    @Override
    public Set<ChannelTag> getChannelTags() throws ClientException {
        return getServlet(ConfigurationServletInterface.class).getChannelTags();
    }

    /**
     * Updates all channel tags.
     * 
     * @see ConfigurationServletInterface#updateChannelTags
     */
    @Override
    public void setChannelTags(Set<ChannelTag> channelTags) throws ClientException {
        getServlet(ConfigurationServletInterface.class).setChannelTags(channelTags);
    }

    /*******************
     * Channel Servlet *
     *******************/

    /**
     * Creates a new channel.
     * 
     * @see ChannelServletInterface#createChannel
     */
    @Override
    public synchronized boolean createChannel(Channel channel) throws ClientException {
        return getServlet(ChannelServletInterface.class).createChannel(channel);
    }

    /**
     * Returns a List of all channels.
     * 
     * @see ChannelServletInterface#getAllChannels
     */
    public List<Channel> getAllChannels() throws ClientException {
        return getServlet(ChannelServletInterface.class).getChannels(null, false);
    }

    /**
     * Retrieve multiple channels by ID.
     * 
     * @see ChannelServletInterface#getChannels
     */
    public List<Channel> getChannels(Set<String> channelIds) throws ClientException {
        return getServlet(ChannelServletInterface.class).getChannels(channelIds, false);
    }

    /**
     * Retrieve multiple channels by ID.
     * 
     * @see ChannelServletInterface#getChannels
     */
    @Override
    public List<Channel> getChannels(Set<String> channelIds, boolean pollingOnly) throws ClientException {
        if (CollectionUtils.size(channelIds) > MAX_QUERY_PARAM_COLLECTION_SIZE) {
            return getChannelsPost(channelIds, pollingOnly);
        }
        return getServlet(ChannelServletInterface.class).getChannels(channelIds, pollingOnly);
    }

    /**
     * Retrieve multiple channels by ID. This is a POST request alternative to GET /channels that
     * may be used when there are too many channel IDs to include in the query parameters.
     * 
     * @see ChannelServletInterface#getChannels
     */
    @Override
    public List<Channel> getChannelsPost(Set<String> channelIds, boolean pollingOnly) throws ClientException {
        return getServlet(ChannelServletInterface.class).getChannelsPost(channelIds, pollingOnly);
    }

    /**
     * Retrieve a single channel by ID.
     * 
     * @see ChannelServletInterface#getChannel
     */
    @Override
    public Channel getChannel(String channelId) throws ClientException {
        return getServlet(ChannelServletInterface.class).getChannel(channelId);
    }

    /**
     * Returns all connector names for a channel.
     * 
     * @see ChannelServletInterface#getConnectorNames
     */
    @Override
    public Map<Integer, String> getConnectorNames(String channelId) throws ClientException {
        return getServlet(ChannelServletInterface.class).getConnectorNames(channelId);
    }

    /**
     * Returns all metadata columns for a channel.
     * 
     * @see ChannelServletInterface#getMetaDataColumns
     */
    @Override
    public List<MetaDataColumn> getMetaDataColumns(String channelId) throws ClientException {
        return getServlet(ChannelServletInterface.class).getMetaDataColumns(channelId);
    }

    /**
     * Returns a list of channel summaries, indicating to a client which channels have changed (been
     * updated, deleted, undeployed, etc.). If a channel was modified, the entire Channel object
     * will be returned.
     * 
     * @see ChannelServletInterface#getChannelSummary
     */
    @Override
    public List<ChannelSummary> getChannelSummary(Map<String, ChannelHeader> cachedChannels, boolean ignoreNewChannels) throws ClientException {
        return getServlet(ChannelServletInterface.class).getChannelSummary(cachedChannels, ignoreNewChannels);
    }

    /**
     * Enables/disables the specified channels.
     * 
     * @see ChannelServletInterface#setChannelEnabled
     */
    @Override
    public synchronized void setChannelEnabled(Set<String> channelIds, boolean enabled) throws ClientException {
        getServlet(ChannelServletInterface.class).setChannelEnabled(channelIds, enabled);
    }

    /**
     * Enables/disables the specified channel.
     * 
     * @see ChannelServletInterface#setChannelEnabled
     */
    @Override
    public synchronized void setChannelEnabled(String channelId, boolean enabled) throws ClientException {
        getServlet(ChannelServletInterface.class).setChannelEnabled(channelId, enabled);
    }

    /**
     * Sets the initial state for the specified channels.
     * 
     * @see ChannelServletInterface#setChannelInitialState
     */
    @Override
    public synchronized void setChannelInitialState(Set<String> channelIds, DeployedState initialState) throws ClientException {
        getServlet(ChannelServletInterface.class).setChannelInitialState(channelIds, initialState);
    }

    /**
     * Sets the initial state for a single channel.
     * 
     * @see ChannelServletInterface#setChannelInitialState
     */
    @Override
    public synchronized void setChannelInitialState(String channelId, DeployedState initialState) throws ClientException {
        getServlet(ChannelServletInterface.class).setChannelInitialState(channelId, initialState);
    }

    /**
     * Updates the specified channel.
     * 
     * @see ChannelServletInterface#updateChannel
     */
    public synchronized boolean updateChannel(Channel channel, boolean override) throws ClientException {
        return updateChannel(channel.getId(), channel, override);
    }

    /**
     * Updates the specified channel.
     * 
     * @see ChannelServletInterface#updateChannel
     */
    @Override
    public synchronized boolean updateChannel(String channelId, Channel channel, boolean override) throws ClientException {
        return getServlet(ChannelServletInterface.class).updateChannel(channelId, channel, override);
    }

    /**
     * Removes the channel with the specified ID.
     * 
     * @see ChannelServletInterface#removeChannel
     */
    @Override
    public synchronized void removeChannel(String channelId) throws ClientException {
        getServlet(ChannelServletInterface.class).removeChannel(channelId);
    }

    /**
     * Removes the channels with the specified IDs.
     * 
     * @see ChannelServletInterface#removeChannels
     */
    @Override
    public synchronized void removeChannels(Set<String> channelIds) throws ClientException {
        if (CollectionUtils.size(channelIds) > MAX_QUERY_PARAM_COLLECTION_SIZE) {
            removeChannelsPost(channelIds);
        } else {
            getServlet(ChannelServletInterface.class).removeChannels(channelIds);
        }
    }

    /**
     * Removes the channels with the specified IDs. This is a POST request alternative to DELETE
     * /channels that may be used when there are too many channel IDs to include in the query
     * parameters.
     * 
     * @see ChannelServletInterface#removeChannels
     */
    @Override
    public synchronized void removeChannelsPost(Set<String> channelIds) throws ClientException {
        getServlet(ChannelServletInterface.class).removeChannelsPost(channelIds);
    }

    /**
     * Retrieves all channel groups.
     * 
     * @see ChannelGroupServletInterface#getChannelGroups
     */
    public List<ChannelGroup> getAllChannelGroups() throws ClientException {
        return getServlet(ChannelGroupServletInterface.class).getChannelGroups(null);
    }

    /**
     * Retrieves selected channel groups.
     * 
     * @see ChannelGroupServletInterface#getChannelGroups
     */
    @Override
    public List<ChannelGroup> getChannelGroups(Set<String> channelGroupIds) throws ClientException {
        if (CollectionUtils.size(channelGroupIds) > MAX_QUERY_PARAM_COLLECTION_SIZE) {
            return getChannelGroupsPost(channelGroupIds);
        }
        return getServlet(ChannelGroupServletInterface.class).getChannelGroups(channelGroupIds);
    }

    /**
     * Retrieves selected channel groups. This is a POST request alternative to GET /channelgroups
     * that may be used when there are too many channel group IDs to include in the query
     * parameters.
     * 
     * @see ChannelGroupServletInterface#getChannelGroups
     */
    @Override
    public List<ChannelGroup> getChannelGroupsPost(Set<String> channelGroupIds) throws ClientException {
        return getServlet(ChannelGroupServletInterface.class).getChannelGroupsPost(channelGroupIds);
    }

    /**
     * Updates channel groups.
     * 
     * @throws ClientException
     * 
     * @see ChannelGroupServletInterface#updateChannelGroups
     */
    @Override
    public boolean updateChannelGroups(Set<ChannelGroup> channelGroups, Set<String> removedChannelGroupIds, boolean override) throws ClientException {
        return getServlet(ChannelGroupServletInterface.class).updateChannelGroups(channelGroups, removedChannelGroupIds, override);
    }

    /**************************
     * Channel Status Servlet *
     **************************/

    /**
     * Returns the dashboard status for a single channel ID.
     * 
     * @see ChannelStatusServletInterface#getChannelStatus
     */
    @Override
    public DashboardStatus getChannelStatus(String channelId) throws ClientException {
        return getServlet(ChannelStatusServletInterface.class).getChannelStatus(channelId);
    }

    /**
     * Returns all channel dashboard statuses.
     * 
     * @see ChannelStatusServletInterface#getChannelStatusList
     */
    public List<DashboardStatus> getAllChannelStatuses() throws ClientException {
        return getServlet(ChannelStatusServletInterface.class).getChannelStatusList(null, null, false);
    }

    /**
     * Returns a DashboardChannelInfo object containing a partial channel status list and a set of
     * remaining channel IDs. The fetch size specifies the maximum number of statuses to return.
     * 
     * @see ChannelStatusServletInterface#getDashboardChannelInfo
     */
    @Override
    public DashboardChannelInfo getDashboardChannelInfo(int fetchSize, String filter) throws ClientException {
        return getServlet(ChannelStatusServletInterface.class).getDashboardChannelInfo(fetchSize, filter);
    }

    /**
     * Returns the channel status list for specific channel IDs.Undeployed channels are not
     * included.
     * 
     * @see ChannelStatusServletInterface#getChannelStatusList
     */
    public List<DashboardStatus> getChannelStatusList(Set<String> channelIds, String filter) throws ClientException {
        return getServlet(ChannelStatusServletInterface.class).getChannelStatusList(channelIds, filter, false);
    }

    /**
     * Returns the channel status list for specific channel IDs. With option to include undeployed
     * channels.
     * 
     * @see ChannelStatusServletInterface#getChannelStatusList
     */
    @Override
    public List<DashboardStatus> getChannelStatusList(Set<String> channelIds, String filter, boolean includeUndeployed) throws ClientException {
        if (CollectionUtils.size(channelIds) > MAX_QUERY_PARAM_COLLECTION_SIZE) {
            return getChannelStatusListPost(channelIds, filter, includeUndeployed);
        }
        return getServlet(ChannelStatusServletInterface.class).getChannelStatusList(channelIds, filter, includeUndeployed);
    }

    /**
     * Returns the channel status list for specific channel IDs. With option to include undeployed
     * channels. This is a POST request alternative to GET /statuses that may be used when there are
     * too many channel IDs to include in the query parameters.
     * 
     * @see ChannelStatusServletInterface#getChannelStatusList
     */
    @Override
    public List<DashboardStatus> getChannelStatusListPost(Set<String> channelIds, String filter, boolean includeUndeployed) throws ClientException {
        return getServlet(ChannelStatusServletInterface.class).getChannelStatusListPost(channelIds, filter, includeUndeployed);
    }

    /**
     * Starts the channel with the specified ID.
     * 
     * @see ChannelStatusServletInterface#startChannel
     */
    public void startChannel(String channelId) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).startChannel(channelId, false);
    }

    /**
     * Starts the channel with the specified ID.
     * 
     * @see ChannelStatusServletInterface#startChannel
     */
    @Override
    public void startChannel(String channelId, boolean returnErrors) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).startChannel(channelId, returnErrors);
    }

    /**
     * Starts the channels with the specified IDs.
     * 
     * @see ChannelStatusServletInterface#startChannels
     */
    public void startChannels(Set<String> channelIds) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).startChannels(channelIds, false);
    }

    /**
     * Starts the channels with the specified IDs.
     * 
     * @see ChannelStatusServletInterface#startChannels
     */
    @Override
    public void startChannels(Set<String> channelIds, boolean returnErrors) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).startChannels(channelIds, returnErrors);
    }

    /**
     * Stops the channel with the specified ID.
     * 
     * @see ChannelStatusServletInterface#stopChannel
     */
    public void stopChannel(String channelId) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).stopChannel(channelId, false);
    }

    /**
     * Stops the channel with the specified ID.
     * 
     * @see ChannelStatusServletInterface#stopChannel
     */
    @Override
    public void stopChannel(String channelId, boolean returnErrors) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).stopChannel(channelId, returnErrors);
    }

    /**
     * Stops the channels with the specified IDs.
     * 
     * @see ChannelStatusServletInterface#stopChannels
     */
    public void stopChannels(Set<String> channelIds) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).stopChannels(channelIds, false);
    }

    /**
     * Stops the channels with the specified IDs.
     * 
     * @see ChannelStatusServletInterface#stopChannels
     */
    @Override
    public void stopChannels(Set<String> channelIds, boolean returnErrors) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).stopChannels(channelIds, returnErrors);
    }

    /**
     * Halts the channel with the specified ID.
     * 
     * @see ChannelStatusServletInterface#haltChannel
     */
    public void haltChannel(String channelId) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).haltChannel(channelId, false);
    }

    /**
     * Halts the channel with the specified ID.
     * 
     * @see ChannelStatusServletInterface#haltChannel
     */
    @Override
    public void haltChannel(String channelId, boolean returnErrors) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).haltChannel(channelId, returnErrors);
    }

    /**
     * Halts the channels with the specified IDs.
     * 
     * @see ChannelStatusServletInterface#haltChannels
     */
    public void haltChannels(Set<String> channelIds) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).haltChannels(channelIds, false);
    }

    /**
     * Halts the channels with the specified IDs.
     * 
     * @see ChannelStatusServletInterface#haltChannels
     */
    @Override
    public void haltChannels(Set<String> channelIds, boolean returnErrors) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).haltChannels(channelIds, returnErrors);
    }

    /**
     * Pauses the channel with the specified ID.
     * 
     * @see ChannelStatusServletInterface#pauseChannel
     */
    public void pauseChannel(String channelId) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).pauseChannel(channelId, false);
    }

    /**
     * Pauses the channel with the specified ID.
     * 
     * @see ChannelStatusServletInterface#pauseChannel
     */
    @Override
    public void pauseChannel(String channelId, boolean returnErrors) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).pauseChannel(channelId, returnErrors);
    }

    /**
     * Pauses the channels with the specified IDs.
     * 
     * @see ChannelStatusServletInterface#pauseChannels
     */
    public void pauseChannels(Set<String> channelIds) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).pauseChannels(channelIds, false);
    }

    /**
     * Pauses the channels with the specified IDs.
     * 
     * @see ChannelStatusServletInterface#pauseChannels
     */
    @Override
    public void pauseChannels(Set<String> channelIds, boolean returnErrors) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).pauseChannels(channelIds, returnErrors);
    }

    /**
     * Resumes the channel with the specified ID.
     * 
     * @see ChannelStatusServletInterface#resumeChannel
     */
    public void resumeChannel(String channelId) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).resumeChannel(channelId, false);
    }

    /**
     * Resumes the channel with the specified ID.
     * 
     * @see ChannelStatusServletInterface#resumeChannel
     */
    @Override
    public void resumeChannel(String channelId, boolean returnErrors) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).resumeChannel(channelId, returnErrors);
    }

    /**
     * Resumes the channels with the specified IDs.
     * 
     * @see ChannelStatusServletInterface#resumeChannels
     */
    public void resumeChannels(Set<String> channelIds) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).resumeChannels(channelIds, false);
    }

    /**
     * Resumes the channels with the specified IDs.
     * 
     * @see ChannelStatusServletInterface#resumeChannels
     */
    @Override
    public void resumeChannels(Set<String> channelIds, boolean returnErrors) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).resumeChannels(channelIds, returnErrors);
    }

    /**
     * Starts the connector with the specified channel and metadata ID.
     * 
     * @see ChannelStatusServletInterface#startConnector
     */
    public void startConnector(String channelId, Integer metaDataId) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).startConnector(channelId, metaDataId, false);
    }

    /**
     * Starts the connector with the specified channel and metadata ID.
     * 
     * @see ChannelStatusServletInterface#startConnector
     */
    @Override
    public void startConnector(String channelId, Integer metaDataId, boolean returnErrors) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).startConnector(channelId, metaDataId, returnErrors);
    }

    /**
     * Starts the connectors with the specified channel and metadata IDs.
     * 
     * @see ChannelStatusServletInterface#startConnectors
     */
    public void startConnectors(Map<String, List<Integer>> connectorInfo) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).startConnectors(connectorInfo, false);
    }

    /**
     * Starts the connectors with the specified channel and metadata IDs.
     * 
     * @see ChannelStatusServletInterface#startConnectors
     */
    @Override
    public void startConnectors(Map<String, List<Integer>> connectorInfo, boolean returnErrors) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).startConnectors(connectorInfo, returnErrors);
    }

    /**
     * Stops the connector with the specified channel and metadata ID.
     * 
     * @see ChannelStatusServletInterface#stopConnector
     */
    public void stopConnector(String channelId, Integer metaDataId) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).stopConnector(channelId, metaDataId, false);
    }

    /**
     * Stops the connector with the specified channel and metadata ID.
     * 
     * @see ChannelStatusServletInterface#stopConnector
     */
    @Override
    public void stopConnector(String channelId, Integer metaDataId, boolean returnErrors) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).stopConnector(channelId, metaDataId, returnErrors);
    }

    /**
     * Stops the connectors with the specified channel and metadata IDs.
     * 
     * @see ChannelStatusServletInterface#stopConnectors
     */
    public void stopConnectors(Map<String, List<Integer>> connectorInfo) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).stopConnectors(connectorInfo, false);
    }

    /**
     * Stops the connectors with the specified channel and metadata IDs.
     * 
     * @see ChannelStatusServletInterface#stopConnectors
     */
    @Override
    public void stopConnectors(Map<String, List<Integer>> connectorInfo, boolean returnErrors) throws ClientException {
        getServlet(ChannelStatusServletInterface.class).stopConnectors(connectorInfo, returnErrors);
    }

    /******************************
     * Channel Statistics Servlet *
     ******************************/

    /**
     * Returns the individual statistics for all deployed channels.
     * 
     * @see ChannelStatisticsServletInterface#getStatistics
     */

    public List<ChannelStatistics> getStatistics() throws ClientException {
        return getServlet(ChannelStatisticsServletInterface.class).getStatistics(null, false, null, null, false);
    }

    /**
     * Returns the individual statistics for channels. Has option to include undeployed channels.
     * 
     * @see ChannelStatisticsServletInterface#getStatistics
     */

    public List<ChannelStatistics> getStatistics(boolean includeUndeployed) throws ClientException {
        return getServlet(ChannelStatisticsServletInterface.class).getStatistics(null, includeUndeployed, null, null, false);
    }

    /**
     * Returns the individual statistics for channels. Has option to include undeployed channels and
     * to aggregate stats.
     * 
     * @see ChannelStatisticsServletInterface#getStatistics
     */

    public List<ChannelStatistics> getStatistics(boolean includeUndeployed, boolean aggregateStats) throws ClientException {
        return getServlet(ChannelStatisticsServletInterface.class).getStatistics(null, includeUndeployed, null, null, aggregateStats);
    }

    /**
     * Returns the individual statistics for channels supplied. Has option to include undeployed
     * channels, aggregate stats, and also include OR exclude connectors.
     * 
     * @see ChannelStatisticsServletInterface#getStatistics
     */
    @Override
    public List<ChannelStatistics> getStatistics(Set<String> channelIds, boolean includeUndeployed, Set<Integer> includeMetadataIds, Set<Integer> excludeMetadataIds, boolean aggregateStats) throws ClientException {
        if (CollectionUtils.size(channelIds) > MAX_QUERY_PARAM_COLLECTION_SIZE) {
            return getStatisticsPost(channelIds, includeUndeployed, includeMetadataIds, excludeMetadataIds, aggregateStats);
        }
        return getServlet(ChannelStatisticsServletInterface.class).getStatistics(channelIds, includeUndeployed, includeMetadataIds, excludeMetadataIds, aggregateStats);
    }

    /**
     * Returns the individual statistics for channels supplied. Has option to include undeployed
     * channels, aggregate stats, and also include OR exclude connectors. This is a POST request
     * alternative to GET /statistics that may be used when there are too many channel IDs to
     * include in the query parameters.
     * 
     * @see ChannelStatisticsServletInterface#getStatistics
     */
    @Override
    public List<ChannelStatistics> getStatisticsPost(Set<String> channelIds, boolean includeUndeployed, Set<Integer> includeMetadataIds, Set<Integer> excludeMetadataIds, boolean aggregateStats) throws ClientException {
        return getServlet(ChannelStatisticsServletInterface.class).getStatisticsPost(channelIds, includeUndeployed, includeMetadataIds, excludeMetadataIds, aggregateStats);
    }

    /**
     * Returns the Statistics for the channel with the specified id.
     * 
     * @see ChannelStatisticsServletInterface#getStatistics
     */
    @Override
    public ChannelStatistics getStatistics(String channelId) throws ClientException {
        return getServlet(ChannelStatisticsServletInterface.class).getStatistics(channelId);
    }

    /**
     * Clears the statistics for the given channels and/or connectors.
     * 
     * @param channelConnectorMap
     *            Channel IDs mapped to lists of metaDataIds (connectors). If the metaDataId list is
     *            null, then all statistics for the channel will be cleared.
     * 
     * @see ChannelStatisticsServletInterface#clearStatistics
     */
    @Override
    public void clearStatistics(Map<String, List<Integer>> channelConnectorMap, boolean received, boolean filtered, boolean sent, boolean error) throws ClientException {
        getServlet(ChannelStatisticsServletInterface.class).clearStatistics(channelConnectorMap, received, filtered, sent, error);
    }

    /**
     * Clears all statistics (including lifetime) for all channels/connectors.
     * 
     * @see ChannelStatisticsServletInterface#clearAllStatistics
     */
    @Override
    public void clearAllStatistics() throws ClientException {
        getServlet(ChannelStatisticsServletInterface.class).clearAllStatistics();
    }

    /******************
     * Engine Servlet *
     ******************/

    /**
     * Redeploys all channels.
     * 
     * @see EngineServletInterface#redeployAllChannels
     */
    public void redeployAllChannels() throws ClientException {
        getServlet(EngineServletInterface.class).redeployAllChannels(false);
    }

    /**
     * Redeploys all channels.
     * 
     * @see EngineServletInterface#redeployAllChannels
     */
    @Override
    public void redeployAllChannels(boolean returnErrors) throws ClientException {
        getServlet(EngineServletInterface.class).redeployAllChannels(returnErrors);
    }

    /**
     * Deploys (or redeploys) a single channel.
     * 
     * @see EngineServletInterface#deployChannel
     */
    public void deployChannel(String channelId) throws ClientException {
        getServlet(EngineServletInterface.class).deployChannel(channelId, false);
    }

    /**
     * Deploys (or redeploys) a single channel.
     * 
     * @see EngineServletInterface#deployChannel
     */
    @Override
    public void deployChannel(String channelId, boolean returnErrors) throws ClientException {
        getServlet(EngineServletInterface.class).deployChannel(channelId, returnErrors);
    }

    /**
     * Deploys (or redeploys) selected channels.
     * 
     * @see EngineServletInterface#deployChannels
     */
    public void deployChannels(Set<String> channelIds) throws ClientException {
        getServlet(EngineServletInterface.class).deployChannels(channelIds, false);
    }

    /**
     * Deploys (or redeploys) selected channels.
     * 
     * @see EngineServletInterface#deployChannels
     */
    @Override
    public void deployChannels(Set<String> channelIds, boolean returnErrors) throws ClientException {
        getServlet(EngineServletInterface.class).deployChannels(channelIds, returnErrors);
    }

    /**
     * Undeploys a single channel.
     * 
     * @see EngineServletInterface#undeployChannel
     */
    public void undeployChannel(String channelId) throws ClientException {
        getServlet(EngineServletInterface.class).undeployChannel(channelId, false);
    }

    /**
     * Undeploys a single channel.
     * 
     * @see EngineServletInterface#undeployChannel
     */
    @Override
    public void undeployChannel(String channelId, boolean returnErrors) throws ClientException {
        getServlet(EngineServletInterface.class).undeployChannel(channelId, returnErrors);
    }

    /**
     * Undeploys selected channels.
     * 
     * @see EngineServletInterface#undeployChannels
     */
    public void undeployChannels(Set<String> channelIds) throws ClientException {
        getServlet(EngineServletInterface.class).undeployChannels(channelIds, false);
    }

    /**
     * Undeploys selected channels.
     * 
     * @see EngineServletInterface#undeployChannels
     */
    @Override
    public void undeployChannels(Set<String> channelIds, boolean returnErrors) throws ClientException {
        getServlet(EngineServletInterface.class).undeployChannels(channelIds, returnErrors);
    }

    /*******************
     * Message Servlet *
     *******************/

    /**
     * Processes a new message through a channel.
     * 
     * @see MessageServletInterface#processMessage
     */
    public void processMessage(String channelId, String rawMessage) throws ClientException {
        processMessage(channelId, rawMessage, null, null, false, false, null);
    }

    /**
     * Processes a new message through a channel.
     * 
     * @see MessageServletInterface#processMessage
     */
    @Override
    public void processMessage(String channelId, String rawData, Set<Integer> destinationMetaDataIds, Set<String> sourceMapEntries, boolean overwrite, boolean imported, Long originalMessageId) throws ClientException {
        getServlet(MessageServletInterface.class).processMessage(channelId, rawData, destinationMetaDataIds, sourceMapEntries, overwrite, imported, originalMessageId);
    }

    /**
     * Processes a new message through a channel, using the RawMessage object.
     * 
     * @see MessageServletInterface#processMessage
     */
    @Override
    public void processMessage(String channelId, RawMessage rawMessage) throws ClientException {
        getServlet(MessageServletInterface.class).processMessage(channelId, rawMessage);
    }

    /**
     * Processes a new message through a channel, using the RawMessage object.
     * 
     * @see MessageServletInterface#getMessageContent
     */
    @Override
    public Message getMessageContent(String channelId, Long messageId, List<Integer> metaDataIds) throws ClientException {
        return getServlet(MessageServletInterface.class).getMessageContent(channelId, messageId, metaDataIds);
    }

    /**
     * Retrieve a list of attachments by message ID.
     * 
     * @see MessageServletInterface#getAttachmentsByMessageId
     */
    public List<Attachment> getAttachmentsByMessageId(String channelId, Long messageId) throws ClientException {
        return getAttachmentsByMessageId(channelId, messageId, true);
    }

    /**
     * Retrieve a list of attachments by message ID.
     * 
     * @see MessageServletInterface#getAttachmentsByMessageId
     */
    @Override
    public List<Attachment> getAttachmentsByMessageId(String channelId, Long messageId, boolean includeContent) throws ClientException {
        return getServlet(MessageServletInterface.class).getAttachmentsByMessageId(channelId, messageId, includeContent);
    }

    /**
     * Retrieve a message attachment by ID.
     * 
     * @see MessageServletInterface#getAttachment
     */
    @Override
    public Attachment getAttachment(String channelId, Long messageId, String attachmentId) throws ClientException {
        return getServlet(MessageServletInterface.class).getAttachment(channelId, messageId, attachmentId);
    }

    /**
     * Given a ConnectorMessage object, reattaches any DICOM attachment data and returns the raw
     * Base64 encoded message data.
     * 
     * @see MessageServletInterface#getDICOMMessage
     */
    public String getDICOMMessage(ConnectorMessage message) throws ClientException {
        return getDICOMMessage(message.getChannelId(), message.getMessageId(), message);
    }

    /**
     * Given a ConnectorMessage object, reattaches any DICOM attachment data and returns the raw
     * Base64 encoded message data.
     * 
     * @see MessageServletInterface#getDICOMMessage
     */
    @Override
    public String getDICOMMessage(String channelId, Long messageId, ConnectorMessage message) throws ClientException {
        return getServlet(MessageServletInterface.class).getDICOMMessage(channelId, messageId, message);
    }

    /**
     * Returns the maximum message ID for the given channel.
     * 
     * @see MessageServletInterface#getMaxMessageId
     */
    @Override
    public Long getMaxMessageId(String channelId) throws ClientException {
        return getServlet(MessageServletInterface.class).getMaxMessageId(channelId);
    }

    /**
     * Search for messages by specific filter criteria.
     * 
     * @see MessageServletInterface#getMessages
     */
    @Override
    public List<Message> getMessages(String channelId, MessageFilter filter, Boolean includeContent, Integer offset, Integer limit) throws ClientException {
        return getServlet(MessageServletInterface.class).getMessages(channelId, filter, includeContent, offset, limit);
    }

    /**
     * Search for messages by specific filter criteria.
     * 
     * @see MessageServletInterface#getMessages
     */
    @Override
    public List<Message> getMessages(String channelId, Long minMessageId, Long maxMessageId, Long minOriginalId, Long maxOriginalId, Long minImportId, Long maxImportId, Calendar startDate, Calendar endDate, String textSearch, Boolean textSearchRegex, Set<Status> statuses, Set<Integer> includedMetaDataIds, Set<Integer> excludedMetaDataIds, String serverId, Set<String> rawContentSearches, Set<String> processedRawContentSearches, Set<String> transformedContentSearches, Set<String> encodedContentSearches, Set<String> sentContentSearches, Set<String> responseContentSearches, Set<String> responseTransformedContentSearches, Set<String> processedResponseContentSearches, Set<String> connectorMapContentSearches, Set<String> channelMapContentSearches, Set<String> sourceMapContentSearches, Set<String> responseMapContentSearches, Set<String> processingErrorContentSearches, Set<String> postprocessorErrorContentSearches, Set<String> responseErrorContentSearches, Set<MetaDataSearch> metaDataSearches, Set<MetaDataSearch> metaDataCaseInsensitiveSearches, Set<String> textSearchMetaDataColumns, Integer minSendAttempts, Integer maxSendAttempts, Boolean attachment, Boolean error, Boolean includeContent, Integer offset, Integer limit) throws ClientException {
        return getServlet(MessageServletInterface.class).getMessages(channelId, minMessageId, maxMessageId, minOriginalId, maxOriginalId, minImportId, maxImportId, startDate, endDate, textSearch, textSearchRegex, statuses, includedMetaDataIds, excludedMetaDataIds, serverId, rawContentSearches, processedRawContentSearches, transformedContentSearches, encodedContentSearches, sentContentSearches, responseContentSearches, responseTransformedContentSearches, processedResponseContentSearches, connectorMapContentSearches, channelMapContentSearches, sourceMapContentSearches, responseMapContentSearches, processingErrorContentSearches, postprocessorErrorContentSearches, responseErrorContentSearches, metaDataSearches, metaDataCaseInsensitiveSearches, textSearchMetaDataColumns, minSendAttempts, maxSendAttempts, attachment, error, includeContent, offset, limit);
    }

    /**
     * Count number for messages by specific filter criteria.
     * 
     * @see MessageServletInterface#getMessageCount
     */
    @Override
    public Long getMessageCount(String channelId, MessageFilter filter) throws ClientException {
        return getServlet(MessageServletInterface.class).getMessageCount(channelId, filter);
    }

    /**
     * Count number for messages by specific filter criteria.
     * 
     * @see MessageServletInterface#getMessageCount
     */
    @Override
    public Long getMessageCount(String channelId, Long minMessageId, Long maxMessageId, Long minOriginalId, Long maxOriginalId, Long minImportId, Long maxImportId, Calendar startDate, Calendar endDate, String textSearch, Boolean textSearchRegex, Set<Status> statuses, Set<Integer> includedMetaDataIds, Set<Integer> excludedMetaDataIds, String serverId, Set<String> rawContentSearches, Set<String> processedRawContentSearches, Set<String> transformedContentSearches, Set<String> encodedContentSearches, Set<String> sentContentSearches, Set<String> responseContentSearches, Set<String> responseTransformedContentSearches, Set<String> processedResponseContentSearches, Set<String> connectorMapContentSearches, Set<String> channelMapContentSearches, Set<String> sourceMapContentSearches, Set<String> responseMapContentSearches, Set<String> processingErrorContentSearches, Set<String> postprocessorErrorContentSearches, Set<String> responseErrorContentSearches, Set<MetaDataSearch> metaDataSearches, Set<MetaDataSearch> metaDataCaseInsensitiveSearches, Set<String> textSearchMetaDataColumns, Integer minSendAttempts, Integer maxSendAttempts, Boolean attachment, Boolean error) throws ClientException {
        return getServlet(MessageServletInterface.class).getMessageCount(channelId, minMessageId, maxMessageId, minOriginalId, maxOriginalId, minImportId, maxImportId, startDate, endDate, textSearch, textSearchRegex, statuses, includedMetaDataIds, excludedMetaDataIds, serverId, rawContentSearches, processedRawContentSearches, transformedContentSearches, encodedContentSearches, sentContentSearches, responseContentSearches, responseTransformedContentSearches, processedResponseContentSearches, connectorMapContentSearches, channelMapContentSearches, sourceMapContentSearches, responseMapContentSearches, processingErrorContentSearches, postprocessorErrorContentSearches, responseErrorContentSearches, metaDataSearches, metaDataCaseInsensitiveSearches, textSearchMetaDataColumns, minSendAttempts, maxSendAttempts, attachment, error);
    }

    /**
     * Reprocesses messages through a channel by specific filter criteria.
     * 
     * @see MessageServletInterface#reprocessMessages
     */
    public void reprocessMessages(String channelId, MessageFilter filter, boolean replace, Collection<Integer> reprocessMetaDataIds) throws ClientException {
        Set<Integer> set = null;
        if (reprocessMetaDataIds != null) {
            set = new HashSet<Integer>(reprocessMetaDataIds);
        }
        reprocessMessages(channelId, filter, replace, reprocessMetaDataIds != null, set);
    }

    /**
     * Reprocesses messages through a channel by specific filter criteria.
     * 
     * @see MessageServletInterface#reprocessMessages
     */
    @Override
    public void reprocessMessages(String channelId, MessageFilter filter, boolean replace, boolean filterDestinations, Set<Integer> reprocessMetaDataIds) throws ClientException {
        getServlet(MessageServletInterface.class).reprocessMessages(channelId, filter, replace, filterDestinations, reprocessMetaDataIds);
    }

    /**
     * Reprocesses messages through a channel by specific filter criteria.
     * 
     * @see MessageServletInterface#reprocessMessages
     */
    @Override
    public void reprocessMessages(String channelId, Long minMessageId, Long maxMessageId, Long minOriginalId, Long maxOriginalId, Long minImportId, Long maxImportId, Calendar startDate, Calendar endDate, String textSearch, Boolean textSearchRegex, Set<Status> statuses, Set<Integer> includedMetaDataIds, Set<Integer> excludedMetaDataIds, String serverId, Set<String> rawContentSearches, Set<String> processedRawContentSearches, Set<String> transformedContentSearches, Set<String> encodedContentSearches, Set<String> sentContentSearches, Set<String> responseContentSearches, Set<String> responseTransformedContentSearches, Set<String> processedResponseContentSearches, Set<String> connectorMapContentSearches, Set<String> channelMapContentSearches, Set<String> sourceMapContentSearches, Set<String> responseMapContentSearches, Set<String> processingErrorContentSearches, Set<String> postprocessorErrorContentSearches, Set<String> responseErrorContentSearches, Set<MetaDataSearch> metaDataSearches, Set<MetaDataSearch> metaDataCaseInsensitiveSearches, Set<String> textSearchMetaDataColumns, Integer minSendAttempts, Integer maxSendAttempts, Boolean attachment, Boolean error, boolean replace, boolean filterDestinations, Set<Integer> reprocessMetaDataIds) throws ClientException {
        getServlet(MessageServletInterface.class).reprocessMessages(channelId, minMessageId, maxMessageId, minOriginalId, maxOriginalId, minImportId, maxImportId, startDate, endDate, textSearch, textSearchRegex, statuses, includedMetaDataIds, excludedMetaDataIds, serverId, rawContentSearches, processedRawContentSearches, transformedContentSearches, encodedContentSearches, sentContentSearches, responseContentSearches, responseTransformedContentSearches, processedResponseContentSearches, connectorMapContentSearches, channelMapContentSearches, sourceMapContentSearches, responseMapContentSearches, processingErrorContentSearches, postprocessorErrorContentSearches, responseErrorContentSearches, metaDataSearches, metaDataCaseInsensitiveSearches, textSearchMetaDataColumns, minSendAttempts, maxSendAttempts, attachment, error, replace, filterDestinations, reprocessMetaDataIds);
    }

    /**
     * Reprocesses and overwrites a single message.
     * 
     * @see MessageServletInterface#reprocessMessage
     */
    public void reprocessMessage(String channelId, Long messageId, boolean replace, Collection<Integer> reprocessMetaDataIds) throws ClientException {
        Set<Integer> set = null;
        if (reprocessMetaDataIds != null) {
            set = new HashSet<Integer>(reprocessMetaDataIds);
        }
        getServlet(MessageServletInterface.class).reprocessMessage(channelId, messageId, replace, reprocessMetaDataIds != null, set);
    }

    /**
     * Reprocesses and overwrites a single message.
     * 
     * @see MessageServletInterface#reprocessMessage
     */
    @Override
    public void reprocessMessage(String channelId, Long messageId, boolean replace, boolean filterDestinations, Set<Integer> reprocessMetaDataIds) throws ClientException {
        getServlet(MessageServletInterface.class).reprocessMessage(channelId, messageId, replace, filterDestinations, reprocessMetaDataIds);
    }

    /**
     * Remove messages by specific filter criteria.
     * 
     * @see MessageServletInterface#removeMessages
     */
    @Override
    public void removeMessages(String channelId, MessageFilter filter) throws ClientException {
        getServlet(MessageServletInterface.class).removeMessages(channelId, filter);
    }

    /**
     * Remove messages by specific filter criteria.
     * 
     * @see MessageServletInterface#removeMessages
     */
    @Override
    public void removeMessages(String channelId, Long minMessageId, Long maxMessageId, Long minOriginalId, Long maxOriginalId, Long minImportId, Long maxImportId, Calendar startDate, Calendar endDate, String textSearch, Boolean textSearchRegex, Set<Status> statuses, Set<Integer> includedMetaDataIds, Set<Integer> excludedMetaDataIds, String serverId, Set<String> rawContentSearches, Set<String> processedRawContentSearches, Set<String> transformedContentSearches, Set<String> encodedContentSearches, Set<String> sentContentSearches, Set<String> responseContentSearches, Set<String> responseTransformedContentSearches, Set<String> processedResponseContentSearches, Set<String> connectorMapContentSearches, Set<String> channelMapContentSearches, Set<String> sourceMapContentSearches, Set<String> responseMapContentSearches, Set<String> processingErrorContentSearches, Set<String> postprocessorErrorContentSearches, Set<String> responseErrorContentSearches, Set<MetaDataSearch> metaDataSearches, Set<MetaDataSearch> metaDataCaseInsensitiveSearches, Set<String> textSearchMetaDataColumns, Integer minSendAttempts, Integer maxSendAttempts, Boolean attachment, Boolean error) throws ClientException {
        getServlet(MessageServletInterface.class).removeMessages(channelId, minMessageId, maxMessageId, minOriginalId, maxOriginalId, minImportId, maxImportId, startDate, endDate, textSearch, textSearchRegex, statuses, includedMetaDataIds, excludedMetaDataIds, serverId, rawContentSearches, processedRawContentSearches, transformedContentSearches, encodedContentSearches, sentContentSearches, responseContentSearches, responseTransformedContentSearches, processedResponseContentSearches, connectorMapContentSearches, channelMapContentSearches, sourceMapContentSearches, responseMapContentSearches, processingErrorContentSearches, postprocessorErrorContentSearches, responseErrorContentSearches, metaDataSearches, metaDataCaseInsensitiveSearches, textSearchMetaDataColumns, minSendAttempts, maxSendAttempts, attachment, error);
    }

    /**
     * Remove a single message by ID.
     * 
     * @see MessageServletInterface#removeMessage
     */
    @Override
    public void removeMessage(String channelId, Long messageId, Integer metaDataId) throws ClientException {
        getServlet(MessageServletInterface.class).removeMessage(channelId, messageId, metaDataId);
    }

    /**
     * Removes all messages for the specified channel.
     * 
     * @see MessageServletInterface#removeAllMessages
     */
    @Override
    public void removeAllMessages(String channelId, boolean restartRunningChannels, boolean clearStatistics) throws ClientException {
        getServlet(MessageServletInterface.class).removeAllMessages(channelId, restartRunningChannels, clearStatistics);
    }

    /**
     * Removes all messages for multiple specified channels.
     * 
     * @see MessageServletInterface#removeAllMessages
     */
    @Override
    public void removeAllMessages(Set<String> channelIds, boolean restartRunningChannels, boolean clearStatistics) throws ClientException {
        if (CollectionUtils.size(channelIds) > MAX_QUERY_PARAM_COLLECTION_SIZE) {
            removeAllMessagesPost(channelIds, restartRunningChannels, clearStatistics);
        } else {
            getServlet(MessageServletInterface.class).removeAllMessages(channelIds, restartRunningChannels, clearStatistics);
        }
    }

    /**
     * Removes all messages for multiple specified channels. This is a POST request alternative to
     * DELETE /_removeAllMessages that may be used when there are too many channel IDs to include in
     * the query parameters.
     * 
     * @see MessageServletInterface#removeAllMessages
     */
    @Override
    public void removeAllMessagesPost(Set<String> channelIds, boolean restartRunningChannels, boolean clearStatistics) throws ClientException {
        getServlet(MessageServletInterface.class).removeAllMessagesPost(channelIds, restartRunningChannels, clearStatistics);
    }

    /**
     * Imports a Message object into a channel. The message will not actually be processed through
     * the channel, only imported.
     * 
     * @see MessageServletInterface#importMessage
     */
    @Override
    public void importMessage(String channelId, Message message) throws ClientException {
        getServlet(MessageServletInterface.class).importMessage(channelId, message);
    }

    /**
     * Imports messages into a channel from a path accessible by the server. The messages will not
     * actually be processed through the channel, only imported.
     * 
     * @see MessageServletInterface#importMessagesServer
     */
    @Override
    public MessageImportResult importMessagesServer(String channelId, String path, boolean includeSubfolders) throws ClientException {
        return getServlet(MessageServletInterface.class).importMessagesServer(channelId, path, includeSubfolders);
    }

    /**
     * Exports messages into a specific directory path accessible by the server.
     * 
     * @see MessageServletInterface#exportMessagesServer
     */
    @Override
    public int exportMessagesServer(final String channelId, final MessageFilter filter, final int pageSize, final MessageWriterOptions writerOptions) throws ClientException {
        return getServlet(MessageServletInterface.class).exportMessagesServer(channelId, filter, pageSize, writerOptions);
    }

    /**
     * Exports messages into a specific directory path accessible by the server.
     * 
     * @see MessageServletInterface#exportMessagesServer
     */
    @Override
    public int exportMessagesServer(String channelId, Long minMessageId, Long maxMessageId, Long minOriginalId, Long maxOriginalId, Long minImportId, Long maxImportId, Calendar startDate, Calendar endDate, String textSearch, Boolean textSearchRegex, Set<Status> statuses, Set<Integer> includedMetaDataIds, Set<Integer> excludedMetaDataIds, String serverId, Set<String> rawContentSearches, Set<String> processedRawContentSearches, Set<String> transformedContentSearches, Set<String> encodedContentSearches, Set<String> sentContentSearches, Set<String> responseContentSearches, Set<String> responseTransformedContentSearches, Set<String> processedResponseContentSearches, Set<String> connectorMapContentSearches, Set<String> channelMapContentSearches, Set<String> sourceMapContentSearches, Set<String> responseMapContentSearches, Set<String> processingErrorContentSearches, Set<String> postprocessorErrorContentSearches, Set<String> responseErrorContentSearches, Set<MetaDataSearch> metaDataSearches, Set<MetaDataSearch> metaDataCaseInsensitiveSearches, Set<String> textSearchMetaDataColumns, Integer minSendAttempts, Integer maxSendAttempts, Boolean attachment, Boolean error, int pageSize, ContentType contentType, boolean destinationContent, boolean encrypt, boolean includeAttachments, String baseFolder, String rootFolder, String filePattern, String archiveFileName, String archiveFormat, String compressFormat, String password, EncryptionType encryptionType) throws ClientException {
        return getServlet(MessageServletInterface.class).exportMessagesServer(channelId, minMessageId, maxMessageId, minOriginalId, maxOriginalId, minImportId, maxImportId, startDate, endDate, textSearch, textSearchRegex, statuses, includedMetaDataIds, excludedMetaDataIds, serverId, rawContentSearches, processedRawContentSearches, transformedContentSearches, encodedContentSearches, sentContentSearches, responseContentSearches, responseTransformedContentSearches, processedResponseContentSearches, connectorMapContentSearches, channelMapContentSearches, sourceMapContentSearches, responseMapContentSearches, processingErrorContentSearches, postprocessorErrorContentSearches, responseErrorContentSearches, metaDataSearches, metaDataCaseInsensitiveSearches, textSearchMetaDataColumns, minSendAttempts, maxSendAttempts, attachment, error, pageSize, contentType, destinationContent, encrypt, includeAttachments, baseFolder, rootFolder, filePattern, archiveFileName, archiveFormat, compressFormat, password, encryptionType);
    }

    /**
     * Exports a message attachment into a specific file path accessible by the server.
     * 
     * @see MessageServletInterface#exportAttachmentServer
     */
    @Override
    public void exportAttachmentServer(String channelId, Long messageId, String attachmentId, String filePath, boolean binary) throws ClientException {
        getServlet(MessageServletInterface.class).exportAttachmentServer(channelId, messageId, attachmentId, filePath, binary);
    }

    /*****************
     * Event Servlet *
     *****************/

    /**
     * Returns the maximum event ID currently in the database.
     * 
     * @see EventServletInterface#getMaxEventId
     */
    @Override
    public Integer getMaxEventId() throws ClientException {
        return getServlet(EventServletInterface.class).getMaxEventId();
    }

    /**
     * Retrieves an event by ID.
     * 
     * @see EventServletInterface#getEvent
     */
    @Override
    public ServerEvent getEvent(Integer eventId) throws ClientException {
        return getServlet(EventServletInterface.class).getEvent(eventId);
    }

    /**
     * Search for events by specific filter criteria.
     * 
     * @see EventServletInterface#getEvents
     */
    @Override
    public List<ServerEvent> getEvents(EventFilter filter, Integer offset, Integer limit) throws ClientException {
        return getServlet(EventServletInterface.class).getEvents(filter, offset, limit);
    }

    /**
     * Search for events by specific filter criteria.
     * 
     * @see EventServletInterface#getEvents
     */
    @Override
    public List<ServerEvent> getEvents(Integer maxEventId, Integer minEventId, Set<Level> levels, Calendar startDate, Calendar endDate, String name, Outcome outcome, Integer userId, String ipAddress, String serverId, Integer offset, Integer limit) throws ClientException {
        return getServlet(EventServletInterface.class).getEvents(maxEventId, minEventId, levels, startDate, endDate, name, outcome, userId, ipAddress, serverId, offset, limit);
    }

    /**
     * Count number for events by specific filter criteria.
     * 
     * @see EventServletInterface#getEventCount
     */
    @Override
    public Long getEventCount(EventFilter filter) throws ClientException {
        return getServlet(EventServletInterface.class).getEventCount(filter);
    }

    /**
     * Count number for events by specific filter criteria.
     * 
     * @see EventServletInterface#getEventCount
     */
    @Override
    public Long getEventCount(Integer maxEventId, Integer minEventId, Set<Level> levels, Calendar startDate, Calendar endDate, String name, Outcome outcome, Integer userId, String ipAddress, String serverId) throws ClientException {
        return getServlet(EventServletInterface.class).getEventCount(maxEventId, minEventId, levels, startDate, endDate, name, outcome, userId, ipAddress, serverId);
    }

    /**
     * Exports all events to the application data directory on the server.
     * 
     * @see EventServletInterface#exportAllEvents
     */
    @Override
    public String exportAllEvents() throws ClientException {
        return getServlet(EventServletInterface.class).exportAllEvents();
    }

    /**
     * Remove all events, with the option to export them first.
     * 
     * @see EventServletInterface#removeAllEvents
     */
    @Override
    public String removeAllEvents(boolean export) throws ClientException {
        return getServlet(EventServletInterface.class).removeAllEvents(export);
    }

    /**
     * Remove all events.
     * 
     * @see EventServletInterface#removeAllEvents
     */
    public String removeAllEvents() throws ClientException {
        return getServlet(EventServletInterface.class).removeAllEvents(false);
    }

    /**
     * Exports all events to the application data directory on the server, then removes all events.
     * 
     * @see EventServletInterface#removeAllEvents
     */
    public String exportAndRemoveAllEvents() throws ClientException {
        return getServlet(EventServletInterface.class).removeAllEvents(true);
    }

    /*****************
     * Alert Servlet *
     *****************/

    /**
     * Creates a new alert.
     * 
     * @see AlertServletInterface#createAlert
     */
    @Override
    public void createAlert(AlertModel alertModel) throws ClientException {
        getServlet(AlertServletInterface.class).createAlert(alertModel);
    }

    /**
     * Retrieves an alert by ID.
     * 
     * @see AlertServletInterface#getAlert
     */
    @Override
    public AlertModel getAlert(String alertId) throws ClientException {
        return getServlet(AlertServletInterface.class).getAlert(alertId);
    }

    /**
     * Retrieves all alerts.
     * 
     * @see AlertServletInterface#getAlerts
     */
    public List<AlertModel> getAllAlerts() throws ClientException {
        return getServlet(AlertServletInterface.class).getAlerts(null);
    }

    /**
     * Retrieves multiple alerts by ID, or all alerts if not specified.
     * 
     * @see AlertServletInterface#getAlerts
     */
    @Override
    public List<AlertModel> getAlerts(Set<String> alertIds) throws ClientException {
        if (CollectionUtils.size(alertIds) > MAX_QUERY_PARAM_COLLECTION_SIZE) {
            return getAlertsPost(alertIds);
        }
        return getServlet(AlertServletInterface.class).getAlerts(alertIds);
    }

    /**
     * Retrieves multiple alerts by ID, or all alerts if not specified. This is a POST request
     * alternative to GET /alerts that may be used when there are too many alert IDs to include in
     * the query parameters.
     * 
     * @see AlertServletInterface#getAlerts
     */
    @Override
    public List<AlertModel> getAlertsPost(Set<String> alertIds) throws ClientException {
        return getServlet(AlertServletInterface.class).getAlertsPost(alertIds);
    }

    /**
     * Returns all alert dashboard statuses.
     * 
     * @see AlertServletInterface#getAlertStatusList
     */
    @Override
    public List<AlertStatus> getAlertStatusList() throws ClientException {
        return getServlet(AlertServletInterface.class).getAlertStatusList();
    }

    /**
     * Returns an AlertInfo object containing the alert model, alert protocol options, and any
     * updated channel summaries.
     * 
     * @see AlertServletInterface#getAlertInfo
     */
    @Override
    public AlertInfo getAlertInfo(String alertId, Map<String, ChannelHeader> cachedChannels) throws ClientException {
        return getServlet(AlertServletInterface.class).getAlertInfo(alertId, cachedChannels);
    }

    /**
     * Returns an AlertInfo object containing alert protocol options and any updated channel
     * summaries.
     * 
     * @see AlertServletInterface#getAlertInfo
     */
    @Override
    public AlertInfo getAlertInfo(Map<String, ChannelHeader> cachedChannels) throws ClientException {
        return getServlet(AlertServletInterface.class).getAlertInfo(cachedChannels);
    }

    /**
     * Returns all alert protocol options.
     * 
     * @see AlertServletInterface#getAlertProtocolOptions
     */
    @Override
    public Map<String, Map<String, String>> getAlertProtocolOptions() throws ClientException {
        return getServlet(AlertServletInterface.class).getAlertProtocolOptions();
    }

    /**
     * Updates the specified alert.
     * 
     * @see AlertServletInterface#updateAlert
     */
    public synchronized void updateAlert(AlertModel alertModel) throws ClientException {
        getServlet(AlertServletInterface.class).updateAlert(alertModel.getId(), alertModel);
    }

    /**
     * Updates the specified alert.
     * 
     * @see AlertServletInterface#updateAlert
     */
    @Override
    public synchronized void updateAlert(String alertId, AlertModel alertModel) throws ClientException {
        getServlet(AlertServletInterface.class).updateAlert(alertId, alertModel);
    }

    /**
     * Enables the specified alert.
     * 
     * @see AlertServletInterface#enableAlert
     */
    @Override
    public synchronized void enableAlert(String alertId) throws ClientException {
        getServlet(AlertServletInterface.class).enableAlert(alertId);
    }

    /**
     * Disables the specified alert.
     * 
     * @see AlertServletInterface#disableAlert
     */
    public synchronized void disableAlert(String alertId) throws ClientException {
        getServlet(AlertServletInterface.class).disableAlert(alertId);
    }

    /**
     * Removes the specified alert.
     * 
     * @see AlertServletInterface#removeAlert
     */
    @Override
    public synchronized void removeAlert(String alertId) throws ClientException {
        getServlet(AlertServletInterface.class).removeAlert(alertId);
    }

    /*************************
     * Code Template Servlet *
     *************************/

    /**
     * Retrieves all code template libraries.
     * 
     * @see CodeTemplateServletInterface#getCodeTemplateLibraries
     */
    public List<CodeTemplateLibrary> getAllCodeTemplateLibraries(boolean includeCodeTemplates) throws ClientException {
        return getServlet(CodeTemplateServletInterface.class).getCodeTemplateLibraries(null, includeCodeTemplates);
    }

    /**
     * Retrieves multiple code template libraries by ID, or all libraries if not specified.
     * 
     * @see CodeTemplateServletInterface#getCodeTemplateLibraries
     */
    @Override
    public List<CodeTemplateLibrary> getCodeTemplateLibraries(Set<String> libraryIds, boolean includeCodeTemplates) throws ClientException {
        if (CollectionUtils.size(libraryIds) > MAX_QUERY_PARAM_COLLECTION_SIZE) {
            return getCodeTemplateLibrariesPost(libraryIds, includeCodeTemplates);
        }
        return getServlet(CodeTemplateServletInterface.class).getCodeTemplateLibraries(libraryIds, includeCodeTemplates);
    }

    /**
     * Retrieves multiple code template libraries by ID, or all libraries if not specified. This is
     * a POST request alternative to GET /codeTemplateLibraries that may be used when there are too
     * many library IDs to include in the query parameters.
     * 
     * @see CodeTemplateServletInterface#getCodeTemplateLibraries
     */
    @Override
    public List<CodeTemplateLibrary> getCodeTemplateLibrariesPost(Set<String> libraryIds, boolean includeCodeTemplates) throws ClientException {
        return getServlet(CodeTemplateServletInterface.class).getCodeTemplateLibrariesPost(libraryIds, includeCodeTemplates);
    }

    /**
     * Retrieves a single code template library.
     * 
     * @see CodeTemplateServletInterface#getCodeTemplateLibrary
     */
    @Override
    public CodeTemplateLibrary getCodeTemplateLibrary(String libraryId, boolean includeCodeTemplates) throws ClientException {
        return getServlet(CodeTemplateServletInterface.class).getCodeTemplateLibrary(libraryId, includeCodeTemplates);
    }

    /**
     * Replaces all code template libraries.
     * 
     * @see CodeTemplateServletInterface#updateCodeTemplateLibraries
     */
    @Override
    public synchronized boolean updateCodeTemplateLibraries(List<CodeTemplateLibrary> libraries, boolean override) throws ClientException {
        return getServlet(CodeTemplateServletInterface.class).updateCodeTemplateLibraries(libraries, override);
    }

    /**
     * Retrieves all code templates.
     * 
     * @see CodeTemplateServletInterface#getCodeTemplates
     */
    public List<CodeTemplate> getAllCodeTemplates() throws ClientException {
        return getServlet(CodeTemplateServletInterface.class).getCodeTemplates(null);
    }

    /**
     * Retrieves multiple code templates by ID, or all templates if not specified.
     * 
     * @see CodeTemplateServletInterface#getCodeTemplates
     */
    @Override
    public List<CodeTemplate> getCodeTemplates(Set<String> codeTemplateIds) throws ClientException {
        if (CollectionUtils.size(codeTemplateIds) > MAX_QUERY_PARAM_COLLECTION_SIZE) {
            return getCodeTemplatesPost(codeTemplateIds);
        }
        return getServlet(CodeTemplateServletInterface.class).getCodeTemplates(codeTemplateIds);
    }

    /**
     * Retrieves multiple code templates by ID, or all templates if not specified. This is a POST
     * request alternative to GET /codeTemplates that may be used when there are too many code
     * template IDs to include in the query parameters.
     * 
     * @see CodeTemplateServletInterface#getCodeTemplates
     */
    @Override
    public List<CodeTemplate> getCodeTemplatesPost(Set<String> codeTemplateIds) throws ClientException {
        return getServlet(CodeTemplateServletInterface.class).getCodeTemplatesPost(codeTemplateIds);
    }

    /**
     * Retrieves a single code template.
     * 
     * @see CodeTemplateServletInterface#getCodeTemplate
     */
    @Override
    public CodeTemplate getCodeTemplate(String codeTemplateId) throws ClientException {
        return getServlet(CodeTemplateServletInterface.class).getCodeTemplate(codeTemplateId);
    }

    /**
     * Returns a list of code template summaries, indicating to a client which code templates have
     * changed. If a code template was modified, the entire CodeTemplate object will be returned.
     * 
     * @see CodeTemplateServletInterface#getCodeTemplateSummary
     */
    @Override
    public List<CodeTemplateSummary> getCodeTemplateSummary(Map<String, Integer> clientRevisions) throws ClientException {
        return getServlet(CodeTemplateServletInterface.class).getCodeTemplateSummary(clientRevisions);
    }

    /**
     * Updates a single code template.
     * 
     * @see CodeTemplateServletInterface#updateCodeTemplate
     */
    public synchronized boolean updateCodeTemplate(CodeTemplate codeTemplate, boolean override) throws ClientException {
        return getServlet(CodeTemplateServletInterface.class).updateCodeTemplate(codeTemplate.getId(), codeTemplate, override);
    }

    /**
     * Updates a single code template.
     * 
     * @see CodeTemplateServletInterface#updateCodeTemplate
     */
    @Override
    public synchronized boolean updateCodeTemplate(String codeTemplateId, CodeTemplate codeTemplate, boolean override) throws ClientException {
        return getServlet(CodeTemplateServletInterface.class).updateCodeTemplate(codeTemplateId, codeTemplate, override);
    }

    /**
     * Removes a single code template.
     * 
     * @see CodeTemplateServletInterface#removeCodeTemplate
     */
    @Override
    public synchronized void removeCodeTemplate(String codeTemplateId) throws ClientException {
        getServlet(CodeTemplateServletInterface.class).removeCodeTemplate(codeTemplateId);
    }

    /**
     * Updates all libraries and updates/removes selected code templates in one request.
     * 
     * @see CodeTemplateServletInterface#updateLibrariesAndTemplates
     */
    @Override
    public synchronized CodeTemplateLibrarySaveResult updateLibrariesAndTemplates(List<CodeTemplateLibrary> libraries, Set<String> removedLibraryIds, List<CodeTemplate> updatedCodeTemplates, Set<String> removedCodeTemplateIds, boolean override) throws ClientException {
        return getServlet(CodeTemplateServletInterface.class).updateLibrariesAndTemplates(libraries, removedLibraryIds, updatedCodeTemplates, removedCodeTemplateIds, override);
    }

    /*************************
     * Database Task Servlet *
     *************************/

    /**
     * Retrieves all current database tasks.
     * 
     * @see DatabaseTaskServletInterface#getDatabaseTasks
     */
    @Override
    public Map<String, DatabaseTask> getDatabaseTasks() throws ClientException {
        return getServlet(DatabaseTaskServletInterface.class).getDatabaseTasks();
    }

    /**
     * Retrieves a single database task.
     * 
     * @see DatabaseTaskServletInterface#getDatabaseTask
     */
    @Override
    public DatabaseTask getDatabaseTask(String databaseTaskId) throws ClientException {
        return getServlet(DatabaseTaskServletInterface.class).getDatabaseTask(databaseTaskId);
    }

    /**
     * Executes the specified database task.
     * 
     * @see DatabaseTaskServletInterface#runDatabaseTask
     */
    @Override
    public String runDatabaseTask(String databaseTaskId) throws ClientException {
        return getServlet(DatabaseTaskServletInterface.class).runDatabaseTask(databaseTaskId);
    }

    /**
     * Cancels execution of the specified database task.
     * 
     * @see DatabaseTaskServletInterface#cancelDatabaseTask
     */
    @Override
    public void cancelDatabaseTask(String databaseTaskId) throws ClientException {
        getServlet(DatabaseTaskServletInterface.class).cancelDatabaseTask(databaseTaskId);
    }

    /***********************************
     * Usage Data Servlet *
     ***********************************/

    /**
     * Generates usage document using data from both the client and server.
     * 
     * @see UsageServletInterface#getUsageData
     */
    @Override
    public String getUsageData(Map<String, Object> clientStats) throws ClientException {
        return getServlet(UsageServletInterface.class).getUsageData(clientStats);
    }

    /*********************
     * Extension Servlet *
     *********************/

    /**
     * Installs an extension.
     * 
     * @see ExtensionServletInterface#installExtension
     */
    public void installExtension(File file) throws ClientException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            getServlet(ExtensionServletInterface.class).installExtension(inputStream);
        } catch (FileNotFoundException e) {
            throw new ClientException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Installs an extension.
     * 
     * @see ExtensionServletInterface#installExtension
     */
    @Override
    public void installExtension(InputStream inputStream) throws ClientException {
        getServlet(ExtensionServletInterface.class).installExtension(inputStream);
    }

    /**
     * Uninstalls an extension.
     * 
     * @see ExtensionServletInterface#uninstallExtension
     */
    @Override
    public void uninstallExtension(String extensionPath) throws ClientException {
        getServlet(ExtensionServletInterface.class).uninstallExtension(extensionPath);
    }

    /**
     * Returns extension metadata by name.
     * 
     * @see ExtensionServletInterface#getExtensionMetaData
     */
    @Override
    public MetaData getExtensionMetaData(String extensionName) throws ClientException {
        return getServlet(ExtensionServletInterface.class).getExtensionMetaData(extensionName);
    }

    /**
     * Returns all active connector metadata.
     * 
     * @see ExtensionServletInterface#getConnectorMetaData
     */
    @Override
    public Map<String, ConnectorMetaData> getConnectorMetaData() throws ClientException {
        return getServlet(ExtensionServletInterface.class).getConnectorMetaData();
    }

    /**
     * Returns all active plugin metadata.
     * 
     * @see ExtensionServletInterface#getPluginMetaData
     */
    @Override
    public Map<String, PluginMetaData> getPluginMetaData() throws ClientException {
        return getServlet(ExtensionServletInterface.class).getPluginMetaData();
    }

    /**
     * Returns the enabled status of an extension.
     * 
     * @see ExtensionServletInterface#isExtensionEnabled
     */
    @Override
    public boolean isExtensionEnabled(String extensionName) throws ClientException {
        return getServlet(ExtensionServletInterface.class).isExtensionEnabled(extensionName);
    }

    /**
     * Enables or disables an extension.
     * 
     * @see ExtensionServletInterface#setExtensionEnabled
     */
    @Override
    public void setExtensionEnabled(String extensionName, boolean enabled) throws ClientException {
        getServlet(ExtensionServletInterface.class).setExtensionEnabled(extensionName, enabled);
    }

    /**
     * Returns properties for a specified extension.
     * 
     * @see ExtensionServletInterface#getPluginProperties
     */
    @Override
    public Properties getPluginProperties(String extensionName) throws ClientException {
        return getServlet(ExtensionServletInterface.class).getPluginProperties(extensionName);
    }

    /**
     * Sets properties for a specified extension.
     * 
     * @see ExtensionServletInterface#setPluginProperties
     */
    @Override
    public void setPluginProperties(String extensionName, Properties properties) throws ClientException {
        getServlet(ExtensionServletInterface.class).setPluginProperties(extensionName, properties);
    }
}
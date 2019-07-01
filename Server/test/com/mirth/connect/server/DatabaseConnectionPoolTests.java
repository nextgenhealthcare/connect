/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.io.Resources;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.Invocation;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mirth.commons.encryption.Digester;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.DonkeyConfiguration;
import com.mirth.connect.donkey.server.DonkeyConnectionPools;
import com.mirth.connect.donkey.server.data.ChannelDoesNotExistException;
import com.mirth.connect.donkey.server.data.jdbc.ConnectionPool;
import com.mirth.connect.donkey.server.data.jdbc.PooledConnection;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelGroup;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.DatabaseSettings;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.User;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;
import com.mirth.connect.model.filters.EventFilter;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.Cache;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.DatabaseTaskController;
import com.mirth.connect.server.controllers.DefaultAlertController;
import com.mirth.connect.server.controllers.DefaultChannelController;
import com.mirth.connect.server.controllers.DefaultConfigurationController;
import com.mirth.connect.server.controllers.DefaultControllerFactory;
import com.mirth.connect.server.controllers.DefaultDatabaseTaskController;
import com.mirth.connect.server.controllers.DefaultEventController;
import com.mirth.connect.server.controllers.DefaultScriptController;
import com.mirth.connect.server.controllers.DefaultUserController;
import com.mirth.connect.server.controllers.DonkeyMessageController;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.controllers.MessageController;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.controllers.UserController;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.server.util.StatementLock;

public class DatabaseConnectionPoolTests {

    private static final boolean USE_REAL_DATABASE_CONNECTIONS = false;

    private static final String database = "postgres";
    private static final String databaseUrl = "jdbc:postgresql://localhost:5432/mirthdb";
    private static final String databaseDriver = null;
    private static final String databaseUsername = "postgres";
    private static final String databasePassword = "postgres";
    private static final Integer databaseMaxConnections = 20;
    private static final String databasePool = null;
    private static final String databaseReadOnly = null;
    private static final String databaseReadOnlyUrl = null;
    private static final String databaseReadOnlyDriver = null;
    private static final String databaseReadOnlyUsername = null;
    private static final String databaseReadOnlyPassword = null;
    private static final Integer databaseReadOnlyMaxConnections = null;
    private static final String databaseReadOnlyPool = null;

    private SqlConfig sqlConfig;
    private Donkey donkey;

    private Connection connection;
    private Connection readOnlyConnection;

    @Test
    public void testCachesReadPool() throws Exception {
        setup(true, false);

        Cache<Channel> channelCache = new Cache<Channel>("Channel", "Channel.getChannelRevision", "Channel.getChannel");
        channelCache.getAllItems();
        channelCache.getCachedIds();

        Cache<ChannelGroup> channelGroupCache = new Cache<ChannelGroup>("Channel Group", "Channel.getChannelGroupRevision", "Channel.getChannelGroup");
        channelGroupCache.getAllItems();
        channelGroupCache.getCachedIds();

        Cache<CodeTemplate> codeTemplateCache = new Cache<CodeTemplate>("Code Template", "CodeTemplate.getCodeTemplateRevision", "CodeTemplate.getCodeTemplate", false);
        codeTemplateCache.getAllItems();
        codeTemplateCache.getCachedIds();

        Cache<CodeTemplateLibrary> libraryCache = new Cache<CodeTemplateLibrary>("Code Template Library", "CodeTemplate.getLibraryRevision", "CodeTemplate.getLibrary");
        libraryCache.getAllItems();
        libraryCache.getCachedIds();

        assertReadOnly();
    }

    @Test
    public void testCachesWritePool() throws Exception {
        setup(true, true);

        Cache<Channel> channelCache = new Cache<Channel>("Channel", "Channel.getChannelRevision", "Channel.getChannel");
        channelCache.getAllItems();
        channelCache.getCachedIds();

        Cache<ChannelGroup> channelGroupCache = new Cache<ChannelGroup>("Channel Group", "Channel.getChannelGroupRevision", "Channel.getChannelGroup");
        channelGroupCache.getAllItems();
        channelGroupCache.getCachedIds();

        Cache<CodeTemplate> codeTemplateCache = new Cache<CodeTemplate>("Code Template", "CodeTemplate.getCodeTemplateRevision", "CodeTemplate.getCodeTemplate", false);
        codeTemplateCache.getAllItems();
        codeTemplateCache.getCachedIds();

        Cache<CodeTemplateLibrary> libraryCache = new Cache<CodeTemplateLibrary>("Code Template Library", "CodeTemplate.getLibraryRevision", "CodeTemplate.getLibrary");
        libraryCache.getAllItems();
        libraryCache.getCachedIds();

        assertWriteOnly();
    }

    @Test
    public void testAlertController() throws Exception {
        setup();
        AlertController alertController = DefaultAlertController.create();
        alertController.getAlerts();
        alertController.getAlert("test");
        assertReadOnly();
    }

    @Test
    public void testChannelController() throws Exception {
        setup();
        ChannelController channelController = DefaultChannelController.create();
        channelController.getChannelRevisions();
        channelController.getChannelSummary(new HashMap<String, ChannelHeader>(), false);
        assertReadOnly();
    }

    @Test
    public void testConfigurationController() throws Exception {
        setup();
        ConfigurationController configurationController = DefaultConfigurationController.create();
        configurationController.getProperty("group", "name");
        configurationController.getPropertiesForGroup("group");
        assertReadOnly();
    }

    @Test
    public void testDatabaseTaskController() throws Exception {
        setup();
        DatabaseTaskController databaseTaskController = DefaultDatabaseTaskController.create();
        databaseTaskController.getDatabaseTasks();
        assertReadOnly();
    }

    @Test
    public void testEventController() throws Exception {
        setup();
        EventController eventController = DefaultEventController.create();
        eventController.getMaxEventId();
        EventFilter eventFilter = new EventFilter();
        eventFilter.setMaxEventId(1000000);
        eventController.getEvents(eventFilter, 0, 100);
        eventController.getEventCount(eventFilter);
        assertReadOnly();
    }

    @Test
    public void testScriptController() throws Exception {
        setup();
        ScriptController scriptController = DefaultScriptController.create();
        scriptController.getScript(ScriptController.GLOBAL_GROUP_ID, ScriptController.DEPLOY_SCRIPT_KEY);
        assertReadOnly();
    }

    @Test
    public void testUserController() throws Exception {
        setup();
        UserController userController = spy(DefaultUserController.create());
        userController.getAllUsers();
        userController.getUser(1, null);

        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setUsername("admin");
        doReturn(adminUser).when(userController).getUser(null, adminUser.getUsername());
        userController.authorizeUser("admin", "admin");

        userController.getUserCredentials(1);
        userController.getUserPreferences(1, null);
        userController.getUserPreference(1, null);

        assertReadOnly();
    }

    @Test
    public void testMessageController() throws Exception {
        setup();

        if (!USE_REAL_DATABASE_CONNECTIONS) {
            com.mirth.connect.donkey.server.controllers.ChannelController channelController = spy(com.mirth.connect.donkey.server.controllers.ChannelController.class);
            doReturn(1L).when(channelController).getLocalChannelId(eq("test"), anyBoolean());
            Injector injector = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    requestStaticInjection(com.mirth.connect.donkey.server.controllers.ChannelController.class);
                    bind(com.mirth.connect.donkey.server.controllers.ChannelController.class).toInstance(channelController);
                }
            });
            injector.getInstance(com.mirth.connect.donkey.server.controllers.ChannelController.class);
        }

        MessageController messageController = DonkeyMessageController.create();
        try {
            messageController.getMaxMessageId("test", true);
        } catch (ChannelDoesNotExistException e) {
        }
        try {
            messageController.getMinMessageId("test", true);
        } catch (ChannelDoesNotExistException e) {
        }

        MessageFilter messageFilter = new MessageFilter();
        messageFilter.setMaxMessageId(1000000L);
        try {
            messageController.getMessageCount(messageFilter, "test");
        } catch (ChannelDoesNotExistException e) {
        }
        try {
            messageController.getMessages(messageFilter, "test", true, 0, 100);
        } catch (ChannelDoesNotExistException e) {
        }
        try {
            messageController.getMessageContent("test", 1L, Arrays.asList(new Integer[] { 0, 1 }));
        } catch (ChannelDoesNotExistException e) {
        }

        try {
            messageController.getMessageAttachmentIds("test", 1L, true);
        } catch (ChannelDoesNotExistException e) {
        }
        try {
            messageController.getMessageAttachment("test", 1L, true);
        } catch (ChannelDoesNotExistException e) {
        }
        assertReadOnly();

        resetMocks();
        try {
            messageController.getMaxMessageId("test", false);
        } catch (ChannelDoesNotExistException e) {
        }
        try {
            messageController.getMinMessageId("test", false);
        } catch (ChannelDoesNotExistException e) {
        }
        try {
            messageController.getMessageAttachmentIds("test", 1L, false);
        } catch (ChannelDoesNotExistException e) {
        }
        try {
            messageController.getMessageAttachment("test", 1L, false);
        } catch (ChannelDoesNotExistException e) {
        }
        assertWriteOnly();
    }

    private void setup() throws Exception {
        setup(true, false);
    }

    private void setup(boolean splitReadWrite, boolean writePoolCache) throws Exception {
        getClass().getClassLoader().loadClass("com.mirth.connect.model.User");
        Resources.setDefaultClassLoader(getClass().getClassLoader());
        Resources.classForName("com.mirth.connect.model.User");

        DatabaseSettings databaseSettings = new DatabaseSettings();
        databaseSettings.setDatabase(database);
        databaseSettings.setDatabaseUrl(databaseUrl);
        databaseSettings.setDatabaseDriver(databaseDriver);
        databaseSettings.setDatabaseUsername(databaseUsername);
        databaseSettings.setDatabasePassword(databasePassword);
        databaseSettings.setDatabaseMaxConnections(databaseMaxConnections);
        databaseSettings.setDatabasePool(databasePool);
        databaseSettings.setDatabaseReadOnly(databaseReadOnly);
        databaseSettings.setDatabaseReadOnlyUrl(databaseReadOnlyUrl);
        databaseSettings.setDatabaseReadOnlyDriver(databaseReadOnlyDriver);
        databaseSettings.setDatabaseReadOnlyUsername(databaseReadOnlyUsername);
        databaseSettings.setDatabaseReadOnlyPassword(databaseReadOnlyPassword);
        databaseSettings.setDatabaseReadOnlyMaxConnections(databaseReadOnlyMaxConnections);
        databaseSettings.setDatabaseReadOnlyPool(databaseReadOnlyPool);
        databaseSettings.setSplitReadWrite(splitReadWrite);
        databaseSettings.setWritePoolCache(writePoolCache);

        DonkeyConnectionPools donkeyCP = spy(DonkeyConnectionPools.class);

        if (!USE_REAL_DATABASE_CONNECTIONS) {
            connection = createConnection(databaseSettings.getDatabase());
            readOnlyConnection = createConnection(StringUtils.defaultIfBlank(databaseSettings.getDatabaseReadOnly(), databaseSettings.getDatabase()));
            doReturn(createPool(connection)).when(donkeyCP).createConnectionPool(anyString(), anyString(), anyString(), anyString(), anyString(), any(), anyBoolean(), anyString(), anyInt(), eq(false));
            doReturn(createPool(readOnlyConnection)).when(donkeyCP).createConnectionPool(anyString(), anyString(), anyString(), anyString(), anyString(), any(), anyBoolean(), anyString(), anyInt(), eq(true));
        }

        Injector donkeyConnectionPoolsInjector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                requestStaticInjection(DonkeyConnectionPools.class);
                bind(DonkeyConnectionPools.class).toInstance(donkeyCP);
            }
        });
        donkeyConnectionPoolsInjector.getInstance(DonkeyConnectionPools.class);

        DonkeyConnectionPools.getInstance().init(databaseSettings.getProperties());

        donkey = spy(Donkey.class);

        Injector donkeyInjector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                requestStaticInjection(Donkey.class);
                bind(Donkey.class).toInstance(donkey);
            }
        });
        donkeyInjector.getInstance(Donkey.class);

        ControllerFactory controllerFactory = spy(new DefaultControllerFactory());

        ConfigurationController configurationController = mock(ConfigurationController.class);
        when(configurationController.getDatabaseSettings()).thenReturn(databaseSettings);
        when(configurationController.getPasswordRequirements()).thenReturn(new PasswordRequirements());
        Digester digester = mock(Digester.class);
        when(digester.matches(anyString(), anyString())).thenReturn(true);
        when(configurationController.getDigester()).thenReturn(digester);
        doReturn(configurationController).when(controllerFactory).createConfigurationController();

        ExtensionController extensionController = mock(ExtensionController.class);
        doReturn(extensionController).when(controllerFactory).createExtensionController();

        Injector controllerFactoryInjector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                requestStaticInjection(ControllerFactory.class);
                bind(ControllerFactory.class).toInstance(controllerFactory);
            }
        });
        controllerFactoryInjector.getInstance(ControllerFactory.class);

        sqlConfig = spy(new SqlConfig());

        Injector sqlConfigInjector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                requestStaticInjection(SqlConfig.class);
                bind(SqlConfig.class).toInstance(sqlConfig);
            }
        });
        sqlConfigInjector.getInstance(SqlConfig.class);

        donkey.startEngine(new DonkeyConfiguration("appdata", databaseSettings.getProperties(), null, null, "testserverid"));

        // These use connections from the write pool, so initialize them now before resetting mocks
        StatementLock.getInstance(DefaultChannelController.VACUUM_LOCK_CHANNEL_STATEMENT_ID);
        StatementLock.getInstance(DefaultAlertController.VACUUM_LOCK_ALERT_STATEMENT_ID);
        StatementLock.getInstance(DefaultConfigurationController.VACUUM_LOCK_STATEMENT_ID);
        StatementLock.getInstance(DefaultScriptController.VACUUM_LOCK_SCRIPT_STATEMENT_ID);
        StatementLock.getInstance(DefaultUserController.VACUUM_LOCK_PERSON_STATEMENT_ID);
        StatementLock.getInstance(DefaultUserController.VACUUM_LOCK_PREFERENCES_STATEMENT_ID);

        resetMocks();
    }

    private Connection createConnection(String database) throws Exception {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);

        DatabaseMetaData dbMetaData = mock(DatabaseMetaData.class);
        when(dbMetaData.getTables(any(), any(), any(), any())).thenReturn(mock(ResultSet.class));

        if (StringUtils.equals(database, "postgres")) {
            when(dbMetaData.getDatabaseMajorVersion()).thenReturn(9);
            when(dbMetaData.getDatabaseMinorVersion()).thenReturn(0);
        }

        when(connection.getMetaData()).thenReturn(dbMetaData);

        return connection;
    }

    private ConnectionPool createPool(Connection connection) throws Exception {
        ConnectionPool connectionPool = mock(ConnectionPool.class);
        when(connectionPool.getConnection()).thenReturn(new PooledConnection(connection, connection));
        when(connectionPool.getMaxConnections()).thenReturn(20);
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(dataSource.getConnection(anyString(), anyString())).thenReturn(connection);
        when(connectionPool.getDataSource()).thenReturn(dataSource);
        return connectionPool;
    }

    private void resetMocks() {
        reset(sqlConfig);
        reset(donkey);

        if (connection != null) {
            reset(connection);
        }
        if (readOnlyConnection != null) {
            reset(readOnlyConnection);
        }
    }

    private void assertReadOnly() {
        verify(donkey, times(0)).getDaoFactory();
        verify(sqlConfig, times(0)).getSqlSessionManager();
        if (connection != null) {
            verifyZeroInteractions(connection);
        }

        int count = 0;
        for (Invocation invocation : Mockito.mockingDetails(donkey).getInvocations()) {
            if (invocation.getMethod().getName().equals("getReadOnlyDaoFactory")) {
                count++;
            }
        }
        for (Invocation invocation : Mockito.mockingDetails(sqlConfig).getInvocations()) {
            if (invocation.getMethod().getName().equals("getReadOnlySqlSessionManager")) {
                count++;
            }
        }

        assertTrue(count > 0);
    }

    private void assertWriteOnly() {
        verify(donkey, times(0)).getReadOnlyDaoFactory();
        verify(sqlConfig, times(0)).getReadOnlySqlSessionManager();
        if (readOnlyConnection != null) {
            verifyZeroInteractions(readOnlyConnection);
        }

        int count = 0;
        for (Invocation invocation : Mockito.mockingDetails(donkey).getInvocations()) {
            if (invocation.getMethod().getName().equals("getDaoFactory")) {
                count++;
            }
        }
        for (Invocation invocation : Mockito.mockingDetails(sqlConfig).getInvocations()) {
            if (invocation.getMethod().getName().equals("getSqlSessionManager")) {
                count++;
            }
        }

        assertTrue(count > 0);
    }
}

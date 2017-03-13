/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;

import com.mirth.connect.client.core.ConnectServiceUtil;
import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.model.LibraryProperties;
import com.mirth.connect.model.ResourceProperties;
import com.mirth.connect.model.ResourcePropertiesList;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.util.MigrationException;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.controllers.MigrationController;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.controllers.UsageController;
import com.mirth.connect.server.controllers.UserController;
import com.mirth.connect.server.logging.JuliToLog4JService;
import com.mirth.connect.server.logging.LogOutputStream;
import com.mirth.connect.server.logging.MirthLog4jFilter;
import com.mirth.connect.server.util.ResourceUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

/**
 * Instantiate a Mirth server that listens for commands from the CommandQueue.
 * 
 */
public class Mirth extends Thread {

    private Logger logger = Logger.getLogger(this.getClass());
    private boolean running = false;
    private PropertiesConfiguration mirthProperties = new PropertiesConfiguration();
    private PropertiesConfiguration versionProperties = new PropertiesConfiguration();
    private MirthWebServer webServer;
    private CommandQueue commandQueue = CommandQueue.getInstance();
    private EngineController engineController = ControllerFactory.getFactory().createEngineController();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private UserController userController = ControllerFactory.getFactory().createUserController();
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
    private MigrationController migrationController = ControllerFactory.getFactory().createMigrationController();
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
    private ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private UsageController usageController = ControllerFactory.getFactory().createUsageController();

    private static List<Thread> shutdownHooks = new ArrayList<Thread>();

    public static void main(String[] args) {
        Mirth mirth = new Mirth();
        mirth.run();
        System.exit(0);
    }

    public static OutputStream getNullOutputStream() {
        return new OutputStream() {
            public void write(int b) throws IOException {
                // ignore output
            }
        };
    }

    public static void addShutdownHook(Thread hook) {
        if (shutdownHooks.contains(hook)) {
            return;
        }

        shutdownHooks.add(hook);
    }

    public void run() {
        Thread.currentThread().setName("Main Server Thread");
        initializeLogging();

        if (initResources()) {
            logger.debug("starting mirth server...");

            // Initialize TLS system properties as early as possible, because otherwise they will be cached
            if (System.getProperty("jdk.tls.ephemeralDHKeySize") == null) {
                // Only allow integers from 1024-2048 as per JSSE specifications
                int keySize = NumberUtils.toInt(mirthProperties.getString("https.ephemeraldhkeysize", "2048"), 2048);
                if (keySize < 1024) {
                    keySize = 1024;
                } else if (keySize > 2048) {
                    keySize = 2048;
                }
                System.setProperty("jdk.tls.ephemeralDHKeySize", String.valueOf(keySize));
            }

            // check the ports to see if they are already in use
            boolean httpPort = testPort(mirthProperties.getString("http.host"), mirthProperties.getString("http.port"), "http.port");
            boolean httpsPort = testPort(mirthProperties.getString("https.host"), mirthProperties.getString("https.port"), "https.port");

            if (!httpPort || !httpsPort) {
                return;
            }

            running = true;

            // add the start command to the queue
            CommandQueue.getInstance().clear();
            CommandQueue.getInstance().addCommand(new Command(Command.Operation.START_SERVER));

            Runtime.getRuntime().addShutdownHook(new ShutdownHook());

            // pulls commands off of the command queue
            while (running) {
                Command command = commandQueue.getCommand();

                if (command.getOperation().equals(Command.Operation.START_SERVER)) {
                    startup();
                } else if (command.getOperation().equals(Command.Operation.SHUTDOWN_SERVER)) {
                    shutdown();
                }
            }
        } else {
            logger.error("could not initialize resources");
        }
    }

    /**
     * Returns true if the resources required by the server have been successfully loaded
     * 
     * @return true if the resources required by the server have been successfully loaded
     */
    public boolean initResources() {
        InputStream mirthPropertiesStream = null;

        try {
            mirthPropertiesStream = ResourceUtil.getResourceStream(this.getClass(), "mirth.properties");
            mirthProperties.setDelimiterParsingDisabled(true);
            mirthProperties.load(mirthPropertiesStream);
        } catch (Exception e) {
            logger.error("could not load mirth.properties", e);
        } finally {
            IOUtils.closeQuietly(mirthPropertiesStream);
        }

        InputStream versionPropertiesStream = null;

        try {
            versionPropertiesStream = ResourceUtil.getResourceStream(this.getClass(), "version.properties");
            versionProperties.setDelimiterParsingDisabled(true);
            versionProperties.load(versionPropertiesStream);
        } catch (Exception e) {
            logger.error("could not load version.properties", e);
        } finally {
            IOUtils.closeQuietly(versionPropertiesStream);
        }

        return (!mirthProperties.isEmpty());
    }

    /**
     * Starts up the server.
     * 
     */
    public void startup() {
        try {
            ObjectXMLSerializer.getInstance().init(versionProperties.getString("mirth.version"));
        } catch (Exception e) {
        }
        Donkey.getInstance().setSerializer(ObjectXMLSerializer.getInstance());

        configurationController.initializeSecuritySettings();
        configurationController.initializeDatabaseSettings();

        try {
            SqlConfig.getSqlSessionManager().startManagedSession();
            SqlConfig.getSqlSessionManager().getConnection();
        } catch (Exception e) {
            // the getCause is needed since the wrapper exception is from the connection pool
            logger.error("Error establishing connection to database, aborting startup. " + e.getCause().getMessage());
            System.exit(0);
        } finally {
            if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                SqlConfig.getSqlSessionManager().close();
            }
        }

        extensionController.removePropertiesForUninstalledExtensions();

        try {
            migrationController.migrate();
        } catch (MigrationException e) {
            logger.error("Failed to migrate database schema", e);
            stopDatabase();
            running = false;
            return;
        }

        // MIRTH-3535 disable Quartz update check
        System.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");

        configurationController.migrateKeystore();
        extensionController.setDefaultExtensionStatus();
        extensionController.uninstallExtensions();
        migrationController.migrateExtensions();
        extensionController.initPlugins();
        migrationController.migrateSerializedData();
        userController.resetUserStatus();

        // disable the velocity logging
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.NullLogSystem");

        eventController.dispatchEvent(new ServerEvent(configurationController.getServerId(), "Server startup"));

        // Start web server before starting the engine in case there is a 
        // problem starting the engine that causes it to hang
        startWebServer();

        configurationController.setStatus(ConfigurationController.STATUS_ENGINE_STARTING);
        startEngine();

        extensionController.startPlugins();

        contextFactoryController.initGlobalContextFactory();

        try {
            alertController.initAlerts();

            configurationController.setStatus(ConfigurationController.STATUS_INITIAL_DEPLOY);

            // Initialize library resources after the above status is set, so that users can login
            try {
                List<LibraryProperties> libraryResources = new ArrayList<LibraryProperties>();
                for (ResourceProperties resource : ObjectXMLSerializer.getInstance().deserialize(configurationController.getResources(), ResourcePropertiesList.class).getList()) {
                    if (resource instanceof LibraryProperties) {
                        libraryResources.add((LibraryProperties) resource);
                    }
                }

                contextFactoryController.updateResources(libraryResources, true);
            } catch (LinkageError e) {
                logger.warn("Unable to initialize library resources.", e);
            } catch (Exception e) {
                logger.warn("Unable to initialize library resources.", e);
            }

            MirthContextFactory contextFactory;
            try {
                contextFactory = contextFactoryController.getGlobalScriptContextFactory();
            } catch (LinkageError e) {
                logger.warn("Unable to initialize global script context factory.", e);
                contextFactory = contextFactoryController.getGlobalContextFactory();
            } catch (Exception e) {
                logger.warn("Unable to initialize global script context factory.", e);
                contextFactory = contextFactoryController.getGlobalContextFactory();
            }
            scriptController.compileGlobalScripts(contextFactory);

            if (configurationController.isStartupDeploy()) {
                engineController.startupDeploy();
            } else {
                logger.info("Property \"server.startupdeploy\" is disabled. Skipping initial deployment of channels...");
            }
        } catch (Exception e) {
            logger.error(e);
        }

        configurationController.setStatus(ConfigurationController.STATUS_OK);
        printSplashScreen();

        // Send usage stats once a day.
        Timer timer = new Timer();
        timer.schedule(new UsageSenderTask(), 0, ConnectServiceUtil.MILLIS_PER_DAY);
    }

    /**
     * Shuts down the server.
     * 
     */
    public void shutdown() {
        logger.info("shutting down mirth due to normal request");

        stopEngine();

        try {
            // check for database connection before trying to log shutdown event
            SqlConfig.getSqlSessionManager().startManagedSession();
            SqlConfig.getSqlSessionManager().getConnection();
            // add event after stopping the engine, but before stopping the plugins
            eventController.dispatchEvent(new ServerEvent(configurationController.getServerId(), "Server shutdown"));
        } catch (Exception e) {
            logger.debug("could not log shutdown even since database is unavailable", e);
        } finally {
            if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                SqlConfig.getSqlSessionManager().close();
            }
        }

        stopWebServer();
        extensionController.stopPlugins();
        stopDatabase();
        running = false;
    }

    /**
     * Starts the engine.
     * 
     */
    private void startEngine() {
        logger.debug("starting engine");

        try {
            engineController.startEngine();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * Stops the engine.
     * 
     */
    private void stopEngine() {
        logger.debug("stopping engine");

        try {
            engineController.stopEngine();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * Starts the web server.
     * 
     */
    private void startWebServer() {
        logger.debug("starting jetty web server");

        try {
            webServer = new MirthWebServer(mirthProperties);
            webServer.startup();
        } catch (Exception e) {
            logger.warn("Could not start web server.", e);

            try {
                if (webServer != null) {
                    webServer.stop();
                }
            } catch (Throwable e1) {
                // Ignore exception stopping
            } finally {
                webServer = null;
            }
        }
    }

    /**
     * Stops the web server.
     * 
     */
    private void stopWebServer() {
        logger.debug("stopping jetty web server");

        try {
            if (webServer != null) {
                webServer.stop();
            }
        } catch (Exception e) {
            logger.warn("Could not stop web server.", e);
        }
    }

    private void stopDatabase() {
        String database = mirthProperties.getString("database");

        if (database.equals("derby")) {
            boolean gotException = false;

            try {
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
            } catch (SQLException sqle) {
                if ((sqle.getSQLState() != null) && sqle.getSQLState().equals("XJ015")) {
                    gotException = true;
                }
            }

            if (gotException) {
                System.out.println("Database shut down normally.");
            }
        }
    }

    private class ShutdownHook extends Thread {
        public void run() {
            Thread.currentThread().setName("Shutdown Hook Thread");
            shutdown();

            for (Thread thread : shutdownHooks) {
                thread.start();
            }

        }
    }

    /**
     * Displays the splash screen information, including the server version and build date, to the
     * system console.
     */
    private void printSplashScreen() {
        logger.info("Mirth Connect " + versionProperties.getString("mirth.version") + " (Built on " + versionProperties.getString("mirth.date") + ") server successfully started.");
        logger.info("This product was developed by Mirth Corporation (http://www.mirthcorp.com) and its contributors (c)2005-" + Calendar.getInstance().get(Calendar.YEAR) + ".");
        logger.info("Running " + System.getProperty("java.vm.name") + " " + System.getProperty("java.version") + " on " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ", " + System.getProperty("os.arch") + "), " + configurationController.getDatabaseType() + ", with charset " + Charset.defaultCharset() + ".");

        if (webServer != null) {
            String httpUrl = getWebServerUrl("http://", mirthProperties.getString("http.host", "0.0.0.0"), mirthProperties.getInt("http.port"), mirthProperties.getString("http.contextpath"));
            String httpsUrl = getWebServerUrl("https://", mirthProperties.getString("https.host", "0.0.0.0"), mirthProperties.getInt("https.port"), mirthProperties.getString("http.contextpath"));

            logger.info("Web server running at " + httpUrl + " and " + httpsUrl);
        }
    }

    private String getWebServerUrl(String prefix, String host, int port, String contextPath) {
        if (StringUtils.equals(host, "0.0.0.0") || StringUtils.equals(host, "::")) {
            try {
                host = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                host = "localhost";
            }
        } else if (StringUtils.isEmpty(host)) {
            host = "localhost";
        }

        if (!StringUtils.startsWith(contextPath, "/")) {
            contextPath = "/" + contextPath;
        }

        if (!StringUtils.endsWith(contextPath, "/")) {
            contextPath = contextPath + "/";
        }

        return prefix + host + ":" + port + contextPath;
    }

    /**
     * Test a port to see if it is already in use.
     * 
     * @param port
     *            The port to test.
     * @param name
     *            A friendly name to display in case of an error.
     * @return
     */
    private boolean testPort(String host, String port, String name) {
        ServerSocket socket = null;

        try {
            if (StringUtils.equals(host, "0.0.0.0") || StringUtils.equals(host, "::")) {
                socket = new ServerSocket(Integer.parseInt(port));
            } else {
                socket = new ServerSocket(Integer.parseInt(port), 0, InetAddress.getByName(host));
            }
        } catch (NumberFormatException ex) {
            logger.error(name + " port is invalid: " + port);
            return false;
        } catch (IOException ex) {
            logger.error(name + " port is already in use: " + port);
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.error("Could not close test socket for " + name + ": " + port);
                    return false;
                }
            }
        }

        return true;
    }

    private void initializeLogging() {
        // Route all System.err messages to log4j error
        System.setErr(new PrintStream(new LogOutputStream()));
        // Route all java.util.logging.Logger output to log4j
        JuliToLog4JService.getInstance().start();

        // Add a custom filter to appenders to suppress SAXParser warnings introduced in 7u40 (MIRTH-3548)
        for (Enumeration<?> en = Logger.getRootLogger().getAllAppenders(); en.hasMoreElements();) {
            ((Appender) en.nextElement()).addFilter(new MirthLog4jFilter());
        }
    }

    private class UsageSenderTask extends TimerTask {
        @Override
        public void run() {
            boolean isSent = ConnectServiceUtil.sendStatistics(configurationController.getServerId(), configurationController.getServerVersion(), true, usageController.createUsageStats(null), configurationController.getHttpsClientProtocols(), configurationController.getHttpsCipherSuites());
            if (isSent) {
                UpdateSettings settings = new UpdateSettings();
                settings.setLastStatsTime(System.currentTimeMillis());
                try {
                    configurationController.setUpdateSettings(settings);
                } catch (ControllerException e) {
                }
            }
        }
    }
}

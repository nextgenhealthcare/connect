/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

import com.mirth.connect.client.core.ConnectServiceUtil;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.util.MigrationException;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerException;
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
import com.mirth.connect.server.servlets.AlertServlet;
import com.mirth.connect.server.servlets.ChannelServlet;
import com.mirth.connect.server.servlets.ChannelStatisticsServlet;
import com.mirth.connect.server.servlets.ChannelStatusServlet;
import com.mirth.connect.server.servlets.CodeTemplateServlet;
import com.mirth.connect.server.servlets.ConfigurationServlet;
import com.mirth.connect.server.servlets.DatabaseTaskServlet;
import com.mirth.connect.server.servlets.EngineServlet;
import com.mirth.connect.server.servlets.EventServlet;
import com.mirth.connect.server.servlets.ExtensionServlet;
import com.mirth.connect.server.servlets.MessageObjectServlet;
import com.mirth.connect.server.servlets.UsageServlet;
import com.mirth.connect.server.servlets.UserServlet;
import com.mirth.connect.server.servlets.WebStartServlet;
import com.mirth.connect.server.tools.ClassPathResource;
import com.mirth.connect.server.util.ResourceUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.MirthSSLUtil;

/**
 * Instantiate a Mirth server that listens for commands from the CommandQueue.
 * 
 */
public class Mirth extends Thread {
    private Logger logger = Logger.getLogger(this.getClass());
    private boolean running = false;
    private PropertiesConfiguration mirthProperties = new PropertiesConfiguration();
    private PropertiesConfiguration versionProperties = new PropertiesConfiguration();
    private Server webServer = null;
    private CommandQueue commandQueue = CommandQueue.getInstance();
    private EngineController engineController = ControllerFactory.getFactory().createEngineController();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private UserController userController = ControllerFactory.getFactory().createUserController();
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
    private MigrationController migrationController = ControllerFactory.getFactory().createMigrationController();
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
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
        initializeLogging();

        if (initResources()) {
            logger.debug("starting mirth server...");

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

        configurationController.migrateKeystore();
        extensionController.setDefaultExtensionStatus();
        extensionController.uninstallExtensions();
        migrationController.migrateExtensions();
        extensionController.initPlugins();
        migrationController.migrateSerializedData();
        userController.resetUserStatus();
        scriptController.compileGlobalScripts();

        // disable the velocity logging
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.NullLogSystem");

        eventController.dispatchEvent(new ServerEvent("Server startup"));

        // Start web server before starting the engine in case there is a 
        // problem starting the engine that causes it to hang
        startWebServer();

        configurationController.setStatus(ConfigurationController.STATUS_ENGINE_STARTING);
        startEngine();

        extensionController.startPlugins();

        try {
            alertController.initAlerts();

            configurationController.setStatus(ConfigurationController.STATUS_INITIAL_DEPLOY);
            engineController.startupDeploy();
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
            eventController.dispatchEvent(new ServerEvent("Server shutdown"));
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
            // this disables a "form too large" error for occuring by setting
            // form size to infinite
            System.setProperty("org.eclipse.jetty.server.Request.maxFormContentSize", "0");

            webServer = new Server();

            // add HTTP listener
            SelectChannelConnector connector = new SelectChannelConnector();
            connector.setName("connector");
            connector.setHost(mirthProperties.getString("http.host", "0.0.0.0"));
            connector.setPort(mirthProperties.getInt("http.port"));

            // add HTTPS listener
            SslSelectChannelConnector sslConnector = new SslSelectChannelConnector();
            /*
             * http://www.mirthcorp.com/community/issues/browse/MIRTH-3070 Keep SSL connections
             * alive for 24 hours unless closed by the client. When the Administrator runs on
             * Windows, the SSL handshake performed when a new connection is created takes about 4-5
             * seconds if connecting via IP address and no reverse DNS entry can be found. By
             * keeping the connection alive longer the Administrator shouldn't have to perform the
             * handshake unless idle for this amount of time.
             */
            sslConnector.setMaxIdleTime(86400000);
            // If the number of connections open reaches 200
            sslConnector.setLowResourcesConnections(200);
            // Then close connections after 200 seconds, which is the default MaxIdleTime value. This should affect existing connections as well.
            sslConnector.setLowResourcesMaxIdleTime(200000);
            sslConnector.setName("sslconnector");
            sslConnector.setHost(mirthProperties.getString("https.host", "0.0.0.0"));
            sslConnector.setPort(mirthProperties.getInt("https.port"));

            SslContextFactory contextFactory = sslConnector.getSslContextFactory();
            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            FileInputStream is = new FileInputStream(new File(mirthProperties.getString("keystore.path")));

            try {
                keyStore.load(is, mirthProperties.getString("keystore.storepass").toCharArray());
            } finally {
                IOUtils.closeQuietly(is);
            }

            contextFactory.setKeyStore(keyStore);
            contextFactory.setCertAlias("mirthconnect");
            contextFactory.setKeyManagerPassword(mirthProperties.getString("keystore.keypass"));

            /*
             * We were previously disabling low and medium strength ciphers (MIRTH-1924). However
             * with MIRTH-3492, we're now always specifying an include list everywhere rather than
             * an exclude list.
             */
            contextFactory.setIncludeProtocols(MirthSSLUtil.getEnabledHttpsProtocols(configurationController.getHttpsProtocols()));
            contextFactory.setIncludeCipherSuites(MirthSSLUtil.getEnabledHttpsCipherSuites(configurationController.getHttpsCipherSuites()));

            HandlerList handlers = new HandlerList();
            String contextPath = mirthProperties.getString("http.contextpath", "");

            // Add a starting slash if one does not exist
            if (!contextPath.startsWith("/")) {
                contextPath = "/" + contextPath;
            }

            // Remove a trailing slash if one exists
            if (contextPath.endsWith("/")) {
                contextPath = contextPath.substring(0, contextPath.length() - 1);
            }

            // find the client-lib path
            String clientLibPath = null;

            if (ClassPathResource.getResourceURI("client-lib") != null) {
                clientLibPath = ClassPathResource.getResourceURI("client-lib").getPath() + File.separator;
            } else {
                clientLibPath = ControllerFactory.getFactory().createConfigurationController().getBaseDir() + File.separator + "client-lib" + File.separator;
            }

            // Create the lib context
            ContextHandler libContextHandler = new ContextHandler();
            libContextHandler.setContextPath(contextPath + "/webstart/client-lib");
            libContextHandler.setResourceBase(clientLibPath);
            libContextHandler.setHandler(new ResourceHandler());
            handlers.addHandler(libContextHandler);

            // Create the extensions context
            ContextHandler extensionsContextHandler = new ContextHandler();
            extensionsContextHandler.setContextPath(contextPath + "/webstart/extensions/libs");
            String extensionsPath = new File(ExtensionController.getExtensionsPath()).getPath();
            extensionsContextHandler.setResourceBase(extensionsPath);
            extensionsContextHandler.setHandler(new ResourceHandler());
            handlers.addHandler(extensionsContextHandler);

            // Create the public_html context
            ContextHandler publicContextHandler = new ContextHandler();
            publicContextHandler.setContextPath(contextPath);
            String publicPath = ControllerFactory.getFactory().createConfigurationController().getBaseDir() + File.separator + "public_html";
            publicContextHandler.setResourceBase(publicPath);
            publicContextHandler.setHandler(new ResourceHandler());
            handlers.addHandler(publicContextHandler);

            // Create the javadocs context
            ContextHandler javadocsContextHandler = new ContextHandler();
            javadocsContextHandler.setContextPath(contextPath + "/javadocs");
            String javadocsPath = ControllerFactory.getFactory().createConfigurationController().getBaseDir() + File.separator + "docs" + File.separator + "javadocs";
            javadocsContextHandler.setResourceBase(javadocsPath);
            ResourceHandler javadocsResourceHandler = new ResourceHandler();
            javadocsResourceHandler.setDirectoriesListed(true);
            javadocsContextHandler.setHandler(javadocsResourceHandler);
            handlers.addHandler(javadocsContextHandler);

            // Create a normal servlet handler
            ServletContextHandler servletContextHandler = new ServletContextHandler();
            servletContextHandler.setContextPath(contextPath);
            servletContextHandler.addServlet(new ServletHolder(new WebStartServlet()), "/webstart.jnlp");
            servletContextHandler.addServlet(new ServletHolder(new WebStartServlet()), "/webstart");
            servletContextHandler.addServlet(new ServletHolder(new WebStartServlet()), "/webstart/extensions/*");
            servletContextHandler.setConnectorNames(new String[] { "connector" });
            handlers.addHandler(servletContextHandler);

            // Load all web apps dynamically
            List<WebAppContext> webapps = new ArrayList<WebAppContext>();

            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().endsWith(".war");
                }
            };

            /*
             * If in an IDE, webapps will be on the classpath as a resource. If that's the case, use
             * that directory. Otherwise, use the mirth home directory and append webapps.
             */
            String webappsDir = null;
            if (ClassPathResource.getResourceURI("webapps") != null) {
                webappsDir = ClassPathResource.getResourceURI("webapps").getPath() + File.separator;
            } else {
                webappsDir = ControllerFactory.getFactory().createConfigurationController().getBaseDir() + File.separator + "webapps" + File.separator;
            }

            File[] listOfFiles = new File(webappsDir).listFiles(filter);

            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    logger.debug("webApp File Path: " + file.getAbsolutePath());

                    WebAppContext webapp = new WebAppContext();
                    webapp.setContextPath(contextPath + "/" + file.getName().substring(0, file.getName().length() - 4));

                    logger.debug("webApp Context Path: " + webapp.getContextPath());

                    webapp.setWar(file.getPath());
                    handlers.addHandler(webapp);
                    webapps.add(webapp);
                }
            }

            // Create the ssl servlet handler
            ServletContextHandler sslServletContextHandler = new ServletContextHandler();
            sslServletContextHandler.setMaxFormContentSize(0);
            sslServletContextHandler.setSessionHandler(new SessionHandler());
            sslServletContextHandler.setContextPath(contextPath);

            // Use our special error handler so that we dont have ugly URL
            // encoding
            sslServletContextHandler.addServlet(new ServletHolder(new WebStartServlet()), "/webstart.jnlp");
            sslServletContextHandler.addServlet(new ServletHolder(new WebStartServlet()), "/webstart");
            sslServletContextHandler.addServlet(new ServletHolder(new WebStartServlet()), "/webstart/extensions/*");
            sslServletContextHandler.addServlet(new ServletHolder(new AlertServlet()), "/alerts");
            sslServletContextHandler.addServlet(new ServletHolder(new ChannelServlet()), "/channels");
            sslServletContextHandler.addServlet(new ServletHolder(new ChannelStatisticsServlet()), "/channelstatistics");
            sslServletContextHandler.addServlet(new ServletHolder(new ChannelStatusServlet()), "/channelstatus");
            sslServletContextHandler.addServlet(new ServletHolder(new CodeTemplateServlet()), "/codetemplates");
            sslServletContextHandler.addServlet(new ServletHolder(new ConfigurationServlet()), "/configuration");
            sslServletContextHandler.addServlet(new ServletHolder(new MessageObjectServlet()), "/messages");
            sslServletContextHandler.addServlet(new ServletHolder(new EngineServlet()), "/engine");
            sslServletContextHandler.addServlet(new ServletHolder(new ExtensionServlet()), "/extensions");
            sslServletContextHandler.addServlet(new ServletHolder(new EventServlet()), "/events");
            sslServletContextHandler.addServlet(new ServletHolder(new UserServlet()), "/users");
            sslServletContextHandler.addServlet(new ServletHolder(new UsageServlet()), "/usage");
            sslServletContextHandler.addServlet(new ServletHolder(new DatabaseTaskServlet()), "/databasetasks");
            sslServletContextHandler.setConnectorNames(new String[] { "sslconnector" });
            handlers.addHandler(sslServletContextHandler);

            // add the default handler for misc requests (favicon, etc.)
            DefaultHandler defaultHandler = new DefaultHandler();
            defaultHandler.setServeIcon(false); // don't serve the Jetty favicon
            handlers.addHandler(defaultHandler);

            webServer.setHandler(handlers);
            webServer.setConnectors(new Connector[] { connector, sslConnector });
            try {
                webServer.start();
            } catch (Throwable e) {
                logger.error("Could not load web app", e);
                try {
                    webServer.stop();
                } catch (Throwable t) {
                    // Ignore exception stopping
                }
                for (WebAppContext webapp : webapps) {
                    handlers.removeHandler(webapp);
                }
                webServer.start();
            }
            logger.debug("started jetty web server on ports: " + connector.getPort() + ", " + sslConnector.getPort());
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
    }

    private class UsageSenderTask extends TimerTask {
        @Override
        public void run() {
            boolean isSent = ConnectServiceUtil.sendStatistics(configurationController.getServerId(), configurationController.getServerVersion(), true, usageController.createUsageStats());
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

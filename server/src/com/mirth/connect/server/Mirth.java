/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
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

import com.mirth.connect.model.Event;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ChannelStatisticsController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.controllers.MessageObjectController;
import com.mirth.connect.server.controllers.MigrationController;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.controllers.UserController;
import com.mirth.connect.server.servlets.AlertServlet;
import com.mirth.connect.server.servlets.ChannelServlet;
import com.mirth.connect.server.servlets.ChannelStatisticsServlet;
import com.mirth.connect.server.servlets.ChannelStatusServlet;
import com.mirth.connect.server.servlets.CodeTemplateServlet;
import com.mirth.connect.server.servlets.ConfigurationServlet;
import com.mirth.connect.server.servlets.EngineServlet;
import com.mirth.connect.server.servlets.EventServlet;
import com.mirth.connect.server.servlets.ExtensionServlet;
import com.mirth.connect.server.servlets.MessageObjectServlet;
import com.mirth.connect.server.servlets.UserServlet;
import com.mirth.connect.server.servlets.WebStartServlet;
import com.mirth.connect.server.tools.ClassPathResource;
import com.mirth.connect.server.util.ResourceUtil;

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
    private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
    private ChannelStatisticsController channelStatisticsController = ControllerFactory.getFactory().createChannelStatisticsController();
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
    private MigrationController migrationController = ControllerFactory.getFactory().createMigrationController();
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();

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
            boolean httpPort = testPort(mirthProperties.getString("http.port"), "http.port");
            boolean httpsPort = testPort(mirthProperties.getString("https.port"), "https.port");
            boolean jmxPort = testPort(mirthProperties.getString("jmx.port"), "jmx.port");

            if (!httpPort || !httpsPort || !jmxPort) {
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
     * Returns true if the resources required by the server have been
     * successfully loaded
     * 
     * @return true if the resources required by the server have been
     *         successfully loaded
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
        configurationController.initializeSecuritySettings();
        migrationController.migrate();
        extensionController.loadExtensions();
        messageObjectController.removeAllFilterTables();
        eventController.removeAllFilterTables();
        extensionController.uninstallExtensions();
        migrationController.migrateExtensions();
        extensionController.initPlugins();
        channelStatisticsController.loadCache();
        channelStatisticsController.startUpdaterThread();
        channelController.loadCache();
        migrationController.migrateChannels();
        userController.resetUserStatus();
        extensionController.startPlugins();
        scriptController.compileGlobalScripts();

        // disable the velocity logging
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.NullLogSystem");
        
        eventController.addEvent(new Event("Server startup"));
        
        // Start web server before starting the engine in case there is a 
        // problem starting the engine that causes it to hang
        startWebServer();
        startEngine();
        printSplashScreen();
        
    }

    /**
     * Shuts down the server.
     * 
     */
    public void shutdown() {
        logger.info("shutting down mirth due to normal request");
        
        stopEngine();
        
        // Add event after stopping the engine, but before stopping the plugins
        eventController.addEvent(new Event("Server shutdown"));
        
        stopWebServer();
        extensionController.stopPlugins();
        // Stats updater thread will update the stats one more time before
        // stopping
        channelStatisticsController.stopUpdaterThread();
        stopDatabase();
        running = false;
    }

    /**
     * Starts the engine.
     * 
     */
    private void startEngine() {
        logger.debug("starting engine");
        configurationController.setEngineStarting(true);

        try {
            engineController.startEngine();
        } catch (Exception e) {
            logger.error(e);
        }

        configurationController.setEngineStarting(false);
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
            connector.setHost(mirthProperties.getString("http.host", "0.0.0.0"));
            connector.setPort(mirthProperties.getInt("http.port"));
            connector.setName("connector");

            // add HTTPS listener
            SslSelectChannelConnector sslConnector = new SslSelectChannelConnector();
            sslConnector.setHost(mirthProperties.getString("https.host", "0.0.0.0"));
            sslConnector.setPort(mirthProperties.getInt("https.port"));
            sslConnector.setKeystore(mirthProperties.getString("keystore.path"));
            sslConnector.setPassword(mirthProperties.getString("keystore.storepass"));
            sslConnector.setKeyPassword(mirthProperties.getString("keystore.keypass"));
            sslConnector.setSslKeyManagerFactoryAlgorithm(mirthProperties.getString("keystore.algorithm"));
            sslConnector.setKeystoreType(mirthProperties.getString("keystore.storetype"));
            sslConnector.setName("sslconnector");
            // Disabling low and medium strength cipers (see MIRTH-1924)
            sslConnector.setExcludeCipherSuites(new String[] { "EXP-EDH-DSS-DES-CBC-SHA", "EDH-DSS-DES-CBC-SHA" });

            HandlerList handlers = new HandlerList();
            String contextPath = mirthProperties.getString("http.contextpath");

            // find the client-lib path
            String clientLibPath = null;
            
            if (ClassPathResource.getResourceURI("client-lib") != null) {
                clientLibPath = ClassPathResource.getResourceURI("client-lib").getPath() + File.separator;
            } else {
                clientLibPath = ControllerFactory.getFactory().createConfigurationController().getBaseDir() + File.separator + "client-lib" + File.separator;
            }
            
            // Create the lib context
            ContextHandler libContextHandler = new ContextHandler();
            libContextHandler.setContextPath(contextPath + "webstart/client-lib");
            libContextHandler.setResourceBase(clientLibPath);
            libContextHandler.setHandler(new ResourceHandler());
            handlers.addHandler(libContextHandler);

            // Create the extensions context
            ContextHandler extensionsContextHandler = new ContextHandler();
            extensionsContextHandler.setContextPath(contextPath + "webstart/extensions/libs");
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

            // Create a normal servlet handler
            ServletContextHandler servletContextHandler = new ServletContextHandler();
            servletContextHandler.setContextPath(contextPath);
            servletContextHandler.addServlet(new ServletHolder(new WebStartServlet()), "/webstart.jnlp");
            servletContextHandler.addServlet(new ServletHolder(new WebStartServlet()), "/webstart");
            servletContextHandler.addServlet(new ServletHolder(new WebStartServlet()), "/webstart/extensions/*");
            servletContextHandler.setConnectorNames(new String[] { "connector" });
            handlers.addHandler(servletContextHandler);
            
            // Create the ssl servlet handler
            ServletContextHandler sslServletContextHandler = new ServletContextHandler();
            sslServletContextHandler.setMaxFormContentSize(0);
            sslServletContextHandler.setSessionHandler(new SessionHandler());
            sslServletContextHandler.setContextPath(contextPath);

            // Use our special error handler so that we dont have ugly URL
            // encoding
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
            sslServletContextHandler.setConnectorNames(new String[] { "sslconnector" });
            handlers.addHandler(sslServletContextHandler);
            
            // add the default handler for misc requests (favicon, etc.)
            handlers.addHandler(new DefaultHandler());
            
            webServer.setHandler(handlers);
            webServer.setConnectors(new Connector[] { connector, sslConnector });
            webServer.start();

            logger.debug("started jetty web server on ports: " + connector.getPort() + ", " + sslConnector.getPort());
        } catch (Exception e) {
            logger.warn("Could not start web server.", e);
        }
    }

    /**
     * Stops the web server.
     * 
     */
    private void stopWebServer() {
        logger.debug("stopping jetty web server");

        try {
            webServer.stop();
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
     * Displays the splash screen information, including the server version and
     * build date, to the system console.
     * 
     */
    private void printSplashScreen() {
        logger.info("Mirth Connect " + versionProperties.getString("mirth.version") + " (Built on " + versionProperties.getString("mirth.date") + ") server successfully started.");
        logger.info("This product was developed by Mirth Corporation (http://www.mirthcorp.com) and its contributors (c)2005-" + Calendar.getInstance().get(Calendar.YEAR) + ".");
        logger.info("Running " + System.getProperty("java.vm.name") + " " + System.getProperty("java.version") + " on " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ", " + System.getProperty("os.arch") + "), " + configurationController.getDatabaseType() + ", with charset " + Charset.defaultCharset() + ".");
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
    private boolean testPort(String port, String name) {
        ServerSocket socket = null;

        try {
            socket = new ServerSocket(Integer.parseInt(port));
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
    }
}

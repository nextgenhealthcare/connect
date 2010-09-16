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
import java.io.OutputStream;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.http.SslListener;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.jetty.servlet.ServletHandler;

import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ChannelStatisticsController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.controllers.MessageObjectController;
import com.mirth.connect.server.controllers.MigrationController;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.controllers.UserController;
import com.mirth.connect.server.servlets.MirthErrorPageHandler;
import com.mirth.connect.util.PropertyLoader;

/**
 * Instantiate a Mirth server that listens for commands from the CommandQueue.
 * 
 */
public class Mirth extends Thread {
    private Logger logger = Logger.getLogger(this.getClass());
    private boolean running = false;
    private Properties mirthProperties = null;
    private Properties versionProperties = null;
    private HttpServer httpServer = null;
    private HttpServer servletContainer = null;
    private CommandQueue commandQueue = CommandQueue.getInstance();
    private EngineController engineController = ControllerFactory.getFactory().createEngineController();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private UserController userController = ControllerFactory.getFactory().createUserController();
    private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
    private ChannelStatisticsController channelStatisticsController = ControllerFactory.getFactory().createChannelStatisticsController();
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
    private MigrationController migrationController = ControllerFactory.getFactory().createMigrationController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
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
        if (initResources()) {
            logger.debug("starting mirth server...");

            // Check the ports to see if they are already in use
            boolean httpPort = testPort(PropertyLoader.getProperty(mirthProperties, "http.port"), "http.port");
            boolean httpsPort = testPort(PropertyLoader.getProperty(mirthProperties, "https.port"), "https.port");
            boolean jmxPort = testPort(PropertyLoader.getProperty(mirthProperties, "jmx.port"), "jmx.port");

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
        mirthProperties = PropertyLoader.loadProperties("mirth");
        versionProperties = PropertyLoader.loadProperties("version");

        return (mirthProperties != null);
    }

    /**
     * Starts up the server.
     * 
     */
    public void startup() {
        startWebServer();
        extensionController.loadExtensions();
        migrationController.migrate();
        messageObjectController.removeAllFilterTables();
        eventController.removeAllFilterTables();
        extensionController.uninstallExtensions();
        migrationController.migrateExtensions();
        extensionController.initPlugins();
        channelStatisticsController.loadCache();
        channelStatisticsController.startUpdaterThread();
        channelController.loadCache();
        migrationController.migrateChannels();
        configurationController.loadEncryptionKey();
        userController.resetUserStatus();
        monitoringController.initPlugins();
        extensionController.startPlugins();
        scriptController.compileGlobalScripts();

        // disable the velocity logging
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.NullLogSystem");
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
            // this disables validaiton of the web.xml file
            // which causes exceptions when Mirth is run
            // behind a firewall and the resources cannot be
            // accessed
            System.setProperty("org.mortbay.xml.XmlParser.NotValidating", "true");
            // this disables a "form too large" error for occuring by setting
            // form size to infinite
            System.setProperty("org.mortbay.http.HttpRequest.maxFormContentSize", "0");

            httpServer = new HttpServer();
            servletContainer = new HttpServer();

            // add HTTPS listener
            SslListener sslListener = new SslListener();
            sslListener.setHost(PropertyLoader.getProperty(mirthProperties, "https.host", "0.0.0.0"));
            sslListener.setCipherSuites(new String[] { "SSL_RSA_WITH_RC4_128_MD5", "SSL_RSA_WITH_RC4_128_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA" });
            sslListener.setPort(Integer.valueOf(PropertyLoader.getProperty(mirthProperties, "https.port")).intValue());
            sslListener.setKeystore(ControllerFactory.getFactory().createConfigurationController().getBaseDir() + File.separator + PropertyLoader.getProperty(mirthProperties, "https.keystore"));
            sslListener.setPassword(PropertyLoader.getProperty(mirthProperties, "https.password"));
            sslListener.setKeyPassword(PropertyLoader.getProperty(mirthProperties, "https.keypassword"));
            sslListener.setAlgorithm(PropertyLoader.getProperty(mirthProperties, "https.algorithm"));
            sslListener.setKeystoreType(PropertyLoader.getProperty(mirthProperties, "https.keystoretype"));
            servletContainer.addListener(sslListener);

            // add HTTP listener
            SocketListener listener = new SocketListener();
            listener.setHost(PropertyLoader.getProperty(mirthProperties, "http.host", "0.0.0.0"));
            listener.setPort(Integer.valueOf(PropertyLoader.getProperty(mirthProperties, "http.port")).intValue());
            httpServer.addListener(listener);

            // Load the context path property and remove the last char if it is
            // a '/'.
            String contextPath = PropertyLoader.getProperty(mirthProperties, "context.path");

            if (contextPath.endsWith("/")) {
                contextPath = contextPath.substring(0, contextPath.length() - 1);
            }

            // Create the lib context
            HttpContext libContext = new HttpContext();
            libContext.setContextPath(contextPath + "/client-lib/");
            httpServer.addContext(libContext);

            // Serve static content from the lib context
            File extensions = new File(ExtensionController.getExtensionsPath());
            String libPath = ControllerFactory.getFactory().createConfigurationController().getBaseDir() + File.separator + "client-lib";

            libContext.setResourceBase(libPath);
            libContext.addHandler(new ResourceHandler());

            // Create the extensions context
            HttpContext extensionsContext = new HttpContext();
            extensionsContext.setContextPath(contextPath + "/extensions/");
            httpServer.addContext(extensionsContext);

            // Serve static content from the extensions context
            String extensionsPath = extensions.getPath();
            extensionsContext.setResourceBase(extensionsPath);
            extensionsContext.addHandler(new ResourceHandler());

            // Create the public_html context
            HttpContext publicContext = new HttpContext();
            publicContext.setContextPath(contextPath + "/");
            httpServer.addContext(publicContext);

            String publicPath = ControllerFactory.getFactory().createConfigurationController().getBaseDir() + File.separator + "public_html";
            publicContext.setResourceBase(publicPath);
            publicContext.addHandler(new ResourceHandler());

            // Create a normal servlet container
            ServletHandler servlets = new ServletHandler();
            HttpContext servletContext = new HttpContext();
            servletContext.setContextPath(contextPath + "/");
            servletContext.addHandler(servlets);
            httpServer.addContext(servletContext);
            servlets.addServlet("WebStart", "/webstart.jnlp", "com.mirth.connect.server.servlets.WebStartServlet");
            // Servlets for backwards compatibility
            servlets.addServlet("WebStart", "/webstart", "com.mirth.connect.server.servlets.WebStartServlet");

            // Create a secure servlet container
            ServletHandler secureServlets = new ServletHandler();
            HttpContext secureServletContext = new HttpContext();
            secureServletContext.setContextPath(contextPath + "/");
            secureServletContext.addHandler(secureServlets);
            servletContainer.addContext(secureServletContext);

            // Use our special error handler so that we dont have ugly URL
            // encoding
            secureServletContext.setAttribute(HttpContext.__ErrorHandler, new MirthErrorPageHandler());

            // Map a servlet onto the container
            secureServlets.addServlet("Alerts", "/alerts", "com.mirth.connect.server.servlets.AlertServlet");
            secureServlets.addServlet("Channels", "/channels", "com.mirth.connect.server.servlets.ChannelServlet");
            secureServlets.addServlet("ChannelStatistics", "/channelstatistics", "com.mirth.connect.server.servlets.ChannelStatisticsServlet");
            secureServlets.addServlet("ChannelStatus", "/channelstatus", "com.mirth.connect.server.servlets.ChannelStatusServlet");
            secureServlets.addServlet("CodeTemplates", "/codetemplates", "com.mirth.connect.server.servlets.CodeTemplateServlet");
            secureServlets.addServlet("Configuration", "/configuration", "com.mirth.connect.server.servlets.ConfigurationServlet");
            secureServlets.addServlet("MessageObject", "/messages", "com.mirth.connect.server.servlets.MessageObjectServlet");
            secureServlets.addServlet("Engine", "/engine", "com.mirth.connect.server.servlets.EngineServlet");
            secureServlets.addServlet("Extensions", "/extensions", "com.mirth.connect.server.servlets.ExtensionServlet");
            secureServlets.addServlet("SystemEvent", "/events", "com.mirth.connect.server.servlets.SystemEventServlet");
            secureServlets.addServlet("Users", "/users", "com.mirth.connect.server.servlets.UserServlet");

            // start the web server
            httpServer.start();
            servletContainer.start();

            logger.debug("started jetty web server on ports: " + listener.getPort() + ", " + sslListener.getPort());
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
            httpServer.stop();
            servletContainer.stop();
        } catch (Exception e) {
            logger.warn("Could not stop web server.", e);
        }
    }

    private void stopDatabase() {
        String database = PropertyLoader.getProperty(mirthProperties, "database");

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
        String version = PropertyLoader.getProperty(versionProperties, "mirth.version");
        String buildDate = PropertyLoader.getProperty(versionProperties, "mirth.date");

        logger.info("Mirth Connect " + version + " (" + buildDate + ") server successfully started: " + (new Date()).toString());
        logger.info("This product was developed by Mirth Corporation (http://www.mirthcorp.com) and its contributors (c)2005-" + Calendar.getInstance().get(Calendar.YEAR) + ".");
        logger.info("Running " + System.getProperty("java.vm.name") + " " + System.getProperty("java.version") + " on " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ", " + System.getProperty("os.arch") + ") with charset " + Charset.defaultCharset() + ".");
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
}

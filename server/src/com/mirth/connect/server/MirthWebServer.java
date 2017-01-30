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
import java.lang.annotation.Annotation;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.ws.rs.ext.Provider;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.LowResourceMonitor;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.wadl.internal.generators.WadlGeneratorJAXBGrammarGenerator;
import org.glassfish.jersey.servlet.ServletContainer;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.mirth.connect.client.core.Version;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.Replaces;
import com.mirth.connect.model.ApiProvider;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.api.providers.ApiOriginFilter;
import com.mirth.connect.server.api.providers.ClickjackingFilter;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.servlets.SwaggerServlet;
import com.mirth.connect.server.servlets.WebStartServlet;
import com.mirth.connect.server.tools.ClassPathResource;
import com.mirth.connect.server.util.PackagePredicate;
import com.mirth.connect.util.MirthSSLUtil;

public class MirthWebServer extends Server {

    private static final String CONNECTOR = "connector";
    private static final String CONNECTOR_SSL = "sslconnector";

    private Logger logger = Logger.getLogger(getClass());
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
    private List<WebAppContext> webapps;
    private HandlerList handlers;
    private ServerConnector connector;
    private ServerConnector sslConnector;

    public MirthWebServer(PropertiesConfiguration mirthProperties) throws Exception {
        // this disables a "form too large" error for occuring by setting
        // form size to infinite
        System.setProperty("org.eclipse.jetty.server.Request.maxFormContentSize", "0");

        // Suppress logging from the WADL generator for OPTIONS requests 
        Logger.getLogger(WadlGeneratorJAXBGrammarGenerator.class).setLevel(Level.OFF);

        String baseAPI = "/api";
        boolean apiAllowHTTP = Boolean.parseBoolean(mirthProperties.getString("server.api.allowhttp", "false"));

        // add HTTP listener
        connector = new ServerConnector(this);
        connector.setName(CONNECTOR);
        connector.setHost(mirthProperties.getString("http.host", "0.0.0.0"));
        connector.setPort(mirthProperties.getInt("http.port"));

        // add HTTPS listener
        sslConnector = createSSLConnector(CONNECTOR_SSL, mirthProperties);

        handlers = new HandlerList();
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

        // Load all web apps dynamically
        webapps = new ArrayList<WebAppContext>();

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
            // Since webapps may use JSP and JSTL, we need to enable the AnnotationConfiguration in order to correctly set up the JSP container.
            Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(this);
            classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration");

            for (File file : listOfFiles) {
                logger.debug("webApp File Path: " + file.getAbsolutePath());

                WebAppContext webapp = new WebAppContext();
                webapp.setContextPath(contextPath + "/" + file.getName().substring(0, file.getName().length() - 4));

                /*
                 * Set the ContainerIncludeJarPattern so that Jetty examines these JARs for TLDs,
                 * web fragments, etc. If you omit the jar that contains the JSTL TLDs, the JSP
                 * engine will scan for them instead.
                 */
                webapp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*/[^/]*(javax\\.servlet-api|taglibs)[^/]*\\.jar$");

                logger.debug("webApp Context Path: " + webapp.getContextPath());

                webapp.setWar(file.getPath());
                handlers.addHandler(webapp);
                webapps.add(webapp);
            }
        }

        // TODO: Fully support backward compatibility for models before exposing earlier servlets
        addApiServlets(handlers, contextPath, baseAPI, apiAllowHTTP, Version.getLatest(), mirthProperties);
        // Add Jersey API / swagger servlets for each specific version
//        Version version = Version.getApiEarliest();
//        while (version != null) {
//            addApiServlets(handlers, contextPath, baseAPI, apiAllowHTTP, version, mirthProperties);
//            version = version.getNextVersion();
//        }
        // Add servlets for the main (default) API endpoint
        addApiServlets(handlers, contextPath, baseAPI, apiAllowHTTP, null, mirthProperties);

        // Create the webstart servlet handler
        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(contextPath);
        servletContextHandler.addFilter(new FilterHolder(new MethodFilter()), "/*", EnumSet.of(DispatcherType.REQUEST));
        servletContextHandler.addServlet(new ServletHolder(new WebStartServlet()), "/webstart.jnlp");
        servletContextHandler.addServlet(new ServletHolder(new WebStartServlet()), "/webstart");
        servletContextHandler.addServlet(new ServletHolder(new WebStartServlet()), "/webstart/extensions/*");
        handlers.addHandler(servletContextHandler);

        // add the default handler for misc requests (favicon, etc.)
        DefaultHandler defaultHandler = new DefaultHandler();
        defaultHandler.setServeIcon(false); // don't serve the Jetty favicon
        handlers.addHandler(defaultHandler);

        setHandler(handlers);
        setConnectors(new Connector[] { connector, sslConnector });
    }

    public void startup() throws Exception {
        try {
            start();
        } catch (Throwable e) {
            logger.error("Could not load web app", e);
            try {
                stop();
            } catch (Throwable t) {
                // Ignore exception stopping
            }
            for (WebAppContext webapp : webapps) {
                handlers.removeHandler(webapp);
            }
            start();
        }
        logger.debug("started jetty web server on ports: " + connector.getPort() + ", " + sslConnector.getPort());
    }

    private ServerConnector createSSLConnector(String name, PropertiesConfiguration mirthProperties) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        FileInputStream is = new FileInputStream(new File(mirthProperties.getString("keystore.path")));
        try {
            keyStore.load(is, mirthProperties.getString("keystore.storepass").toCharArray());
        } finally {
            IOUtils.closeQuietly(is);
        }

        SslContextFactory contextFactory = new SslContextFactory();
        contextFactory.setKeyStore(keyStore);
        contextFactory.setCertAlias("mirthconnect");
        contextFactory.setKeyManagerPassword(mirthProperties.getString("keystore.keypass"));

        HttpConfiguration config = new HttpConfiguration();
        config.setSecureScheme("https");
        config.setSecurePort(mirthProperties.getInt("https.port"));
        config.addCustomizer(new SecureRequestCustomizer());

        ServerConnector sslConnector = new ServerConnector(this, new SslConnectionFactory(contextFactory, HttpVersion.HTTP_1_1.asString()), new HttpConnectionFactory(config));

        /*
         * http://www.mirthcorp.com/community/issues/browse/MIRTH-3070 Keep SSL connections alive
         * for 24 hours unless closed by the client. When the Administrator runs on Windows, the SSL
         * handshake performed when a new connection is created takes about 4-5 seconds if
         * connecting via IP address and no reverse DNS entry can be found. By keeping the
         * connection alive longer the Administrator shouldn't have to perform the handshake unless
         * idle for this amount of time.
         */
        sslConnector.setIdleTimeout(86400000);

        LowResourceMonitor lowResourceMonitor = new LowResourceMonitor(this);
        lowResourceMonitor.setMonitoredConnectors(Collections.singleton((Connector) sslConnector));
        // If the number of connections open reaches 200
        lowResourceMonitor.setMaxConnections(200);
        // Then close connections after 200 seconds, which is the default MaxIdleTime value. This should affect existing connections as well.
        lowResourceMonitor.setLowResourcesIdleTimeout(200000);

        sslConnector.setName(name);
        sslConnector.setHost(mirthProperties.getString("https.host", "0.0.0.0"));
        sslConnector.setPort(mirthProperties.getInt("https.port"));

        /*
         * We were previously disabling low and medium strength ciphers (MIRTH-1924). However with
         * MIRTH-3492, we're now always specifying an include list everywhere rather than an exclude
         * list.
         */
        contextFactory.setIncludeProtocols(MirthSSLUtil.getEnabledHttpsProtocols(configurationController.getHttpsServerProtocols()));
        contextFactory.setIncludeCipherSuites(MirthSSLUtil.getEnabledHttpsCipherSuites(configurationController.getHttpsCipherSuites()));

        return sslConnector;
    }

    private void addApiServlets(HandlerList handlers, String contextPath, String baseAPI, boolean apiAllowHTTP, Version version, PropertiesConfiguration mirthProperties) {
        String apiPath = "";
        Version apiVersion = version;
        if (apiVersion != null) {
            apiPath += "/" + apiVersion.toString();
        } else {
            apiVersion = Version.getLatest();
        }

        // Create the servlet handler for the API
        ServletContextHandler apiServletContextHandler = new ServletContextHandler();
        apiServletContextHandler.setMaxFormContentSize(0);
        apiServletContextHandler.setSessionHandler(new SessionHandler());
        apiServletContextHandler.setContextPath(contextPath + baseAPI + apiPath);
        apiServletContextHandler.addFilter(new FilterHolder(new ApiOriginFilter(mirthProperties)), "/*", EnumSet.of(DispatcherType.REQUEST));
        apiServletContextHandler.addFilter(new FilterHolder(new ClickjackingFilter(mirthProperties)), "/*", EnumSet.of(DispatcherType.REQUEST));
        apiServletContextHandler.addFilter(new FilterHolder(new MethodFilter()), "/*", EnumSet.of(DispatcherType.REQUEST));
        setConnectorNames(apiServletContextHandler, apiAllowHTTP);

        ApiProviders apiProviders = getApiProviders(apiVersion);

        // Add versioned Jersey API servlet
        ServletHolder jerseyVersionedServlet = apiServletContextHandler.addServlet(ServletContainer.class, "/*");
        jerseyVersionedServlet.setInitOrder(1);
        jerseyVersionedServlet.setInitParameter(ServerProperties.PROVIDER_PACKAGES, StringUtils.join(apiProviders.providerPackages, ','));
        jerseyVersionedServlet.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, joinClasses(apiProviders.providerClasses));

        // Add versioned Swagger bootstrap configuration servlet
        ServletHolder swaggerVersionedServlet = new ServletHolder(new SwaggerServlet(contextPath + baseAPI + apiPath, version, apiVersion, apiProviders.servletInterfacePackages, apiProviders.servletInterfaces, apiAllowHTTP));
        swaggerVersionedServlet.setInitOrder(2);
        apiServletContextHandler.addServlet(swaggerVersionedServlet, "/swagger*");

        // Add Swagger UI web page servlet
        handlers.addHandler(getSwaggerContextHandler(contextPath, baseAPI, apiAllowHTTP, version));
        // Add API handler
        handlers.addHandler(apiServletContextHandler);
    }

    private ContextHandler getSwaggerContextHandler(String contextPath, String baseAPI, boolean apiAllowHTTP, Version version) {
        ContextHandler swaggerContextHandler = new ContextHandler();
        swaggerContextHandler.setContextPath(contextPath + baseAPI + (version != null ? "/" + version.toString() : ""));
        swaggerContextHandler.setResourceBase(ControllerFactory.getFactory().createConfigurationController().getBaseDir() + File.separator + "public_api_html");
        swaggerContextHandler.setHandler(new ResourceHandler());
        setConnectorNames(swaggerContextHandler, apiAllowHTTP);
        return swaggerContextHandler;
    }

    private void setConnectorNames(ContextHandler contextHandler, boolean apiAllowHTTP) {
        List<String> connectorNames = new ArrayList<String>();
        connectorNames.add("@" + CONNECTOR_SSL);
        if (apiAllowHTTP) {
            connectorNames.add("@" + CONNECTOR);
        }
        contextHandler.setVirtualHosts(connectorNames.toArray(new String[connectorNames.size()]));
    }

    private class ApiProviders {
        public Set<String> servletInterfacePackages;
        public Set<Class<?>> servletInterfaces;
        public Set<String> providerPackages;
        public Set<Class<?>> providerClasses;

        public ApiProviders(Set<String> servletInterfacePackages, Set<Class<?>> servletInterfaces, Set<String> providerPackages, Set<Class<?>> providerClasses) {
            this.servletInterfacePackages = servletInterfacePackages;
            this.servletInterfaces = servletInterfaces;
            this.providerPackages = providerPackages;
            this.providerClasses = providerClasses;
        }
    }

    private ApiProviders getApiProviders(Version version) {
        // These contain only the shared servlet interfaces, and will be used to generate the Swagger models.
        Set<String> servletInterfacePackages = new LinkedHashSet<String>();
        Set<Class<?>> servletInterfaces = new LinkedHashSet<Class<?>>();
        servletInterfaces.addAll(getApiClassesForVersion("com.mirth.connect.client.core.api.servlets", version, new Class<?>[] {
                BaseServletInterface.class }, new Class<?>[0]));

        // These are JAX-RS providers that should be shared on the client and server.
        Set<String> coreProviderPackages = new LinkedHashSet<String>();
        Set<Class<?>> coreProviderClasses = new LinkedHashSet<Class<?>>();
        coreProviderClasses.addAll(getApiClassesForVersion("com.mirth.connect.client.core.api.providers", version, new Class<?>[0], new Class<?>[] {
                Provider.class }));
        coreProviderClasses.add(MultiPartFeature.class);

        /*
         * These are JAX-RS providers that are on the server side only. Servlet implementation
         * classes should be added directly to the class set, as JAX-RS does not scan for subclasses
         * of a parent class that has provider annotations.
         */
        Set<String> serverProviderPackages = new LinkedHashSet<String>();
        serverProviderPackages.add("io.swagger.jaxrs.listing");
        Set<Class<?>> serverProviderClasses = new LinkedHashSet<Class<?>>();
        serverProviderClasses.addAll(getApiClassesForVersion("com.mirth.connect.server.api.providers", version, new Class<?>[0], new Class<?>[] {
                Provider.class }));
        serverProviderClasses.addAll(getApiClassesForVersion("com.mirth.connect.server.api.servlets", version, new Class<?>[] {
                MirthServlet.class }, new Class<?>[0]));

        // Add JAX-RS providers from extensions
        for (MetaData metaData : CollectionUtils.union(extensionController.getPluginMetaData().values(), extensionController.getConnectorMetaData().values())) {
            for (ApiProvider apiProvider : metaData.getApiProviders(version)) {
                try {
                    switch (apiProvider.getType()) {
                        case SERVLET_INTERFACE_PACKAGE:
                            servletInterfacePackages.add(apiProvider.getName());
                            break;
                        case SERVLET_INTERFACE:
                            servletInterfaces.add(Class.forName(apiProvider.getName()));
                            break;
                        case CORE_PACKAGE:
                            coreProviderPackages.add(apiProvider.getName());
                            break;
                        case SERVER_PACKAGE:
                            serverProviderPackages.add(apiProvider.getName());
                            break;
                        case CORE_CLASS:
                            coreProviderClasses.add(Class.forName(apiProvider.getName()));
                            break;
                        case SERVER_CLASS:
                            serverProviderClasses.add(Class.forName(apiProvider.getName()));
                            break;
                    }
                } catch (Throwable t) {
                    logger.error("Error adding API provider to web server: " + apiProvider);
                }
            }
        }

        Set<String> providerPackages = new LinkedHashSet<String>();
        Set<Class<?>> providerClasses = new LinkedHashSet<Class<?>>();
        providerPackages.addAll(coreProviderPackages);
        providerPackages.addAll(serverProviderPackages);
        providerClasses.addAll(coreProviderClasses);
        providerClasses.addAll(serverProviderClasses);

        return new ApiProviders(servletInterfacePackages, servletInterfaces, providerPackages, providerClasses);
    }

    private Set<Class<?>> getApiClassesForVersion(String packageName, Version version, Class<?>[] baseClasses, Class<?>[] annotations) {
        // If it's the latest version always use the default package
        if (version == Version.getLatest()) {
            return getClassesInPackage(packageName, baseClasses, annotations);
        }

        /*
         * First, see if there are any versioned packages ahead of the given version. If so, then we
         * know we're not going to be using the default package.
         */
        Version testVersion = version.getNextVersion();
        boolean useDefaultPackage = true;
        while (testVersion != null) {
            if (testPackageVersion(packageName, testVersion, baseClasses, annotations)) {
                useDefaultPackage = false;
                break;
            }
            testVersion = testVersion.getNextVersion();
        }
        if (useDefaultPackage) {
            return getClassesInPackage(packageName, baseClasses, annotations);
        }

        /*
         * At this point we know we have to use an older version of the package. So start at the
         * beginning and work forwards, replacing classes as needed.
         */
        Set<Class<?>> classes = new HashSet<Class<?>>();
        testVersion = Version.getApiEarliest();
        while (testVersion != null && testVersion.ordinal() <= version.ordinal()) {
            for (Class<?> clazz : getClassesInPackage(getVersionedPackageName(packageName, testVersion), baseClasses, annotations)) {
                Replaces replaces = clazz.getAnnotation(Replaces.class);
                if (replaces != null) {
                    classes.remove(replaces.value());
                }
                classes.add(clazz);
            }

            testVersion = testVersion.getNextVersion();
        }
        return classes;
    }

    @SuppressWarnings("unchecked")
    private Set<Class<?>> getClassesInPackage(String packageName, Class<?>[] baseClasses, Class<?>[] annotations) {
        ConfigurationBuilder config = new ConfigurationBuilder();
        config.setScanners(new ResourcesScanner(), new TypeAnnotationsScanner(), new SubTypesScanner(false));
        config.addUrls(ClasspathHelper.forPackage(packageName));
        config.setInputsFilter(new PackagePredicate(packageName));
        Reflections reflections = new Reflections(config);

        Set<Class<?>> classes = new HashSet<Class<?>>();
        if (ArrayUtils.isNotEmpty(baseClasses)) {
            for (Class<?> baseClass : baseClasses) {
                classes.addAll(reflections.getSubTypesOf(baseClass));
            }
        }
        if (ArrayUtils.isNotEmpty(annotations)) {
            for (Class<?> annotation : annotations) {
                if (annotation.isAnnotation()) {
                    classes.addAll(reflections.getTypesAnnotatedWith((Class<? extends Annotation>) annotation));
                }
            }
        }
        return classes;
    }

    private boolean testPackageVersion(String packageName, Version version, Class<?>[] baseClasses, Class<?>[] annotations) {
        packageName = getVersionedPackageName(packageName, version);
        try {
            // Look for package-info.java first
            Class.forName(packageName + ".package-info");
            return true;
        } catch (ClassNotFoundException e) {
        }
        return CollectionUtils.isNotEmpty(getClassesInPackage(packageName, baseClasses, annotations));
    }

    private String getVersionedPackageName(String packageName, Version version) {
        return packageName + "." + version.toPackageString();
    }

    private String joinClasses(Set<Class<?>> classes) {
        StringBuilder builder = new StringBuilder();

        if (CollectionUtils.isNotEmpty(classes)) {
            boolean added = false;
            for (Class<?> clazz : classes) {
                if (clazz != null) {
                    String name = clazz.getCanonicalName();
                    if (name != null) {
                        if (added) {
                            builder.append(',');
                        }
                        builder.append(name);
                        added = true;
                    }
                }
            }
        }

        return builder.toString();
    }
}
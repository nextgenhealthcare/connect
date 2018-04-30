/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.util.Arrays;
import org.eclipse.jetty.io.RuntimeIOException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.mirth.connect.model.ExtensionLibrary;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.ResourceUtil;
import com.mirth.connect.util.MirthSSLUtil;

public class WebStartServlet extends HttpServlet {
    private Logger logger = Logger.getLogger(this.getClass());
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

    /*
     * Override last modified time to always be modified so it updates changes to JNLP.
     */
    @Override
    protected long getLastModified(HttpServletRequest arg0) {
        return System.currentTimeMillis();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // MIRTH-1745
        response.setCharacterEncoding("UTF-8");

        try {
            response.setContentType("application/x-java-jnlp-file");
            response.setHeader("Pragma", "no-cache");
            PrintWriter out = response.getWriter();
            Document jnlpDocument = null;

            if (request.getServletPath().equals("/webstart.jnlp") || request.getServletPath().equals("/webstart")) {
                jnlpDocument = getAdministratorJnlp(request);
            } else if (request.getServletPath().equals("/webstart/extensions")) {
                String extensionPath = StringUtils.removeEnd(StringUtils.removeStart(request.getPathInfo(), "/"), ".jnlp");
                jnlpDocument = getExtensionJnlp(extensionPath);
            }

            DocumentSerializer docSerializer = new DocumentSerializer(true);
            docSerializer.toXML(jnlpDocument, out);
        } catch (RuntimeIOException rio) {
            logger.debug(rio);
        } catch (Throwable t) {
            logger.error(ExceptionUtils.getStackTrace(t));
            throw new ServletException(t);
        }
    }

    private Document getAdministratorJnlp(HttpServletRequest request) throws Exception {
        InputStream is = ResourceUtil.getResourceStream(this.getClass(), "mirth-client.jnlp");
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        IOUtils.closeQuietly(is);

        Element jnlpElement = document.getDocumentElement();

        // Change the title to include the version of Mirth Connect
        PropertiesConfiguration versionProperties = new PropertiesConfiguration();
        versionProperties.setDelimiterParsingDisabled(true);
        versionProperties.load(ResourceUtil.getResourceStream(getClass(), "version.properties"));
        String version = versionProperties.getString("mirth.version");

        Element informationElement = (Element) jnlpElement.getElementsByTagName("information").item(0);
        Element title = (Element) informationElement.getElementsByTagName("title").item(0);
        String titleText = title.getTextContent() + " " + version;

        // If a server name is set, prepend the application title with it
        String serverName = configurationController.getServerSettings().getServerName();
        if (StringUtils.isNotBlank(serverName)) {
            titleText = serverName + " - " + titleText;
        }

        title.setTextContent(titleText);

        String scheme = request.getScheme();
        String serverHostname = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        String codebase = scheme + "://" + serverHostname + ":" + serverPort + contextPath;

        PropertiesConfiguration mirthProperties = new PropertiesConfiguration();
        mirthProperties.setDelimiterParsingDisabled(true);
        mirthProperties.load(ResourceUtil.getResourceStream(getClass(), "mirth.properties"));

        String server = null;

        if (StringUtils.isNotBlank(mirthProperties.getString("server.url"))) {
            server = mirthProperties.getString("server.url");
        } else {
            int httpsPort = mirthProperties.getInt("https.port", 8443);
            String contextPathProp = mirthProperties.getString("http.contextpath", "");

            // Add a starting slash if one does not exist
            if (!contextPathProp.startsWith("/")) {
                contextPathProp = "/" + contextPathProp;
            }

            // Remove a trailing slash if one exists
            if (contextPathProp.endsWith("/")) {
                contextPathProp = contextPathProp.substring(0, contextPathProp.length() - 1);
            }

            server = "https://" + serverHostname + ":" + httpsPort + contextPathProp;
        }

        jnlpElement.setAttribute("codebase", codebase);

        Element resourcesElement = (Element) jnlpElement.getElementsByTagName("resources").item(0);

        String maxHeapSize = request.getParameter("maxHeapSize");
        if (StringUtils.isBlank(maxHeapSize)) {
            maxHeapSize = mirthProperties.getString("administrator.maxheapsize");
        }
        if (StringUtils.isNotBlank(maxHeapSize)) {
            NodeList j2seList = resourcesElement.getElementsByTagName("j2se");

            for (int i = 0; i < j2seList.getLength(); i++) {
                Element j2se = (Element) j2seList.item(i);
                j2se.setAttribute("max-heap-size", maxHeapSize);
            }
        }

        List<String> defaultClientLibs = new ArrayList<String>();
        defaultClientLibs.add("mirth-client.jar");
        defaultClientLibs.add("mirth-client-core.jar");
        defaultClientLibs.add("mirth-crypto.jar");
        defaultClientLibs.add("mirth-vocab.jar");

        for (String defaultClientLib : defaultClientLibs) {
            Element jarElement = document.createElement("jar");
            jarElement.setAttribute("download", "eager");
            jarElement.setAttribute("href", "webstart/client-lib/" + defaultClientLib);

            if (defaultClientLib.equals("mirth-client.jar")) {
                jarElement.setAttribute("main", "true");
            }

            resourcesElement.appendChild(jarElement);
        }

        List<String> clientLibs = ControllerFactory.getFactory().createExtensionController().getClientLibraries();

        for (String clientLib : clientLibs) {
            if (!defaultClientLibs.contains(clientLib)) {
                Element jarElement = document.createElement("jar");
                jarElement.setAttribute("download", "eager");
                jarElement.setAttribute("href", "webstart/client-lib/" + clientLib);
                resourcesElement.appendChild(jarElement);
            }
        }

        List<MetaData> allExtensions = new ArrayList<MetaData>();
        allExtensions.addAll(ControllerFactory.getFactory().createExtensionController().getConnectorMetaData().values());
        allExtensions.addAll(ControllerFactory.getFactory().createExtensionController().getPluginMetaData().values());

        // we are using a set so that we don't have duplicates
        Set<String> extensionPathsToAddToJnlp = new HashSet<String>();

        for (MetaData extension : allExtensions) {
            if (doesExtensionHaveClientOrSharedLibraries(extension)) {
                extensionPathsToAddToJnlp.add(extension.getPath());
            }
        }

        for (String extensionPath : extensionPathsToAddToJnlp) {
            Element extensionElement = document.createElement("extension");
            extensionElement.setAttribute("href", "webstart/extensions/" + extensionPath + ".jnlp");
            resourcesElement.appendChild(extensionElement);
        }

        Element applicationDescElement = (Element) jnlpElement.getElementsByTagName("application-desc").item(0);
        Element serverArgumentElement = document.createElement("argument");
        serverArgumentElement.setTextContent(server);
        applicationDescElement.appendChild(serverArgumentElement);
        Element versionArgumentElement = document.createElement("argument");
        versionArgumentElement.setTextContent(version);
        applicationDescElement.appendChild(versionArgumentElement);

        String[] protocols = configurationController.getHttpsClientProtocols();
        String[] cipherSuites = configurationController.getHttpsCipherSuites();

        // Only add arguments for the protocols / cipher suites if they are non-default
        if (!Arrays.areEqual(protocols, MirthSSLUtil.DEFAULT_HTTPS_CLIENT_PROTOCOLS) || !Arrays.areEqual(cipherSuites, MirthSSLUtil.DEFAULT_HTTPS_CIPHER_SUITES)) {
            Element sslArgumentElement = document.createElement("argument");
            sslArgumentElement.setTextContent("-ssl");
            applicationDescElement.appendChild(sslArgumentElement);

            Element protocolsArgumentElement = document.createElement("argument");
            protocolsArgumentElement.setTextContent(StringUtils.join(protocols, ','));
            applicationDescElement.appendChild(protocolsArgumentElement);

            Element cipherSuitesArgumentElement = document.createElement("argument");
            cipherSuitesArgumentElement.setTextContent(StringUtils.join(cipherSuites, ','));
            applicationDescElement.appendChild(cipherSuitesArgumentElement);
        }

        return document;
    }

    private boolean doesExtensionHaveClientOrSharedLibraries(MetaData extension) {
        for (ExtensionLibrary lib : extension.getLibraries()) {
            if (lib.getType().equals(ExtensionLibrary.Type.CLIENT) || lib.getType().equals(ExtensionLibrary.Type.SHARED)) {
                return true;
            }
        }

        return false;
    }

    private Document getExtensionJnlp(String extensionPath) throws Exception {
        List<MetaData> allExtensions = new ArrayList<MetaData>();
        allExtensions.addAll(ControllerFactory.getFactory().createExtensionController().getConnectorMetaData().values());
        allExtensions.addAll(ControllerFactory.getFactory().createExtensionController().getPluginMetaData().values());
        Set<String> librariesToAddToJnlp = new HashSet<String>();
        List<String> extensionsWithThePath = new ArrayList<String>();

        for (MetaData metaData : allExtensions) {
            if (metaData.getPath().equals(extensionPath)) {
                extensionsWithThePath.add(metaData.getName());

                for (ExtensionLibrary library : metaData.getLibraries()) {
                    if (library.getType().equals(ExtensionLibrary.Type.CLIENT) || library.getType().equals(ExtensionLibrary.Type.SHARED)) {
                        librariesToAddToJnlp.add(library.getPath());
                    }
                }
            }
        }

        if (extensionsWithThePath.isEmpty()) {
            throw new Exception("Extension metadata could not be located for the path: " + extensionPath);
        }

        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element jnlpElement = document.createElement("jnlp");

        Element informationElement = document.createElement("information");

        Element titleElement = document.createElement("title");
        titleElement.setTextContent("Mirth Connect Extension - [" + StringUtils.join(extensionsWithThePath, ",") + "]");
        informationElement.appendChild(titleElement);

        Element vendorElement = document.createElement("vendor");
        vendorElement.setTextContent("Mirth Corporation");
        informationElement.appendChild(vendorElement);

        jnlpElement.appendChild(informationElement);

        Element securityElement = document.createElement("security");
        securityElement.appendChild(document.createElement("all-permissions"));
        jnlpElement.appendChild(securityElement);

        Element resourcesElement = document.createElement("resources");

        for (String library : librariesToAddToJnlp) {
            Element jarElement = document.createElement("jar");
            jarElement.setAttribute("download", "eager");
            // this path is relative to the servlet path
            jarElement.setAttribute("href", "libs/" + extensionPath + "/" + library);
            resourcesElement.appendChild(jarElement);
        }

        jnlpElement.appendChild(resourcesElement);
        jnlpElement.appendChild(document.createElement("component-desc"));
        document.appendChild(jnlpElement);
        return document;
    }
}

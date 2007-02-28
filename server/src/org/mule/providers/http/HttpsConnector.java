/*
 * $Header: /home/projects/mule/scm/mule/providers/http/src/java/org/mule/providers/http/HttpsConnector.java,v 1.6 2005/10/10 14:02:52 rossmason Exp $
 * $Revision: 1.6 $
 * $Date: 2005/10/10 14:02:52 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.providers.http;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.util.Utility;

/**
 * <code>HttpsConnector</code> provides Https connectivity
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.6 $
 */
public class HttpsConnector extends HttpConnector
{
    public static final String DEFAULT_KEYSTORE = ".keystore";
    public static final String DEFAULT_KEYSTORE_TYPE = KeyStore.getDefaultType();
    public static final String DEFAULT_SSL_TYPE = "SSLv3";
    public static final String DEFAULT_MANAGER_ALGORITHM = "SunX509";
    public static final String DEFAULT_PROTOCOL_HANDLER = "com.sun.net.ssl.internal.www.protocol";

    private String keyStore = DEFAULT_KEYSTORE;
    private String keyPassword = null;
    private String storePassword = null;
    private String keystoreType = KeyStore.getDefaultType();
    private String keyManagerAlgorithm = DEFAULT_MANAGER_ALGORITHM;
    private String sslType = DEFAULT_SSL_TYPE;
    private Provider provider = new com.sun.net.ssl.internal.ssl.Provider();
    private String protocolHandler = DEFAULT_PROTOCOL_HANDLER;
    private String clientKeyStore = null;
    private String clientKeyStorePassword = null;
    private String trustStore = null;
    private String trustStorePassword = null;
    private boolean explicitTrustStoreOnly = false;

    private KeyManagerFactory keyManagerFactory = null;
    private boolean requireClientAuthentication = true;

    public void doInitialise() throws InitialisationException
    {
        if (getProvider() == null)
            throw new NullPointerException("The security provider cannot be null");
        if (getKeyStore() == null)
            throw new NullPointerException("The KeyStore location cannot be null");
        if (getKeyPassword() == null)
            throw new NullPointerException("The Key password cannot be null");
        if (getStorePassword() == null)
            throw new NullPointerException("The KeyStore password cannot be null");
        if (getKeyManagerAlgorithm() == null)
            throw new NullPointerException("The Key Manager Algorithm cannot be null");

        KeyStore keystore = null;
        try {
            Security.addProvider(getProvider());
            // Create keyStore
            keystore = KeyStore.getInstance(keystoreType);
            InputStream is = Utility.loadResource(getKeyStore(), getClass());
            if (is == null) {
                throw new FileNotFoundException(new Message(Messages.CANT_LOAD_X_FROM_CLASSPATH_FILE, "Keystore: "
                        + getKeyStore()).getMessage());
            }
            keystore.load(is, getKeyPassword().toCharArray());
        } catch (Exception e) {
            throw new InitialisationException(new Message(Messages.FAILED_LOAD_X, "KeyStore: " + getKeyStore()),
                                              e,
                                              this);
        }
        try {
            // Get key manager
            keyManagerFactory = KeyManagerFactory.getInstance(getKeyManagerAlgorithm());
            // Initialize the KeyManagerFactory to work with our keyStore
            keyManagerFactory.init(keystore, getStorePassword().toCharArray());
        } catch (Exception e) {
            throw new InitialisationException(new Message(Messages.FAILED_LOAD_X, "Key Manager"), e, this);
        }

        super.doInitialise();

        if (protocolHandler != null) {
            System.setProperty("java.protocol.handler.pkgs", protocolHandler);
        }
        if (clientKeyStore != null) {
            try {
                String clientPath = Utility.getResourcePath(clientKeyStore, getClass());
                if (clientPath == null) {
                    throw new InitialisationException(new Message(Messages.FAILED_LOAD_X, "ClientKeyStore: "
                            + clientKeyStore), this);
                }
                System.setProperty("javax.net.ssl.keyStore", clientPath);
                System.setProperty("javax.net.ssl.keyStorePassword", clientKeyStorePassword);

                logger.info("Set Client Key store: javax.net.ssl.keyStore=" + clientPath);
            } catch (IOException e) {
                throw new InitialisationException(new Message(Messages.FAILED_LOAD_X, "ClientKeyStore: "
                        + clientKeyStore), this);
            }
        }

        if (trustStore != null) {
            System.setProperty("javax.net.ssl.trustStore", getTrustStore());
            if(getTrustStorePassword()!=null) {
                System.setProperty("javax.net.ssl.trustStorePassword", getTrustStorePassword());
            }
            logger.debug("Set Trust store: javax.net.ssl.trustStore=" + getTrustStore());
        } else if (!isExplicitTrustStoreOnly()) {
            logger.info("Defaulting trust store to client Key Store");
            trustStore = getClientKeyStore();
            trustStorePassword = getClientKeyStorePassword();
            System.setProperty("javax.net.ssl.trustStore", getTrustStore());
            System.setProperty("javax.net.ssl.trustStorePassword", getTrustStorePassword());
            logger.debug("Set Trust store: javax.net.ssl.trustStore=" + getTrustStore());
        }

    }

    public String getProtocol()
    {
        return "HTTPS";
    }

    public String getKeyStore()
    {
        return keyStore;
    }

    public void setKeyStore(String keyStore)
    {
        this.keyStore = keyStore;
    }

    public String getKeyPassword()
    {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword)
    {
        this.keyPassword = keyPassword;
    }

    public String getStorePassword()
    {
        return storePassword;
    }

    public void setStorePassword(String storePassword)
    {
        this.storePassword = storePassword;
    }

    public String getKeystoreType()
    {
        return keystoreType;
    }

    public void setKeystoreType(String keystoreType)
    {
        this.keystoreType = keystoreType;
    }

    public String getKeyManagerAlgorithm()
    {
        return keyManagerAlgorithm;
    }

    public void setKeyManagerAlgorithm(String keyManagerAlgorithm)
    {
        this.keyManagerAlgorithm = keyManagerAlgorithm;
    }

    public String getSslType()
    {
        return sslType;
    }

    public void setSslType(String sslType)
    {
        this.sslType = sslType;
    }

    public boolean isRequireClientAuthentication()
    {
        return requireClientAuthentication;
    }

    public void setRequireClientAuthentication(boolean requireClientAuthentication)
    {
        this.requireClientAuthentication = requireClientAuthentication;
    }

    public KeyManagerFactory getKeyManagerFactory()
    {
        return keyManagerFactory;
    }

    public Provider getProvider()
    {
        return provider;
    }

    public void setProvider(Provider provider)
    {
        this.provider = provider;
    }

    public String getProtocolHandler()
    {
        return protocolHandler;
    }

    public void setProtocolHandler(String protocolHandler)
    {
        this.protocolHandler = protocolHandler;
    }

    public String getClientKeyStore()
    {
        return clientKeyStore;
    }

    public void setClientKeyStore(String clientKeyStore) throws IOException
    {
        this.clientKeyStore = clientKeyStore;
        if (this.clientKeyStore != null) {
            this.clientKeyStore = Utility.getResourcePath(clientKeyStore, getClass());
            logger.debug("Normalised clientKeyStore path to: " + getClientKeyStore());
        }
    }

    public String getClientKeyStorePassword()
    {
        return clientKeyStorePassword;
    }

    public void setClientKeyStorePassword(String clientKeyStorePassword)
    {
        this.clientKeyStorePassword = clientKeyStorePassword;
    }

    public String getTrustStore()
    {
        return trustStore;
    }

    public void setTrustStore(String trustStore) throws IOException
    {
        this.trustStore = trustStore;
        if (this.trustStore != null) {
            this.trustStore = Utility.getResourcePath(trustStore, getClass());
            logger.debug("Normalised trustStore path to: " + getTrustStore());
        }
    }

    public String getTrustStorePassword()
    {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword)
    {
        this.trustStorePassword = trustStorePassword;
    }

    public boolean isExplicitTrustStoreOnly()
    {
        return explicitTrustStoreOnly;
    }

    public void setExplicitTrustStoreOnly(boolean explicitTrustStoreOnly)
    {
        this.explicitTrustStoreOnly = explicitTrustStoreOnly;
    }
}

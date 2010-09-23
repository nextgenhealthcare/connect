/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.crypto.SecretKey;

import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.ServerConfiguration;

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 */
public abstract class ConfigurationController extends Controller {
    // status codes

    public static final int STATUS_OK = 0;
    public static final int STATUS_UNAVAILABLE = 1;
    public static final int STATUS_ENGINE_STARTING = 2;

    public static ConfigurationController getInstance() {
        return ControllerFactory.getFactory().createConfigurationController();
    }

    /**
     * Checks for an existing certificate to user for secure communication
     * between the server and client. If no certficate exists, this will
     * generate a new one.
     * 
     */
    public abstract void generateKeyPair();

    /**
     * Loads the encryption key from the database and stores it in memory.
     * 
     */
    public abstract void loadEncryptionKey();

    /**
     * Returns the database type (ex. derby)
     * 
     * @return the database type
     */
    public abstract String getDatabaseType();

    /**
     * Returns the server's unique ID
     * 
     * @return the server's unique ID
     */
    public abstract String getServerId();

    public abstract String getServerTimezone(Locale locale);

    /**
     * Returns all of the charset encodings available on the server.
     * 
     * @return a list of charset encoding names
     * @throws ControllerException
     */
    public abstract List<String> getAvaiableCharsetEncodings() throws ControllerException;

    /**
     * Returns the encryption key.
     * 
     * @return a <code>SecretKey</code> object
     */
    public abstract SecretKey getEncryptionKey();

    /**
     * Returns the base directory for the server.
     * 
     * @return the base directory for the server.
     */
    public abstract String getBaseDir();

    public abstract String getConfigurationDir();

    public abstract String getApplicationDataDir();

    public abstract Properties getServerProperties() throws ControllerException;

    public abstract void setServerProperties(Properties properties) throws ControllerException;

    public abstract String generateGuid() throws ControllerException;

    public abstract List<DriverInfo> getDatabaseDrivers() throws ControllerException;

    public abstract String getServerVersion();

    public abstract int getSchemaVersion();

    public abstract String getBuildDate();

    public abstract ServerConfiguration getServerConfiguration() throws ControllerException;

    public abstract void setServerConfiguration(ServerConfiguration serverConfiguration) throws ControllerException;

    public abstract PasswordRequirements getPasswordRequirements();

    // status

    public abstract int getStatus();

    public abstract boolean isEngineStarting();

    public abstract void setEngineStarting(boolean isEngineStarting);

    // properties

    public abstract Properties getPropertiesForGroup(String group);

    public abstract String getProperty(String group, String name);

    public abstract void saveProperty(String group, String name, String property);

    public abstract void removeProperty(String group, String name);
}

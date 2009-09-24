/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.controllers;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.crypto.SecretKey;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.DriverInfo;
import com.webreach.mirth.model.PasswordRequirements;
import com.webreach.mirth.model.ServerConfiguration;

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 * @author geraldb
 * 
 */
public abstract class ConfigurationController extends Controller {
    public static final String GLOBAL_POSTPROCESSOR_KEY = "Postprocessor";
    public static final String GLOBAL_PREPROCESSOR_KEY = "Preprocessor";
    public static final String GLOBAL_SHUTDOWN_KEY = "Shutdown";
    public static final String GLOBAL_DEPLOY_KEY = "Deploy";

    // status codes
    
    public static final int STATUS_OK = 0;
    public static final int STATUS_UNAVAILABLE = 1;
    public static final int STATUS_ENGINE_STARTING = 2;
    
    public static ConfigurationController getInstance() {
        return ControllerFactory.getFactory().createConfigurationController();
    }

    public abstract void shutdown();

    public abstract void deployChannels() throws ControllerException;
    
    public abstract void loadEncryptionKey();
    
    // scripts

    public abstract Map<String, String> getGlobalScripts() throws ControllerException;

    public abstract void setGlobalScripts(Map<String, String> scripts) throws ControllerException;

    public abstract void compileScripts(List<Channel> channels) throws Exception;

    public abstract void executeChannelDeployScripts(List<Channel> channels);

    public abstract void executeChannelShutdownScripts(List<Channel> channels);

    public abstract void executeGlobalDeployScript();

    public abstract void executeGlobalShutdownScript();

    public abstract void executeGlobalScript(String scriptType);

    // mule configs

    public abstract File getLatestConfiguration() throws ControllerException;

    public abstract String getDefaultConfiguration() throws Exception;

    public abstract void deleteLatestConfiguration();

    // config parameters
    
    public abstract String getDatabaseType();

    public abstract String getMuleConfigurationPath();

    public abstract String getMuleBootPath();

    public abstract String getServerId();

    public abstract List<String> getAvaiableCharsetEncodings() throws ControllerException;

    public abstract SecretKey getEncryptionKey();

    public abstract String getBaseDir();

    public abstract Properties getServerProperties() throws ControllerException;

    public abstract void setServerProperties(Properties properties) throws ControllerException;

    public abstract String getGuid() throws ControllerException;

    public abstract List<DriverInfo> getDatabaseDrivers() throws ControllerException;

    public abstract String getServerVersion();

    public abstract int getSchemaVersion();

    public abstract String getBuildDate();

    public abstract String getQueuestorePath();

    public abstract ServerConfiguration getServerConfiguration() throws ControllerException;

    public abstract void setServerConfiguration(ServerConfiguration serverConfiguration) throws ControllerException;
    
    public abstract PasswordRequirements getPasswordRequirements();
    // status
    
    public abstract int getStatus();

    public abstract boolean isEngineStarting();

    public abstract void setEngineStarting(boolean isEngineStarting);
}

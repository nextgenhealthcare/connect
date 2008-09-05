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
import com.webreach.mirth.model.ServerConfiguration;

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 * @author geraldb
 * 
 */
public interface ConfigurationController {
    public static final String GLOBAL_POSTPROCESSOR_KEY = "Postprocessor";
    public static final String GLOBAL_PREPROCESSOR_KEY = "Preprocessor";
    public static final String GLOBAL_SHUTDOWN_KEY = "Shutdown";
    public static final String GLOBAL_DEPLOY_KEY = "Deploy";

    // status codes
    
    public static final int STATUS_OK = 0;
    public static final int STATUS_UNAVAILABLE = 1;
    public static final int STATUS_ENGINE_STARTING = 2;

    public void initialize();

    public void shutdown();

    public void deployChannels() throws ControllerException;

    // scripts

    public Map<String, String> getGlobalScripts() throws ControllerException;

    public void setGlobalScripts(Map<String, String> scripts) throws ControllerException;

    public void compileScripts(List<Channel> channels) throws Exception;

    public void executeChannelDeployScripts(List<Channel> channels);

    public void executeChannelShutdownScripts(List<Channel> channels);

    public void executeGlobalDeployScript();

    public void executeGlobalShutdownScript();

    public void executeGlobalScript(String scriptType);

    // mule configs

    public File getLatestConfiguration() throws ControllerException;

    public String getDefaultConfiguration() throws Exception;

    public void deleteLatestConfiguration();

    // config parameters
    
    public String getDatabaseType();

    public String getMuleConfigurationPath();

    public String getMuleBootPath();

    public String getServerId();

    public List<String> getAvaiableCharsetEncodings() throws ControllerException;

    public SecretKey getEncryptionKey();

    public String getBaseDir();

    public Properties getServerProperties() throws ControllerException;

    public void setServerProperties(Properties properties) throws ControllerException;

    public String getGuid() throws ControllerException;

    public List<DriverInfo> getDatabaseDrivers() throws ControllerException;

    public String getServerVersion();

    public int getSchemaVersion();

    public String getBuildDate();

    public String getQueuestorePath();

    public ServerConfiguration getServerConfiguration() throws ControllerException;

    public void setServerConfiguration(ServerConfiguration serverConfiguration) throws ControllerException;

    // status
    
    public int getStatus();

    public boolean isEngineStarting();

    public void setEngineStarting(boolean isEngineStarting);
}

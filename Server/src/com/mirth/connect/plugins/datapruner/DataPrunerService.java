/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datapruner;

import static com.mirth.connect.client.core.api.servlets.ExtensionServletInterface.OPERATION_PLUGIN_PROPERTIES_GET;
import static com.mirth.connect.client.core.api.servlets.ExtensionServletInterface.OPERATION_PLUGIN_PROPERTIES_SET;
import static com.mirth.connect.plugins.datapruner.DataPrunerServletInterface.PERMISSION_SAVE;
import static com.mirth.connect.plugins.datapruner.DataPrunerServletInterface.PERMISSION_START_STOP;
import static com.mirth.connect.plugins.datapruner.DataPrunerServletInterface.PERMISSION_VIEW;
import static com.mirth.connect.plugins.datapruner.DataPrunerServletInterface.PLUGIN_POINT;
import static com.mirth.connect.plugins.datapruner.DataPrunerServletInterface.TASK_START;
import static com.mirth.connect.plugins.datapruner.DataPrunerServletInterface.TASK_STOP;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.mirth.connect.client.core.TaskConstants;
import com.mirth.connect.client.core.api.util.OperationUtil;
import com.mirth.connect.donkey.model.channel.PollConnectorProperties;
import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.ServicePlugin;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class DataPrunerService implements ServicePlugin {

    public static final String PLUGINPOINT = "Data Pruner";

    private DataPrunerController dataPrunerController = DataPrunerController.getInstance();
    private ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    public String getPluginPointName() {
        return PLUGINPOINT;
    }

    @Override
    public void start() {
        try {
            dataPrunerController.start();
        } catch (DataPrunerException e) {
            logger.error("Failed to start data pruner service.", e);
        }
    }

    @Override
    public void stop() {
        try {
            dataPrunerController.stop(false);
        } catch (DataPrunerException e) {
            logger.error("Failed to stop data pruner service.", e);
        }
    }

    @Override
    public void init(Properties properties) {
        try {
            dataPrunerController.init(properties);
        } catch (DataPrunerException e) {
            logger.error("Failed to initialize data pruner service.", e);
        }
    }

    @Override
    public void update(Properties properties) {
        try {
            dataPrunerController.update(properties);
        } catch (DataPrunerException e) {
            logger.error("Failed to reschedule the data pruner.", e);
        }
    }

    @Override
    public Properties getDefaultProperties() {
        Properties properties = new Properties();
        properties.put("enabled", "false");

        PollConnectorProperties defaultProperties = new PollConnectorProperties();
        defaultProperties.setPollingFrequency(3600000);
        properties.put("pollingProperties", serializer.serialize(defaultProperties));

        properties.put("pruningBlockSize", String.valueOf(DataPruner.DEFAULT_PRUNING_BLOCK_SIZE));
        properties.put("archiveEnabled", serializer.serialize(false));
        properties.put("archiverBlockSize", String.valueOf(DataPruner.DEFAULT_ARCHIVING_BLOCK_SIZE));
        properties.put("includeAttachments", serializer.serialize(false));
        properties.put("archiverOptions", serializer.serialize(new MessageWriterOptions()));
        properties.put("pruneEvents", Boolean.toString(false));
        properties.put("maxEventAge", "");
        return properties;
    }

    @Override
    public ExtensionPermission[] getExtensionPermissions() {
        ExtensionPermission viewPermission = new ExtensionPermission(PLUGIN_POINT, PERMISSION_VIEW, "Displays the Data Pruner settings.", OperationUtil.getOperationNamesForPermission(PERMISSION_VIEW, DataPrunerServletInterface.class, OPERATION_PLUGIN_PROPERTIES_GET), new String[] {
                TaskConstants.SETTINGS_REFRESH });
        ExtensionPermission savePermission = new ExtensionPermission(PLUGIN_POINT, PERMISSION_SAVE, "Allows changing the Data Pruner settings.", OperationUtil.getOperationNamesForPermission(PERMISSION_SAVE, DataPrunerServletInterface.class, OPERATION_PLUGIN_PROPERTIES_SET), new String[] {
                TaskConstants.SETTINGS_SAVE });
        ExtensionPermission startStopPermission = new ExtensionPermission(PLUGIN_POINT, PERMISSION_START_STOP, "Allows starting or stopping the Data Pruner on-demand.", OperationUtil.getOperationNamesForPermission(PERMISSION_START_STOP, DataPrunerServletInterface.class), new String[] {
                TASK_START, TASK_STOP });

        return new ExtensionPermission[] { viewPermission, savePermission, startStopPermission };
    }
}
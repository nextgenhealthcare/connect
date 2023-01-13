/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.migration;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import com.mirth.connect.client.core.PropertiesConfigurationUtil;
import com.mirth.connect.model.util.MigrationException;
import com.mirth.connect.server.tools.ClassPathResource;
import com.mirth.connect.server.util.DatabaseUtil;

public class Migrate3_1_0 extends Migrator implements ConfigurationMigrator {
    private Logger logger = LogManager.getLogger(getClass());

    @Override
    public void migrate() throws MigrationException {
        executeScript(getDatabaseType() + "-3.0.3-3.1.0.sql");
        migrateLog4jProperties();
        migrateMessageSequences();
    }

    private void migrateLog4jProperties() {
        try {
            FileBasedConfigurationBuilder<PropertiesConfiguration> builder = PropertiesConfigurationUtil.createBuilder(new File(ClassPathResource.getResourceURI("log4j2.properties")));
            PropertiesConfiguration log4jproperties = builder.getConfiguration();

            String level = (String) log4jproperties.getProperty("logger.shutdown.level");
            if (level != null) {
                log4jproperties.setProperty("logger.undeploy.level", level);
                log4jproperties.clearProperty("logger.shutdown.level");
                Logger logger2 = LogManager.getLogger("undeploy");
                Configurator.setLevel(logger2.getName(),Level.toLevel(level));
            }

            level = (String) log4jproperties.getProperty("logger.recoveryTask.level");
            if (StringUtils.isBlank(level)) {
                level = "INFO";
                log4jproperties.setProperty("logger.recoveryTask.level", level);
                Logger logger2 = LogManager.getLogger("com.mirth.connect.donkey.server.channel.RecoveryTask");
                Configurator.setLevel(logger2.getName(),Level.toLevel(level));
            }

            builder.save();
        } catch (ConfigurationException | IOException e) {
            logger.error("Failed to migrate log4j properties.", e);
        }
    }

    private void migrateMessageSequences() throws MigrationException {
        try {
            if (scriptExists(getDatabaseType() + "-3.0.3-3.1.0-create-msg-seq.sql") && DatabaseUtil.tableExists(getConnection(), "D_MESSAGE_SEQUENCES")) {
                logger.debug("Migrating message sequences for " + getDatabaseType());

                PreparedStatement preparedStatement = null;
                ResultSet resultSet = null;

                try {
                    preparedStatement = getConnection().prepareStatement("SELECT LOCAL_CHANNEL_ID, ID FROM D_MESSAGE_SEQUENCES");
                    resultSet = preparedStatement.executeQuery();

                    while (resultSet.next()) {
                        Map<String, Object> replacements = new HashMap<String, Object>();
                        replacements.put("localChannelId", resultSet.getLong(1));
                        replacements.put("initialValue", resultSet.getLong(2));

                        logger.debug("Migrating message sequence for local channel ID " + replacements.get("localChannelId") + ", with initial value of " + replacements.get("initialValue"));
                        executeScript(getDatabaseType() + "-3.0.3-3.1.0-create-msg-seq.sql", replacements);
                    }
                } finally {
                    DbUtils.closeQuietly(resultSet);
                    DbUtils.closeQuietly(preparedStatement);
                }

                logger.debug("Finished creating new message sequence tables, dropping old D_MESSAGE_SEQUENCES table");
                executeStatement("DROP TABLE D_MESSAGE_SEQUENCES");
            }
        } catch (Exception e) {
            throw new MigrationException("An error occurred while migrating message sequences or checking to see if sequences need migration.", e);
        }
    }

    @Override
    public void migrateSerializedData() throws MigrationException {}

    @Override
    public Map<String, Object> getConfigurationPropertiesToAdd() {
        Map<String, Object> propertiesToAdd = new LinkedHashMap<String, Object>();

        propertiesToAdd.put("configurationmap.path", "${dir.appdata}/configuration.properties");

        return propertiesToAdd;
    }

    @Override
    public String[] getConfigurationPropertiesToRemove() {
        return null;
    }

    @Override
    public void updateConfiguration(PropertiesConfiguration configuration) {}
}

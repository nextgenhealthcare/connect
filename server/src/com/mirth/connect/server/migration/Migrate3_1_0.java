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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.mirth.connect.model.util.MigrationException;
import com.mirth.connect.server.tools.ClassPathResource;
import com.mirth.connect.server.util.DatabaseUtil;

public class Migrate3_1_0 extends Migrator implements ConfigurationMigrator {
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void migrate() throws MigrationException {
        executeScript(getDatabaseType() + "-3.0.3-3.1.0.sql");
        migrateLog4jProperties();
        migrateMessageSequences();
    }

    private void migrateLog4jProperties() {
        PropertiesConfiguration log4jproperties = new PropertiesConfiguration();
        log4jproperties.setDelimiterParsingDisabled(true);
        log4jproperties.setFile(new File(ClassPathResource.getResourceURI("log4j.properties")));
        try {
            log4jproperties.load();

            String level = (String) log4jproperties.getProperty("log4j.logger.shutdown");
            if (level != null) {
                log4jproperties.setProperty("log4j.logger.undeploy", level);
                log4jproperties.clearProperty("log4j.logger.shutdown");
                Logger.getLogger("undeploy").setLevel(Level.toLevel(level));
            }

            level = (String) log4jproperties.getProperty("log4j.logger.com.mirth.connect.donkey.server.channel.RecoveryTask");
            if (StringUtils.isBlank(level)) {
                level = "INFO";
                log4jproperties.setProperty("log4j.logger.com.mirth.connect.donkey.server.channel.RecoveryTask", level);
                Logger.getLogger("com.mirth.connect.donkey.server.channel.RecoveryTask").setLevel(Level.toLevel(level));
            }

            log4jproperties.save();
        } catch (ConfigurationException e) {
            logger.error("Failed to migrate log4j properties.");
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

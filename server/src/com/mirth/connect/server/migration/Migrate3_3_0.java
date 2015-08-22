/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;
import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.util.MigrationException;

public class Migrate3_3_0 extends Migrator implements ConfigurationMigrator {

    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void migrate() throws MigrationException {
        executeScript(getDatabaseType() + "-3.2.2-3.3.0.sql");
        migrateCodeTemplates();
    }

    private void migrateCodeTemplates() {
        PreparedStatement preparedStatement = null;
        ResultSet results = null;

        /*
         * MIRTH-1667: Derby fails if autoCommit is set to true and there are a large number of
         * results. The following error occurs: "ERROR 40XD0: Container has been closed"
         */
        Connection connection = getConnection();
        Boolean autoCommit = null;

        try {
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            List<String> codeTemplateIds = new ArrayList<String>();
            preparedStatement = connection.prepareStatement("SELECT ID, CODE_TEMPLATE FROM OLD_CODE_TEMPLATE");
            results = preparedStatement.executeQuery();

            PreparedStatement updateStatement = null;
            updateStatement = connection.prepareStatement("INSERT INTO CODE_TEMPLATE (ID, NAME, REVISION, CODE_TEMPLATE) VALUES (?, ?, ?, ?)");

            try {
                while (results.next()) {
                    String codeTemplateId = "";

                    try {
                        codeTemplateId = results.getString(1);
                        String codeTemplate = results.getString(2);
                        String name = new DonkeyElement(codeTemplate).getChildElement("name").getTextContent();
                        Integer revision = 1;

                        updateStatement.setString(1, codeTemplateId);
                        updateStatement.setString(2, name);
                        updateStatement.setInt(3, revision);
                        updateStatement.setString(4, codeTemplate);
                        updateStatement.executeUpdate();

                        codeTemplateIds.add(codeTemplateId);
                    } catch (Exception e) {
                        logger.error("Error migrating code template " + codeTemplateId + ".", e);
                    }
                }
            } finally {
                DbUtils.closeQuietly(updateStatement);
            }

            try {
                String libraryId = UUID.randomUUID().toString();
                String name = "Library 1";
                Integer revision = 1;

                DonkeyElement libraryElement = new DonkeyElement("<codeTemplateLibrary/>");
                libraryElement.setAttribute("version", "3.3.0");

                libraryElement.addChildElement("id", libraryId);
                libraryElement.addChildElement("name", name);
                libraryElement.addChildElement("revision", String.valueOf(revision));
                try {
                    libraryElement.addChildElementFromXml(ObjectXMLSerializer.getInstance().serialize(Calendar.getInstance())).setNodeName("lastModified");
                } catch (DonkeyElementException e) {
                    throw new SerializerException("Failed to migrate code template library last modified date.", e);
                }
                libraryElement.addChildElement("description", "This library was added upon migration to version 3.3.0. It includes all pre-existing\ncode templates, and is set to be included on all pre-existing and new channels.\n\nYou should create your own new libraries and assign code templates to them as you\nsee fit. You should also link libraries to specific channels, so that you're not\nnecessarily including all code templates on all channels all the time.");
                libraryElement.addChildElement("includeNewChannels", "true");
                libraryElement.addChildElement("enabledChannelIds");
                libraryElement.addChildElement("disabledChannelIds");

                DonkeyElement codeTemplatesElement = libraryElement.addChildElement("codeTemplates");
                for (String codeTemplateId : codeTemplateIds) {
                    DonkeyElement codeTemplateElement = codeTemplatesElement.addChildElement("codeTemplate");
                    codeTemplateElement.setAttribute("version", "3.3.0");
                    codeTemplateElement.addChildElement("id", codeTemplateId);
                }

                String libraryXml = libraryElement.toXml();

                try {
                    updateStatement = connection.prepareStatement("INSERT INTO CODE_TEMPLATE_LIBRARY (ID, NAME, REVISION, LIBRARY) VALUES (?, ?, ?, ?)");
                    updateStatement.setString(1, libraryId);
                    updateStatement.setString(2, name);
                    updateStatement.setInt(3, revision);
                    updateStatement.setString(4, libraryXml);
                    updateStatement.executeUpdate();
                } finally {
                    DbUtils.closeQuietly(updateStatement);
                }
            } catch (Exception e) {
                logger.error("Error creating code template library for migration.", e);
            }

            connection.commit();
        } catch (SQLException e) {
            logger.error("Error migrating code templates.", e);
        } finally {
            DbUtils.closeQuietly(results);
            DbUtils.closeQuietly(preparedStatement);
            if (autoCommit != null) {
                try {
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException e) {
                }
            }
        }
    }

    @Override
    public void migrateSerializedData() throws MigrationException {}

    @Override
    public Map<String, Object> getConfigurationPropertiesToAdd() {
        return null;
    }

    @Override
    public String[] getConfigurationPropertiesToRemove() {
        return null;
    }

    @Override
    public void updateConfiguration(PropertiesConfiguration configuration) {}
}
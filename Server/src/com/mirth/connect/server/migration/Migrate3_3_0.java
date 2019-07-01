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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.text.DateFormatter;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;
import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.util.MigrationException;

public class Migrate3_3_0 extends Migrator implements ConfigurationMigrator {

    private Logger logger = Logger.getLogger(getClass());
    private ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();

    @Override
    public void migrate() throws MigrationException {
        executeScript(getDatabaseType() + "-3.2.2-3.3.0.sql");
        migrateCodeTemplates();
        migrateDataPrunerConfiguration();
    }

    private void migrateCodeTemplates() {
        PreparedStatement preparedStatement = null;
        ResultSet results = null;

        try {
            Connection connection = getConnection();

            /*
             * MIRTH-1667: Derby fails if autoCommit is set to true and there are a large number of
             * results. The following error occurs: "ERROR 40XD0: Container has been closed"
             */
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
                    libraryElement.addChildElementFromXml(serializer.serialize(Calendar.getInstance())).setNodeName("lastModified");
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
        }
    }

    private void migrateDataPrunerConfiguration() {
        boolean enabled = true;
        String time = "";
        String interval = "";
        String dayOfWeek = "";
        String dayOfMonth = "1";

        ResultSet results = null;
        PreparedStatement statement = null;
        Connection connection = null;

        try {
            connection = getConnection();
            try {
                statement = connection.prepareStatement("SELECT NAME, VALUE FROM CONFIGURATION WHERE CATEGORY = 'Data Pruner'");

                results = statement.executeQuery();
                while (results.next()) {
                    String name = results.getString(1);
                    String value = results.getString(2);

                    if (name.equals("interval")) {
                        interval = value;
                    } else if (name.equals("time")) {
                        time = value;
                    } else if (name.equals("dayOfWeek")) {
                        dayOfWeek = value;
                    } else if (name.equals("dayOfMonth")) {
                        dayOfMonth = value;
                    }
                }
            } catch (SQLException e) {
                logger.error("Failed to read Data Pruner configuration properties.", e);
            } finally {
                DbUtils.closeQuietly(statement);
                DbUtils.closeQuietly(results);
            }

            enabled = !interval.equals("disabled");
            String pollingType = "INTERVAL";
            String pollingHour = "12";
            String pollingMinute = "0";
            boolean weekly = !StringUtils.equals(interval, "monthly");
            boolean[] activeDays = new boolean[] { true, true, true, true, true, true, true, true };

            if (enabled && !StringUtils.equals(interval, "hourly")) {
                SimpleDateFormat timeDateFormat = new SimpleDateFormat("hh:mm aa");
                DateFormatter timeFormatter = new DateFormatter(timeDateFormat);
                Date timeDate = null;

                try {
                    timeDate = (Date) timeFormatter.stringToValue(time);
                    Calendar timeCalendar = Calendar.getInstance();
                    timeCalendar.setTime(timeDate);

                    pollingType = "TIME";
                    pollingHour = String.valueOf(timeCalendar.get(Calendar.HOUR_OF_DAY));
                    pollingMinute = String.valueOf(timeCalendar.get(Calendar.MINUTE));

                    if (StringUtils.equals(interval, "weekly")) {
                        SimpleDateFormat dayDateFormat = new SimpleDateFormat("EEEEEEEE");
                        DateFormatter dayFormatter = new DateFormatter(dayDateFormat);

                        Date dayDate = (Date) dayFormatter.stringToValue(dayOfWeek);
                        Calendar dayCalendar = Calendar.getInstance();
                        dayCalendar.setTime(dayDate);

                        activeDays = new boolean[] { false, false, false, false, false, false,
                                false, false };
                        activeDays[dayCalendar.get(Calendar.DAY_OF_WEEK)] = true;
                    }
                } catch (Exception e) {
                    logger.error("Failed to get Data Pruner time properties", e);
                }
            }

            DonkeyElement pollingProperties = new DonkeyElement("<com.mirth.connect.donkey.model.channel.PollConnectorProperties/>");
            pollingProperties.setAttribute("version", "3.3.0");
            pollingProperties.addChildElementIfNotExists("pollingType", pollingType);
            pollingProperties.addChildElementIfNotExists("pollOnStart", "false");
            pollingProperties.addChildElementIfNotExists("pollingFrequency", "3600000");
            pollingProperties.addChildElementIfNotExists("pollingHour", pollingHour);
            pollingProperties.addChildElementIfNotExists("pollingMinute", pollingMinute);
            pollingProperties.addChildElementIfNotExists("cronJobs");

            DonkeyElement advancedProperties = pollingProperties.addChildElementIfNotExists("pollConnectorPropertiesAdvanced");
            advancedProperties.addChildElementIfNotExists("weekly", weekly ? "true" : "false");

            DonkeyElement inactiveDays = advancedProperties.addChildElementIfNotExists("inactiveDays");
            if (inactiveDays != null) {
                for (int index = 0; index < 8; ++index) {
                    inactiveDays.addChildElement("boolean", activeDays[index] ? "false" : "true");
                }
            }

            advancedProperties.addChildElementIfNotExists("dayOfMonth", dayOfMonth);
            advancedProperties.addChildElementIfNotExists("allDay", "true");
            advancedProperties.addChildElementIfNotExists("startingHour", "8");
            advancedProperties.addChildElementIfNotExists("startingMinute", "0");
            advancedProperties.addChildElementIfNotExists("endingHour", "17");
            advancedProperties.addChildElementIfNotExists("endingMinute", "0");

            PreparedStatement inputStatement = null;
            try {
                inputStatement = connection.prepareStatement("INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES (?, ?, ?)");

                inputStatement.setString(1, "Data Pruner");
                inputStatement.setString(2, "pollingProperties");
                inputStatement.setString(3, pollingProperties.toXml());
                inputStatement.executeUpdate();
            } catch (Exception e) {
                logger.error("Failed to insert Data Pruner configuration pollingProperties.", e);
            } finally {
                DbUtils.closeQuietly(inputStatement);
            }

            PreparedStatement updateStatement = null;
            try {
                updateStatement = connection.prepareStatement("UPDATE CONFIGURATION SET NAME = ?, VALUE = ? WHERE CATEGORY = ? AND NAME = ?");
                updateStatement.setString(1, "enabled");
                updateStatement.setString(2, enabled ? "true" : "false");
                updateStatement.setString(3, "Data Pruner");
                updateStatement.setString(4, "interval");
                updateStatement.executeUpdate();
            } finally {
                DbUtils.closeQuietly(updateStatement);
            }

            PreparedStatement deleteStatement = null;
            try {
                deleteStatement = connection.prepareStatement("DELETE FROM CONFIGURATION WHERE CATEGORY = ? AND NAME IN (?, ?, ?)");
                deleteStatement.setString(1, "Data Pruner");
                deleteStatement.setString(2, "time");
                deleteStatement.setString(3, "dayOfWeek");
                deleteStatement.setString(4, "dayOfMonth");
                deleteStatement.executeUpdate();
            } finally {
                DbUtils.closeQuietly(deleteStatement);
            }
        } catch (Exception e) {
            logger.error("Failed to modify Data Pruner configuration properties", e);
        }
    }

    @Override
    public void migrateSerializedData() throws MigrationException {}

    @Override
    public Map<String, Object> getConfigurationPropertiesToAdd() {
        Map<String, Object> propertiesToAdd = new LinkedHashMap<String, Object>();

        propertiesToAdd.put("server.startupdeploy", new MutablePair<Object, String>(true, "Determines whether or not channels are deployed on server startup."));

        return propertiesToAdd;
    }

    @Override
    public String[] getConfigurationPropertiesToRemove() {
        return null;
    }

    @Override
    public void updateConfiguration(PropertiesConfiguration configuration) {}
}
/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.migration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.util.MigrationException;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class Migrate3_8_0 extends Migrator {

    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void migrate() throws MigrationException {
        // Read dbdrivers.xml and update the database
        ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
        try {
            List<DriverInfo> drivers = configurationController.getDatabaseDrivers();

            for (Iterator<DriverInfo> it = drivers.iterator(); it.hasNext();) {
                DriverInfo driver = it.next();

                // The class name changed with the new version of the MySQL JDBC driver
                if (StringUtils.equals(driver.getClassName(), "com.mysql.jdbc.Driver")) {
                    logger.info("Updating MySQL JDBC driver from \"com.mysql.jdbc.Driver\" to \"com.mysql.cj.jdbc.Driver\"");
                    driver.setClassName("com.mysql.cj.jdbc.Driver");
                    driver.setAlternativeClassNames(new ArrayList<String>(Arrays.asList(new String[] {
                            "com.mysql.jdbc.Driver" })));
                }

                // The JDBC-ODBC bridge driver was removed with Java 7
                if (StringUtils.equals(driver.getClassName(), "sun.jdbc.odbc.JdbcOdbcDriver")) {
                    logger.info("Removing JDBC-ODBC driver which was removed in Java 7");
                    it.remove();
                }
            }

            configurationController.setDatabaseDrivers(drivers);
        } catch (ControllerException e) {
            throw new MigrationException(e);
        }
    }

    @Override
    public void migrateSerializedData() throws MigrationException {}
}

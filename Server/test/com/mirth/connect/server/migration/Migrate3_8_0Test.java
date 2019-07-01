/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.Invocation;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class Migrate3_8_0Test {

    private static ControllerFactory controllerFactory;
    private static ConfigurationController configurationController;

    @BeforeClass
    public static void setup() throws Exception {
        controllerFactory = mock(ControllerFactory.class);

        configurationController = mock(ConfigurationController.class);
        when(controllerFactory.createConfigurationController()).thenReturn(configurationController);

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                requestStaticInjection(ControllerFactory.class);
                bind(ControllerFactory.class).toInstance(controllerFactory);
            }
        });
        injector.getInstance(ControllerFactory.class);
    }

    @Test
    public void migrate_NoChanges() throws Exception {
        List<DriverInfo> drivers = getDefaultDrivers();

        when(configurationController.getDatabaseDrivers()).thenReturn(new ArrayList<DriverInfo>(drivers));

        Migrator migrator = new Migrate3_8_0();
        migrator.migrate();

        boolean found = false;
        for (Invocation invocation : mockingDetails(configurationController).getInvocations()) {
            if (invocation.getMethod().getName().equals("setDatabaseDrivers")) {
                found = true;
                assertEquals(drivers, invocation.getArgument(0));
            }
        }

        if (!found) {
            fail("Method setDatabaseDrivers not called.");
        }
    }

    @Test
    public void migrate_Updated() throws Exception {
        List<DriverInfo> drivers = new ArrayList<DriverInfo>();
        drivers.add(new DriverInfo("Sun JDBC-ODBC Bridge", "sun.jdbc.odbc.JdbcOdbcDriver", "jdbc:odbc:DSN", ""));
        drivers.add(new DriverInfo("MySQL", "com.mysql.jdbc.Driver", "jdbc:mysql://host:port/dbname", "SELECT * FROM ? LIMIT 1"));
        drivers.add(new DriverInfo("Oracle", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@host:port:dbname", "SELECT * FROM ? WHERE ROWNUM < 2"));
        drivers.add(new DriverInfo("PostgreSQL", "org.postgresql.Driver", "jdbc:postgresql://host:port/dbname", "SELECT * FROM ? LIMIT 1"));
        drivers.add(new DriverInfo("SQL Server/Sybase", "net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://host:port/dbname", "SELECT TOP 1 * FROM ?"));
        drivers.add(new DriverInfo("SQLite", "org.sqlite.JDBC", "jdbc:sqlite:dbfile.db", "SELECT * FROM ? LIMIT 1"));

        when(configurationController.getDatabaseDrivers()).thenReturn(new ArrayList<DriverInfo>(drivers));

        Migrator migrator = new Migrate3_8_0();
        migrator.migrate();

        boolean found = false;
        for (Invocation invocation : mockingDetails(configurationController).getInvocations()) {
            if (invocation.getMethod().getName().equals("setDatabaseDrivers")) {
                found = true;
                assertEquals(getDefaultDrivers(), invocation.getArgument(0));
            }
        }

        if (!found) {
            fail("Method setDatabaseDrivers not called.");
        }
    }

    private List<DriverInfo> getDefaultDrivers() {
        List<DriverInfo> drivers = new ArrayList<DriverInfo>();
        drivers.add(new DriverInfo("MySQL", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://host:port/dbname", "SELECT * FROM ? LIMIT 1", new ArrayList<String>(Arrays.asList(new String[] {
                "com.mysql.jdbc.Driver" }))));
        drivers.add(new DriverInfo("Oracle", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@host:port:dbname", "SELECT * FROM ? WHERE ROWNUM < 2"));
        drivers.add(new DriverInfo("PostgreSQL", "org.postgresql.Driver", "jdbc:postgresql://host:port/dbname", "SELECT * FROM ? LIMIT 1"));
        drivers.add(new DriverInfo("SQL Server/Sybase", "net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://host:port/dbname", "SELECT TOP 1 * FROM ?"));
        drivers.add(new DriverInfo("SQLite", "org.sqlite.JDBC", "jdbc:sqlite:dbfile.db", "SELECT * FROM ? LIMIT 1"));
        return drivers;
    }
}

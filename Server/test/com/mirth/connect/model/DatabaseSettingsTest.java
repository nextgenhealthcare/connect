/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class DatabaseSettingsTest {

    @Test
    public void getMappedDatabaseDriver_NullDriver_InvalidDatabase() throws Exception {
        DatabaseSettings databaseSettings = new DatabaseSettings();
        databaseSettings.setDatabase("dummy");
        databaseSettings.setDatabaseDriver(null);
        assertNull(databaseSettings.getMappedDatabaseDriver());
    }

    @Test
    public void getMappedDatabaseDriver_BlankDriver_ValidDatabase() throws Exception {
        DatabaseSettings databaseSettings = new DatabaseSettings();
        databaseSettings.setDatabase("postgres");
        databaseSettings.setDatabaseDriver(" ");
        assertEquals("org.postgresql.Driver", databaseSettings.getMappedDatabaseDriver());
    }

    @Test
    public void getMappedDatabaseDriver_NonBlankDriver() throws Exception {
        DatabaseSettings databaseSettings = new DatabaseSettings();
        databaseSettings.setDatabase("mysql");
        databaseSettings.setDatabaseDriver("test");
        assertEquals("test", databaseSettings.getMappedDatabaseDriver());
    }

    @Test
    public void getMappedReadOnlyDatabaseDriver_NullDriver_NullRODriver_NullDatabaseRO() throws Exception {
        DatabaseSettings databaseSettings = spy(new DatabaseSettings());
        databaseSettings.setDatabase("oracle");
        databaseSettings.setDatabaseDriver(null);
        databaseSettings.setDatabaseReadOnly(null);
        databaseSettings.setDatabaseReadOnlyDriver(null);
        assertEquals("oracle.jdbc.OracleDriver", databaseSettings.getMappedReadOnlyDatabaseDriver());
        verify(databaseSettings, times(1)).getMappedDatabaseDriver();
    }

    @Test
    public void getMappedReadOnlyDatabaseDriver_NullDriver_NullRODriver_InvalidDatabaseRO() throws Exception {
        DatabaseSettings databaseSettings = spy(new DatabaseSettings());
        databaseSettings.setDatabase("oracle");
        databaseSettings.setDatabaseDriver(null);
        databaseSettings.setDatabaseReadOnly("dummy");
        databaseSettings.setDatabaseReadOnlyDriver(null);
        assertNull(databaseSettings.getMappedReadOnlyDatabaseDriver());
        verify(databaseSettings, times(0)).getMappedDatabaseDriver();
    }

    @Test
    public void getMappedReadOnlyDatabaseDriver_NullDriver_NullRODriver_DifferentDatabaseRO() throws Exception {
        DatabaseSettings databaseSettings = spy(new DatabaseSettings());
        databaseSettings.setDatabase("oracle");
        databaseSettings.setDatabaseDriver(null);
        databaseSettings.setDatabaseReadOnly("mysql");
        databaseSettings.setDatabaseReadOnlyDriver(null);
        assertEquals("com.mysql.cj.jdbc.Driver", databaseSettings.getMappedReadOnlyDatabaseDriver());
        verify(databaseSettings, times(0)).getMappedDatabaseDriver();
    }

    @Test
    public void getMappedReadOnlyDatabaseDriver_NullDriver_NullRODriver_ValidDatabaseRO() throws Exception {
        DatabaseSettings databaseSettings = spy(new DatabaseSettings());
        databaseSettings.setDatabase("oracle");
        databaseSettings.setDatabaseDriver(null);
        databaseSettings.setDatabaseReadOnly("sqlserver");
        databaseSettings.setDatabaseReadOnlyDriver(null);
        assertEquals("net.sourceforge.jtds.jdbc.Driver", databaseSettings.getMappedReadOnlyDatabaseDriver());
        verify(databaseSettings, times(0)).getMappedDatabaseDriver();
    }

    @Test
    public void getMappedReadOnlyDatabaseDriver_NonBlankRODriver() throws Exception {
        DatabaseSettings databaseSettings = spy(new DatabaseSettings());
        databaseSettings.setDatabase("oracle");
        databaseSettings.setDatabaseDriver(null);
        databaseSettings.setDatabaseReadOnly(null);
        databaseSettings.setDatabaseReadOnlyDriver("test");
        assertEquals("test", databaseSettings.getMappedReadOnlyDatabaseDriver());
        verify(databaseSettings, times(0)).getMappedDatabaseDriver();
    }
}

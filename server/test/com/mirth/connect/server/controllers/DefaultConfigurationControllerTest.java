/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.DriverInfo;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.Invocation;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DefaultConfigurationControllerTest {

    @BeforeClass
    public static void setup() throws Exception {
        ControllerFactory controllerFactory = mock(ControllerFactory.class);

        ScriptController scriptController = mock(ScriptController.class);
        when(controllerFactory.createScriptController()).thenReturn(scriptController);

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
    public void getDatabaseDrivers_NotBlank() throws Exception {
        DefaultConfigurationController configurationController = spy(new DefaultConfigurationController());

        String databaseDriversXml = DEFAULT_DRIVERS_XML;
        doReturn(databaseDriversXml).when(configurationController).getProperty(any(), any());

        List<DriverInfo> drivers = configurationController.getDatabaseDrivers();
        assertDefaultDrivers(drivers, false);
    }

    @Test
    public void getDatabaseDrivers_Blank() throws Exception {
        DefaultConfigurationController configurationController = spy(new DefaultConfigurationController());

        String databaseDriversXml = null;
        doReturn(databaseDriversXml).when(configurationController).getProperty(any(), any());

        File testConfDir = new File("./testconf");
        testConfDir.mkdir();
        File dbDriversFile = new File(testConfDir, "dbdrivers.xml");
        FileUtils.writeStringToFile(dbDriversFile, DEFAULT_DBDRIVERS_FILE, "UTF-8", false);
        doReturn(dbDriversFile).when(configurationController).getDbDriversFile();

        List<DriverInfo> drivers = configurationController.getDatabaseDrivers();
        assertDefaultDrivers(drivers, true);
    }

    @Test
    public void getDatabaseDrivers_FileDoesNotExist() throws Exception {
        DefaultConfigurationController configurationController = spy(new DefaultConfigurationController());

        String databaseDriversXml = null;
        doReturn(databaseDriversXml).when(configurationController).getProperty(any(), any());

        File testConfDir = new File("./testconf");
        testConfDir.mkdir();
        File dbDriversFile = new File(testConfDir, "dummy.xml");
        if (dbDriversFile.exists()) {
            dbDriversFile.delete();
        }
        doReturn(dbDriversFile).when(configurationController).getDbDriversFile();

        List<DriverInfo> drivers = configurationController.getDatabaseDrivers();
        assertDefaultDrivers(drivers, false);
    }

    @Test
    public void getDatabaseDrivers_Exception() throws Exception {
        DefaultConfigurationController configurationController = spy(new DefaultConfigurationController());

        String databaseDriversXml = null;
        doReturn(databaseDriversXml).when(configurationController).getProperty(any(), any());

        File testConfDir = new File("./testconf");
        testConfDir.mkdir();
        File dbDriversFile = new File(testConfDir, "dummy.xml");
        FileUtils.writeStringToFile(dbDriversFile, "test", "UTF-8", false);
        doReturn(dbDriversFile).when(configurationController).getDbDriversFile();

        try {
            configurationController.getDatabaseDrivers();
            fail("Exception should have been thrown");
        } catch (ControllerException e) {
            // Expected
        }
    }

    @Test
    public void setDatabaseDrivers() throws Exception {
        DefaultConfigurationController configurationController = spy(new DefaultConfigurationController());

        doNothing().when(configurationController).saveProperty(any(), any(), any());

        List<DriverInfo> drivers = new ArrayList<DriverInfo>();
        drivers.add(new DriverInfo("MySQL", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://host:port/dbname", "SELECT * FROM ? LIMIT 1", new ArrayList<String>(Arrays.asList(new String[] {
                "com.mysql.jdbc.Driver" }))));
        drivers.add(new DriverInfo("Oracle", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@host:port:dbname", "SELECT * FROM ? WHERE ROWNUM < 2"));
        drivers.add(new DriverInfo("PostgreSQL", "org.postgresql.Driver", "jdbc:postgresql://host:port/dbname", "SELECT * FROM ? LIMIT 1"));
        drivers.add(new DriverInfo("SQL Server/Sybase", "net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://host:port/dbname", "SELECT TOP 1 * FROM ?"));
        drivers.add(new DriverInfo("SQLite", "org.sqlite.JDBC", "jdbc:sqlite:dbfile.db", "SELECT * FROM ? LIMIT 1"));

        configurationController.setDatabaseDrivers(drivers);

        boolean found = false;
        for (Invocation invocation : mockingDetails(configurationController).getInvocations()) {
            if (invocation.getMethod().getName().equals("saveProperty")) {
                found = true;
                assertEquals("core", invocation.getArgument(0));
                assertEquals("databaseDrivers", invocation.getArgument(1));

                String expected = normalizeXml(DEFAULT_DRIVERS_XML);
                String actual = normalizeXml((String) invocation.getArgument(2));
                assertEquals(expected, actual);
            }
        }

        if (!found) {
            fail("Method saveProperty not called.");
        }
    }

    @Test
    public void testRhinoVersion15() {
        assertEquals(150, (int) new DefaultConfigurationController().getRhinoLanguageVersion("1.5"));
    }

    @Test
    public void testRhinoVersionES6LowerCase() {
        assertEquals(200, (int) new DefaultConfigurationController().getRhinoLanguageVersion("es6"));
    }

    @Test
    public void testRhinoVersionES6UpperCase() {
        assertEquals(200, (int) new DefaultConfigurationController().getRhinoLanguageVersion("ES6"));
    }

    @Test
    public void testRhinoVersionDefault() {
        assertEquals(0, (int) new DefaultConfigurationController().getRhinoLanguageVersion("default"));
    }

    @Test
    public void testRhinoVersionUnknown() {
        assertEquals(0, (int) new DefaultConfigurationController().getRhinoLanguageVersion("asdf"));
    }

    private void assertDefaultDrivers(List<DriverInfo> drivers, boolean includeODBC) {
        assertEquals(includeODBC ? 6 : 5, drivers.size());
        int i = 0;

        if (includeODBC) {
            assertEquals("Sun JDBC-ODBC Bridge", drivers.get(i).getName());
            assertEquals("sun.jdbc.odbc.JdbcOdbcDriver", drivers.get(i).getClassName());
            assertEquals("jdbc:odbc:DSN", drivers.get(i).getTemplate());
            assertEquals("", drivers.get(i).getSelectLimit());
            assertEquals(new ArrayList<String>(), drivers.get(i).getAlternativeClassNames());
            i++;
        }

        assertEquals("MySQL", drivers.get(i).getName());
        assertEquals("com.mysql.cj.jdbc.Driver", drivers.get(i).getClassName());
        assertEquals("jdbc:mysql://host:port/dbname", drivers.get(i).getTemplate());
        assertEquals("SELECT * FROM ? LIMIT 1", drivers.get(i).getSelectLimit());
        assertEquals(Arrays.asList(new String[] {
                "com.mysql.jdbc.Driver" }), drivers.get(i).getAlternativeClassNames());
        i++;

        assertEquals("Oracle", drivers.get(i).getName());
        assertEquals("oracle.jdbc.driver.OracleDriver", drivers.get(i).getClassName());
        assertEquals("jdbc:oracle:thin:@host:port:dbname", drivers.get(i).getTemplate());
        assertEquals("SELECT * FROM ? WHERE ROWNUM < 2", drivers.get(i).getSelectLimit());
        assertEquals(new ArrayList<String>(), drivers.get(i).getAlternativeClassNames());
        i++;

        assertEquals("PostgreSQL", drivers.get(i).getName());
        assertEquals("org.postgresql.Driver", drivers.get(i).getClassName());
        assertEquals("jdbc:postgresql://host:port/dbname", drivers.get(i).getTemplate());
        assertEquals("SELECT * FROM ? LIMIT 1", drivers.get(i).getSelectLimit());
        assertEquals(new ArrayList<String>(), drivers.get(i).getAlternativeClassNames());
        i++;

        assertEquals("SQL Server/Sybase", drivers.get(i).getName());
        assertEquals("net.sourceforge.jtds.jdbc.Driver", drivers.get(i).getClassName());
        assertEquals("jdbc:jtds:sqlserver://host:port/dbname", drivers.get(i).getTemplate());
        assertEquals("SELECT TOP 1 * FROM ?", drivers.get(i).getSelectLimit());
        assertEquals(new ArrayList<String>(), drivers.get(i).getAlternativeClassNames());
        i++;

        assertEquals("SQLite", drivers.get(i).getName());
        assertEquals("org.sqlite.JDBC", drivers.get(i).getClassName());
        assertEquals("jdbc:sqlite:dbfile.db", drivers.get(i).getTemplate());
        assertEquals("SELECT * FROM ? LIMIT 1", drivers.get(i).getSelectLimit());
        assertEquals(new ArrayList<String>(), drivers.get(i).getAlternativeClassNames());
    }

    private String normalizeXml(String xml) throws Exception {
        Source source = new StreamSource(new StringReader(xml));
        Writer writer = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(writer));
        return writer.toString();
    }

    // @formatter:off
    private String DEFAULT_DRIVERS_XML = "<list>\n"+
            "  <driverInfo>\n"+
            "    <className>com.mysql.cj.jdbc.Driver</className>\n"+
            "    <name>MySQL</name>\n"+
            "    <template>jdbc:mysql://host:port/dbname</template>\n"+
            "    <selectLimit>SELECT * FROM ? LIMIT 1</selectLimit>\n"+
            "    <alternativeClassNames>\n"+
            "      <string>com.mysql.jdbc.Driver</string>\n"+
            "    </alternativeClassNames>\n"+
            "  </driverInfo>\n"+
            "  <driverInfo>\n"+
            "    <className>oracle.jdbc.driver.OracleDriver</className>\n"+
            "    <name>Oracle</name>\n"+
            "    <template>jdbc:oracle:thin:@host:port:dbname</template>\n"+
            "    <selectLimit>SELECT * FROM ? WHERE ROWNUM &lt; 2</selectLimit>\n"+
            "    <alternativeClassNames/>\n"+
            "  </driverInfo>\n"+
            "  <driverInfo>\n"+
            "    <className>org.postgresql.Driver</className>\n"+
            "    <name>PostgreSQL</name>\n"+
            "    <template>jdbc:postgresql://host:port/dbname</template>\n"+
            "    <selectLimit>SELECT * FROM ? LIMIT 1</selectLimit>\n"+
            "    <alternativeClassNames/>\n"+
            "  </driverInfo>\n"+
            "  <driverInfo>\n"+
            "    <className>net.sourceforge.jtds.jdbc.Driver</className>\n"+
            "    <name>SQL Server/Sybase</name>\n"+
            "    <template>jdbc:jtds:sqlserver://host:port/dbname</template>\n"+
            "    <selectLimit>SELECT TOP 1 * FROM ?</selectLimit>\n"+
            "    <alternativeClassNames/>\n"+
            "  </driverInfo>\n"+
            "  <driverInfo>\n"+
            "    <className>org.sqlite.JDBC</className>\n"+
            "    <name>SQLite</name>\n"+
            "    <template>jdbc:sqlite:dbfile.db</template>\n"+
            "    <selectLimit>SELECT * FROM ? LIMIT 1</selectLimit>\n"+
            "    <alternativeClassNames/>\n"+
            "  </driverInfo>\n"+
            "</list>\n";
    
    private String DEFAULT_DBDRIVERS_FILE = "<drivers>\n"+
            "   <driver class=\"sun.jdbc.odbc.JdbcOdbcDriver\" name=\"Sun JDBC-ODBC Bridge\" template=\"jdbc:odbc:DSN\" selectLimit=\"\" />\n"+
            "   <driver class=\"com.mysql.cj.jdbc.Driver\" alternativeClasses=\"com.mysql.jdbc.Driver\" name=\"MySQL\" template=\"jdbc:mysql://host:port/dbname\" selectLimit=\"SELECT * FROM ? LIMIT 1\" />\n"+
            "   <driver class=\"oracle.jdbc.driver.OracleDriver\" name=\"Oracle\" template=\"jdbc:oracle:thin:@host:port:dbname\" selectLimit=\"SELECT * FROM ? WHERE ROWNUM &lt; 2\" />\n"+
            "   <driver class=\"org.postgresql.Driver\" name=\"PostgreSQL\" template=\"jdbc:postgresql://host:port/dbname\" selectLimit=\"SELECT * FROM ? LIMIT 1\" />\n"+
            "   <driver class=\"net.sourceforge.jtds.jdbc.Driver\" name=\"SQL Server/Sybase\" template=\"jdbc:jtds:sqlserver://host:port/dbname\" selectLimit=\"SELECT TOP 1 * FROM ?\" />\n"+
            "   <driver class=\"org.sqlite.JDBC\" name=\"SQLite\" template=\"jdbc:sqlite:dbfile.db\" selectLimit=\"SELECT * FROM ? LIMIT 1\" />\n"+
            "</drivers>\n";
    // @formatter:on
}

/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.migration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.model.util.MigrationException;

public abstract class Migrator {
    private Connection connection;
    private String databaseType;
    private String defaultScriptPath;

    public abstract void migrate() throws MigrationException;

    public abstract void migrateSerializedData() throws MigrationException;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getDefaultScriptPath() {
        return defaultScriptPath;
    }

    public void setDefaultScriptPath(String defaultScriptPath) {
        this.defaultScriptPath = defaultScriptPath;
    }

    public List<String> getUninstallStatements() throws MigrationException {
        return null;
    }

    /**
     * Executes a SQL script
     * 
     * @param scriptFile
     *            The script file to execute. If scriptFile does not begin with a directory
     *            separator ('/'), the defaultScriptFolder is prepended.
     */
    protected void executeScript(String scriptFile) throws MigrationException {
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            List<String> statements = readStatements(scriptFile);

            connection.setAutoCommit(true);
            statement = connection.createStatement();

            for (String statementString : statements) {
                statement.execute(statementString);
            }
        } catch (Exception e) {
            throw new MigrationException("Failed to execute script: " + scriptFile, e);
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(resultSet);
        }
    }

    /**
     * Read statements from a SQL script
     * 
     * @param scriptResourceName
     *            The resource name of the script file to execute. If scriptResourceName does not
     *            begin with '/', the defaultScriptPath string is prepended.
     */
    protected List<String> readStatements(String scriptResourceName) throws IOException {
        List<String> script = new ArrayList<String>();
        Scanner scanner = null;

        if (scriptResourceName.charAt(0) != '/' && defaultScriptPath != null) {
            scriptResourceName = defaultScriptPath + "/" + scriptResourceName;
        }

        try {
            scanner = new Scanner(IOUtils.toString(ResourceUtil.getResourceStream(getClass(), scriptResourceName)));

            while (scanner.hasNextLine()) {
                StringBuilder stringBuilder = new StringBuilder();
                boolean blankLine = false;

                while (scanner.hasNextLine() && !blankLine) {
                    String temp = scanner.nextLine();

                    if (temp.trim().length() > 0) {
                        stringBuilder.append(temp + " ");
                    } else {
                        blankLine = true;
                    }
                }

                // Trim ending semicolons so Oracle doesn't throw
                // "java.sql.SQLException: ORA-00911: invalid character"
                String statementString = StringUtils.removeEnd(stringBuilder.toString().trim(), ";");

                if (statementString.length() > 0) {
                    script.add(statementString);
                }
            }

            return script;
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }
}

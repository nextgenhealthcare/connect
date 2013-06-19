package com.mirth.connect.server.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public abstract class Migrator {
    private Connection connection;
    private String databaseType;
    private String defaultScriptFolder;
    
    public abstract void migrate() throws MigrationException;
    
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

    public String getDefaultScriptFolder() {
        return defaultScriptFolder;
    }

    public void setDefaultScriptFolder(String defaultScriptFolder) {
        this.defaultScriptFolder = defaultScriptFolder;
    }
    
    /**
     * Executes a SQL migration script file
     * 
     * @param scriptFile
     *            The script file to execute. If scriptFile does not begin with a directory
     *            separator ('/'), the defaultScriptFolder is prepended.
     */
    protected void executeScript(String scriptFile) throws MigrationException {
        Statement statement = null;
        ResultSet resultSet = null;
        Scanner scanner = null;

        try {
            if (scriptFile.charAt(0) != IOUtils.DIR_SEPARATOR && defaultScriptFolder != null) {
                scriptFile = defaultScriptFolder + IOUtils.DIR_SEPARATOR + scriptFile;
            }

            String migrationScript = IOUtils.toString(getClass().getResourceAsStream(scriptFile));

            connection.setAutoCommit(true);
            statement = connection.createStatement();
            scanner = new Scanner(migrationScript);

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
                    statement.execute(statementString);
                }
            }
        } catch (Exception e) {
            throw new MigrationException(e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }

            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(resultSet);
        }
    }
}

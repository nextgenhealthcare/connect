package com.mirth.connect.server.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public abstract class ServerMigrator {
    private final static String DELTA_SCRIPT_FOLDER = IOUtils.DIR_SEPARATOR + "deltas";

    private Connection connection;
    private String databaseType;

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

    public abstract void migrate() throws DatabaseSchemaMigrationException;

    /**
     * Executes the given SQL migration script file on the provided connection.
     * 
     * @param connection
     * @param scriptFile
     *            The script file to execute. The file is assumed to be located in
     *            /deltas/[database-type]/, unless scriptFile begins with a directory separator
     *            ('/').
     */
    protected void executeDeltaScript(String scriptFile) throws DatabaseSchemaMigrationException {
        Statement statement = null;
        ResultSet resultSet = null;
        Scanner scanner = null;

        try {
            if (scriptFile.charAt(0) != IOUtils.DIR_SEPARATOR) {
                scriptFile = DELTA_SCRIPT_FOLDER + IOUtils.DIR_SEPARATOR + scriptFile;
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
            throw new DatabaseSchemaMigrationException(e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }

            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(resultSet);
        }
    }
}

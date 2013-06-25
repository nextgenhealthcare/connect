package com.mirth.connect.server.migration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.server.data.DonkeyDaoException;

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
            throw new MigrationException(e);
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
            scanner = new Scanner(IOUtils.toString(getClass().getResourceAsStream(scriptResourceName)));
            
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
    
    /**
     * Tell whether or not the given table exists in the database
     */
    protected boolean tableExists(String tableName) {
        ResultSet resultSet = null;

        try {
            DatabaseMetaData metaData = connection.getMetaData();
            resultSet = metaData.getTables(null, null, tableName.toUpperCase(), new String[] { "TABLE" });

            if (resultSet.next()) {
                return true;
            }

            resultSet = metaData.getTables(null, null, tableName.toLowerCase(), new String[] { "TABLE" });
            return resultSet.next();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
        }
    }
}

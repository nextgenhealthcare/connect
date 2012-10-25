/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;

public class DatabaseUtil {
    private static Logger logger = Logger.getLogger(DatabaseUtil.class);

    public static void executeScript(String script, boolean ignoreErrors) throws Exception {
        SqlSessionManager sqlSessionManger = SqlConfig.getSqlSessionManager();

        Connection conn = null;
        ResultSet resultSet = null;
        Statement statement = null;

        try {
            sqlSessionManger.startManagedSession();
            conn = sqlSessionManger.getConnection();

            /*
             * Set auto commit to false or an exception will be thrown when trying to rollback
             */
            conn.setAutoCommit(false);

            statement = conn.createStatement();

            Scanner s = new Scanner(script);

            while (s.hasNextLine()) {
                StringBuilder sb = new StringBuilder();
                boolean blankLine = false;

                while (s.hasNextLine() && !blankLine) {
                    String temp = s.nextLine();

                    if (temp.trim().length() > 0)
                        sb.append(temp + " ");
                    else
                        blankLine = true;
                }

                // Trim ending semicolons so Oracle doesn't throw
                // "java.sql.SQLException: ORA-00911: invalid character"
                String statementString = StringUtils.removeEnd(sb.toString().trim(), ";");

                if (statementString.length() > 0) {
                    try {
                        statement.execute(statementString);
                        conn.commit();
                    } catch (SQLException se) {
                        if (!ignoreErrors) {
                            throw se;
                        } else {
                            logger.error("Error was encountered and ignored while executing statement: " + statementString, se);
                            conn.rollback();
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(conn);
            sqlSessionManger.close();
        }
    }

    public static void executeScript(List<String> script, boolean ignoreErrors) throws Exception {
        SqlSessionManager sqlSessionManger = SqlConfig.getSqlSessionManager();

        Connection conn = null;
        ResultSet resultSet = null;
        Statement statement = null;

        try {
            sqlSessionManger.startManagedSession();
            conn = sqlSessionManger.getConnection();
            /*
             * Set auto commit to false or an exception will be thrown when trying to rollback
             */
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            for (String statementString : script) {
                statementString = statementString.trim();
                if (statementString.length() > 0) {
                    try {
                        statement.execute(statementString);
                        conn.commit();
                    } catch (SQLException se) {
                        if (!ignoreErrors) {
                            throw se;
                        } else {
                            logger.error("Error was encountered and ignored while executing statement: " + statementString, se);
                            conn.rollback();
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(conn);
            sqlSessionManger.close();
        }
    }

    /**
     * Returns true if the statement exists in the SqlMap, false otherwise.
     * 
     * @param statement
     *            the statement, including the namespace
     * @return
     */
    public static boolean statementExists(String statement) {
        try {
            SqlConfig.getSqlSessionManager().getConfiguration().getMappedStatement(statement);
        } catch (IllegalArgumentException e) {
            // The statement does not exist
            return false;
        }

        return true;
    }

    /**
     * Returns true if the statement exists in the SqlMap, false otherwise.
     * 
     * @param statement
     *            the statement, including the namespace
     * @param session
     *            the SqlMapSession to use
     * @return
     */
    public static boolean statementExists(String statement, SqlSession session) {
        try {
            session.getConfiguration().getMappedStatement(statement);
        } catch (IllegalArgumentException e) {
            // The statement does not exist
            return false;
        }

        return true;
    }

    public static List<String> joinSqlStatements(Collection<String> scripts) {
        List<String> scriptList = new ArrayList<String>();

        for (String script : scripts) {
            script = script.trim();
            StringBuilder sb = new StringBuilder();
            boolean blankLine = false;
            Scanner scanner = new Scanner(script);

            while (scanner.hasNextLine()) {
                String temp = scanner.nextLine();

                if (temp.trim().length() > 0) {
                    sb.append(temp + " ");
                } else {
                    blankLine = true;
                }

                if (blankLine || !scanner.hasNextLine()) {
                    scriptList.add(sb.toString().trim());
                    blankLine = false;
                    sb.delete(0, sb.length());
                }
            }
        }

        return scriptList;
    }
}

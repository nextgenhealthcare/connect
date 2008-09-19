/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.util;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;

public class DatabaseUtil {
    
    private Logger logger = Logger.getLogger(this.getClass());
    
	/**
	 * Closes the specified ResultSet.
	 * 
	 * @param result
	 *            the ResultSet to be closed.
	 * @throws RuntimeException
	 */
	public static void close(ResultSet result) throws RuntimeException {
		try {
			if (result != null) {
				result.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Closes the specified Statement.
	 * 
	 * @param statement
	 *            the Statement to be closed.
	 * @throws RuntimeException
	 */
	public static void close(Statement statement) throws RuntimeException {
		try {
			if (statement != null) {
				statement.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void close(DatabaseConnection connection) throws RuntimeException {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void close(Connection connection) throws RuntimeException {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    
    public static void executeScript(File script, boolean ignoreErrors) throws Exception {
        SqlMapClient sqlMap = SqlConfig.getSqlMapClient();
        
        Connection conn = null;
        ResultSet resultSet = null;
        Statement statement = null;
        
        try {
            conn = sqlMap.getDataSource().getConnection();
            statement = conn.createStatement();
            
            Scanner s = new Scanner(script);
            while(s.hasNextLine()) {
                StringBuilder sb = new StringBuilder();
                boolean blankLine = false;
                
                while(s.hasNextLine() && !blankLine)
                {
                    String temp = s.nextLine();
                    
                    if (temp.trim().length() > 0)
                    	sb.append(temp + " ");
                    else
                        blankLine = true;
                }
                
                String statmentString = sb.toString().trim();
                if(statmentString.length() > 0)
                {
                	try { 
                		statement.execute(statmentString);
                		conn.commit(); 
                	} catch (SQLException se) { 
                		if(!ignoreErrors) { 
                			throw se;
                		} else { 
                			conn.rollback();
                		}
                	}
                }
            }          
                            
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            close(statement);    
            close(resultSet);
            close(conn);
        }
    }
}

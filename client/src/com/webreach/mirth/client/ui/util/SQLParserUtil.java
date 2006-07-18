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


package com.webreach.mirth.client.ui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

import org.apache.log4j.Logger;

/*
 * Parses a sql statement for column names
 */
public class SQLParserUtil {
	private String REGEX = "`[^`]*`";
	String _sqlStatement = "";	
	private Logger logger = Logger.getLogger(this.getClass());
	public SQLParserUtil(String statement){
		_sqlStatement = statement;
	}
	public SQLParserUtil(){
		
	}
	public String[] Parse(String statement){
		_sqlStatement = statement;
		return Parse();
	}
	public String[] Parse(){
		
		try{
			//Pattern pattern = Pattern.compile(REGEX);
			int fromClause = _sqlStatement.toUpperCase().indexOf("FROM");
			if (fromClause > 0){
				String columnText = _sqlStatement.substring(7, fromClause).trim();
				return columnText.replaceAll(" ", "").replaceAll("`","").split(",");
				
			}
			return new String[0];
		}catch(Exception e){
			logger.error(e);
			return new String[0];
		}
	}
    public static void main(String[] args){
    	SQLParserUtil squ = new SQLParserUtil("SELECT `pd_lname`,`pd_fname`,    `pd_tname` FROM `patients`;");
    	String[] columns = squ.Parse();
    	for(int i = 0; i < columns.length; i++){
    		System.out.println(columns[i]);
    	}
    }
}

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

import org.apache.log4j.Logger;

/*
 * Parses a sql statement for column names
 */
public class SQLParserUtil
{
    private Logger logger = Logger.getLogger(this.getClass());
    private String[] keywords = {"INTO", "DISTINCT", "UNIQUE", "FIRST", "MIDDLE", "SKIP", "LIMIT"};
    String _sqlStatement = "";
    
    public SQLParserUtil(String statement)
    {
        _sqlStatement = statement;
    }
    public SQLParserUtil()
    {
        
    }
    public String[] Parse(String statement)
    {
        _sqlStatement = statement;
        return Parse();
    }
    public String[] Parse()
    {
        try
        {
            //Pattern pattern = Pattern.compile(REGEX);
            ArrayList<String> varList = new ArrayList<String>();
            int fromClause = _sqlStatement.toUpperCase().indexOf("FROM");
            int selectClause = _sqlStatement.toUpperCase().indexOf("SELECT");
            if (fromClause > 0)
            {
                String columnText = _sqlStatement.substring(selectClause + 6, fromClause); //replaceAll(" ", "").replaceAll("\\(","").replaceAll("\\)","")
                String[] vars = columnText.replaceAll("`","").split(",");
                for(int i = 0; i < vars.length; i++)
                {
                    vars[i] = vars[i].trim();
                    if(vars[i].length() > 0)
                    {
                        for(int j = 0; j < keywords.length; j++)
                        {
                            int index = vars[i].toUpperCase().indexOf(keywords[j] + " ");
                            int size = (keywords[j] + " ").length();
                            if(index != -1)
                            {
                                vars[i] = vars[i].replaceAll(vars[i].substring(index, index + size), "");
                            }
                        }
                        vars[i] = vars[i].trim();
                        vars[i] = vars[i].toLowerCase();
                        if(vars[i].length() > 0)
                        {
                            if(vars[i].toUpperCase().indexOf(" AS ") != -1)
                            {
                                varList.add( (vars[i].substring(vars[i].toUpperCase().indexOf(" AS ") + 4)).replaceAll(" ", "").replaceAll("\\(","").replaceAll("\\)","") );
                            }
                            else if (vars[i].indexOf('(') != -1 || vars[i].indexOf(')') != -1 || vars[i].indexOf('}') != -1 || vars[i].indexOf('{') != -1 ||  vars[i].indexOf('*') != -1)
                            {
                                continue;
                            }
                            else
                            {
                                varList.add( vars[i].replaceAll(" ", "").replaceAll("\\(","").replaceAll("\\)","") );
                            }
                        }
                       
                    }
                }
                return varList.toArray( new String[varList.size()] );
            }
        }
        catch(Exception e)
        {
            logger.error(e);           
        }
        return new String[0];
    }
    public static void main(String[] args)
    {
        SQLParserUtil squ = new SQLParserUtil("SELECT `pd_lname`,`pd_fname`,    `pd_tname` FROM `patients`;");
        String[] columns = squ.Parse();
        for(int i = 0; i < columns.length; i++)
        {
            System.out.println(columns[i]);
        }
    }
}

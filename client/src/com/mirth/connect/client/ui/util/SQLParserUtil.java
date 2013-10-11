/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.util;

import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/*
 * Parses a sql statement for column names
 */
public class SQLParserUtil {

    private Logger logger = Logger.getLogger(this.getClass());
    private String[] keywords = {"INTO", "DISTINCT", "UNIQUE", "FIRST", "MIDDLE", "SKIP", "LIMIT"};
    private final String SQL_PATTERN = "[s|S][e|E][l|L][e|E][c|C][t|T].*[f|F][r|R][o|O][m|M][\\s]";
    String _sqlStatement = "";

    public SQLParserUtil(String statement) {
        _sqlStatement = statement.replaceAll("\\[", "").replaceAll("\\]", "").replace('\n', ' ').replace('\r', ' ');
    }

    public SQLParserUtil() {
    }

    public String[] Parse(String statement) {
        _sqlStatement = statement.replaceAll("\\[", "").replaceAll("\\]", "").replace('\n', ' ').replace('\r', ' ');
        return Parse();
    }

    public String[] Parse() {
        try {
            LinkedHashSet<String> varList = new LinkedHashSet<String>();

            Pattern pattern = Pattern.compile(SQL_PATTERN, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(_sqlStatement);
            while (matcher.find()) {
                String key = matcher.group();
                int fromClause = key.toUpperCase().indexOf(" FROM ");

                if (fromClause > 0) {
                    String columnText = key.substring(6, fromClause).replaceAll("`", "");
                    columnText = removeNestedFunctions(columnText, 0);

                    String[] vars = columnText.split(",");

                    for (int i = 0; i < vars.length; i++) {
                        if (vars[i].length() > 0) {
                            for (int j = 0; j < keywords.length; j++) {
                                int index = vars[i].toUpperCase().indexOf(keywords[j]);
                                int size = (keywords[j]).length();
                                if (index != -1) {
                                    if (index > 0) {
                                        if (vars[i].substring(index - 1, index).equals(" ") && (vars[i].length() == index + size || vars[i].substring(index + size, index + size + 1).equals(" "))) {
                                            vars[i] = vars[i].replaceAll(vars[i].substring(index, index + size), "");
                                        }
                                    } else if (vars[i].length() == index + size || vars[i].substring(index + size, index + size + 1).equals(" ")) {
                                        vars[i] = vars[i].replaceAll(vars[i].substring(index, index + size), "");
                                    }
                                }
                            }
                            if (vars[i].length() > 0) {
                                String var;
                                
                                if (vars[i].toUpperCase().indexOf(" AS ") != -1) {
                                    var = (vars[i].substring(vars[i].toUpperCase().indexOf(" AS ") + 4)).replaceAll(" ", "").replaceAll("\\(", "").replaceAll("\\)", "");
                                } else if (vars[i].indexOf('(') != -1 || vars[i].indexOf(')') != -1 || vars[i].indexOf('}') != -1 || vars[i].indexOf('{') != -1 || vars[i].indexOf('*') != -1) {
                                    continue;
                                } else {
                                    vars[i] = vars[i].trim();
                                    var = vars[i].replaceAll(" ", "").replaceAll("\\(", "").replaceAll("\\)", "");
                                }

                                if ((StringUtils.substring(var, 0, 1).equals("\"") && StringUtils.substring(var, -1).equals("\"")) || (StringUtils.substring(var, 0, 1).equals("'") && StringUtils.substring(var, -1).equals("'"))) {
                                    var = StringUtils.substring(var, 1, -1);
                                }
                                
                                if ((StringUtils.substring(var, 0, 2).equals("\\\"") && StringUtils.substring(var, -2).equals("\\\"")) || (StringUtils.substring(var, 0, 2).equals("\\'") && StringUtils.substring(var, -2).equals("\\'"))) {
                                    var = StringUtils.substring(var, 2, -2);
                                }

                                var = StringUtils.lowerCase(var);

                                varList.add(var);
                            }
                        }
                    }
                }
            }
            return varList.toArray(new String[varList.size()]);
        } catch (Exception e) {
            logger.error(e);
        }
        return new String[0];
    }

    private String removeNestedFunctions(String string, int currentIndex) {
        while (currentIndex < string.length()) {
            if (string.charAt(currentIndex) == '(') {
                string = removeNestedFunctions(string, currentIndex + 1);
            } else if (string.charAt(currentIndex) == ')') {
                string = string.substring(0, string.substring(0, currentIndex).lastIndexOf('(')) + string.substring(currentIndex + 1, string.length());
                return string;
            }

            currentIndex++;
        }

        return string;
    }

    public static void main(String[] args) {
        SQLParserUtil squ = new SQLParserUtil("SELECT `pd_lname`,`pd_fname`,    `pd_tname` FROM `patients`;");
        String[] columns = squ.Parse();
        for (int i = 0; i < columns.length; i++) {
            System.out.println(columns[i]);
        }
    }
}

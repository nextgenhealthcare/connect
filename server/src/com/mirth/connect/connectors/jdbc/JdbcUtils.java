/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.server.util.AttachmentUtil;
import com.mirth.connect.server.util.TemplateValueReplacer;

import edu.emory.mathcs.backport.java.util.Arrays;

public class JdbcUtils {
    public static void close(Connection con) throws SQLException {
        if (con != null && !con.isClosed()) {
            con.close();
        }
    }

    public static void commitAndClose(Connection con) throws SQLException {
        if (con != null && !con.isClosed()) {
            if (con.getAutoCommit() == false) {
                con.commit();
            }

            con.close();
        }
    }

    public static void rollbackAndClose(Connection con) throws SQLException {
        if (con != null && !con.isClosed()) {
            if (con.getAutoCommit() == false) {
                con.rollback();
            }

            con.close();
        }
    }

    /**
     * Parse the given statement filling the parameter list and return the ready
     * to use statement.
     * 
     * @param statement
     * @param params
     * @return
     */
    public static String parseStatement(String statement, List<String> params) {
        if (statement != null) {
            Pattern p = Pattern.compile("\\$\\{[^\\}]*\\}");
            Matcher m = p.matcher(statement);
            StringBuffer sb = new StringBuffer();

            while (m.find()) {
                String key = m.group();
                m.appendReplacement(sb, "?");
                params.add(key);
            }

            m.appendTail(sb);
            return sb.toString();

        } else {
            return statement;
        }
    }

    /*
     * Takes a multi-line SQL statement and removes comments based on the following rule:
     * - If the line starts with a comment marker, remove it
     * - If the line contains a comment marker, remove everything after the comment marker
     */
    public static String stripSqlComments(String statement) {
        if (StringUtils.isNotBlank(statement)) {
            List<String> lines = Arrays.asList(statement.split("\n"));
            StringBuilder result = new StringBuilder();
            
            for (String line : lines) {
                if (line.trim().startsWith("--")) {
                    // ignore it
                } else if (line.contains("--")) {
                    result.append(line.substring(0, line.indexOf("--")).trim());
                    result.append("\n");
                } else {
                    result.append(line);
                    result.append("\n");
                }
            }
            
            return result.toString();
        } else {
            return statement;
        }
    }

    public static Object[] getParams(List<String> paramNames, Object root) throws Exception {
        Object[] params = new Object[paramNames.size()];

        for (int i = 0; i < paramNames.size(); i++) {
            String param = paramNames.get(i);
            String name = param.substring(2, param.length() - 1);
            Object value = null;

            if ("NOW".equalsIgnoreCase(name)) {
                value = new Timestamp(Calendar.getInstance().getTimeInMillis());
            } else if ("payload".equals(name)) {
                value = root;
            } else if (root instanceof ConnectorMessage) {
                TemplateValueReplacer parser = new TemplateValueReplacer();
                value = parser.replaceValues(param, (ConnectorMessage) root);
                if (AttachmentUtil.hasAttachmentKeys((String) value)) {
                    value = org.apache.commons.codec.binary.StringUtils.newString(AttachmentUtil.reAttachMessage((String) value, (ConnectorMessage) root, Constants.ATTACHMENT_CHARSET, false), Constants.ATTACHMENT_CHARSET);
                }
            } else if (root instanceof Map) {
                value = ((Map) root).get(name);
            }
            
            // TODO: Is this needed?
//            else if (root instanceof org.w3c.dom.Document) {
//                org.w3c.dom.Document x3cDoc = (org.w3c.dom.Document) root;
//                org.dom4j.Document dom4jDoc = new DOMReader().read(x3cDoc);
//
//                try {
//                    Node node = dom4jDoc.selectSingleNode(name);
//                    if (node != null) {
//                        value = node.getText();
//                    }
//                } catch (Exception ignored) {
//                    value = null;
//                }
//            } else if (root instanceof org.dom4j.Document) {
//                org.dom4j.Document dom4jDoc = (org.dom4j.Document) root;
//
//                try {
//                    Node node = dom4jDoc.selectSingleNode(name);
//                    if (node != null) {
//                        value = node.getText();
//                    }
//                } catch (Exception ignored) {
//                    value = null;
//                }
//            } else if (root instanceof org.dom4j.Node) {
//                org.dom4j.Node dom4jNode = (org.dom4j.Node) root;
//
//                try {
//                    Node node = dom4jNode.selectSingleNode(name);
//                    if (node != null) {
//                        value = node.getText();
//                    }
//                } catch (Exception ignored) {
//                    value = null;
//                }
//            } else {
//                try {
//                    value = BeanUtils.getProperty(root, name);
//                } catch (Exception ignored) {
//                    value = null;
//                }
//            }

            // TODO: Is this needed?
//            if (value == null) {
//                value = uri.getParams().getProperty(name);
//            }

            if (value == null) {
                throw new IllegalArgumentException("Can not retrieve argument " + name);
            }

            params[i] = value;
        }

        return params;
    }

}

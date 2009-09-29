package com.webreach.mirth.connectors.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.endpoint.UMOEndpointURI;

import com.webreach.mirth.model.MessageObject;

public abstract class JdbcUtils {
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
    public static String parseStatement(String statement, List params) {
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

    public static Object[] getParams(UMOEndpointURI uri, List paramNames, Object root) throws Exception {
        Object[] params = new Object[paramNames.size()];

        for (int i = 0; i < paramNames.size(); i++) {
            String param = (String) paramNames.get(i);
            String name = param.substring(2, param.length() - 1);
            Object value = null;

            if ("NOW".equalsIgnoreCase(name)) {
                value = new Timestamp(Calendar.getInstance().getTimeInMillis());
            } else if ("payload".equals(name)) {
                value = root;
            } else if (root instanceof MessageObject) {
                TemplateValueReplacer parser = new TemplateValueReplacer();
                value = parser.replaceValues(param, (MessageObject) root);
            } else if (root instanceof Map) {
                value = ((Map) root).get(name);
            } else if (root instanceof org.w3c.dom.Document) {
                org.w3c.dom.Document x3cDoc = (org.w3c.dom.Document) root;
                org.dom4j.Document dom4jDoc = new DOMReader().read(x3cDoc);
                
                try {
                    Node node = dom4jDoc.selectSingleNode(name);
                    if (node != null) {
                        value = node.getText();
                    }
                } catch (Exception ignored) {
                    value = null;
                }
            } else if (root instanceof org.dom4j.Document) {
                org.dom4j.Document dom4jDoc = (org.dom4j.Document) root;
                
                try {
                    Node node = dom4jDoc.selectSingleNode(name);
                    if (node != null) {
                        value = node.getText();
                    }
                } catch (Exception ignored) {
                    value = null;
                }
            } else if (root instanceof org.dom4j.Node) {
                org.dom4j.Node dom4jNode = (org.dom4j.Node) root;
                
                try {
                    Node node = dom4jNode.selectSingleNode(name);
                    if (node != null) {
                        value = node.getText();
                    }
                } catch (Exception ignored) {
                    value = null;
                }
            } else {
                try {
                    value = BeanUtils.getProperty(root, name);
                } catch (Exception ignored) {
                    value = null;
                }
            }

            if (value == null) {
                value = uri.getParams().getProperty(name);
            }

            if (value == null) {
                throw new IllegalArgumentException("Can not retrieve argument " + name);
            }
            
            params[i] = value;
        }
        
        return params;
    }

}

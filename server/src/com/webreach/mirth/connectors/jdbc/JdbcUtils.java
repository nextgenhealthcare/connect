/*
 * $Header: /home/projects/mule/scm/mule/providers/jdbc/src/java/org/mule/providers/jdbc/JdbcUtils.java,v 1.9 2005/11/10 02:50:13 lajos Exp $
 * $Revision: 1.9 $
 * $Date: 2005/11/10 02:50:13 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
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

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.9 $
 */
public abstract class JdbcUtils
{
	
	public static void close(Connection con) throws SQLException
	{
		if (con != null) {
			con.close();
		}
	}
	
	public static void commitAndClose(Connection con) throws SQLException
	{
		if (con != null) {
			if (con.getAutoCommit() == false) {
				con.commit();
			}
			con.close();
		}
	}
	
	public static void rollbackAndClose(Connection con) throws SQLException
	{
		if (con != null) {
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
	 * @param stmt
	 * @param params
	 * @return
	 */
	public static String parseStatement(String stmt, List params)
	{
		if (stmt == null) {
			return stmt;
		}
		Pattern p = Pattern.compile("\\$\\{[^\\}]*\\}");
		Matcher m = p.matcher(stmt);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String key = m.group();
			m.appendReplacement(sb, "?");
			params.add(key);
		}
		m.appendTail(sb);
		return sb.toString();
	}
	
	public static Object[] getParams(UMOEndpointURI uri, List paramNames, Object root) throws Exception
	{
		Object[] params = new Object[paramNames.size()];
		for (int i = 0; i < paramNames.size(); i++) {
			String param = (String) paramNames.get(i);
			String name = param.substring(2, param.length() - 1);
			Object value = null;
			
			if ("NOW".equalsIgnoreCase(name)) {
				value = new Timestamp(Calendar.getInstance().getTimeInMillis());
			} else if (root instanceof MessageObject){
				TemplateValueReplacer parser = new TemplateValueReplacer();
				value = parser.replaceValues(param, (MessageObject)root);
				/*
				value = parser.
				//If we have a hashmap cast our root object to a HashMap
				Map valueMap = ((MessageObject) root).getVariableMap();
				
				try{
					if (valueMap.containsKey(name)){
						//Assign the value to the value in the hash with the key we're looking for (name)
						value = valueMap.get(name).toString();
					}else if (GlobalVariableStore.getInstance().containsKey(name)){
						//Try to get Mirth global hash
						value = GlobalVariableStore.getInstance().get(name).toString();
					}
				}catch (Exception ignored){
					value = null;
				}
				*/
			}else if (root instanceof Map){
				value = ((Map)root).get(name);
			}else if (root instanceof org.w3c.dom.Document) {				
				
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
			if(name.equals("payload")) {
				value = root;
			}
			
			if (value == null) {
				throw new IllegalArgumentException("Can not retrieve argument " + name);
			}
			params[i] = value;
		}
		return params;
	}
	
}

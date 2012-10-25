/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.sqlmap.extensions;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class MapTypeHandler implements TypeHandler {
    //TODO use this typehandler for inserting maps into DB. However, this and ObjectXMLSerializer are on the server, not donkey.
    private ObjectXMLSerializer serializer = new ObjectXMLSerializer();

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType arg3) throws SQLException {
        Map parameterMap = (Map) parameter;
        Map map = new HashMap();

        // convert the values in the variable map to strings
        for (Iterator iter = parameterMap.entrySet().iterator(); iter.hasNext();) {
            Entry entry = (Entry) iter.next();
            String keyName = new String();
            if (entry.getKey() != null) {
                keyName = entry.getKey().toString();
                String keyValue = new String();
                if (entry.getValue() == null) {
                    keyValue = "";
                } else {
                    keyValue = entry.getValue().toString();
                }
                map.put(keyName, keyValue);
            }
        }
        ps.setString(i, serializer.toXML(map));
    }

    public Object valueOf(String source) {
        return source;
    }

    @Override
    public Object getResult(ResultSet resultSet, String columnName) throws SQLException {
        return (Map) serializer.fromXML(resultSet.getString(columnName));
    }

    @Override
    public Object getResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return (Map) serializer.fromXML(resultSet.getString(columnIndex));
    }

    @Override
    public Object getResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        String resultString = callableStatement.getString(columnIndex);

        if (resultString != null) {
            return (Map) serializer.fromXML(resultString);
        }
        return null;
    }

}
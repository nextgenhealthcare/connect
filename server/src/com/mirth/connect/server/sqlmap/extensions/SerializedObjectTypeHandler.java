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

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class SerializedObjectTypeHandler implements TypeHandler {
    private ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();

    @Override
    public void setParameter(PreparedStatement statement, int i, Object parameter, JdbcType jdbc) throws SQLException {
        if (parameter == null) {
            statement.setNull(i, java.sql.Types.LONGVARCHAR);
        } else {
            statement.setString(i, serializer.toXML(parameter));
        }
    }

    @Override
    public Object getResult(ResultSet resultSet, String columnName) throws SQLException {
        return serializer.fromXML(resultSet.getString(columnName));
    }

    @Override
    public Object getResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return serializer.fromXML(resultSet.getString(columnIndex));
    }

    @Override
    public Object getResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        String resultString = callableStatement.getString(columnIndex);

        if (resultString != null) {
            return serializer.fromXML(resultString);
        }

        return null;
    }

    public Object valueOf(String source) {
        return source;
    }
}
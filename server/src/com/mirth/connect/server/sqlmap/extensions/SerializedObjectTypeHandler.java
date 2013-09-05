/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.sqlmap.extensions;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.mirth.connect.model.converters.ObjectXMLSerializer;

public abstract class SerializedObjectTypeHandler<T> implements TypeHandler<T> {
    private Class<T> clazz;

    protected SerializedObjectTypeHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void setParameter(PreparedStatement statement, int i, T parameter, JdbcType jdbc) throws SQLException {
        if (parameter == null) {
            statement.setNull(i, java.sql.Types.LONGVARCHAR);
        } else {
            statement.setString(i, ObjectXMLSerializer.getInstance().serialize(parameter));
        }
    }

    @Override
    public T getResult(ResultSet resultSet, String columnName) throws SQLException {
        return ObjectXMLSerializer.getInstance().deserialize(resultSet.getString(columnName), clazz);
    }

    @Override
    public T getResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return ObjectXMLSerializer.getInstance().deserialize(resultSet.getString(columnIndex), clazz);
    }

    @Override
    public T getResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        String resultString = callableStatement.getString(columnIndex);

        if (resultString != null) {
            return ObjectXMLSerializer.getInstance().deserialize(resultString, clazz);
        }

        return null;
    }
}
/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.sqlmap.extensions;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

public abstract class EnumTypeHandler<E extends Enum> implements TypeHandler {
    private Class<E> enumClass;

    public EnumTypeHandler(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public void setParameter(PreparedStatement statement, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            statement.setNull(i, java.sql.Types.VARCHAR);
        } else {
            statement.setString(i, ((E) parameter).name());
        }
    }

    @Override
    public Object getResult(ResultSet resultSet, String columnName) throws SQLException {
        return valueOf(resultSet.getString(columnName));
    }

    @Override
    public Object getResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return valueOf(resultSet.getString(columnIndex));
    }

    @Override
    public Object getResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        return valueOf(callableStatement.getString(columnIndex));
    }

    @SuppressWarnings("unchecked")
    public Object valueOf(String s) {
        return Enum.valueOf(enumClass, s);
    }
}
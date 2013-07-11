/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.sqlmap.extensions;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class MapTypeHandler implements TypeHandler<Map<?, ?>> {
    @Override
    public Map<?, ?> getResult(ResultSet resultSet, String columnName) throws SQLException {
        return ObjectXMLSerializer.getInstance().deserialize(resultSet.getString(columnName), Map.class);
    }

    @Override
    public Map<?, ?> getResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return ObjectXMLSerializer.getInstance().deserialize(resultSet.getString(columnIndex), Map.class);
    }

    @Override
    public Map<?, ?> getResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        return ObjectXMLSerializer.getInstance().deserialize(callableStatement.getString(columnIndex), Map.class);
    }

    @Override
    public void setParameter(PreparedStatement preparedStatement, int columnIndex, Map<?, ?> parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            preparedStatement.setNull(columnIndex, java.sql.Types.LONGVARCHAR);
        } else {
            preparedStatement.setString(columnIndex, ObjectXMLSerializer.getInstance().serialize((Serializable) parameter));
        }
    }
}

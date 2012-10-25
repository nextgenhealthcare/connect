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

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.mirth.connect.donkey.server.Donkey;

public class SerializableHandler implements TypeHandler {
    @Override
    public Object getResult(ResultSet resultSet, String columnName) throws SQLException {
        return Donkey.getInstance().getSerializer().deserialize(resultSet.getString(columnName));
    }

    @Override
    public Object getResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return Donkey.getInstance().getSerializer().deserialize(resultSet.getString(columnIndex));
    }

    @Override
    public Object getResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        return Donkey.getInstance().getSerializer().deserialize(callableStatement.getString(columnIndex));
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, java.sql.Types.LONGVARCHAR);
        } else {
            ps.setString(i, Donkey.getInstance().getSerializer().serialize((Serializable) parameter));
        }
    }
}

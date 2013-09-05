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

import com.mirth.connect.donkey.model.message.Status;

public class StatusTypeHandler implements TypeHandler {

    @Override
    public Object getResult(ResultSet resultSet, String columnName) throws SQLException {
        return Status.fromChar(resultSet.getString(columnName).charAt(0));
    }

    @Override
    public Object getResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return Status.fromChar(resultSet.getString(columnIndex).charAt(0));
    }

    @Override
    public Object getResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        return Status.fromChar(callableStatement.getString(columnIndex).charAt(0));
    }

    @Override
    public void setParameter(PreparedStatement statement, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            statement.setNull(i, java.sql.Types.CHAR);
        } else {
            statement.setString(i, Character.toString(((Status) parameter).getStatusCode()));
        }
    }
}

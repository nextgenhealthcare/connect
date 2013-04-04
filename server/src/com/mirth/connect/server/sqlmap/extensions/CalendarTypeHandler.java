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
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

public class CalendarTypeHandler implements TypeHandler {
    @Override
    public void setParameter(final PreparedStatement ps, final int i, final Object parameter, final JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, java.sql.Types.LONGVARCHAR);
        } else {
            Calendar calendar = (Calendar) parameter;
            ps.setTimestamp(i, new Timestamp(calendar.getTimeInMillis()));
        }
    }

    @Override
    public Object getResult(final ResultSet resultSet, final String columnName) throws SQLException {
        if (resultSet.getTimestamp(columnName) == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(resultSet.getTimestamp(columnName).getTime());
        return calendar;
    }

    @Override
    public Object getResult(ResultSet resultSet, int columnIndex) throws SQLException {
        if (resultSet.getTimestamp(columnIndex) == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(resultSet.getTimestamp(columnIndex).getTime());
        return calendar;
    }

    @Override
    public Object getResult(final CallableStatement callableStatement, final int columnIndex) throws SQLException {
        if (callableStatement.getTimestamp(columnIndex) == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(callableStatement.getTimestamp(columnIndex).getTime());
        return calendar;
    }

    public Object valueOf(String source) {
        if (source == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.valueOf(source));
        return calendar;
    }
}
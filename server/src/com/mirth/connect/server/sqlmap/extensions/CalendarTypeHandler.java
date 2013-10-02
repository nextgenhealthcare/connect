/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.sqlmap.extensions;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

public class CalendarTypeHandler implements TypeHandlerCallback {
    public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
        if (parameter == null) {
            setter.setTimestamp(null);
        } else {
            Calendar calendar = (Calendar) parameter;
            setter.setTimestamp(new Timestamp(calendar.getTimeInMillis()));
        }
    }

    public Object getResult(ResultGetter getter) throws SQLException {
        if (getter.getTimestamp() == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getter.getTimestamp().getTime());
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
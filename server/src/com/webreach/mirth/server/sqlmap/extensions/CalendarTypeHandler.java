package com.webreach.mirth.server.sqlmap.extensions;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

public class CalendarTypeHandler implements TypeHandlerCallback {
	public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
		Calendar calendar = (Calendar) parameter;
		setter.setTimestamp(new Timestamp(calendar.getTimeInMillis()));
	}

	public Object getResult(ResultGetter getter) throws SQLException {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(getter.getTimestamp().getTime());
		return calendar;
	}

	public Object valueOf(String source) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Long.valueOf(source));
		return calendar;
	}	
}
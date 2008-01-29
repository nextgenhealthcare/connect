package com.webreach.mirth.server.sqlmap.extensions;

import java.sql.SQLException;

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

public abstract class EnumTypeHandler<E extends Enum> implements TypeHandlerCallback {
	private Class<E> enumClass;

	public EnumTypeHandler(Class<E> enumClass) {
		this.enumClass = enumClass;
	}

	@SuppressWarnings("unchecked")
	public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
		setter.setString(((E) parameter).name());
	}

	public Object getResult(ResultGetter getter) throws SQLException {
		return valueOf(getter.getString());
	}

	@SuppressWarnings("unchecked")
	public Object valueOf(String s) {
		return Enum.valueOf(enumClass, s);
	}
}
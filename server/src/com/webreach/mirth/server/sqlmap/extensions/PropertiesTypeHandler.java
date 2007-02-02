package com.webreach.mirth.server.sqlmap.extensions;

import java.sql.SQLException;
import java.util.Properties;

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

public class PropertiesTypeHandler implements TypeHandlerCallback {
	private ObjectXMLSerializer serializer = new ObjectXMLSerializer();

	public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
		Properties properties = (Properties) parameter;
		setter.setString(serializer.toXML(properties));
	}

	public Object getResult(ResultGetter getter) throws SQLException {
		return (Properties) serializer.fromXML(getter.getString());
	}

	public Object valueOf(String source) {
		return source;
	}
}
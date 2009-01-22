package com.webreach.mirth.server.sqlmap.extensions;

import java.sql.SQLException;

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

public class ConnectorTypeHandler implements TypeHandlerCallback {
	private ObjectXMLSerializer serializer = new ObjectXMLSerializer();

	public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
		Connector connector = (Connector) parameter;
		setter.setString(serializer.toXML(connector));
	}

	public Object getResult(ResultGetter getter) throws SQLException {
		return (Connector) serializer.fromXML(getter.getString());
	}

	public Object valueOf(String source) {
		return source;
	}
}
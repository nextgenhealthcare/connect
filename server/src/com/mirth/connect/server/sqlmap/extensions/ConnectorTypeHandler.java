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

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

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
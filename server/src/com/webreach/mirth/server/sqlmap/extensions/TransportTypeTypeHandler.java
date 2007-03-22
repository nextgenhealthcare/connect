package com.webreach.mirth.server.sqlmap.extensions;

import com.webreach.mirth.model.Transport.Type;

public class TransportTypeTypeHandler extends EnumTypeHandler<Type> {
	public TransportTypeTypeHandler() {
		super(Type.class);
	}
}
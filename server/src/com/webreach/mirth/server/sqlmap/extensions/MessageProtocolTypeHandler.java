package com.webreach.mirth.server.sqlmap.extensions;

import com.webreach.mirth.model.MessageObject.Protocol;

public class MessageProtocolTypeHandler extends EnumTypeHandler<Protocol> {
	public MessageProtocolTypeHandler() {
		super(Protocol.class);
	}
}
package com.webreach.mirth.server.sqlmap.extensions;

import com.webreach.mirth.model.Channel.Protocol;

public class ChannelProtocolTypeHandler extends EnumTypeHandler<Protocol> {
	public ChannelProtocolTypeHandler() {
		super(Protocol.class);
	}
}
package com.webreach.mirth.server.sqlmap.extensions;

import com.webreach.mirth.model.Channel.Mode;

public class ChannelModeTypeHandler extends EnumTypeHandler<Mode> {
	public ChannelModeTypeHandler() {
		super(Mode.class);
	}
}
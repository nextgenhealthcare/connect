package com.webreach.mirth.server.sqlmap.extensions;

import com.webreach.mirth.model.Channel.Direction;

public class ChannelDirectionTypeHandler extends EnumTypeHandler<Direction> {
	public ChannelDirectionTypeHandler() {
		super(Direction.class);
	}
}
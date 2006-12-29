package com.webreach.mirth.server.sqlmap.extensions;

import com.webreach.mirth.model.SystemEvent.Level;

public class EventLevelTypeHandler extends EnumTypeHandler<Level> {
	public EventLevelTypeHandler() {
		super(Level.class);
	}
}
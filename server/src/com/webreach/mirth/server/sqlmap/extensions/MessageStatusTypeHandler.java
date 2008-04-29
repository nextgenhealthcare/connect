package com.webreach.mirth.server.sqlmap.extensions;

import com.webreach.mirth.model.MessageObject.Status;

public class MessageStatusTypeHandler extends EnumTypeHandler<Status> {
	public MessageStatusTypeHandler() {
		super(Status.class);
	}
}
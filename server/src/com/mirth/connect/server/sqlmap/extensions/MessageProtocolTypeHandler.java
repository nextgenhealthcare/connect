/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.sqlmap.extensions;

import com.mirth.connect.model.MessageObject.Protocol;

public class MessageProtocolTypeHandler extends EnumTypeHandler<Protocol> {
	public MessageProtocolTypeHandler() {
		super(Protocol.class);
	}
}
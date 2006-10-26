package com.webreach.mirth.server.util;

import java.util.UUID;

public class UUIDGenerator {
	public static String getUUID() {
		return UUID.randomUUID().toString();
	}
}

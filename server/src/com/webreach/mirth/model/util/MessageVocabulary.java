package com.webreach.mirth.model.util;

import com.webreach.mirth.model.MessageObject.Protocol;

public abstract class MessageVocabulary {
	public MessageVocabulary(String version, String type){}
	public abstract String getDescription(String elementId);
	public abstract Protocol getProtocol();
}

package org.mule.providers;

import org.mule.umo.provider.UMOMessageAdapter;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.mule.providers.file.FilenameParser;

public class VariableFilenameParser implements FilenameParser {
	public static final String PROPERTY_ORIGINAL_FILENAME = "originalFilename";
	private TemplateValueReplacer replacer = new TemplateValueReplacer();
	private MessageObject messageObject;

	public MessageObject getMessageObject() {
		return messageObject;
	}

	public void setMessageObject(MessageObject messageObject) {
		this.messageObject = messageObject;
	}

	public String getFilename(UMOMessageAdapter adaptor, String pattern) {
		String originalFilename = (String) adaptor.getProperty(PROPERTY_ORIGINAL_FILENAME);
		return replacer.replaceValues(pattern, messageObject);
	}

}

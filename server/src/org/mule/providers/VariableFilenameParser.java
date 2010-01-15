package org.mule.providers;

import org.mule.umo.provider.UMOMessageAdapter;

import com.webreach.mirth.connectors.file.FilenameParser;
import com.webreach.mirth.model.MessageObject;

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
		return replacer.replaceValues(pattern, messageObject, originalFilename);
	}

}

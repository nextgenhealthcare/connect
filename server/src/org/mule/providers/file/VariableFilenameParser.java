package org.mule.providers.file;

import org.mule.providers.ProviderUtil;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.util.UUID;
import org.mule.util.Utility;

import com.webreach.mirth.model.MessageObject;

public class VariableFilenameParser implements FilenameParser {

	private MessageObject messageObject;

	public String getFilename(UMOMessageAdapter adaptor, String pattern) {
		String filename = (String) adaptor
				.getProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME);
		try {
			return ProviderUtil.replaceValues(pattern, filename, messageObject);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return pattern;
		}
	}
	public MessageObject getMessageObject() {
		return messageObject;
	}

	public void setMessageObject(MessageObject messageObject) {
		this.messageObject = messageObject;
	}
}

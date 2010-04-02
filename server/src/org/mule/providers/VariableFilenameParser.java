/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package org.mule.providers;

import org.mule.umo.provider.UMOMessageAdapter;

import com.mirth.connect.connectors.file.FilenameParser;
import com.mirth.connect.model.MessageObject;

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

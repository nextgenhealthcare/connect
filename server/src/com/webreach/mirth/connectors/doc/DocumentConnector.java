/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.connectors.doc;

import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.providers.VariableFilenameParser;

import com.webreach.mirth.connectors.file.FilenameParser;

public class DocumentConnector extends AbstractServiceEnabledConnector {
	public static final String PROPERTY_FILENAME = "filename";
	public static final String PROPERTY_TEMPLATE = "template";
	public static final String PROPERTY_OUTPUT_PATTERN = "outputPattern";

	private FilenameParser filenameParser = new VariableFilenameParser();
	private String template;
	private String outputPattern;
	private boolean encrypt;
	private String password;
	private String documentType;
	private String channelId;

	public String getTemplate() {
		return this.template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getOutputPattern() {
		return this.outputPattern;
	}

	public void setOutputPattern(String outputPattern) {
		this.outputPattern = outputPattern;
	}

	public FilenameParser getFilenameParser() {
		return this.filenameParser;
	}

	public void setFilenameParser(FilenameParser filenameParser) {
		this.filenameParser = filenameParser;
	}

	public boolean isEncrypt() {
		return this.encrypt;
	}

	public void setEncrypt(boolean encrypted) {
		this.encrypt = encrypted;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDocumentType() {
		return this.documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getProtocol() {
		return "doc";
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
}

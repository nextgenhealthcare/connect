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
	private boolean encrypted;
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

	public boolean isEncrypted() {
		return this.encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
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

	public String getStatusMode() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getStatusMessage() {
		// TODO Auto-generated method stub
		return null;
	}

}

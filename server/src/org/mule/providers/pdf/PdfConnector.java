package org.mule.providers.pdf;

import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.providers.file.FilenameParser;
import org.mule.providers.file.SimpleFilenameParser;

public class PdfConnector extends AbstractServiceEnabledConnector {
	public static final String PROPERTY_FILENAME = "filename";
	public static final String PROPERTY_TEMPLATE = "template";
	public static final String PROPERTY_OUTPUT_PATTERN = "outputPattern";
	private FilenameParser filenameParser = new SimpleFilenameParser();
	private String template;
	private String outputPattern;

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

	public String getProtocol() {
		return "pdf";
	}

}

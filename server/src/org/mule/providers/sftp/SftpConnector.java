package org.mule.providers.sftp;

import java.util.Map;

import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.providers.file.FilenameParser;
import org.mule.providers.file.SimpleFilenameParser;
import org.mule.umo.UMOComponent;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.provider.UMOMessageReceiver;

public class SftpConnector extends AbstractServiceEnabledConnector {
	public static final String PROPERTY_POLLING_FREQUENCY = "pollingFrequency";
	public static final String PROPERTY_FILENAME = "filename";
	public static final String PROPERTY_TEMPLATE = "template";

	private long pollingFrequency = 0;
	private String outputPattern = null;
	private String template = null;
	private FilenameParser filenameParser = new SimpleFilenameParser();

	public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception {
		long polling = pollingFrequency;
		Map props = endpoint.getProperties();

		if (props != null) {
			// Override properties on the endpoint for the specific endpoint
			String tempPolling = (String) props.get(PROPERTY_POLLING_FREQUENCY);

			if (tempPolling != null) {
				polling = Long.parseLong(tempPolling);
			}
		}

		if (polling <= 0) {
			polling = 1000;
		}

		logger.debug("set polling frequency to: " + polling);
		return serviceDescriptor.createMessageReceiver(this, component, endpoint, new Object[] { new Long(polling) });
	}

	public String getProtocol() {
		return "sftp";
	}

	public FilenameParser getFilenameParser() {
		return this.filenameParser;
	}

	public void setFilenameParser(FilenameParser filenameParser) {
		this.filenameParser = filenameParser;
	}

	public String getOutputPattern() {
		return this.outputPattern;
	}

	public void setOutputPattern(String outputPattern) {
		this.outputPattern = outputPattern;
	}

	public long getPollingFrequency() {
		return this.pollingFrequency;
	}

	public void setPollingFrequency(long pollingFrequency) {
		this.pollingFrequency = pollingFrequency;
	}

	public String getTemplate() {
		return this.template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

}

package com.webreach.mirth.core.util;

import java.util.List;

import com.webreach.mirth.core.Channel;
import com.webreach.mirth.core.Transport;

public class MuleConfigurationBuilder {
	private List<Channel> channels = null;
	private List<Transport> transports = null;
	
	public MuleConfigurationBuilder(List<Channel> channels, List<Transport> transports) {
		this.channels = channels;
		this.transports = transports;
	}
	
	public String getConfiguration() throws ConfigurationBuilderException {
		if ((channels == null) || (transports == null)) {
			throw new ConfigurationBuilderException();	
		}

		// TODO: traverse channels and transports to generate mule configuration using DOM object
		throw new ConfigurationBuilderException();
	}
}

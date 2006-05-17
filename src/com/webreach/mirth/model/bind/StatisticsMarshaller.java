package com.webreach.mirth.model.bind;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.Statistics;

public class StatisticsMarshaller {
	public static final String[] cDataElements = null;
	private Logger logger = Logger.getLogger(PropertiesMarshaller.class);
	
	/**
	 * Returns a Document representation of a Statistics object.
	 * 
	 * @param statistics
	 * @return
	 * @throws MarshalException
	 */
	public Document marshal(Statistics statistics) throws MarshalException {
		logger.debug("marshalling statistics");
		
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element statisticsElement = document.createElement("statistics");
			
			Element sentElement = document.createElement("sent");
			sentElement.setAttribute("statistic", "sentCount");
			sentElement.setTextContent(String.valueOf(statistics.getSentCount()));
			statisticsElement.appendChild(sentElement);

			Element receivedElement = document.createElement("received");
			receivedElement.setTextContent(String.valueOf(statistics.getReceivedCount()));
			statisticsElement.appendChild(receivedElement);

			Element errorElement = document.createElement("error");
			errorElement.setTextContent(String.valueOf(statistics.getErrorCount()));
			statisticsElement.appendChild(errorElement);

			Element queueElement = document.createElement("queue");
			queueElement.setTextContent(String.valueOf(statistics.getQueueSize()));
			statisticsElement.appendChild(queueElement);
			
			document.appendChild(statisticsElement);
			return document;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}
}

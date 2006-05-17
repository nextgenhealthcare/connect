package com.webreach.mirth.model.bind;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.Statistics;

public class StatisticsUnmarshaller {
	private Logger logger = Logger.getLogger(StatisticsUnmarshaller.class);

	/**
	 * Returns a Statistics object given an XML string representation.
	 * 
	 * @param source
	 * @return
	 * @throws UnmarshalException
	 */
	public Statistics unmarshal(String source) throws UnmarshalException {
		logger.debug("unmarshalling statistics from string");

		try {
			InputStream is = new ByteArrayInputStream(source.getBytes());
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			Document document = docBuilderFactory.newDocumentBuilder().parse(is);
			return unmarshal(document);
		} catch (UnmarshalException e) {
			throw e;
		} catch (Exception e) {
			throw new UnmarshalException("Could not parse source.", e);
		}
	}

	/**
	 * Returns a Statistics object given a Document representation.
	 * 
	 * @param document
	 * @return
	 * @throws UnmarshalException
	 */
	public Statistics unmarshal(Document document) throws UnmarshalException {
		logger.debug("unmarshalling statistics from document");
		
		if ((document == null) || (!document.getDocumentElement().getTagName().equals("statistics"))) {
			throw new UnmarshalException("Document is invalid.");
		}

		Statistics statistics = new Statistics();
		Element statisticsElement = document.getDocumentElement();
		
		statistics.setReceivedCount(Integer.valueOf(statisticsElement.getElementsByTagName("received").item(0).getTextContent()));
		statistics.setSentCount(Integer.valueOf(statisticsElement.getElementsByTagName("sent").item(0).getTextContent()));
		statistics.setErrorCount(Integer.valueOf(statisticsElement.getElementsByTagName("error").item(0).getTextContent()));
		statistics.setQueueSize(Integer.valueOf(statisticsElement.getElementsByTagName("queue").item(0).getTextContent()));
		
		return statistics;
	}
}
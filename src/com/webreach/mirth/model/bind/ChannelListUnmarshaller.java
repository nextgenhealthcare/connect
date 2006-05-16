package com.webreach.mirth.model.bind;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.webreach.mirth.model.Channel;

public class ChannelListUnmarshaller {
	private Logger logger = Logger.getLogger(ChannelListUnmarshaller.class);

	/**
	 * Returns a List of Channel objects given a XML string representation.
	 * 
	 * @param source
	 * @return
	 * @throws UnmarshalException
	 */
	public List<Channel> unmarshal(String source) throws UnmarshalException {
		logger.debug("unmarshalling channel list");

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
	 * Returns a List of Channel objects given a Document representation.
	 * 
	 * @param document
	 * @return
	 * @throws UnmarshalException
	 */
	public List<Channel> unmarshal(Document document) throws UnmarshalException {
		if ((document == null) || (!document.getDocumentElement().getTagName().equals("channels"))) {
			throw new UnmarshalException("Document is invalid.");
		}
		
		try {
			List<Channel> channelList = new ArrayList<Channel>();
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			ChannelUnmarshaller channelUnmarshaller = new ChannelUnmarshaller();

			for (int i = 0; i < document.getElementsByTagName("channel").getLength(); i++) {
				Document channelDocument = docBuilderFactory.newDocumentBuilder().newDocument();
				channelDocument.appendChild(channelDocument.importNode(document.getElementsByTagName("channel").item(i), false));
				channelList.add(channelUnmarshaller.unmarshal(channelDocument));
			}
			
			return channelList;
		} catch (Exception e) {
			throw new UnmarshalException(e);
		}
	}
}
package com.webreach.mirth.model.bind;

import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.Channel;

public class ChannelListMarshaller {
	private Logger logger = Logger.getLogger(ChannelListMarshaller.class);
	
	/**
	 * Returns a Document representation of a List of Users.
	 * 
	 * @param userList
	 * @return
	 * @throws MarshalException
	 */
	public Document marshal(List<Channel> userList) throws MarshalException {
		logger.debug("marshaling channel list");
		
		try {
			ChannelMarshaller marshaller = new ChannelMarshaller();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element channelListElement = document.createElement("channels");
			
			for (Iterator iter = userList.iterator(); iter.hasNext();) {
				Channel channel = (Channel) iter.next();
				channelListElement.appendChild(document.importNode(marshaller.marshal(channel).getDocumentElement(), false));
			}
			
			document.appendChild(channelListElement);
			return document;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}
}

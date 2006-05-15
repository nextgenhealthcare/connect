package com.webreach.mirth.server.core.util;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.User;

public class UserMarshaller {
	private Logger logger = Logger.getLogger(UserMarshaller.class);
	
	public UserMarshaller() {}
	
	public Document marshal(User user) throws MarshalException {
		logger.debug("marshaling user: " + user.toString());
		
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element userElement = document.createElement("user");
			userElement.setAttribute("id", String.valueOf(user.getId()));
			userElement.setAttribute("username", user.getUsername());
			userElement.setAttribute("password", user.getPassword());
			document.appendChild(userElement);
			return document;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}
}

package com.webreach.mirth.server.core.util;

import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.User;

public class UserListMarshaller {
	private Logger logger = Logger.getLogger(UserListMarshaller.class);
	
	public UserListMarshaller() {}
	
	public Document marshal(List<User> userList) throws MarshalException {
		logger.debug("marshaling user list");
		
		try {
			UserMarshaller userMarshaller = new UserMarshaller();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element userListElement = document.createElement("users");
			
			for (Iterator iter = userList.iterator(); iter.hasNext();) {
				User user = (User) iter.next();
				userListElement.appendChild(document.importNode(userMarshaller.marshal(user).getDocumentElement(), false));
			}
			
			document.appendChild(userListElement);
			return document;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}
}

package com.webreach.mirth.model.bind;

import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.User;

public class UserListMarshaller {
	private Logger logger = Logger.getLogger(UserListMarshaller.class);
	
	/**
	 * Returns a Document representation of a List of User objects.
	 * 
	 * @param userList
	 * @return
	 * @throws MarshalException
	 */
	public Document marshal(List<User> userList) throws MarshalException {
		logger.debug("marshalling user list");
		
		try {
			UserMarshaller marshaller = new UserMarshaller();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element userListElement = document.createElement("users");
			
			for (Iterator iter = userList.iterator(); iter.hasNext();) {
				User user = (User) iter.next();
				userListElement.appendChild(document.importNode(marshaller.marshal(user).getDocumentElement(), false));
			}
			
			document.appendChild(userListElement);
			return document;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}
}

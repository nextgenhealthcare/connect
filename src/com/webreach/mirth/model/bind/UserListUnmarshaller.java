package com.webreach.mirth.server.core.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.webreach.mirth.model.User;

public class UserListUnmarshaller {
	private Logger logger = Logger.getLogger(UserListUnmarshaller.class);

	public List<User> unmarshal(String source) throws UnmarshalException {
		logger.debug("unmarshalling user list");

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

	public List<User> unmarshal(Document document) throws UnmarshalException {
		if ((document == null) || (!document.getDocumentElement().getTagName().equals("users"))) {
			throw new UnmarshalException("Document is invalid.");
		}
		
		try {
			List<User> userList = new ArrayList<User>();
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			UserUnmarshaller userUnmarshaller = new UserUnmarshaller();

			for (int i = 0; i < document.getElementsByTagName("user").getLength(); i++) {
				Document userDocument = docBuilderFactory.newDocumentBuilder().newDocument();
				userDocument.importNode(document.getElementsByTagName("user").item(i), false);
				userList.add(userUnmarshaller.unmarshal(userDocument));
			}
			
			return userList;
		} catch (Exception e) {
			throw new UnmarshalException(e);
		}
	}
}
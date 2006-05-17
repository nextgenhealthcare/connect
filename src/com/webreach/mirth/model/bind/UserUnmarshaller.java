package com.webreach.mirth.model.bind;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.User;

public class UserUnmarshaller {
	private Logger logger = Logger.getLogger(UserUnmarshaller.class);

	/**
	 * Returns a User object given an XML string representation.
	 * 
	 * @param source
	 * @return
	 * @throws UnmarshalException
	 */
	public User unmarshal(String source) throws UnmarshalException {
		logger.debug("unmarshalling user from string");

		try {
			InputStream is = new ByteArrayInputStream(source.getBytes());
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilderFactory.setNamespaceAware(true);
			Document document = docBuilderFactory.newDocumentBuilder().parse(is);
			return unmarshal(document);
		} catch (UnmarshalException e) {
			throw e;
		} catch (Exception e) {
			throw new UnmarshalException("Could not parse source.", e);
		}
	}

	/**
	 * Returns a User object given a Document representation.
	 * 
	 * @param document
	 * @return
	 * @throws UnmarshalException
	 */
	public User unmarshal(Document document) throws UnmarshalException {
		logger.debug("unmarshalling user from document");
		
		if ((document == null) || (!document.getDocumentElement().getTagName().equals("user"))) {
			throw new UnmarshalException("Document is invalid.");
		}

		User user = new User();
		Element userElement = document.getDocumentElement();
		user.setId(Integer.parseInt(userElement.getAttribute("id")));
		user.setUsername(userElement.getAttribute("username"));
		user.setPassword(userElement.getAttribute("password"));
		return user;
	}
}
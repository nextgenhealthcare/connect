package com.webreach.mirth.core.dao;

public class MirthDAOFactory {
	public static MessageDAO getMessageDAO() {
		return new MessageDAO();
	}
}

package com.webreach.mirth.core.handlers;

import java.util.List;

import com.webreach.mirth.core.dao.MessageDAO;
import com.webreach.mirth.core.dao.MirthDAOFactory;
import com.webreach.mirth.core.util.ValueListHandler;

public class MessageListHandler extends ValueListHandler {
	private MessageDAO dao = null;
	private MessageSearchCriteria criteria = null;

	public MessageListHandler(MessageSearchCriteria criteria) throws ListHandlerException {
		try {
			this.criteria = criteria;
			this.dao = MirthDAOFactory.getMessageDAO();
			executeSearch();
		} catch (Exception e) {
			throw new ListHandlerException(e);
		}
	}

	public void setCriteria(MessageSearchCriteria criteria) {
		this.criteria = criteria;
	}

	public void executeSearch() throws ListHandlerException {
		try {
			if (criteria == null) {
				throw new ListHandlerException("Search criteria required.");
			}
			
			List resultsList = dao.executeSelect(criteria);
			setList(resultsList);
		} catch (Exception e) {
			throw new ListHandlerException(e);
		}
	}
}
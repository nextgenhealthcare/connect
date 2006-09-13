package com.webreach.mirth.client.core;

import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.log4j.Logger;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.filters.MessageObjectFilter;

public class MessageListHandler implements ListHandler {
	private Logger logger = Logger.getLogger(this.getClass());
	private MessageObjectFilter filter;
	private ServerConnection connection;
	private ObjectXMLSerializer serializer = new ObjectXMLSerializer();
	private int currentPage;
	
	public MessageListHandler(MessageObjectFilter filter, ServerConnection connection) {
		this.filter = filter;
		this.connection = connection;
	}
	
	public List<MessageObject> getFirstPage() throws ListHandlerException {
		logger.debug("retrieving first page of " + filter.getPageSize() + " results");
		
		currentPage = 0;
		filter.setPage(currentPage);
		return getPage(filter);
	}
	
	public List<MessageObject> getNextPage() throws ListHandlerException  {
		logger.debug("retrieving next page of " + filter.getPageSize() + " results");
		
		currentPage++;
		filter.setPage(currentPage);
		return getPage(filter);		
	}

	public List<MessageObject> getPreviousPage() throws ListHandlerException  {
		logger.debug("retrieving previous page of " + filter.getPageSize() + " results");
		
		if (currentPage > 0) {
			currentPage--;	
			filter.setPage(currentPage);
			return getPage(filter);
		} else {
			throw new ListHandlerException("Invalid page.");
		}
	}
	
	public int getSize() throws ListHandlerException {
		NameValuePair[] params = { new NameValuePair("op", "getMessageCount"), new NameValuePair("filter", serializer.toXML(filter)) };
		
		try {
			return Integer.parseInt(connection.executePostMethod(Client.LOGGER_SERVLET, params));	
		} catch (ClientException e) {
			throw new ListHandlerException(e);
		}
	}
	
	public int getCurrentPage() {
		return currentPage;
	}
	
	public void resetIndex() {
		currentPage = 0;
	}
	
	private List<MessageObject> getPage(MessageObjectFilter filter) throws ListHandlerException {
		NameValuePair[] params = { new NameValuePair("op", "getMessages"), new NameValuePair("filter", serializer.toXML(filter)) };
		
		try {
			return (List<MessageObject>) serializer.fromXML(connection.executePostMethod(Client.LOGGER_SERVLET, params));	
		} catch (ClientException e) {
			throw new ListHandlerException(e);
		}
	}
}

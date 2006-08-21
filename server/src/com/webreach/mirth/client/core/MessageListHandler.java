package com.webreach.mirth.client.core;

import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.log4j.Logger;

import com.webreach.mirth.model.MessageEvent;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.filters.MessageEventFilter;

public class MessageListHandler implements ListHandler {
	private Logger logger = Logger.getLogger(this.getClass());
	private MessageEventFilter filter;
	private ServerConnection connection;
	private ObjectXMLSerializer serializer = new ObjectXMLSerializer();
	private int currentPage;
	
	public MessageListHandler(MessageEventFilter filter, ServerConnection connection) {
		this.filter = filter;
		this.connection = connection;
	}
	
	public List<MessageEvent> getFirstPage() throws ListHandlerException {
		logger.debug("retrieving first page of " + filter.getPageSize() + " results");
		
		currentPage = 0;
		filter.setPage(currentPage);
		return getPage(filter);
	}
	
	public List<MessageEvent> getNextPage() throws ListHandlerException  {
		logger.debug("retrieving next page of " + filter.getPageSize() + " results");
		
		currentPage++;
		filter.setPage(currentPage);
		return getPage(filter);		
	}

	public List<MessageEvent> getPreviousPage() throws ListHandlerException  {
		logger.debug("retrieving previous page of " + filter.getPageSize() + " results");
		
		if (currentPage > 1) {
			currentPage--;	
			filter.setPage(currentPage);
			return getPage(filter);
		} else {
			throw new ListHandlerException("Invalid page.");
		}
	}
	
	public int getSize() throws ListHandlerException {
		NameValuePair[] params = { new NameValuePair("op", "getMessageEventsCount"), new NameValuePair("filter", serializer.toXML(filter)) };
		
		try {
			return Integer.parseInt(connection.executePostMethod(Client.LOGGER_SERVLET, params));	
		} catch (ClientException e) {
			throw new ListHandlerException(e);
		}
	}
	
	public void resetIndex() {
		currentPage = 0;
	}
	
	private List<MessageEvent> getPage(MessageEventFilter filter) throws ListHandlerException {
		NameValuePair[] params = { new NameValuePair("op", "getMessageEvents"), new NameValuePair("filter", serializer.toXML(filter)) };
		
		try {
			return (List<MessageEvent>) serializer.fromXML(connection.executePostMethod(Client.LOGGER_SERVLET, params));	
		} catch (ClientException e) {
			throw new ListHandlerException(e);
		}
	}
}

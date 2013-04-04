/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.MessageFilter;

public class MessageListHandler implements ListHandler {
	private Logger logger = Logger.getLogger(this.getClass());
	private ServerConnection connection;
	private ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
	private String channelId;
	private MessageFilter filter;
	private int pageSize;
	private int currentPage;
	private int size = 0;
	private String uid;
	
	public MessageListHandler(String channelId, MessageFilter filter, int pageSize, String uid, ServerConnection connection) throws ClientException {
		this.channelId = channelId;
	    this.filter = filter;
		this.pageSize = pageSize;
		this.connection = connection;
		this.uid = uid;
	}
	
	public MessageFilter getFilter() {
		return filter;
	}
	
	public int getPageSize() {
		return pageSize;
	}
	
	public int getCurrentPage() {
		return currentPage;
	}
	
	public int getSize() {
		return size;
	}
	
	public void resetIndex() {
		currentPage = 0;
	}
	
	public List<ConnectorMessage> getAllPages() throws ListHandlerException {
		logger.debug("retrieving all pages");
		return getMessagesByPage(-1);
	}
	
	public List<ConnectorMessage> getFirstPage() throws ListHandlerException {
		logger.debug("retrieving first page of " + pageSize + " results");
		
		currentPage = 0;
		return getMessagesByPage(currentPage);
	}
	
	public List<ConnectorMessage> getNextPage() throws ListHandlerException  {
		logger.debug("retrieving next page of " + pageSize + " results");
		
		currentPage++;
		return getMessagesByPage(currentPage);		
	}

	public List<ConnectorMessage> getPreviousPage() throws ListHandlerException  {
		logger.debug("retrieving previous page of " + pageSize + " results");
		
		if (currentPage > 0) {
			currentPage--;	
			return getMessagesByPage(currentPage);
		} else {
			throw new ListHandlerException("Invalid page.");
		}
	}
	
	public void removeFilterTables() throws ClientException {
		NameValuePair[] params = { new NameValuePair("op", Operations.MESSAGE_FILTER_TABLES_REMOVE.getName()), new NameValuePair("uid", uid) };
		connection.executePostMethod(Client.MESSAGE_SERVLET, params);
	}
	
	private List<ConnectorMessage> getMessagesByPage(int page) throws ListHandlerException {	
//		NameValuePair[] params = { new NameValuePair("op", Operations.MESSAGE_GET_BY_PAGE_LIMIT.getName()), 
//				new NameValuePair("page", String.valueOf(page)), 
//				new NameValuePair("pageSize", String.valueOf(pageSize)), 
//				new NameValuePair("maxMessages", String.valueOf(size)), 
//				new NameValuePair("uid", uid), 
//				new NameValuePair("filter", serializer.toXML(filter))};

       NameValuePair[] params = { new NameValuePair("op", Operations.GET_MESSAGES.getName()), 
                new NameValuePair("channelId", String.valueOf(page)), 
                new NameValuePair("pageSize", String.valueOf(pageSize)), 
                new NameValuePair("maxMessages", String.valueOf(size)), 
                new NameValuePair("uid", uid), 
                new NameValuePair("filter", serializer.toXML(filter))};

		try {
			return (List<ConnectorMessage>) serializer.fromXML(connection.executePostMethod(Client.MESSAGE_SERVLET, params));	
		} catch (ClientException e) {
			throw new ListHandlerException(e);
		}
	}
}

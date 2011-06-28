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

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.MessageObjectFilter;

public class MessageListHandler implements ListHandler {
	private Logger logger = Logger.getLogger(this.getClass());
	private ServerConnection connection;
	private ObjectXMLSerializer serializer = new ObjectXMLSerializer();
	private MessageObjectFilter filter;
	private int pageSize;
	private int currentPage;
	private int size = 0;
	private boolean tempEnabled = true;
	private String uid;
	
	public MessageListHandler(MessageObjectFilter filter, int pageSize, String uid, ServerConnection connection) throws ClientException {
		this.filter = filter;
		this.pageSize = pageSize;
		this.connection = connection;
		this.uid = uid;

		try {
			size = createMessagesTempTable();
			
			if (size == -1) {
				tempEnabled = false;
			}
		} catch (Exception e) {
			logger.error(e);
			throw new ClientException (e);
		}
	}
	
	public MessageObjectFilter getFilter() {
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
	
	public List<MessageObject> getAllPages() throws ListHandlerException {
		logger.debug("retrieving all pages");
		return getMessagesByPage(-1);
	}
	
	public List<MessageObject> getFirstPage() throws ListHandlerException {
		logger.debug("retrieving first page of " + pageSize + " results");
		
		currentPage = 0;
		return getMessagesByPage(currentPage);
	}
	
	public List<MessageObject> getNextPage() throws ListHandlerException  {
		logger.debug("retrieving next page of " + pageSize + " results");
		
		currentPage++;
		return getMessagesByPage(currentPage);		
	}

	public List<MessageObject> getPreviousPage() throws ListHandlerException  {
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
	
	private int createMessagesTempTable() throws ListHandlerException {
		NameValuePair[] params = { new NameValuePair("op", Operations.MESSAGE_CREATE_TEMP_TABLE.getName()), new NameValuePair("filter", serializer.toXML(filter)), new NameValuePair("uid", uid) };
		
		try {
			return Integer.parseInt(connection.executePostMethod(Client.MESSAGE_SERVLET, params));	
		} catch (ClientException e) {
			throw new ListHandlerException(e);
		}
	}
	
	private List<MessageObject> getMessagesByPage(int page) throws ListHandlerException {	
		NameValuePair[] params = { (tempEnabled ? new NameValuePair("op", Operations.MESSAGE_GET_BY_PAGE.getName()) : new NameValuePair("op", Operations.MESSAGE_GET_BY_PAGE_LIMIT.getName())), 
				new NameValuePair("page", String.valueOf(page)), 
				new NameValuePair("pageSize", String.valueOf(pageSize)), 
				new NameValuePair("maxMessages", String.valueOf(size)), 
				new NameValuePair("uid", uid), 
				(tempEnabled ? new NameValuePair("filter", "") : new NameValuePair("filter", serializer.toXML(filter)))};

		try {
			return (List<MessageObject>) serializer.fromXML(connection.executePostMethod(Client.MESSAGE_SERVLET, params));	
		} catch (ClientException e) {
			throw new ListHandlerException(e);
		}
	}
}

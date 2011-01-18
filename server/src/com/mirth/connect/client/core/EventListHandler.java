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

import com.mirth.connect.model.Event;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.EventFilter;

public class EventListHandler implements ListHandler {
	private Logger logger = Logger.getLogger(this.getClass());
	private ServerConnection connection;
	private ObjectXMLSerializer serializer = new ObjectXMLSerializer();
	private EventFilter filter;
	private int pageSize;
	private int currentPage;
	private int size = 0;
	private boolean tempEnabled = true;
	private String uid;
	
	public EventListHandler(EventFilter filter, int pageSize, String uid, ServerConnection connection) throws ClientException {
		this.filter = filter;
		this.pageSize = pageSize;
		this.connection = connection;
		this.uid = uid;

		try {
			size = createEventTempTable();
			
			if (size == -1) {
				tempEnabled = false;
			}
		} catch (Exception e) {
			logger.error(e);
			throw new ClientException (e);
		}
	}
	
	public EventFilter getFilter() {
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
	
	public List<Event> getAllPages() throws ListHandlerException {
		logger.debug("retrieving all pages");
		return getEventsByPage(-1);
	}
	
	public List<Event> getFirstPage() throws ListHandlerException {
		logger.debug("retrieving first page of " + pageSize + " results");
		
		currentPage = 0;
		return getEventsByPage(currentPage);
	}
	
	public List<Event> getNextPage() throws ListHandlerException  {
		logger.debug("retrieving next page of " + pageSize + " results");
		
		currentPage++;
		return getEventsByPage(currentPage);		
	}

	public List<Event> getPreviousPage() throws ListHandlerException  {
		logger.debug("retrieving previous page of " + pageSize + " results");
		
		if (currentPage > 0) {
			currentPage--;	
			return getEventsByPage(currentPage);
		} else {
			throw new ListHandlerException("Invalid page.");
		}
	}
	
	public void removeEventFilterTables() throws ClientException {
		NameValuePair[] params = { new NameValuePair("op", Operations.EVENT_REMOVE_FILTER_TABLES), new NameValuePair("uid", uid) };
		connection.executePostMethod(Client.EVENT_SERVLET, params);
	}
	
	private int createEventTempTable() throws ListHandlerException {
		NameValuePair[] params = { new NameValuePair("op", Operations.EVENT_CREATE_TEMP_TABLE), new NameValuePair("filter", serializer.toXML(filter)), new NameValuePair("uid", uid) };
		
		try {
			return Integer.parseInt(connection.executePostMethod(Client.EVENT_SERVLET, params));	
		} catch (ClientException e) {
			throw new ListHandlerException(e);
		}
	}
	
	private List<Event> getEventsByPage(int page) throws ListHandlerException {	
		NameValuePair[] params = { (tempEnabled ? new NameValuePair("op", Operations.EVENT_GET_BY_PAGE) : new NameValuePair("op", Operations.EVENT_GET_BY_PAGE_LIMIT)), 
				new NameValuePair("page", String.valueOf(page)), 
				new NameValuePair("pageSize", String.valueOf(pageSize)), 
				new NameValuePair("maxEvents", String.valueOf(size)), 
				new NameValuePair("uid", uid), 
				(tempEnabled ? new NameValuePair("filter", "") : new NameValuePair("filter", serializer.toXML(filter)))};

		try {
			return (List<Event>) serializer.fromXML(connection.executePostMethod(Client.EVENT_SERVLET, params));	
		} catch (ClientException e) {
			throw new ListHandlerException(e);
		}
	}
}

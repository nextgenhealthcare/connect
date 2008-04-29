/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.client.core;

import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.log4j.Logger;

import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.filters.SystemEventFilter;

public class SystemEventListHandler implements ListHandler {
	private Logger logger = Logger.getLogger(this.getClass());
	private ServerConnection connection;
	private ObjectXMLSerializer serializer = new ObjectXMLSerializer();
	private SystemEventFilter filter;
	private int pageSize;
	private int currentPage;
	private int size = 0;
	private boolean tempEnabled = true;
	private String uid;
	
	public SystemEventListHandler(SystemEventFilter filter, int pageSize, String uid, ServerConnection connection) throws ClientException {
		this.filter = filter;
		this.pageSize = pageSize;
		this.connection = connection;
		this.uid = uid;

		try {
			size = createSystemEventsTempTable();
			
			if (size == -1) {
				tempEnabled = false;
			}
		} catch (Exception e) {
			logger.error(e);
			throw new ClientException (e);
		}
	}
	
	public SystemEventFilter getFilter() {
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
	
	public List<SystemEvent> getAllPages() throws ListHandlerException {
		logger.debug("retrieving all pages");
		return getSystemEventsByPage(-1);
	}
	
	public List<SystemEvent> getFirstPage() throws ListHandlerException {
		logger.debug("retrieving first page of " + pageSize + " results");
		
		currentPage = 0;
		return getSystemEventsByPage(currentPage);
	}
	
	public List<SystemEvent> getNextPage() throws ListHandlerException  {
		logger.debug("retrieving next page of " + pageSize + " results");
		
		currentPage++;
		return getSystemEventsByPage(currentPage);		
	}

	public List<SystemEvent> getPreviousPage() throws ListHandlerException  {
		logger.debug("retrieving previous page of " + pageSize + " results");
		
		if (currentPage > 0) {
			currentPage--;	
			return getSystemEventsByPage(currentPage);
		} else {
			throw new ListHandlerException("Invalid page.");
		}
	}
	
	public void removeFilterTables() throws ClientException {
		NameValuePair[] params = { new NameValuePair("op", "removeFilterTables"), new NameValuePair("uid", uid) };
		connection.executePostMethod(Client.EVENT_SERVLET, params);
	}
	
	private int createSystemEventsTempTable() throws ListHandlerException {
		NameValuePair[] params = { new NameValuePair("op", "createSystemEventsTempTable"), new NameValuePair("filter", serializer.toXML(filter)), new NameValuePair("uid", uid) };
		
		try {
			return Integer.parseInt(connection.executePostMethod(Client.EVENT_SERVLET, params));	
		} catch (ClientException e) {
			throw new ListHandlerException(e);
		}
	}
	
	private List<SystemEvent> getSystemEventsByPage(int page) throws ListHandlerException {	
		NameValuePair[] params = { (tempEnabled ? new NameValuePair("op", "getSystemEventsByPage") : new NameValuePair("op", "getSystemEventsByPageLimit")), 
				new NameValuePair("page", String.valueOf(page)), 
				new NameValuePair("pageSize", String.valueOf(pageSize)), 
				new NameValuePair("maxSystemEvents", String.valueOf(size)), 
				new NameValuePair("uid", uid), 
				(tempEnabled ? new NameValuePair("filter", "") : new NameValuePair("filter", serializer.toXML(filter)))};

		try {
			return (List<SystemEvent>) serializer.fromXML(connection.executePostMethod(Client.EVENT_SERVLET, params));	
		} catch (ClientException e) {
			throw new ListHandlerException(e);
		}
	}
}

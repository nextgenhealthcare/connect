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


package com.webreach.mirth.client.core.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.webreach.mirth.model.Message;
import com.webreach.mirth.server.core.util.DatabaseConnection;
import com.webreach.mirth.server.core.util.DatabaseUtil;

public class MessageListHandler extends ValueListHandler {
	private String query;

	public MessageListHandler(String query) {
		this.query = query;
		executeSearch();
	}

	public void executeSearch() {
		DatabaseConnection dbConnection = new DatabaseConnection();
		ResultSet result = null;
		
		try {
			result = dbConnection.query(query);
			setList(prepareResult(result));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}
	
	private List prepareResult(ResultSet result) throws SQLException {
		ArrayList<Message> list = new ArrayList<Message>();

		while (result.next()) {
			Message message = new Message();
			message.setId(result.getInt("ID"));
			message.setEvent(result.getString("EVENT"));
			message.setControlId(result.getString("CONTROL_ID"));
			message.setSendingFacility(result.getString("SENDING_FACILITY"));
			message.setDate(result.getTimestamp("DATE_CREATED"));
			message.setMessage(result.getString("MESSAGE"));
			message.setSize(result.getString("MESSAGE").getBytes().length);
			list.add(message);
		}

		return list;
	}	
}

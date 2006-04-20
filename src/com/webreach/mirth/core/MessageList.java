package com.webreach.mirth.core;

import com.webreach.mirth.core.dao.MessageDAO;
import com.webreach.mirth.core.handlers.MessageListHandler;
import com.webreach.mirth.core.handlers.MessageSearchCriteria;

public class MessageList {
	private Channel channel;
	
	public MessageList(Channel channel) {
		this.channel = channel;
	}
	
	// add a message to the message list
	public void add(Message message) {
		MessageDAO dao = new MessageDAO();
		dao.insert(message);
	}
	
	// return all messages
	public MessageListHandler getMessages() {
		MessageSearchCriteria criteria = new MessageSearchCriteria();
		criteria.setChannelName(channel.getName());
		return new MessageListHandler(criteria);	
	}

	// return messages by id range
	public MessageListHandler getMessagesByIdRange(int min, int max) {
		MessageSearchCriteria criteria = new MessageSearchCriteria();
		criteria.setChannelName(channel.getName());
		criteria.setMinId(min);
		criteria.setMaxId(max);
		return new MessageListHandler(criteria);	
	}

	// return messages by date range
	public MessageListHandler getMessagesByDateRange(String min, String max) {
		MessageSearchCriteria criteria = new MessageSearchCriteria();
		criteria.setChannelName(channel.getName());
		criteria.setMinDate(min);
		criteria.setMaxDate(max);
		return new MessageListHandler(criteria);	
	}

}

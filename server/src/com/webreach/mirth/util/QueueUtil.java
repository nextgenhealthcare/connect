package com.webreach.mirth.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.MuleManager;
import org.mule.util.queue.Queue;
import org.mule.util.queue.QueueManager;
import org.mule.util.queue.QueueSession;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.controllers.ChannelController;

public class QueueUtil {
	private Log logger = LogFactory.getLog(getClass());
	private static QueueUtil instance = null;

	private QueueUtil() {

	}

	public static QueueUtil getInstance() {
		synchronized (QueueUtil.class) {
			if (instance == null) {
				instance = new QueueUtil();
			}

			return instance;
		}
	}

	public void removeAllQueuesForChannel(Channel channel) throws Exception {
		removeQueue(channel.getId());
		
		// iterate through all destinations, create queue name, remove queue
        for (int i = 1; i <= channel.getDestinationConnectors().size(); i++) {
            removeQueue(getQueueName(channel.getId(), String.valueOf(i)));
		}
	}
	
	private void removeQueue(String queueName) throws Exception {
		QueueManager qm = MuleManager.getInstance().getQueueManager();
		QueueSession session = qm.getQueueSession();
		session.deleteQueue(queueName);
	}

	public void removeAllQueues() throws Exception {
		QueueManager qm = MuleManager.getInstance().getQueueManager();
		QueueSession session = qm.getQueueSession();
		List<String> queueNames = qm.getAllQueueNames();

		for (String queueName : queueNames) {
			session.deleteQueue(queueName);
		}
	}

	public void removeMessageFromQueue(String queueName, String messageId) throws Exception {
		QueueManager qm = MuleManager.getInstance().getQueueManager();
		QueueSession session = qm.getQueueSession();
		Queue queue = session.getQueue(queueName);
		queue.remove(messageId);
	}
	
	public String getQueueName(String channelId, String connectorId) {
		return channelId + "_destination_" + connectorId + "_connector";
	}
}
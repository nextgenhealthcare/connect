package com.webreach.mirth.util;

import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.MuleManager;
import org.mule.util.queue.Queue;
import org.mule.util.queue.QueueManager;
import org.mule.util.queue.QueueSession;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.QueuedSenderProperties;
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

	public void removeAllQueuesForChannel(Channel channel) {
		// iterate through all destinations, create queue name, remove queue
		for (ListIterator iterator = channel.getDestinationConnectors().listIterator(); iterator.hasNext();) {
			Connector connector = (Connector) iterator.next();

			if ((connector.getProperties().getProperty(QueuedSenderProperties.USE_PERSISTENT_QUEUES) != null) && connector.getProperties().getProperty(QueuedSenderProperties.USE_PERSISTENT_QUEUES).equals("1")) {
				removeQueue(getQueueName(channel.getId(), String.valueOf(iterator.nextIndex())));
			}
		}
	}

	private void removeQueue(String queueName) {
		try {
			QueueManager qm = MuleManager.getInstance().getQueueManager();
			QueueSession session = qm.getQueueSession();
			session.deleteQueue(queueName);
		} catch (Exception e) {
			logger.debug("Could not remove queue: " + queueName);
		}
	}

	public void removeAllQueues() {
		try {
			List<Channel> channels = ChannelController.getInstance().getChannel(null);

			for (Channel channel : channels) {
				removeQueue(channel.getId());
				removeAllQueuesForChannel(channel);
			}
		} catch (Exception e) {
			logger.debug("Could not remove all queues", e);
		}
	}

	public void removeMessageFromQueue(String queueName, String messageId) {
		QueueManager qm = MuleManager.getInstance().getQueueManager();
		QueueSession session = qm.getQueueSession();
		Queue queue = session.getQueue(queueName);

		try {
			queue.remove(messageId);
		} catch (Exception e) {
			logger.debug("Could not remove message: " + messageId + " from queue: " + queueName);
		}
	}

	public String getQueueName(String channelId, String connectorId) {
		return channelId + "_destination_" + connectorId + "_connector";
	}
}
/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package org.mule.providers;

import java.net.SocketException;
import java.util.List;

import javax.resource.spi.work.Work;

import org.mule.MuleManager;
import org.mule.config.QueueProfile;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.util.queue.Queue;
import org.mule.util.queue.QueueManager;
import org.mule.util.queue.QueueSession;

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.QueuedMessage;
import com.mirth.connect.model.filters.MessageObjectFilter;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageObjectController;

public class QueueEnabledConnector extends AbstractServiceEnabledConnector {
	private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
	private AlertController alertController = ControllerFactory.getFactory().createAlertController();

	protected Queue queue = null;
	protected QueueSession queueSession = null;

	private QueueEnabledMessageDispatcher dispatcher;

	private String connectorErrorCode;
	private boolean rotateQueue = false;
	private boolean usePersistentQueues = false;
	private long pollMaxTime = 10000;
	private int maxQueues = 16;
	private int reconnectMillisecs = 10000;
	private int queuePollInterval = 200;
	private QueueProfile queueProfile;

	private Thread queueThread = null;
	private QueueWorker work = null;

	private boolean killQueueThread = false;

	public void startQueueThread() {
		try {
			killQueueThread = false;
			work = (QueueWorker) createWork(queue);
			queueThread = new Thread(work);
			queueThread.setName(getName() + "_queue_thread");
			queueThread.start();
		} catch (Exception e) {
			logger.error("Error starting queuing thread", e);
		}
	}

	public void stopQueueThread() {
		if (queueThread != null) {
			try {
				killQueueThread = true;
				
				if(dispatcher != null) {
					dispatcher.doDispose();
				}
				
				queueThread.interrupt();
				queueThread.join();
			} catch (Exception e) {
				logger.error("Could not stop queue thread", e);
			}
		}
	}

	@Override
	public void startDispatchers(UMOComponent component, UMOEndpoint endpoint) throws UMOException {
		super.startDispatchers(component, endpoint);
		
		// MIRTH-1427: Let the AbstractModel's registerListeners() start the 
		// queue thread. This prevents the queue thread from being started 
		// during a deploy with the initial channel state of stopped.
		// startQueueThread();
	}

	@Override
	public void stopDispatchers(UMOComponent component, UMOEndpoint endpoint) throws UMOException {
		super.stopDispatchers(component, endpoint);
		stopQueueThread();	
	}

	public void setQueues() {
		try {
			this.queue = getQueue();
		} catch (Exception e) {
			logger.error("Error setting queues to the endpoint\n", e);
		}
	}

	public synchronized void putMessageInQueue(UMOEndpointURI endpointUri, MessageObject messageObject) {
		try{
			messageObjectController.setQueued(messageObject, "Message is queued", null);
			
			QueuedMessage queuedMessage = new QueuedMessage();
			queuedMessage.setEndpointUri(endpointUri);
			queuedMessage.setMessageObject(messageObject);
	
			queue.put(queuedMessage);
		} catch (Exception e) {
			String exceptionMessage = "Can't save payload to queue";
			logger.error(exceptionMessage, e);
			messageObjectController.setError(messageObject, getConnectorErrorCode(), exceptionMessage, e, null);
			alertController.sendAlerts(messageObject.getChannelId(), getConnectorErrorCode(), exceptionMessage, e);
			return;
		}
	}

	protected Work createWork(Queue queue) throws SocketException {
		return new QueueWorker();
	}

	protected class QueueWorker implements Work {

		public QueueWorker() {

		}

		public void release() {

		}

		public void rotateCurrentMessage() throws InterruptedException {
			QueuedMessage tempMessage = (QueuedMessage) queue.poll(getPollMaxTime());
			if (tempMessage != null) {
				try {
					queue.put(tempMessage);
				} catch (InterruptedException e) {
					throw e;
				} catch (Exception e) {
					logger.error("Could not rotate message in queue", e);
					alertController.sendAlerts(tempMessage.getMessageObject().getChannelId(), getConnectorErrorCode(), null, e);
					messageObjectController.setError(tempMessage.getMessageObject(), getConnectorErrorCode(), "Could not rotate message in queue", e, null);
				}
			}
		}

		public void run() {

			try {
				logger.debug("queuing thread started on connector: " + getName());
				while (!killQueueThread) {
					boolean connected = true;
					boolean interrupted = false;

					if (queue == null) {
						setQueues();
					}
					
					if(queue != null) { 
						logger.debug("queue size = " + queue.size());
						
						if(queue.size() == 0) {
							Thread.sleep(queuePollInterval);
						} else {
							// If the endpoint is active, try to send without
							// waiting
							while ((queue.size() > 0) && connected && !killQueueThread) {
								Object thePayload = null;
								QueuedMessage theMessage = null;
								
								try {
									thePayload = queue.peek();
									
									// Remove a null queued message (blank file)
									if (thePayload == null) {
										queue.poll(getPollMaxTime());
										continue;
									}
									
									if(thePayload instanceof MessageObject) { 
										MessageObject messageObject = (MessageObject)thePayload;
										Exception auxEx = new Exception("Unsupported message format in queue.  Removing message from the queue.  Reprocessing this message will fix this problem.");
										String auxErrStr = "Unsupported message format in queue.";
										alertController.sendAlerts(theMessage.getMessageObject().getChannelId(), getConnectorErrorCode(), auxErrStr, auxEx);
										messageObjectController.setError(messageObject, getConnectorErrorCode(), auxErrStr, auxEx, null);
										queue.poll(getPollMaxTime());
										continue;
									} else {
										theMessage = (QueuedMessage) thePayload;
										
										// Remove a queued message with a null message object
										if(theMessage.getMessageObject() == null) { 
											queue.poll(getPollMaxTime());
											continue;
										}
									}
									
									logger.debug("retrying queued message: id = " + theMessage.getMessageObject().getId() + ", endpointUri = " + theMessage.getEndpointUri().toString());
									if (dispatcher.sendPayload(theMessage)) {
										// message sent, so poll is and start processing quickly to this destination
										
										queue.poll(getPollMaxTime());
										connected = true;
									} else {
										// didn't throw an exception, but still failed to send.  reset the queued status
										
										if (isRotateQueue()) {
											rotateCurrentMessage();
										}
										connected = false;
										messageObjectController.resetQueuedStatus(theMessage.getMessageObject());
									}
								} catch (Throwable t) {
									if (t instanceof InterruptedException) {
										interrupted = true;
									}
	
									if (theMessage != null && theMessage.getMessageObject().getStatus().equals(MessageObject.Status.ERROR)) {
										// normal failure to connect to destination, reset the queued status
										
										if (isRotateQueue()) {
											rotateCurrentMessage();
										}
										logger.debug("Connection error [" + t + "] " + " at " + theMessage.getEndpointUri().toString() + " queue size " + new Integer(queue.size()).toString());
										messageObjectController.resetQueuedStatus(theMessage.getMessageObject());
									} else {
										if (!interrupted) {		
											// we have a corrupted queued message file and need to remove it and set the message to errored
											
											try {
												Object id = queue.removeTop();
												logger.error("Encountered invalid queued message.  Message removed from queue: queueId=" + getQueueName());
												if(id != null) { 
													MessageObjectFilter filter = new MessageObjectFilter();
													filter.setId((String)id);
													
													String tempTableId = System.currentTimeMillis() + "";
													messageObjectController.createMessagesTempTable(filter, tempTableId, true);
													List<MessageObject> messages = messageObjectController.getMessagesByPageLimit(0, 1, 1, tempTableId, filter);
													if(messages.size() > 0) { 
														MessageObject message = messages.get(0);
														alertController.sendAlerts(theMessage.getMessageObject().getChannelId(), Constants.ERROR_400, "Encountered invalid queued message.  Message removed from queue: queueId=" + getQueueName(), t);
														messageObjectController.setError(message, Constants.ERROR_400, "Encountered invalid queued message.  Message removed from queue: queueId=" + getQueueName(), t, null);
													}
												}
											} catch(Exception e) {
												// do nothing here, this is an invalid state
											}
										}
									}
									connected = false;
								}
								if (!connected && !interrupted) {
									Thread.sleep(getReconnectMillisecs());
								}
							}
						}
					}
				}
			} catch (InterruptedException e) {
			}
			logger.debug("queuing thread ended on connector: " + getName());
		}
	}

	@Override
	public String getProtocol() {
		// override in implementing class
		return null;
	}

	public void doInitialise() throws InitialisationException {
		super.doInitialise();

		if (isUsePersistentQueues() && queueProfile == null) {
			queueProfile = MuleManager.getConfiguration().getQueueProfile();
			configureQueues();
			setQueues();
		}
	}

	public String getQueueName() {
		String queueName = this.getName();
		queueName = queueName.replace("\\", "");
		queueName = queueName.replace("/", "");
		queueName = queueName.replace(":", "_");
		queueName = queueName.replace(" ", "_");
		return queueName;
	}

	public void configureQueues() {

		try {
			queueProfile.configureQueue(getQueueName());
		} catch (Throwable t) {
			logger.warn("could not configure queue for endpoint " + t);
		}
	}

	public QueueProfile getQueueProfile() {
		return queueProfile;
	}

	public void setQueueProfile(QueueProfile queueProfile) {
		this.queueProfile = queueProfile;
	}

	public void setMaxQueues(int maxQueues) {
		this.maxQueues = maxQueues;
	}

	public int getMaxQueues(int maxQueues) {
		return this.maxQueues;
	}

	public Queue getQueue() throws Exception {

		initQueueSession();
		if (getQueueSession() == null) {
			throw new Exception("Could not initialize queuesession for " + getQueueName());
		}
		Queue q = getQueueSession().getQueue(getQueueName());
		return q;
	}

	public void initQueueSession() {
		QueueManager qm = MuleManager.getInstance().getQueueManager();
		logger.debug("Retrieving new queue session from queue manager " + this.getName());
		setQueueSession(qm.getQueueSession());
	}

	public QueueSession getQueueSession() {
		return queueSession;
	}

	public void setQueueSession(QueueSession queueSession) {
		this.queueSession = queueSession;
	}

	public boolean isRotateQueue() {
		return rotateQueue;
	}

	public void setRotateQueue(boolean rotateQueue) {
		this.rotateQueue = rotateQueue;
	}

	public boolean isUsePersistentQueues() {
		return usePersistentQueues;
	}

	public void setUsePersistentQueues(boolean usePersistentQueues) {
		this.usePersistentQueues = usePersistentQueues;
	}

	public long getPollMaxTime() {
		return pollMaxTime;
	}

	public void setPollMaxTime(long pollMaxTime) {
		this.pollMaxTime = pollMaxTime;
	}

	public int getReconnectMillisecs() {
		return reconnectMillisecs;
	}

	public void setReconnectMillisecs(int reconnectMillisecs) {
		this.reconnectMillisecs = reconnectMillisecs;
	}

	public int getQueuePollInterval() {
        return queuePollInterval;
    }

    public void setQueuePollInterval(int queuePollInterval) {
        this.queuePollInterval = queuePollInterval;
    }

    public QueueEnabledMessageDispatcher getDispatcher() {
		return dispatcher;
	}

	public void setDispatcher(QueueEnabledMessageDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	public int getMaxQueues() {
		return maxQueues;
	}

	public String getConnectorErrorCode() {
		return connectorErrorCode;
	}

	public void setConnectorErrorCode(String connectorErrorCode) {
		this.connectorErrorCode = connectorErrorCode;
	}
}

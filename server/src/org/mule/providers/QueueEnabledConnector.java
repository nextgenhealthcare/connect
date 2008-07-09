package org.mule.providers;

import java.net.SocketException;

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

import com.webreach.mirth.connectors.tcp.TcpConnector;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.QueuedMessage;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.MessageObjectController;

public class QueueEnabledConnector extends AbstractServiceEnabledConnector {
	private MessageObjectController messageObjectController = MessageObjectController.getInstance();
	private AlertController alertController = AlertController.getInstance();

	protected Queue queue = null;
	protected QueueSession queueSession = null;

	private QueueEnabledMessageDispatcher dispatcher;

	public static final String PROPERTY_ROTATE_QUEUE = "rotateQueue";
	public static final long DEFAULT_POLLING_FREQUENCY = 10;

	private String connectorErrorCode;
	private boolean rotateQueue = false;
	private boolean usePersistentQueues = false;
	private long pollMaxTime = 10000;
	private int maxQueues = 16;
	private int reconnectMillisecs = 10000;
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
			logger.error("Error starting queuing thread:\n " + e);
		}
	}

	public void stopQueueThread() {
		if (queueThread != null) {
			try {
				killQueueThread = true;
				queueThread.interrupt();
				queueThread.join();
			} catch (Exception e) {
				logger.error("Cound not stop queue thread: " + e);
			}
		}
	}

	@Override
	public void startDispatchers(UMOComponent component, UMOEndpoint endpoint) throws UMOException {
		// TODO Auto-generated method stub
		super.startDispatchers(component, endpoint);
		startQueueThread();
	}

	@Override
	public void stopDispatchers(UMOComponent component, UMOEndpoint endpoint) throws UMOException {
		// TODO Auto-generated method stub
		super.stopDispatchers(component, endpoint);
		stopQueueThread();
	}

	public void setQueues() {
		try {
			this.queue = getQueue();
		} catch (Exception e) {
			logger.error("Error setting queues to the endpoint\n" + e);
		}
	}

	public synchronized void putMessageInQueue(UMOEndpointURI endpointUri, MessageObject messageObject) {
		try{
			messageObjectController.setQueued(messageObject, "Message is queued");
			
			QueuedMessage queuedMessage = new QueuedMessage();
			queuedMessage.setEndpointUri(endpointUri);
			queuedMessage.setMessageObject(messageObject);
	
			queue.put(queuedMessage);
		} catch (Exception e) {
			String exceptionMessage = "Can't save payload to queue";
			logger.error("Can't save payload to queue", e);
			messageObjectController.setError(messageObject, getConnectorErrorCode(), exceptionMessage, e);
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
					logger.error("Could not rotate message in queue: " + e);
					alertController.sendAlerts(tempMessage.getMessageObject().getChannelId(), getConnectorErrorCode(), null, e);
					messageObjectController.setError(tempMessage.getMessageObject(), getConnectorErrorCode(), "Could not rotate message in queue", e);
				}
			}
		}

		public void run() {

			try {
				logger.debug("queuing thread started on connector: " + getName());
				while (!killQueueThread) {
					boolean connected = true;
					boolean interrupted = false;

					logger.debug("queue.size = " + queue.size());
					if (queue == null || queue.size() == 0) {
						Thread.sleep(getReconnectMillisecs());
					} else {
						// If the endpoint is active, try to send without
						// waiting
						while ((queue.size() > 0) && connected) {
							QueuedMessage thePayload = null;

							try {
								thePayload = (QueuedMessage) queue.peek();
								logger.debug("retrying queued message: id = " + thePayload.getMessageObject().getId() + ", endpointUri = " + thePayload.getEndpointUri().toString());
								if (dispatcher.sendPayload(thePayload)) {
									queue.poll(getPollMaxTime());
									connected = true;
								} else {
									if (isRotateQueue()) {
										rotateCurrentMessage();
									}
									MessageObjectController.getInstance().resetQueuedStatus(thePayload.getMessageObject());
								}
							} catch (Throwable t) {
								if (t instanceof InterruptedException) {
									interrupted = true;
								}

								if (thePayload != null && thePayload.getMessageObject().getStatus().equals(MessageObject.Status.ERROR)) {
									if (isRotateQueue()) {
										rotateCurrentMessage();
									}
									logger.debug("Conection error [" + t + "] " + " at " + thePayload.getEndpointUri().toString() + " queue size " + new Integer(queue.size()).toString());
									MessageObjectController.getInstance().resetQueuedStatus(thePayload.getMessageObject());
								} else {
									if (!interrupted) {
										logger.warn("Error reading message off the queue. Queue out of sync with filesystem: ", t);
										queueSession.resyncQueue(getName());
										
										//queue.poll(getPollMaxTime());
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

	@Override
	protected void doStart() throws UMOException {
		// TODO Auto-generated method stub
		super.doStart();
		if (isUsePersistentQueues()) {
			startQueueThread();
		}
	}

	@Override
	protected void doStop() throws UMOException {
		// TODO Auto-generated method stub
		super.doStop();
		if (isUsePersistentQueues()) {
			stopQueueThread();
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

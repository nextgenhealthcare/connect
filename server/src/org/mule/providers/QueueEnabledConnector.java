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

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.QueuedMessage;
import com.webreach.mirth.server.controllers.MessageObjectController;

public class QueueEnabledConnector extends AbstractServiceEnabledConnector {
	private MessageObjectController messageObjectController = MessageObjectController.getInstance();
	
	protected Queue queue = null;
	protected Queue errorQueue = null;
	
	private QueueEnabledMessageDispatcher dispatcher;

	public static final String PROPERTY_ROTATE_QUEUE = "rotateQueue";
	public static final long DEFAULT_POLLING_FREQUENCY = 10;

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
			this.errorQueue = getErrorQueue();
		} catch (Exception e) {
			logger.error("Error setting queues to the endpoint\n" + e);
		}
	}

	public void putMessageInQueue(UMOEndpointURI endpointUri, MessageObject messageObject) throws Exception {
		messageObjectController.setQueued(messageObject, "Message is queued");
		
		QueuedMessage queuedMessage = new QueuedMessage();
		queuedMessage.setEndpointUri(endpointUri);
		queuedMessage.setMessageObject(messageObject);
		
		queue.put(queuedMessage);
	}

	protected Work createWork(Queue queue) throws SocketException {
		return new QueueWorker();
	}

	protected class QueueWorker implements Work {

		public QueueWorker() {

		}

		public void release() {

		}

		public void run() {
				
			try {
				logger.debug("queuing thread started on connector: " + getName());		
				while (!killQueueThread) {
					boolean connected = true;
					
					try {
						queue = getQueueSession().resyncQueue(getQueueName());
					} catch (Exception ex) { 
						logger.error(ex);
					}
					
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
										QueuedMessage tempMessage = (QueuedMessage) queue.poll(getPollMaxTime());
										if(tempMessage != null) { 
											queue.put(tempMessage);
										}
									}
									MessageObjectController.getInstance().resetQueuedStatus(thePayload.getMessageObject());
								}
							} catch (Throwable t) { 
								if (thePayload != null) {
									if (isRotateQueue()) {
										QueuedMessage tempMessage = (QueuedMessage) queue.poll(getPollMaxTime());
										if(tempMessage != null) { 
											queue.put(tempMessage);
										}
									}
									logger.debug("Conection error [" + t + "] " + " at " + thePayload.getEndpointUri().toString() + " queue size " + new Integer(queue.size()).toString());
									MessageObjectController.getInstance().resetQueuedStatus(thePayload.getMessageObject());
								} else { 
									queue.poll(getPollMaxTime());
									logger.error("error reading message off the queue: " + t);
								}
								connected = false;
							}
							if (!connected) { 
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

	public String getErrorQueueName() {
		return "_Error" + getQueueName();
	}

	public void configureQueues() {

		try {
			queueProfile.configureQueue(getQueueName());
			queueProfile.configureQueue(getErrorQueueName());
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

	public Queue getQueue() throws InitialisationException {

		QueueSession qs = getQueueSession();
		Queue q = qs.getQueue(getQueueName());
		return q;
	}

	public Queue getErrorQueue() throws InitialisationException {

		QueueSession qs = getQueueSession();
		Queue q = qs.getQueue(getErrorQueueName());
		return q;
	}

	public QueueSession getQueueSession() throws InitialisationException {

		QueueManager qm = MuleManager.getInstance().getQueueManager();

		logger.debug("Retrieving new queue session from queue manager " + this.getName());

		QueueSession session = qm.getQueueSession();

		return session;
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
}

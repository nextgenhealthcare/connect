package org.mule.providers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.swing.text.DateFormatter;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;

import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;

public abstract class PollingMessageReceiver extends AbstractMessageReceiver implements Work {
	public static final long STARTUP_DELAY = 1000;
	public static final long DEFAULT_POLL_FREQUENCY = 1000;
	public static final String DEFAULT_TIME = "12:00 AM";

	private long frequency = DEFAULT_POLL_FREQUENCY;

	private String time = DEFAULT_TIME;
	private boolean useTime = false;
	private boolean workDone = false;
	private ConnectorType connectorType = ConnectorType.READER;

	public PollingMessageReceiver(UMOConnector connector, UMOComponent component, final UMOEndpoint endpoint, Long frequency) throws InitialisationException {
		super(connector, component, endpoint);
		this.frequency = frequency.longValue();
	}

	public void doStart() throws UMOException {
		try {
			getWorkManager().scheduleWork(this, WorkManager.INDEFINITE, null, null);
		} catch (WorkException e) {
			stopped.set(true);
			throw new InitialisationException(new Message(Messages.FAILED_TO_SCHEDULE_WORK), e, this);
		}
	}

	public void run() {
		try {
			Thread.sleep(STARTUP_DELAY);

			if (useTime) {
				try {
					SimpleDateFormat timeDateFormat = new SimpleDateFormat("hh:mm aa");
					DateFormatter timeFormatter = new DateFormatter(timeDateFormat);
					Date timeDate = (Date) timeFormatter.stringToValue(time);
					Calendar timeCalendar = Calendar.getInstance();
					timeCalendar.setTime(timeDate);

					while (!stopped.get()) {
						connected.whenTrue(null);
						try {
							if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == timeCalendar.get(Calendar.HOUR_OF_DAY) && Calendar.getInstance().get(Calendar.MINUTE) == timeCalendar.get(Calendar.MINUTE)) {
								if (!workDone) {
									workDone = true;
									poll();
								}
							} else {
								workDone = false;
							}
						} catch (InterruptedException e) {
							return;
						} catch (Exception e) {
							handleException(e);
						}
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					return;
				} catch (ParseException e) {
					handleException(e);
				}
			} else {
				while (!stopped.get()) {
					connected.whenTrue(null);
					try {
						poll();
					} catch (InterruptedException e) {
						return;
					} catch (Exception e) {
						handleException(e);
					}
					Thread.sleep(frequency);
				}
			}
		} catch (InterruptedException e) {
		} finally {
		    ControllerFactory.getFactory().createMonitoringController().updateStatus(connector, connectorType, Event.DISCONNECTED);
		}
	}

	public void release() {
		this.stop();
	}

	public void setFrequency(long l) {
		if (l <= 0) {
			frequency = DEFAULT_POLL_FREQUENCY;
		} else {
			frequency = l;
		}
		useTime = false;
	}

	public long getFrequency() {
		return frequency;
	}

	public void setTime(String time) {
		this.time = time;
		useTime = true;
	}

	public String getTime() {
		return this.time;
	}

	protected void doDispose() {

	}

	public abstract void poll() throws Exception;
}

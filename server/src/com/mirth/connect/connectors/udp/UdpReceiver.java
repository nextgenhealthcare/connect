/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.PollConnector;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class UdpReceiver extends PollConnector {
	private Logger logger = Logger.getLogger(getClass());
	private EventController eventController = ControllerFactory.getFactory().createEventController();
	private ContextFactoryController contextFactoryController = ControllerFactory.getFactory()
			.createContextFactoryController();
	private UdpReceiverProperties connectorProperties;

	private DatagramSocket socket;
	private boolean running;
	private byte[] buf = new byte[256];

	@Override
	public void onDeploy() throws ConnectorTaskException {
		this.connectorProperties = (UdpReceiverProperties) getConnectorProperties();
		eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(),
				ConnectionStatusEventType.IDLE));
	}

	@Override
	public void onUndeploy() throws ConnectorTaskException {
	}

	Thread t = null;

	@Override
	public void onStart() throws ConnectorTaskException {
		try {
			socket = new DatagramSocket(connectorProperties.getPort());
			t = new Thread(runThread);
			t.start();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public Runnable runThread = new Runnable() {

		@Override
		public void run() {
			try {
				running = true;

				while (running) {
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					socket.receive(packet);

					InetAddress address = packet.getAddress();
					int port = packet.getPort();
					packet = new DatagramPacket(buf, buf.length, address, port);
					String received = new String(packet.getData(), 0, packet.getLength());

					if (received.equals("end")) {
						running = false;
						continue;
					}

					eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(),
							getSourceName(), ConnectionStatusEventType.READING));
					DispatchResult dispatchResult = null;

					try {
						dispatchResult = dispatchRawMessage(new RawMessage(received));
					} catch (ChannelException e) {
						// Do nothing. An error should have been logged.
					} finally {
						finishDispatch(dispatchResult);
					}
					socket.send(new DatagramPacket("ACK".getBytes(), "ACK".getBytes().length,address, port));
//					socket.send(packet);
				}
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public void onStop() throws ConnectorTaskException {
		try {
			if (t != null)
				t.stop();
			this.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onHalt() throws ConnectorTaskException {
		try {
			this.halt();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void handleRecoveredResponse(DispatchResult dispatchResult) {
		finishDispatch(dispatchResult);
	}

	@Override
	public void poll() throws InterruptedException {
//        Object result = null;
//        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.READING));
//        DispatchResult dispatchResult = null;
//
//        try {
//            dispatchResult = dispatchRawMessage(rawMessage);
//        } catch (ChannelException e) {
//            // Do nothing. An error should have been logged.
//        } finally {
//            finishDispatch(dispatchResult);
//        }

	}

}

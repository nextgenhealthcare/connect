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


package com.webreach.mirth.applets.logviewer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import org.apache.log4j.spi.LoggingEvent;

public class tcpClient extends Thread {
	private int m_Port;
	private String m_Message;
	private String m_Server;
	private int m_Delay;
	private MessageReceivedEvent m_messageReceived;

	public tcpClient(String server, int port, MessageReceivedEvent messageEvent) {
		m_Port = port;
		m_Server = server;

		this.m_messageReceived = messageEvent;
	}

	public void run() {
		// Connect to the tcp socket and start reading messages
		try {
			Socket skt = new Socket(m_Server, m_Port);
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(skt.getInputStream());
			} catch (IOException ex) {
			}

			while (true) {
				LoggingEvent event = null;
				try {
					event = (LoggingEvent) ois.readObject();
				} catch (Exception ex1) {

					System.out.println(ex1.getMessage());
				}
				m_messageReceived.MessageReceived(event.getLoggerName(), event.getRenderedMessage(), skt);
			}

		} catch (Exception e) {
			System.out.print(e.getMessage());
		}

	}
}

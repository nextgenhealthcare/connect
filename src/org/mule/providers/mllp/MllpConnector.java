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
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package org.mule.providers.mllp;

import org.mule.config.i18n.Message;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.providers.mllp.protocols.DefaultProtocol;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.util.ClassHelper;

public class MllpConnector extends AbstractServiceEnabledConnector {
	public static final int DEFAULT_SOCKET_TIMEOUT = 5000;
	public static final int DEFAULT_BUFFER_SIZE = 64 * 1024;
	public static final long DEFAULT_POLLING_FREQUENCY = 10;
	public static final int DEFAULT_BACKLOG = 256;
	private int timeout = DEFAULT_SOCKET_TIMEOUT;
	private int bufferSize = DEFAULT_BUFFER_SIZE;
	private int backlog = DEFAULT_BACKLOG;
	private String tcpProtocolClassName = DefaultProtocol.class.getName();
	private TcpProtocol tcpProtocol;

	public void doInitialise() throws InitialisationException {
		super.doInitialise();

		if (tcpProtocol == null) {
			try {
				tcpProtocol = (TcpProtocol) ClassHelper.instanciateClass(tcpProtocolClassName, null);
			} catch (Exception e) {
				throw new InitialisationException(new Message("mllp", 3), e);
			}
		}
	}

	public String getProtocol() {
		return "mllp";
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		if (timeout < 0)
			timeout = DEFAULT_SOCKET_TIMEOUT;
		this.timeout = timeout;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		if (bufferSize < 1)
			bufferSize = DEFAULT_BUFFER_SIZE;
		this.bufferSize = bufferSize;
	}

	public int getBacklog() {
		return backlog;
	}

	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	public TcpProtocol getTcpProtocol() {
		return tcpProtocol;
	}

	public void setTcpProtocol(TcpProtocol tcpProtocol) {
		this.tcpProtocol = tcpProtocol;
	}

	public String getTcpProtocolClassName() {
		return tcpProtocolClassName;
	}

	public void setTcpProtocolClassName(String protocolClassName) {
		this.tcpProtocolClassName = protocolClassName;
	}

	public boolean isRemoteSyncEnabled() {
		return true;
	}
}

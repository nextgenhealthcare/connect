package com.webreach.mirth.connectors.sftp;

import org.mule.umo.UMOException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageDispatcherFactory;

public class SftpMessageDispatcherFactory implements UMOMessageDispatcherFactory {
	public UMOMessageDispatcher create(UMOConnector connector) throws UMOException {
		return new SftpMessageDispatcher((SftpConnector) connector);
	}
}

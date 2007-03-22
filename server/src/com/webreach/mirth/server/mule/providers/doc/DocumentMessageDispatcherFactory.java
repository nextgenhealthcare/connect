package com.webreach.mirth.server.mule.providers.doc;

import org.mule.umo.UMOException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageDispatcherFactory;

public class DocumentMessageDispatcherFactory implements UMOMessageDispatcherFactory {

	public UMOMessageDispatcher create(UMOConnector connector) throws UMOException {
		return new DocumentMessageDispatcher((DocumentConnector) connector);
	}

}

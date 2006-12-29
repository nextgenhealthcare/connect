package org.mule.providers.pdf;

import org.mule.umo.UMOException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageDispatcherFactory;

public class PdfMessageDispatcherFactory implements UMOMessageDispatcherFactory {

	public UMOMessageDispatcher create(UMOConnector connector) throws UMOException {
		return new PdfMessageDispatcher((PdfConnector) connector);
	}

}

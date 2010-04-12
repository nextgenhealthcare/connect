package com.mirth.connect.connectors.http;

import org.mule.umo.UMOException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageDispatcherFactory;

public class HttpMessageDispatcherFactory implements UMOMessageDispatcherFactory {
    public UMOMessageDispatcher create(UMOConnector connector) throws UMOException {
        return new HttpMessageDispatcher((HttpConnector) connector);
    }
}

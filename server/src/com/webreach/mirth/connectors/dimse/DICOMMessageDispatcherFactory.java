package com.webreach.mirth.connectors.dimse;

import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageDispatcherFactory;
import org.mule.umo.UMOException;

/**
 * Created by IntelliJ IDEA.
 * Date: Jun 11, 2008
 * Time: 10:23:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class DICOMMessageDispatcherFactory implements UMOMessageDispatcherFactory {
    public UMOMessageDispatcher create(UMOConnector connector) throws UMOException
    {
        return new DICOMMessageDispatcher((DICOMConnector) connector);
    }
}

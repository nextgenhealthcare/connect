/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.webreach.mirth.connectors.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The TcpProtocol interface enables to plug different application level
 * protocols on a TcpConnector.
 * 
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.2 $
 */
public interface TcpProtocol {
    /**
     * Sets the parent tcpConnector
     * 
     * @param tcpConnector
     *            the parent tcpConnector
     */
    public void setTcpConnector(TcpConnector tcpConnector);

    /**
     * Reads the input stream and returns a whole message.
     * 
     * @param is
     *            the input stream
     * @return an array of byte containing a full message
     * @throws IOException
     *             if an exception occurs
     */
    byte[] read(InputStream is) throws IOException;

    /**
     * Write the specified message to the output stream.
     * 
     * @param os
     *            the output stream to write to
     * @param data
     *            the data to write
     * @throws IOException
     *             if an exception occurs
     */
    void write(OutputStream os, byte[] data) throws IOException;
}

/*
 * $Header: /home/projects/mule/scm/mule/providers/tcp/src/java/org/mule/providers/tcp/protocols/LengthProtocol.java,v 1.2 2005/06/03 01:20:35 gnt Exp $
 * $Revision: 1.2 $
 * $Date: 2005/06/03 01:20:35 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package org.mule.providers.tcp.protocols;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.mule.providers.tcp.TcpProtocol;

/**
 * The LengthProtocol is an application level tcp protocol that can be used to
 * transfer large amounts of data without risking some data to be loss. The
 * protocol is defined by sending / reading an integer (the packet length) and
 * then the data to be transfered.
 * 
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.2 $
 */
public class LengthProtocol implements TcpProtocol
{

    public byte[] read(InputStream is) throws IOException
    {
        // Use a mark / reset here so that an exception
        // will not be thrown is the read times out.
        // So use the read(byte[]) method that returns 0
        // if no data can be read and reset the mark.
        // This is necessary because when no data is available
        // reading an int would throw a SocketTimeoutException.
        DataInputStream dis = new DataInputStream(is);
        byte[] buffer = new byte[32];
        int length;
        dis.mark(32);
        while ((length = dis.read(buffer)) == 0) {
        }
        if (length == -1) {
            return null;
        }
        dis.reset();
        length = dis.readInt();
        buffer = new byte[length];
        dis.readFully(buffer);
        return buffer;
    }

    public void write(OutputStream os, byte[] data) throws IOException
    {
        // Write the length and then the data.
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeInt(data.length);
        dos.write(data);
        dos.flush();
    }

}

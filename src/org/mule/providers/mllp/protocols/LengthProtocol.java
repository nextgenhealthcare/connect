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


package org.mule.providers.mllp.protocols;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.mule.providers.mllp.TcpProtocol;

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

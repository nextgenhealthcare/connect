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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.providers.mllp.TcpProtocol;

/**
 * The DefaultProtocol class is an application level tcp protocol that does
 * nothing. Reading is performed in reading the socket until no more bytes are
 * available. Writing simply writes the data to the socket.
 *
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.4 $
 */
public class MllpProtocol implements TcpProtocol
{

    private static final int BUFFER_SIZE = 8192;

    private static final Log logger = LogFactory.getLog(MllpProtocol.class);

    public byte[] read(InputStream is) throws IOException
    {
        ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];
        int len = 0;
        try {
            while ((len = is.read(buffer)) == 0) {
            }
        } catch (SocketException e) {
            // do not pollute the log with a stacktrace, log only the message
            logger.debug("Socket exception occured: " + e.getMessage());
            return null;
        } catch (SocketTimeoutException e) {
            logger.debug("Socket timeout, returning null.");
            return null;
        }
        if (len == -1) {
            return null;
        } else {
            do {
                baos.write(buffer, 0, len);
                if (len < buffer.length) {
                    break;
                }
                int av = is.available();
                if (av == 0) {
                    break;
                }
            } while ((len = is.read(buffer)) > 0);

            baos.flush();
            baos.close();
            return baos.toByteArray();
        }
    }

    public void write(OutputStream os, byte[] data) throws IOException
    {
        os.write(data);
    }

}

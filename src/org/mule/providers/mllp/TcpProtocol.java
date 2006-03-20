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
public interface TcpProtocol
{

    /**
     * Reads the input stream and returns a whole message.
     *
     * @param is the input stream
     * @return an array of byte containing a full message
     * @throws IOException if an exception occurs
     */
    byte[] read(InputStream is) throws IOException;

    /**
     * Write the specified message to the output stream.
     *
     * @param os the output stream to write to
     * @param data the data to write
     * @throws IOException if an exception occurs
     */
    void write(OutputStream os, byte[] data) throws IOException;
}

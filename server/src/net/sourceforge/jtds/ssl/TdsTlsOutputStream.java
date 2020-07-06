// jTDS JDBC Driver for Microsoft SQL Server and Sybase
//Copyright (C) 2004 The jTDS Project
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package net.sourceforge.jtds.ssl;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.jtds.jdbc.TdsCore;

/**
 * An output stream that mediates between JSSE and the DB server.
 * <p/>
 * SQL Server 2000 has the following requirements:
 * <ul>
 *   <li>All handshake records are delivered in TDS packets.
 *   <li>The "Client Key Exchange" (CKE), "Change Cipher Spec" (CCS) and
 *     "Finished" (FIN) messages are to be submitted in the delivered in both
 *     the same TDS packet and the same TCP packet.
 *   <li>From then on TLS/SSL records should be transmitted as normal -- the
 *     TDS packet is part of the encrypted application data.
 *
 * @author Rob Worsnop
 * @author Mike Hutchinson
 * @version $Id: TdsTlsOutputStream.java,v 1.4 2005-04-28 14:29:31 alin_sinpalean Exp $
 */
class TdsTlsOutputStream extends FilterOutputStream {
    /**
     * Used for holding back CKE, CCS and FIN records.
     */
    final private List bufferedRecords = new ArrayList();
    private int totalSize;

    /**
     * Constructs a TdsTlsOutputStream based on an underlying output stream.
     *
     * @param out the underlying output stream
     */
    TdsTlsOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Holds back a record for batched transmission.
     *
     * @param record the TLS record to buffer
     * @param len    the length of the TLS record to buffer
     */
    private void deferRecord(byte record[], int len) {
        byte tmp[] = new byte[len];
        System.arraycopy(record, 0, tmp, 0, len);
        bufferedRecords.add(tmp);
        totalSize += len;
    }

    /**
     * Transmits the buffered batch of records.
     */
    private void flushBufferedRecords() throws IOException {
        byte tmp[] = new byte[totalSize];
        int off = 0;
        for (int i = 0; i < bufferedRecords.size(); i++) {
            byte x[] = (byte[])bufferedRecords.get(i);
            System.arraycopy(x, 0, tmp, off, x.length);
            off += x.length;
        }
        putTdsPacket(tmp, off);
        bufferedRecords.clear();
        totalSize = 0;
    }

    public void write(byte[] b, int off, int len) throws IOException {

        if (len < Ssl.TLS_HEADER_SIZE || off > 0) {
            // Too short for a TLS packet just write it
            out.write(b, off, len);
            return;
        }
        //
        // Extract relevant TLS header fields
        //
        int contentType = b[0] & 0xFF;
        int length  = ((b[3] & 0xFF) << 8) | (b[4] & 0xFF);
        //
        // Check to see if probably a SSL client hello
        //
        if (contentType < Ssl.TYPE_CHANGECIPHERSPEC ||
            contentType > Ssl.TYPE_APPLICATIONDATA ||
            length != len - Ssl.TLS_HEADER_SIZE) {
            // Assume SSLV2 Client Hello
            putTdsPacket(b, len);
            return;
        }
        //
        // Process TLS records
        //
        switch (contentType) {

            case Ssl.TYPE_APPLICATIONDATA:
                // Application data, just copy to output
                out.write(b, off, len);
                break;

            case Ssl.TYPE_CHANGECIPHERSPEC:
                // Cipher spec change has to be buffered
                deferRecord(b, len);
                break;

            case Ssl.TYPE_ALERT:
                // Alert record ignore!
                break;

            case Ssl.TYPE_HANDSHAKE:
                // TLS Handshake records
                if (len >= (Ssl.TLS_HEADER_SIZE + Ssl.HS_HEADER_SIZE)) {
                    // Long enough for a handshake subheader
                    int hsType = b[5];
                    int hsLen  = (b[6] & 0xFF) << 16 |
                                 (b[7] & 0xFF) << 8  |
                                 (b[8] & 0xFF);

                    if (hsLen == len - (Ssl.TLS_HEADER_SIZE + Ssl.HS_HEADER_SIZE) &&
                        // Client hello has to go in its own TDS packet
                        hsType == Ssl.TYPE_CLIENTHELLO) {
                        putTdsPacket(b, len);
                        break;
                    }
                    // All others have to be deferred and sent as a block
                    deferRecord(b, len);
                    //
                    // Now see if we have a finish record which will flush the
                    // buffered records.
                    //
                    if (hsLen != len - (Ssl.TLS_HEADER_SIZE + Ssl.HS_HEADER_SIZE) ||
                        hsType != Ssl.TYPE_CLIENTKEYEXCHANGE) {
                        // This is probably a finish record
                        flushBufferedRecords();
                    }
                    break;
                }
            default:
                // Short or unknown record output it anyway
                out.write(b, off, len);
                break;
        }
    }

    /**
     * Write a TDS packet containing the TLS record(s).
     *
     * @param b   the TLS record
     * @param len the length of the TLS record
     */
    void putTdsPacket(byte[] b, int len) throws IOException {
        byte tdsHdr[] = new byte[TdsCore.PKT_HDR_LEN];
        tdsHdr[0] = TdsCore.PRELOGIN_PKT;
        tdsHdr[1] = 0x01;
        tdsHdr[2] = (byte)((len + TdsCore.PKT_HDR_LEN) >> 8);
        tdsHdr[3] = (byte)(len + TdsCore.PKT_HDR_LEN);
        out.write(tdsHdr, 0, tdsHdr.length);
        out.write(b, 0, len);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.OutputStream#flush()
     */
    public void flush() throws IOException {
        super.flush();
    }

}

